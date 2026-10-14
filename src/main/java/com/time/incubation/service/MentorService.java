package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Mentor;
import com.time.incubation.repository.MentorRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MentorService {

    private final MentorRepository repository;
    private final StartupRepository startupRepository;

    public MentorService(MentorRepository repository, StartupRepository startupRepository) {
        this.repository = repository;
        this.startupRepository = startupRepository;
    }

    public void addMentor(Mentor m) throws InvalidInputException, DuplicateIdException,
            DataBaseException {

        validate(m);

        try {
            if (repository.findById(m.getMentorId()) != null) {
                throw new DuplicateIdException("Mentor ID already exists: " + m.getMentorId());
            }
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to save mentor.", e);
        }
    }

    public void updateMentor(Mentor m) throws InvalidInputException, RecordNotFoundException,
            DataBaseException {

        validate(m);

        try {
            if (repository.findById(m.getMentorId()) == null) {
                throw new RecordNotFoundException("Mentor not found: " + m.getMentorId());
            }
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to update mentor.", e);
        }
    }

    public void deleteMentor(String mentorId) throws RecordNotFoundException, DataBaseException {
        try {
            if (repository.findById(mentorId) == null) {
                throw new RecordNotFoundException("Mentor not found: " + mentorId);
            }
            repository.delete(mentorId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to delete mentor.", e);
        }
    }

    public Mentor findById(String mentorId) throws RecordNotFoundException, DataBaseException {
        try {
            Mentor m = repository.findById(mentorId);
            if (m == null) throw new RecordNotFoundException("Mentor not found: " + mentorId);
            return m;
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load mentor.", e);
        }
    }

    public List<Mentor> getAll() throws DataBaseException {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load mentors.", e);
        }
    }

    public List<Mentor> searchByName(String namePart) throws DataBaseException {
        try {
            return repository.searchByName(namePart);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to search mentors.", e);
        }
    }

    // ---------- Assignment ----------

    public void assignMentorToStartup(String startupId, String mentorId)
            throws RecordNotFoundException, DataBaseException {
        try {
            if (startupRepository.findById(startupId) == null) {
                throw new RecordNotFoundException("Startup not found: " + startupId);
            }
            if (repository.findById(mentorId) == null) {
                throw new RecordNotFoundException("Mentor not found: " + mentorId);
            }
            repository.assignToStartup(startupId, mentorId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to assign mentor.", e);
        }
    }

    public void unassignMentorFromStartup(String startupId, String mentorId)
            throws DataBaseException {
        try {
            repository.unassignFromStartup(startupId, mentorId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to unassign mentor.", e);
        }
    }

    public List<Mentor> getMentorsForStartup(String startupId) throws DataBaseException {
        try {
            return repository.findMentorsForStartup(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load mentors for startup.", e);
        }
    }

    /** mentor ID -> comma-separated names of the startups assigned to that mentor. */
    public Map<String, String> getAssignedStartupNames() throws DataBaseException {
        try {
            return repository.findAssignedStartupNames();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load assigned startups.", e);
        }
    }

    private void validate(Mentor m) throws InvalidInputException {
        if (ValidationUtils.isNullOrEmpty(m.getMentorId())) {
            throw new InvalidInputException("Mentor ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(m.getFullName())) {
            throw new InvalidInputException("Mentor full name is required.");
        }
        if (m.getEmail() != null && !m.getEmail().isEmpty()
                && !ValidationUtils.isValidEmail(m.getEmail())) {
            throw new InvalidInputException("Invalid email: " + m.getEmail());
        }
        if (m.getPhone() != null && !m.getPhone().isEmpty()
                && !ValidationUtils.isValidPhone(m.getPhone())) {
            throw new InvalidInputException("Invalid phone: " + m.getPhone());
        }
    }
}