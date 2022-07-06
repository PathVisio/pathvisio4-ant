/* Copyright 2022 Egon Willighagen
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
 */
package org.pathvisio.core.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

/*
 * Helper class to get bibliographic services that export Citation Styling Language (CSL)
 * data format in JSON format.
 */
public class JSONCSLQuery {

	/**
	 * Helper helper method to download the content of a online webpage as a string.
	 */
	private String download(String url, String mimeType) throws Exception {
		StringBuffer content = new StringBuffer();
		URLConnection rawConn = new URL(url).openConnection();
		rawConn.addRequestProperty("User-Agent", "PathVisio (https://pathvisio.org/)");
		rawConn.addRequestProperty("Accept", mimeType);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(rawConn.getInputStream())
				);
		String line = reader.readLine();
		while (line != null) {
			content.append(line).append('\n');
			line = reader.readLine();
		}
		return content.toString();
	}

	/**
	 * Extracts a {@link DOIResult} object from the JSON CSL reply from the service.
	 */
	protected DOIResult extractDOIResult(String url, String id) throws Exception {
		String jsonContent = download(url, "application/vnd.citationstyles.csl+json");
	    JSONObject json = new JSONObject(jsonContent);
	    DOIResult result = new DOIResult();
		result.setId(id);
		result.setTitle(json.getString("title"));
		result.setSource(json.getString("container-title"));
		result.setYear("" + json.getJSONObject("published").getJSONArray("date-parts").getJSONArray(0).getInt(0));
		return result;
	}

	/**
	 * Extracts a {@link PubMedResult} object from the JSON CSL reply from the service.
	 */
	protected PubMedResult extractPubMedResult(String url, String id) throws Exception {
		String jsonContent = download(url, "application/vnd.citationstyles.csl+json");
		System.out.println(jsonContent);
	    JSONObject json = new JSONObject(jsonContent);
	    PubMedResult result = new PubMedResult();
		result.setId(id);
		result.setTitle(json.getString("title"));
		result.setSource(json.getString("container-title"));
		result.setYear("" + json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(0));
		return result;
	}

}
