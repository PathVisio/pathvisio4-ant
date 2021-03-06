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

import java.util.Set;

import org.bridgedb.Xref;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.prop.StaticProperty;

/**
 * This class stores information for an Interaction pathway element.
 *
 * @author finterly
 */
public class Interaction extends LineElement implements Xrefable {

	private Xref xref; // optional

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * Instantiates an Interaction pathway element given all possible parameters.
	 *
	 * @param xref the interaction Xref.
	 */
	public Interaction(Xref xref) {
		super();
		this.xref = xref;
	}

	/**
	 * Instantiates an Interaction pathway element given all required parameters
	 * except xref.
	 */
	public Interaction() {
		this(null);
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the object type of this pathway element.
	 *
	 * @return the object type.
	 */
	@Override
	public ObjectType getObjectType() {
		return ObjectType.INTERACTION;
	}

	/**
	 * Returns the Xref for this interaction.
	 *
	 * @return xref the xref of interaction.
	 */
	@Override
	public Xref getXref() {
		return xref;
	}

	/**
	 * Sets the Xref for this interaction.
	 *
	 * @param v the xref to set for this interaction.
	 */
	@Override
	public void setXref(Xref v) {
		if (v != null) {
			xref = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.XREF));
		}
	}

	// ================================================================================
	// Copy Methods
	// ================================================================================
	/**
	 * Copies values from the given source pathway element.
	 *
	 * @param src the source pathway element.
	 */
	public void copyValuesFrom(Interaction src) {
		super.copyValuesFrom(src);
		xref = src.xref;
		fireObjectModifiedEvent(PathwayObjectEvent.createAllPropertiesEvent(this));
	}

	/**
	 * Copies this pathway element.
	 *
	 * @return the copyElement for the new pathway element and this source pathway
	 *         element.
	 */
	@Override
	public CopyElement copy() {
		Interaction result = new Interaction();
		result.copyValuesFrom(this);
		return new CopyElement(result, this);
	}

	// ================================================================================
	// Property Methods
	// ================================================================================
	/**
	 * Returns all static properties for this pathway object.
	 *
	 * @return result the set of static property for this pathway object.
	 */
	@Override
	public Set<StaticProperty> getStaticPropertyKeys() {
		Set<StaticProperty> result = super.getStaticPropertyKeys();
		result.add(StaticProperty.XREF);
		return result;
	}

	/**
	 * Returns static property value for given key.
	 *
	 * @param key the key.
	 * @return the static property value.
	 */
	@Override
	public Object getStaticProperty(StaticProperty key) { 
		Object result = super.getStaticProperty(key);
		if (result == null) {
			switch (key) {
			case XREF:
				result = getXref();
				break;
			default:
				// do nothing
			}
		}
		return result;
	}

	/**
	 * This works so that o.setNotes(x) is the equivalent of o.setProperty("Notes",
	 * x);
	 *
	 * Value may be null in some cases, e.g. graphRef
	 * 
	 * @param key the key.
	 * @param value the static property value.
	 */
	@Override
	public void setStaticProperty(StaticProperty key, Object value) {
		super.setStaticProperty(key, value);
		switch (key) {
		case XREF:
			setXref((Xref) value);
			break;
		default:
			// do nothing
		}
	}

}
