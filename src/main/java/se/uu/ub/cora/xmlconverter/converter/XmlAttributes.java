package se.uu.ub.cora.xmlconverter.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class XmlAttributes {
	public String repeatId = "";
	public Map<String, String> attributes = new HashMap<>();

	public XmlAttributes() {
		// TODO Auto-generated constructor stub
	}

	public XmlAttributes(String repeatId, Map<String, String> attributes) {
		this.repeatId = repeatId;
		this.attributes = attributes;
	}

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