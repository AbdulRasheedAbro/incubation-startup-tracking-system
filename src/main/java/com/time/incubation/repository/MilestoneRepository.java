package com.time.incubation.repository;

import com.time.incubation.model.Milestone;
import com.time.incubation.model.MilestoneStatus;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MilestoneRepository {

    public void save(Milestone m) throws SQLException {
        String sql = """
                INSERT INTO milestones (milestone_id, startup_id, title, description,
                                        start_date, deadline, completion_date, status,
                                        completion_percentage)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (milestone_id) DO UPDATE SET
                    startup_id = EXCLUDED.startup_id,
                    title = EXCLUDED.title,
                    description = EXCLUDED.description,
                    start_date = EXCLUDED.start_date,
                    deadline = EXCLUDED.deadline,
                    completion_date = EXCLUDED.completion_date,
                    status = EXCLUDED.status,
                    completion_percentage = EXCLUDED.completion_percentage
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMilestoneId());
            ps.setString(2, m.getStartupId());
            ps.setString(3, m.getTitle());
            ps.setString(4, m.getDescription());
            if (m.getStartDate() != null) ps.setDate(5, Date.valueOf(m.getStartDate()));
            else ps.setNull(5, Types.DATE);
            if (m.getDeadline() != null) ps.setDate(6, Date.valueOf(m.getDeadline()));
            else ps.setNull(6, Types.DATE);
            if (m.getCompletionDate() != null) ps.setDate(7, Date.valueOf(m.getCompletionDate()));
            else ps.setNull(7, Types.DATE);
            ps.setString(8, m.getStatus().name());
            ps.setInt(9, m.getCompletionPercentage());
            ps.executeUpdate();
        }
    }

    public void delete(String milestoneId) throws SQLException {
        String sql = "DELETE FROM milestones WHERE milestone_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, milestoneId);
            ps.executeUpdate();
        }
    }

    public Milestone findById(String milestoneId) throws SQLException {
        String sql = "SELECT * FROM milestones WHERE milestone_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, milestoneId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Milestone> findAll() throws SQLException {
        List<Milestone> list = new ArrayList<>();
        String sql = "SELECT * FROM milestones ORDER BY deadline NULLS LAST, title";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Milestone> findByStartup(String startupId) throws SQLException {
        List<Milestone> list = new ArrayList<>();
        String sql = "SELECT * FROM milestones WHERE startup_id = ? ORDER BY deadline NULLS LAST, title";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Milestone> findByStatus(MilestoneStatus status) throws SQLException {
        List<Milestone> list = new ArrayList<>();
        String sql = "SELECT * FROM milestones WHERE status = ? ORDER BY deadline NULLS LAST";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Milestones whose deadline has passed but are not Completed yet.
     */
    public List<Milestone> findDelayed() throws SQLException {
        List<Milestone> list = new ArrayList<>();
        String sql = """
                SELECT * FROM milestones
                WHERE deadline < CURRENT_DATE AND status <> 'COMPLETED'
                ORDER BY deadline
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int countByStatus(MilestoneStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM milestones WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countDelayed() throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM milestones
                WHERE deadline < CURRENT_DATE AND status <> 'COMPLETED'
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countCompletedForStartup(String startupId) throws SQLException {
        return countForStartupByStatus(startupId, MilestoneStatus.COMPLETED);
    }

    public int countPendingForStartup(String startupId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM milestones
                WHERE startup_id = ? AND status <> 'COMPLETED'
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private int countForStartupByStatus(String startupId, MilestoneStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM milestones WHERE startup_id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Milestone map(ResultSet rs) throws SQLException {
        Date start = rs.getDate("start_date");
        Date deadline = rs.getDate("deadline");
        Date completion = rs.getDate("completion_date");
        return new Milestone(
                rs.getString("milestone_id"),
                rs.getString("startup_id"),
                rs.getString("title"),
                rs.getString("description"),
                start != null ? start.toLocalDate() : null,
                deadline != null ? deadline.toLocalDate() : null,
                completion != null ? completion.toLocalDate() : null,
                MilestoneStatus.valueOf(rs.getString("status")),
                rs.getInt("completion_percentage")
        );
    }
}