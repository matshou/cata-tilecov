/*
 * Cata-Tilecov - Generates tile coverage reports for Cataclysm.
 * Copyright (C) 2022 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.matshou.cata.tilecov.coverage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import io.matshou.cata.tilecov.Main;

import static io.matshou.cata.tilecov.coverage.TilesetCoverage.CoverageStats;
import static io.matshou.cata.tilecov.coverage.TilesetCoverage.CoverageType;

/**
 * This class represents a report that relays information on how much game objects
 * a particular tileset is covering. It also includes information on the type of
 * coverage being provided to each object.
 * <p>
 * Once the class object is constructed an HTML document will be created will a complete
 * coverage report. To write the report to file call {@link #writeToFile(Path)} method.
 */
public class TilesetCoverageReport {

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

	private static final Element FLEX_TABLE = divWithAttributes(Map.of("class", "flex-table"));
	private static final Element FLEX_ROW = divWithAttributes(Map.of("class", "flex-row"));
	private static final Element FLEX_COLUMN = divWithAttributes(Map.of("class", "flex-column"));
	private static final Element INDENTED_TEXT = divWithAttributes(Map.of("class", "indented-text"));

	private final Map<TilesetCoverage, Document> coverageReports = new HashMap<>();

	/**
	 * Create coverage reports for given {@code Set} of tileset coverages.
	 * The constructor will create HTML document objects that will contain statistical
	 * reports for every tileset. To write the documents to file call {@link #writeToFile(Path).}
	 *
	 * @param tilesetCoverage {@code Set} of tileset coverage to generate reports for.
	 */
	public TilesetCoverageReport(Set<TilesetCoverage> tilesetCoverage) {
		Document htmlDocument = Jsoup.parse("<html lang=\"en\">");

		// HTML head element
		Element head = htmlDocument.head();

		Attributes linkAttributes = new Attributes();
		linkAttributes.put("rel", "stylesheet");
		linkAttributes.put("href", "coverage.css");

		head.appendChild(new Element(Tag.valueOf("link"), null, linkAttributes));
		head.appendChild(new Element("title").text("Cataclysm Tileset Coverage Report"));

		tilesetCoverage.forEach(this::createCoverageReport);
	}

	private void createCoverageReport(TilesetCoverage coverage) {
		Document htmlDocument = Jsoup.parse("<html lang=\"en\">");

		Attributes linkAttributes = new Attributes();
		linkAttributes.put("rel", "stylesheet");
		linkAttributes.put("href", "coverage.css");

		// tileset display name
		String tilesetName = coverage.getTileset().getDisplayName();

		// HTML head element
		Element head = htmlDocument.head();

		head.appendChild(new Element(Tag.valueOf("link"), null, linkAttributes));
		head.appendChild(new Element("title").text(tilesetName + " - Tileset Coverage Report"));

		// HTML body element
		Element body = htmlDocument.body();

		body.appendChild(new Element("h1").text(tilesetName));
		body.appendChild(new Element("hr"));

		// table that will contain report data for tileset
		Element table = FLEX_TABLE.shallowClone();

		// report table columns
		Element tableColumns = FLEX_ROW.shallowClone().appendChildren(List.of(
				cloneElement(FLEX_COLUMN, INDENTED_TEXT.shallowClone().text("Files")),
				cloneElement(FLEX_COLUMN, imageElement("assets/total.png", "total", 25)),
				cloneElement(FLEX_COLUMN, imageElement("assets/eye.png", "looks-like", 25)),
				cloneElement(FLEX_COLUMN, imageElement("assets/x.png", "no-coverage", 25)),
				cloneElement(FLEX_COLUMN, INDENTED_TEXT.shallowClone().text("Coverage"))
		));
		table.appendChild(tableColumns);

		for (Map.Entry<Path, ImmutableMap<String, CoverageType>> entry : coverage.data.entrySet()) {
			CoverageStats coverageStats = Objects.requireNonNull(coverage.stats.get(entry.getKey()));
			table.appendChild(getReportTableRow(entry.getKey(),
					coverageStats.getObjectsTotal(),
					coverageStats.getUniqueCoverageTotal(),
					coverageStats.getInheritedTotal(),
					coverageStats.getNoCoverageTotal()
			));
		}
		body.appendChild(table);

		// map the document to tileset coverage instance
		coverageReports.put(coverage, htmlDocument);
	}

	/**
	 * Write the contents of HTML document containing the coverage report to a file
	 * in directory denoted by designated output directory. The name of the file will
	 * always be {@code coverage.html}. Along with the HTML file multiple
	 * dependency files will be included in the output directory.
	 *
	 * @param outputDir path to directory where to write the file.
	 *
	 * @throws IOException when an I/O error occurred while reading or writing from stream.
	 * @throws FileNotFoundException when unable to find {@code coverage.css} file in jar or output directory.
	 */
	public void writeToFile(Path outputDir) throws IOException {

		// coverage CSS file
		copyFileFromJar("coverage.css", outputDir);

		// copy all HTML asset files
		String[] assetFilePaths = { "eye.png", "total.png", "x.png" };
		for (String assetFilePath : assetFilePaths) {
			copyFileFromJar("assets/" + assetFilePath, outputDir);
		}
		// write coverage report HTML documents to file
		for (Map.Entry<TilesetCoverage, Document> entry : coverageReports.entrySet()) {
			Path htmlPath = outputDir.resolve(entry.getKey().getTileset().getName() + ".html");
			Files.asCharSink(htmlPath.toFile(), Charset.defaultCharset()).write(entry.getValue().outerHtml());
		}
	}

	/**
	 * Copy file from application jar to specified output directory.
	 * <p>
	 * Note that the file must be a regular file and <b>not</b> a directory.
	 *
	 * @param filePath path to file to copy.
	 * @param outputDir path to directory to copy the file to.
	 *
	 * @throws IOException when an I/O exception occurred while copying file.
	 */
	private void copyFileFromJar(String filePath, Path outputDir) throws IOException {

		InputStream stream = TilesetCoverageReport.class.getResourceAsStream('/' + filePath);
		if (stream == null) {
			String msg = "Unable to find '%s' file in jar";
			throw new FileNotFoundException(String.format(msg, filePath));
		}
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);

		File targetFile = outputDir.resolve(filePath).toFile();
		File targetParentFile = targetFile.getParentFile();
		if (!targetParentFile.exists() && !targetParentFile.mkdirs()) {
			throw new IOException("Unable to create directory structure for path: " + targetFile.getPath());
		}
		Files.write(buffer, targetFile);
		if (!targetFile.exists()) {
			String msg = "Unable to find '%s' file in output dir: %s";
			throw new FileNotFoundException(String.format(msg, filePath, outputDir));
		}
	}

	private static Element divWithAttributes(Map<String, String> attributesMap) {

		Attributes attributes = new Attributes();
		for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
			attributes.add(entry.getKey(), entry.getValue());
		}
		return new Element(Tag.valueOf("div"), null, attributes);
	}

	private static Element linkElement(String link, String name) {

		Attributes attributes = new Attributes();
		attributes.add("href", link);
		attributes.add("target", "blank");

		return new Element(Tag.valueOf("a"), null, attributes).text(name);
	}

	private static Element imageElement(String src, String alt, int size) {

		Attributes attributes = new Attributes();
		attributes.add("src", src);
		attributes.add("alt", alt);
		if (size > 0) {
			attributes.add("width", String.valueOf(size));
			attributes.add("height", String.valueOf(size));
		}
		return new Element(Tag.valueOf("img"), null, attributes);
	}

	private static Element cloneElement(Element element, Element appendChild) {
		return element.shallowClone().appendChild(appendChild);
	}

	private static Element getReportTableRow(Path path, int total, int unique, int inherited, int none) {

		String color = "green";
		double percent = (inherited + unique) / ((double) total) * 100;
		if (percent < 33) {
			color = "red";
		}
		else if (percent < 66) {
			color = "blue";
		}
		String pathName = Main.getGameDirectory().relativize(path).toString().replace('\\', '/');
		Element result = FLEX_ROW.shallowClone();

		Element linkElement = cloneElement(INDENTED_TEXT, linkElement("file:///" + path, pathName));
		result.appendChild(new Element("div").appendChild(linkElement));

		result.appendChild(new Element("div").text(String.valueOf(total)));
		result.appendChild(new Element("div").text(String.valueOf(inherited)));
		result.appendChild(new Element("div").text(String.valueOf(none)));

		String percentText = DECIMAL_FORMAT.format(percent) + '%';
		Element coverageBar = divWithAttributes(Map.of("class", "coverage-bar",
				"color", color, "style", "flex: 0 0 " + percentText
		));
		Element coverageText = divWithAttributes(Map.of("class", "coverage-text")).text(percentText);
		return result.appendChild(new Element("div").appendChild(coverageBar).appendChild(coverageText));
	}
}
