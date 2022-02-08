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
package org.pathvisio.core.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.libgpml.biopax.BiopaxReferenceManager;
import org.pathvisio.libgpml.biopax.PublicationXref;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.type.ObjectType;

public class Test extends TestCase {

	PathwayModel pwy = null;
	VPathwayModel vPwy = null;
	PathwayElement eltDn = null, eltSh = null, eltLi = null, eltLa = null;
	Graphics vDn = null, vSh = null, vLi = null, vLa = null;

	public void setUp()
	{
		PreferenceManager.init();
    	pwy = new PathwayModel();
    	eltDn = PathwayElement.createPathwayElement(ObjectType.DATANODE);
    	eltDn.setCenterX(3000);
    	eltDn.setCenterY(3000);
    	eltDn.setIdentifier("1234");
    	eltDn.setTextLabel("Gene");
    	eltDn.setWidth(1000);
    	eltDn.setHeight(1000);
    	eltSh = PathwayElement.createPathwayElement(ObjectType.SHAPE);
    	eltSh.setCenterX(6000);
    	eltSh.setCenterY(3000);
    	eltSh.setWidth(300);
    	eltSh.setHeight(700);
    	eltLi = PathwayElement.createPathwayElement(ObjectType.LINE);
    	eltLi.setStartLinePointX(500);
    	eltLi.setStartLinePointY(1000);
    	eltLi.setEndLinePointX(2500);
    	eltLi.setEndLinePointY(4000);
    	eltLa = PathwayElement.createPathwayElement(ObjectType.LABEL);
    	eltLa.setCenterX(6000);
    	eltLa.setCenterY(6000);
    	eltLa.setWidth(300);
    	eltLa.setHeight(700);
    	eltLa.setTextLabel("Test");
    	pwy.add(eltDn);
    	pwy.add(eltSh);
    	pwy.add(eltLi);
    	pwy.add(eltLa);
    	vPwy = new VPathwayModel(null);
    	vPwy.fromModel(pwy);

    	for(VPathwayElement e : vPwy.getDrawingObjects())
    	{
    		if(e instanceof Graphics) {
    			PathwayElement pe = ((Graphics)e).getPathwayElement();
    			if			(pe == eltDn) {
    				vDn = (Graphics)e;
    			} else if 	(pe == eltSh) {
    				vSh = (Graphics)e;
    			} else if	(pe == eltLi) {
    				vLi = (Graphics)e;
    			} else if	(pe == eltLa) {
    				vLa = (Graphics)e;
    			}
    		}
    	}

    	assertFalse(vDn == null);
    	assertFalse(vSh == null);
    	assertFalse(vLi == null);
    	assertFalse(vLa == null);
	}

	public void testCopyPaste()
	{
    	PathwayModel pTarget = new PathwayModel();
    	VPathwayModel vpTarget = new VPathwayModel(null);
    	vpTarget.fromModel(pTarget);

		vPwy.selectObject(vDn);
		vPwy.copyToClipboard();

		vpTarget.pasteFromClipboard();

		PathwayElement pasted = null;
		for(PathwayElement e : pTarget.getDataObjects()) {
			if("1234".equals(e.getIdentifier())) {
				pasted = e;
			}
		}
		//TODO: does not work if VPathwayWrapper is not VPathwaySwing.
//		assertNotNull(pasted);

		//Now copy mappinfo
//		PathwayElement info = pSource.getMappInfo();
//		info.setMapInfoName("test pathway");
//		vpSource.selectObject(vpSource.getPathwayElementView(info));
//		vpSource.copyToClipboard();

//		vpTarget.pasteFromClipboard();

		//test if mappinfo has been pasted to the target pathway
//		assertTrue("test pathway".equals(pTarget.getMappInfo().getMapInfoName()));
    }

    public void testOrderAction()
    {
    	assertTrue(eltDn.getZOrder() > eltLa.getZOrder());
    	assertTrue(eltLa.getZOrder() > eltSh.getZOrder());
    	assertTrue(eltSh.getZOrder() > eltLi.getZOrder());

    	vPwy.moveGraphicsTop(Arrays.asList(new Graphics[] {vLa}));
    	vPwy.moveGraphicsBottom(Arrays.asList(new Graphics[] {vSh}));

    	assertTrue(eltLa.getZOrder() > eltDn.getZOrder());
    	assertTrue(eltDn.getZOrder() > eltLi.getZOrder());
    	assertTrue(eltLi.getZOrder() > eltSh.getZOrder());
    }

    /**
     * Test sorting of vpathway elements
     *
     * handles should be on top.
     * VPoints should be below handles
     * Any Graphics type should be below non-Graphics types
     */
    public void testVpwySort()
    {
    	assertTrue(eltDn.getZOrder() > eltSh.getZOrder());
    	assertTrue(eltSh.getZOrder() > eltLi.getZOrder());

    	vDn.select();
    	VPathwayElement h = ((VDataNode)vDn).getHandles()[0];
//    	VPoint pnt = ((Line)vLi).getEnd();

    	vPwy.addScheduled();
    	List<VPathwayElement> elements = vPwy.getDrawingObjects();

    	//Test natural / z order
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { vLi, vSh, vLa, vDn, h }, elements);

    	//order should not change when selecting
    	vLi.select();
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { vLi, vSh, vLa, vDn, h }, elements);

    	//Test reset after unselected
    	vLi.deselect();
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { vLi, vSh, vLa, vDn, h }, elements);
    }

    public void checkDrawingOrder(VPathwayElement[] order, List<VPathwayElement> elements) {
    	int[] indices = new int[order.length];
    	for(int i = 0; i < order.length; i++) {
    		indices[i] = elements.indexOf(order[i]);
    	}
    	for(int i = 0; i < indices.length  - 1; i++) {
    		assertTrue("Element " + i + "(" + indices[i] + ") should be below element " + (1+i) + "(" + indices[i+1] + ")",
    				indices[i] < indices[i+1]);
    	}
    }

    public void testDelete()
    {
    	assertTrue (vPwy.getDrawingObjects().contains(vSh));
    	assertTrue (pwy.getDataObjects().contains(eltSh));

    	vPwy.removeDrawingObject(vSh, true);

    	assertFalse (vPwy.getDrawingObjects().contains(vSh));
    	assertFalse (pwy.getDataObjects().contains(eltSh));

    	assertTrue (vPwy.getDrawingObjects().contains(vLa));
    	assertTrue (pwy.getDataObjects().contains(eltLa));
    	assertTrue (vPwy.getDrawingObjects().contains(vLi));
    	assertTrue (pwy.getDataObjects().contains(eltLi));

    	vPwy.removeDrawingObjects (Arrays.asList(new VPathwayElement[] { vLa, vLi } ), true);
    	
    	assertFalse (vPwy.getDrawingObjects().contains(vLa));
    	assertFalse (pwy.getDataObjects().contains(eltLa));
    	assertFalse (vPwy.getDrawingObjects().contains(vLi));
    	assertFalse (pwy.getDataObjects().contains(eltLi));

    	assertTrue (vPwy.getDrawingObjects().contains(vDn));
    	assertTrue (pwy.getDataObjects().contains(eltDn));

    	vPwy.removeDrawingObjects (Arrays.asList(new VPathwayElement[] { vDn } ));
    	
    	assertFalse (vPwy.getDrawingObjects().contains(vDn));
    	assertTrue (pwy.getDataObjects().contains(eltDn));
    }

    public void testUndoAction()
    {
    	//TODO
    }

    public void testGroupingAction()
    {
    	//TODO
    }

    public void testSelection()
    {
    	//TODO
    }

    public void testNewAction()
	{
    	//TODO
	}

    public void testNudgeAction()
    {
    	//TODO
    }

    public void testAddAnchorAction()
    {
    	//TODO
    }

    public void testConnector()
    {
    	//TODO
    }

    public void testLitRef()
    {
    	// test that addition of a reference in the model leads to the creation of a Citation object
    	// in the view.
    	// See also bug 855: http://www.bigcat.unimaas.nl/tracprojects/pathvisio/ticket/855

    	assertNull (vDn.getCitation());

		BiopaxReferenceManager m = eltDn.getBiopaxReferenceManager();
		PublicationXref cit = new PublicationXref();
		cit.setPubmedId("18651794"); // Just a dummy value, no query is sent
		m.addElementReference(cit);

    	assertNotNull (vDn.getCitation());
    	assertEquals (vDn.getCitation().getRefMgr().getPublicationXRefs().get(0).getPubmedId(),
    			"18651794");

    	// now remove it again
    	m.removeElementReference(cit);

    	assertNull (vDn.getCitation());
    }

}
