package com.j256.ormlite.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;

import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.support.Results;

/**
 * Wrapper around a {@link PreparedStatement} object which we delegate to.
 * 
 * @author graywatson
 */
public class JdbcPreparedStmt implements PreparedStmt {

	private final PreparedStatement preparedStatement;
	private ResultSetMetaData metaData = null;

	public JdbcPreparedStmt(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	public int getColumnCount() throws SQLException {
		if (metaData == null) {
			metaData = preparedStatement.getMetaData();
		}
		return metaData.getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		if (metaData == null) {
			metaData = preparedStatement.getMetaData();
		}
		return metaData.getColumnName(column);
	}

	public boolean execute() throws SQLException {
		return preparedStatement.execute();
	}

	public int executeUpdate() throws SQLException {
		return preparedStatement.executeUpdate();
	}

	public Results executeQuery() throws SQLException {
		return new JdbcResults(preparedStatement.executeQuery());
	}

	public String getWarning() throws SQLException {
		SQLWarning warning = preparedStatement.getWarnings();
		if (warning == null) {
			return null;
		} else {
			return warning.toString();
		}
	}

	public boolean getMoreResults() throws SQLException {
		return preparedStatement.getMoreResults();
	}

	public Results getResults() throws SQLException {
		return new JdbcResults(preparedStatement.getResultSet());
	}

	public Results getGeneratedKeys() throws SQLException {
		return new JdbcResults(preparedStatement.getGeneratedKeys());
	}

	public void close() throws SQLException {
		preparedStatement.close();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		preparedStatement.setNull(parameterIndex, sqlType);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		preparedStatement.setObject(parameterIndex, x, targetSqlType);
	}

	public void setMaxRows(int max) throws SQLException {
		preparedStatement.setMaxRows(max);
	}
}