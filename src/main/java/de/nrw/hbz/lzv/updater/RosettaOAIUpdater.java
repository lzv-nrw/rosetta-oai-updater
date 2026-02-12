package de.nrw.hbz.lzv.updater;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nrw.hbz.lzv.updater.oai.OAIService;
import de.nrw.hbz.lzv.updater.oracle.OracleDBService;
import de.nrw.hbz.lzv.updater.solr.SolrService;
import de.nrw.hbz.lzv.updater.transformation.XMLTransformService;

/**
 * Entry point for the Rosetta OAI Updater application.
 */
public class RosettaOAIUpdater {
	private static final Logger logger = LoggerFactory.getLogger(RosettaOAIUpdater.class);

	/**
	 * Application entry point. Loads configuration, wires dependencies, and starts
	 * the process for generating update XML files.
	 */
	public static void main(String[] args) {
		logger.info("Rosetta OAI Updater starting...");

		try {
			// Load configuration
			Path configPath;
			if (System.getProperty("config.path") != null && !System.getProperty("config.path").trim().isEmpty()) {
				configPath = Path.of(System.getProperty("config.path"));
			} else {
				configPath = Path.of("config.yml");
			}
			logger.info("Loading configuration from " + configPath + "...");

			ConfigLoader configLoader = new ConfigLoader(configPath);
			ConfigLoader.Config config = configLoader.loadConfig();

			// Instantiate services
			OAIService oaiService = new OAIService();
			SolrService solrService = new SolrService(config.getSolr());
			OracleDBService oracleService = new OracleDBService(config.getOracle());
			XMLTransformService xmlService = new XMLTransformService();

			// Extract Oracle credentials from config
			String oracleUrl = config.getOracle().getUrl();
			String oracleUser = config.getOracle().getUser();
			String oraclePw = config.getOracle().getPassword();

			// Wire dependencies and execute business logic
			RosettaUpdateService service = new RosettaUpdateService(oaiService, solrService, oracleService, xmlService);
			service.processInstitutions(config, oracleUrl, oracleUser, oraclePw);

			logger.info("Processing completed successfully",
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

		} catch (Exception e) {
			logger.error("Application failed with error", e);
			System.exit(1);
		}
	}
}
