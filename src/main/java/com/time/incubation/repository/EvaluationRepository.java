package com.time.incubation.repository;

import com.time.incubation.model.Evaluation;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationRepository {

    public void save(Evaluation e) throws SQLException {
        String sql = """
                INSERT INTO evaluations (evaluation_id, startup_id,
                    team_score, problem_validation_score, product_development_score,
                    market_validation_score, business_model_score, traction_score,
                    revenue_score, pitch_readiness_score, overall_score, evaluated_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (evaluation_id) DO UPDATE SET
                    startup_id = EXCLUDED.startup_id,
                    team_score = EXCLUDED.team_score,
                    problem_validation_score = EXCLUDED.problem_validation_score,
                    product_development_score = EXCLUDED.product_development_score,
                    market_validation_score = EXCLUDED.market_validation_score,
                    business_model_score = EXCLUDED.business_model_score,
                    traction_score = EXCLUDED.traction_score,
                    revenue_score = EXCLUDED.revenue_score,
                    pitch_readiness_score = EXCLUDED.pitch_readiness_score,
                    overall_score = EXCLUDED.overall_score,
                    evaluated_date = EXCLUDED.evaluated_date
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEvaluationId());
            ps.setString(2, e.getStartupId());
            ps.setInt(3, e.getTeamScore());
            ps.setInt(4, e.getProblemValidationScore());
            ps.setInt(5, e.getProductDevelopmentScore());
            ps.setInt(6, e.getMarketValidationScore());
            ps.setInt(7, e.getBusinessModelScore());
            ps.setInt(8, e.getTractionScore());
            ps.setInt(9, e.getRevenueScore());
            ps.setInt(10, e.getPitchReadinessScore());
            ps.setDouble(11, e.getOverallScore());
            ps.setDate(12, Date.valueOf(e.getEvaluatedDate()));
            ps.executeUpdate();
        }
    }

    public void delete(String evaluationId) throws SQLException {
        String sql = "DELETE FROM evaluations WHERE evaluation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evaluationId);
            ps.executeUpdate();
        }
    }

    public Evaluation findById(String evaluationId) throws SQLException {
        String sql = "SELECT * FROM evaluations WHERE evaluation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evaluationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Evaluation> findAll() throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluations ORDER BY evaluated_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Evaluation> findByStartup(String startupId) throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluations WHERE startup_id = ? ORDER BY evaluated_date DESC";
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
     * Most recent evaluation for a startup, or null if none exist.
     */
    public Evaluation findLatestForStartup(String startupId) throws SQLException {
        String sql = """
                SELECT * FROM evaluations
                WHERE startup_id = ?
                ORDER BY evaluated_date DESC
                LIMIT 1
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /**
     * Average overall score across all evaluations. Returns 0 if no rows.
     */
    public double averageOverallScore() throws SQLException {
        String sql = "SELECT AVG(overall_score) FROM evaluations";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    private Evaluation map(ResultSet rs) throws SQLException {
        return new Evaluation(
                rs.getString("evaluation_id"),
                rs.getString("startup_id"),
                rs.getInt("team_score"),
                rs.getInt("problem_validation_score"),
                rs.getInt("product_development_score"),
                rs.getInt("market_validation_score"),
                rs.getInt("business_model_score"),
                rs.getInt("traction_score"),
                rs.getInt("revenue_score"),
                rs.getInt("pitch_readiness_score"),
                rs.getDouble("overall_score"),
                rs.getDate("evaluated_date").toLocalDate()
        );
    }
}