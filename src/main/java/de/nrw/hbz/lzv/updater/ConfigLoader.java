package de.nrw.hbz.lzv.updater;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Loads and parses a YAML configuration file into typed Java objects. The
 * configuration includes Solr connection details and institution-specific
 * settings.
 */
public class ConfigLoader {

	private final Path configPath;

	/**
	 * Creates a new ConfigLoader with the given path to the YAML configuration
	 * file.
	 *
	 * @param configPath Path to the YAML configuration file.
	 */
	public ConfigLoader(Path configPath) {
		this.configPath = configPath;
	}

	/**
	 * Reads and deserializes the YAML configuration file into a {@link Config}
	 * instance using SnakeYAML.
	 *
	 * @return A populated {@link Config} object representing the configuration
	 *         content.
	 * @throws IOException      if the configuration file cannot be opened or read.
	 * @throws RuntimeException if the YAML structure is invalid or cannot be
	 *                          mapped.
	 */
	public Config loadConfig() throws IOException {
		LoaderOptions loaderOptions = new LoaderOptions();
		Constructor constructor = new Constructor(Config.class, loaderOptions);
		Yaml yaml = new Yaml(constructor);

		try (InputStream input = Files.newInputStream(configPath)) {
			return yaml.loadAs(input, Config.class);
		} catch (IOException e) {
			throw new IOException("Failed to read YAML configuration: " + configPath, e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse YAML configuration: " + configPath, e);
		}
	}

	/**
	 * Root object representing the entire YAML configuration structure.
	 */
	public static class Config {
		private SolrConfig solr;
		private OracleConfig oracle;
		private Map<String, Institution> institutions;

		/** @return The Solr configuration section. */
		public SolrConfig getSolr() {
			return solr;
		}

		/** Sets the Solr configuration section. */
		public void setSolr(SolrConfig solr) {
			this.solr = solr;
		}

		/** @return The Oracle configuration section. */
		public OracleConfig getOracle() {
			return oracle;
		}

		/** Sets the Oracle configuration section. */
		public void setOracle(OracleConfig oracle) {
			this.oracle = oracle;
		}

		/** @return A map of institutions keyed by their identifiers. */
		public Map<String, Institution> getInstitutions() {
			return institutions;
		}

		/** Sets the map of institutions. */
		public void setInstitutions(Map<String, Institution> institutions) {
			this.institutions = institutions;
		}
	}

	/**
	 * Holds connection details for a Solr instance.
	 */
	public static class SolrConfig {
		private String url;
		private String authHeader;

		/** @return The Solr base URL. */
		public String getUrl() {
			return url;
		}

		/** Sets the Solr base URL. */
		public void setUrl(String url) {
			this.url = url;
		}

		/** @return The HTTP authentication header used to connect to Solr. */
		public String getAuthHeader() {
			return authHeader;
		}

		/** Sets the HTTP authentication header for Solr access. */
		public void setAuthHeader(String authHeader) {
			this.authHeader = authHeader;
		}
	}

	/**
	 * Holds connection details for an Oracle database instance.
	 */
	public static class OracleConfig {
		private String url;
		private String user;
		private String password;

		/** @return The Oracle URL. */
		public String getUrl() {
			return url;
		}

		/** Sets the Oracle base URL. */
		public void setUrl(String url) {
			this.url = url;
		}

		/** @return The user used to connect to Oracle database. */
		public String getUser() {
			return user;
		}

		/** Sets the user for Oracle database access. */
		public void setUser(String user) {
			this.user = user;
		}

		/** @return The password used to connect to Oracle database. */
		public String getPassword() {
			return password;
		}

		/** Sets the password for Oracle database access. */
		public void setPassword(String password) {
			this.password = password;
		}
	}

	/**
	 * Represents an institution with configured material flows.
	 */
	public static class Institution {
		private List<MaterialFlow> materialFlows;

		/** @return The list of material flows for this institution. */
		public List<MaterialFlow> getMaterialFlows() {
			return materialFlows;
		}

		/** Sets the material flows for this institution. */
		public void setMaterialFlows(List<MaterialFlow> materialFlows) {
			this.materialFlows = materialFlows;
		}
	}

	/**
	 * Holds metadata and processing configuration for a material flow defined in
	 * the YAML configuration.
	 */
	public static class MaterialFlow {
		private String name;
		private String baseUrl;
		private List<String> sets;
		private String metadataPrefix;
		private String resultPath;
		private String xsltPath;

		/** @return The name of the material flow. */
		public String getName() {
			return name;
		}

		/** Sets the name of the material flow. */
		public void setName(String name) {
			this.name = name;
		}

		/** @return The OAI-PMH base URL for harvesting. */
		public String getBaseUrl() {
			return baseUrl;
		}

		/** Sets the OAI-PMH base URL for harvesting. */
		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		/** @return The OAI-PMH set identifier. */
		public List<String> getSets() {
			return sets;
		}

		/** Sets the OAI-PMH set identifier. */
		public void setSets(List<String> sets) {
			this.sets = sets;
		}

		/** @return The OAI-PMH metadata prefix. */
		public String getMetadataPrefix() {
			return metadataPrefix;
		}

		/** Sets the OAI-PMH metadata prefix. */
		public void setMetadataPrefix(String metadataPrefix) {
			this.metadataPrefix = metadataPrefix;
		}

		/** @return The path to the XSLT file used for metadata transformation. */
		public String getXsltPath() {
			return xsltPath;
		}

		/** Sets the path to the XSLT file used for metadata transformation. */
		public void setXsltPath(String xsltPath) {
			this.xsltPath = xsltPath;
		}

		/** @return The output path for the generated update XML. */
		public String getResultPath() {
			return resultPath;
		}

		/** Sets the output path for the generated update XML. */
		public void setResultPath(String resultPath) {
			this.resultPath = resultPath;
		}
	}
}
