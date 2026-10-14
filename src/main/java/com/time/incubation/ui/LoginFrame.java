package com.time.incubation.ui;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.User;
import com.time.incubation.model.UserRole;
import com.time.incubation.repository.*;
import com.time.incubation.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.ExecutionException;

/**
 * Modern login screen: gradient brand panel on the left, clean card-style form on the right.
 * Constructor signature and login logic are unchanged, so no other class needs editing.
 */
public class LoginFrame extends JFrame {

    // ---------- Palette (self-contained, no dependency on UiTheme) ----------
    private static final Color PRIMARY       = new Color(0x4F46E5);
    private static final Color PRIMARY_HOVER = new Color(0x4338CA);
    private static final Color VIOLET        = new Color(0x7C3AED);
    private static final Color BG_DARK_1     = new Color(0x1E1B4B);
    private static final Color BG_DARK_2     = new Color(0x4338CA);
    private static final Color CYAN          = new Color(0x22D3EE);
    private static final Color TEXT          = new Color(0x0F172A);
    private static final Color TEXT_MUTED    = new Color(0x64748B);
    private static final Color FIELD_BG      = new Color(0xF1F5F9);
    private static final Color FIELD_BORDER  = new Color(0xE2E8F0);
    private static final Color ERROR_BG      = new Color(0xFEE2E2);
    private static final Color ERROR_TEXT    = new Color(0xB91C1C);

    private static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    // ---------- Dependencies ----------
    private final AuthService authService;

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final FounderRepository founderRepository;
    private final MentorRepository mentorRepository;
    private final MilestoneRepository milestoneRepository;
    private final MeetingRepository meetingRepository;
    private final EvaluationRepository evaluationRepository;
    private final ProgressReportRepository progressReportRepository;

    // ---------- UI state ----------
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ErrorBanner errorLabel;
    private GradientButton loginButton;

    public LoginFrame(AuthService authService,
                      UserRepository userRepository,
                      StartupRepository startupRepository,
                      FounderRepository founderRepository,
                      MentorRepository mentorRepository,
                      MilestoneRepository milestoneRepository,
                      MeetingRepository meetingRepository,
                      EvaluationRepository evaluationRepository,
                      ProgressReportRepository progressReportRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.startupRepository = startupRepository;
        this.founderRepository = founderRepository;
        this.mentorRepository = mentorRepository;
        this.milestoneRepository = milestoneRepository;
        this.meetingRepository = meetingRepository;
        this.evaluationRepository = evaluationRepository;
        this.progressReportRepository = progressReportRepository;
        initUI();
    }

    private void initUI() {
        setTitle("TIME Incubation & Startup Tracking System — Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().setDefaultButton(loginButton);
    }

    // =====================================================================
    //  LEFT: brand panel
    // =====================================================================
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setPaint(new GradientPaint(0, 0, BG_DARK_1, w, h, BG_DARK_2));
                g2.fillRect(0, 0, w, h);

                // decorative translucent circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-120, h - 260, 340, 340);
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 40));
                g2.fillOval(w - 140, -90, 260, 260);
                g2.setColor(new Color(VIOLET.getRed(), VIOLET.getGreen(), VIOLET.getBlue(), 90));
                g2.fillOval(w - 200, h - 220, 300, 300);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(60, 40, 120, 120);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 52, 0, 40));

        LogoBadge logo = new LogoBadge();
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("TIME Incubation");
        brand.setFont(font(Font.BOLD, 15));
        brand.setForeground(new Color(0xC7D2FE));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(new EmptyBorder(18, 0, 0, 0));

        JLabel headline = new JLabel("<html>Build startups.<br>Track progress.<br>Grow together.</html>");
        headline.setFont(font(Font.BOLD, 34));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        headline.setBorder(new EmptyBorder(14, 0, 14, 0));

        JLabel sub = new JLabel("<html><div style='width:280px'>One workspace for startups, founders, mentors, "
                + "milestones, meetings and evaluations.</div></html>");
        sub.setFont(font(Font.PLAIN, 14));
        sub.setForeground(new Color(0xA5B4FC));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(brand);
        panel.add(headline);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(30));
        panel.add(featureRow("Startup & founder management"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(featureRow("Milestones & progress reports"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(featureRow("Mentor meetings & evaluations"));
        panel.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("© TIME Institute");
        footer.setFont(font(Font.PLAIN, 12));
        footer.setForeground(new Color(0x818CF8));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footer);
        panel.add(Box.createVerticalStrut(26));

        return panel;
    }

    private JLabel featureRow(String text) {
        JLabel l = new JLabel(text, new DotIcon(CYAN, 10), SwingConstants.LEFT);
        l.setIconTextGap(12);
        l.setFont(font(Font.PLAIN, 14));
        l.setForeground(Color.WHITE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // =====================================================================
    //  RIGHT: form
    // =====================================================================
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 430));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(font(Font.BOLD, 28));
        welcome.setForeground(TEXT);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account to continue");
        subtitle.setFont(font(Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(6, 0, 30, 0));

        // Username
        JLabel userLabel = fieldLabel("Username");
        usernameField = new JTextField();
        FieldShell userShell = new FieldShell(usernameField, null);

        // Password (with show/hide toggle)
        JLabel pwLabel = fieldLabel("Password");
        pwLabel.setBorder(new EmptyBorder(18, 0, 8, 0));
        passwordField = new JPasswordField();
        final char defaultEcho = passwordField.getEchoChar();
        passwordField.addActionListener(this::onLogin);

        JLabel toggle = new JLabel("Show");
        toggle.setFont(font(Font.BOLD, 12));
        toggle.setForeground(PRIMARY);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean hidden = passwordField.getEchoChar() != (char) 0;
                passwordField.setEchoChar(hidden ? (char) 0 : defaultEcho);
                toggle.setText(hidden ? "Hide" : "Show");
            }
        });
        FieldShell pwShell = new FieldShell(passwordField, toggle);

        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        // Error banner
        errorLabel = new ErrorBanner();

        // Button
        loginButton = new GradientButton("Sign In");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(340, 48));
        loginButton.setPreferredSize(new Dimension(340, 48));
        loginButton.addActionListener(this::onLogin);

        JLabel hint = new JLabel("Secure access for authorized TIME staff only");
        hint.setFont(font(Font.PLAIN, 12));
        hint.setForeground(new Color(0x94A3B8));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(18, 0, 0, 0));

        form.add(welcome);
        form.add(subtitle);
        form.add(userLabel);
        form.add(userShell);
        form.add(pwLabel);
        form.add(pwShell);
        form.add(Box.createVerticalStrut(14));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(14));
        form.add(loginButton);
        form.add(hint);

        outer.add(form);
        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(new Color(0x334155));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    // =====================================================================
    //  Login logic (same behaviour as before, but off the UI thread)
    // =====================================================================
    private void onLogin(ActionEvent e) {
        final String username = usernameField.getText().trim();
        final String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        errorLabel.setText(" ");
        loginButton.setEnabled(false);
        loginButton.setText("Signing in…");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user == null) {
                        errorLabel.setText("Invalid username or password.");
                        resetButton();
                        return;
                    }
                    openMainFrame(user);
                } catch (ExecutionException ex) {
                    Throwable t = ex.getCause();
                    if (t instanceof DataBaseException) {
                        logDbError((DataBaseException) t);
                        errorLabel.setText("Login failed: " + t.getMessage());
                    } else {
                        t.printStackTrace();
                        errorLabel.setText("Unexpected error: " + t.getMessage());
                    }
                    resetButton();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    resetButton();
                }
            }
        }.execute();
    }

    private void resetButton() {
        loginButton.setEnabled(true);
        loginButton.setText("Sign In");
    }

    private void openMainFrame(User user) {
        dispose();
        if (user.getRole() == UserRole.ADMIN) {
            new AdminMainFrame(user, authService,
                    userRepository, startupRepository, founderRepository,
                    mentorRepository, milestoneRepository, meetingRepository,
                    evaluationRepository, progressReportRepository).setVisible(true);
        } else {
            new ManagerMainFrame(user, authService,
                    userRepository, startupRepository, founderRepository,
                    mentorRepository, milestoneRepository, meetingRepository,
                    evaluationRepository, progressReportRepository).setVisible(true);
        }
    }

    private void logDbError(DataBaseException ex) {
        System.err.println("========== LOGIN ERROR ==========");
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Class:   " + ex.getClass().getName());
        Throwable cause = ex.getCause();
        while (cause != null) {
            System.err.println("----");
            System.err.println("Caused by: " + cause.getClass().getName());
            System.err.println("           " + cause.getMessage());
            cause = cause.getCause();
        }
        System.err.println("=================================");
        ex.printStackTrace();
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** Rounded input shell with focus ring; wraps a text component and an optional trailing component. */
    private static class FieldShell extends JPanel {
        private boolean focused;

        FieldShell(JTextComponent input, JComponent trailing) {
            super(new BorderLayout(8, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(340, 46));
            setMaximumSize(new Dimension(340, 46));

            input.setOpaque(false);
            input.setBorder(null);
            input.setFont(font(Font.PLAIN, 15));
            input.setForeground(TEXT);
            input.setCaretColor(PRIMARY);
            input.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
            });

            add(input, BorderLayout.CENTER);
            if (trailing != null) add(trailing, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (focused) {
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 13, 13));
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.6f));
                g2.draw(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 13, 13));
            } else {
                g2.setColor(FIELD_BG);
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14));
                g2.setColor(FIELD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded pill that only paints when it has text; reserves its space so the layout never jumps. */
    private static class ErrorBanner extends JLabel {
        ErrorBanner() {
            super(" ");
            setFont(font(Font.PLAIN, 13));
            setForeground(ERROR_TEXT);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(340, 38));
            setMaximumSize(new Dimension(340, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            String t = getText();
            if (t == null || t.isBlank()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ERROR_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Gradient rounded button with hover / pressed / disabled states. */
    private static class GradientButton extends JButton {
        private boolean hover;

        GradientButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            Color c1 = hover ? PRIMARY_HOVER : PRIMARY;
            Color c2 = hover ? PRIMARY : VIOLET;
            if (!isEnabled()) {
                c1 = new Color(0xA5B4FC);
                c2 = new Color(0xC4B5FD);
            }
            if (isEnabled() && !getModel().isPressed()) {
                // soft shadow
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 45));
                g2.fill(new RoundRectangle2D.Float(2, 5, w - 4, h - 4, 16, 16));
            }
            int dy = getModel().isPressed() ? 1 : 0;
            g2.setPaint(new GradientPaint(0, 0, c1, w, 0, c2));
            g2.fill(new RoundRectangle2D.Float(0, dy, w, h - 4, 16, 16));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded gradient logo tile with a "T". */
    private static class LogoBadge extends JComponent {
        LogoBadge() {
            Dimension d = new Dimension(56, 56);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, CYAN, getWidth(), getHeight(), VIOLET));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.setColor(Color.WHITE);
            g2.setFont(font(Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            String s = "T";
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Small filled circle used as a bullet. */
    private static class DotIcon implements Icon {
        private final Color color;
        private final int size;

        DotIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
            g2.fillOval(x - 3, y - 3, size + 6, size + 6);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }
    }
}