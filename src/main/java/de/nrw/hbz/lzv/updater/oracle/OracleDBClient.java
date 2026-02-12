package de.nrw.hbz.lzv.updater.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for executing prepared SQL queries against Oracle databases.
 * Provides safe resource management using try-with-resources.
 * 
 */
public class OracleDBClient {
	private static final Logger logger = LoggerFactory.getLogger(OracleDBClient.class);

    /**
     * Executes a prepared SQL SELECT query with IEPID parameter and returns ResultSet.
     * 
     * @param url      JDBC connection URL
     * @param user     Database username
     * @param password Database password
     * @param sqlQuery Prepared SQL query with single placeholder for IEPID
     * @param iepid    IEPID parameter value (position 1)
     * @return ResultSet containing query results (caller must close via try-with-resources)
     * @throws SQLException if connection fails, query fails, or database error occurs
     */
	public ResultSet executePreparedQuery(String url, String user, String password, String sqlQuery, String iepid)
			throws SQLException {
		logger.info("Executing prepared Oracle query");

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DriverManager.getConnection(url, user, password);
			pstmt = conn.prepareStatement(sqlQuery);
			pstmt.setString(1, iepid);
			return pstmt.executeQuery();
		} catch (SQLException e) {
			logger.error("Query failed for IEPID {}: {}", iepid, e.getMessage(), e);
			closeQuietly(pstmt, conn);
			throw e;
		}
	}

	private void closeQuietly(PreparedStatement pstmt, Connection conn) {
		try {
			if (pstmt != null)
				pstmt.close();
		} catch (SQLException ignored) {
		}
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException ignored) {
		}
	}
}
