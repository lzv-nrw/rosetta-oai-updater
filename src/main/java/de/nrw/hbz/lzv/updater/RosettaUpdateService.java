package de.nrw.hbz.lzv.updater;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.nrw.hbz.lzv.updater.oai.OAIService;
import de.nrw.hbz.lzv.updater.oracle.OracleDBService;
import de.nrw.hbz.lzv.updater.solr.SolrService;
import de.nrw.hbz.lzv.updater.transformation.XMLTransformService;

/**
 * Service for Rosetta OAI updates. Orchestrates OAI harvesting, Solr lookups,
 * Oracle timestamp comparisons, and XML transformations across institutions and
 * material flows.
 */
public class RosettaUpdateService {
	private static final Logger logger = LoggerFactory.getLogger(RosettaUpdateService.class);

	private final OAIService oaiService;
	private final SolrService solrService;
	private final OracleDBService oracleService;
	private final XMLTransformService xmlTransformService;

	/**
	 * Constructs the service with required service dependencies.
	 * 
	 * @param oaiService          OAI harvesting service for identifier with
	 *                            datestamp and record retrieval
	 * @param solrService         Solr service for IEPID lookup by institution and
	 *                            oai identifier
	 * @param oracleService       Oracle service for datestamp and property queries
	 * @param xmlTransformService XML transformation service for update xml
	 *                            generation
	 */
	public RosettaUpdateService(OAIService oaiService, SolrService solrService, OracleDBService oracleService,
			XMLTransformService xmlTransformService) {
		this.oaiService = oaiService;
		this.solrService = solrService;
		this.oracleService = oracleService;
		this.xmlTransformService = xmlTransformService;
	}

	/**
	 * Processes all institutions from the provided configuration. Iterates through
	 * institutions and material flows, performing datestamp comparisons and
	 * triggering updates when OAI records are newer than IE
	 * 
	 * @param config     Loaded application configuration containing institutions
	 *                   and settings
	 * @param oracleUrl  Oracle database connection URL
	 * @param oracleUser Oracle database username
	 * @param oraclePw   Oracle database password
	 */
	public void processInstitutions(ConfigLoader.Config config, String oracleUrl, String oracleUser, String oraclePw) {
		for (Map.Entry<String, ConfigLoader.Institution> entry : config.getInstitutions().entrySet()) {
			String institutionCode = entry.getKey();
			ConfigLoader.Institution institution = entry.getValue();

			processInstitution(institutionCode, institution, config, oracleUrl, oracleUser, oraclePw);
		}
	}

	/**
	 * Processes a single institution by iterating through all configured material
	 * flows.
	 */
	private void processInstitution(String institutionCode, ConfigLoader.Institution institution,
			ConfigLoader.Config config, String oracleUrl, String oracleUser, String oraclePw) {
		logger.info("Processing institution {}", institutionCode);

		for (ConfigLoader.MaterialFlow materialFlow : institution.getMaterialFlows()) {
			processMaterialFlow(institutionCode, materialFlow, config, oracleUrl, oracleUser, oraclePw);
		}
	}

	/**
	 * Processes a single material flow: harvests OAI identifiers and determines
	 * updates needed.
	 */
	private void processMaterialFlow(String institutionCode, ConfigLoader.MaterialFlow materialFlow,
			ConfigLoader.Config config, String oracleUrl, String oracleUser, String oraclePw) {
		String name = materialFlow.getName();
		String baseUrl = materialFlow.getBaseUrl();
		List<String> sets = materialFlow.getSets();
		String metadataPrefix = materialFlow.getMetadataPrefix();

		Path xsltPath;
		String resultPath;
		try {
			xsltPath = Path.of(materialFlow.getXsltPath());
			resultPath = materialFlow.getResultPath();
		} catch (Exception e) {
			logger.error("Invalid xslt path for material flow {}: {}", name, e.getMessage());
			return;
		}

		logger.info("Processing material flow {}", name);
		try {
			if (sets == null || sets.isEmpty()) {
				logger.info("No sets are selected; harvesting all identifiers");
				for (Map.Entry<String, String> entry : oaiService.harvestAllIdentifiers(baseUrl, metadataPrefix, null)
						.entrySet()) {

					processIdentifier(institutionCode, entry, oracleUrl, oracleUser, oraclePw, baseUrl, metadataPrefix,
							xsltPath, resultPath, config);
				}
			} else {
				for (String set : sets) {
					logger.info("Harvesting identifier from OAI set {}", set);
					for (Map.Entry<String, String> entry : oaiService
							.harvestAllIdentifiers(baseUrl, metadataPrefix, set).entrySet()) {

						processIdentifier(institutionCode, entry, oracleUrl, oracleUser, oraclePw, baseUrl,
								metadataPrefix, xsltPath, resultPath, config);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Failed to harvest identifiers for {}: {}", name, e.getMessage(), e);
		}
	}

	/**
	 * Processes single OAI identifier: Solr lookup, datestamp comparison, generate
	 * update IE if newer.
	 */
	private void processIdentifier(String institutionCode, Map.Entry<String, String> entry, String oracleUrl,
			String oracleUser, String oraclePw, String baseUrl, String metadataPrefix, Path xsltPath, String resultPath,
			ConfigLoader.Config config) {
		String identifier = entry.getKey();

		String iepid;
		try {
			iepid = solrService.executeQueryAndCheckIEPID(institutionCode, identifier);
		} catch (Exception e) {
			logger.warn("Solr lookup failed for identifier {}: {}", identifier, e.getMessage());
			return;
		}

		if (iepid != null && shouldUpdate(entry.getValue(), iepid, oracleUrl, oracleUser, oraclePw)) {
			logger.info("IEPID {} needs to be updated", iepid);
			updateIE(baseUrl, metadataPrefix, identifier, iepid, xsltPath, resultPath, oracleUrl, oracleUser, oraclePw);
		} else {
			logger.info("Nothing to do");
		}
	}

	/**
	 * Determines if an OAI record needs updating based on datestamp comparison.
	 * 
	 * @param oaiDateStr OAI datestamp string in ISO format (yyyy-MM-dd)
	 * @param iepid      IEPID from Solr lookup
	 * @param oracleUrl  Oracle connection URL
	 * @param oracleUser Oracle username
	 * @param oraclePw   Oracle password
	 * @return true if OAI datestamp is newer than IE creation date
	 */
	private boolean shouldUpdate(String oaiDateStr, String iepid, String oracleUrl, String oracleUser,
			String oraclePw) {
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(oaiDateStr);
			String oaiDate = zdt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);

			// Convert IE creation Date from yyyy-MM-dd HH:mm:ss to yyyy-MM-dd
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime ldt = LocalDateTime.parse(oracleService.getCreationDate(iepid), fmt);
			String ieCreationDate = ldt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);

			return LocalDate.parse(oaiDate).isAfter(LocalDate.parse(ieCreationDate));
		} catch (Exception e) {
			logger.warn("Date comparison failed for IEPID: {}", iepid, e);
			return false;
		}
	}

	/**
	 * Triggers XML transformation for record that needs updating.
	 */
	private void updateIE(String baseUrl, String metadataPrefix, String identifier, String iepid, Path xsltPath,
			String resultPath, String oracleUrl, String oracleUser, String oraclePw) {
		try {
			Document recordXml = oaiService.harvestRecord(baseUrl, identifier, metadataPrefix);
			Map<String, String> metadata = oracleService.getMidAndSubType(iepid);
			String mid = metadata.get("mid");
			String subType = metadata.get("subType");

			logger.info("Generating Update XML file for OAI identifier: {} (IEPID: {})", identifier, iepid);
			xmlTransformService.transformToUpdateXML(iepid, mid, subType, recordXml, xsltPath, resultPath);
		} catch (Exception e) {
			logger.error("Generating Update XML file failed for OAI identifier: {} (IEPID: {})", identifier, iepid, e);
		}
	}
}
