/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2022 BiGCaT Bioinformatics, WikiPathways
 *           2022 Egon Willighagen
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

import java.io.FileNotFoundException;

/**
 * This class can handle a query for a DOI record. Just instantiate this class
 * with a given DOI id (doi), and run execute() (this method may block, so don't
 * call it from the UI thread) The result can then be obtained with getResult()
 * TODO: move DefaultHandler methods to private subclass, they don't need to be
 * exposed.
 * 
 * @author Egon Willighagen
 */
public class DOIQuery extends JSONCSLQuery {

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
	public void execute() throws Exception, FileNotFoundException {
		String url = "https://doi.org/" + this.id;
		this.result = super.extractDOIResult(url, id);
	}

	/**
	 * get the result, after execute() has finished.
	 */
	public DOIResult getResult() {
		return result;
	}

}
