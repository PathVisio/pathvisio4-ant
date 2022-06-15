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
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class can handle a query for a pubmed record.
 * Just instantiate this class with a given pubmed id (pmid),
 * and run execute() (this method may block, so don't call it from the UI thread)
 * The result can then be obtained with getResult()
 * TODO: move DefaultHandler methods to private subclass, they don't need to be exposed.
 */
public class DOIQuery2 extends DefaultHandler {
	static final String URL_BASE = "https://api.crossref.org/works/";

	String id;
	DOIResult2 result;

	/**
	 * Prepares a new pubmed query for the given pmid, e.g. "17588266".
	 */
	public DOIQuery2(String id) {
		this.id = id;
	}

	/**
	 * Execute a query. Don't call this from the UI thread, because
	 * this method blocks.
	 * 
	 * @return true if valid DOI identifier (url), false otherwise
	 * @throws FileNotFoundException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void execute() throws FileNotFoundException, MalformedURLException, IOException {
		//TODO: assert not being in UI thread
		String urlString = URL_BASE;
		urlString += id + "/agency";
		System.out.println("Here1 ");

		URL url = new URL(urlString);
//		InputStream is = url.openStream();

		String inputLine = null;
		try {       
            URLConnection conn = url.openConnection();
            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));
            br.close();
            result = new DOIResult2();
    		result.setId(id);
			JOptionPane.showConfirmDialog(null, "DOI found for identifier.", "Message", JOptionPane.PLAIN_MESSAGE);
		} catch (FileNotFoundException e) {  // Could not find DOI id file
			JOptionPane.showConfirmDialog(null, "DOI not found for identifier.", "Warning", JOptionPane.PLAIN_MESSAGE);
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
	public DOIResult2 getResult() {
		return result;
	}

}
