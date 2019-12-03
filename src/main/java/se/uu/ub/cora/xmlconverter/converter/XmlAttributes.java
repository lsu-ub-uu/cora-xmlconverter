package se.uu.ub.cora.xmlconverter.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class XmlAttributes {
	String repeatId = "";
	Map<String, String> attributes = new HashMap<>();

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public Set<Entry<String, String>> getAttributeSet() {
		return attributes.entrySet();
	}

	public boolean hasAttributes() {
		return attributes.size() > 0;
	}
}