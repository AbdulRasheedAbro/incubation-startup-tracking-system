package com.time.incubation.repository;

import com.time.incubation.model.ProgressReport;
import com.time.incubation.model.StartupStage;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgressReportRepository {

    public void save(ProgressReport r) throws SQLException {
        String sql = """
                INSERT INTO progress_reports (report_id, startup_id, report_date, summary,
                                              current_stage, overall_score,
                                              completed_milestones, pending_milestones)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (report_id) DO UPDATE SET
                    startup_id = EXCLUDED.startup_id,
                    report_date = EXCLUDED.report_date,
                    summary = EXCLUDED.summary,
                    current_stage = EXCLUDED.current_stage,
                    overall_score = EXCLUDED.overall_score,
                    completed_milestones = EXCLUDED.completed_milestones,
                    pending_milestones = EXCLUDED.pending_milestones
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getReportId());
            ps.setString(2, r.getStartupId());
            ps.setDate(3, Date.valueOf(r.getReportDate()));
            ps.setString(4, r.getSummary());
            ps.setString(5, r.getCurrentStage() != null ? r.getCurrentStage().name() : null);
            ps.setDouble(6, r.getOverallScore());
            ps.setInt(7, r.getCompletedMilestones());
            ps.setInt(8, r.getPendingMilestones());
            ps.executeUpdate();
        }
    }

    public void delete(String reportId) throws SQLException {
        String sql = "DELETE FROM progress_reports WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId);
            ps.executeUpdate();
        }
    }

    public ProgressReport findById(String reportId) throws SQLException {
        String sql = "SELECT * FROM progress_reports WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<ProgressReport> findAll() throws SQLException {
        List<ProgressReport> list = new ArrayList<>();
        String sql = "SELECT * FROM progress_reports ORDER BY report_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<ProgressReport> findByStartup(String startupId) throws SQLException {
        List<ProgressReport> list = new ArrayList<>();
        String sql = "SELECT * FROM progress_reports WHERE startup_id = ? ORDER BY report_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private ProgressReport map(ResultSet rs) throws SQLException {
        String stage = rs.getString("current_stage");
        return new ProgressReport(
                rs.getString("report_id"),
                rs.getString("startup_id"),
                rs.getDate("report_date").toLocalDate(),
                rs.getString("summary"),
                stage != null ? StartupStage.valueOf(stage) : null,
                rs.getDouble("overall_score"),
                rs.getInt("completed_milestones"),
                rs.getInt("pending_milestones")
        );
    }
}