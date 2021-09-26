package org.example.jdbc;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public class JdbcTemplate {
  private final DataSource dataSource;

  public <T> Set<T> queryAll(String sql, RowMapper<T> rowMapper, Object... args) {
    return execute(sql, args, statement -> {
      try (final var resultSet = statement.executeQuery()) {
        final var result = new HashSet<T>();
        while (resultSet.next()) {
          result.add(rowMapper.mapRow(resultSet));
        }
        return result;
      }
    });
  }

  public <T> Optional<T> queryOne(String sql, RowMapper<T> rowMapper, Object... args) {
    return execute(sql, args, statement -> {
      try (final var resultSet = statement.executeQuery()) {
        return resultSet.next() ? Optional.of(rowMapper.mapRow(resultSet)) : Optional.empty();
      }
    });
  }

  public boolean isExists(String sql, Object... args) {
    return execute(sql, args, statement -> {
      try (final var resultSet = statement.executeQuery()) {
        return resultSet.next();
      }
    });
  }

  public int update(String sql, Object... args) {
    return execute(sql, args, PreparedStatement::executeUpdate);
  }

  private <T> T execute(String sql, Object[] args, Executor<T> executor) {
    try (
        final var connection = dataSource.getConnection();
        final var statement = connection.prepareStatement(sql)
    ) {
      for (int i = 0; i < args.length; i++) {
        final var arg = args[i];
        statement.setObject(i + 1, arg);
      }

      return executor.execute(statement);

    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }
}
