package de.nrw.hbz.lzv.updater.solr;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrClient {
	private static final Logger logger = LoggerFactory.getLogger(SolrClient.class);

	/**
	 * Executes Solr query and returns raw JSON response.
	 * 
	 * @param url        Solr base URL
	 * @param authHeader Authorization header value
	 * @param qParam     Query parameter
	 * @param fqParam    Filter query parameter
	 * @return Raw JSON response body
	 * @throws Exception on HTTP errors or network issues
	 */
	public String queryResponse(String url, String authHeader, String qParam, String fqParam) throws Exception {
		String query = "q=" + encode(qParam) + "&fq=" + encode(fqParam) + "&group.ngroups=true&group.field="
				+ encode("IE.internalIdentifier.internalIdentifierType.PID.string.single") + "&group=true&wt=json";

		logger.debug("Executing Solr query: {}?{}", url, query);
		URI uri = URI.create(url + "?" + query);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(uri).header("Authorization", authHeader)
				.header("Accept", "application/json").GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			logger.error("Solr request failed: HTTP {} - {}", response.statusCode(), response.body());
			throw new RuntimeException("Solr request failed: HTTP " + response.statusCode() + "\n" + response.body());
		}
		return response.body();
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
