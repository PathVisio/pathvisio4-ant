package org.pathvisio.libgpml.util;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;

import junit.framework.TestCase;

/**
 * Tests for XrefUtils class.
 * 
 * @author finterly
 */
public class TestXref extends TestCase {

	public void testXref() {

		DataSourceTxt.init();

		DataSource ds = null; 
		
		Xref xref = new Xref("1", ds);
	
		System.out.println(xref);
	}

}
