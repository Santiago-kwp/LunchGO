import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "http://10.0.2.6:8080";
const emailPrefix = __ENV.EMAIL_PREFIX || "loadtest.user";
const emailDomain = __ENV.EMAIL_DOMAIN || "example.com";
const password = __ENV.PASSWORD || "Passw0rd!123";
const loadVus = Number(__ENV.LOAD_VUS || 10);
const loadDuration = __ENV.LOAD_DURATION || "1m";
const loadIterations = Number(__ENV.LOAD_ITERATIONS || 1);
const loadMode = String(__ENV.LOAD_MODE || "constant-vus").toLowerCase();
const useLoginQueue =
  String(__ENV.USE_LOGIN_QUEUE || "").toLowerCase() === "true" ||
  String(__ENV.USE_LOGIN_QUEUE || "") === "1";
const queuePollIntervalMs = Number(__ENV.LOGIN_QUEUE_POLL_MS || 1000);
const queueMaxWaitMs = Number(__ENV.LOGIN_QUEUE_MAX_WAIT_MS || 60000);

export const options = {
  scenarios: {
    login:
      loadMode === "one-shot"
        ? {
            executor: "per-vu-iterations",
            vus: loadVus,
            iterations: loadIterations,
            maxDuration: loadDuration,
          }
        : {
            executor: "constant-vus",
            vus: loadVus,
            duration: loadDuration,
          },
  },
};

function buildEmail(vu) {
  const seq = String(vu).padStart(4, "0");
  return `${emailPrefix}${seq}@${emailDomain}`;
}

function getQueueStatus(token) {
  return http.get(`${baseUrl}/api/login/queue?token=${encodeURIComponent(token)}`);
}

function waitForLoginTurn() {
  const joinRes = http.post(`${baseUrl}/api/login/queue`, JSON.stringify({}), {
    headers: { "Content-Type": "application/json" },
  });

  check(joinRes, {
    "queue join 200": (r) => r.status === 200,
  });

  if (joinRes.status !== 200) {
    return { ok: false, queueToken: null, reason: "join_failed" };
  }

  const joinData = joinRes.json() || {};
  const queueToken = joinData.queueToken || null;
  if (joinData.allowed) {
    return { ok: true, queueToken, reason: "allowed" };
  }

  if (!queueToken) {
    return { ok: false, queueToken: null, reason: "token_missing" };
  }

  const startTime = Date.now();
  while (Date.now() - startTime < queueMaxWaitMs) {
    sleep(queuePollIntervalMs / 1000);
    const statusRes = getQueueStatus(queueToken);
    if (statusRes.status !== 200) {
      return { ok: false, queueToken, reason: "status_failed" };
    }
    const statusData = statusRes.json() || {};
    if (statusData.allowed) {
      return { ok: true, queueToken, reason: "allowed" };
    }
    if (statusData.expired) {
      return { ok: false, queueToken, reason: "expired" };
    }
  }

  return { ok: false, queueToken, reason: "timeout" };
}

export default function () {
  let queueToken = null;
  if (useLoginQueue) {
    const queueResult = waitForLoginTurn();
    if (!queueResult.ok) {
      const reason = queueResult.reason || "unknown";
      check({ reason }, {
        "queue allowed": (r) => r.reason === "allowed",
        "queue join failed": (r) => r.reason === "join_failed",
        "queue token missing": (r) => r.reason === "token_missing",
        "queue status failed": (r) => r.reason === "status_failed",
        "queue expired": (r) => r.reason === "expired",
        "queue timeout": (r) => r.reason === "timeout",
      });
      return;
    }
    queueToken = queueResult.queueToken;
    check({ reason: queueResult.reason }, {
      "queue allowed": (r) => r.reason === "allowed",
    });
  }

  const payload = {
    email: buildEmail(__VU),
    password,
    userType: "USER",
  };
  if (queueToken) {
    payload.queueToken = queueToken;
  }

  const loginPayload = JSON.stringify(payload);

  const res = http.post(`${baseUrl}/api/login`, loginPayload, {
    headers: { "Content-Type": "application/json" },
  });

  check(res, {
    "login status 200": (r) => r.status === 200,
  });
}

export function handleSummary(data) {
  const checks = data.root_group?.checks || [];
  const pickRate = (name) => {
    const entry = checks.find((check) => check.name === name);
    if (!entry) return null;
    const passes = entry.passes || 0;
    const fails = entry.fails || 0;
    const total = passes + fails;
    if (!total) return null;
    return ((passes / total) * 100).toFixed(2);
  };

  const summary = {
    queue_allowed_rate: pickRate("queue allowed"),
    queue_timeout_rate: pickRate("queue timeout"),
    queue_expired_rate: pickRate("queue expired"),
    queue_join_failed_rate: pickRate("queue join failed"),
    queue_token_missing_rate: pickRate("queue token missing"),
    queue_status_failed_rate: pickRate("queue status failed"),
  };

  return {
    stdout: `${JSON.stringify(summary, null, 2)}\n`,
  };
}
