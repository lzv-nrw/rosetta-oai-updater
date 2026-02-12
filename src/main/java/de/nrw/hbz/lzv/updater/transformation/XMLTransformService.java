package de.nrw.hbz.lzv.updater.transformation;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for generating UpdateMD XML files from OAI records via XSLT using
 * XMLTransformer and UpdateXMLWriter.
 */
public class XMLTransformService {
	private static final Logger logger = LoggerFactory.getLogger(XMLTransformService.class);
	private final XMLTransformer transformer;
	private final UpdateXMLWriter writer;

	public XMLTransformService() {
		this.transformer = new XMLTransformer();
		this.writer = new UpdateXMLWriter();
	}

	/**
	 * Transforms OAI record XML to UpdateMD format using XSLT and writes to file.
	 * 
	 * @param iepid      IEPID for the record (used in filename and passed to XSLT)
	 * @param mid        Metadata identifier (passed to XSLT)
	 * @param subType    Metadata subtype (passed to XSLT)
	 * @param recordXml  Input OAI record as DOM Document
	 * @param xslPath    Path to XSLT file
	 * @param resultPath Directory path for output file
	 * @throws Exception if transformation fails, XSLT error, or file write fails
	 */
	public void transformToUpdateXML(String iepid, String mid, String subType, Document recordXml, Path xslPath,
			String resultPath) throws Exception {
		logger.info("Generating update XML file for IEPID {}", iepid);

		Source xmlSource = new DOMSource(recordXml);
		try (InputStream xslStream = Files.newInputStream(xslPath)) {

			String transformedXml = transformer.transform(xmlSource, xslStream, iepid, mid, subType);

			writer.writeXML(transformedXml, iepid, resultPath);
		}
	}
}
