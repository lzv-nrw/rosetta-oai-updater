package de.nrw.hbz.lzv.updater.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nrw.hbz.lzv.updater.ConfigLoader.OracleConfig;

/**
 * Service for querying Oracle metadata by IEPID using OracleDBClient and
 * OracleDBParser.
 */
public class OracleDBService {
	private static final Logger logger = LoggerFactory.getLogger(OracleDBService.class);

	private final OracleDBClient client;
	private final OracleDBParser parser;
	private final OracleConfig oracleConfig;

	public OracleDBService(OracleConfig oracleConfig) {
		this.client = new OracleDBClient();
		this.parser = new OracleDBParser();
		this.oracleConfig = oracleConfig;
	}

	/**
	 * Retrieves creation date of the IE from Oracle.
	 * 
	 * * @param iepid IEPID to lookup
	 */
	public String getCreationDate(String iepid) throws SQLException {
		logger.info("Retrieving creationDate for IEPID {}", iepid);

		String query = "SELECT HDE.CREATEDATE FROM V201_RPT00.HDECONTROL_VIEW HDE "
				+ "WHERE HDE.OBJECTTYPE = 'INTELLECTUAL_ENTITY' AND HDE.LIFECYCLE = 'IN_PERMANENT_REPOSITORY' "
				+ "AND HDE.PID = ? ORDER BY HDE.VERSION DESC FETCH FIRST 1 ROW ONLY";

		try (ResultSet rs = client.executePreparedQuery(oracleConfig.getUrl(), oracleConfig.getUser(),
				oracleConfig.getPassword(), query, iepid)) {
			return parser.parseCreationDate(rs);

		} catch (SQLException e) {
			logger.error("Creation date query failed for IEPID {}: {}", iepid, e.getMessage());
			throw e;
		}
	}

	/**
	 * Returns raw MID and SUB_TYPE values needed for sourceMD section.
	 * 
	 * * @param iepid IEPID to lookup
	 */
	public Map<String, String> getMidAndSubType(String iepid) throws SQLException {

		logger.debug("Fetching MID and SUB_TYPE for IEPID {}", iepid);

		String query = "SELECT h1.MID, h3.SUB_TYPE FROM V201_ROS00.HDEMETADATA h1 "
				+ "INNER JOIN V201_ROS00.HDEPIDMID h2 ON h2.MID = h1.MID "
				+ "INNER JOIN V201_ROS00.HDEMETADATAREGISTRY h3 ON h3.MDID = h1.MDID "
				+ "WHERE h2.PID = ? AND (h1.MDID BETWEEN 61 AND 70 OR h1.MDID BETWEEN 73 AND 76) "
				+ "AND h3.TYPE = 'source'";

		try (ResultSet rs = client.executePreparedQuery(oracleConfig.getUrl(), oracleConfig.getUser(),
				oracleConfig.getPassword(), query, iepid)) {

			return parser.parseMidAndSubType(rs);

		} catch (SQLException e) {
			logger.error("MID/subType query failed for IEPID {}: {}", iepid, e.getMessage());
			throw e;
		}
	}
}
