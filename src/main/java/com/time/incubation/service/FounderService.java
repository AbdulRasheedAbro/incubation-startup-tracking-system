package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Founder;
import com.time.incubation.repository.FounderRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

public class FounderService {

    private final FounderRepository repository;
    private final StartupRepository startupRepository;

    public FounderService(FounderRepository repository, StartupRepository startupRepository) {
        this.repository = repository;
        this.startupRepository = startupRepository;
    }

    public void addFounder(Founder f) throws InvalidInputException, DuplicateIdException,
            RecordNotFoundException, DataBaseException {

        validate(f);

        try {
            if (repository.findById(f.getFounderId()) != null) {
                throw new DuplicateIdException("Founder ID already exists: " + f.getFounderId());
            }
            if (startupRepository.findById(f.getStartupId()) == null) {
                throw new RecordNotFoundException("Startup not found: " + f.getStartupId());
            }
            repository.save(f);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to save founder.", e);
        }
    }

    public void updateFounder(Founder f) throws InvalidInputException, RecordNotFoundException,
            DataBaseException {

        validate(f);

        try {
            if (repository.findById(f.getFounderId()) == null) {
                throw new RecordNotFoundException("Founder not found: " + f.getFounderId());
            }
            if (startupRepository.findById(f.getStartupId()) == null) {
                throw new RecordNotFoundException("Startup not found: " + f.getStartupId());
            }
            repository.save(f);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to update founder.", e);
        }
    }

    public void deleteFounder(String founderId) throws RecordNotFoundException, DataBaseException {
        try {
            if (repository.findById(founderId) == null) {
                throw new RecordNotFoundException("Founder not found: " + founderId);
            }
            repository.delete(founderId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to delete founder.", e);
        }
    }

    public Founder findById(String founderId) throws RecordNotFoundException, DataBaseException {
        try {
            Founder f = repository.findById(founderId);
            if (f == null) throw new RecordNotFoundException("Founder not found: " + founderId);
            return f;
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load founder.", e);
        }
    }

    public List<Founder> getAll() throws DataBaseException {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load founders.", e);
        }
    }

    public List<Founder> findByStartup(String startupId) throws DataBaseException {
        try {
            return repository.findByStartup(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load founders for startup.", e);
        }
    }

    public List<Founder> searchByName(String namePart) throws DataBaseException {
        try {
            return repository.searchByName(namePart);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to search founders.", e);
        }
    }

    private void validate(Founder f) throws InvalidInputException {
        if (ValidationUtils.isNullOrEmpty(f.getFounderId())) {
            throw new InvalidInputException("Founder ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(f.getName())) {
            throw new InvalidInputException("Founder name is required.");
        }
        if (ValidationUtils.isNullOrEmpty(f.getStartupId())) {
            throw new InvalidInputException("Founder must be linked to a startup.");
        }
        if (f.getEmail() != null && !f.getEmail().isEmpty()
                && !ValidationUtils.isValidEmail(f.getEmail())) {
            throw new InvalidInputException("Invalid email: " + f.getEmail());
        }
        if (f.getPhone() != null && !f.getPhone().isEmpty()
                && !ValidationUtils.isValidPhone(f.getPhone())) {
            throw new InvalidInputException("Invalid phone: " + f.getPhone());
        }
    }
}