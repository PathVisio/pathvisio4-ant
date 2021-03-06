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
package org.pathvisio.core.view.model;

import java.awt.geom.Point2D;

import junit.framework.TestCase;

import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.ShapedElement;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.LayoutType;

/**
 * Test various operations related to groups, such as adding to / removing from
 * them, selecting them, etc.
 */
public class TestGroups extends TestCase {
	static final int DATANODE_COUNT = 10;

	public void setUp() {
		PreferenceManager.init();
		vpwy = new VPathwayModel(null);
		pwy = new PathwayModel();
		vpwy.fromModel(pwy);

		for (int i = 0; i < DATANODE_COUNT; ++i) {
			dn[i] = new DataNode("", DataNodeType.UNDEFINED);
			dn[i].setCenterX(i * 1000);
			dn[i].setCenterY(3000);
			dn[i].setWidth(500);
			dn[i].setHeight(500);
			vDn[i] = (VDataNode) addElement(vpwy, dn[i]);
//			dn[i].setGeneratedElementId(); // TODO
		}
		vLn[0] = (VLineElement) addConnector(vpwy, dn[0], dn[1]);
		vLn[1] = (VLineElement) addConnector(vpwy, dn[0], dn[2]);

		vpwy.clearSelection();
		assertNull(dn[0].getGroupRef());
		assertNull(dn[1].getGroupRef());
		vDn[0].select();
		vDn[1].select();
		vLn[0].select();
		// create a group
		vpwy.toggleGroup(vpwy.getSelectedGraphics());
		Group grp1 = dn[0].getGroupRef();
		assertNotNull(grp1);
		assertEquals(grp1, dn[1].getGroupRef());
		vGrp1 = (VGroup) vpwy.getPathwayElementView(grp1);
//		grp1.setGeneratedElementId(); TODO 
	}

	private VPathwayModel vpwy;
	private PathwayModel pwy;

	private VDataNode[] vDn = new VDataNode[DATANODE_COUNT];
	private VLineElement[] vLn = new VLineElement[2];
	private DataNode[] dn = new DataNode[DATANODE_COUNT];
	private Group grp1 = null;
	private VGroup vGrp1 = null;

	/** helper for adding elements to a vpathway */
	private VElement addElement(VPathwayModel vpwy, PathwayElement pelt) {
		vpwy.getPathwayModel().add(pelt);

		VPathwayObject result = vpwy.getPathwayElementView(pelt);
		assertNotNull("PathwayElement not found through view after adding it to the model.", result);
		return result;
	}

	/** helper for adding connectors to a vpathway */
	private VElement addConnector(VPathwayModel vpwy, ShapedElement l1, ShapedElement l2) {
		Interaction elt = new Interaction();
		elt.setConnectorType(ConnectorType.ELBOW);
		elt.setStartElementRef(l1);
		elt.setEndElementRef(l2);
		elt.setStartLinePointX(l1.getCenterX());
		elt.setStartLinePointY(l1.getCenterY());
		elt.setEndLinePointX(l2.getCenterX());
		elt.setEndLinePointY(l2.getCenterY());

		return addElement(vpwy, elt);
	}

	public void testNesting() {
		vpwy.clearSelection();
		vGrp1.select();
		vDn[2].select();
		// create a 2nd, nested group
		vpwy.toggleGroup(vpwy.getSelectedGraphics());

		Group grp2 = dn[2].getGroupRef();
		assertNotNull(grp2);
		assertEquals(grp2, ((ShapedElement) grp1).getGroupRef());
		VGroup vGrp2 = (VGroup) vpwy.getPathwayElementView(grp2);
	}

	public void testDrag() {
		vpwy.clearSelection();
		double startX = vDn[0].getVCenterX();
		vGrp1.select();
		vpwy.selection.vMoveBy(50, 50);
		double endX = vDn[0].getVCenterX();
		// TODO: make this test work
//		assertEquals (startX, endX - 50, 0.1);
	}

	public void testSelect() {
		// TODO: make this test work
		Point2D p1 = new Point2D.Double(vDn[0].getVCenterX() - 10, vDn[0].getVCenterY() - 10);
		Point2D p2 = new Point2D.Double(vDn[0].getVCenterX(), vDn[0].getVCenterY());
		vpwy.startSelecting(p1);
		vpwy.doClickSelect(p2, false);
//		assertTrue (vDn[0].isSelected());
//		assertTrue (vGrp1.isSelected());
		vpwy.startSelecting(p1);
		vpwy.doClickSelect(p2, false);
//		assertFalse (vGrp1.isSelected());
//		assertTrue (vDn[0].isSelected());
		vpwy.startSelecting(p1);
		vpwy.doClickSelect(p2, false);
//		assertTrue (vGrp1.isSelected());
	}

	/**
	 * Test aligning something that is in a group
	 */
	public void testAlign() {
		vDn[0].select();
		vDn[1].select();
		vpwy.layoutSelected(LayoutType.ALIGN_LEFT);
	}

	/**
	 * Test removal of a group
	 */
	public void testDelete() {
		VLineElement vLn3 = (VLineElement) addConnector(vpwy, dn[0], grp1);

		double oldEx = vLn3.getVEndX();
		double oldEy = vLn3.getVEndY();
		assertEquals(vGrp1.getVCenterX(), oldEx, 0.01);
		assertEquals(vGrp1.getVCenterY(), oldEy, 0.01);
		assertEquals(grp1.getElementId(), vLn3.getPathwayObject().getEndElementRef());
		assertNotNull(vLn3.getPathwayObject().getEndElementRef());

		vpwy.clearSelection();
		vGrp1.select();
		vpwy.toggleGroup(vpwy.getSelectedGraphics());
		assertNull(dn[0].getGroupRef());

		// assure that line hasn't moved by deletion of group (bug #1058)
		assertEquals(oldEx, vLn3.getVEndX(), 0.01);
		assertEquals(oldEy, vLn3.getVEndY(), 0.01);
		assertNull(vLn3.getPathwayObject().getEndElementRef());

	}

}
