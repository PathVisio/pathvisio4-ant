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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.prop.StaticProperty;

/**
 * Abstract class of pathway elements which are part of a pathway and have an
 * elementId.
 * 
 * Children: DataNode, State, Interaction, GraphicalLine, Label, Shape, Group,
 * Anchor, Point, Annotation, Citation, and Evidence.
 * 
 * @author finterly
 */
public abstract class PathwayObject {

	/* parent pathway model: may be null (e.g. when object is in clipboard) */
	protected PathwayModel pathwayModel; // TODO protected?
	private String elementId;

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * Instantiates a pathway element. Parent pathway model and elementId are set
	 * through {@link PathwayModel} add pathway element methods. elementId.
	 */
	public PathwayObject() {
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the object type of the pathway element.
	 * 
	 * @return objectType the object type.
	 */
	abstract public ObjectType getObjectType();

	/**
	 * Returns the pathway model for this pathway element.
	 * 
	 * @return pathwayModel the parent pathway model.
	 */
	public PathwayModel getPathwayModel() {
		return pathwayModel;
	}

	/**
	 * Checks whether this pathway element belongs to a pathway model.
	 *
	 * @return true if and only if the pathwayNodel of this pathway element is
	 *         effective.
	 */
	public boolean hasPathwayModel() {
		return getPathwayModel() != null;
	}

	/**
	 * Sets the pathway model for this pathway element. NB: Only set when a pathway
	 * model adds this pathway element.
	 * 
	 * NB: This method is not used directly. It is called by
	 * {@link PathwayModel#addPathwayObject}.
	 * 
	 * @param pathwayModel the parent pathway model.
	 */
	protected void setPathwayModelTo(PathwayModel pathwayModel) throws IllegalArgumentException, IllegalStateException {
		if (pathwayModel == null)
			throw new IllegalArgumentException("Invalid pathway model.");
		if (hasPathwayModel())
			throw new IllegalStateException("Pathway element already belongs to a pathway model.");
		setPathwayModel(pathwayModel);
	}

	/**
	 * Sets the pathway model of this pathway element to the given pathway model.
	 * 
	 * NB: This method is not used directly. It is called by
	 * {@link #setPathwayModelTo}.
	 * 
	 * @param pathwayModel the new pathway model for this pathway element.
	 */
	protected void setPathwayModel(PathwayModel pathwayModel) {
		this.pathwayModel = pathwayModel;
	}

	/**
	 * Unsets the pathway model, if any, from this pathway element. The pathway
	 * element no longer belongs to a pathway model.
	 * 
	 * NB: This method is not used directly. It is called by {@link #terminate}.
	 */
	protected void unsetPathwayModel() {
		if (hasPathwayModel()) {
			setPathwayModel(null);
		}
	}

	/**
	 * Returns the elementId of the pathway element.
	 * 
	 * @return elementId the unique pathway element identifier.
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * Sets the elementId of the pathway element.
	 * 
	 * NB: This method is not used directly.
	 * 
	 * @param v the unique pathway element identifier.
	 */
	protected void setElementId(String v) { // TODO
		if (v != null) {
			if (pathwayModel != null && pathwayModel.getElementIds().contains(v)) {
				throw new IllegalArgumentException("elementId '" + v + "' is not unique");
			}
			elementId = v;
			fireObjectModifiedEvent(
					PathwayObjectEvent.createSinglePropertyEvent(PathwayObject.this, StaticProperty.ELEMENTID));
		}
	}

	/**
	 * Sets the elementId to generated elementId from pathwayModel.
	 * 
	 * NB: This method does not automatically add this elementId to the pathway
	 * model.
	 */
	protected String setGeneratedElementId() {
		setElementId(pathwayModel.getUniqueElementId());
		return elementId;
	}

	/**
	 * Terminates this pathway element. The pathway model, if any, is unset from
	 * this pathway element. The elementId of this pathway element is changed.
	 */
	protected void terminate() {
		unsetPathwayModel();
		// We do not use the method setElementId, because it does not accept null
		this.elementId = null;
	}

	// ================================================================================
	// FireEvent and Listener Methods
	// ================================================================================
	int noFire = 0;

	/**
	 * @param times
	 */
	public void dontFireEvents(int times) {
		noFire = times;
	}

	private Set<PathwayObjectListener> listeners = new HashSet<PathwayObjectListener>();

	/**
	 * @param v
	 */
	public void addListener(PathwayObjectListener v) {
		if (!listeners.contains(v))
			listeners.add(v);
	}

	/**
	 * @param v
	 */
	public void removeListener(PathwayObjectListener v) {
		listeners.remove(v);
	}

	/**
	 * @param e
	 */
	public void fireObjectModifiedEvent(PathwayObjectEvent e) {
		if (noFire > 0) {
			noFire -= 1;
			return;
		}
		if (pathwayModel != null)
			pathwayModel.childModified(e);
		for (PathwayObjectListener g : listeners) {
			g.gmmlObjectModified(e);
		}
	}

	// ================================================================================
	// Copy Methods
	// ================================================================================
//	/**
//	 * Note: doesn't change parent, only fields TODO 
//	 *
//	 * Used by UndoAction.
//	 *
//	 * @param src the source pathway object 
//	 */
//	public abstract void copyValuesFrom(PathwayObject src);

//	/**
//	 * Copy Object. The object will not be part of the same Pathway object, it's
//	 * parent will be set to null.
//	 *
//	 * No events will be sent to the parent of the original.
//	 */
//	public abstract PathwayObject copy();

	// ================================================================================
	// Property Methods
	// ================================================================================
	/**
	 * 
	 */
	private static final Map<ObjectType, Set<StaticProperty>> ALLOWED_PROPS;

	static {
		ALLOWED_PROPS = new EnumMap<ObjectType, Set<StaticProperty>>(ObjectType.class);

		// common properties
		Set<StaticProperty> propsPathwayObject = EnumSet.of(StaticProperty.ELEMENTID);

		Set<StaticProperty> propsPathwayElement = EnumSet.of(StaticProperty.COMMENT, StaticProperty.ANNOTATION,
				StaticProperty.CITATION, StaticProperty.EVIDENCE); // TODO dynamic property
		propsPathwayElement.addAll(propsPathwayObject);

		Set<StaticProperty> propsShapedElement = EnumSet.of(StaticProperty.GROUPREF, StaticProperty.CENTERX,
				StaticProperty.CENTERY, StaticProperty.WIDTH, StaticProperty.HEIGHT, StaticProperty.TEXTCOLOR,
				StaticProperty.FONTNAME, StaticProperty.FONTWEIGHT, StaticProperty.FONTSTYLE,
				StaticProperty.FONTDECORATION, StaticProperty.FONTSTRIKETHRU, StaticProperty.FONTSIZE,
				StaticProperty.HALIGN, StaticProperty.VALIGN, StaticProperty.BORDERCOLOR, StaticProperty.BORDERSTYLE,
				StaticProperty.BORDERWIDTH, StaticProperty.FILLCOLOR, StaticProperty.SHAPETYPE, StaticProperty.ZORDER,
				StaticProperty.ROTATION);
		propsShapedElement.addAll(propsPathwayElement);

		Set<StaticProperty> propsLineElement = EnumSet.of(StaticProperty.GROUPREF, StaticProperty.LINECOLOR,
				StaticProperty.LINESTYLE, StaticProperty.LINEWIDTH, StaticProperty.CONNECTORTYPE,
				StaticProperty.ZORDER);
		propsLineElement.addAll(propsPathwayElement);

		// TODO
		Set<StaticProperty> propsAuthor = EnumSet.of(StaticProperty.AUTHOR, StaticProperty.NAME,
				StaticProperty.USERNAME, StaticProperty.ORDER);

		// pathway
		Set<StaticProperty> propsPathway = EnumSet.of(StaticProperty.TITLE, StaticProperty.ORGANISM,
				StaticProperty.SOURCE, StaticProperty.VERSION, StaticProperty.LICENSE, StaticProperty.BOARDWIDTH,
				StaticProperty.BOARDHEIGHT, StaticProperty.BACKGROUNDCOLOR);
		propsPathway.addAll(propsAuthor); // TODO
		propsPathway.addAll(propsPathwayElement);
		ALLOWED_PROPS.put(ObjectType.PATHWAY, propsPathway);

		// datanode
		Set<StaticProperty> propsDataNode = EnumSet.of(StaticProperty.TEXTLABEL, StaticProperty.DATANODETYPE,
				StaticProperty.XREF, StaticProperty.ALIASREF);
		propsDataNode.addAll(propsShapedElement);
		ALLOWED_PROPS.put(ObjectType.DATANODE, propsDataNode);

		// state
		Set<StaticProperty> propsState = EnumSet.of(StaticProperty.TEXTLABEL, StaticProperty.STATETYPE,
				StaticProperty.RELX, StaticProperty.RELY, StaticProperty.XREF);
		propsState.addAll(propsShapedElement);
		ALLOWED_PROPS.put(ObjectType.STATE, propsState);

		// interaction
		Set<StaticProperty> propsInteraction = EnumSet.of(StaticProperty.XREF);
		propsInteraction.addAll(propsLineElement);
		ALLOWED_PROPS.put(ObjectType.INTERACTION, propsInteraction);

		// graphical line
		ALLOWED_PROPS.put(ObjectType.GRAPHLINE, propsLineElement);

		// label
		Set<StaticProperty> propsLabel = EnumSet.of(StaticProperty.TEXTLABEL, StaticProperty.HREF);
		propsLabel.addAll(propsShapedElement);
		ALLOWED_PROPS.put(ObjectType.LABEL, propsLabel);

		// shape
		Set<StaticProperty> propsShape = EnumSet.of(StaticProperty.TEXTLABEL);
		propsShape.addAll(propsShapedElement);
		ALLOWED_PROPS.put(ObjectType.SHAPE, propsShape);

		// group
		Set<StaticProperty> propsGroup = EnumSet.of(StaticProperty.GROUPTYPE, StaticProperty.XREF,
				StaticProperty.TEXTLABEL);
		propsGroup.addAll(propsShapedElement);
		ALLOWED_PROPS.put(ObjectType.GROUP, propsGroup);
	};

	/**
	 * Returns keys of available static properties and dynamic properties as an
	 * object list
	 */
	public Set<Object> getPropertyKeys() {
		Set<Object> keys = new HashSet<Object>();
		keys.addAll(getStaticPropertyKeys());
		return keys;
	}

	/**
	 * get all attributes that are stored as static members.
	 */
	public Set<StaticProperty> getStaticPropertyKeys() {
		return ALLOWED_PROPS.get(getObjectType());
	}

	public Object getPropertyEx(Object key) {
		if (key instanceof StaticProperty) {
			return getStaticProperty((StaticProperty) key);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Set dynamic or static properties at the same time Will be replaced with
	 * setProperty in the future.
	 */
	public void setPropertyEx(Object key, Object value) {
		if (key instanceof StaticProperty) {
			setStaticProperty((StaticProperty) key, value);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public Object getStaticProperty(StaticProperty key) {
		if (!getStaticPropertyKeys().contains(key))
			throw new IllegalArgumentException(
					"Property " + key.name() + " is not allowed for objects of type " + getObjectType());
		Object result = null;
		switch (key) {
		case ELEMENTID:
			result = getElementId();// TODO
			break;
		default:
			// do nothing
		}
		return result;
	}

	/**
	 * This works so that o.setNotes(x) is the equivalent of o.setProperty("Notes",
	 * x);
	 *
	 * Value may be null in some cases, e.g. graphRef
	 *
	 * @param key
	 * @param value
	 */
	public void setStaticProperty(StaticProperty key, Object value) {
		if (!getStaticPropertyKeys().contains(key))
			throw new IllegalArgumentException(
					"Property " + key.name() + " is not allowed for objects of type " + getObjectType());
		switch (key) {
		case ELEMENTID:
//			setElementId((String) value); TODO not to be used? 
			break;
		default:
			// do nothing
		}
	}
}