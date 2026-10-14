package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.Milestone;
import com.time.incubation.model.MilestoneStatus;
import com.time.incubation.model.Startup;
import com.time.incubation.service.MilestoneService;
import com.time.incubation.service.StartupService;
import com.time.incubation.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MilestonePanel extends JPanel {

    private final MilestoneService service;
    private final StartupService startupService;

    private DefaultTableModel model;
    private JTable table;

    private JTextField idField, titleField, descriptionField;
    private JTextField startDateField, deadlineField, completionDateField;
    private JTextField completionPctField;
    private JComboBox<String> startupCombo;
    private JComboBox<MilestoneStatus> statusCombo;

    public MilestonePanel(MilestoneService service, StartupService startupService) {
        this.service = service;
        this.startupService = startupService;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Milestones", "Track progress of startup milestones"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UiTheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildFormCard(), BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        model = new DefaultTableModel(
                new Object[]{"ID", "Startup", "Title", "Status", "Deadline", "Progress %"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        JLabel heading = new JLabel("Milestone Details");
        heading.setFont(UiTheme.FONT_SUBHEADING);

        idField = new JTextField();
        titleField = new JTextField();
        descriptionField = new JTextField();
        startDateField = new JTextField("YYYY-MM-DD");
        deadlineField = new JTextField("YYYY-MM-DD");
        completionDateField = new JTextField("YYYY-MM-DD");
        completionPctField = new JTextField("0");
        startupCombo = new JComboBox<>();
        statusCombo = new JComboBox<>(MilestoneStatus.values());

        UiTheme.styleField(idField); UiTheme.styleField(titleField);
        UiTheme.styleField(descriptionField); UiTheme.styleField(startDateField);
        UiTheme.styleField(deadlineField); UiTheme.styleField(completionDateField);
        UiTheme.styleField(completionPctField); UiTheme.styleCombo(startupCombo);
        UiTheme.styleCombo(statusCombo);

        JPanel grid = new JPanel(new GridLayout(4, 6, 8, 8));
        grid.setOpaque(false);
        grid.add(new JLabel("ID:")); grid.add(idField);
        grid.add(new JLabel("Title:")); grid.add(titleField);
        grid.add(new JLabel("Startup:")); grid.add(startupCombo);

        grid.add(new JLabel("Description:")); grid.add(descriptionField);
        grid.add(new JLabel("Start Date:")); grid.add(startDateField);
        grid.add(new JLabel("Deadline:")); grid.add(deadlineField);

        grid.add(new JLabel("Completion Date:")); grid.add(completionDateField);
        grid.add(new JLabel("Progress %:")); grid.add(completionPctField);
        grid.add(new JLabel("Status:")); grid.add(statusCombo);

        grid.add(new JLabel("")); grid.add(new JLabel(""));
        grid.add(new JLabel("")); grid.add(new JLabel(""));
        grid.add(new JLabel("")); grid.add(new JLabel(""));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton add = UiTheme.primaryButton("Add");
        add.addActionListener(e -> onAdd());
        JButton update = UiTheme.secondaryButton("Update");
        update.addActionListener(e -> onUpdate());
        JButton progress = UiTheme.secondaryButton("Update Progress");
        progress.addActionListener(e -> onUpdateProgress());
        JButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> onDelete());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        buttons.add(clear); buttons.add(delete); buttons.add(progress);
        buttons.add(update); buttons.add(add);

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        String milestoneId = model.getValueAt(r, 0).toString();

        Milestone m;
        try {
            m = service.findById(milestoneId);
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
            return;
        }
        if (m == null) return;

        idField.setText(m.getMilestoneId());
        idField.setEditable(false);

        titleField.setText(m.getTitle() != null ? m.getTitle() : "");

        selectComboById(startupCombo, m.getStartupId());

        descriptionField.setText(m.getDescription() != null ? m.getDescription() : "");

        startDateField.setText(m.getStartDate() != null ? m.getStartDate().toString() : "");
        deadlineField.setText(m.getDeadline() != null ? m.getDeadline().toString() : "");
        completionDateField.setText(m.getCompletionDate() != null
                ? m.getCompletionDate().toString() : "");

        completionPctField.setText(String.valueOf(m.getCompletionPercentage()));

        if (m.getStatus() != null) {
            statusCombo.setSelectedItem(m.getStatus());
        }
    }

    /**
     * Selects the combo item whose label starts with the given ID,
     * e.g. id="ST001" matches item "ST001 — Tihami Tech".
     */
    private static void selectComboById(JComboBox<String> combo, String id) {
        if (id == null || id.isBlank()) {
            if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.startsWith(id + " ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
    }

    private void onAdd() {
        try {
            service.addMilestone(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Milestone added.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onUpdate() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Select a milestone first."); return; }
            service.updateMilestone(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Milestone updated.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onUpdateProgress() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Select a milestone first."); return; }
            int pct = Integer.parseInt(completionPctField.getText().trim());
            MilestoneStatus status = (MilestoneStatus) statusCombo.getSelectedItem();
            service.updateProgress(id, pct, status);
            refresh();
            UiTheme.showInfo(this, "Progress updated.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onDelete() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a milestone first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete milestone " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.deleteMilestone(id);
            refresh(); clearForm();
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private Milestone buildFromForm() {
        String startupId = startupCombo.getSelectedItem() == null ? null
                : startupCombo.getSelectedItem().toString().split(" ")[0];
        int pct;
        try { pct = Integer.parseInt(completionPctField.getText().trim()); }
        catch (Exception e) { pct = 0; }

        return new Milestone(
                idField.getText().trim(),
                startupId,
                titleField.getText().trim(),
                emptyOrNull(descriptionField.getText()),
                parseOrNull(startDateField.getText()),
                parseOrNull(deadlineField.getText()),
                parseOrNull(completionDateField.getText()),
                (MilestoneStatus) statusCombo.getSelectedItem(),
                pct);
    }

    private static LocalDate parseOrNull(String s) {
        if (ValidationUtils.isNullOrEmpty(s) || "YYYY-MM-DD".equals(s.trim())) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }

    private static String emptyOrNull(String s) {
        return ValidationUtils.isNullOrEmpty(s) ? null : s.trim();
    }

    private void clearForm() {
        idField.setText(""); idField.setEditable(true);
        titleField.setText(""); descriptionField.setText("");
        startDateField.setText("YYYY-MM-DD"); deadlineField.setText("YYYY-MM-DD");
        completionDateField.setText("YYYY-MM-DD"); completionPctField.setText("0");
        statusCombo.setSelectedIndex(0);
        if (startupCombo.getItemCount() > 0) startupCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    public void refresh() {
        try {
            startupCombo.removeAllItems();
            for (Startup s : startupService.getAll()) {
                startupCombo.addItem(s.getStartupId() + " — " + s.getStartupName());
            }
            populate(service.getAll());
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    private void populate(List<Milestone> list) {
        model.setRowCount(0);
        try {
            List<Startup> startups = startupService.getAll();

            for (Milestone m : list) {
                // Look up the startup name (fall back to ID if not found)
                String startupLabel = m.getStartupId();
                if (m.getStartupId() != null) {
                    for (Startup s : startups) {
                        if (s.getStartupId().equals(m.getStartupId())) {
                            startupLabel = s.getStartupName();
                            break;
                        }
                    }
                }

                model.addRow(new Object[]{
                        m.getMilestoneId(),
                        startupLabel,
                        m.getTitle(),
                        m.getStatus().name(),
                        m.getDeadline() == null ? "—" : m.getDeadline(),
                        m.getCompletionPercentage()
                });
            }
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }
}