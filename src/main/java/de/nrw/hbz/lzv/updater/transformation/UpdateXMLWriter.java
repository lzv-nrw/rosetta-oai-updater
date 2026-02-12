package de.nrw.hbz.lzv.updater.transformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes transformed XML to file with standardized naming. Creates files named
 * update_{IEPID}.xml in specified output directory.
 */
public class UpdateXMLWriter {

	private static final Logger logger = LoggerFactory.getLogger(UpdateXMLWriter.class);

	/**
	 * Writes transformed XML content to file with IEPID-based naming.
	 *
	 * @param xmlContent Transformed UpdateMD XML as String
	 * @param iepid      IEPID used for filename generation
	 * @param resultDir  Path to output directory (must exist)
	 * @throws IOException if directory doesn't exist or write fails
	 */
	public void writeXML(String xmlContent, String iepid, String resultDir) throws IOException {

		Path resultPath = createResultPath(iepid, resultDir);

		try {
			Files.write(resultPath, xmlContent.getBytes(StandardCharsets.UTF_8));
			logger.info("Written update XML file for IEPID {} to {}", iepid, resultPath);
		} catch (IOException e) {
			logger.error("Failed to write update XML file for IEPID {} to {}: {}", iepid, resultPath, e.getMessage(), e);
			throw new IOException("Failed to write update XML for " + iepid + " to " + resultPath, e);
		}

	}

	/**
	 * Creates output file Path with standardized naming: update_{IEPID}.xml.
	 *
	 * @param iepid     Record identifier
	 * @param resultDir Output directory path
	 * @return Full Path to result file
	 * @throws IllegalArgumentException if paths are invalid
	 */
	private Path createResultPath(String iepid, String resultDir) {
		if (iepid == null || iepid.trim().isEmpty()) {
			logger.error("IEPID cannot be null or empty");
			throw new IllegalArgumentException("IEPID cannot be null or empty");
		}

		Path dirPath = Paths.get(resultDir).normalize();
		if (!Files.exists(dirPath)) {
			logger.error("Output directory does not exist: " + dirPath);
			throw new IllegalArgumentException("Output directory does not exist: " + dirPath);
		}

		return dirPath.resolve("update_" + iepid + ".xml");
	}
}
