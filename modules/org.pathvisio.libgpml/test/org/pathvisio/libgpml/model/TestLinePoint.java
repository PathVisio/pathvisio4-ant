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
package org.pathvisio.libgpml.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pathvisio.libgpml.model.LineElement.LinePoint;

import junit.framework.TestCase;

/**
 * Test for {@link LinePoint}.
 * 
 * @author finterly
 */
public class TestLinePoint extends TestCase {

	private PathwayModel p;
	private Interaction i1;
	private LinePoint pt3;
	private LinePoint pt4;

	/**
	 * Creates and adds anchor to line, line to pathwayModel.
	 */
	@Before
	public void setUp() {
		p = new PathwayModel();
		i1 = new Interaction();
		LinePoint pt1 = i1.getLinePoints().get(0);
		LinePoint pt2 = i1.getLinePoints().get(1);

		// checks
		assertNull(i1.getPathwayModel());
		assertNull(pt1.getPathwayModel());
		assertNull(pt2.getPathwayModel());
		p.addInteraction(i1);

		// checks
		assertTrue(p.hasPathwayObject(i1));
		assertTrue(p.getInteractions().contains(i1));
		assertEquals(i1.getPathwayModel(), p);
		assertTrue(p.hasPathwayObject(pt1));
		assertTrue(p.hasPathwayObject(pt2));
		assertEquals(pt1.getPathwayModel(), p);
		assertEquals(pt2.getPathwayModel(), p);

		// set new points
		List<LinePoint> points = new ArrayList<LinePoint>();
		pt3 = i1.new LinePoint(10, 18);
		pt4 = i1.new LinePoint(20, 18);
		points.add(pt3);
		points.add(pt4);
		i1.setLinePoints(points);

		// checks
		assertTrue(p.hasPathwayObject(i1));
		assertTrue(p.getInteractions().contains(i1));
		assertEquals(i1.getPathwayModel(), p);
		assertFalse(p.hasPathwayObject(pt1));
		assertFalse(p.hasPathwayObject(pt2));
		assertNull(pt1.getPathwayModel());
		assertNull(pt2.getPathwayModel());
		assertTrue(p.hasPathwayObject(pt3));
		assertTrue(p.hasPathwayObject(pt4));
		assertEquals(pt3.getPathwayModel(), p);
		assertEquals(pt4.getPathwayModel(), p);
	}

	/**
	 * Tests removing line and thus anchor.
	 */
	@Test
	public void testRemoveLine() {
		p.removeInteraction(i1);
		assertFalse(p.hasPathwayObject(i1));
		assertFalse(p.hasPathwayObject(pt3));
		assertFalse(p.hasPathwayObject(pt4));
	}

	/**
	 * Tests calling setLinePoints() before adding line to pathway model.
	 */
	@Test
	public void testSetLinePoints() {
		PathwayModel p = new PathwayModel();
		GraphicalLine i1 = new GraphicalLine();
		LinePoint pt1 = i1.getLinePoints().get(0);
		LinePoint pt2 = i1.getLinePoints().get(1);

		// checks
		assertNull(i1.getPathwayModel());
		assertNull(pt1.getPathwayModel());
		assertNull(pt2.getPathwayModel());

		// checks
		assertFalse(p.hasPathwayObject(i1));
		assertFalse(p.getGraphicalLines().contains(i1));
		assertNull(i1.getPathwayModel());
		assertFalse(p.hasPathwayObject(pt1));
		assertFalse(p.hasPathwayObject(pt2));

		// set new points
		List<LinePoint> points = new ArrayList<LinePoint>();
		pt3 = i1.new LinePoint(10, 18);
		pt4 = i1.new LinePoint(20, 18);
		points.add(pt3);
		points.add(pt4);
		i1.setLinePoints(points);

		p.addGraphicalLine(i1);

		// checks
		assertTrue(p.hasPathwayObject(i1));
		assertTrue(p.getGraphicalLines().contains(i1));
		assertEquals(i1.getPathwayModel(), p);
		assertFalse(p.hasPathwayObject(pt1));
		assertFalse(p.hasPathwayObject(pt2));
		assertNull(pt1.getPathwayModel());
		assertNull(pt2.getPathwayModel());
		assertTrue(p.hasPathwayObject(pt3));
		assertTrue(p.hasPathwayObject(pt4));
		assertEquals(pt3.getPathwayModel(), p);
		assertEquals(pt4.getPathwayModel(), p);
	}
}