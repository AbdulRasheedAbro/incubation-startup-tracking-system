package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.Meeting;
import com.time.incubation.model.MeetingType;
import com.time.incubation.model.Mentor;
import com.time.incubation.model.Startup;
import com.time.incubation.service.MeetingService;
import com.time.incubation.service.MentorService;
import com.time.incubation.service.StartupService;
import com.time.incubation.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MeetingPanel extends JPanel {

    private final MeetingService service;
    private final StartupService startupService;
    private final MentorService mentorService;

    private DefaultTableModel model;
    private JTable table;

    private JTextField idField, dateField, timeField;
    private JTextField notesField, nextActionField, nextDateField;
    private JComboBox<String> startupCombo;
    private JComboBox<String> mentorCombo;
    private JComboBox<MeetingType> typeCombo;

    public MeetingPanel(MeetingService service,
                        StartupService startupService,
                        MentorService mentorService) {
        this.service = service;
        this.startupService = startupService;
        this.mentorService = mentorService;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Meetings", "Track meetings between TIME, mentors, and startups"), BorderLayout.NORTH);

        // The whole page (table + form + buttons) lives in ONE scroll pane,
        // so scrolling down always reaches the Add / Update / Delete / Clear Form buttons.
        ScrollableBody body = new ScrollableBody(new BorderLayout(0, 12));
        body.setBackground(UiTheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildFormCard(), BorderLayout.SOUTH);

        JScrollPane pageScroll = new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pageScroll.setBorder(null);
        pageScroll.getViewport().setBackground(UiTheme.BG_LIGHT);
        pageScroll.getVerticalScrollBar().setUnitIncrement(20);
        add(pageScroll, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());
        // Fixed table height: the table has its own scrollbar for long lists
        card.setPreferredSize(new Dimension(100, 260));

        model = new DefaultTableModel(
                new Object[]{"ID", "Startup", "Mentor", "Date", "Type", "Next Meeting"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        JLabel heading = new JLabel("Meeting Details");
        heading.setFont(UiTheme.FONT_SUBHEADING);

        idField = new JTextField();
        dateField = new JTextField("YYYY-MM-DD");
        timeField = new JTextField("HH:MM");
        notesField = new JTextField();
        nextActionField = new JTextField();
        nextDateField = new JTextField("YYYY-MM-DD");
        startupCombo = new JComboBox<>();
        mentorCombo = new JComboBox<>();
        typeCombo = new JComboBox<>(MeetingType.values());

        UiTheme.styleField(idField); UiTheme.styleField(dateField);
        UiTheme.styleField(timeField); UiTheme.styleField(notesField);
        UiTheme.styleField(nextActionField); UiTheme.styleField(nextDateField);
        UiTheme.styleCombo(startupCombo); UiTheme.styleCombo(mentorCombo);
        UiTheme.styleCombo(typeCombo);

        // 3 rows x 6 columns (the old 4th row was empty and only wasted height)
        JPanel grid = new JPanel(new GridLayout(3, 6, 8, 8));
        grid.setOpaque(false);
        grid.add(new JLabel("ID:")); grid.add(idField);
        grid.add(new JLabel("Startup:")); grid.add(startupCombo);
        grid.add(new JLabel("Mentor:")); grid.add(mentorCombo);

        grid.add(new JLabel("Date:")); grid.add(dateField);
        grid.add(new JLabel("Time:")); grid.add(timeField);
        grid.add(new JLabel("Type:")); grid.add(typeCombo);

        grid.add(new JLabel("Notes:")); grid.add(notesField);
        grid.add(new JLabel("Next Action:")); grid.add(nextActionField);
        grid.add(new JLabel("Next Date:")); grid.add(nextDateField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        buttons.setOpaque(false);

        JButton add = UiTheme.primaryButton("Add");
        add.addActionListener(e -> onAdd());
        JButton update = UiTheme.secondaryButton("Update");
        update.addActionListener(e -> onUpdate());
        JButton delete = UiTheme.dangerButton("Delete");
        delete.addActionListener(e -> onDelete());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(clear);

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        String meetingId = model.getValueAt(r, 0).toString();

        Meeting m;
        try {
            m = service.findById(meetingId);
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
            return;
        }
        if (m == null) return;

        idField.setText(m.getMeetingId());
        idField.setEditable(false);

        dateField.setText(m.getMeetingDate() != null ? m.getMeetingDate().toString() : "");
        timeField.setText(m.getMeetingTime() != null ? m.getMeetingTime() : "");

        if (m.getMeetingType() != null) {
            typeCombo.setSelectedItem(m.getMeetingType());
        }

        selectComboById(startupCombo, m.getStartupId());

        if (m.getMentorId() == null || m.getMentorId().isBlank()) {
            mentorCombo.setSelectedItem("None");
        } else {
            selectComboById(mentorCombo, m.getMentorId());
        }

        notesField.setText(m.getDiscussionNotes() != null ? m.getDiscussionNotes() : "");
        nextActionField.setText(m.getNextAction() != null ? m.getNextAction() : "");
        nextDateField.setText(m.getNextMeetingDate() != null
                ? m.getNextMeetingDate().toString() : "");
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
            service.scheduleMeeting(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Meeting added.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onUpdate() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) { UiTheme.showError(this, "Select a meeting first."); return; }
            service.updateMeeting(buildFromForm());
            refresh(); clearForm();
            UiTheme.showInfo(this, "Meeting updated.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onDelete() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a meeting first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete meeting " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.deleteMeeting(id);
            refresh(); clearForm();
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private Meeting buildFromForm() {
        String startupId = startupCombo.getSelectedItem() == null ? null
                : startupCombo.getSelectedItem().toString().split(" ")[0];
        String mentorId = mentorCombo.getSelectedItem() == null ? null
                : mentorCombo.getSelectedItem().toString().split(" ")[0];

        if ("None".equals(mentorId)) mentorId = null;

        return new Meeting(
                idField.getText().trim(),
                startupId,
                mentorId,
                null,
                parseOrThrow(dateField.getText().trim(), "Meeting date"),
                emptyOrNull(timeField.getText()),
                (MeetingType) typeCombo.getSelectedItem(),
                emptyOrNull(notesField.getText()),
                emptyOrNull(nextActionField.getText()),
                parseOrNull(nextDateField.getText()));
    }

    private static LocalDate parseOrThrow(String s, String field) {
        try { return LocalDate.parse(s); }
        catch (Exception e) { throw new IllegalArgumentException(field + " must be YYYY-MM-DD."); }
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
        dateField.setText("YYYY-MM-DD"); timeField.setText("HH:MM");
        notesField.setText(""); nextActionField.setText("");
        nextDateField.setText("YYYY-MM-DD");
        typeCombo.setSelectedIndex(0);
        if (startupCombo.getItemCount() > 0) startupCombo.setSelectedIndex(0);
        if (mentorCombo.getItemCount() > 0) mentorCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    public void refresh() {
        try {
            startupCombo.removeAllItems();
            for (Startup s : startupService.getAll()) {
                startupCombo.addItem(s.getStartupId() + " — " + s.getStartupName());
            }
            mentorCombo.removeAllItems();
            mentorCombo.addItem("None");
            for (Mentor m : mentorService.getAll()) {
                mentorCombo.addItem(m.getMentorId() + " — " + m.getFullName());
            }
            populate(service.getAll());
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    private void populate(List<Meeting> list) {
        model.setRowCount(0);
        try {
            for (Meeting m : list) {
                // Look up the startup name
                String startupLabel = m.getStartupId();
                if (m.getStartupId() != null) {
                    for (Startup s : startupService.getAll()) {
                        if (s.getStartupId().equals(m.getStartupId())) {
                            startupLabel = s.getStartupName();
                            break;
                        }
                    }
                }

                // Look up the mentor name
                String mentorLabel;
                if (m.getMentorId() == null || m.getMentorId().isBlank()) {
                    mentorLabel = "—";
                } else {
                    mentorLabel = m.getMentorId();
                    for (Mentor mentor : mentorService.getAll()) {
                        if (mentor.getMentorId().equals(m.getMentorId())) {
                            mentorLabel = mentor.getFullName();
                            break;
                        }
                    }
                }

                model.addRow(new Object[]{
                        m.getMeetingId(),
                        startupLabel,
                        mentorLabel,
                        m.getMeetingDate(),
                        m.getMeetingType().name(),
                        m.getNextMeetingDate() == null ? "—" : m.getNextMeetingDate()
                });
            }
        } catch (DataBaseException e) {
            UiTheme.showError(this, e.getMessage());
        }
    }

    /**
     * Page container used inside the scroll pane.
     * - Window big enough  -> stretches to fill it (no scrollbar).
     * - Window too small   -> keeps its natural size and the scroll pane scrolls,
     *                         so you can scroll down to the buttons (or sideways if narrow).
     */
    private static class ScrollableBody extends JPanel implements Scrollable {
        ScrollableBody(LayoutManager layout) { super(layout); }

        @Override public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return 20;
        }

        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visible.height - 40 : visible.width - 40;
        }

        @Override public boolean getScrollableTracksViewportWidth() {
            Container p = getParent();
            return p instanceof JViewport && p.getWidth() >= getPreferredSize().width;
        }

        @Override public boolean getScrollableTracksViewportHeight() {
            Container p = getParent();
            return p instanceof JViewport && p.getHeight() >= getPreferredSize().height;
        }
    }
}