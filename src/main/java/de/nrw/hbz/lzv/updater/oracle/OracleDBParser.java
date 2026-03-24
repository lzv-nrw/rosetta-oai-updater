package de.nrw.hbz.lzv.updater.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for extracting specific metadata fields from Oracle ResultSets.
 * Converts database query results into a Map.
 */
public class OracleDBParser {

	/**
	 * Extracts the CREATEDATE from ResultSet as metadata map.
	 * 
	 * @param rs ResultSet positioned before first row
	 * @return Map containing "creationDate" key with value, empty map if no rows
	 * @throws SQLException if database read fails
	 */
	public String parseCreationDate(ResultSet rs) throws SQLException {

		if (rs.next()) {
			return rs.getString("IEDATE");
		}
		return null;
	}

	/**
	 * Extracts MID and SUB_TYPE from ResultSet as metadata map.
	 * 
	 * @param rs ResultSet positioned before first row (caller must not call next())
	 * @return Map containing "mid" and "subType" keys, empty map if no rows
	 * @throws SQLException if database read fails
	 */
	public Map<String, String> parseMidAndSubType(ResultSet rs) throws SQLException {
		Map<String, String> metadataMap = new HashMap<>();
		if (rs.next()) {
			metadataMap.put("mid", rs.getString("MID"));
			metadataMap.put("subType", rs.getString("SUB_TYPE"));
		}
		return metadataMap;
	}
}
