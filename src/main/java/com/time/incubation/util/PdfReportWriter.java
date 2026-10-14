package com.time.incubation.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal PDF writer for the reports (no external library needed).
 * Produces an A4 document: coloured title band, then a two-column "Field / Value" table
 * with wrapped text, striped rows, automatic page breaks and page numbers.
 */
public final class PdfReportWriter {

    private static final float PW = 595f, PH = 842f;   // A4 in points
    private static final float M = 50f;                // page margin
    private static final float COL1 = 170f;            // width of the "Field" column
    private static final float PAD = 8f;               // cell padding
    private static final float FS = 10f;               // body font size
    private static final float LH = 14f;               // line height
    private static final float FOOTER_SPACE = 40f;

    private static final Charset WIN_1252 = Charset.forName("windows-1252");

    /** Helvetica glyph widths (per 1000 units) for ASCII 32..126. */
    private static final int[] W = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584
    };

    private PdfReportWriter() { }

    /** Writes the report to filePath. Throws IOException if the file cannot be written. */
    public static void write(String filePath, String title, Map<String, String> data)
            throws IOException {
        Layout layout = new Layout(title);
        int i = 0;
        for (Map.Entry<String, String> e : data.entrySet()) {
            layout.row(e.getKey(), e.getValue(), i++);
        }
        byte[] pdf = layout.toPdf();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdf);
        }
    }

    // =====================================================================
    //  Page layout
    // =====================================================================
    private static final class Layout {
        private final String title;
        private final List<StringBuilder> pages = new ArrayList<>();
        private StringBuilder cur;
        private float y;

        Layout(String title) {
            this.title = title == null ? "Report" : title;
            newPage(true);
        }

        private void newPage(boolean first) {
            cur = new StringBuilder();
            pages.add(cur);
            if (first) {
                // title band
                fill(0.31f, 0.27f, 0.90f);
                rect(0, PH - 95, PW, 95);
                text("F2", 20, M, PH - 50, 1f, 1f, 1f, title);
                String sub = "TIME Incubation & Startup Tracking System  -  Generated on "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));
                text("F1", 9, M, PH - 72, 0.78f, 0.82f, 0.99f, sub);
                y = PH - 95 - 30;
            } else {
                y = PH - M;
            }
            tableHeader();
        }

        private void tableHeader() {
            float h = 26f;
            fill(0.93f, 0.95f, 1f);
            rect(M, y - h, PW - 2 * M, h);
            text("F2", FS, M + PAD, y - 17, 0.26f, 0.22f, 0.79f, "Field");
            text("F2", FS, M + COL1 + PAD, y - 17, 0.26f, 0.22f, 0.79f, "Value");
            y -= h;
        }

        void row(String label, String value, int index) {
            float valueW = PW - 2 * M - COL1 - 2 * PAD;
            List<String> labelLines = wrap(label, COL1 - 2 * PAD, true);
            List<String> valueLines = wrap(value, valueW, false);
            int lines = Math.max(labelLines.size(), valueLines.size());
            float h = lines * LH + 10f;

            if (y - h < M + FOOTER_SPACE) newPage(false);

            if (index % 2 == 1) {
                fill(0.973f, 0.98f, 0.988f);
                rect(M, y - h, PW - 2 * M, h);
            }
            float base = y - 15f;
            for (int i = 0; i < labelLines.size(); i++) {
                text("F2", FS, M + PAD, base - i * LH, 0.12f, 0.16f, 0.23f, labelLines.get(i));
            }
            for (int i = 0; i < valueLines.size(); i++) {
                text("F1", FS, M + COL1 + PAD, base - i * LH, 0.06f, 0.09f, 0.16f, valueLines.get(i));
            }
            // bottom border
            cur.append("0.5 w ").append(f(0.89f)).append(' ').append(f(0.91f)).append(' ')
                    .append(f(0.94f)).append(" RG ")
                    .append(f(M)).append(' ').append(f(y - h)).append(" m ")
                    .append(f(PW - M)).append(' ').append(f(y - h)).append(" l S\n");
            y -= h;
        }

        // ---- drawing helpers ----
        private void fill(float r, float g, float b) {
            cur.append(f(r)).append(' ').append(f(g)).append(' ').append(f(b)).append(" rg\n");
        }

        private void rect(float x, float yy, float w, float h) {
            cur.append(f(x)).append(' ').append(f(yy)).append(' ')
                    .append(f(w)).append(' ').append(f(h)).append(" re f\n");
        }

        private void text(String font, float size, float x, float yy,
                          float r, float g, float b, String s) {
            cur.append(f(r)).append(' ').append(f(g)).append(' ').append(f(b)).append(" rg\n");
            cur.append("BT /").append(font).append(' ').append(f(size)).append(" Tf ")
                    .append(f(x)).append(' ').append(f(yy)).append(" Td (")
                    .append(escape(s)).append(") Tj ET\n");
        }

        // ---- PDF assembly ----
        byte[] toPdf() throws IOException {
            int n = pages.size();
            for (int i = 0; i < n; i++) {
                cur = pages.get(i);
                text("F1", 8, M, 30, 0.39f, 0.45f, 0.55f,
                        "TIME Incubation  -  Page " + (i + 1) + " of " + n);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            out.write("%PDF-1.4\n".getBytes(WIN_1252));

            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < n; i++) kids.append(5 + 2 * i).append(" 0 R ");

            obj(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
            obj(out, offsets, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>");
            obj(out, offsets, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
                    + "/Encoding /WinAnsiEncoding >>");
            obj(out, offsets, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold "
                    + "/Encoding /WinAnsiEncoding >>");

            for (int i = 0; i < n; i++) {
                int pageObj = 5 + 2 * i, contentObj = 6 + 2 * i;
                obj(out, offsets, pageObj, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                        + (int) PW + " " + (int) PH + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> "
                        + "/Contents " + contentObj + " 0 R >>");
                byte[] content = pages.get(i).toString().getBytes(WIN_1252);
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                body.write(("<< /Length " + content.length + " >>\nstream\n").getBytes(WIN_1252));
                body.write(content);
                body.write("\nendstream".getBytes(WIN_1252));
                obj(out, offsets, contentObj, body.toByteArray());
            }

            int xrefPos = out.size();
            int count = offsets.size() + 1;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n0 ").append(count).append("\n0000000000 65535 f \n");
            for (int off : offsets) xref.append(String.format(Locale.US, "%010d 00000 n \n", off));
            xref.append("trailer\n<< /Size ").append(count).append(" /Root 1 0 R >>\nstartxref\n")
                    .append(xrefPos).append("\n%%EOF\n");
            out.write(xref.toString().getBytes(WIN_1252));
            return out.toByteArray();
        }

        private static void obj(ByteArrayOutputStream out, List<Integer> offsets,
                                int num, String body) throws IOException {
            obj(out, offsets, num, body.getBytes(WIN_1252));
        }

        private static void obj(ByteArrayOutputStream out, List<Integer> offsets,
                                int num, byte[] body) throws IOException {
            // objects are written in order 1..N, so offsets.get(num - 1) is this object
            offsets.add(out.size());
            out.write((num + " 0 obj\n").getBytes(WIN_1252));
            out.write(body);
            out.write("\nendobj\n".getBytes(WIN_1252));
        }
    }

    // =====================================================================
    //  Text helpers
    // =====================================================================
    private static String f(float v) {
        return String.format(Locale.US, "%.2f", v);
    }

    /** Escapes PDF string characters and drops control characters. */
    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') sb.append('\\').append(c);
            else if (c < 32) sb.append(' ');
            else sb.append(c);
        }
        return sb.toString();
    }

    private static float width(String s, boolean bold) {
        float total = 0;
        for (char c : s.toCharArray()) {
            if (c >= 32 && c <= 126) total += W[c - 32];
            else if (c == '\u2014') total += 1000;
            else total += 556;
        }
        return total / 1000f * FS * (bold ? 1.08f : 1f);
    }

    /** Word-wraps text to maxW points; honours line breaks; splits words that are too long. */
    private static List<String> wrap(String text, float maxW, boolean bold) {
        List<String> lines = new ArrayList<>();
        String t = text == null ? "" : text.replace("\r", "").replace("\t", " ");
        for (String para : t.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (String word : para.split(" ")) {
                if (word.isEmpty()) continue;
                // break a single very long word into pieces that fit
                while (width(word, bold) > maxW) {
                    int cut = 1;
                    while (cut < word.length() && width(word.substring(0, cut + 1), bold) <= maxW) cut++;
                    if (line.length() > 0) { lines.add(line.toString()); line.setLength(0); }
                    lines.add(word.substring(0, cut));
                    word = word.substring(cut);
                }
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (width(candidate, bold) <= maxW) {
                    line.setLength(0);
                    line.append(candidate);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }
}