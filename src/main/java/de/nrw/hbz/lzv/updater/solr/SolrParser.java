package de.nrw.hbz.lzv.updater.solr;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parses Solr JSON responses to extract IEPIDs from OAI identifiers.
 */
public class SolrParser {

	/**
	 * Extracts IEPID from Solr JSON response if OAI identifier matches.
	 * 
	 * @param jsonResponse JSON response from Solr
	 * @return IEPID string or null if no match found
	 */
	public String extractIEPID(String json) {

		JSONObject jsonResponse = new JSONObject(json);

		JSONObject grouped = jsonResponse.optJSONObject("grouped");
		if (grouped == null) {
			return null;
		}

		JSONObject pidGroup = grouped.optJSONObject("IE.internalIdentifier.internalIdentifierType.PID.string.single");
		if (pidGroup == null) {
			return null;
		}
		if (pidGroup.optInt("matches", 0) > 0) {
			JSONArray groups = pidGroup.optJSONArray("groups");
			if (groups == null || groups.isEmpty()) {
				return null;
			}

			String IEPID = groups.getJSONObject(0).optString("groupValue");
			return IEPID;
		}

		return null;
	}
}
