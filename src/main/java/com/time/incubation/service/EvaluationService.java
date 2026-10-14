package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Evaluation;
import com.time.incubation.repository.EvaluationRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EvaluationService {

    private final EvaluationRepository repository;
    private final StartupRepository startupRepository;

    public EvaluationService(EvaluationRepository repository,
                             StartupRepository startupRepository) {
        this.repository = repository;
        this.startupRepository = startupRepository;
    }

    /**
     * Creates a new evaluation. The overall score is computed automatically
     * from the eight individual scores — the caller does not pass it.
     */
    public Evaluation evaluate(String evaluationId, String startupId,
                               int team, int problemValidation, int productDevelopment,
                               int marketValidation, int businessModel, int traction,
                               int revenue, int pitchReadiness)
            throws InvalidInputException, DuplicateIdException,
            RecordNotFoundException, DataBaseException {

        validateScores(team, problemValidation, productDevelopment, marketValidation,
                businessModel, traction, revenue, pitchReadiness);

        if (ValidationUtils.isNullOrEmpty(evaluationId)) {
            throw new InvalidInputException("Evaluation ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(startupId)) {
            throw new InvalidInputException("Startup ID is required.");
        }

        try {
            if (repository.findById(evaluationId) != null) {
                throw new DuplicateIdException("Evaluation ID already exists: " + evaluationId);
            }
            if (startupRepository.findById(startupId) == null) {
                throw new RecordNotFoundException("Startup not found: " + startupId);
            }

            double overall = Evaluation.computeOverall(
                    team, problemValidation, productDevelopment, marketValidation,
                    businessModel, traction, revenue, pitchReadiness);

            Evaluation e = new Evaluation(
                    evaluationId, startupId,
                    team, problemValidation, productDevelopment, marketValidation,
                    businessModel, traction, revenue, pitchReadiness,
                    overall, LocalDate.now());

            repository.save(e);
            return e;

        } catch (SQLException ex) {
            throw new DataBaseException("Failed to save evaluation.", ex);
        }
    }

    public void delete(String evaluationId)
            throws RecordNotFoundException, DataBaseException {
        try {
            if (repository.findById(evaluationId) == null) {
                throw new RecordNotFoundException("Evaluation not found: " + evaluationId);
            }
            repository.delete(evaluationId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to delete evaluation.", e);
        }
    }

    public Evaluation findById(String evaluationId)
            throws RecordNotFoundException, DataBaseException {
        try {
            Evaluation e = repository.findById(evaluationId);
            if (e == null) {
                throw new RecordNotFoundException("Evaluation not found: " + evaluationId);
            }
            return e;
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load evaluation.", e);
        }
    }

    public List<Evaluation> getAll() throws DataBaseException {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load evaluations.", e);
        }
    }

    public List<Evaluation> findByStartup(String startupId) throws DataBaseException {
        try {
            return repository.findByStartup(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load evaluations for startup.", e);
        }
    }

    /**
     * Most recent evaluation for a startup, or null if none exists.
     */
    public Evaluation findLatestForStartup(String startupId) throws DataBaseException {
        try {
            return repository.findLatestForStartup(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load latest evaluation.", e);
        }
    }

    public double averageOverallScore() throws DataBaseException {
        try {
            return repository.averageOverallScore();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to compute average score.", e);
        }
    }

    private void validateScores(int... scores) throws InvalidInputException {
        for (int score : scores) {
            if (!ValidationUtils.isValidScore(score)) {
                throw new InvalidInputException(
                        "All scores must be between 1 and 10. Got: " + score);
            }
        }
    }
}