package de.nrw.hbz.lzv.updater.oai;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.*;

import org.w3c.dom.*;

/**
 * HTTP client for OAI-PMH endpoints. Performs GET requests with configurable
 * timeouts and parses XML responses.
 */
public class OAIClient {

	private static final int CONNECT_TIMEOUT_MS = 10_000;
	private static final int READ_TIMEOUT_MS = 10_000;

	/**
	 * Fetches and parses an XML document from the given OAI-PMH URL.
	 *
	 * @param urlString Full OAI request URL
	 * @return Parsed {@link Document} containing the OAI-PMH response XML
	 * @throws RuntimeException if URL is invalid, HTTP request fails, or XML
	 *                          parsing fails
	 */
	public Document fetchXml(String urlString) {
		try {

			URL url = new URI(urlString).toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
			conn.setReadTimeout(READ_TIMEOUT_MS);

			// Validate HTTP status
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("HTTP " + status + " for URL: " + urlString);
			}

			try (InputStream in = conn.getInputStream()) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				DocumentBuilder db = dbf.newDocumentBuilder();
				return db.parse(in);

			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch XML from " + urlString, e);
		}
	}
}
