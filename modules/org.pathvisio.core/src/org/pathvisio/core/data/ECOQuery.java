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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class can handle a query for a ECO record. Just instantiate this class
 * with a given ECO id (ECO), and run execute() (this method may block, so don't
 * call it from the UI thread) The result can then be obtained with getResult()
 * TODO: move DefaultHandler methods to private subclass, they don't need to be
 * exposed.
 */
public class ECOQuery extends DefaultHandler {
	static final String URL_BASE = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";

	String id;
	PubMedResult result;

	/**
	 * Prepares a new pubmed query for the given pmid, e.g. "17588266".
	 */
	public ECOQuery(String id) {
		this.id = id;
	}

	/**
	 * Execute a query. Don't call this from the UI thread, because this method
	 * blocks.
	 */
	public void execute() throws IOException, SAXException {
		// TODO: assert not being in UI thread
		String urlString = URL_BASE;
		urlString += "?db=pubmed&id=" + id;

		URL url = new URL(urlString);
		InputStream is = url.openStream();

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setEntityResolver(this);

		result = new PubMedResult();
		result.setId(id);
		xmlReader.parse(new InputSource(is));

		is.close();
	}

	/**
	 * get the result, after execute() has finished.
	 */
	public PubMedResult getResult() {
		return result;
	}

	String parsingId;
	String parsingName;
	String parsingElement;
	String parsingValue;

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		System.out.println("New element: " + localName + ", " + attributes.getValue(NAME));
		parsingElement = localName;
		parsingName = attributes.getValue(NAME);
		parsingValue = "";
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		parsingValue += new String(ch, start, length).trim();
//		System.out.println("characters: " + new String(ch, start, length).trim());
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
//		System.out.println("End element: " + localName);
		if (parsingElement == ID) {
			parsingId = parsingValue;
		}
		if (TITLE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing title: " + value);
			result.setTitle(parsingValue);
		} else if (PUBDATE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing pubdate: " + value);
			if (parsingValue.length() >= 4)
				parsingValue = parsingValue.substring(0, 4);
			result.setYear(parsingValue);
		} else if (SOURCE.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsing source: " + value);
			result.setSource(parsingValue);
		} else if (AUTHOR.equalsIgnoreCase(parsingName)) {
//			System.out.println("Parsed author: " + parsingValue);
			result.addAuthor(parsingValue);
		}
		parsingElement = "";
		parsingName = "";
	}

	static final String ITEM = "Item";
	static final String ID = "Id";
	static final String NAME = "Name";
	static final String TITLE = "Title";
	static final String PUBDATE = "PubDate";
	static final String SOURCE = "Source";
	static final String AUTHOR_LIST = "AuthorList";
	static final String AUTHOR = "Author";

}
