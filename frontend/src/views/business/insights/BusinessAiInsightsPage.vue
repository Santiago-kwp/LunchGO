<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import httpRequest from "@/router/httpRequest";
import BusinessSidebar from "@/components/ui/BusinessSideBar.vue";
import BusinessHeader from "@/components/ui/BusinessHeader.vue";
import { useAccountStore } from "@/stores/account";
import { Line, Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Tooltip,
  Legend
);

const route = useRoute();
const router = useRouter();
const restaurantId = ref(Number(route.query.restaurantId || 0));

const accountStore = useAccountStore();

const getStoredMember = () => {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("member");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());

const userRole = computed(() => {
  if (member.value?.role === "ROLE_OWNER") return "owner";
  if (member.value?.role === "ROLE_STAFF") return "staff";
  return "";
});

const insight = ref(null);
const isLoading = ref(false);
const errorMessage = ref("");

const ensureRestaurantId = async () => {
  if (restaurantId.value) return restaurantId.value;
  try {
    const res = await httpRequest.get("/api/business/me/restaurant");
    const rid = res.data?.restaurantId;
    if (rid) {
      await router.replace({
        query: { ...route.query, restaurantId: String(rid) },
      });
      return Number(rid);
    }
  } catch (error) {
    console.error("사업자 restaurantId 조회 실패:", error);
  }
  return 0;
};

const loadInsights = async () => {
  const rid = await ensureRestaurantId();
  if (!rid) {
    errorMessage.value = "사업자 권한이 필요합니다.";
    return;
  }
  isLoading.value = true;
  errorMessage.value = "";
  try {
    const response = await httpRequest.get(
      `/api/business/restaurants/${rid}/stats/weekly`
    );
    insight.value = response.data;

    // [MEDIUM] 디버깅 목적으로 사용된 console.log 문입니다.
    // 프로덕션 코드에 포함되지 않도록 병합 전에 제거하는 것이 좋습니다.
    // 브라우저 콘솔에 불필요한 로그가 출력되는 것을 방지하고 코드를 깔끔하게 유지할 수 있습니다.
    // console.log("=== AI 인사이트 데이터 디버깅 ===");
    // console.log("예측 주 시작일:", insight.value?.predictionWeekStart);
    // console.log("예측 주 종료일:", insight.value?.predictionWeekEnd);
    // console.log("예측 데이터 개수:", insight.value?.predictions?.length || 0);
    // console.log("예측 데이터 상세:", insight.value?.predictions);

    // // 각 예측 데이터의 날짜 확인
    // if (insight.value?.predictions && insight.value?.predictionWeekStart) {
    //   const weekStart = new Date(insight.value.predictionWeekStart);
    //   console.log("예측 주 시작일 (Date 객체):", weekStart);
    //   insight.value.predictions.forEach((pred) => {
    //     const mondayIndex = 2;
    //     const offset = pred.weekday === 1 ? 6 : pred.weekday - mondayIndex;
    //     const date = new Date(weekStart);
    //     date.setDate(date.getDate() + offset);
    //     const dateStr = `${date.getFullYear()}-${String(
    //       date.getMonth() + 1
    //     ).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    //     console.log(
    //       `요일 ${pred.weekday} (${formatWeekday(
    //         pred.weekday
    //       )}): ${dateStr}, 예측: ${pred.expectedMin}~${pred.expectedMax}`
    //     );
    //   });
    // }
    // console.log("================================");
  } catch (error) {
    console.error("AI 인사이트 조회 실패:", error);
    errorMessage.value = "AI 인사이트를 불러오지 못했습니다.";
  } finally {
    isLoading.value = false;
  }
};

const downloadWeeklyPdf = () => {
  window.print();
};

const isWeeklyReportLoading = ref(false);

const downloadWeeklyReport = async () => {
  const rid = await ensureRestaurantId();
  if (!rid) return alert("권한이 없습니다.");

  const token = localStorage.getItem("accessToken");
  if (!token) {
    window.alert("로그인이 필요합니다.");
    return;
  }

  if (userRole.value === "ROLE_STAFF")
    return alert("사업자만 요약서 확인이 가능합니다.");

  if (isWeeklyReportLoading.value) return;
  isWeeklyReportLoading.value = true;

  try {
    const response = await httpRequest.get(
      `/api/business/restaurants/${rid}/stats/weekly.pdf`,
      null,
      { responseType: "blob" }
    );

    const blob = response.data;
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `LunchGo-weekly-stats-${rid}.pdf`;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
  } catch (error) {
    const status = error?.response?.status ?? "unknown";
    window.alert(`문제가 발생했습니다. code: ${status}`);
  } finally {
    isWeeklyReportLoading.value = false;
  }
};

onMounted(loadInsights);

const pickSummaryIcon = (line) => {
  if (line.includes("예약")) return "📌";
  if (line.includes("매출") || line.includes("금액")) return "💰";
  if (line.includes("구내식당")) return "🍽️";
  if (line.includes("취향")) return "❤️";
  if (line.includes("불일치")) return "⚠️";
  if (line.includes("공유")) return "🔗";
  if (line.includes("회사")) return "🏢";
  return "•";
};

const formatSummaryLines = (body) => {
  if (!body) return [];
  const cleaned = cleanSummaryBody(body)
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean);
  return cleaned.map((line) => ({
    text: line,
    icon: pickSummaryIcon(line),
  }));
};

const formatKeyword = (keyword) => {
  const map = {
    오이: "🥒",
    고수: "🌿",
    해물: "🦐",
    치즈: "🧀",
    "인스타 감성": "📸",
    매운: "🌶️",
    고기: "🥩",
    채식: "🥗",
    돈가스: "🍱",
    카츠: "🍱",
    로스카츠: "🍱",
    치즈카츠: "🧀",
    카레카츠: "🍛",
    미니우동: "🍜",
    새우튀김: "🍤",
    "새우튀김 2마리": "🍤",
    치킨가라아게: "🍗",
    콜라: "🥤",
    돈가스김치나베: "🍲",
  };
  if (!keyword) return "";
  const emoji = map[keyword] || "🔖";
  return `${emoji} ${keyword}`;
};

const formatConfidenceBadge = (confidence) => {
  const value = confidence || "LOW";
  return {
    label: `신뢰도: ${value}`,
    tone:
      value === "HIGH"
        ? "badge-high"
        : value === "MEDIUM"
        ? "badge-mid"
        : "badge-low",
  };
};

const summarySections = computed(() => {
  const summary = insight.value?.aiSummary || "";
  if (!summary) return [];
  const normalized = summary
    .replace(/\r/g, "")
    .replace(/\n## /g, "\n@@SECTION@@");
  const prepared = normalized.startsWith("## ")
    ? "@@SECTION@@" + normalized.slice(3)
    : normalized;
  return prepared
    .split("@@SECTION@@")
    .map((section) => section.trim())
    .filter(Boolean)
    .map((section) => {
      const [title, ...rest] = section.split("\n");
      return {
        title: title.trim(),
        lines: formatSummaryLines(rest.join("\n")),
      };
    })
    .filter((section) => section.title && section.title !== "금주 방문 예측")
    .filter((section) => section.lines.length > 0);
});

const recommendationSection = computed(() => {
  return summarySections.value.find((section) =>
    section.title.includes("통합 분석")
  );
});

const reservations = computed(() => insight.value?.reservations || []);
const stats = computed(() => insight.value?.stats || []);
const predictions = computed(() => insight.value?.predictions || []);
const lastWeekPredictions = computed(
  () => insight.value?.lastWeekPredictions || []
);

const reservationChartData = computed(() => {
  if (!reservations.value.length) return null;

  const labels = reservations.value.map((point) => point.date);
  const datasets = [
    {
      label: "예약 건수",
      data: reservations.value.map((point) => point.count),
      borderColor: "#FF6B4A",
      backgroundColor: "rgba(255,107,74,0.15)",
      pointBackgroundColor: "#FF6B4A",
      tension: 0.35,
    },
  ];

  // 저번 주 예측 데이터 추가
  if (lastWeekPredictions.value.length > 0) {
    // 저번 주 예측 데이터를 날짜별로 매핑
    const lastWeekDataMap = new Map();
    lastWeekPredictions.value.forEach((pred) => {
      if (pred.date) {
        lastWeekDataMap.set(pred.date, pred.expectedAvg);
      }
    });

    // 예약 데이터의 각 날짜에 대해 저번 주 같은 요일의 예측 데이터 찾기
    const lastWeekDataArray = labels.map((date) => {
      // 날짜 문자열을 파싱 (YYYY-MM-DD 형식)
      const [year, month, day] = date.split("-").map(Number);
      // 로컬 타임존으로 날짜 객체 생성 (타임존 문제 방지)
      const dateObj = new Date(year, month - 1, day);

      // 저번 주 같은 요일의 날짜 계산 (정확히 7일 전)
      dateObj.setDate(dateObj.getDate() - 7);

      // YYYY-MM-DD 형식으로 변환
      const lastWeekYear = dateObj.getFullYear();
      const lastWeekMonth = String(dateObj.getMonth() + 1).padStart(2, "0");
      const lastWeekDay = String(dateObj.getDate()).padStart(2, "0");
      const lastWeekDateStr = `${lastWeekYear}-${lastWeekMonth}-${lastWeekDay}`;

      const value = lastWeekDataMap.get(lastWeekDateStr);
      return value !== undefined ? value : null;
    });

    datasets.push({
      label: "저번 주 AI 예측 건수",
      data: lastWeekDataArray,
      borderColor: "#9CA3AF",
      backgroundColor: "rgba(156,163,175,0.15)",
      pointBackgroundColor: "#9CA3AF",
      borderDash: [5, 5],
      tension: 0.35,
    });
  }

  return {
    labels,
    datasets,
  };
});

const revenueChartData = computed(() => {
  if (!reservations.value.length) return null;
  return {
    labels: reservations.value.map((point) => point.date),
    datasets: [
      {
        label: "예약 금액",
        data: reservations.value.map((point) => point.amount),
        borderColor: "#6366F1",
        backgroundColor: "rgba(99,102,241,0.12)",
        pointBackgroundColor: "#6366F1",
        tension: 0.35,
      },
    ],
  };
});

const predictionChartData = computed(() => {
  if (!predictions.value.length) return null;
  // 월요일(2)부터 시작하도록 정렬: 일요일(1)을 맨 뒤로
  const sorted = [...predictions.value].sort((a, b) => {
    // 일요일(1)을 맨 뒤로 보내기 위해 8로 변환
    const aWeekday = a.weekday === 1 ? 8 : a.weekday;
    const bWeekday = b.weekday === 1 ? 8 : b.weekday;
    return aWeekday - bWeekday;
  });
  const weekStart = insight.value?.predictionWeekStart
    ? new Date(insight.value.predictionWeekStart)
    : null;

  // 요일과 날짜를 함께 표시하는 라벨 생성
  const labels = sorted.map((p) => {
    const weekday = formatWeekday(p.weekday);
    if (weekStart && !Number.isNaN(weekStart.getTime())) {
      const mondayIndex = 2;
      const offset = p.weekday === 1 ? 6 : p.weekday - mondayIndex;
      const date = new Date(weekStart);
      date.setDate(date.getDate() + offset);
      const month = date.getMonth() + 1;
      const day = date.getDate();
      return `${weekday} (${month}/${day})`;
    }
    return weekday;
  });

  return {
    labels,
    datasets: [
      {
        label: "예상 최소",
        data: sorted.map((p) => p.expectedMin),
        borderColor: "#FF9A62",
        backgroundColor: "rgba(255,154,98,0.12)",
        tension: 0.35,
      },
      {
        label: "예상 최대",
        data: sorted.map((p) => p.expectedMax),
        borderColor: "#1E3A5F",
        backgroundColor: "rgba(30,58,95,0.12)",
        tension: 0.35,
      },
    ],
  };
});

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { position: "bottom" },
  },
  scales: {
    y: { beginAtZero: true },
  },
};

const barChartOptions = {
  responsive: true,
  indexAxis: "y",
  plugins: {
    legend: { position: "bottom" },
  },
  scales: {
    x: { beginAtZero: true },
  },
};

const formatWeekday = (weekday) => {
  switch (weekday) {
    case 1:
      return "일";
    case 2:
      return "월";
    case 3:
      return "화";
    case 4:
      return "수";
    case 5:
      return "목";
    case 6:
      return "금";
    case 7:
      return "토";
    default:
      return String(weekday);
  }
};

const signalSummary = computed(() => insight.value?.signalSummary || {});

const funnelTotals = computed(() => {
  if (!stats.value.length) return null;
  return stats.value.reduce(
    (acc, row) => {
      acc.view += row.viewCount ?? 0;
      acc.try += row.tryCount ?? 0;
      acc.confirm += row.confirmCount ?? 0;
      acc.visit += row.visitCount ?? 0;
      return acc;
    },
    { view: 0, try: 0, confirm: 0, visit: 0 }
  );
});

const funnelStages = computed(() => {
  const total = funnelTotals.value;
  if (!total) return [];
  const max = Math.max(total.view, 1);
  return [
    { label: "조회", value: total.view, color: "#FFB199" },
    { label: "시도", value: total.try, color: "#FFC4B8" },
    { label: "확정", value: total.confirm, color: "#FF6B4A" },
    { label: "방문", value: total.visit, color: "#1E3A5F" },
  ].map((stage) => ({
    ...stage,
    width: Math.round((stage.value / max) * 100),
  }));
});

const mismatchChartData = computed(() => {
  const mismatch = signalSummary.value?.mismatchDates || [];
  const weekStart = insight.value?.predictionWeekStart;
  const weekEnd = insight.value?.predictionWeekEnd;
  if (!weekStart || !weekEnd) return null;
  const mismatchMap = new Map();
  mismatch.forEach((entry) => {
    const [date, count] = String(entry).split(":");
    mismatchMap.set(date, Number(count || 0));
  });
  const labels = [];
  const values = [];
  let cursor = new Date(weekStart);
  const end = new Date(weekEnd);
  while (cursor <= end) {
    const date = cursor.toISOString().slice(0, 10);
    labels.push(date);
    values.push(mismatchMap.get(date) ?? 0);
    cursor.setDate(cursor.getDate() + 1);
  }
  return {
    labels,
    datasets: [
      {
        label: "비선호 키워드 건수",
        data: values,
        backgroundColor: "#FF8A5B",
        borderRadius: 8,
      },
    ],
  };
});

const conversionRate = computed(() => {
  if (!stats.value.length) return 0;
  const totals = stats.value.reduce(
    (acc, row) => {
      acc.confirm += row.confirmCount ?? 0;
      acc.visit += row.visitCount ?? 0;
      return acc;
    },
    { confirm: 0, visit: 0 }
  );
  if (totals.confirm === 0) return 0;
  return Math.round((totals.visit / totals.confirm) * 100);
});

const predictionDetails = computed(() => {
  if (!predictions.value.length) return [];
  // 월요일(2)부터 시작하도록 정렬: 일요일(1)을 맨 뒤로
  const sorted = [...predictions.value].sort((a, b) => {
    // 일요일(1)을 맨 뒤로 보내기 위해 8로 변환
    const aWeekday = a.weekday === 1 ? 8 : a.weekday;
    const bWeekday = b.weekday === 1 ? 8 : b.weekday;
    return aWeekday - bWeekday;
  });
  const weekStart = insight.value?.predictionWeekStart
    ? new Date(insight.value.predictionWeekStart)
    : null;
  const dateForWeekday = (weekday) => {
    if (!weekStart || Number.isNaN(weekStart.getTime())) return "";
    const mondayIndex = 2;
    const offset = weekday === 1 ? 6 : weekday - mondayIndex;
    const date = new Date(weekStart);
    date.setDate(date.getDate() + offset);
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  };
  return sorted.map((row) => {
    const evidence = row.evidence?.filter(Boolean) || [];
    const evidenceText = evidence.slice(0, 2).join(", ");
    return {
      weekday: formatWeekday(row.weekday),
      date: dateForWeekday(row.weekday),
      range: `${row.expectedMin} ~ ${row.expectedMax}건`,
      confidence: formatConfidenceBadge(row.confidence),
      evidence: evidenceText,
    };
  });
});

const restaurantMatchNote = computed(() => {
  const menuKeywords = signalSummary.value?.restaurantMenuKeywords || [];
  const overlap = signalSummary.value?.restaurantMenuOverlap ?? 0;
  if (!menuKeywords.length) {
    return "우리 식당 메뉴/취향 매칭 지수 N/A";
  }
  const ratio = overlap / menuKeywords.length;
  const score = Number.isFinite(ratio) ? (0.9 + 0.2 * ratio).toFixed(2) : "N/A";
  return `우리 식당 메뉴/취향 매칭 지수 ${score}`;
});

const weekdayTokens = [
  "월요일",
  "화요일",
  "수요일",
  "목요일",
  "금요일",
  "토요일",
  "일요일",
];

const stripMarkdown = (text) => {
  if (!text) return "";
  return text
    .replace(/\*\*(.+?)\*\*/g, "$1")
    .replace(/\*(.+?)\*/g, "$1")
    .replace(/`(.+?)`/g, "$1")
    .replace(/_{1,2}(.+?)_{1,2}/g, "$1");
};

const shouldDropLine = (line) => {
  const trimmed = line.trim();
  if (!trimmed) return false;
  const isBullet = /^[-*•]\s+/.test(trimmed);
  if (!isBullet) return false;
  if (/\d{4}-\d{2}-\d{2}/.test(trimmed)) return true;
  if (/\d{1,2}월/.test(trimmed)) return true;
  if (weekdayTokens.some((token) => trimmed.includes(token))) return true;
  if (/\d+\s*~\s*\d+/.test(trimmed)) return true;
  return false;
};

const cleanSummaryBody = (body) => {
  if (!body) return "";
  const lines = stripMarkdown(body)
    .split("\n")
    .map((line) => line.replace(/^\s*[-*•]\s+/, "").trim())
    .filter((line) => line.length > 0)
    .filter((line) => !shouldDropLine(line));
  return lines.join("\n");
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <div class="print-hide">
      <BusinessSidebar activeMenu="ai-insights" />
    </div>

    <div class="flex-1 flex flex-col overflow-hidden">
      <div class="print-hide">
        <BusinessHeader />
      </div>

      <main class="flex-1 overflow-y-auto p-8 print-area">
        <div class="max-w-7xl mx-auto space-y-8">
          <div class="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h2 class="text-3xl font-bold text-[#1e3a5f]">
                AI 인사이트/예측
              </h2>
              <p class="text-sm text-[#6c757d] mt-1">
                {{ insight?.startDate }} ~ {{ insight?.endDate }}
              </p>
              <p class="text-xs text-[#9ca3af] mt-1">
                ⏰ AI 인사이트는 매일 자정에 한 번 업데이트됩니다.
              </p>
            </div>
            <button
              type="button"
              @click="downloadWeeklyReport"
              :disabled="isWeeklyReportLoading"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-semibold text-white bg-gradient-to-r from-[#6366F1] via-[#EC4899] to-[#F97316] transition-opacity',
                isWeeklyReportLoading
                  ? 'opacity-70 cursor-not-allowed'
                  : 'hover:opacity-90 cursor-pointer',
              ]"
            >
              <span
                v-if="isWeeklyReportLoading"
                class="inline-flex items-center gap-2"
              >
                <span class="loading-spinner"></span>
                AI 요약 생성 중...
              </span>
              <span v-else>AI 요약 분석서 PDF 다운로드</span>
            </button>
          </div>

          <div
            v-if="isLoading"
            class="bg-white rounded-xl border border-[#e9ecef] p-10 text-[#1e3a5f]"
          >
            <div class="insight-loading">
              <div class="insight-spinner"></div>
              <div class="insight-loading-text">
                <p class="text-lg font-semibold">AI 인사이트 추론 중</p>
                <p class="text-sm text-[#6c757d] mt-2">
                  요약과 예측을 생성하는 중입니다. 잠시만 기다려주세요.
                </p>
              </div>
            </div>
          </div>

          <div
            v-else-if="errorMessage"
            class="bg-white rounded-xl border border-[#e9ecef] p-6 text-sm text-[#dc3545]"
          >
            {{ errorMessage }}
          </div>

          <div v-else class="space-y-8">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div
                class="bg-white rounded-xl border border-[#e9ecef] p-6 lg:col-span-2"
              >
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">AI 요약</h3>
                <div
                  v-if="insight?.aiFallbackUsed"
                  class="text-sm text-[#dc3545] mb-3"
                >
                  AI 요약 실패로 규칙 기반 요약을 표시합니다.
                </div>
                <div class="space-y-4">
                  <div v-for="section in summarySections" :key="section.title">
                    <h4 class="text-sm font-semibold text-[#1e3a5f] mb-1">
                      {{ section.title }}
                    </h4>
                    <ul class="space-y-2">
                      <li
                        v-for="(line, idx) in section.lines"
                        :key="idx"
                        class="summary-line"
                      >
                        <span class="summary-icon">{{ line.icon }}</span>
                        <span class="summary-text">{{ line.text }}</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">신호 요약</h3>
                <div class="space-y-3 text-sm text-[#1e3a5f]">
                  <div>
                    공개 북마크: {{ signalSummary.publicBookmarkCount ?? 0 }}
                  </div>
                  <div>
                    공유 링크: {{ signalSummary.approvedLinkCount ?? 0 }}
                  </div>
                  <div>
                    우리 식당 메뉴 키워드:
                    {{
                      signalSummary.restaurantMenuKeywords
                        ?.map(formatKeyword)
                        .join(", ") || "없음"
                    }}
                  </div>
                  <div>
                    우리 식당 메뉴/취향 겹침:
                    {{ signalSummary.restaurantMenuOverlap ?? 0 }}
                  </div>
                  <div>
                    구내식당 메뉴 키워드:
                    {{
                      signalSummary.menuKeywords
                        ?.map(formatKeyword)
                        .join(", ") || "없음"
                    }}
                  </div>
                  <div>
                    사용자 취향 키워드:
                    {{
                      signalSummary.preferenceKeywords
                        ?.map(formatKeyword)
                        .join(", ") || "없음"
                    }}
                  </div>
                  <div>
                    키워드 겹침: {{ signalSummary.keywordOverlap ?? 0 }}
                  </div>
                  <div>
                    불일치 높은 날짜:
                    {{ signalSummary.mismatchDates?.join(", ") || "없음" }}
                  </div>
                  <div>
                    구내식당 미운영 날짜:
                    {{ signalSummary.noMenuDates?.join(", ") || "없음" }}
                  </div>
                  <div v-if="signalSummary.topCompanyName">
                    상위 회사: {{ signalSummary.topCompanyName }} ({{
                      Math.round(signalSummary.topCompanyShare * 100)
                    }}%)
                  </div>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">예약 건수</h3>
                <Line
                  v-if="reservationChartData"
                  :data="reservationChartData"
                  :options="chartOptions"
                />
                <p v-else class="text-sm text-[#6c757d]">데이터가 없습니다.</p>
              </div>
              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">예약 금액</h3>
                <Line
                  v-if="revenueChartData"
                  :data="revenueChartData"
                  :options="chartOptions"
                />
                <p v-else class="text-sm text-[#6c757d]">데이터가 없습니다.</p>
              </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">
                  요일별 예측 범위
                </h3>
                <Line
                  v-if="predictionChartData"
                  :data="predictionChartData"
                  :options="chartOptions"
                />
                <p v-else class="text-sm text-[#6c757d]">데이터가 없습니다.</p>
              </div>
              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">예측 상세</h3>
                <p class="text-xs text-[#6c757d] mb-3">
                  구내식당/취향 매칭 지수는 좋아요(선호) 키워드 기준의 매칭
                  비율입니다.
                </p>
                <p
                  class="text-xs text-[#6c757d] mb-3"
                  v-if="restaurantMatchNote"
                >
                  {{ restaurantMatchNote }}
                </p>
                <div class="space-y-3 text-sm text-[#1e3a5f]">
                  <div
                    v-for="row in predictionDetails"
                    :key="row.weekday"
                    class="prediction-row"
                  >
                    <div class="prediction-main">
                      <span class="prediction-day">
                        {{ row.weekday }}요일
                      </span>
                      <span class="prediction-date" v-if="row.date">
                        {{ row.date }}
                      </span>
                      <span class="prediction-range">{{ row.range }}</span>
                      <span
                        class="prediction-badge"
                        :class="row.confidence.tone"
                      >
                        {{ row.confidence.label }}
                      </span>
                    </div>
                    <div class="prediction-evidence" v-if="row.evidence">
                      {{ row.evidence }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div
                class="bg-white rounded-xl border border-[#e9ecef] p-6 lg:col-span-2"
              >
                <div class="flex items-center justify-between mb-4">
                  <h3 class="text-lg font-bold text-[#1e3a5f]">퍼널 분석</h3>
                  <span class="text-xs text-[#6c757d]">
                    방문 전환율 {{ conversionRate }}%
                  </span>
                </div>
                <div v-if="funnelStages.length" class="space-y-3">
                  <div
                    v-for="stage in funnelStages"
                    :key="stage.label"
                    class="funnel-row"
                  >
                    <div class="funnel-label">{{ stage.label }}</div>
                    <div class="funnel-bar">
                      <div
                        class="funnel-bar-fill"
                        :class="{ 'funnel-bar-dark': stage.label === '방문' }"
                        :style="{
                          width: `${stage.width}%`,
                          backgroundColor: stage.color,
                        }"
                      >
                        <span class="funnel-value">{{ stage.value }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <p v-else class="text-sm text-[#6c757d]">데이터가 없습니다.</p>
              </div>
              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">
                  구내식당 메뉴 불일치(비선호 키워드 건수)
                </h3>
                <Bar
                  v-if="mismatchChartData"
                  :data="mismatchChartData"
                  :options="barChartOptions"
                />
                <p v-else class="text-sm text-[#6c757d]">데이터가 없습니다.</p>
              </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div
                class="bg-white rounded-xl border border-[#e9ecef] p-6 lg:col-span-2"
              >
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">
                  통합 분석 및 추천
                </h3>
                <div class="space-y-2 text-sm text-[#1e3a5f]">
                  <div
                    v-for="(line, idx) in recommendationSection?.lines || []"
                    :key="idx"
                    class="summary-line"
                  >
                    <span class="summary-icon">{{ line.icon }}</span>
                    <span class="summary-text">{{ line.text }}</span>
                  </div>
                </div>
              </div>
              <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
                <h3 class="text-lg font-bold text-[#1e3a5f] mb-4">핵심 지표</h3>
                <div class="space-y-3 text-sm text-[#1e3a5f]">
                  <div>
                    공개 북마크: {{ signalSummary.publicBookmarkCount ?? 0 }}
                  </div>
                  <div>
                    공유 링크: {{ signalSummary.approvedLinkCount ?? 0 }}
                  </div>
                  <div>
                    키워드 겹침: {{ signalSummary.keywordOverlap ?? 0 }}
                  </div>
                  <div v-if="signalSummary.topCompanyName">
                    상위 회사: {{ signalSummary.topCompanyName }} ({{
                      Math.round(signalSummary.topCompanyShare * 100)
                    }}%)
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
@media print {
  .print-hide {
    display: none !important;
  }

  .print-area {
    padding: 0 !important;
  }

  :global(body) {
    background: white !important;
  }
}

.funnel-row {
  display: grid;
  grid-template-columns: 64px 1fr;
  align-items: center;
  gap: 12px;
}

.funnel-label {
  font-size: 12px;
  font-weight: 600;
  color: #1e3a5f;
}

.funnel-bar {
  position: relative;
  height: 28px;
  border-radius: 999px;
  background: #f1f3f5;
  overflow: hidden;
}

.funnel-bar-fill {
  height: 100%;
  border-radius: 999px;
  display: flex;
  align-items: center;
  padding-right: 12px;
  justify-content: flex-end;
  color: #1e3a5f;
  font-weight: 700;
  min-width: 48px;
  transition: width 0.4s ease;
}

.funnel-bar-dark {
  color: #ffffff;
}

.funnel-value {
  font-size: 12px;
}

.summary-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.summary-icon {
  font-size: 14px;
  line-height: 1.4;
}

.summary-text {
  color: #1e3a5f;
  font-weight: 600;
  line-height: 1.5;
}

.prediction-row {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #f1f3f5;
  background: #ffffff;
}

.prediction-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.prediction-day {
  font-weight: 700;
}

.prediction-range {
  color: #1e3a5f;
  font-weight: 600;
}

.prediction-date {
  font-size: 12px;
  color: #6c757d;
}

.prediction-badge {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.badge-high {
  background: #e6f4ea;
  color: #1e8e3e;
}

.badge-mid {
  background: #fff4e5;
  color: #ff8a00;
}

.badge-low {
  background: #fde8e8;
  color: #d93025;
}

.prediction-evidence {
  margin-top: 6px;
  color: #6c757d;
  font-size: 12px;
  line-height: 1.4;
}

.insight-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  text-align: center;
}

.insight-spinner {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 6px solid rgba(255, 107, 74, 0.2);
  border-top-color: #ff6b4a;
  animation: spin 1s linear infinite;
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #ffffff;
  animation: spin 0.8s linear infinite;
}

.insight-loading-text {
  max-width: 320px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
