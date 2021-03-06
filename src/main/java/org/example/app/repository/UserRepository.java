package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.entity.UserEntity;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("username")
    );
    private final RowMapper<UserWithPassword> rowMapperWithPassword = resultSet -> new UserWithPassword(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            resultSet.getString("password")
    );
    private final RowMapper<String> rowMapperRoles = resultSet -> resultSet.getString("name");
    private final RowMapper<Double> rowMapperTimes = resultSet -> resultSet.getDouble("time");

    public Optional<User> getByUsername(String username) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne("SELECT id, username FROM users WHERE username = ?", rowMapper, username);
    }

    public Optional<UserWithPassword> getByUsernameWithPassword(String username) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne("SELECT id, username, password FROM users WHERE username = ?", rowMapperWithPassword, username);
    }

    public Optional<UserWithPassword> getByUsernameWithPassword(EntityManager entityManager, EntityTransaction transaction, String username) {
        // em, emt - closeable
        return entityManager.createNamedQuery(UserEntity.FIND_BY_USERNAME, UserEntity.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultStream()
                .map(o -> new UserWithPassword(o.getId(), o.getUsername(), o.getPassword()))
                .findFirst();
        // language=PostgreSQL
        // return jdbcTemplate.queryOne("SELECT id, username, password FROM users WHERE username = ?", rowMapperWithPassword, username);
    }

    /**
     * saves user to db
     *
     * @param id       - user id, if 0 - insert, if not 0 - update
     * @param username
     * @param hash
     */
    // TODO: DuplicateKeyException <-
    public Optional<User> save(long id, String username, String hash) {
        // language=PostgreSQL
        return id == 0 ? jdbcTemplate.queryOne(
                """
                        INSERT INTO users(username, password) VALUES (?, ?) RETURNING id, username
                        """,
                rowMapper,
                username, hash
        ) : jdbcTemplate.queryOne(
                """
                        UPDATE users SET username = ?, password = ? WHERE id = ? RETURNING id, username
                        """,
                rowMapper,
                username, hash, id
        );
    }

    public Set<String> getRolesByUserId(User u) {
        // language=PostgreSQL
        return jdbcTemplate.queryAll(
                """
                        SELECT r.name FROM roles r
                        JOIN users_roles ur on r.id = ur."rolesId"
                        WHERE ur."userId" = ?
                        """,
                rowMapperRoles,
                u.getId()
        );
    }

    public Optional<User> findByToken(String token) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne(
                """
                        SELECT u.id, u.username FROM tokens t
                        JOIN users u ON t."userId" = u.id
                        WHERE t.token = ?
                        """,
                rowMapper,
                token
        );
    }

    public void saveToken(long userId, String token) {
        // query - SELECT'???? (ResultSet)
        // update - ? int/long
        // language=PostgreSQL
        jdbcTemplate.update(
                """
                        INSERT INTO tokens(token, "userId") VALUES (?, ?)
                        """,
                token, userId
        );
    }

    public Optional<Double> getTokenLifeTime(String token) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne(
                """
                        SELECT (EXTRACT(epoch FROM CURRENT_TIMESTAMP) -
                        EXTRACT(EPOCH FROM (SELECT created FROM tokens WHERE token = ?))) as time;
                        """, rowMapperTimes, token
        );
    }

    public void generateResetCode(String username, String code) {
        // language=PostgreSQL
        jdbcTemplate.update(
                """
                        INSERT INTO reset_codes(code, username) VALUES (?, ?)
                        """,
                code, username
        );
    }

    public boolean checkResetCode(String username, String code) {
        // language=PostgreSQL
        return jdbcTemplate.isExists(
                """
                        SELECT username, code FROM reset_codes WHERE code = ? AND username = ?
                        """, code, username
        );
    }

    public Optional<User> resetPass(String username, String password) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne(
                """
                        UPDATE users SET password = ? WHERE username = ? RETURNING id, username
                        """,
                rowMapper,
                password, username
        );
    }

    public void updateTokenCreated(String token) {
        // language=PostgreSQL
        jdbcTemplate.update(
                """
                        UPDATE tokens SET created = current_timestamp WHERE token = ?
                        """, token
        );

    }
}
