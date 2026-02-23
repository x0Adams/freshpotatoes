package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.StaffDao;
import hu.freshpotatoes.model.Country;
import hu.freshpotatoes.model.Staff;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffDaoImpl implements StaffDao {

    private final DataSource dataSource;

    public StaffDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Staff> findById(Integer id) {
        String sql = "SELECT id, name, birthday, birth_country FROM fresh_potatoes.staff WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStaff(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Staff> findAll() {
        String sql = "SELECT id, name, birthday, birth_country FROM fresh_potatoes.staff";
        List<Staff> staffList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                staffList.add(mapRowToStaff(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return staffList;
    }

    @Override
    public Staff save(Staff entity) {
        String sql = "INSERT INTO fresh_potatoes.staff (name, birthday, birth_country) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStaffParameters(stmt, entity);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Staff entity) {
        String sql = "UPDATE fresh_potatoes.staff SET name = ?, birthday = ?, birth_country = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStaffParameters(stmt, entity);
            stmt.setInt(4, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM fresh_potatoes.staff WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setStaffParameters(PreparedStatement stmt, Staff entity) throws SQLException {
        stmt.setString(1, entity.getName());
        stmt.setDate(2, Date.valueOf(entity.getBirthday()));
        stmt.setInt(3, entity.getBirthCountry().getId());
    }

    private Staff mapRowToStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setId(rs.getInt("id"));
        staff.setName(rs.getString("name"));
        staff.setBirthday(rs.getDate("birthday").toLocalDate());

        Country country = new Country();
        country.setId(rs.getInt("birth_country"));
        staff.setBirthCountry(country);

        return staff;
    }
}