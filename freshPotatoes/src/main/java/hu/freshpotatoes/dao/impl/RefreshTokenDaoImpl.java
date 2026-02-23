package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.RefreshTokenDao;
import hu.freshpotatoes.model.RefreshToken;
import hu.freshpotatoes.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefreshTokenDaoImpl implements RefreshTokenDao {

    private final DataSource dataSource;

    public RefreshTokenDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<RefreshToken> findById(Integer id) {
        String sql = "SELECT id, user_id, token, creation_date, expiration_date, used " +
                "FROM fresh_potatoes.refresh_token WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRefreshToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<RefreshToken> findAll() {
        String sql = "SELECT id, user_id, token, creation_date, expiration_date, used " +
                "FROM fresh_potatoes.refresh_token";
        List<RefreshToken> refreshTokens = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                refreshTokens.add(mapRowToRefreshToken(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return refreshTokens;
    }

    @Override
    public RefreshToken save(RefreshToken entity) {
        String sql = "INSERT INTO fresh_potatoes.refresh_token (user_id, token, creation_date, expiration_date, used) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setRefreshTokenParameters(stmt, entity);
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
    public void update(RefreshToken entity) {
        String sql = "UPDATE fresh_potatoes.refresh_token SET user_id = ?, token = ?, creation_date = ?, " +
                "expiration_date = ?, used = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setRefreshTokenParameters(stmt, entity);
            stmt.setInt(6, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM fresh_potatoes.refresh_token WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setRefreshTokenParameters(PreparedStatement stmt, RefreshToken entity) throws SQLException {
        stmt.setInt(1, entity.getUser().getId());
        stmt.setString(2, entity.getToken());
        stmt.setTimestamp(3, Timestamp.from(entity.getCreationDate()));
        stmt.setTimestamp(4, Timestamp.from(entity.getExpirationDate()));
        stmt.setInt(5, entity.getUsed());
    }

    private RefreshToken mapRowToRefreshToken(ResultSet rs) throws SQLException {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(rs.getInt("id"));

        User user = new User();
        user.setId(rs.getInt("user_id"));
        refreshToken.setUser(user);

        refreshToken.setToken(rs.getString("token"));
        refreshToken.setCreationDate(rs.getTimestamp("creation_date").toInstant());
        refreshToken.setExpirationDate(rs.getTimestamp("expiration_date").toInstant());
        refreshToken.setUsed(rs.getInt("used"));

        return refreshToken;
    }
}