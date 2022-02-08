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

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Set;

import org.pathvisio.libgpml.util.Utils;

/**
 * This class only contains static methods and should not be instantiated.
 */
public abstract class GraphLink {

	/**
	 * All classes that have a graphId must implement this interface. Those are
	 * PathwayElement.MPoint (i.e. points) and PathwayElement (i.e. DataNodes,
	 * Shapes, etc). They are needed for being refered to.
	 *
	 * This interface exists so we can easily iterate through all objects containing
	 * a graphId.
	 */
	public interface LinkableTo {
		String getElementId();

		void setElementId(String id);

		/** generate a unique graph Id and use that. */
		String setGeneratedElementId();

		Set<LinkableFrom> getReferences();

		/**
		 * return the parent Gmmldata Object, needed for maintaining a consistent list
		 * of graphId's
		 */
		PathwayModel getPathwayModel();

		/**
		 * Convert a point to shape coordinates (relative to the bounds of the
		 * GraphIdContainer)
		 */
		Point2D toRelativeCoordinate(Point2D p);

		/**
		 * Convert a point to pathway coordinates (relative to the pathway)
		 */
		Point2D toAbsoluteCoordinate(Point2D p);
	}

	/**
	 * All classes that want to refer *to* a GraphIdContainer must implement this
	 * interface. At this time that only goes for PathwayElement.MPoint.
	 */
	public interface LinkableFrom {
		String getElementRef();

		void linkTo(LinkableTo idc, double relX, double relY);

		void unlink();

		double getRelX();

		double getRelY();

		/**
		 * return the parent Pathway object, needed for maintaining a consistent list of
		 * graphId's
		 */
		PathwayModel getPathwayModel();

		/**
		 * Called whenever the object being referred to changes coordinates.
		 */
		void refeeChanged();
	}

	/**
	 * Give an object that implements the graphId interface a graphId, thereby
	 * possibly linking it to new objects.
	 *
	 * This is a helper for classes that need to implement the GraphIdContainer
	 * interface, to avoid duplication.
	 *
	 * @param v  the graphId
	 * @param c  the object to is going to get the new graphId
	 * @param gd the pathway model, which is maintaining a complete list of all
	 *           graphId's in this pathway
	 */
	protected static void setGraphId(String v, LinkableTo c, PathwayModel data) {
		String graphId = c.getElementId();
		if (graphId == null || !graphId.equals(v)) {
			if (data != null) {
				if (graphId != null) {
					data.removeGraphId(graphId);
				}
				if (v != null) {
					data.addGraphId(v, c);
				}
			}
		}
	}

	/**
	 * Return a list of GraphRefContainers (i.e. points) referring to a certain
	 * GraphId.
	 *
	 * @param gid
	 * @param gd
	 * @return
	 */
	public static Set<LinkableFrom> getReferences(LinkableTo gid, PathwayModel gd) {
		if (gd == null || Utils.isEmpty(gid.getElementId()))
			return Collections.emptySet();
		else
			return gd.getReferringObjects(gid.getElementId());
	}
}
