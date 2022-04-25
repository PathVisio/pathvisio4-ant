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
package org.pathvisio.gui.handler;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.PathwayObject;
import org.pathvisio.libgpml.model.shape.ShapeType;
import org.pathvisio.libgpml.model.type.HAlignType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.model.type.LineStyleType;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.StateType;
import org.pathvisio.libgpml.model.type.VAlignType;
import org.pathvisio.libgpml.prop.Property;
import org.pathvisio.libgpml.prop.PropertyType;
import org.pathvisio.libgpml.prop.StaticProperty;
import org.pathvisio.libgpml.prop.StaticPropertyType;

/**
 * This class manages how properties should be displayed. It keeps track of
 * TypeHandlers, which properties should be visible, and the order in which
 * properties should be displayed.
 * <p>
 * The main entry point for plugins are {@link #registerProperty(Property)} and
 * {@link #registerTypeHandler(TypeHandler)}.
 *
 * @author Mark Woon
 */
public class PropertyDisplayManager {
	private static final Map<PropertyType, TypeHandler> TYPE_HANDLERS = new HashMap<PropertyType, TypeHandler>();
	private static final Map<Property, PropPreference> PROPERTY_PREFERENCES = new HashMap<Property, PropPreference>();
	private static final Map<String, Property> DYNAMIC_PROPERTIES = new HashMap<String, Property>();
	private static final Set<Property> MANAGED_DYNAMIC_PROPERTIES = new HashSet<Property>();
	private static final Map<Property, EnumSet<ObjectType>> PROPERTY_SCOPE = new HashMap<Property, EnumSet<ObjectType>>();
	private static boolean STORE_PREFERENCES = false;

	static {
		PreferenceManager.init();

		// register core static property types
		registerTypeHandler(new BooleanHandler());
		registerTypeHandler(NumberHandler.buildHandler(StaticPropertyType.DOUBLE, Double.class));
		registerTypeHandler(NumberHandler.buildHandler(StaticPropertyType.INTEGER, Integer.class));
		registerTypeHandler(new AngleHandler());
		registerTypeHandler(new ColorHandler());
		registerTypeHandler(new FontHandler());
		registerTypeHandler(new CommentsHandler());
		registerTypeHandler(new DescriptionHandler());
		registerTypeHandler(new XrefHandler());
		registerTypeHandler(new DataSourceHandler());
		registerTypeHandler(new ComboHandler(StaticPropertyType.ORGANISM, Organism.latinNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.DATANODETYPE, DataNodeType.getNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.STATETYPE, StateType.getNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.GROUPTYPE, GroupType.getNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.ARROWHEADTYPE, ArrowHeadType.getNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.LINESTYLETYPE, LineStyleType.getNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.CONNECTORTYPE, ConnectorType.getNames(), false));
		registerTypeHandler(new ComboHandler(StaticPropertyType.SHAPETYPE, ShapeType.getVisibleNames(),
				ShapeType.getVisibleValues()));
		registerTypeHandler(
				new ComboHandler(StaticPropertyType.VALIGNTYPE, VAlignType.getNames(), VAlignType.values()));
		registerTypeHandler(
				new ComboHandler(StaticPropertyType.HALIGNTYPE, HAlignType.getNames(), HAlignType.values()));

		// register core properties
		for (StaticProperty p : StaticProperty.values()) {
			registerProperty(p);
		}
	}

	/**
	 * Private constructor - not meant to be instantiated.
	 */
	private PropertyDisplayManager() {
	}

	/**
	 * Returns the visible property keys for the given PathwayElement.
	 * 
	 * @param e the given pathway element.
	 * @return set of property keys.
	 */
	public static Set<Object> getVisiblePropertyKeys(PathwayElement e) {

		Set<Object> result = new HashSet<Object>();
		// add static properties
		for (Property p : e.getStaticPropertyKeys()) {
			ObjectType ot = e.getObjectType();
			// for Group, do not add BorderColor/Width/Style, FillColor and ShapeType
			if (ot == ObjectType.GROUP) {
				if (p == StaticProperty.BORDERCOLOR || p == StaticProperty.BORDERSTYLE
						|| p == StaticProperty.BORDERWIDTH || p == StaticProperty.FILLCOLOR
						|| p == StaticProperty.SHAPETYPE) {
					continue;
				}
			}
			// for Datanode, do not add AliasRef unless type Alias
			if (ot == ObjectType.DATANODE) {
				if (((DataNode) e).getType() != DataNodeType.ALIAS && p == StaticProperty.ALIASREF) {
					continue;
				}
			}
			// for Datanode, Label, and Group, do not add Rotation
			if (ot == ObjectType.DATANODE || ot == ObjectType.LABEL || ot == ObjectType.GROUP) {
				if (p == StaticProperty.ROTATION) {
					continue;
				}
			}
			if (isVisible(p)) {
				result.add(p);
			}
		}
		// add dynamic properties
		for (String key : e.getDynamicPropertyKeys()) {
			Property p = getDynamicProperty(key);
			if (p == null || isVisible(p)) {
				// is visible if it's not associated with a property
				// or is associated with a property whose visibility preference is set
				result.add(key);
			}
		}
		// add managed dynamic properties
		for (Property p : getManagedDynamicProperties(e)) {
			if (isVisible(p))
				result.add(p.getId());
		}
		return result;
	}

	/**
	 * Gets the TypeHandler for the given PropertyType.
	 *
	 * @return the TypeHandler for the given PropertyType or null if none exists
	 */
	public static TypeHandler getTypeHandler(PropertyType type) {
		return TYPE_HANDLERS.get(type);
	}

	/**
	 * Registers a TypeHandler.
	 *
	 * @return true if no other TypeHandler has been registered for the given
	 *         PropertyType, false otherwise
	 */
	public static boolean registerTypeHandler(TypeHandler handler) {

		boolean exists = TYPE_HANDLERS.containsKey(handler.getType());
		if (exists) {
			Logger.log.info("Overwriting type handler for " + handler.getType());
		}
		TYPE_HANDLERS.put(handler.getType(), handler);
		return !exists;
	}

	/**
	 * Registers a property here.
	 * <p>
	 * Dynamic properties will automatically be managed by the display manager,
	 * which means that the property will be editable from the property panel (for
	 * all object types by default, see {@link #setPropertyScope(Property, EnumSet)}
	 * to customize this behavior). Note that the property is not saved to GPML
	 * unless the property is modified.
	 */
	public static void registerProperty(Property prop) {
		registerProperty(prop, true);
	}

	/**
	 * Register a property here.
	 * <p>
	 * Dynamic properties will only be managed by the display manager if
	 * <code>manage</code> is set to true. When the display manager manages a
	 * property, it means that the property will be editable from the property panel
	 * (for all object types by default, see
	 * {@link #setPropertyScope(Property, EnumSet)} to customize this behavior).
	 * Note that the property is not saved to GPML unless the property is modified.
	 *
	 * @param prop   the Property being registered
	 * @param manage true if the display manager should manage this property, false
	 *               if not
	 */
	public static void registerProperty(Property prop, boolean manage) {

		if (!(prop instanceof StaticProperty)) {
			DYNAMIC_PROPERTIES.put(prop.getId(), prop);
			if (manage) {
				MANAGED_DYNAMIC_PROPERTIES.add(prop);
			}
		}
		loadPreference(prop);
	}

	/**
	 * Get all managed dynamic properties that fall within the scope of the given
	 * pathway element.
	 *
	 * @see #setPropertyScope(Property, EnumSet) for info on how to configure the
	 *      property scope.
	 */
	private static Collection<Property> getManagedDynamicProperties(PathwayObject e) {
		Set<Property> props = new HashSet<Property>();

		ObjectType type = e.getObjectType();
		for (Property p : MANAGED_DYNAMIC_PROPERTIES) {
			EnumSet<ObjectType> scope = PROPERTY_SCOPE.get(p);
			if (scope == null || scope.contains(type))
				props.add(p);
		}
		return props;
	}

	/**
	 * Set the scope of a dynamic property (on which object types it applies). The
	 * property panel will only display the property editor for the given object
	 * types.
	 *
	 * @param prop  The property this scope applies to.
	 * @param types The object types that define the scope of this property. If null
	 *              is supplied as value, the property will be displayed on all
	 *              object types (default behavior).
	 */
	public static void setPropertyScope(Property prop, EnumSet<ObjectType> types) {
		PROPERTY_SCOPE.put(prop, types);
	}

	/**
	 * Get the scope of a property (defines on which object types it should apply).
	 *
	 * @return The set of object types that this property applies to or null if the
	 *         property has no specific scope (and should apply to all object
	 *         types).
	 */
	public static EnumSet<ObjectType> getPropertyScope(Property prop) {
		return PROPERTY_SCOPE.get(prop);
	}

	public static Property getDynamicProperty(String key) {
		return DYNAMIC_PROPERTIES.get(key);
	}

	/**
	 * Gets the order of the given Property. If not is found,
	 * {@link Integer#MAX_VALUE} is returned.
	 */
	public static int getPropertyOrder(Property prop) {
		return loadPreference(prop).getOrder();
	}

	/**
	 * Sets the order of the given Property.
	 */
	public static void setPropertyOrder(Property prop, Integer order) {
		loadPreference(prop).setOrder(order);
	}

	/**
	 * Gets whether the given Property should be visible. Default is visible.
	 */
	public static boolean isVisible(Property prop) {
		return loadPreference(prop).isVisible();
	}

	/**
	 * Sets whether the given Property should be visible.
	 */
	public static void setVisible(Property prop, boolean isVisible) {
		loadPreference(prop).setVisible(isVisible);
	}

	/**
	 * Gets whether visibility and order of properties is stored as a preference.
	 */
	public static boolean isStorePreferences() {
		return STORE_PREFERENCES;
	}

	/**
	 * Sets whether visibility and order of properties is stored as a preference.
	 */
	public static void setStorePreferences(boolean storePreferences) {

		if (storePreferences && !STORE_PREFERENCES) {
			// store all existing preferences
			for (PropPreference pref : PROPERTY_PREFERENCES.values()) {
				pref.store();
			}
		}
		STORE_PREFERENCES = storePreferences;

	}

	private static PropPreference loadPreference(Property prop) {

		PropPreference pref = PROPERTY_PREFERENCES.get(prop);
		if (pref == null) {
			pref = new PropPreference(prop);
			if (STORE_PREFERENCES) {
				pref.parsePrefString(PreferenceManager.getCurrent().get(pref));
			}
			PROPERTY_PREFERENCES.put(prop, pref);
		}
		return pref;
	}

	/**
	 * Preference for property display information. Knows how to handle
	 * StaticProperty property.
	 */
	public static class PropPreference implements Preference {
		private static final Integer DEFAULT_ORDER = Integer.MAX_VALUE - 100;
		private Property prop;
		private Integer order = DEFAULT_ORDER;
		private boolean isVisible = true;

		public PropPreference(Property aProp) {
			prop = aProp;
			if (prop instanceof StaticProperty) {
				StaticProperty sp = (StaticProperty) prop;
				order = sp.getOrder();
				if (sp.isHidden() || (sp.isAdvanced()
						&& !PreferenceManager.getCurrent().getBoolean(GlobalPreference.SHOW_ADVANCED_PROPERTIES))) {
					isVisible = false;
				}
			}
		}

		public void store() {
			PreferenceManager.getCurrent().set(this, buildPrefString());
		}

		public void parsePrefString(String value) {
			if (value != null) {
				String[] rez = value.split(":");
				order = new Integer(rez[0]);
				isVisible = Boolean.parseBoolean(rez[1]);
			}
		}

		public String buildPrefString() {
			return order.toString() + ":" + isVisible;
		}

		public Integer getOrder() {
			return order;
		}

		public void setOrder(int aOrder) {
			order = aOrder;
			if (STORE_PREFERENCES) {
				PreferenceManager.getCurrent().set(this, buildPrefString());
			}
		}

		public boolean isVisible() {
			return isVisible;
		}

		public void setVisible(boolean visible) {
			isVisible = visible;
			if (STORE_PREFERENCES) {
				PreferenceManager.getCurrent().set(this, buildPrefString());
			}
		}

		public String name() {
			return "propertyDisplayManager." + prop.getId();
		}

		public String getDefault() {
			Integer defaultOrder = DEFAULT_ORDER;
			if (prop instanceof StaticProperty) {
				defaultOrder = ((StaticProperty) prop).getOrder();
			}
			return defaultOrder + ":" + Boolean.TRUE;
		}
	}
}
