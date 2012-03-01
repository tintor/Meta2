package tintor.apps.facebook_spider;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XPathExample {
	public static void main(final String[] args) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		//domFactory.setNamespaceAware(true); // never forget this!
		domFactory.setXIncludeAware(false);
		final DocumentBuilder builder = domFactory.newDocumentBuilder();
		final Document doc = builder.parse("src/tintor/apps/facebook_spider/sample.htm");

		final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//a");

		//final Object result = expr.evaluate(doc, XPathConstants.NODESET);
		//final NodeList nodes = (NodeList) result;
		//for (int i = 0; i < nodes.getLength(); i++) {
		//		System.out.println(nodes.item(i).getNodeValue());
		//	}

	}
}