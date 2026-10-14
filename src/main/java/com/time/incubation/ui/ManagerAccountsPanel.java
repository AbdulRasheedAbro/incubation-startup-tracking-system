package com.time.incubation.ui;

import com.time.incubation.model.User;
import com.time.incubation.model.UserRole;
import com.time.incubation.repository.UserRepository;
import com.time.incubation.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.UUID;

/**
 * Admin-only page: list manager accounts, add a new manager, remove a manager.
 */
public class ManagerAccountsPanel extends JPanel {

    private final UserRepository userRepository;
    private final AuthService authService;

    private DefaultTableModel model;
    private JTable table;

    private JTextField fullNameField, usernameField, emailField;
    private JPasswordField passwordField, confirmField;

    public ManagerAccountsPanel(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;

        setLayout(new BorderLayout());
        setBackground(UiTheme.BG_LIGHT);
        add(UiTheme.banner("Managers", "Add or remove incubation manager accounts"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UiTheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildFormCard(), BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        refresh();
    }

    // ---------------------------------------------------------------
    //  Table
    // ---------------------------------------------------------------
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());
        card.setPreferredSize(new Dimension(100, 200));

        model = new DefaultTableModel(
                new Object[]{"ID", "Full Name", "Username", "Email", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------------
    //  Form
    // ---------------------------------------------------------------
    private JPanel buildFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiTheme.cardBorder());

        JLabel heading = new JLabel("New Manager");
        heading.setFont(UiTheme.FONT_SUBHEADING);

        fullNameField = new JTextField();
        usernameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmField = new JPasswordField();

        UiTheme.styleField(fullNameField);
        UiTheme.styleField(usernameField);
        UiTheme.styleField(emailField);
        UiTheme.styleField(passwordField);
        UiTheme.styleField(confirmField);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        addPair(grid, "Full Name:",        fullNameField, 0, 0);
        addPair(grid, "Username:",         usernameField, 2, 0);
        addPair(grid, "Email:",            emailField,    4, 0);
        addPair(grid, "Password:",         passwordField, 0, 1);
        addPair(grid, "Confirm Password:", confirmField,  2, 1);

        JButton add = UiTheme.primaryButton("Add Manager");
        add.addActionListener(e -> onAdd());
        JButton remove = UiTheme.dangerButton("Remove Selected");
        remove.addActionListener(e -> onRemove());
        JButton clear = UiTheme.secondaryButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(remove);
        buttons.add(clear);

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private static void addPair(JPanel grid, String label, JComponent field, int x, int y) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = x; lc.gridy = y;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 0, 4, 8);
        grid.add(new JLabel(label), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = x + 1; fc.gridy = y;
        fc.weightx = 1;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(4, 0, 4, 20);
        grid.add(field, fc);
    }

    // ---------------------------------------------------------------
    //  Actions
    // ---------------------------------------------------------------
    private void onAdd() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            UiTheme.showError(this, "Full name, username and password are required.");
            return;
        }
        if (!password.equals(confirm)) {
            UiTheme.showError(this, "Passwords do not match.");
            return;
        }

        // Short auto-generated ID, e.g. "UA1B2C3"
        String userId = "U" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6).toUpperCase();

        try {
            // AuthService validates the input, checks duplicates and hashes the password.
            authService.registerUser(userId, username, password,
                    UserRole.MANAGER, fullName, email.isEmpty() ? null : email);
            refresh();
            clearForm();
            UiTheme.showInfo(this, "Manager \"" + username + "\" added.");
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void onRemove() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiTheme.showError(this, "Select a manager in the table first.");
            return;
        }
        String id = model.getValueAt(row, 0).toString();
        String username = model.getValueAt(row, 2).toString();

        int choice = JOptionPane.showConfirmDialog(this,
                "Remove manager \"" + username + "\"?\nThey will no longer be able to log in.",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            userRepository.delete(id);
            refresh();
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }

    private void clearForm() {
        fullNameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        table.clearSelection();
    }

    /** Reloads the table with all users whose role is MANAGER. */
    public void refresh() {
        try {
            model.setRowCount(0);
            for (User u : userRepository.findAll()) {
                if (u.getRole() != UserRole.MANAGER) continue;
                model.addRow(new Object[]{
                        u.getUserId(),
                        u.getFullName(),
                        u.getUsername(),
                        u.getEmail() == null ? "—" : u.getEmail(),
                        u.isActive() ? "Active" : "Inactive"
                });
            }
        } catch (Exception ex) {
            UiTheme.showError(this, ex.getMessage());
        }
    }   
}