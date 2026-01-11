import http from "k6/http";
import { check } from "k6";

const baseUrl = __ENV.BASE_URL || "http://10.0.2.6:8080";
const emailPrefix = __ENV.EMAIL_PREFIX || "loadtest.user";
const emailDomain = __ENV.EMAIL_DOMAIN || "example.com";
const password = __ENV.PASSWORD || "Passw0rd!123";

const restaurantId = Number(__ENV.RESTAURANT_ID || 4);
const slotDate = __ENV.SLOT_DATE || "2026-01-16";
const slotTime = __ENV.SLOT_TIME || "12:00";
const partySize = Number(__ENV.PARTY_SIZE || 4);
const reservationType = __ENV.RESERVATION_TYPE || "RESERVATION_DEPOSIT";

const loadVus = Number(__ENV.LOAD_VUS || 10);
const loadDuration = __ENV.LOAD_DURATION || "30s";

export const options = {
  scenarios: {
    reservations: {
      executor: "per-vu-iterations",
      vus: loadVus,
      iterations: 1,
      maxDuration: loadDuration,
    },
  },
};

function buildEmail(vu) {
  const seq = String(vu).padStart(4, "0");
  return `${emailPrefix}${seq}@${emailDomain}`;
}

export default function () {
  const vu = __VU;
  const loginPayload = JSON.stringify({
    email: buildEmail(vu),
    password,
    userType: "USER",
  });

  const loginRes = http.post(`${baseUrl}/api/login`, loginPayload, {
    headers: { "Content-Type": "application/json" },
  });

  check(loginRes, {
    "login status 200": (r) => r.status === 200,
  });

  if (loginRes.status !== 200) {
    return;
  }

  const loginBody = loginRes.json();
  const token = loginBody.accessToken;
  const userId = loginBody.id;

  const reservePayload = JSON.stringify({
    userId,
    restaurantId,
    slotDate,
    slotTime,
    partySize,
    reservationType,
    requestMessage: null,
  });

  const reserveRes = http.post(`${baseUrl}/api/reservations`, reservePayload, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  check(reserveRes, {
    "reservation status 2xx/4xx": (r) => r.status >= 200 && r.status < 500,
  });
}
