package com.time.incubation.repository;

import com.time.incubation.model.Founder;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FounderRepository {

    public void save(Founder f) throws SQLException {
        String sql = """
                INSERT INTO founders (founder_id, name, startup_id, email, phone,
                                      qualification, skills, linkedin_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (founder_id) DO UPDATE SET
                    name = EXCLUDED.name,
                    startup_id = EXCLUDED.startup_id,
                    email = EXCLUDED.email,
                    phone = EXCLUDED.phone,
                    qualification = EXCLUDED.qualification,
                    skills = EXCLUDED.skills,
                    linkedin_url = EXCLUDED.linkedin_url
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFounderId());
            ps.setString(2, f.getName());
            ps.setString(3, f.getStartupId());
            ps.setString(4, f.getEmail());
            ps.setString(5, f.getPhone());
            ps.setString(6, f.getQualification());
            ps.setString(7, f.getSkills());
            ps.setString(8, f.getLinkedinUrl());
            ps.executeUpdate();
        }
    }

    public void delete(String founderId) throws SQLException {
        String sql = "DELETE FROM founders WHERE founder_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, founderId);
            ps.executeUpdate();
        }
    }

    public Founder findById(String founderId) throws SQLException {
        String sql = "SELECT * FROM founders WHERE founder_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, founderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Founder> findAll() throws SQLException {
        List<Founder> list = new ArrayList<>();
        String sql = "SELECT * FROM founders ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Founder> findByStartup(String startupId) throws SQLException {
        List<Founder> list = new ArrayList<>();
        String sql = "SELECT * FROM founders WHERE startup_id = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Founder> searchByName(String namePart) throws SQLException {
        List<Founder> list = new ArrayList<>();
        String sql = "SELECT * FROM founders WHERE LOWER(name) LIKE ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + namePart.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM founders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Founder map(ResultSet rs) throws SQLException {
        return new Founder(
                rs.getString("founder_id"),
                rs.getString("name"),
                rs.getString("startup_id"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("qualification"),
                rs.getString("skills"),
                rs.getString("linkedin_url")
        );
    }
}