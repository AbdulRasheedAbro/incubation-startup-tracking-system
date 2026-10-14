package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.service.DashboardService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;
    private final JPanel statsContainer;
    private JLabel avgScoreLabel;

    public DashboardPanel(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);

        add(UiTheme.banner("Dashboard", "Overview of startups, mentors, and meetings"), BorderLayout.NORTH);

        statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.Y_AXIS));
        statsContainer.setBackground(UiTheme.BG_LIGHT);
        statsContainer.setBorder(new EmptyBorder(24, 32, 24, 32));

        JScrollPane scroll = new JScrollPane(statsContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiTheme.BG_LIGHT);
        add(scroll, BorderLayout.CENTER);

        JButton refresh = UiTheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(UiTheme.BG_LIGHT);
        bar.add(refresh);
        add(bar, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        statsContainer.removeAll();
        try {
            Map<String, Integer> stats = dashboardService.getAllStats();

            statsContainer.add(sectionLabel("Startups"));
            statsContainer.add(statsRow(
                    stat("Total", stats.get("Total Startups")),
                    stat("Active", stats.get("Active Startups")),
                    stat("Graduated", stats.get("Graduated")),
                    stat("On Hold", stats.get("On Hold"))));

            statsContainer.add(Box.createVerticalStrut(20));
            statsContainer.add(sectionLabel("Startup Stages"));
            statsContainer.add(statsRow(
                    stat("Idea Stage", stats.get("Idea Stage")),
                    stat("MVP Stage", stats.get("MVP Stage")),
                    stat("Early Revenue", stats.get("Early Revenue"))));

            statsContainer.add(Box.createVerticalStrut(20));
            statsContainer.add(sectionLabel("Activity"));
            statsContainer.add(statsRow(
                    stat("Total Mentors", stats.get("Total Mentors")),
                    stat("Total Meetings", stats.get("Total Meetings")),
                    stat("Upcoming", stats.get("Upcoming Meetings")),
                    stat("Delayed Milestones", stats.get("Delayed Milestones"))));

            statsContainer.add(Box.createVerticalStrut(20));
            statsContainer.add(sectionLabel("Evaluation"));
            double avg = dashboardService.averageEvaluationScore();
            avgScoreLabel = new JLabel(String.format("%.2f / 10", avg));
            avgScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            avgScoreLabel.setForeground(UiTheme.BRAND_ACCENT);
            statsContainer.add(avgScoreLabel);

        } catch (DataBaseException e) {
            statsContainer.add(new JLabel("Failed to load stats: " + e.getMessage()));
        }
        statsContainer.revalidate();
        statsContainer.repaint();
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(UiTheme.FONT_SUBHEADING);
        l.setForeground(UiTheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JPanel statsRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JPanel c : cards) row.add(c);
        return row;
    }

    private JPanel stat(String label, Integer value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        JLabel caption = new JLabel(label);
        caption.setFont(UiTheme.FONT_SMALL);
        caption.setForeground(UiTheme.TEXT_MUTED);
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel v = new JLabel(value == null ? "0" : String.valueOf(value));
        v.setFont(new Font("SansSerif", Font.BOLD, 26));
        v.setForeground(UiTheme.BRAND_ACCENT);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setBorder(new EmptyBorder(6, 0, 0, 0));

        card.add(caption);
        card.add(v);
        return card;
    }
}