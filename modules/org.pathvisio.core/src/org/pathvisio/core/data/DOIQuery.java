/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2022 BiGCaT Bioinformatics, WikiPathways
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import org.pathvisio.libgpml.debug.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class can handle a query for a DOI record. Just instantiate this class
 * with a given DOI id (doi), and run execute() (this method may block, so don't
 * call it from the UI thread) The result can then be obtained with getResult()
 * TODO: move DefaultHandler methods to private subclass, they don't need to be
 * exposed.
 */
public class DOIQuery extends DefaultHandler {
	static final String URL_BASE = "https://www.crossref.org/openurl/";

	String id;
	DOIResult result;

	/**
	 * Prepares a new DOI query for the given doi , e.g.
	 * "10.1016/0006-291X(75)90498-2".
	 */
	public DOIQuery(String id) {
		this.id = id;
	}

	/**
	 * Execute a query. Don't call this from the UI thread, because this method
	 * blocks.
	 */
	public void execute() throws IOException, SAXException {
		// TODO: assert not being in UI thread
		String urlString = URL_BASE;
		urlString += "?pid=hu.finterly@gmail.com&format=unixref&id=doi:" + id + "&noredirect=true"; // TODO
		URL url = new URL(urlString);
		try {
			InputStream is = url.openStream();

			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.setEntityResolver(this);
			result = new DOIResult();
			result.setId(id);
			xmlReader.parse(new InputSource(is));
			is.close();
		} catch (FileNotFoundException e) { // Could not find DOI id file
			JOptionPane.showConfirmDialog(null, "DOI not found.", "Warning", JOptionPane.PLAIN_MESSAGE);
			Logger.log.error("Couldn't open doi url " + url);
		} catch (MalformedURLException e) {
			Logger.log.error("Couldn't open doi url " + url);
		} catch (IOException e) {
			Logger.log.error("Couldn't open doi url " + url);
		}
	}

	/**
	 * get the result, after execute() has finished.
	 */
	public DOIResult getResult() {
		return result;
	}

//	String parsingId;
//	String parsingName;
//	String parsingElement;
//	String parsingValue;

//	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		System.out.println("New element: " + localName + ", " + attributes.getValue(NAME));
//		parsingElement = localName;
//		parsingName = attributes.getValue(NAME);
//		parsingValue = "";
//	}

//	public void characters(char[] ch, int start, int length) throws SAXException {
//		parsingValue += new String(ch, start, length).trim();
//		System.out.println("characters: " + new String(ch, start, length).trim());
//	}

//	public void endElement(String uri, String localName, String qName) throws SAXException {
//		System.out.println("End element: " + localName);
//		if (parsingElement == ID) {
//			parsingId = parsingValue;
//		}
//		if (TITLE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing title: " + parsingValue);
//			result.setTitle(parsingValue);
//		} else if (PUBDATE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing pubdate: " + parsingValue);
//			if (parsingValue.length() >= 4)
//				parsingValue = parsingValue.substring(0, 4);
//			result.setYear(parsingValue);
//		} else if (SOURCE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing source: " + parsingValue);
//			result.setSource(parsingValue);
//		} else if (AUTHOR.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsed author: " + parsingValue);
//			result.addAuthor(parsingValue);
//		}
//		parsingElement = "";
//		parsingName = "";
//	}

//	static final String ITEM = "Item";
	static final String ID = "doi";
	static final String NAME = "Name";
	static final String TITLE = "title";
	static final String PUBDATE = "month";
	static final String SOURCE = "full_title";
//	static final String AUTHOR_LIST = "AuthorList";
	static final String AUTHOR = "given_name";

}
