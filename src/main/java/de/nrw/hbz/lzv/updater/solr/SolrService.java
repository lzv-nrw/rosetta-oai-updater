package de.nrw.hbz.lzv.updater.solr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nrw.hbz.lzv.updater.ConfigLoader.SolrConfig;

/**
 * Service for looking up IEPIDs by OAI identifier using Solr. Orchestrates
 * SolrClient for HTTP requests and SolrParser for JSON processing.
 */
public class SolrService {
	private static final Logger logger = LoggerFactory.getLogger(SolrService.class);
	private final SolrClient client;
	private final SolrParser parser;
	private final SolrConfig solrConfig;

	/**
	 * Default constructor creating default client and parser implementations.
	 */
	public SolrService(SolrConfig solrConfig) {
		this.client = new SolrClient();
		this.parser = new SolrParser();
		this.solrConfig = solrConfig;

	}

	/**
	 * Executes Solr lookup for IEPID by OAI identifier.
	 * 
	 * @param oaiIdentifier OAI identifier to lookup
	 * @return IEPID or null if not found
	 * @throws Exception on HTTP or parsing errors
	 */
	public String executeQueryAndCheckIEPID(String institutionCode, String oaiIdentifier) throws Exception {
		logger.info("Looking up IEPID for OAI identifier: {} at institution: {}", oaiIdentifier, institutionCode);

		String qParam = "((IE.objectIdentifier.objectIdentifierType.OAI.string.single:\"" + oaiIdentifier + "\"))";
		String fqParam = "IE.objectCharacteristics.owner.string.single:CRS00." + institutionCode
				+ " OR IE.objectCharacteristics.owner.string.single:CRS00." + institutionCode + ".*";

		String jsonResponse = client.queryResponse(solrConfig.getUrl(), solrConfig.getAuthHeader(), qParam, fqParam);
		String iepid = parser.extractIEPID(jsonResponse);

		if (iepid != null) {
			logger.info("Found IEPID {} for OAI identifier {}", iepid, oaiIdentifier);
		} else {
			logger.info("No results for OAI identifier {}", oaiIdentifier);
		}

		return iepid;
	}
}
