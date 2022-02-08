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

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.libgpml.model.type.ObjectType;

/**
 * Group specific implementation of methods that calculate derived
 * coordinates that are not stored in GPML directly
 * @author thomas
 */
public class Group extends PathwayElement {
	protected Group() {
		super(ObjectType.GROUP);
	}

	/**
	 * Center x of the group bounds
	 */
	public double getCenterX() {
		return getBounds().getCenterX();
	}

	/**
	 * Center y of the group bounds
	 */
	public double getCenterY() {
		return getBounds().getCenterY();
	}

	/**
	 * Height of the group bounds
	 */
	public double getHeight() {
		return getBounds().getHeight();
	}

	/**
	 * Left of the group bounds
	 */
	public double getLeft() {
		return getBounds().getX();
	}

	/**
	 * Top of the group bounds
	 */
	public double getTop() {
		return getBounds().getY();
	}

	/**
	 * Width of the group bounds
	 */
	public double getWidth() {
		return getBounds().getWidth();
	}

	public void setCenterX(double v) {
		double d = v - getBounds().getCenterX();
		for(PathwayElement e : getGroupElements()) {
			e.setCenterX(e.getCenterX() + d);
		}
	}

	public void setCenterY(double v) {
		double d = v - getBounds().getCenterY();
		for(PathwayElement e : getGroupElements()) {
			e.setCenterY(e.getCenterY() + d);
		}
	}

	public void setHeight(double v) {
		double d = v - getBounds().getHeight();
		for(PathwayElement e : getGroupElements()) {
			e.setHeight(e.getHeight() + d);
		}
	}

	public void setWidth(double v) {
		double d = v - getBounds().getWidth();
		for(PathwayElement e : getGroupElements()) {
			e.setWidth(e.getWidth() + d);
		}
	}

	public void setLeft(double v) {
		double d = v - getBounds().getX();
		for(PathwayElement e : getGroupElements()) {
			e.setLeft(e.getLeft() + d);
		}
	}

	public void setTop(double v) {
		double d = v - getBounds().getY();
		for(PathwayElement e : getGroupElements()) {
			e.setTop(e.getTop() + d);
		}
	}

	/**
	 * Iterates over all group elements to find
	 * the total rectangular bounds.
	 * Note: doesn't include rotation of the nested elements.
	 * If you want to include rotation, use {@link #getRotatedBounds()} instead.
	 */
	public Rectangle2D getBounds() {
		Rectangle2D bounds = null;
		for(PathwayElement e : getGroupElements()) {
			if(e == this) continue; //To prevent recursion error
			if(bounds == null) bounds = e.getBounds();
			else bounds.add(e.getBounds());
		}
		if(bounds != null) {
			double margin = getGroupType().getMMargin();
			return new Rectangle2D.Double(
				bounds.getX() - margin,
				bounds.getY() - margin,
				bounds.getWidth() + 2*margin,
				bounds.getHeight() + 2*margin
			);
		} else {
			return new Rectangle2D.Double();
		}
	}

	/**
	 * Iterates over all group elements to find
	 * the total rectangular bounds, taking into
	 * account rotation of the nested elements
	 */
	public Rectangle2D getRotatedBounds() {
		Rectangle2D bounds = null;
		for(PathwayElement e : getGroupElements()) {
			if(e == this) continue; //To prevent recursion error
			if(bounds == null) bounds = e.getRotatedBounds();
			else bounds.add(e.getRotatedBounds());
		}
		if(bounds != null) {
			double margin = groupType.getMMargin();
			return new Rectangle2D.Double(
				bounds.getX() - margin,
				bounds.getY() - margin,
				bounds.getWidth() + 2 * margin,
				bounds.getHeight() + 2 * margin
			);
		} else {
			return new Rectangle2D.Double();
		}
	}

	/**
	 * Get the group elements. Convenience method that
	 * checks for a valid parent and never returns
	 * null
	 */
	public Set<PathwayElement> getGroupElements() {
		Set<PathwayElement> result = new HashSet<PathwayElement>();
		PathwayModel parent = getParent();
		if(parent != null) {
			result = parent.getGroupElements(getGroupId());
		}
		return result;
	}
}
