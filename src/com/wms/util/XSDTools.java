package com.wms.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashSet;

import javax.xml.soap.Node;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class XSDTools {
	
	static public boolean clearTags = false;
	static private HashSet<String> tags = null;
	
	static public boolean ValidateXSDVsXML( File xsd, String xmlNodeAsString) {
		
		boolean isValid = false;
		File schemaFile = null;
		
		schemaFile = new File(xsd.getAbsolutePath()) ;

		
		InputStream is = new ByteArrayInputStream( xmlNodeAsString.getBytes( Charset.defaultCharset()  ) );
		Source xmlSource = new StreamSource( is );
		// or File schemaFile = new File("/location/to/xsd"); // etc.
		Source xmlFile = new StreamSource(xsd);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XPATH_DATATYPE_NS_URI);
		try {
		  Schema schema = schemaFactory.newSchema(schemaFile);
		  Validator validator = schema.newValidator();
		  validator.validate(xmlSource);
		  System.out.println(xmlFile.getSystemId() + " is valid");
		} catch (SAXException e) {
		  System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
		} catch (IOException e) {}
	
		return isValid;
	}
	
	static boolean ValidateXSDVsXML( File xsd, Node xmlNode) {
		return false;
	}
	
	static public void clearTags() {
		clearTags = true;
	}
	
	static public boolean testForOutOfRangeTags( File xsd, String xmlNodeAsString ) {
		
		if( tags == null ) {
			tags = new HashSet<String>();
		}
		else if ( tags != null && clearTags ) {
			tags.clear();
			clearTags = false;
		}
		
		boolean outOfRange = false;
		// this will avoid normal validation, this is a "low tech/power way of discovering if an xml snippet has tags that
		// are present that are not in the xsd, these snippets are well formed but not valid (hence validation is not possible) 
		Source xsdFileSource = new StreamSource(xsd);
		
		String xsdAsString = getStringFromSource( xsdFileSource );
		
		String[ ] rawStrings = xsdAsString.split(">");
		for( String s: rawStrings) {
			if( s != null && s.contains( "xsd:element") ) {
				// get rid of bad "structures"
				String clearStr = s.replaceAll("/", "");
				// parse out the "name" 
				int startOfName = clearStr.indexOf("name");
				int endOfName = clearStr.indexOf(' ', startOfName);
				// if we got nothing, then maybe we want end of string
				if( endOfName == -1 ) { 
					endOfName = clearStr.length();
				}
				if( startOfName > 0 && endOfName > 0 ) {
					String nameValue = clearStr.substring(startOfName, endOfName);
					if( nameValue != null ) {
						// remove unneeded quotes
						String cleanNamePair = nameValue.replaceAll("\"", "");
						String cleanNVPair[] = cleanNamePair.split("=");
						
						if( cleanNVPair != null ) {
							String key = cleanNVPair[0];
							String value = cleanNVPair[1];
							if( key != null && key.equals("name") ) {
								tags.add(value);
							}
						}
						
					}
				}
			}
		}
		
		return outOfRange;
	}
	
	static public String getStringFromSource( Source sourceToUse ) {
		
		String retStr = "";
		
		try {		    
		    StringWriter writer = new StringWriter();
		    StreamResult result = new StreamResult(writer);
		    TransformerFactory tFactory = TransformerFactory.newInstance();
		    Transformer transformer = tFactory.newTransformer();
		    transformer.transform(sourceToUse,result);
		    retStr = writer.toString();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		return retStr;
	}
	

}
