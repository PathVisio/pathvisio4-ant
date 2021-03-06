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

import org.pathvisio.libgpml.prop.Property;

/**
 * This event is used to notify {@link PathwayObjectListener}s of changes to
 * properties of a PathwayElement.
 * <p>
 * There are three variations on this event:
 * <ol>
 * <li>Only a single, known property may have changed, that is not a coordinate
 * change.
 * <li>Only one of the coordinate properties (x, y, width, height) may have
 * changed.
 * <li>Any property may have changed.
 * </ol>
 *
 * Variation 2 is introduced for performance reasons. Coordinate changes
 * generate a lot of events (e.g. resizing or dragging an object) and typically
 * change in groups (if MLeft changes, MCenterX also changes). Listeners that
 * are interested in coordinate changes, may filter out changes to these
 * properties by using the {@link #isCoordinateChange()} property. Listeners
 * that are not interested in coordinate changes may use the
 * {@link #affectsProperty(Property)} method to find out if a property of
 * interest may have changed.
 *
 * @author Mark Woon
 */
public final class PathwayObjectEvent {
	private final PathwayObject pwElement;
	private final Object property;
	private final boolean coordinateChange;

	/**
	 * Creates a single property event.
	 * 
	 * @param pathwayObject the pathway object.
	 * @param property      the property.
	 * @return the pathway object event.
	 */
	protected static PathwayObjectEvent createSinglePropertyEvent(PathwayObject pathwayObject, Object property) {
		return new PathwayObjectEvent(pathwayObject, property, false);
	}

	/**
	 * Creates an all properties event.
	 * 
	 * @param pathwayObject the pathway object.
	 * @return the pathway object event.
	 */
	protected static PathwayObjectEvent createAllPropertiesEvent(PathwayObject pathwayObject) {
		return new PathwayObjectEvent(pathwayObject, null, false);
	}

	/**
	 * Creates a coordinate property event.
	 * 
	 * @param pathwayObject the pathway object.
	 * @return the pathway object event.
	 */
	protected static PathwayObjectEvent createCoordinatePropertyEvent(PathwayObject pathwayObject) {
		return new PathwayObjectEvent(pathwayObject, null, true);
	}

	/**
	 * Constructor.
	 *
	 * @param elem             the PathwayElement that's been modified
	 * @param prop             the Property on the element that's been modified
	 * @param coordinateChange Flag to indicate this event applies to a coordinate
	 *                         change.
	 */
	private PathwayObjectEvent(PathwayObject elem, Object prop, boolean coordinateChange) {
		pwElement = elem;
		property = prop;
		this.coordinateChange = coordinateChange;
	}

	/**
	 * Returns true if this event was caused by a coordinate change (e.g. movement
	 * or resize operation).
	 */
	public boolean isCoordinateChange() {
		return coordinateChange;
	}

	/**
	 * Gets the PathwayElement whose properties have been modified.
	 */
	public PathwayObject getModifiedPathwayObject() {
		return pwElement;
	}

	/**
	 * Check if the given static property may have been modified in this event. Note
	 * that this method does not apply to coordinate properties (position, size),
	 * these need to be checked with {@link #isCoordinateChange()}.
	 * 
	 * @param prop The property to check.
	 * @return true if the property may have been modified, false if not.
	 */
	public boolean affectsProperty(Property prop) {
		return property == null || property.equals(prop);
	}

	/**
	 * Checks if the given dynamic property may have been modified in this event.
	 * Note that this method does not apply to coordinate properties (position,
	 * size), these need to be checked with {@link #isCoordinateChange()}.
	 * 
	 * @param prop The property to check.
	 * @return true if the property may have been modified, false if not.
	 */
	public boolean affectsProperty(String prop) {
		return property == null || property.equals(prop);
	}
}
