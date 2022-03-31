package se.uu.ub.cora.xmlconverter.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class XmlAttributes {
	String repeatId = "";
	Map<String, String> attributes = new HashMap<>();

	void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	Set<Entry<String, String>> getAttributeSet() {
		return attributes.entrySet();
	}

}