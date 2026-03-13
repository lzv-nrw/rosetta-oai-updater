package de.nrw.hbz.lzv.updater.transformation;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Templates;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs XSLT transformations with parameter injection and returns
 * transformed String.
 */
public class XMLTransformer {
	private static final Logger logger = LoggerFactory.getLogger(XMLTransformer.class);

	/**
	 * Transforms XML source using XSLT with dynamic parameters.
	 *
	 * @param source  XML input source (DOMSource, StreamSource, etc.)
	 * @param xsl     XSLT stylesheet as InputStream
	 * @param iepid   IEPID parameter for XSLT
	 * @param mid     Metadata ID parameter for XSLT
	 * @param subType Metadata subtype parameter for XSLT
	 * @return Transformed XML as String
	 * @throws TransformerException if XSLT processing fails
	 */
	public String transform(Source source, InputStream xsl, String iepid, String mid, String subType) throws Exception {

		TransformerFactory factory = createSecureTransformerFactory();

		try {
			Templates templates = factory.newTemplates(new StreamSource(xsl));
			Transformer transformer = templates.newTransformer();

			// Set XSLT parameters
			transformer.setParameter("IEPID", iepid);
			if (mid != null) {
				transformer.setParameter("mid", mid);
				transformer.setParameter("subType", subType);
			}

			StringWriter resultWriter = new StringWriter();
			StreamResult result = new StreamResult(resultWriter);

			transformer.transform(source, result);
			String transformedXml = resultWriter.toString();
			logger.info("XSLT transformation successful for IEPID {}", iepid);
			return transformedXml;

		} catch (TransformerException e) {
			logger.error("XSLT transformation failed for IEPID {}: {}", iepid, e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Creates TransformerFactory with XXE protection (critical for external XSL!).
	 */
	private TransformerFactory createSecureTransformerFactory() {
		TransformerFactory factory = TransformerFactory.newInstance();

		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		} catch (Exception e) {
			logger.warn("Could not set XSLT security features: {}", e.getMessage());
		}

		return factory;
	}
}
