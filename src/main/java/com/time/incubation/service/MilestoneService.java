package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Milestone;
import com.time.incubation.model.MilestoneStatus;
import com.time.incubation.repository.MilestoneRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MilestoneService {

    private final MilestoneRepository repository;
    private final StartupRepository startupRepository;

    public MilestoneService(MilestoneRepository repository,
                            StartupRepository startupRepository) {
        this.repository = repository;
        this.startupRepository = startupRepository;
    }

    public void addMilestone(Milestone m) throws InvalidInputException, DuplicateIdException,
            RecordNotFoundException, DataBaseException {

        validate(m);

        try {
            if (repository.findById(m.getMilestoneId()) != null) {
                throw new DuplicateIdException(
                        "Milestone ID already exists: " + m.getMilestoneId());
            }
            if (startupRepository.findById(m.getStartupId()) == null) {
                throw new RecordNotFoundException("Startup not found: " + m.getStartupId());
            }
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to save milestone.", e);
        }
    }

    public void updateMilestone(Milestone m) throws InvalidInputException,
            RecordNotFoundException, DataBaseException {

        validate(m);

        try {
            if (repository.findById(m.getMilestoneId()) == null) {
                throw new RecordNotFoundException(
                        "Milestone not found: " + m.getMilestoneId());
            }
            if (startupRepository.findById(m.getStartupId()) == null) {
                throw new RecordNotFoundException("Startup not found: " + m.getStartupId());
            }
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to update milestone.", e);
        }
    }

    public void deleteMilestone(String milestoneId)
            throws RecordNotFoundException, DataBaseException {
        try {
            if (repository.findById(milestoneId) == null) {
                throw new RecordNotFoundException("Milestone not found: " + milestoneId);
            }
            repository.delete(milestoneId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to delete milestone.", e);
        }
    }

    public Milestone findById(String milestoneId)
            throws RecordNotFoundException, DataBaseException {
        try {
            Milestone m = repository.findById(milestoneId);
            if (m == null) {
                throw new RecordNotFoundException("Milestone not found: " + milestoneId);
            }
            return m;
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load milestone.", e);
        }
    }

    public List<Milestone> getAll() throws DataBaseException {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load milestones.", e);
        }
    }

    public List<Milestone> findByStartup(String startupId) throws DataBaseException {
        try {
            return repository.findByStartup(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load milestones.", e);
        }
    }

    public List<Milestone> findByStatus(MilestoneStatus status) throws DataBaseException {
        try {
            return repository.findByStatus(status);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to filter milestones.", e);
        }
    }

    public List<Milestone> findDelayed() throws DataBaseException {
        try {
            return repository.findDelayed();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load delayed milestones.", e);
        }
    }

    public void updateProgress(String milestoneId, int percentage, MilestoneStatus status)
            throws InvalidInputException, RecordNotFoundException, DataBaseException {

        if (percentage < 0 || percentage > 100) {
            throw new InvalidInputException(
                    "Completion percentage must be between 0 and 100.");
        }

        Milestone m = findById(milestoneId);
        m.setCompletionPercentage(percentage);
        m.setStatus(status);

        if (status == MilestoneStatus.COMPLETED) {
            m.setCompletionPercentage(100);
            if (m.getCompletionDate() == null) {
                m.setCompletionDate(LocalDate.now());
            }
        } else {
            m.setCompletionDate(null);
        }

        try {
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to update progress.", e);
        }
    }

    private void validate(Milestone m) throws InvalidInputException {
        if (ValidationUtils.isNullOrEmpty(m.getMilestoneId())) {
            throw new InvalidInputException("Milestone ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(m.getStartupId())) {
            throw new InvalidInputException("Milestone must belong to a startup.");
        }
        if (ValidationUtils.isNullOrEmpty(m.getTitle())) {
            throw new InvalidInputException("Milestone title is required.");
        }
        if (m.getStatus() == null) {
            throw new InvalidInputException("Milestone status is required.");
        }
        if (m.getCompletionPercentage() < 0 || m.getCompletionPercentage() > 100) {
            throw new InvalidInputException(
                    "Completion percentage must be between 0 and 100.");
        }
        if (m.getStartDate() != null && m.getDeadline() != null
                && !ValidationUtils.isValidDateRange(m.getStartDate(), m.getDeadline())) {
            throw new InvalidInputException("Deadline cannot be before start date.");
        }
    }
}