package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Evaluation;
import com.time.incubation.model.Mentor;
import com.time.incubation.model.Milestone;
import com.time.incubation.model.MilestoneStatus;
import com.time.incubation.model.Startup;
import com.time.incubation.model.StartupStage;
import com.time.incubation.model.StartupStatus;
import com.time.incubation.repository.EvaluationRepository;
import com.time.incubation.repository.MeetingRepository;
import com.time.incubation.repository.MentorRepository;
import com.time.incubation.repository.MilestoneRepository;
import com.time.incubation.repository.StartupRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final StartupRepository startupRepo;
    private final MentorRepository mentorRepo;
    private final MilestoneRepository milestoneRepo;
    private final MeetingRepository meetingRepo;
    private final EvaluationRepository evaluationRepo;

    public ReportService(StartupRepository startupRepo,
                         MentorRepository mentorRepo,
                         MilestoneRepository milestoneRepo,
                         MeetingRepository meetingRepo,
                         EvaluationRepository evaluationRepo) {
        this.startupRepo = startupRepo;
        this.mentorRepo = mentorRepo;
        this.milestoneRepo = milestoneRepo;
        this.meetingRepo = meetingRepo;
        this.evaluationRepo = evaluationRepo;
    }

    // ======================================================
    // 1. Startup Progress Report
    // ======================================================

    public Map<String, String> buildStartupProgressReport(String startupId)
            throws RecordNotFoundException, DataBaseException {
        try {
            Startup s = startupRepo.findById(startupId);
            if (s == null) {
                throw new RecordNotFoundException("Startup not found: " + startupId);
            }

            List<Milestone> milestones = milestoneRepo.findByStartup(startupId);
            long completed = milestones.stream()
                    .filter(m -> m.getStatus() == MilestoneStatus.COMPLETED)
                    .count();
            long pending = milestones.size() - completed;

            Evaluation eval = evaluationRepo.findLatestForStartup(startupId);

            List<Mentor> mentors = mentorRepo.findMentorsForStartup(startupId);
            String mentorNames = mentors.isEmpty()
                    ? "—"
                    : mentors.stream().map(Mentor::getFullName)
                    .reduce((a, b) -> a + ", " + b).orElse("—");

            Map<String, String> report = new LinkedHashMap<>();
            report.put("Startup ID",             s.getStartupId());
            report.put("Startup Name",           s.getStartupName());
            report.put("Founder",                nz(s.getFounderName()));
            report.put("Co-Founder",             nz(s.getCoFounderName()));
            report.put("Industry",               nz(s.getIndustry()));
            report.put("Current Stage",          s.getStage().name());
            report.put("Current Status",         s.getCurrentStatus().name());
            report.put("Incubation Start",       s.getIncubationStartDate().toString());
            report.put("Expected Graduation",    s.getExpectedGraduationDate() != null
                    ? s.getExpectedGraduationDate().toString() : "—");
            report.put("Mentors",                mentorNames);
            report.put("Completed Milestones",   String.valueOf(completed));
            report.put("Pending Milestones",     String.valueOf(pending));
            report.put("Overall Score",          eval != null
                    ? String.format("%.2f / 10", eval.getOverallScore())
                    : "Not evaluated");

            return report;

        } catch (SQLException e) {
            throw new DataBaseException("Failed to build startup progress report.", e);
        }
    }

    // ======================================================
    // 2. Mentor Report
    // ======================================================

    public Map<String, String> buildMentorReport(String mentorId)
            throws RecordNotFoundException, DataBaseException {
        try {
            Mentor mentor = mentorRepo.findById(mentorId);
            if (mentor == null) {
                throw new RecordNotFoundException("Mentor not found: " + mentorId);
            }

            int meetingsCount = meetingRepo.countForMentor(mentorId);

            // Assigned startups: iterate all startups and pick those this mentor is
            // linked to via startup_mentors
            StringBuilder startupsList = new StringBuilder();
            int assignedCount = 0;
            for (Startup s : startupRepo.findAll()) {
                List<Mentor> assigned = mentorRepo.findMentorsForStartup(s.getStartupId());
                for (Mentor m : assigned) {
                    if (m.getMentorId().equals(mentorId)) {
                        if (assignedCount > 0) startupsList.append(", ");
                        startupsList.append(s.getStartupName());
                        assignedCount++;
                        break;
                    }
                }
            }

            Map<String, String> report = new LinkedHashMap<>();
            report.put("Mentor ID",             mentor.getMentorId());
            report.put("Mentor Name",           mentor.getFullName());
            report.put("Expertise",             nz(mentor.getExpertise()));
            report.put("Organization",          nz(mentor.getOrganization()));
            report.put("Email",                 nz(mentor.getEmail()));
            report.put("Phone",                 nz(mentor.getPhone()));
            report.put("Assigned Startups",     String.valueOf(assignedCount));
            report.put("Startup List",          assignedCount == 0 ? "—" : startupsList.toString());
            report.put("Meetings Conducted",    String.valueOf(meetingsCount));

            return report;

        } catch (SQLException e) {
            throw new DataBaseException("Failed to build mentor report.", e);
        }
    }

    // ======================================================
    // 3. Incubation Performance Report
    // ======================================================

    public Map<String, String> buildIncubationPerformanceReport() throws DataBaseException {
        try {
            Map<String, String> report = new LinkedHashMap<>();

            report.put("Total Startups",       String.valueOf(startupRepo.countAll()));
            report.put("Active Startups",      String.valueOf(startupRepo.countByStatus(StartupStatus.ACTIVE)));
            report.put("Graduated Startups",   String.valueOf(startupRepo.countByStatus(StartupStatus.GRADUATED)));
            report.put("On Hold Startups",     String.valueOf(startupRepo.countByStatus(StartupStatus.ON_HOLD)));
            report.put("Dropped Startups",     String.valueOf(startupRepo.countByStatus(StartupStatus.DROPPED)));

            report.put("Idea Stage",           String.valueOf(startupRepo.countByStage(StartupStage.IDEA_STAGE)));
            report.put("Prototype Stage",      String.valueOf(startupRepo.countByStage(StartupStage.PROTOTYPE)));
            report.put("MVP Stage",            String.valueOf(startupRepo.countByStage(StartupStage.MVP)));
            report.put("Early Revenue Stage",  String.valueOf(startupRepo.countByStage(StartupStage.EARLY_REVENUE)));
            report.put("Growth Stage",         String.valueOf(startupRepo.countByStage(StartupStage.GROWTH_STAGE)));

            double avg = evaluationRepo.averageOverallScore();
            report.put("Average Evaluation Score",
                    String.format("%.2f / 10", avg));

            return report;

        } catch (SQLException e) {
            throw new DataBaseException("Failed to build incubation performance report.", e);
        }
    }

    // ======================================================
    // CSV export
    // ======================================================

    /**
     * Writes a two-column CSV of key/value pairs.
     * Suitable for any of the three reports above.
     */
    public void exportToCsv(Map<String, String> report, String filePath)
            throws DataBaseException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Field,Value\n");
            for (Map.Entry<String, String> entry : report.entrySet()) {
                writer.write(csv(entry.getKey()));
                writer.write(",");
                writer.write(csv(entry.getValue()));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new DataBaseException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    private static String csv(String value) {
        if (value == null) return "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}