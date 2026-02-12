package de.nrw.hbz.lzv.updater.oai;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.*;

import org.w3c.dom.*;

/**
 * Parses OAI-PMH XML responses into domain objects. Handles ListIdentifiers and
 * GetRecord response structures.
 */
public class OAIParser {

	/**
	 * Extracts all non-deleted record identifiers with datestamps from
	 * ListIdentifiers response.
	 *
	 * @param doc OAI-PMH ListIdentifiers XML response
	 * @return Map of identifier with datestamp for all non-deleted records
	 */
	public Map<String, String> extractIdentifiers(Document doc) {
		Map<String, String> map = new HashMap<>();
		NodeList headers = doc.getElementsByTagName("header");

		for (int i = 0; i < headers.getLength(); i++) {
			Element header = (Element) headers.item(i);
			if ("deleted".equals(header.getAttribute("status")))
				continue;

			String identifier = getChildText(header, "identifier");
			String datestamp = getChildText(header, "datestamp");

			if (identifier != null && datestamp != null) {
				map.put(identifier, datestamp);
			}
		}
		return map;
	}

	/**
	 * Extracts the resumptionToken from ListIdentifiers response (if available).
	 *
	 * @param doc OAI-PMH ListIdentifiers XML response
	 * @return resumption token, or null if no token available or empty token
	 */
	public String extractResumptionToken(Document doc) {
		NodeList tokens = doc.getElementsByTagName("resumptionToken");
		if (tokens.getLength() == 0)
			return null;

		String token = tokens.item(0).getTextContent().trim();
		if (token.isEmpty()) {
			return null;
		} else {
			return token;
		}
	}

	/**
	 * Extracts the first record element from GetRecord response.
	 *
	 * @param doc OAI-PMH GetRecord XML response
	 * @return New Document containing the record element
	 * @throws RuntimeException if no record element found or extraction fails
	 */
	public Document extractRecord(Document doc) {
		NodeList recordNodes = doc.getElementsByTagName("record");
		if (recordNodes.getLength() == 0)
			throw new RuntimeException("No record element found in GetRecord response.");

		Element recordElement = (Element) recordNodes.item(0);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document newDoc = db.newDocument();

			Node imported = newDoc.importNode(recordElement, true);
			newDoc.appendChild(imported);

			return newDoc;
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract record", e);
		}
	}

	/**
	 * Helper method to extract text content from named child element.
	 * 
	 * @param parent Parent element to search in
	 * @param tag    Child element tag name
	 * @return Text content or null if not found
	 */
	private String getChildText(Element parent, String tag) {
		NodeList list = parent.getElementsByTagName(tag);

		if (list.getLength() > 0) {
			return list.item(0).getTextContent();
		} else {
			return null;
		}
	}
}
