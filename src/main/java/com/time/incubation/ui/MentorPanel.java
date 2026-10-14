package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.Mentor;
import com.time.incubation.model.Startup;
import com.time.incubation.service.MentorService;
import com.time.incubation.service.StartupService;
import com.time.incubation.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MentorPanel extends JPanel {

    /** Separator between startup ID and name in the dropdown text, e.g. "S001 — GreenTech". */
    private static final String SEP = " — ";

    private final MentorService service;
    private final StartupService startupService;

    private DefaultTableModel model;
    private JTable table;

    /** Mentors currently shown in the table (same order as the rows). */
    private List<Mentor> shown = new ArrayList<>();

    private JTextField idField, nameField, expertiseField, emailField, phoneField, orgField;
    private JComboBox<String> startupCombo;

    public MentorPanel(MentorService service, StartupService startupService) {
        this.service = service;
        this.startupService = startupService;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Mentors", "Manage mentor profiles and assignments"), BorderLayout.NORTH);

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
                new Object[]{"ID", "Name", "Expertise", "Organization",
                        "Assigned Startups", "Email", "Phone"}, 0) {
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

        JLabel heading = new JLabel("Mentor Details");
        heading.setFont(UiTheme.FONT_SUBHEADING);

        idField = new JTextField();
        nameField = new JTextField();
        expertiseField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        orgField = new JTextField();
        startupCombo = new JComboBox<>();

        UiTheme.styleField(idField); UiTheme.styleField(nameField);
        UiTheme.styleField(expertiseField); UiTheme.styleField(emailField);
        UiTheme.styleField(phoneField); UiTheme.styleField(orgField);
        UiTheme.styleCombo(startupCombo);

        JPanel grid = new JPanel(new GridLayout(3, 6, 8, 8));
        grid.setOpaque(false);
        grid.add(new JLabel("ID:")); grid.add(idField);
        grid.add(new JLabel("Name:")); grid.add(nameField);
        grid.add(new JLabel("Expertise:")); grid.add(expertiseField);

        grid.add(new JLabel("Email:")); grid.add(emailField);
        grid.add(new JLabel("Phone:")); grid.add(phoneField);
        grid.add(new JLabel("Organization:")); grid.add(orgField);

        grid.add(new JLabel("Assign to Startup:")); grid.add(startupCombo);
        grid.add(new JLabel("")); grid.add(new JLabel(""));
        grid.add(new JLabel("")); grid.add(new JLabel(""));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton add = UiTheme.primaryButton("Add");
        add.addActionListener(e -> onAdd());
        JButton update = UiTheme.secondaryButton("Update");
        update.addActionListener(e -> onUpdate());
        JButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> onDelete());
        JButton assign = UiTheme.secondaryButton("Assign");
        assign.addActionListener(e -> onAssign());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        buttons.add(clear); buttons.add(delete); buttons.add(assign);
        buttons.add(update); buttons.add(add);

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    /** Fills the form from the real Mentor object (not from the "—" placeholders in the table). */
    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= shown.size()) return;
        Mentor m = shown.get(r);

        idField.setText(m.getMentorId());
        nameField.setText(m.getFullName());
        expertiseField.setText(nullToEmpty(m.getExpertise()));
        emailField.setText(nullToEmpty(m.getEmail()));
        phoneField.setText(nullToEmpty(m.getPhone()));
        orgField.setText(nullToEmpty(m.getOrganization()));
        idField.setEditable(false);
    }

    private void onAdd() {
        try {
            service.addMentor(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Mentor added.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onUpdate() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Select a mentor first."); return; }
            service.updateMentor(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Mentor updated.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onDelete() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a mentor first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete mentor " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.deleteMentor(id);
            refresh(); clearForm();
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onAssign() {
        String mentorId = idField.getText().trim();
        if (mentorId.isEmpty()) { UiTheme.showError(this, "Select a mentor first."); return; }
        String startupId = selectedStartupId();
        if (startupId == null) {
            UiTheme.showError(this, "No startup selected."); return;
        }
        try {
            service.assignMentorToStartup(startupId, mentorId);
            refresh();   // so the new startup shows in the "Assigned Startups" column
            UiTheme.showInfo(this, "Mentor assigned to " + startupId + ".");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private Mentor buildFromForm() {
        return new Mentor(
                idField.getText().trim(),
                nameField.getText().trim(),
                emptyOrNull(expertiseField.getText()),
                emptyOrNull(emailField.getText()),
                emptyOrNull(phoneField.getText()),
                emptyOrNull(orgField.getText()));
    }

    /** Returns only the startup ID (e.g. "S001") from the dropdown text "S001 — GreenTech". */
    private String selectedStartupId() {
        Object item = startupCombo.getSelectedItem();
        if (item == null) return null;
        String text = item.toString();
        int i = text.indexOf(SEP);
        return (i >= 0 ? text.substring(0, i) : text).trim();
    }

    private static String emptyOrNull(String s) {
        return ValidationUtils.isNullOrEmpty(s) ? null : s.trim();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void clearForm() {
        idField.setText(""); idField.setEditable(true);
        nameField.setText(""); expertiseField.setText("");
        emailField.setText(""); phoneField.setText(""); orgField.setText("");
        table.clearSelection();
    }

    /** Reloads the startup dropdown and the mentors table from the database. */
    public void refresh() {
        try {
            startupCombo.removeAllItems();
            for (Startup s : startupService.getAll()) {
                startupCombo.addItem(s.getStartupId() + SEP + s.getStartupName());
            }
            populate(service.getAll(), service.getAssignedStartupNames());
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    private void populate(List<Mentor> list, Map<String, String> assigned) {
        shown = new ArrayList<>(list);
        model.setRowCount(0);
        for (Mentor m : shown) {
            model.addRow(new Object[]{
                    m.getMentorId(), m.getFullName(),
                    m.getExpertise() == null ? "—" : m.getExpertise(),
                    m.getOrganization() == null ? "—" : m.getOrganization(),
                    assigned.getOrDefault(m.getMentorId(), "—"),
                    m.getEmail() == null ? "—" : m.getEmail(),
                    m.getPhone() == null ? "—" : m.getPhone()
            });
        }
    }
}