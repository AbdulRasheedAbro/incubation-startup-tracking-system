package com.time.incubation.ui;

import com.time.incubation.exception.*;
import com.time.incubation.model.Founder;
import com.time.incubation.model.Startup;
import com.time.incubation.service.FounderService;
import com.time.incubation.service.StartupService;
import com.time.incubation.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FounderPanel extends JPanel {

    /** Separator between startup ID and name in the dropdown text, e.g. "S001 — GreenTech". */
    private static final String SEP = " — ";

    private final FounderService service;
    private final StartupService startupService;

    private DefaultTableModel model;
    private JTable table;

    /** Founders currently shown in the table (same order as the rows). */
    private List<Founder> shown = new ArrayList<>();

    /** startup ID -> startup name, used to show names instead of IDs in the table. */
    private final Map<String, String> startupNames = new HashMap<>();

    private JTextField idField, nameField, emailField, phoneField;
    private JTextField qualificationField, skillsField, linkedinField;
    private JComboBox<String> startupCombo;

    public FounderPanel(FounderService service, StartupService startupService) {
        this.service = service;
        this.startupService = startupService;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Founders", "Manage startup founders"), BorderLayout.NORTH);

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
                new Object[]{"ID", "Name", "Startup", "Email", "Phone", "Qualification"}, 0) {
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

        JLabel heading = new JLabel("Founder Details");
        heading.setFont(UiTheme.FONT_SUBHEADING);

        idField = new JTextField();
        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        qualificationField = new JTextField();
        skillsField = new JTextField();
        linkedinField = new JTextField();
        startupCombo = new JComboBox<>();

        UiTheme.styleField(idField); UiTheme.styleField(nameField);
        UiTheme.styleField(emailField); UiTheme.styleField(phoneField);
        UiTheme.styleField(qualificationField); UiTheme.styleField(skillsField);
        UiTheme.styleField(linkedinField); UiTheme.styleCombo(startupCombo);

        JPanel grid = new JPanel(new GridLayout(3, 6, 8, 8));
        grid.setOpaque(false);
        grid.add(new JLabel("ID:")); grid.add(idField);
        grid.add(new JLabel("Name:")); grid.add(nameField);
        grid.add(new JLabel("Startup:")); grid.add(startupCombo);

        grid.add(new JLabel("Email:")); grid.add(emailField);
        grid.add(new JLabel("Phone:")); grid.add(phoneField);
        grid.add(new JLabel("Qualification:")); grid.add(qualificationField);

        grid.add(new JLabel("Skills:")); grid.add(skillsField);
        grid.add(new JLabel("LinkedIn:")); grid.add(linkedinField);
        grid.add(new JLabel("")); grid.add(new JLabel(""));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton add = UiTheme.primaryButton("Add");
        add.addActionListener(e -> onAdd());
        JButton update = UiTheme.secondaryButton("Update");
        update.addActionListener(e -> onUpdate());
        JButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> onDelete());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        buttons.add(clear); buttons.add(delete); buttons.add(update); buttons.add(add);

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    /** Fills the form from the real Founder object (not from the "—" placeholders in the table). */
    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= shown.size()) return;
        Founder f = shown.get(r);

        idField.setText(f.getFounderId());
        nameField.setText(f.getName());
        emailField.setText(nullToEmpty(f.getEmail()));
        phoneField.setText(nullToEmpty(f.getPhone()));
        qualificationField.setText(nullToEmpty(f.getQualification()));
        skillsField.setText(nullToEmpty(f.getSkills()));
        linkedinField.setText(nullToEmpty(f.getLinkedinUrl()));
        selectStartup(f.getStartupId());
        idField.setEditable(false);
    }

    private void onAdd() {
        try {
            service.addFounder(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Founder added.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onUpdate() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Select a founder first."); return; }
            service.updateFounder(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Founder updated.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onDelete() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a founder first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete founder " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.deleteFounder(id);
            refresh(); clearForm();
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private Founder buildFromForm() {
        return new Founder(
                idField.getText().trim(),
                nameField.getText().trim(),
                selectedStartupId(),
                emptyOrNull(emailField.getText()),
                emptyOrNull(phoneField.getText()),
                emptyOrNull(qualificationField.getText()),
                emptyOrNull(skillsField.getText()),
                emptyOrNull(linkedinField.getText()));
    }

    /** Returns only the startup ID (e.g. "S001") from the dropdown text "S001 — GreenTech". */
    private String selectedStartupId() {
        Object item = startupCombo.getSelectedItem();
        if (item == null) return null;
        String text = item.toString();
        int i = text.indexOf(SEP);
        return (i >= 0 ? text.substring(0, i) : text).trim();
    }

    private void selectStartup(String startupId) {
        if (startupId == null) return;
        for (int i = 0; i < startupCombo.getItemCount(); i++) {
            String item = startupCombo.getItemAt(i);
            if (item.equals(startupId) || item.startsWith(startupId + SEP)) {
                startupCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static String emptyOrNull(String s) {
        return ValidationUtils.isNullOrEmpty(s) ? null : s.trim();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void clearForm() {
        idField.setText(""); idField.setEditable(true);
        nameField.setText(""); emailField.setText(""); phoneField.setText("");
        qualificationField.setText(""); skillsField.setText(""); linkedinField.setText("");
        table.clearSelection();
    }

    /** Reloads the startup dropdown and the founders table from the database. */
    public void refresh() {
        try {
            startupCombo.removeAllItems();
            startupNames.clear();
            for (Startup s : startupService.getAll()) {
                startupCombo.addItem(s.getStartupId() + SEP + s.getStartupName());
                startupNames.put(s.getStartupId(), s.getStartupName());
            }
            populate(service.getAll());
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    private void populate(List<Founder> list) {
        shown = new ArrayList<>(list);
        model.setRowCount(0);
        for (Founder f : shown) {
            model.addRow(new Object[]{
                    f.getFounderId(), f.getName(),
                    startupNames.getOrDefault(f.getStartupId(), f.getStartupId()),
                    f.getEmail() == null ? "—" : f.getEmail(),
                    f.getPhone() == null ? "—" : f.getPhone(),
                    f.getQualification() == null || f.getQualification().isBlank()
                            ? "—" : f.getQualification()
            });
        }
    }
}