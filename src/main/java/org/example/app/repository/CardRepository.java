package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class CardRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Card> cardRowMapper = resultSet -> new Card(
            resultSet.getLong("id"),
            resultSet.getString("number"),
            resultSet.getLong("balance")
    );
    private final RowMapper<Long> ownerRowMapper = resultSet -> resultSet.getLong("ownerId");

    public Set<Card> getAllByOwnerId(long ownerId) {
        // language=PostgreSQL
        return jdbcTemplate.queryAll(
                "SELECT id, number, balance FROM cards WHERE \"ownerId\" = ? AND active = TRUE",
                cardRowMapper,
                ownerId
        );
    }

    public Optional<Card> getById(long id) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne(
                "SELECT id, number, balance FROM cards WHERE id = ?",
                cardRowMapper,
                id
        );
    }

    public Optional<Long> getOwnerById(long id) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne("""
                        SELECT "ownerId" FROM cards WHERE id = ?
                        """,
                ownerRowMapper,
                id
        );
    }


    public Optional<Card> save(long userId, String number) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne("""
                            INSERT INTO cards("ownerId", number) 
                            VALUES (?, ?) RETURNING id, number, balance
                        """,
                cardRowMapper,
                userId, number);
    }

    public Optional<Card> delete(long id) {
        // language=PostgreSQL
        return jdbcTemplate.queryOne(
                "DELETE FROM cards WHERE id = ? RETURNING id, number, balance",
                cardRowMapper,
                id
        );
    }

    public void transfer(long from, long to, long amount) {
        // language=PostgreSQL
        jdbcTemplate.update(
                "UPDATE cards SET balance = balance - ? WHERE id = ?",
                amount, from);
        // language=PostgreSQL
        jdbcTemplate.update(
                "UPDATE cards SET balance = balance + ? WHERE id = ?",
                amount, to);
    }
}
