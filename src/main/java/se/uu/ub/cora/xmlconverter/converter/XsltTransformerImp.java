package se.uu.ub.cora.xmlconverter.converter;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XsltTransformerImp implements XsltTransformer {

	private String xslt;

	public XsltTransformerImp(String xsltPath) {
		this.xslt = ResourceReader.readResourceAsString(xsltPath);
	}

	@Override
	public String transform(String inputXml) {
		return tryToTransform(inputXml);
	}

	private String tryToTransform(String inputXml) {
		try {
			return transformXmlUsingXslt(inputXml).trim();
		} catch (Exception e) {
			throw ParseException.withMessageAndException(
					"Error converting place to Cora place: Can not read xml: " + e.getCause(), e);
		}
	}

	private String transformXmlUsingXslt(String xmlFromFedora) throws Exception {
		Transformer transformer = generateTransformer();
		return transformUsingTransformer(xmlFromFedora, transformer);
	}

	private Transformer generateTransformer() throws Exception {
		Source xslInput = new StreamSource(new StringReader(xslt));
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		return transformerFactory.newTransformer(xslInput);
	}

	private String transformUsingTransformer(String xmlFromFedora, Transformer transformer)
			throws TransformerException {
		Source xmlSource = createSourceFromFedoraXml(xmlFromFedora);
		return transformUsingTransformerAndSource(transformer, xmlSource);
	}

	private Source createSourceFromFedoraXml(String xmlFromFedora) {
		StringReader stringReader = new StringReader(xmlFromFedora);
		return new StreamSource(stringReader);
	}

	private String transformUsingTransformerAndSource(Transformer transformer, Source xmlSource)
			throws TransformerException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Result outputResult = new StreamResult(output);
		transformer.transform(xmlSource, outputResult);
		return output.toString(StandardCharsets.UTF_8);
	}

	String getXslt() {
		return this.xslt;
	}
}
