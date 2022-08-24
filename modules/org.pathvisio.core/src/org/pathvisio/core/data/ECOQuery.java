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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class can handle a query for a ECO record. Just instantiate this class
 * with a given ECO id (ECO), and run execute() (this method may block, so don't
 * call it from the UI thread) The result can then be obtained with getResult()
 * TODO: move DefaultHandler methods to private subclass, they don't need to be
 * exposed.
 * 
 * @author Egon Willighagen, finterly
 */
public class ECOQuery extends DefaultHandler {

	String id;
	ECOResult result;

	/**
	 * Prepares a new ECO query for the given id, e.g. "[ECO_0000253]".
	 */
	public ECOQuery(String id) {
		this.id = id;
	}

	/**
	 * Execute a query. Don't call this from the UI thread, because this method
	 * blocks.
	 */
	public void execute() throws IOException, SAXException, URISyntaxException {
		HashMap<String, String> termToId = new HashMap<String, String>();
		ClassLoader cl = ECOQuery.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("/data/eco.csv");
		String line = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if (is != null) {
				while ((line = reader.readLine()) != null) {
					String[] row = line.split(",");
					termToId.put(row[1], row[0]);
				}
			}
			reader.close();
		} finally {
			try {
				is.close();
			} catch (Throwable ignore) {
			}
		}
		//remove all non-numeric char from id 
		id = id.replaceAll("\\D", "");
		String term = termToId.get("[ECO_" + id + "]"); // TODO format from eco.csv file
		if (term != null) { // was found, otherwise result is null 
			result = new ECOResult();
			result.setId(id);
			result.setTerm(term);
		}
	}

	/**
	 * get the result, after execute() has finished.
	 */
	public ECOResult getResult() {
		return result;
	}

}
