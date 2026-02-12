package de.nrw.hbz.lzv.updater.oai;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

/**
 * Service for harvesting metadata from OAI-PMH endpoints. Uses OAIClient for
 * HTTP requests and OAIParser for XML processing.
 */
public class OAIService {

	private final OAIClient client;
	private final OAIParser parser;
	private static final Logger logger = LoggerFactory.getLogger(OAIService.class);

	/**
	 * Creates an OAIService instance using client and parser implementations.
	 */
	public OAIService() {
		this.client = new OAIClient();
		this.parser = new OAIParser();
	}

	/**
	 * Harvests all identifiers with their datestamps from an OAI-PMH endpoint using
	 * ListIdentifiers.
	 *
	 * @param baseUrl        OAI-PMH base URL
	 * @param metadataPrefix Metadata format
	 * @param set            OAI set name (null for all sets)
	 * @return A map containing record identifiers as keys and datestamps as values
	 * @throws RuntimeException if HTTP request or XML parsing fails
	 */
	public Map<String, String> harvestAllIdentifiers(String baseUrl, String metadataPrefix, String set) {

		Map<String, String> allIdentifiers = new HashMap<>();
		String resumptionToken = null;

		do {
			String requestUrl;
			if (resumptionToken == null) {
				if (set == null) {
					requestUrl = baseUrl + "?verb=ListIdentifiers" + "&metadataPrefix=" + metadataPrefix;
				} else {
					requestUrl = baseUrl + "?verb=ListIdentifiers&set=" + set + "&metadataPrefix=" + metadataPrefix;
				}
			} else {
				requestUrl = baseUrl + "?verb=ListIdentifiers&resumptionToken=" + resumptionToken;
			}

			logger.info("Fetching OAI identifiers from: {}", requestUrl);

			Document doc = client.fetchXml(requestUrl);
			allIdentifiers.putAll(parser.extractIdentifiers(doc));
			resumptionToken = parser.extractResumptionToken(doc);

		} while (resumptionToken != null);

		return allIdentifiers;
	}

	/**
	 * Fetches a single OAI record using GetRecord verb.
	 *
	 * @param baseUrl        OAI-PMH basr URL
	 * @param identifier     OAI record identifier
	 * @param metadataPrefix Metadata format
	 * @return A parsed XML {@link Document} containing the record
	 */
	public Document harvestRecord(String baseUrl, String identifier, String metadataPrefix) {
		String requestUrl = baseUrl + "?verb=GetRecord&metadataPrefix=" + metadataPrefix + "&identifier=" + identifier;
		logger.info("Fetching OAI record: {}", requestUrl);

		Document doc = client.fetchXml(requestUrl);
		return parser.extractRecord(doc);
	}
}
