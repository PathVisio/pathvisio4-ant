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
package org.pathvisio.gui.panels;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.pathvisio.core.util.Resources;

import junit.framework.TestCase;

/**
 * Test Xref toString.
 */
public class Test extends TestCase {

	public void test() {
		
		DataSourceTxt.init();
		Xref xref = new Xref("2", DataSource.getExistingByFullName("PubMed"));	
		System.out.println(xref.getDataSource().getMainUrl());
		System.out.println(xref.getKnownUrl());

	
	}
}
