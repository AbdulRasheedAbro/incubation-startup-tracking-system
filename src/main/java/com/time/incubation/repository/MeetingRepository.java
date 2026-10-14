package com.time.incubation.repository;

import com.time.incubation.model.Meeting;
import com.time.incubation.model.MeetingType;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MeetingRepository {

    public void save(Meeting m) throws SQLException {
        String sql = """
                INSERT INTO meetings (meeting_id, startup_id, mentor_id, manager_id,
                                      meeting_date, meeting_time, meeting_type,
                                      discussion_notes, next_action, next_meeting_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (meeting_id) DO UPDATE SET
                    startup_id = EXCLUDED.startup_id,
                    mentor_id = EXCLUDED.mentor_id,
                    manager_id = EXCLUDED.manager_id,
                    meeting_date = EXCLUDED.meeting_date,
                    meeting_time = EXCLUDED.meeting_time,
                    meeting_type = EXCLUDED.meeting_type,
                    discussion_notes = EXCLUDED.discussion_notes,
                    next_action = EXCLUDED.next_action,
                    next_meeting_date = EXCLUDED.next_meeting_date
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMeetingId());
            ps.setString(2, m.getStartupId());
            ps.setString(3, m.getMentorId());
            ps.setString(4, m.getManagerId());
            ps.setDate(5, Date.valueOf(m.getMeetingDate()));
            ps.setString(6, m.getMeetingTime());
            ps.setString(7, m.getMeetingType().name());
            ps.setString(8, m.getDiscussionNotes());
            ps.setString(9, m.getNextAction());
            if (m.getNextMeetingDate() != null) {
                ps.setDate(10, Date.valueOf(m.getNextMeetingDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public void delete(String meetingId) throws SQLException {
        String sql = "DELETE FROM meetings WHERE meeting_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meetingId);
            ps.executeUpdate();
        }
    }

    public Meeting findById(String meetingId) throws SQLException {
        String sql = "SELECT * FROM meetings WHERE meeting_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meetingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Meeting> findAll() throws SQLException {
        List<Meeting> list = new ArrayList<>();
        String sql = "SELECT * FROM meetings ORDER BY meeting_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Meeting> findByStartup(String startupId) throws SQLException {
        List<Meeting> list = new ArrayList<>();
        String sql = "SELECT * FROM meetings WHERE startup_id = ? ORDER BY meeting_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Meeting> findByMentor(String mentorId) throws SQLException {
        List<Meeting> list = new ArrayList<>();
        String sql = "SELECT * FROM meetings WHERE mentor_id = ? ORDER BY meeting_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mentorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Meeting> findUpcoming() throws SQLException {
        List<Meeting> list = new ArrayList<>();
        String sql = "SELECT * FROM meetings WHERE meeting_date >= CURRENT_DATE ORDER BY meeting_date, meeting_time";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM meetings";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countUpcoming() throws SQLException {
        String sql = "SELECT COUNT(*) FROM meetings WHERE meeting_date >= CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countForMentor(String mentorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM meetings WHERE mentor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mentorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Meeting map(ResultSet rs) throws SQLException {
        Date next = rs.getDate("next_meeting_date");
        return new Meeting(
                rs.getString("meeting_id"),
                rs.getString("startup_id"),
                rs.getString("mentor_id"),
                rs.getString("manager_id"),
                rs.getDate("meeting_date").toLocalDate(),
                rs.getString("meeting_time"),
                MeetingType.valueOf(rs.getString("meeting_type")),
                rs.getString("discussion_notes"),
                rs.getString("next_action"),
                next != null ? next.toLocalDate() : null
        );
    }
}