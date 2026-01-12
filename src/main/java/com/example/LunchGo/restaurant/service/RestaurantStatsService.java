package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.ai.service.AiChatService;
import com.example.LunchGo.reservation.dto.BusinessReservationItemResponse;
import com.example.LunchGo.reservation.service.BusinessReservationQueryService;
import com.example.LunchGo.restaurant.entity.DailyRestaurantStats;
import com.example.LunchGo.restaurant.repository.DailyRestaurantStatsRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import java.awt.Color;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantStatsService {
    private final BusinessReservationQueryService businessReservationQueryService;
    private final DailyRestaurantStatsRepository dailyRestaurantStatsRepository;
    private final AiChatService aiChatService;
    @Value("${pdf.korean-font-path:}")
    private String koreanFontPath;

    public byte[] generateWeeklyStatsPdf(Long restaurantId) {
        try {
            List<BusinessReservationItemResponse> allReservations =
                businessReservationQueryService.getList(restaurantId);

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(6);

            List<BusinessReservationItemResponse> weeklyReservations = allReservations.stream()
                .filter(item -> isWithinLast7Days(item.getDatetime(), start, end))
                .toList();

            List<DailyRestaurantStats> dailyStats =
                dailyRestaurantStatsRepository.findLast7DaysByRestaurantId(restaurantId);

            String prompt = buildPrompt(restaurantId, start, end, weeklyReservations, dailyStats);
            String aiSummary = aiChatService.chat(prompt);

            return buildPdf(restaurantId, start, end, weeklyReservations, dailyStats, aiSummary);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.contains("RESOURCE_EXHAUSTED") || message.contains("quota")) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "ai_quota_exceeded", e);
            }
            if (message.contains("NOT_FOUND")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ai_model_not_found", e);
            }
            if (message.contains("timeout")) {
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "ai_timeout", e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ai_error", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "stats_pdf_failed", e);
        }
    }

    private boolean isWithinLast7Days(String datetime, LocalDate start, LocalDate end) {
        if (datetime == null || datetime.isBlank()) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);
        LocalDate date = LocalDateTime.parse(datetime, formatter).toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }

        private String buildPrompt(
        Long restaurantId,
        LocalDate start,
        LocalDate end,
        List<BusinessReservationItemResponse> reservations,
        List<DailyRestaurantStats> dailyStats
    ) {
        Map<LocalDate, Integer> reservationCounts = new TreeMap<>();
        Map<LocalDate, Integer> reservationAmounts = new TreeMap<>();

        for (BusinessReservationItemResponse item : reservations) {
            LocalDate date = LocalDate.parse(item.getDatetime().substring(0, 10));
            reservationCounts.merge(date, 1, Integer::sum);
            int amount = item.getAmount() == null ? 0 : item.getAmount();
            reservationAmounts.merge(date, amount, Integer::sum);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("다음 데이터를 기반으로 주간 통계 요약을 한국어로 작성해 주세요.");
        builder.append("반드시 아래 섹션 제목을 그대로 포함하세요: ");
        builder.append("## 핵심 요약 ");
        builder.append("## 상세 분석 ");
        builder.append("## 통합 분석 및 추천 ");

        builder.append("일자별 예약 요약 (건수, 합계금액): ");
        for (Map.Entry<LocalDate, Integer> entry : reservationCounts.entrySet()) {
            int amount = reservationAmounts.getOrDefault(entry.getKey(), 0);
            builder.append(entry.getKey()).append(" => ")
                .append(entry.getValue()).append(", ")
                .append(amount).append(" ");
        }

        builder.append("일자별 식당 통계: ");
        dailyStats.stream()
            .sorted(Comparator.comparing(stats -> stats.getId().getStatDate()))
            .forEach(stats -> builder.append(stats.getId().getStatDate()).append(" | ")
                .append("view=").append(nullToZero(stats.getViewCount())).append(", ")
                .append("try=").append(nullToZero(stats.getTryCount())).append(", ")
                .append("confirm=").append(nullToZero(stats.getConfirmCount())).append(", ")
                .append("visit=").append(nullToZero(stats.getVisitCount())).append(", ")
                .append("noshow=").append(nullToZero(stats.getDefendedNoshowCnt())).append(", ")
                .append("penalty=").append(nullToZero(stats.getPenaltyStlAmt())).append(", ")
                .append("revenue=").append(nullToZeroLong(stats.getRevenueTotal()))
                .append(" "));

        builder.append("응답은 반드시 한국어로 작성하세요. 일자별 예약 요약과 일자별 식당 통계는 참고만 하고 작성하지 않아도 됩니다.");
        return builder.toString();
    }

private byte[] buildPdf(
        Long restaurantId,
        LocalDate start,
        LocalDate end,
        List<BusinessReservationItemResponse> reservations,
        List<DailyRestaurantStats> dailyStats,
        String aiSummary
    ) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDType0Font font = loadKoreanFont(document);
            PDPage page = new PDPage();
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            float margin = 48f;
            float contentWidth = mediaBox.getWidth() - (margin * 2);
            float y = mediaBox.getHeight() - margin;

            PdfCursor cursor = new PdfCursor(document, page, y, margin, margin);
            PDPageContentStream content = null;
            try {
                content = new PDPageContentStream(document, page);
                cursor.setContent(content);
                cursor.setY(drawHeader(content, font, mediaBox, margin, cursor.getY(), restaurantId, start, end));
                cursor.setY(cursor.getY() - 8);
                cursor.setY(drawMarkdownSection(cursor, font, aiSummary, contentWidth, 14));
            } finally {
                if (cursor.getContent() != null) {
                    cursor.getContent().close();
                }
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate PDF", e);
        }
    }

    private List<String> wrapText(String text, PDType0Font font, int fontSize, float maxWidth) throws IOException {
        if (text == null || text.isBlank()) {
            return List.of("");
        }
        String[] rawLines = text.replace("\r", "").split("\n");
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (String raw : rawLines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                lines.add("");
                continue;
            }
            lines.addAll(wrapByWidth(line, font, fontSize, maxWidth));
        }
        return lines;
    }

    private List<String> wrapByWidth(String text, PDType0Font font, int fontSize, float maxWidth) throws IOException {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            line.append(text.charAt(i));
            if (getTextWidth(font, fontSize, line.toString()) > maxWidth) {
                if (line.length() > 1) {
                    lines.add(line.substring(0, line.length() - 1));
                    line.delete(0, line.length() - 1);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);
                }
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private float drawHeader(
        PDPageContentStream content,
        PDType0Font font,
        PDRectangle mediaBox,
        float margin,
        float y,
        Long restaurantId,
        LocalDate start,
        LocalDate end
    ) throws IOException {
        float headerHeight = 70f;
        content.setNonStrokingColor(new Color(245, 248, 255));
        content.addRect(0, y - headerHeight + 12, mediaBox.getWidth(), headerHeight);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(Color.BLACK);
        content.setFont(font, 18);
        content.newLineAtOffset(margin, y - 28);
        content.showText("주간 식당 통계 리포트");
        content.endText();

        content.beginText();
        content.setFont(font, 12);
        content.newLineAtOffset(margin, y - 50);
        content.showText("기간: " + start + " ~ " + end);
        content.endText();

        return y - headerHeight;
    }

    private float drawSummaryCards(
        PDPageContentStream content,
        PDType0Font font,
        float margin,
        float contentWidth,
        float y,
        int reservationCount,
        int statsCount
    ) throws IOException {
        float cardHeight = 48f;
        float gap = 12f;
        float cardWidth = (contentWidth - gap) / 2f;
        float cardY = y - cardHeight - 8;

        drawCard(content, margin, cardY, cardWidth, cardHeight, new Color(255, 246, 235));
        drawCard(content, margin + cardWidth + gap, cardY, cardWidth, cardHeight, new Color(235, 248, 240));

        content.beginText();
        content.setNonStrokingColor(Color.BLACK);
        content.setFont(font, 11);
        content.newLineAtOffset(margin + 12, cardY + 30);
        content.showText("예약 건수");
        content.newLineAtOffset(0, -16);
        content.setFont(font, 14);
        content.showText(String.valueOf(reservationCount));
        content.endText();

        content.beginText();
        content.setFont(font, 11);
        content.newLineAtOffset(margin + cardWidth + gap + 12, cardY + 30);
        content.showText("일자별 통계 행 수");
        content.newLineAtOffset(0, -16);
        content.setFont(font, 14);
        content.showText(String.valueOf(statsCount));
        content.endText();

        return cardY - 18;
    }

    private void drawCard(
        PDPageContentStream content,
        float x,
        float y,
        float width,
        float height,
        Color fill
    ) throws IOException {
        content.setNonStrokingColor(fill);
        content.addRect(x, y, width, height);
        content.fill();
        content.setStrokingColor(new Color(220, 220, 220));
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private float drawSectionTitle(
        PDPageContentStream content,
        PDType0Font font,
        float margin,
        float y,
        String title
    ) throws IOException {
        content.setNonStrokingColor(new Color(30, 58, 95));
        content.beginText();
        content.setFont(font, 13);
        content.newLineAtOffset(margin, y);
        content.showText(title);
        content.endText();
        return y - 18;
    }

    private float drawParagraph(
        PdfCursor cursor,
        PDType0Font font,
        String text,
        float maxWidth,
        int fontSize,
        int lineHeight
    ) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, maxWidth);
        for (String line : lines) {
            cursor.ensureSpace(lineHeight);
            PDPageContentStream content = cursor.getContent();
            content.beginText();
            content.setNonStrokingColor(Color.BLACK);
            content.setFont(font, fontSize);
            content.newLineAtOffset(cursor.getMargin(), cursor.getY());
            content.showText(line);
            content.endText();
            cursor.addY(-lineHeight);
        }
        return cursor.getY() - 8;
    }

    private float drawList(
        PdfCursor cursor,
        PDType0Font font,
        List<String> items,
        float maxWidth,
        int fontSize,
        int lineHeight
    ) throws IOException {
        for (String item : items) {
            List<String> lines = wrapText("- " + item, font, fontSize, maxWidth);
            for (String line : lines) {
                cursor.ensureSpace(lineHeight);
                PDPageContentStream content = cursor.getContent();
                content.beginText();
                content.setNonStrokingColor(Color.BLACK);
                content.setFont(font, fontSize);
                content.newLineAtOffset(cursor.getMargin(), cursor.getY());
                content.showText(line);
                content.endText();
                cursor.addY(-lineHeight);
            }
        }
        return cursor.getY() - 6;
    }

    private float drawMarkdownSection(
        PdfCursor cursor,
        PDType0Font font,
        String markdown,
        float maxWidth,
        int lineHeight
    ) throws IOException {
        if (markdown == null || markdown.isBlank()) {
            return drawParagraph(
                cursor,
                font,
                "AI 요약을 생성하지 못했습니다. 데이터만 참고해 주세요.",
                maxWidth,
                11,
                lineHeight
            );
        }

        String[] lines = markdown.replace("\r", "").split("\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                cursor.addY(-8);
                continue;
            }

            if (line.startsWith("### ")) {
                drawParagraph(cursor, font, stripMarkdown(line.substring(4)), maxWidth, 12, 16);
                continue;
            }
            if (line.startsWith("## ")) {
                drawParagraph(cursor, font, stripMarkdown(line.substring(3)), maxWidth, 13, 18);
                continue;
            }
            if (line.startsWith("# ")) {
                drawParagraph(cursor, font, stripMarkdown(line.substring(2)), maxWidth, 14, 20);
                continue;
            }

            if (line.startsWith("- ") || line.startsWith("* ")) {
                drawList(cursor, font, List.of(stripMarkdown(line.substring(2))), maxWidth, 11, lineHeight);
                continue;
            }

            drawParagraph(cursor, font, stripMarkdown(line), maxWidth, 11, lineHeight);
        }
        return cursor.getY();
    }

    private String stripMarkdown(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
            .replaceAll("`(.+?)`", "$1")
            .replaceAll("_([^_]+)_", "$1");
    }

    private List<String> buildReservationLines(List<BusinessReservationItemResponse> reservations) {
        Map<LocalDate, Integer> counts = new TreeMap<>();
        Map<LocalDate, Integer> amounts = new TreeMap<>();
        for (BusinessReservationItemResponse item : reservations) {
            LocalDate date = LocalDate.parse(item.getDatetime().substring(0, 10));
            counts.merge(date, 1, Integer::sum);
            int amount = item.getAmount() == null ? 0 : item.getAmount();
            amounts.merge(date, amount, Integer::sum);
        }
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : counts.entrySet()) {
            lines.add(entry.getKey() + "  |  예약 " + entry.getValue() + "건  |  합계 " +
                amounts.getOrDefault(entry.getKey(), 0) + "원");
        }
        if (lines.isEmpty()) {
            lines.add("최근 7일간 예약 데이터가 없습니다.");
        }
        return lines;
    }

    private List<String> buildStatsLines(List<DailyRestaurantStats> dailyStats) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        dailyStats.stream()
            .sorted(Comparator.comparing(stats -> stats.getId().getStatDate()))
            .forEach(stats -> lines.add(
                stats.getId().getStatDate() + "  |  조회 " + nullToZero(stats.getViewCount()) +
                    "  |  시도 " + nullToZero(stats.getTryCount()) +
                    "  |  확정 " + nullToZero(stats.getConfirmCount()) +
                    "  |  방문 " + nullToZero(stats.getVisitCount())
            ));
        if (lines.isEmpty()) {
            lines.add("최근 7일간 통계 데이터가 없습니다.");
        }
        return lines;
    }

    private PDType0Font loadKoreanFont(PDDocument document) throws IOException {
        if (koreanFontPath != null && !koreanFontPath.isBlank()) {
            Path path = Path.of(koreanFontPath);
            if (Files.exists(path)) {
                return PDType0Font.load(document, path.toFile());
            }
        }

        Path windowsFont = Path.of("C:\\Windows\\Fonts\\malgun.ttf");
        if (Files.exists(windowsFont)) {
            return PDType0Font.load(document, windowsFont.toFile());
        }

        try (InputStream stream = RestaurantStatsService.class.getResourceAsStream(
            "/fonts/NotoSansKR-Regular.ttf"
        )) {
            if (stream != null) {
                return PDType0Font.load(document, stream);
            }
        }

        throw new IllegalStateException(
            "Korean font not found. Set pdf.korean-font-path or add /fonts/NotoSansKR-Regular.ttf."
        );
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private long nullToZeroLong(Long value) {
        return value == null ? 0L : value;
    }

    private float getTextWidth(PDType0Font font, int fontSize, String text) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }


    private static class PdfCursor {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream content;
        private final float margin;
        private final float minY;
        private float y;

        private PdfCursor(PDDocument document, PDPage page, float y, float margin, float minY) {
            this.document = document;
            this.page = page;
            this.y = y;
            this.margin = margin;
            this.minY = minY;
        }

        private void setContent(PDPageContentStream content) {
            this.content = content;
        }

        private PDPageContentStream getContent() {
            return content;
        }

        private float getY() {
            return y;
        }

        private void setY(float y) {
            this.y = y;
        }

        private void addY(float delta) {
            this.y += delta;
        }

        private float getMargin() {
            return margin;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed >= minY) {
                return;
            }
            if (content != null) {
                content.close();
            }
            page = new PDPage();
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            y = mediaBox.getHeight() - margin;
            content = new PDPageContentStream(document, page);
        }
    }
}
