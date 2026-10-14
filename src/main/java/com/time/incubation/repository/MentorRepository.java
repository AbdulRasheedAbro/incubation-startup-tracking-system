package com.time.incubation.repository;

import com.time.incubation.model.Mentor;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MentorRepository {

    public void save(Mentor m) throws SQLException {
        String sql = """
                INSERT INTO mentors (mentor_id, full_name, expertise, email, phone, organization)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (mentor_id) DO UPDATE SET
                    full_name = EXCLUDED.full_name,
                    expertise = EXCLUDED.expertise,
                    email = EXCLUDED.email,
                    phone = EXCLUDED.phone,
                    organization = EXCLUDED.organization
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMentorId());
            ps.setString(2, m.getFullName());
            ps.setString(3, m.getExpertise());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getPhone());
            ps.setString(6, m.getOrganization());
            ps.executeUpdate();
        }
    }

    public void delete(String mentorId) throws SQLException {
        String sql = "DELETE FROM mentors WHERE mentor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mentorId);
            ps.executeUpdate();
        }
    }

    public Mentor findById(String mentorId) throws SQLException {
        String sql = "SELECT * FROM mentors WHERE mentor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mentorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Mentor> findAll() throws SQLException {
        List<Mentor> list = new ArrayList<>();
        String sql = "SELECT * FROM mentors ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Mentor> searchByName(String namePart) throws SQLException {
        List<Mentor> list = new ArrayList<>();
        String sql = "SELECT * FROM mentors WHERE LOWER(full_name) LIKE ? ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + namePart.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ---------- Many-to-many: startup_mentors ----------

    public void assignToStartup(String startupId, String mentorId) throws SQLException {
        String sql = """
                INSERT INTO startup_mentors (startup_id, mentor_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            ps.setString(2, mentorId);
            ps.executeUpdate();
        }
    }

    public void unassignFromStartup(String startupId, String mentorId) throws SQLException {
        String sql = "DELETE FROM startup_mentors WHERE startup_id = ? AND mentor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            ps.setString(2, mentorId);
            ps.executeUpdate();
        }
    }

    public List<Mentor> findMentorsForStartup(String startupId) throws SQLException {
        List<Mentor> list = new ArrayList<>();
        String sql = """
                SELECT m.* FROM mentors m
                JOIN startup_mentors sm ON m.mentor_id = sm.mentor_id
                WHERE sm.startup_id = ?
                ORDER BY m.full_name
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * For every mentor that has at least one startup: mentor_id -> "StartupA, StartupB".
     * Uses JOIN + GROUP BY + STRING_AGG (PostgreSQL). Mentors with no startups are not in the map.
     */
    public Map<String, String> findAssignedStartupNames() throws SQLException {
        Map<String, String> result = new HashMap<>();
        String sql = """
                SELECT sm.mentor_id,
                       STRING_AGG(s.startup_name, ', ' ORDER BY s.startup_name) AS startups
                FROM startup_mentors sm
                JOIN startups s ON sm.startup_id = s.startup_id
                GROUP BY sm.mentor_id
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getString("mentor_id"), rs.getString("startups"));
            }
        }
        return result;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM mentors";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Mentor map(ResultSet rs) throws SQLException {
        return new Mentor(
                rs.getString("mentor_id"),
                rs.getString("full_name"),
                rs.getString("expertise"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("organization")
        );
    }
}