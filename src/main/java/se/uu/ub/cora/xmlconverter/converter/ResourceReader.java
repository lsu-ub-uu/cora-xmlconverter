/*
 * Copyright 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.xmlconverter.converter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class ResourceReader {
	private ResourceReader() {
		// not called
		throw new UnsupportedOperationException();
	}

	public static String readResourceAsString(String resourceFile) {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try (Stream<String> lines = new BufferedReader(new InputStreamReader(
				contextClassLoader.getResourceAsStream(resourceFile), StandardCharsets.UTF_8))
						.lines();) {
			return tryToReadResourceLines(lines);
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to read resource to string for file: " + resourceFile, e);
		}
	}

	private static String tryToReadResourceLines(Stream<String> lines) {
		StringBuilder data = readAllLinesWithLinebreaks(lines);
		removeAddedExtraLineBreakAtEnd(data);
		return data.toString();
	}

	private static StringBuilder readAllLinesWithLinebreaks(Stream<String> lines) {
		StringBuilder data = new StringBuilder();
		lines.forEach(line -> data.append(line).append('\n'));
		return data;
	}

	private static void removeAddedExtraLineBreakAtEnd(StringBuilder data) {
		data.replace(data.lastIndexOf("\n"), data.length(), "");
	}

}
