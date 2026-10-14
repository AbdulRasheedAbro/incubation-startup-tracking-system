package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.Evaluation;
import com.time.incubation.model.Startup;
import com.time.incubation.service.EvaluationService;
import com.time.incubation.service.StartupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EvaluationPanel extends JPanel {

    private final EvaluationService service;
    private final StartupService startupService;

    private DefaultTableModel model;
    private JTable table;

    private JTextField idField;
    private JComboBox<String> startupCombo;
    private JSlider teamSlider, problemSlider, productSlider, marketSlider;
    private JSlider businessSlider, tractionSlider, revenueSlider, pitchSlider;
    private JLabel overallLabel;

    public EvaluationPanel(EvaluationService service, StartupService startupService) {
        this.service = service;
        this.startupService = startupService;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Evaluations", "Score startups on 8 criteria"), BorderLayout.NORTH);

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
                new Object[]{"ID", "Startup", "Team", "Problem", "Product", "Market",
                        "Business", "Traction", "Revenue", "Pitch", "Overall", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
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

        JLabel heading = new JLabel("Evaluation Form");
        heading.setFont(UiTheme.FONT_SUBHEADING);

        idField = new JTextField();
        UiTheme.styleField(idField);

        startupCombo = new JComboBox<>();
        UiTheme.styleCombo(startupCombo);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        topRow.setOpaque(false);
        topRow.add(new JLabel("Evaluation ID:"));
        topRow.add(idField);
        topRow.add(new JLabel("Startup:"));
        topRow.add(startupCombo);

        teamSlider    = slider();
        problemSlider = slider();
        productSlider = slider();
        marketSlider  = slider();
        businessSlider = slider();
        tractionSlider = slider();
        revenueSlider  = slider();
        pitchSlider    = slider();

        JPanel sliders = new JPanel(new GridLayout(4, 4, 8, 8));
        sliders.setOpaque(false);
        addSlider(sliders, "Team", teamSlider);
        addSlider(sliders, "Problem Validation", problemSlider);
        addSlider(sliders, "Product Development", productSlider);
        addSlider(sliders, "Market Validation", marketSlider);
        addSlider(sliders, "Business Model", businessSlider);
        addSlider(sliders, "Traction", tractionSlider);
        addSlider(sliders, "Revenue", revenueSlider);
        addSlider(sliders, "Pitch Readiness", pitchSlider);

        overallLabel = new JLabel("Overall: —");
        overallLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        overallLabel.setForeground(UiTheme.BRAND_ACCENT);

        JPanel overallPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        overallPanel.setOpaque(false);
        overallPanel.add(overallLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton evaluate = UiTheme.primaryButton("Save Evaluation");
        evaluate.addActionListener(e -> onEvaluate());
        JButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> onDelete());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());
        buttons.add(clear); buttons.add(delete); buttons.add(evaluate);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(topRow);
        center.add(Box.createVerticalStrut(6));
        center.add(sliders);
        center.add(Box.createVerticalStrut(6));
        center.add(overallPanel);

        card.add(heading, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JSlider slider() {
        JSlider s = new JSlider(1, 10, 5);
        s.setMajorTickSpacing(1);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setOpaque(false);
        s.addChangeListener(e -> updateOverall());
        return s;
    }

    private void addSlider(JPanel parent, String label, JSlider slider) {
        JLabel l = new JLabel(label + ":");
        l.setFont(UiTheme.FONT_BODY);
        parent.add(l);
        parent.add(slider);
    }

    private void updateOverall() {
        double overall = Evaluation.computeOverall(
                teamSlider.getValue(), problemSlider.getValue(), productSlider.getValue(),
                marketSlider.getValue(), businessSlider.getValue(), tractionSlider.getValue(),
                revenueSlider.getValue(), pitchSlider.getValue());
        overallLabel.setText(String.format("Overall: %.2f / 10", overall));
    }

    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        String evaluationId = model.getValueAt(r, 0).toString();

        Evaluation e;
        try {
            e = service.findById(evaluationId);
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
            return;
        }
        if (e == null) return;

        // ID
        idField.setText(e.getEvaluationId());
        idField.setEditable(false);

        // Startup dropdown
        selectComboById(startupCombo, e.getStartupId());

        // Sliders
        teamSlider.setValue(e.getTeamScore());
        problemSlider.setValue(e.getProblemValidationScore());
        productSlider.setValue(e.getProductDevelopmentScore());
        marketSlider.setValue(e.getMarketValidationScore());
        businessSlider.setValue(e.getBusinessModelScore());
        tractionSlider.setValue(e.getTractionScore());
        revenueSlider.setValue(e.getRevenueScore());
        pitchSlider.setValue(e.getPitchReadinessScore());

        updateOverall();
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

    private void onEvaluate() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Evaluation ID is required."); return; }
            if (startupCombo.getSelectedItem() == null) {
                UiTheme.showError(this, "Select a startup."); return;
            }
            String startupId = startupCombo.getSelectedItem().toString().split(" ")[0];

            Evaluation e = service.evaluate(id, startupId,
                    teamSlider.getValue(), problemSlider.getValue(), productSlider.getValue(),
                    marketSlider.getValue(), businessSlider.getValue(), tractionSlider.getValue(),
                    revenueSlider.getValue(), pitchSlider.getValue());

            refresh();
            clearForm();
            UiTheme.showInfo(this,
                    String.format("Evaluation saved. Overall: %.2f / 10", e.getOverallScore()));
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onDelete() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select an evaluation."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete evaluation " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.delete(id);
            refresh(); clearForm();
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void clearForm() {
        idField.setText(""); idField.setEditable(true);
        teamSlider.setValue(5); problemSlider.setValue(5);
        productSlider.setValue(5); marketSlider.setValue(5);
        businessSlider.setValue(5); tractionSlider.setValue(5);
        revenueSlider.setValue(5); pitchSlider.setValue(5);
        updateOverall();
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

    private void populate(List<Evaluation> list) {
        model.setRowCount(0);
        try {
            List<Startup> startups = startupService.getAll();

            for (Evaluation e : list) {
                // Look up the startup name (fall back to ID if not found)
                String startupLabel = e.getStartupId();
                if (e.getStartupId() != null) {
                    for (Startup s : startups) {
                        if (s.getStartupId().equals(e.getStartupId())) {
                            startupLabel = s.getStartupName();
                            break;
                        }
                    }
                }

                model.addRow(new Object[]{
                        e.getEvaluationId(),
                        startupLabel,
                        e.getTeamScore(),
                        e.getProblemValidationScore(),
                        e.getProductDevelopmentScore(),
                        e.getMarketValidationScore(),
                        e.getBusinessModelScore(),
                        e.getTractionScore(),
                        e.getRevenueScore(),
                        e.getPitchReadinessScore(),
                        String.format("%.2f", e.getOverallScore()),
                        e.getEvaluatedDate()
                });
            }
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }
}