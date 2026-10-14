package com.time.incubation.repository;

import com.time.incubation.model.Startup;
import com.time.incubation.model.StartupStage;
import com.time.incubation.model.StartupStatus;
import com.time.incubation.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StartupRepository {

    // ---------- Write ----------

    public void save(Startup s) throws SQLException {
        String sql = """
                INSERT INTO startups (startup_id, startup_name, founder_name, co_founder_name,
                                      email, phone_number, industry, stage,
                                      incubation_start_date, expected_graduation_date,
                                      current_status, program_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (startup_id) DO UPDATE SET
                    startup_name = EXCLUDED.startup_name,
                    founder_name = EXCLUDED.founder_name,
                    co_founder_name = EXCLUDED.co_founder_name,
                    email = EXCLUDED.email,
                    phone_number = EXCLUDED.phone_number,
                    industry = EXCLUDED.industry,
                    stage = EXCLUDED.stage,
                    incubation_start_date = EXCLUDED.incubation_start_date,
                    expected_graduation_date = EXCLUDED.expected_graduation_date,
                    current_status = EXCLUDED.current_status,
                    program_id = EXCLUDED.program_id
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStartupId());
            ps.setString(2, s.getStartupName());
            ps.setString(3, s.getFounderName());
            ps.setString(4, s.getCoFounderName());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getPhoneNumber());
            ps.setString(7, s.getIndustry());
            ps.setString(8, s.getStage().name());
            ps.setDate(9, Date.valueOf(s.getIncubationStartDate()));
            if (s.getExpectedGraduationDate() != null) {
                ps.setDate(10, Date.valueOf(s.getExpectedGraduationDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            ps.setString(11, s.getCurrentStatus().name());
            ps.setString(12, s.getProgramId());
            ps.executeUpdate();
        }
    }

    public void delete(String startupId) throws SQLException {
        String sql = "DELETE FROM startups WHERE startup_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            ps.executeUpdate();
        }
    }

    // ---------- Read ----------

    public Startup findById(String startupId) throws SQLException {
        String sql = "SELECT * FROM startups WHERE startup_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Startup> findAll() throws SQLException {
        List<Startup> list = new ArrayList<>();
        String sql = "SELECT * FROM startups ORDER BY startup_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Startup> findByIndustry(String industry) throws SQLException {
        List<Startup> list = new ArrayList<>();
        String sql = "SELECT * FROM startups WHERE industry = ? ORDER BY startup_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, industry);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Startup> findByStage(StartupStage stage) throws SQLException {
        List<Startup> list = new ArrayList<>();
        String sql = "SELECT * FROM startups WHERE stage = ? ORDER BY startup_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stage.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Startup> findByStatus(StartupStatus status) throws SQLException {
        List<Startup> list = new ArrayList<>();
        String sql = "SELECT * FROM startups WHERE current_status = ? ORDER BY startup_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Startup> searchByName(String namePart) throws SQLException {
        List<Startup> list = new ArrayList<>();
        String sql = "SELECT * FROM startups WHERE LOWER(startup_name) LIKE ? ORDER BY startup_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + namePart.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ---------- Filters combined ----------

    public List<Startup> findWithFilters(String industry, StartupStage stage,
                                         StartupStatus status, String namePart)
            throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM startups WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (industry != null && !industry.isBlank()) {
            sql.append(" AND industry = ?");
            params.add(industry);
        }
        if (stage != null) {
            sql.append(" AND stage = ?");
            params.add(stage.name());
        }
        if (status != null) {
            sql.append(" AND current_status = ?");
            params.add(status.name());
        }
        if (namePart != null && !namePart.isBlank()) {
            sql.append(" AND LOWER(startup_name) LIKE ?");
            params.add("%" + namePart.toLowerCase() + "%");
        }
        sql.append(" ORDER BY startup_name");

        List<Startup> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ---------- Aggregates for dashboard ----------

    public int countAll() throws SQLException {
        return countBy("SELECT COUNT(*) FROM startups", null);
    }

    public int countByStatus(StartupStatus status) throws SQLException {
        return countBy("SELECT COUNT(*) FROM startups WHERE current_status = ?", status.name());
    }

    public int countByStage(StartupStage stage) throws SQLException {
        return countBy("SELECT COUNT(*) FROM startups WHERE stage = ?", stage.name());
    }

    private int countBy(String sql, String param) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ---------- Row mapping ----------

    private Startup map(ResultSet rs) throws SQLException {
        Date grad = rs.getDate("expected_graduation_date");
        return new Startup(
                rs.getString("startup_id"),
                rs.getString("startup_name"),
                rs.getString("founder_name"),
                rs.getString("co_founder_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("industry"),
                StartupStage.valueOf(rs.getString("stage")),
                rs.getDate("incubation_start_date").toLocalDate(),
                grad != null ? grad.toLocalDate() : null,
                StartupStatus.valueOf(rs.getString("current_status")),
                rs.getString("program_id")
        );
    }
}