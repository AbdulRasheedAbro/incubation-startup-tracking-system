package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateEmailException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Founder;
import com.time.incubation.model.Startup;
import com.time.incubation.model.StartupStage;
import com.time.incubation.model.StartupStatus;
import com.time.incubation.repository.FounderRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

public class StartupService {

    // Founders created automatically from a startup get predictable IDs,
    // e.g. "S001-F1" (founder) and "S001-F2" (co-founder).
    private static final String FOUNDER_SUFFIX = "-F1";
    private static final String COFOUNDER_SUFFIX = "-F2";

    private final StartupRepository repository;
    private final FounderRepository founderRepository;

    public StartupService(StartupRepository repository, FounderRepository founderRepository) {
        this.repository = repository;
        this.founderRepository = founderRepository;
    }

    // ---------- Create / Update ----------

    public void addStartup(Startup s) throws InvalidInputException, DuplicateIdException,
            DuplicateEmailException, DataBaseException {

        validate(s);

        try {
            if (repository.findById(s.getStartupId()) != null) {
                throw new DuplicateIdException(
                        "Startup ID already exists: " + s.getStartupId());
            }
            ensureEmailUnique(s.getEmail(), null);
            repository.save(s);
            syncFounders(s);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to save startup.", e);
        }
    }

    public void updateStartup(Startup s) throws InvalidInputException, DuplicateEmailException,
            RecordNotFoundException, DataBaseException {

        validate(s);

        try {
            if (repository.findById(s.getStartupId()) == null) {
                throw new RecordNotFoundException(
                        "Startup not found: " + s.getStartupId());
            }
            ensureEmailUnique(s.getEmail(), s.getStartupId());
            repository.save(s);
            syncFounders(s);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to update startup.", e);
        }
    }

    public void deleteStartup(String startupId) throws RecordNotFoundException, DataBaseException {
        try {
            if (repository.findById(startupId) == null) {
                throw new RecordNotFoundException("Startup not found: " + startupId);
            }
            // remove this startup's founders first (works even without ON DELETE CASCADE)
            for (Founder f : founderRepository.findByStartup(startupId)) {
                founderRepository.delete(f.getFounderId());
            }
            repository.delete(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to delete startup.", e);
        }
    }

    // ---------- Read ----------

    public Startup findById(String startupId) throws RecordNotFoundException, DataBaseException {
        try {
            Startup s = repository.findById(startupId);
            if (s == null) {
                throw new RecordNotFoundException("Startup not found: " + startupId);
            }
            return s;
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load startup.", e);
        }
    }

    public List<Startup> getAll() throws DataBaseException {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load startups.", e);
        }
    }

    // ---------- Search & filters ----------

    public List<Startup> filter(String industry, StartupStage stage,
                                StartupStatus status, String namePart)
            throws DataBaseException {
        try {
            return repository.findWithFilters(industry, stage, status, namePart);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to filter startups.", e);
        }
    }

    public List<Startup> searchByName(String namePart) throws DataBaseException {
        try {
            return repository.searchByName(namePart);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to search startups.", e);
        }
    }

    public List<Startup> filterByIndustry(String industry) throws DataBaseException {
        try {
            return repository.findByIndustry(industry);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to filter by industry.", e);
        }
    }

    public List<Startup> filterByStage(StartupStage stage) throws DataBaseException {
        try {
            return repository.findByStage(stage);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to filter by stage.", e);
        }
    }

    public List<Startup> filterByStatus(StartupStatus status) throws DataBaseException {
        try {
            return repository.findByStatus(status);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to filter by status.", e);
        }
    }

    // ---------- Founder sync ----------

    /**
     * Keeps the founders table in step with the startup's Founder Name / Co-Founder Name.
     * Only the two auto-created rows (IDs ending -F1 / -F2) are touched; founders the admin
     * added manually on the Founders page are never changed.
     */
    private void syncFounders(Startup s) throws SQLException {
        syncFounder(s, s.getStartupId() + FOUNDER_SUFFIX,
                s.getFounderName(), s.getEmail(), s.getPhoneNumber());
        syncFounder(s, s.getStartupId() + COFOUNDER_SUFFIX,
                s.getCoFounderName(), null, null);
    }

    private void syncFounder(Startup s, String founderId, String name,
                             String email, String phone) throws SQLException {
        Founder existing = founderRepository.findById(founderId);
        boolean hasName = name != null && !name.isBlank();

        if (!hasName) {
            if (existing != null) founderRepository.delete(founderId);
            return;
        }

        if (existing == null) {
            founderRepository.save(new Founder(founderId, name.trim(), s.getStartupId(),
                    email, phone, "", "", ""));
        } else {
            // only the name follows the startup; qualification, skills, etc. stay as the admin set them
            existing.setName(name.trim());
            founderRepository.save(existing);
        }
    }

    // ---------- Validation ----------

    private void validate(Startup s) throws InvalidInputException {
        if (ValidationUtils.isNullOrEmpty(s.getStartupId())) {
            throw new InvalidInputException("Startup ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(s.getStartupName())) {
            throw new InvalidInputException("Startup name is required.");
        }
        if (!ValidationUtils.isValidEmail(s.getEmail())) {
            throw new InvalidInputException("Invalid email: " + s.getEmail());
        }
        if (s.getIncubationStartDate() == null) {
            throw new InvalidInputException("Incubation start date is required.");
        }
        if (s.getExpectedGraduationDate() != null
                && !ValidationUtils.isValidDateRange(
                s.getIncubationStartDate(), s.getExpectedGraduationDate())) {
            throw new InvalidInputException(
                    "Expected graduation date cannot be before incubation start date.");
        }
        if (s.getStage() == null) {
            throw new InvalidInputException("Startup stage is required.");
        }
        if (s.getCurrentStatus() == null) {
            throw new InvalidInputException("Startup status is required.");
        }
    }

    private void ensureEmailUnique(String email, String currentId)
            throws DuplicateEmailException, SQLException {
        List<Startup> all = repository.findAll();
        for (Startup s : all) {
            if (s.getEmail().equalsIgnoreCase(email)
                    && (currentId == null || !s.getStartupId().equals(currentId))) {
                throw new DuplicateEmailException(
                        "Another startup already uses email: " + email);
            }
        }
    }
}