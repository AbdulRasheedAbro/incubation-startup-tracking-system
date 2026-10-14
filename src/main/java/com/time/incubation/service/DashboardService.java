package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.model.StartupStage;
import com.time.incubation.model.StartupStatus;
import com.time.incubation.repository.EvaluationRepository;
import com.time.incubation.repository.MeetingRepository;
import com.time.incubation.repository.MentorRepository;
import com.time.incubation.repository.MilestoneRepository;
import com.time.incubation.repository.StartupRepository;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {

    private final StartupRepository startupRepo;
    private final MentorRepository mentorRepo;
    private final MeetingRepository meetingRepo;
    private final MilestoneRepository milestoneRepo;
    private final EvaluationRepository evaluationRepo;

    public DashboardService(StartupRepository startupRepo,
                            MentorRepository mentorRepo,
                            MeetingRepository meetingRepo,
                            MilestoneRepository milestoneRepo,
                            EvaluationRepository evaluationRepo) {
        this.startupRepo = startupRepo;
        this.mentorRepo = mentorRepo;
        this.meetingRepo = meetingRepo;
        this.milestoneRepo = milestoneRepo;
        this.evaluationRepo = evaluationRepo;
    }

    /**
     * All eleven values the task requires, in one call.
     */
    public Map<String, Integer> getAllStats() throws DataBaseException {
        try {
            Map<String, Integer> stats = new LinkedHashMap<>();

            stats.put("Total Startups",     startupRepo.countAll());
            stats.put("Active Startups",    startupRepo.countByStatus(StartupStatus.ACTIVE));
            stats.put("Graduated",          startupRepo.countByStatus(StartupStatus.GRADUATED));
            stats.put("On Hold",            startupRepo.countByStatus(StartupStatus.ON_HOLD));

            stats.put("Idea Stage",         startupRepo.countByStage(StartupStage.IDEA_STAGE));
            stats.put("MVP Stage",          startupRepo.countByStage(StartupStage.MVP));
            stats.put("Early Revenue",      startupRepo.countByStage(StartupStage.EARLY_REVENUE));

            stats.put("Total Mentors",      mentorRepo.countAll());
            stats.put("Total Meetings",     meetingRepo.countAll());
            stats.put("Upcoming Meetings",  meetingRepo.countUpcoming());
            stats.put("Delayed Milestones", milestoneRepo.countDelayed());

            return stats;

        } catch (SQLException e) {
            throw new DataBaseException("Failed to load dashboard stats.", e);
        }
    }

    public double averageEvaluationScore() throws DataBaseException {
        try {
            return evaluationRepo.averageOverallScore();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load average score.", e);
        }
    }
}