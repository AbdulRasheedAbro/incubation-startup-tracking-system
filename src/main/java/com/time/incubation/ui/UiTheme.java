package com.time.incubation.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class UiTheme {

    private UiTheme() {}

    // ---------- Palette: teal/emerald, distinct from Task 01's blue/slate ----------
    public static final Color BRAND_DARK    = new Color(0x064E3B);  // deep emerald
    public static final Color BRAND_DARK_2  = new Color(0x022C22);
    public static final Color BRAND_ACCENT  = new Color(0x10B981);  // emerald-500
    public static final Color BRAND_ACCENT_SOFT = new Color(0xD1FAE5);
    public static final Color SIDEBAR_BG    = new Color(0x111827);  // near-black charcoal
    public static final Color SIDEBAR_HOVER = new Color(0x1F2937);
    public static final Color SIDEBAR_TEXT  = new Color(0x9CA3AF);
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    public static final Color BG_LIGHT      = new Color(0xF3F4F6);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color BORDER_GRAY   = new Color(0xE5E7EB);
    public static final Color FIELD_BORDER  = new Color(0xD1D5DB);
    public static final Color TEXT_DARK     = new Color(0x111827);
    public static final Color TEXT_MUTED    = new Color(0x6B7280);
    public static final Color SUCCESS       = new Color(0x059669);
    public static final Color WARNING       = new Color(0xD97706);
    public static final Color DANGER        = new Color(0xDC2626);

    public static final Font FONT_TITLE      = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADING    = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_SUBHEADING = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BUTTON     = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_NAV        = new Font("SansSerif", Font.PLAIN, 14);

    public static Border cardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_GRAY, 1, true),
                new EmptyBorder(18, 22, 18, 22));
    }

    public static Border fieldBorder() {
        return new CompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12));
    }

    public static void styleField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBorder(fieldBorder());
    }

    public static void styleField(JPasswordField field) {
        field.setFont(FONT_BODY);
        field.setBorder(fieldBorder());
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setBorder(new LineBorder(FIELD_BORDER, 1, true));
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(BRAND_ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(130, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setForeground(TEXT_DARK);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(5, 16, 5, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(130, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel banner(String title, String subtitle) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Color.WHITE);
        banner.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 1, 0) {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                        g.setColor(BORDER_GRAY);
                        g.fillRect(x, y + h - 1, w, 1);
                    }
                },
                new EmptyBorder(22, 32, 22, 32)));
        banner.setPreferredSize(new Dimension(0, 76));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(TEXT_DARK);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_SMALL);
        s.setForeground(TEXT_MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setBorder(new EmptyBorder(3, 0, 0, 0));

        textBlock.add(t);
        textBlock.add(s);
        banner.add(textBlock, BorderLayout.WEST);
        return banner;
    }

    // ---------- Sidebar nav button ----------
    public static JButton sidebarButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_NAV);
        b.setForeground(SIDEBAR_TEXT);
        b.setBackground(SIDEBAR_BG);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(12, 24, 12, 16));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    public static void setSidebarButtonActive(JButton b, boolean active) {
        b.setBackground(active ? SIDEBAR_HOVER : SIDEBAR_BG);
        b.setForeground(active ? SIDEBAR_TEXT_ACTIVE : SIDEBAR_TEXT);
        b.setFont(active ? FONT_NAV.deriveFont(Font.BOLD) : FONT_NAV);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}