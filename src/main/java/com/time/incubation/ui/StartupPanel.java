package com.time.incubation.ui;

import com.time.incubation.exception.*;
import com.time.incubation.model.Startup;
import com.time.incubation.model.StartupStage;
import com.time.incubation.model.StartupStatus;
import com.time.incubation.service.StartupService;
import com.time.incubation.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class StartupPanel extends JPanel {

    private final StartupService service;

    private DefaultTableModel model;
    private JTable table;

    private JTextField idField, nameField, founderField, coFounderField;
    private JTextField emailField, phoneField, industryField;
    private JTextField startDateField, graduationDateField;
    private JComboBox<StartupStage> stageCombo;
    private JComboBox<StartupStatus> statusCombo;

    private JTextField filterIndustryField;
    private JTextField filterNameField;
    private JComboBox<String> filterStageCombo;
    private JComboBox<String> filterStatusCombo;

    private boolean readOnly = false;

    public StartupPanel(StartupService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Startups", "Manage all startups in the incubation program"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UiTheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        body.add(buildFilterCard(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildFormCard(), BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);

        refresh();
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        filterNameField = new JTextField(12);
        UiTheme.styleField(filterNameField);

        filterIndustryField = new JTextField(10);
        UiTheme.styleField(filterIndustryField);

        filterStageCombo = new JComboBox<>();
        filterStageCombo.addItem("All Stages");
        for (StartupStage s : StartupStage.values()) filterStageCombo.addItem(s.name());
        UiTheme.styleCombo(filterStageCombo);

        filterStatusCombo = new JComboBox<>();
        filterStatusCombo.addItem("All Statuses");
        for (StartupStatus s : StartupStatus.values()) filterStatusCombo.addItem(s.name());
        UiTheme.styleCombo(filterStatusCombo);

        JButton apply = UiTheme.secondaryButton("Apply");
        apply.addActionListener(e -> applyFilters());

        JButton clear = UiTheme.secondaryButton("Clear");
        clear.addActionListener(e -> {
            filterNameField.setText("");
            filterIndustryField.setText("");
            filterStageCombo.setSelectedIndex(0);
            filterStatusCombo.setSelectedIndex(0);
            refresh();
        });

        card.add(new JLabel("Name:")); card.add(filterNameField);
        card.add(new JLabel("Industry:")); card.add(filterIndustryField);
        card.add(new JLabel("Stage:")); card.add(filterStageCombo);
        card.add(new JLabel("Status:")); card.add(filterStatusCombo);
        card.add(apply); card.add(clear);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Industry", "Stage", "Status",
                        "Start", "Graduation", "Email"}, 0) {
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

        JLabel heading = new JLabel("Startup Details");
        heading.setFont(UiTheme.FONT_SUBHEADING);
        heading.setBorder(new EmptyBorder(0, 0, 10, 0));

        idField = new JTextField();
        nameField = new JTextField();
        founderField = new JTextField();
        coFounderField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        industryField = new JTextField();
        startDateField = new JTextField("YYYY-MM-DD");
        graduationDateField = new JTextField("YYYY-MM-DD");
        stageCombo = new JComboBox<>(StartupStage.values());
        statusCombo = new JComboBox<>(StartupStatus.values());

        UiTheme.styleField(idField); UiTheme.styleField(nameField);
        UiTheme.styleField(founderField); UiTheme.styleField(coFounderField);
        UiTheme.styleField(emailField); UiTheme.styleField(phoneField);
        UiTheme.styleField(industryField); UiTheme.styleField(startDateField);
        UiTheme.styleField(graduationDateField);
        UiTheme.styleCombo(stageCombo); UiTheme.styleCombo(statusCombo);

        JPanel grid = new JPanel(new GridLayout(4, 6, 8, 8));
        grid.setOpaque(false);

        grid.add(new JLabel("ID:")); grid.add(idField);
        grid.add(new JLabel("Name:")); grid.add(nameField);
        grid.add(new JLabel("Industry:")); grid.add(industryField);

        grid.add(new JLabel("Founder:")); grid.add(founderField);
        grid.add(new JLabel("Co-Founder:")); grid.add(coFounderField);
        grid.add(new JLabel("Email:")); grid.add(emailField);

        grid.add(new JLabel("Phone:")); grid.add(phoneField);
        grid.add(new JLabel("Start:")); grid.add(startDateField);
        grid.add(new JLabel("Graduation:")); grid.add(graduationDateField);

        grid.add(new JLabel("Stage:")); grid.add(stageCombo);
        grid.add(new JLabel("Status:")); grid.add(statusCombo);
        grid.add(new JLabel("")); grid.add(new JLabel(""));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setOpaque(false);

        JButton add = UiTheme.primaryButton("Add");
        add.addActionListener(e -> onAdd());
        JButton update = UiTheme.secondaryButton("Update");
        update.addActionListener(e -> onUpdate());
        JButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> onDelete());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        if (!readOnly) {
            buttons.add(clear); buttons.add(delete); buttons.add(update); buttons.add(add);
        } else {
            buttons.add(clear);
        }

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        idField.setText(model.getValueAt(r, 0).toString());
        nameField.setText(model.getValueAt(r, 1).toString());
        industryField.setText(String.valueOf(model.getValueAt(r, 2)));
        stageCombo.setSelectedItem(StartupStage.valueOf(model.getValueAt(r, 3).toString()));
        statusCombo.setSelectedItem(StartupStatus.valueOf(model.getValueAt(r, 4).toString()));
        startDateField.setText(model.getValueAt(r, 5).toString());
        graduationDateField.setText(model.getValueAt(r, 6).toString());
        emailField.setText(String.valueOf(model.getValueAt(r, 7)));
        idField.setEditable(false);
    }

    private void onAdd() {
        if (readOnly) return;
        try {
            Startup s = buildFromForm();
            service.addStartup(s);
            refresh();
            clearForm();
            UiTheme.showInfo(this, "Startup added.");
        } catch (DuplicateIdException | DuplicateEmailException | InvalidInputException
                 | DataBaseException | IllegalArgumentException ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onUpdate() {
        if (readOnly) return;
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Select a startup first."); return; }
            Startup s = buildFromForm();
            service.updateStartup(s);
            refresh();
            clearForm();
            UiTheme.showInfo(this, "Startup updated.");
        } catch (DuplicateEmailException | InvalidInputException | RecordNotFoundException
                 | DataBaseException | IllegalArgumentException ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onDelete() {
        if (readOnly) return;
        String id = idField.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a startup first."); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Delete startup " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            service.deleteStartup(id);
            refresh();
            clearForm();
        } catch (RecordNotFoundException | DataBaseException ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private Startup buildFromForm() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String industry = industryField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate start = parseDateOrThrow(startDateField.getText().trim(), "Start date");
        LocalDate grad = ValidationUtils.isNullOrEmpty(graduationDateField.getText().trim())
                ? null
                : parseDateOrThrow(graduationDateField.getText().trim(), "Graduation date");

        return new Startup(
                id, name,
                ValidationUtils.isNullOrEmpty(founderField.getText().trim()) ? null : founderField.getText().trim(),
                ValidationUtils.isNullOrEmpty(coFounderField.getText().trim()) ? null : coFounderField.getText().trim(),
                email,
                ValidationUtils.isNullOrEmpty(phone) ? null : phone,
                ValidationUtils.isNullOrEmpty(industry) ? null : industry,
                (StartupStage) stageCombo.getSelectedItem(),
                start, grad,
                (StartupStatus) statusCombo.getSelectedItem(),
                null);
    }

    private LocalDate parseDateOrThrow(String s, String field) {
        try { return LocalDate.parse(s); }
        catch (Exception e) { throw new IllegalArgumentException(field + " must be YYYY-MM-DD."); }
    }

    private void clearForm() {
        idField.setText(""); idField.setEditable(true);
        nameField.setText(""); founderField.setText(""); coFounderField.setText("");
        emailField.setText(""); phoneField.setText(""); industryField.setText("");
        startDateField.setText("YYYY-MM-DD"); graduationDateField.setText("YYYY-MM-DD");
        stageCombo.setSelectedIndex(0); statusCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private void applyFilters() {
        try {
            String name = filterNameField.getText().trim();
            String industry = filterIndustryField.getText().trim();
            StartupStage stage = filterStageCombo.getSelectedIndex() == 0
                    ? null : StartupStage.valueOf((String) filterStageCombo.getSelectedItem());
            StartupStatus status = filterStatusCombo.getSelectedIndex() == 0
                    ? null : StartupStatus.valueOf((String) filterStatusCombo.getSelectedItem());

            List<Startup> results = service.filter(
                    industry.isEmpty() ? null : industry, stage, status,
                    name.isEmpty() ? null : name);
            populate(results);
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    public void refresh() {
        try {
            populate(service.getAll());
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    private void populate(List<Startup> startups) {
        model.setRowCount(0);
        for (Startup s : startups) {
            model.addRow(new Object[]{
                    s.getStartupId(), s.getStartupName(),
                    s.getIndustry() == null ? "—" : s.getIndustry(),
                    s.getStage().name(), s.getCurrentStatus().name(),
                    s.getIncubationStartDate(),
                    s.getExpectedGraduationDate() == null ? "—" : s.getExpectedGraduationDate(),
                    s.getEmail()
            });
        }
    }
}