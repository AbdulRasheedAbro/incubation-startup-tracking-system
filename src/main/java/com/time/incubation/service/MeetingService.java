package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.exception.RecordNotFoundException;
import com.time.incubation.model.Meeting;
import com.time.incubation.repository.MeetingRepository;
import com.time.incubation.repository.MentorRepository;
import com.time.incubation.repository.StartupRepository;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

public class MeetingService {

    private final MeetingRepository repository;
    private final StartupRepository startupRepository;
    private final MentorRepository mentorRepository;

    public MeetingService(MeetingRepository repository,
                          StartupRepository startupRepository,
                          MentorRepository mentorRepository) {
        this.repository = repository;
        this.startupRepository = startupRepository;
        this.mentorRepository = mentorRepository;
    }

    public void scheduleMeeting(Meeting m) throws InvalidInputException, DuplicateIdException,
            RecordNotFoundException, DataBaseException {

        validate(m);

        try {
            if (repository.findById(m.getMeetingId()) != null) {
                throw new DuplicateIdException(
                        "Meeting ID already exists: " + m.getMeetingId());
            }
            if (startupRepository.findById(m.getStartupId()) == null) {
                throw new RecordNotFoundException("Startup not found: " + m.getStartupId());
            }
            if (m.getMentorId() != null && !m.getMentorId().isEmpty()) {
                if (mentorRepository.findById(m.getMentorId()) == null) {
                    throw new RecordNotFoundException(
                            "Mentor not found: " + m.getMentorId());
                }
            }
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to schedule meeting.", e);
        }
    }

    public void updateMeeting(Meeting m) throws InvalidInputException, RecordNotFoundException,
            DataBaseException {

        validate(m);

        try {
            if (repository.findById(m.getMeetingId()) == null) {
                throw new RecordNotFoundException("Meeting not found: " + m.getMeetingId());
            }
            if (startupRepository.findById(m.getStartupId()) == null) {
                throw new RecordNotFoundException("Startup not found: " + m.getStartupId());
            }
            repository.save(m);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to update meeting.", e);
        }
    }

    public void deleteMeeting(String meetingId)
            throws RecordNotFoundException, DataBaseException {
        try {
            if (repository.findById(meetingId) == null) {
                throw new RecordNotFoundException("Meeting not found: " + meetingId);
            }
            repository.delete(meetingId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to delete meeting.", e);
        }
    }

    public Meeting findById(String meetingId)
            throws RecordNotFoundException, DataBaseException {
        try {
            Meeting m = repository.findById(meetingId);
            if (m == null) throw new RecordNotFoundException("Meeting not found: " + meetingId);
            return m;
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load meeting.", e);
        }
    }

    public List<Meeting> getAll() throws DataBaseException {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load meetings.", e);
        }
    }

    public List<Meeting> findByStartup(String startupId) throws DataBaseException {
        try {
            return repository.findByStartup(startupId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load meetings for startup.", e);
        }
    }

    public List<Meeting> findByMentor(String mentorId) throws DataBaseException {
        try {
            return repository.findByMentor(mentorId);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load meetings for mentor.", e);
        }
    }

    public List<Meeting> findUpcoming() throws DataBaseException {
        try {
            return repository.findUpcoming();
        } catch (SQLException e) {
            throw new DataBaseException("Failed to load upcoming meetings.", e);
        }
    }

    private void validate(Meeting m) throws InvalidInputException {
        if (ValidationUtils.isNullOrEmpty(m.getMeetingId())) {
            throw new InvalidInputException("Meeting ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(m.getStartupId())) {
            throw new InvalidInputException("Meeting must be linked to a startup.");
        }
        if (m.getMeetingDate() == null) {
            throw new InvalidInputException("Meeting date is required.");
        }
        if (m.getMeetingType() == null) {
            throw new InvalidInputException("Meeting type is required.");
        }
        if (m.getNextMeetingDate() != null
                && m.getNextMeetingDate().isBefore(m.getMeetingDate())) {
            throw new InvalidInputException(
                    "Next meeting date cannot be before this meeting's date.");
        }
    }
}