package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.Mentor;
import com.time.incubation.model.Startup;
import com.time.incubation.service.MentorService;
import com.time.incubation.service.ReportService;
import com.time.incubation.service.StartupService;
import com.time.incubation.util.PdfReportWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReportPanel extends JPanel {

    /** Separator between ID and name in the dropdown text, e.g. "S001 — GreenTech". */
    private static final String SEP = " — ";

    private final ReportService service;
    private final StartupService startupService;
    private final MentorService mentorService;

    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> startupCombo;
    private JComboBox<String> mentorCombo;
    private DefaultTableModel model;
    private JTable table;
    private JButton exportButton;
    private Map<String, String> currentReport;
    private String currentTitle = "Report";

    public ReportPanel(ReportService service,
                       StartupService startupService,
                       MentorService mentorService) {
        this.service = service;
        this.startupService = startupService;
        this.mentorService = mentorService;

        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Reports", "Generate and export reports"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UiTheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));
        body.add(buildControls(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildControls() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        reportTypeCombo = new JComboBox<>(new String[]{
                "Startup Progress Report",
                "Mentor Report",
                "Incubation Performance Report"
        });
        UiTheme.styleCombo(reportTypeCombo);

        startupCombo = new JComboBox<>();
        UiTheme.styleCombo(startupCombo);

        mentorCombo = new JComboBox<>();
        UiTheme.styleCombo(mentorCombo);

        // Fixed preferred height, but allow shrinking so nothing gets clipped
        Dimension pref = new Dimension(220, 32);
        Dimension min = new Dimension(120, 32);
        for (JComboBox<String> combo : java.util.List.of(reportTypeCombo, startupCombo, mentorCombo)) {
            combo.setPreferredSize(pref);
            combo.setMinimumSize(min);
        }

        JButton generate = UiTheme.primaryButton("Generate");
        generate.addActionListener(e -> onGenerate());

        exportButton = UiTheme.secondaryButton("Export PDF");
        exportButton.addActionListener(e -> onExport());
        exportButton.setEnabled(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: labels + combos
        c.gridy = 0;
        c.gridx = 0; c.weightx = 0; card.add(new JLabel("Report:"), c);
        c.gridx = 1; c.weightx = 1; card.add(reportTypeCombo, c);
        c.gridx = 2; c.weightx = 0; card.add(new JLabel("Startup:"), c);
        c.gridx = 3; c.weightx = 1; card.add(startupCombo, c);
        c.gridx = 4; c.weightx = 0; card.add(new JLabel("Mentor:"), c);
        c.gridx = 5; c.weightx = 1; card.add(mentorCombo, c);

        // Row 1: buttons, right-aligned, always fully visible
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(generate);
        buttons.add(exportButton);

        c.gridy = 1; c.gridx = 0; c.gridwidth = 6; c.weightx = 1;
        card.add(buttons, c);

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        model = new DefaultTableModel(new Object[]{"Field", "Value"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(500);

        JScrollPane scroll = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void onGenerate() {
        model.setRowCount(0);
        currentReport = null;
        exportButton.setEnabled(false);

        String choice = (String) reportTypeCombo.getSelectedItem();
        try {
            if ("Startup Progress Report".equals(choice)) {
                String id = idOf(startupCombo);
                if (id == null) {
                    UiTheme.showError(this, "Select a startup first.");
                    return;
                }
                currentReport = service.buildStartupProgressReport(id);
            } else if ("Mentor Report".equals(choice)) {
                String id = idOf(mentorCombo);
                if (id == null) {
                    UiTheme.showError(this, "Select a mentor first.");
                    return;
                }
                currentReport = service.buildMentorReport(id);
            } else {
                currentReport = service.buildIncubationPerformanceReport();
            }

            currentTitle = choice;
            for (Map.Entry<String, String> e : currentReport.entrySet()) {
                model.addRow(new Object[]{e.getKey(), e.getValue()});
            }
            exportButton.setEnabled(true);

        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onExport() {
        if (currentReport == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF document (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(currentTitle.replace(' ', '_') + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            PdfReportWriter.write(file.getAbsolutePath(), currentTitle, currentReport);
            UiTheme.showInfo(this, "Report exported to " + file.getName());
        } catch (IOException ex) {
            UiTheme.showError(this, "Could not save the PDF: " + ex.getMessage());
        }
    }

    /** Returns only the ID (e.g. "S001") from dropdown text like "S001 — GreenTech", or null. */
    private static String idOf(JComboBox<String> combo) {
        Object item = combo.getSelectedItem();
        if (item == null) return null;
        String text = item.toString();
        int i = text.indexOf(SEP);
        return (i >= 0 ? text.substring(0, i) : text).trim();
    }

    public void refresh() {
        try {
            startupCombo.removeAllItems();
            for (Startup s : startupService.getAll()) {
                startupCombo.addItem(s.getStartupId() + SEP + s.getStartupName());
            }
            mentorCombo.removeAllItems();
            for (Mentor m : mentorService.getAll()) {
                mentorCombo.addItem(m.getMentorId() + SEP + m.getFullName());
            }
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }
}
