package com.time.incubation;

import com.formdev.flatlaf.FlatLightLaf;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.model.UserRole;
import com.time.incubation.repository.EvaluationRepository;
import com.time.incubation.repository.FounderRepository;
import com.time.incubation.repository.MeetingRepository;
import com.time.incubation.repository.MentorRepository;
import com.time.incubation.repository.MilestoneRepository;
import com.time.incubation.repository.ProgressReportRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.repository.UserRepository;
import com.time.incubation.service.AuthService;
import com.time.incubation.ui.LoginFrame;
import com.time.incubation.util.DatabaseConnection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Table.rowHeight", 32);

            try {
                DatabaseConnection.initialize();
                System.out.println("[Main] Database initialized successfully.");
            } catch (RuntimeException e) {
                System.err.println("========== DATABASE ERROR ==========");
                System.err.println("Message: " + e.getMessage());
                System.err.println("Class:   " + e.getClass().getName());

                Throwable cause = e.getCause();
                while (cause != null) {
                    System.err.println("----");
                    System.err.println("Caused by: " + cause.getClass().getName());
                    System.err.println("           " + cause.getMessage());
                    cause = cause.getCause();
                }
                System.err.println("====================================");

                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "Database initialization failed:\n" + e.getMessage()
                                + "\n\nSee IntelliJ's Run panel for the full error.",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Build repositories
            UserRepository userRepository = new UserRepository();
            StartupRepository startupRepository = new StartupRepository();
            FounderRepository founderRepository = new FounderRepository();
            MentorRepository mentorRepository = new MentorRepository();
            MilestoneRepository milestoneRepository = new MilestoneRepository();
            MeetingRepository meetingRepository = new MeetingRepository();
            EvaluationRepository evaluationRepository = new EvaluationRepository();
            ProgressReportRepository progressReportRepository = new ProgressReportRepository();

            // Build the auth service
            AuthService authService = new AuthService(userRepository);

            // Open the login screen
            LoginFrame loginFrame = new LoginFrame(
                    authService,
                    userRepository,
                    startupRepository,
                    founderRepository,
                    mentorRepository,
                    milestoneRepository,
                    meetingRepository,
                    evaluationRepository,
                    progressReportRepository
            );
            try {
                authService.registerUser(
                        "U001",           // user ID
                        "abdurrafay",     // username
                        "abdurrafay",     // password (stored hashed, not as plain text)
                        UserRole.ADMIN,
                        "Abdur Rafay",    // full name
                        null);            // email (optional)
            } catch (DuplicateIdException ignored) {
                // admin already exists, nothing to do
            } catch (Exception e) {
                e.printStackTrace();
            }
            loginFrame.setVisible(true);
        });
    }
}