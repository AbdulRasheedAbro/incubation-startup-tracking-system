package com.time.incubation.ui;

import com.time.incubation.model.User;
import com.time.incubation.repository.*;
import com.time.incubation.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Modern manager shell: gradient sidebar, header bar with page title, card-based content area.
 * Constructor signature is unchanged, so LoginFrame / other classes need no edits.
 */
public class ManagerMainFrame extends JFrame {

    // ---------- Palette (matches the new LoginFrame) ----------
    private static final Color PRIMARY    = new Color(0x4F46E5);
    private static final Color VIOLET     = new Color(0x7C3AED);
    private static final Color BG_DARK_1  = new Color(0x1E1B4B);
    private static final Color BG_DARK_2  = new Color(0x4338CA);
    private static final Color CYAN       = new Color(0x22D3EE);
    private static final Color TEXT       = new Color(0x0F172A);
    private static final Color TEXT_MUTED = new Color(0x64748B);
    private static final Color PAGE_BG    = new Color(0xF1F5F9);
    private static final Color LINE       = new Color(0xE2E8F0);

    private static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    // ---------- Dependencies ----------
    private final User currentUser;
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
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final Map<String, String> subtitles = new LinkedHashMap<>();

    public ManagerMainFrame(User currentUser,
                            AuthService authService,
                            UserRepository userRepository,
                            StartupRepository startupRepository,
                            FounderRepository founderRepository,
                            MentorRepository mentorRepository,
                            MilestoneRepository milestoneRepository,
                            MeetingRepository meetingRepository,
                            EvaluationRepository evaluationRepository,
                            ProgressReportRepository progressReportRepository) {
        this.currentUser = currentUser;
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
        setupWindowCloseHandler();
    }

    private void initUI() {
        setTitle("TIME Incubation — Incubation Manager");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1320, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        StartupService startupService = new StartupService(startupRepository, founderRepository);
        MentorService mentorService = new MentorService(mentorRepository, startupRepository);
        MilestoneService milestoneService = new MilestoneService(milestoneRepository, startupRepository);
        MeetingService meetingService = new MeetingService(
                meetingRepository, startupRepository, mentorRepository);
        EvaluationService evaluationService = new EvaluationService(
                evaluationRepository, startupRepository);
        DashboardService dashboardService = new DashboardService(
                startupRepository, mentorRepository, meetingRepository,
                milestoneRepository, evaluationRepository);
        ReportService reportService = new ReportService(
                startupRepository, mentorRepository, milestoneRepository,
                meetingRepository, evaluationRepository);

        subtitles.put("Dashboard", "Overview of your incubation program");
        subtitles.put("Startups", "Manage startups enrolled in the program");
        subtitles.put("Milestones", "Track startup milestones and deadlines");
        subtitles.put("Meetings", "Schedule and review meetings");
        subtitles.put("Evaluations", "Score and evaluate startup progress");
        subtitles.put("Reports", "Generate and export reports");

        contentArea.setBackground(PAGE_BG);
        contentArea.add(new DashboardPanel(dashboardService), "Dashboard");
        contentArea.add(new StartupPanel(startupService), "Startups");
        contentArea.add(new MilestonePanel(milestoneService, startupService), "Milestones");
        contentArea.add(new MeetingPanel(meetingService, startupService, mentorService), "Meetings");
        contentArea.add(new EvaluationPanel(evaluationService, startupService), "Evaluations");
        contentArea.add(new ReportPanel(reportService, startupService, mentorService), "Reports");

        add(buildSidebar(subtitles.keySet().toArray(new String[0])), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(PAGE_BG);
        right.add(buildHeader(), BorderLayout.NORTH);
        right.add(contentArea, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        selectNav("Dashboard");
    }

    // =====================================================================
    //  Sidebar
    // =====================================================================
    private JPanel buildSidebar(String[] items) {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, BG_DARK_1, w, h, BG_DARK_2));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillOval(-100, h - 220, 300, 300);
                g2.setColor(new Color(VIOLET.getRed(), VIOLET.getGreen(), VIOLET.getBlue(), 70));
                g2.fillOval(w - 120, h - 140, 220, 220);
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(255, 0));

        // Brand block
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 22, 6, 22));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(new LogoBadge(42));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setBorder(new EmptyBorder(0, 12, 0, 0));
        JLabel appName = new JLabel("TIME Incubation");
        appName.setFont(font(Font.BOLD, 16));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("Startup Tracking");
        appSub.setFont(font(Font.PLAIN, 12));
        appSub.setForeground(new Color(0xA5B4FC));
        brandText.add(appName);
        brandText.add(appSub);
        brand.add(brandText);
        sidebar.add(brand);

        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(font(Font.BOLD, 11));
        menuLabel.setForeground(new Color(0x818CF8));
        menuLabel.setBorder(new EmptyBorder(22, 26, 10, 0));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menuLabel);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(0, 14, 0, 14));
        nav.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String item : items) {
            NavButton b = new NavButton(item);
            b.addActionListener(e -> selectNav(item));
            navButtons.put(item, b);
            nav.add(b);
            nav.add(Box.createVerticalStrut(4));
        }
        sidebar.add(nav);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildUserCard());
        return sidebar;
    }

    private JPanel buildUserCard() {
        String name = displayName();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        card.add(new Avatar(initials(name), 40), BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(font(Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel("Incubation Manager");
        roleLabel.setFont(font(Font.PLAIN, 12));
        roleLabel.setForeground(new Color(0xA5B4FC));
        info.add(Box.createVerticalGlue());
        info.add(nameLabel);
        info.add(roleLabel);
        info.add(Box.createVerticalGlue());
        card.add(info, BorderLayout.CENTER);

        OutlineButton logout = new OutlineButton("Log out");
        logout.addActionListener(e -> handleLogout());
        card.add(logout, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(10, 14, 20, 14));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    // =====================================================================
    //  Header
    // =====================================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                new EmptyBorder(14, 32, 14, 32)));

        JLabel hello = new JLabel("Hello, " + firstName(displayName()) + "");
        hello.setFont(font(Font.BOLD, 18));
        hello.setForeground(TEXT);
        header.add(hello, BorderLayout.WEST);

        JLabel date = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        date.setFont(font(Font.PLAIN, 13));
        date.setForeground(TEXT_MUTED);
        header.add(date, BorderLayout.EAST);

        return header;
    }

    // =====================================================================
    //  Behaviour
    // =====================================================================
    private void selectNav(String name) {
        cardLayout.show(contentArea, name);
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(name));
        }
    }

    private void setupWindowCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int c = JOptionPane.showConfirmDialog(ManagerMainFrame.this,
                        "Exit the application?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    private void handleLogout() {
        int c = JOptionPane.showConfirmDialog(this,
                "Log out and return to the login screen?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;

        LoginFrame login = new LoginFrame(authService,
                userRepository, startupRepository, founderRepository,
                mentorRepository, milestoneRepository, meetingRepository,
                evaluationRepository, progressReportRepository);
        login.setVisible(true);
        dispose();
    }

    // ---------- helpers ----------
    private String displayName() {
        String n = currentUser.getFullName();
        if (n == null || n.isBlank()) n = currentUser.getUsername();
        return n == null ? "User" : n.trim();
    }

    private static String firstName(String full) {
        int i = full.indexOf(' ');
        return i > 0 ? full.substring(0, i) : full;
    }

    private static String initials(String full) {
        String[] parts = full.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.length() == 0 ? "U" : sb.toString();
    }

    // =====================================================================
    //  Custom components
    // =====================================================================

    /** Sidebar navigation item with hover and active states. */
    private static class NavButton extends JButton {
        private boolean active;
        private boolean hover;

        NavButton(String text) {
            super(text);
            setFont(font(Font.PLAIN, 14));
            setForeground(new Color(0xC7D2FE));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 46, 0, 12));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(220, 44));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        void setActive(boolean a) {
            this.active = a;
            setForeground(a ? Color.WHITE : new Color(0xC7D2FE));
            setFont(font(a ? Font.BOLD : Font.PLAIN, 14));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (active) {
                g2.setColor(new Color(255, 255, 255, 36));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                g2.setColor(CYAN);
                g2.fill(new RoundRectangle2D.Float(0, 10, 4, h - 20, 4, 4));
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
            }

            // bullet
            int d = 8;
            int cy = (h - d) / 2;
            g2.setColor(active ? CYAN : new Color(0x818CF8));
            g2.fillOval(22, cy, d, d);
            if (active) {
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 70));
                g2.fillOval(19, cy - 3, d + 6, d + 6);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Pill-shaped outlined button used for log out. */
    private static class OutlineButton extends JButton {
        private boolean hover;

        OutlineButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setPreferredSize(new Dimension(100, 34));
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
            if (hover) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            }
            g2.setColor(new Color(255, 255, 255, 90));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded gradient logo tile with a "T". */
    private static class LogoBadge extends JComponent {
        private final int size;

        LogoBadge(int size) {
            this.size = size;
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, CYAN, size, size, VIOLET));
            g2.fill(new RoundRectangle2D.Float(0, 0, size, size, 14, 14));
            g2.setColor(Color.WHITE);
            g2.setFont(font(Font.BOLD, size / 2 + 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("T", (size - fm.stringWidth("T")) / 2,
                    (size - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Circular avatar with initials. */
    private static class Avatar extends JComponent {
        private final String text;
        private final int size;

        Avatar(String text, int size) {
            this.text = text;
            this.size = size;
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, CYAN, size, size, PRIMARY));
            g2.fillOval(0, 0, size, size);
            g2.setColor(Color.WHITE);
            g2.setFont(font(Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (size - fm.stringWidth(text)) / 2,
                    (size - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}