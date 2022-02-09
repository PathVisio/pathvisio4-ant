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

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
//import org.pathvisio.core.preferences.GlobalPreference; TODO 
//import org.pathvisio.core.preferences.PreferenceManager;
//import org.pathvisio.core.view.VState;
import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.biopax.BiopaxElement;
import org.pathvisio.libgpml.biopax.BiopaxReferenceManager;
import org.pathvisio.libgpml.model.GraphLink.LinkableFrom;
import org.pathvisio.libgpml.model.shape.IShape;
import org.pathvisio.libgpml.model.type.HAlignType;
import org.pathvisio.libgpml.model.type.AnchorShapeType;
import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.model.type.LineStyleType;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.type.OrientationType;
import org.pathvisio.libgpml.model.type.ShapeType;
import org.pathvisio.libgpml.model.type.VAlignType;
import org.pathvisio.libgpml.prop.StaticProperty;
import org.pathvisio.libgpml.util.Utils;

/**
 * PathwayElement is responsible for maintaining the data for all the individual
 * objects that can appear on a pwy (Lines, GeneProducts, Shapes, etc.)
 * <p>
 * All PathwayElements have an ObjectType. This ObjectType is specified at
 * creation time and can't be modified. To create a PathwayElement, use the
 * createPathwayElement() function. This is a factory method that returns a
 * different implementation class depending on the specified ObjectType.
 * <p>
 * PathwayElements have a number of properties which consist of a key, value
 * pair.
 * <p>
 * There are two types of properties: Static and Dynamic Static properties are
 * one of the properties
 * <p>
 * Dynamic properties can have any String as key. Their value is always of type
 * String. Dynamic properties are not essential for the functioning of PathVisio
 * and can be used to store arbitrary data. In GPML, dynamic properties are
 * stored in an <Attribute key="" value=""/> tag. Internally, dynamic properties
 * are stored in a Map<String, String>
 * <p>
 * Static properties must have a key from the StaticProperty enum Their value
 * can be various types which can be obtained from StaticProperty.type(). Static
 * properties can be queried with getStaticProperty (key) and
 * setStaticProperty(key, value), but also specific accessors such as e.g.
 * getTextLabel() and setTextLabel()
 * <p>
 * Internally, dynamic properties are stored in various fields of the
 * PathwayElement Object. The static properties are a union of all possible
 * fields (e.g it has both start and endpoints for lines, and label text for
 * labels)
 * <p>
 * the setPropertyEx() and getPropertyEx() functions can be used to access both
 * dynamic and static properties from the same function. If key instanceof
 * String then it's assumed the caller wants a dynamic property, if key
 * instanceof StaticProperty then the static property is used.
 * <p>
 * most static properties cannot be set to null. Notable exceptions are graphId,
 * startGraphRef and endGraphRef.
 */
public class PathwayObject implements LinkableTo, Comparable<PathwayObject> {
	// TreeMap has better performance than HashMap
	// in the (common) case where no attributes are present
	// This map should never contain non-null values, if a value
	// is set to null the key should be removed.
	private Map<String, String> attributes = new TreeMap<String, String>();

	/**
	 * get a set of all dynamic property keys
	 */
	public Set<String> getDynamicPropertyKeys() {
		return attributes.keySet();
	}

	/**
	 * set a dynamic property. Setting to null means removing this dynamic property
	 * altogether
	 */
	public void setDynamicProperty(String key, String value) {
		if (value == null)
			attributes.remove(key);
		else
			attributes.put(key, value);
		fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, key));
	}

	/**
	 * get a dynamic property
	 */
	public String getDynamicProperty(String key) {
		return attributes.get(key);
	}

	/**
	 * A comment in a pathway element: each element can have zero or more comments
	 * with it, and each comment has a source and a text.
	 */
	public class Comment implements Cloneable {
		public Comment(String aComment, String aSource) {
			commentSource = aSource;
			commentText = aComment;
		}

		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		private String commentSource;
		private String commentText;

		public String getCommentSource() {
			return commentSource;
		}

		public String getCommentText() {
			return commentText;
		}

		public void setCommentSource(String s) {
			if (s != null && !commentSource.equals(s)) {
				commentSource = s;
				changed();
			}
		}

		public void setCommentText(String c) {
			if (c != null && !commentText.equals(c)) {
				commentText = c;
				changed();
			}
		}

		private void changed() {
			fireObjectModifiedEvent(
					PathwayObjectEvent.createSinglePropertyEvent(PathwayObject.this, StaticProperty.COMMENTS));
		}

		public String toString() {
			String src = "";
			if (commentSource != null && !"".equals(commentSource)) {
				src = " (" + commentSource + ")";
			}
			return commentText + src;
		}
	}

	/**
	 * Represents a generic point in an coordinates.length dimensional space. The
	 * point is automatically a {@link LinkableTo} and therefore lines can link to
	 * the point.
	 * 
	 * @see LinePoint
	 * @see Anchor
	 * @author thomas
	 *
	 */
	private abstract class GenericPoint implements Cloneable, LinkableTo {
		private double[] coordinates;

		private String elementId;

		GenericPoint(double[] coordinates) {
			this.coordinates = coordinates;
		}

		GenericPoint(GenericPoint p) {
			coordinates = new double[p.coordinates.length];
			System.arraycopy(p.coordinates, 0, coordinates, 0, coordinates.length);
			if (p.elementId != null)
				elementId = p.elementId;
		}

		protected void moveBy(double[] delta) {
			for (int i = 0; i < coordinates.length; i++) {
				coordinates[i] += delta[i];
			}
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(PathwayObject.this));
		}

		protected void moveTo(double[] coordinates) {
			this.coordinates = coordinates;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(PathwayObject.this));
		}

		protected void moveTo(GenericPoint p) {
			coordinates = p.coordinates;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(PathwayObject.this));
			;
		}

		protected double getCoordinate(int i) {
			return coordinates[i];
		}

		public String getElementId() {
			return elementId;
		}

		public String setGeneratedElementId() {
			setElementId(parent.getUniqueGraphId());
			return elementId;
		}

		public void setElementId(String v) {
			GraphLink.setGraphId(v, this, PathwayObject.this.parent);
			elementId = v;
			fireObjectModifiedEvent(
					PathwayObjectEvent.createSinglePropertyEvent(PathwayObject.this, StaticProperty.GRAPHID));
		}

		public Object clone() throws CloneNotSupportedException {
			GenericPoint p = (GenericPoint) super.clone();
			if (elementId != null)
				p.elementId = elementId;
			return p;
		}

		public Set<LinkableFrom> getReferences() {
			return GraphLink.getReferences(this, parent);
		}

		public PathwayModel getPathwayModel() {
			return parent;
		}

		public PathwayObject getParent() {
			return PathwayObject.this;
		}
	}

	/**
	 * This class represents the Line.Graphics.Point element in GPML.
	 * 
	 * @author thomas
	 *
	 */
	public class LinePoint extends GenericPoint implements LinkableFrom {
		private String elementRef;
		private boolean relativeSet;

		public LinePoint(double x, double y) {
			super(new double[] { x, y, 0, 0 });
		}

		LinePoint(LinePoint p) {
			super(p);
			if (p.elementRef != null)
				elementRef = p.elementRef;
		}

		public void moveBy(double dx, double dy) {
			super.moveBy(new double[] { dx, dy, 0, 0 });
		}

		public void moveTo(double x, double y) {
			super.moveTo(new double[] { x, y, 0, 0 });
		}

		public void setX(double nx) {
			if (nx != getX())
				moveBy(nx - getX(), 0);
		}

		public void setY(double ny) {
			if (ny != getY())
				moveBy(0, ny - getY());
		}

		public double getX() {
			if (isRelative()) {
				return getAbsolute().getX();
			} else {
				return getCoordinate(0);
			}
		}

		public double getY() {
			if (isRelative()) {
				return getAbsolute().getY();
			} else {
				return getCoordinate(1);
			}
		}

		protected double getRawX() {
			return getCoordinate(0);
		}

		protected double getRawY() {
			return getCoordinate(1);
		}

		public double getRelX() {
			return getCoordinate(2);
		}

		public double getRelY() {
			return getCoordinate(3);
		}

		private Point2D getAbsolute() {
			return getGraphIdContainer().toAbsoluteCoordinate(new Point2D.Double(getRelX(), getRelY()));
		}

		public void setRelativePosition(double rx, double ry) {
			moveTo(new double[] { getX(), getY(), rx, ry });
			relativeSet = true;
		}

		/**
		 * Checks if the position of this point should be stored as relative or absolute
		 * coordinates
		 * 
		 * @return true if the coordinates are relative, false if not
		 */
		public boolean isRelative() {
			PathwayModel p = getPathwayModel();
			if (p != null) {
				LinkableTo gc = getPathwayModel().getGraphIdContainer(elementRef);
				return gc != null;
			}
			return false;
		}

		/**
		 * Helper method for converting older GPML files without relative coordinates.
		 * 
		 * @return true if {@link #setRelativePosition(double, double)} was called to
		 *         set the relative coordinates, false if not.
		 */
		protected boolean relativeSet() {
			return relativeSet;
		}

		private LinkableTo getGraphIdContainer() {
			return getPathwayModel().getGraphIdContainer(elementRef);
		}

		public String getElementRef() {
			return elementRef;
		}

		/**
		 * Set a reference to another object with a graphId. If a parent is set, this
		 * will automatically deregister the previously held reference and register the
		 * new reference as necessary
		 *
		 * @param v reference to set.
		 */
		public void setElementRef(String v) {
			if (!Utils.stringEquals(elementRef, v)) {
				if (parent != null) {
					if (elementRef != null) {
						parent.removeGraphRef(elementRef, this);
					}
					if (v != null) {
						parent.addGraphRef(v, this);
					}
				}
				elementRef = v;
			}
		}

		public Object clone() throws CloneNotSupportedException {
			LinePoint p = (LinePoint) super.clone();
			if (elementRef != null)
				p.elementRef = elementRef;
			return p;
		}

		public Point2D toPoint2D() {
			return new Point2D.Double(getX(), getY());
		}

		/**
		 * Link to an object. Current absolute coordinates will be converted to relative
		 * coordinates based on the object to link to.
		 */
		public void linkTo(LinkableTo idc) {
			Point2D rel = idc.toRelativeCoordinate(toPoint2D());
			linkTo(idc, rel.getX(), rel.getY());
		}

		/**
		 * Link to an object using the given relative coordinates
		 */
		public void linkTo(LinkableTo idc, double relX, double relY) {
			String id = idc.getElementId();
			if (id == null)
				id = idc.setGeneratedElementId();
			setElementRef(idc.getElementId());
			setRelativePosition(relX, relY);
		}

		/**
		 * note that this may be called any number of times when this point is already
		 * unlinked
		 */
		public void unlink() {
			if (elementRef != null) {
				if (getPathwayModel() != null) {
					Point2D abs = getAbsolute();
					moveTo(abs.getX(), abs.getY());
				}
				relativeSet = false;
				setElementRef(null);
				fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(PathwayObject.this));
			}
		}

		public Point2D toAbsoluteCoordinate(Point2D p) {
			return new Point2D.Double(p.getX() + getX(), p.getY() + getY());
		}

		public Point2D toRelativeCoordinate(Point2D p) {
			return new Point2D.Double(p.getX() - getX(), p.getY() - getY());
		}

		/**
		 * Find out if this point is linked to an object. Returns true if a graphRef
		 * exists and is not an empty string
		 */
		public boolean isLinked() {
			String ref = getElementRef();
			return ref != null && !"".equals(ref);
		}

		public void refeeChanged() {
			// called whenever the object being referred to has changed.
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(PathwayObject.this));
		}
	}

	/**
	 * This class represents the Line.Graphics.Anchor element in GPML
	 * 
	 * @author thomas
	 *
	 */
	public class Anchor extends GenericPoint {
		AnchorShapeType shape = AnchorShapeType.NONE;

		public Anchor(double position) {
			super(new double[] { position });
		}

		public Anchor(Anchor a) {
			super(a);
			shape = a.shape;
		}

		public void setShape(AnchorShapeType type) {
			if (!this.shape.equals(type) && type != null) {
				this.shape = type;
				fireObjectModifiedEvent(
						PathwayObjectEvent.createSinglePropertyEvent(PathwayObject.this, StaticProperty.LINESTYLE));
			}
		}

		public AnchorShapeType getShape() {
			return shape;
		}

		public double getPosition() {
			return getCoordinate(0);
		}

		public void setPosition(double position) {
			if (position != getPosition()) {
				moveBy(position - getPosition());
			}
		}

		public void moveBy(double delta) {
			super.moveBy(new double[] { delta });
		}

		public Point2D toAbsoluteCoordinate(Point2D p) {
			Point2D l = ((LineElement) getParent()).getConnectorShape().fromLineCoordinate(getPosition());
			return new Point2D.Double(p.getX() + l.getX(), p.getY() + l.getY());
		}

		public Point2D toRelativeCoordinate(Point2D p) {
			Point2D l = ((LineElement) getParent()).getConnectorShape().fromLineCoordinate(getPosition());
			return new Point2D.Double(p.getX() - l.getX(), p.getY() - l.getY());
		}
	}

	/* Some default values */
	private static final int M_INITIAL_FONTSIZE = 12;
	private static final int M_INITIAL_LABEL_WIDTH = 90;
	private static final int M_INITIAL_LABEL_HEIGHT = 25;
	private static final int M_INITIAL_LINE_LENGTH = 30;
	private static final int M_INITIAL_STATE_SIZE = 15;
	private static final int M_INITIAL_SHAPE_SIZE = 30;
	private static final int M_INITIAL_CELLCOMP_HEIGHT = 100;
	private static final int M_INITIAL_CELLCOMP_WIDTH = 200;
	private static final int M_INITIAL_BRACE_HEIGHT = 15;
	private static final int M_INITIAL_BRACE_WIDTH = 60;
	private static final int M_INITIAL_GENEPRODUCT_WIDTH = 90;
	private static final int M_INITIAL_GENEPRODUCT_HEIGHT = 25;

	// groups should be behind other graphics
	// to allow background colors
	private static final int Z_ORDER_GROUP = 0x1000;
	// default order of geneproduct, label, shape and line determined
	// by GenMAPP legacy
	private static final int Z_ORDER_GENEPRODUCT = 0x8000;
	private static final int Z_ORDER_LABEL = 0x7000;
	private static final int Z_ORDER_SHAPE = 0x4000;
	private static final int Z_ORDER_LINE = 0x3000;
	// default order of uninteresting elements.
	private static final int Z_ORDER_DEFAULT = 0x0000;

	/**
	 * default z order for newly created objects
	 */
	private static int getDefaultZOrder(ObjectType value) {
		switch (value) {
		case SHAPE:
			return Z_ORDER_SHAPE;
		case STATE:
			return Z_ORDER_GENEPRODUCT + 10;
		case DATANODE:
			return Z_ORDER_GENEPRODUCT;
		case LABEL:
			return Z_ORDER_LABEL;
		case LINE:
			return Z_ORDER_LINE;
		case GRAPHLINE:
			return Z_ORDER_LINE;
		case LEGEND:
		case INFOBOX:
		case MAPPINFO:
		case BIOPAX:
			return Z_ORDER_DEFAULT;
		case GROUP:
			return Z_ORDER_GROUP;
		default:
			throw new IllegalArgumentException("Invalid object type " + value);
		}
	}

	/**
	 * Instantiate a pathway element. The required parameter objectType ensures only
	 * objects with a valid type can be created.
	 *
	 * @param ot Type of object, one of the ObjectType.* fields
	 */
	public static PathwayObject createPathwayElement(ObjectType ot) {
		PathwayObject e;
		switch (ot) {
		case BIOPAX:
			e = new BiopaxElement();
			break;
		case GROUP:
			e = new Group();
			break;
		case LINE:
			e = new LineElement(ObjectType.LINE);
			break;
		case GRAPHLINE:
			e = new LineElement(ObjectType.GRAPHLINE);
			break;
		case STATE:
			e = new State();
			break;
		default:
			e = new PathwayObject(ot);
			break;
		}
		return e;
	}

	protected PathwayObject(ObjectType ot) {
		/* set default value for transparency */
		if (ot == ObjectType.LINE || ot == ObjectType.LABEL || ot == ObjectType.DATANODE || ot == ObjectType.STATE
				|| ot == ObjectType.GRAPHLINE) {
			fillColor = Color.WHITE;
		} else {
			fillColor = null;
		}
		/* set default value for shapeType */
		if (ot == ObjectType.LABEL) {
			shapeType = ShapeType.NONE;
		} else {
			shapeType = ShapeType.RECTANGLE;
		}

		objectType = ot;
		zOrder = getDefaultZOrder(ot);
	}

	int zOrder;

	public int getZOrder() {
		return zOrder;
	}

	public void setZOrder(int z) {
		if (z != zOrder) {
			zOrder = z;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.ZORDER));
		}
	}

	/**
	 * Parent of this object: may be null (for example, when object is in clipboard)
	 */
	protected PathwayModel parent = null;

	public PathwayModel getParent() {
		return parent;
	}

	/**
	 * Get the parent pathway. Same as {@link #getParent()}, but necessary to comply
	 * to the {@link LinkableTo} interface.
	 */
	public PathwayModel getPathwayModel() {
		return parent;
	}

	/**
	 * Set parent. Do not use this method directly! parent is set automatically when
	 * using Pathway.add/remove
	 * 
	 * @param v the parentGENEID
	 */
	void setParent(PathwayModel v) {
		parent = v;
	}

	/**
	 * Returns keys of available static properties and dynamic properties as an
	 * object list
	 */
	public Set<Object> getPropertyKeys() {
		Set<Object> keys = new HashSet<Object>();
		keys.addAll(getStaticPropertyKeys());
		keys.addAll(getDynamicPropertyKeys());
		return keys;
	}

	private static final Map<ObjectType, Set<StaticProperty>> ALLOWED_PROPS;

	static {
		Set<StaticProperty> propsCommon = EnumSet.of(StaticProperty.COMMENTS, StaticProperty.GRAPHID,
				StaticProperty.GROUPREF, StaticProperty.BIOPAXREF, StaticProperty.ZORDER);
		Set<StaticProperty> propsCommonShape = EnumSet.of(StaticProperty.CENTERX, StaticProperty.CENTERY,
				StaticProperty.WIDTH, StaticProperty.HEIGHT, StaticProperty.COLOR);
		Set<StaticProperty> propsCommonStyle = EnumSet.of(StaticProperty.TEXTLABEL, StaticProperty.FONTNAME,
				StaticProperty.FONTWEIGHT, StaticProperty.FONTSTYLE, StaticProperty.FONTSIZE, StaticProperty.ALIGN,
				StaticProperty.VALIGN, StaticProperty.COLOR, StaticProperty.FILLCOLOR, StaticProperty.TRANSPARENT,
				StaticProperty.SHAPETYPE, StaticProperty.LINETHICKNESS, StaticProperty.LINESTYLE);
		Set<StaticProperty> propsCommonLine = EnumSet.of(StaticProperty.COLOR, StaticProperty.STARTX,
				StaticProperty.STARTY, StaticProperty.ENDX, StaticProperty.ENDY, StaticProperty.STARTLINETYPE,
				StaticProperty.ENDLINETYPE, StaticProperty.LINESTYLE, StaticProperty.LINETHICKNESS,
				StaticProperty.STARTGRAPHREF, StaticProperty.ENDGRAPHREF);
		ALLOWED_PROPS = new EnumMap<ObjectType, Set<StaticProperty>>(ObjectType.class);
		{
			Set<StaticProperty> propsMappinfo = EnumSet.of(StaticProperty.COMMENTS, StaticProperty.MAPINFONAME,
					StaticProperty.ORGANISM, StaticProperty.MAPINFO_DATASOURCE, StaticProperty.VERSION,
					StaticProperty.AUTHOR, StaticProperty.MAINTAINED_BY, StaticProperty.EMAIL,
					StaticProperty.LAST_MODIFIED, StaticProperty.LICENSE, StaticProperty.BOARDWIDTH,
					StaticProperty.BOARDHEIGHT);
			ALLOWED_PROPS.put(ObjectType.MAPPINFO, propsMappinfo);
		}
		{
			Set<StaticProperty> propsState = EnumSet.of(StaticProperty.RELX, StaticProperty.RELY, StaticProperty.WIDTH,
					StaticProperty.HEIGHT, StaticProperty.MODIFICATIONTYPE, StaticProperty.GRAPHREF,
					StaticProperty.ROTATION);
			propsState.addAll(propsCommon);
			propsState.addAll(propsCommonStyle);
			ALLOWED_PROPS.put(ObjectType.STATE, propsState);
		}
		{
			Set<StaticProperty> propsShape = EnumSet.of(StaticProperty.FILLCOLOR, StaticProperty.SHAPETYPE,
					StaticProperty.ROTATION, StaticProperty.TRANSPARENT, StaticProperty.LINESTYLE);
			propsShape.addAll(propsCommon);
			propsShape.addAll(propsCommonStyle);
			propsShape.addAll(propsCommonShape);
			ALLOWED_PROPS.put(ObjectType.SHAPE, propsShape);
		}
		{
			Set<StaticProperty> propsDatanode = EnumSet.of(StaticProperty.GENEID, StaticProperty.DATASOURCE,
					StaticProperty.TEXTLABEL, StaticProperty.TYPE);
			propsDatanode.addAll(propsCommon);
			propsDatanode.addAll(propsCommonStyle);
			propsDatanode.addAll(propsCommonShape);
			ALLOWED_PROPS.put(ObjectType.DATANODE, propsDatanode);
		}
		{
			Set<StaticProperty> propsGraphLine = new HashSet<StaticProperty>();
			propsGraphLine.addAll(propsCommon);
			propsGraphLine.addAll(propsCommonLine);
			ALLOWED_PROPS.put(ObjectType.GRAPHLINE, propsGraphLine);
		}
		{
			Set<StaticProperty> propsLine = EnumSet.of(StaticProperty.GENEID, StaticProperty.DATASOURCE);
			propsLine.addAll(propsCommon);
			propsLine.addAll(propsCommonLine);
			ALLOWED_PROPS.put(ObjectType.LINE, propsLine);
		}
		{
			Set<StaticProperty> propsLabel = EnumSet.of(StaticProperty.HREF);
			propsLabel.addAll(propsCommon);
			propsLabel.addAll(propsCommonStyle);
			propsLabel.addAll(propsCommonShape);
			ALLOWED_PROPS.put(ObjectType.LABEL, propsLabel);
		}
		{
			Set<StaticProperty> propsGroup = EnumSet.of(StaticProperty.GROUPID, StaticProperty.GROUPREF,
					StaticProperty.BIOPAXREF, StaticProperty.GROUPSTYLE, StaticProperty.TEXTLABEL,
					StaticProperty.COMMENTS, StaticProperty.ZORDER);
			ALLOWED_PROPS.put(ObjectType.GROUP, propsGroup);
		}
		{
			Set<StaticProperty> propsInfobox = EnumSet.of(StaticProperty.CENTERX, StaticProperty.CENTERY,
					StaticProperty.ZORDER);
			ALLOWED_PROPS.put(ObjectType.INFOBOX, propsInfobox);
		}
		{
			Set<StaticProperty> propsLegend = EnumSet.of(StaticProperty.CENTERX, StaticProperty.CENTERY,
					StaticProperty.ZORDER);
			ALLOWED_PROPS.put(ObjectType.LEGEND, propsLegend);
		}
		{
			Set<StaticProperty> propsBiopax = EnumSet.noneOf(StaticProperty.class);
			ALLOWED_PROPS.put(ObjectType.BIOPAX, propsBiopax);
		}
	};

	/**
	 * get all attributes that are stored as static members.
	 */
	public Set<StaticProperty> getStaticPropertyKeys() {
		return ALLOWED_PROPS.get(getObjectType());
	}

	/**
	 * Set dynamic or static properties at the same time Will be replaced with
	 * setProperty in the future.
	 */
	public void setPropertyEx(Object key, Object value) {
		if (key instanceof StaticProperty) {
			setStaticProperty((StaticProperty) key, value);
		} else if (key instanceof String) {
			setDynamicProperty((String) key, value.toString());
		} else {
			throw new IllegalArgumentException();
		}
	}

	public Object getPropertyEx(Object key) {
		if (key instanceof StaticProperty) {
			return getStaticProperty((StaticProperty) key);
		} else if (key instanceof String) {
			return getDynamicProperty((String) key);
		} else {
			throw new IllegalArgumentException();
		}
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
		case COMMENTS:
			setComments((List<Comment>) value);
			break;
		case COLOR:
			setColor((Color) value);
			break;

		case CENTERX:
			setCenterX((Double) value);
			break;
		case CENTERY:
			setCenterY((Double) value);
			break;
		case WIDTH:
			setWidth((Double) value);
			break;
		case HEIGHT:
			setHeight((Double) value);
			break;

		case FILLCOLOR:
			setFillColor((Color) value);
			break;
		case SHAPETYPE:
			setShapeType((IShape) value);
			break;
		case ROTATION:
			setRotation((Double) value);
			break;
		case RELX:
			setRelX((Double) value);
			break;
		case RELY:
			setRelY((Double) value);
			break;
		case STARTX:
			setStartLinePointX((Double) value);
			break;
		case STARTY:
			setStartLinePointY((Double) value);
			break;
		case ENDX:
			setEndLinePointX((Double) value);
			break;
		case ENDY:
			setEndLinePointY((Double) value);
			break;
		case ENDLINETYPE:
			setEndLineType((ArrowHeadType) value);
			break;
		case STARTLINETYPE:
			setStartLineType((ArrowHeadType) value);
			break;
		case LINESTYLE:
			setLineStyle((Integer) value);
			break;

		case ORIENTATION:
			setOrientation((Integer) value);
			break;

		case GENEID:
			setIdentifier((String) value);
			break;
		case DATASOURCE:
			if (value instanceof DataSource) {
				setDataSource((DataSource) value);
			} else {
				setDataSource(DataSource.getExistingByFullName((String) value));
			}
			break;
		case TYPE:
			setDataNodeType((String) value);
			break;

		case TEXTLABEL:
			setTextLabel((String) value);
			break;
		case HREF:
			setHref((String) value);
			break;
		case FONTNAME:
			setFontName((String) value);
			break;
		case FONTWEIGHT:
			setFontWeight((Boolean) value);
			break;
		case FONTSTYLE:
			setFontStyle((Boolean) value);
			break;
		case FONTSIZE:
			setFontSize((Double) value);
			break;
		case MAPINFONAME:
			setTitle((String) value);
			break;
		case ORGANISM:
			setOrganism((String) value);
			break;
		case MAPINFO_DATASOURCE:
			setSouce((String) value);
			break;
		case VERSION:
			setVersion((String) value);
			break;
		case AUTHOR:
			setAuthor((String) value);
			break;
		case MAINTAINED_BY:
			setMaintainer((String) value);
			break;
		case EMAIL:
			setEmail((String) value);
			break;
		case LAST_MODIFIED:
			setLastModified((String) value);
			break;
		case LICENSE:
			setCopyright((String) value);
			break;
		case BOARDWIDTH:
			// ignore, board width is calculated automatically
			break;
		case BOARDHEIGHT:
			// ignore, board width is calculated automatically
			break;
		case GRAPHID:
			setElementId((String) value);
			break;
		case STARTGRAPHREF:
			setStartElementRef((String) value);
			break;
		case ENDGRAPHREF:
			setEndElementRef((String) value);
			break;
		case GROUPID:
			setGroupId((String) value);
			break;
		case GROUPREF:
			setGroupRef((String) value);
			break;
		case TRANSPARENT:
			setTransparent((Boolean) value);
			break;

		case BIOPAXREF:
			setBiopaxRefs((List<String>) value);
			break;
		case ZORDER:
			setZOrder((Integer) value);
			break;
		case GROUPSTYLE:
			if (value instanceof GroupType) {
				setGroupType((GroupType) value);
			} else {
				setGroupType(GroupType.fromName((String) value));
			}
			break;
		case ALIGN:
			setHAlign((HAlignType) value);
			break;
		case VALIGN:
			setVAlign((VAlignType) value);
			break;
		case LINETHICKNESS:
			setLineWidth((Double) value);
			break;
		}
	}

	public Object getStaticProperty(StaticProperty key) {
		if (!getStaticPropertyKeys().contains(key))
			throw new IllegalArgumentException(
					"Property " + key.name() + " is not allowed for objects of type " + getObjectType());
		Object result = null;
		switch (key) {
		case COMMENTS:
			result = getComments();
			break;
		case COLOR:
			result = getColor();
			break;

		case CENTERX:
			result = getCenterX();
			break;
		case CENTERY:
			result = getCenterY();
			break;
		case WIDTH:
			result = getWidth();
			break;
		case HEIGHT:
			result = getHeight();
			break;

		case FILLCOLOR:
			result = getFillColor();
			break;
		case SHAPETYPE:
			result = getShapeType();
			break;
		case ROTATION:
			result = getRotation();
			break;
		case RELX:
			result = getRelX();
			break;
		case RELY:
			result = getRelY();
			break;
		case STARTX:
			result = getStartLinePointX();
			break;
		case STARTY:
			result = getStartLinePointY();
			break;
		case ENDX:
			result = getEndLinePointX();
			break;
		case ENDY:
			result = getEndLinePointY();
			break;
		case ENDLINETYPE:
			result = getEndLineType();
			break;
		case STARTLINETYPE:
			result = getStartLineType();
			break;
		case LINESTYLE:
			result = getLineStyle();
			break;

		case ORIENTATION:
			result = getOrientation();
			break;

		case GENEID:
			result = getIdentifier();
			break;
		case DATASOURCE:
			result = getDataSource();
			break;
		case TYPE:
			result = getDataNodeType();
			break;

		case TEXTLABEL:
			result = getTextLabel();
			break;
		case HREF:
			result = getHref();
			break;
		case FONTNAME:
			result = getFontName();
			break;
		case FONTWEIGHT:
			result = getFontWeight();
			break;
		case FONTSTYLE:
			result = getFontStyle();
			break;
		case FONTSIZE:
			result = getFontSize();
			break;

		case MAPINFONAME:
			result = getTitle();
			break;
		case ORGANISM:
			result = getOrganism();
			break;
		case MAPINFO_DATASOURCE:
			result = getSource();
			break;
		case VERSION:
			result = getVersion();
			break;
		case AUTHOR:
			result = getAuthor();
			break;
		case MAINTAINED_BY:
			result = getMaintainer();
			break;
		case EMAIL:
			result = getEmail();
			break;
		case LAST_MODIFIED:
			result = getLastModified();
			break;
		case LICENSE:
			result = getCopyright();
			break;
		case BOARDWIDTH:
			result = getBoardSize()[0];
			break;
		case BOARDHEIGHT:
			result = getBoardSize()[1];
			break;
		case GRAPHID:
			result = getElementId();
			break;
		case STARTGRAPHREF:
			result = getStartElementRef();
			break;
		case ENDGRAPHREF:
			result = getEndElementRef();
			break;
		case GROUPID:
			result = createGroupId();
			break;
		case GROUPREF:
			result = getGroupRef();
			break;
		case TRANSPARENT:
			result = isTransparent();
			break;
		case BIOPAXREF:
			result = getBiopaxRefs();
			break;
		case ZORDER:
			result = getZOrder();
			break;
		case GROUPSTYLE:
			result = getGroupType().toString();
			break;
		case ALIGN:
			result = getHAlign();
			break;
		case VALIGN:
			result = getVAlign();
			break;
		case LINETHICKNESS:
			result = getLineWidth();
			break;
		}

		return result;
	}

	/**
	 * Note: doesn't change parent, only fields
	 *
	 * Used by UndoAction.
	 *
	 * @param src
	 */
	public void copyValuesFrom(PathwayObject src) {
		attributes = new TreeMap<String, String>(src.attributes); // create copy
		author = src.author;
		copyright = src.copyright;
		centerX = src.centerX;
		centerY = src.centerY;
		relX = src.relX;
		relY = src.relY;
		zOrder = src.zOrder;
		color = src.color;
		fillColor = src.fillColor;
		dataSource = src.dataSource;
		email = src.email;
		fontName = src.fontName;
		fontSize = src.fontSize;
		fontWeight = src.fontWeight;
		fontStyle = src.fontStyle;
		fontStrikethru = src.fontStrikethru;
		fontDecoration = src.fontDecoration;
		setIdentifier = src.setIdentifier;
		dataNodeType = src.dataNodeType;
		height = src.height;
		textLabel = src.textLabel;
		href = src.href;
		lastModified = src.lastModified;
		lineStyle = src.lineStyle;
		startLineType = src.startLineType;
		endLineType = src.endLineType;
		maintainer = src.maintainer;
		source = src.source;
		title = src.title;
		organism = src.organism;
		rotation = src.rotation;
		shapeType = src.shapeType;
		lineWidth = src.lineWidth;
		hAlign = src.hAlign;
		vAlign = src.vAlign;
		linePoints = new ArrayList<LinePoint>();
		for (LinePoint p : src.linePoints) {
			linePoints.add(new LinePoint(p));
		}
		for (Anchor a : src.anchors) {
			anchors.add(new Anchor(a));
		}
		comments = new ArrayList<Comment>();
		for (Comment c : src.comments) {
			try {
				comments.add((Comment) c.clone());
			} catch (CloneNotSupportedException e) {
				assert (false);
				/* not going to happen */
			}
		}
		version = src.version;
		width = src.width;
		elementId = src.elementId;
		elementRef = src.elementRef;
		groupId = src.groupId;
		groupRef = src.groupRef;
		groupType = src.groupType;
		connectorType = src.connectorType;
		biopaxRefs = (List<String>) ((ArrayList<String>) src.biopaxRefs).clone();
		fireObjectModifiedEvent(PathwayObjectEvent.createAllPropertiesEvent(this));
	}

	/**
	 * Copy Object. The object will not be part of the same Pathway object, it's
	 * parent will be set to null.
	 *
	 * No events will be sent to the parent of the original.
	 */
	public PathwayObject copy() {
		PathwayObject result = PathwayObject.createPathwayElement(objectType);
		result.copyValuesFrom(this);
		result.parent = null;
		return result;
	}

	protected ObjectType objectType = ObjectType.DATANODE;

	public ObjectType getObjectType() {
		return objectType;
	}

	// only for lines
	private List<LinePoint> linePoints = Arrays.asList(new LinePoint(0, 0), new LinePoint(0, 0));

	public void setLinePoints(List<LinePoint> points) {
		if (points != null) {
			if (points.size() < 2) {
				throw new IllegalArgumentException("Points array should at least have two elements");
			}
			linePoints = points;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	public LinePoint getStartLinePoint() {
		return linePoints.get(0);
	}

	public void setStartLinePoint(LinePoint p) {
		getStartLinePoint().moveTo(p);
	}

	public LinePoint getEndLinePoint() {
		return linePoints.get(linePoints.size() - 1);
	}

	public void setEndLinePoint(LinePoint p) {
		getEndLinePoint().moveTo(p);
	}

	public List<LinePoint> getLinePoints() {
		return linePoints;
	}

	public double getStartLinePointX() {
		return getStartLinePoint().getX();
	}

	public void setStartLinePointX(double v) {
		getStartLinePoint().setX(v);
	}

	public double getStartLinePointY() {
		return getStartLinePoint().getY();
	}

	public void setStartLinePointY(double v) {
		getStartLinePoint().setY(v);
	}

	public double getEndLinePointX() {
		return linePoints.get(linePoints.size() - 1).getX();
	}

	public void setEndLinePointX(double v) {
		getEndLinePoint().setX(v);
	}

	public double getEndLinePointY() {
		return getEndLinePoint().getY();
	}

	public void setEndLinePointY(double v) {
		getEndLinePoint().setY(v);
	}

	protected int lineStyle = LineStyleType.SOLID;

	public int getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(int value) {
		if (lineStyle != value) {
			lineStyle = value;
			// handle LineStyle.DOUBLE until GPML is updated
			// TODO: remove after next GPML update
			if (lineStyle == LineStyleType.DOUBLE)
				setDynamicProperty(LineStyleType.DOUBLE_LINE_KEY, "Double");
			else
				setDynamicProperty(LineStyleType.DOUBLE_LINE_KEY, null);
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		}
	}

	protected ArrowHeadType endLineType = ArrowHeadType.LINE;
	protected ArrowHeadType startLineType = ArrowHeadType.LINE;

	public ArrowHeadType getStartLineType() {
		return startLineType == null ? ArrowHeadType.LINE : startLineType;
	}

	public ArrowHeadType getEndLineType() {
		return endLineType == null ? ArrowHeadType.LINE : endLineType;
	}

	public void setStartLineType(ArrowHeadType value) {
		if (startLineType != value) {
			startLineType = value;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.STARTLINETYPE));
		}
	}

	public void setEndLineType(ArrowHeadType value) {
		if (endLineType != value) {
			endLineType = value;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.ENDLINETYPE));
		}
	}

	private ConnectorType connectorType = ConnectorType.STRAIGHT;

	public void setConnectorType(ConnectorType type) {
		if (connectorType == null) {
			throw new IllegalArgumentException();
		}
		if (!connectorType.equals(type)) {
			connectorType = type;
			// TODO: create a static property for connector type, linestyle is not the
			// correct mapping
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		}
	}

	public ConnectorType getConnectorType() {
		return connectorType;
	}

//TODO: end of new elements
	protected List<Anchor> anchors = new ArrayList<Anchor>();

	/**
	 * Get the anchors for this line.
	 * 
	 * @return A list with the anchors, or an empty list, if no anchors are defined
	 */
	public List<Anchor> getAnchors() {
		return anchors;
	}

	/**
	 * Add a new anchor to this line at the given position.
	 * 
	 * @param position The relative position on the line, between 0 (start) to 1
	 *                 (end).
	 */
	public Anchor addAnchor(double position) {
		if (position < 0 || position > 1) {
			throw new IllegalArgumentException("Invalid position value '" + position + "' must be between 0 and 1");
		}
		Anchor anchor = new Anchor(position);
		anchors.add(anchor);
		// No property for anchor, use LINESTYLE as dummy property to force redraw on
		// line
		fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		return anchor;
	}

	/**
	 * Remove the given anchor
	 */
	public void removeAnchor(Anchor anchor) {
		if (anchors.remove(anchor)) {
			// No property for anchor, use LINESTYLE as dummy property to force redraw on
			// line
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		}
	}

	protected Color color = new Color(0, 0, 0);

	public Color getColor() {
		return color;
	}

	public void setColor(Color v) {
		if (v == null)
			throw new IllegalArgumentException();
		if (color != v) {
			color = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.COLOR));
		}
	}

	/**
	 * a fillcolor of null is equivalent to transparent.
	 */
	protected Color fillColor = null;

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color v) {
		if (fillColor != v) {
			fillColor = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FILLCOLOR));
		}
	}

	/**
	 * checks if fill color is equal to null or the alpha value is equal to 0.
	 */
	public boolean isTransparent() {
		return fillColor == null || fillColor.getAlpha() == 0;
	}

	/**
	 * sets the alpha component of fillColor to 0 if true sets the alpha component
	 * of fillColor to 255 if true
	 */
	public void setTransparent(boolean v) {
		if (isTransparent() != v) {
			if (fillColor == null) {
				fillColor = Color.WHITE;
			}
			int alpha = v ? 0 : 255;
			fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);

			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.TRANSPARENT));
		}
	}

	// general
	List<Comment> comments = new ArrayList<Comment>();

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> value) {
		if (comments != value) {
			comments = value;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.COMMENTS));
		}
	}

	public void addComment(String comment, String source) {
		addComment(new Comment(comment, source));
	}

	public void addComment(Comment comment) {
		comments.add(comment);
		fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.COMMENTS));
	}

	public void removeComment(Comment comment) {
		comments.remove(comment);
		fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.COMMENTS));
	}

	/**
	 * Finds the first comment with a specific source
	 */
	public String findComment(String source) {
		for (Comment c : comments) {
			if (source.equals(c.commentSource)) {
				return c.commentText;
			}
		}
		return null;
	}

	protected String setIdentifier = "";

	public String getIdentifier() {
		return setIdentifier;
	}

	public void setIdentifier(String v) {
		if (v == null)
			throw new IllegalArgumentException();
		v = v.trim();
		if (!Utils.stringEquals(setIdentifier, v)) {
			setIdentifier = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.GENEID));
		}
	}

	protected String dataNodeType = "Unknown";

	public String getDataNodeType() {
		return dataNodeType;
	}

	public void setDataNodeType(DataNodeType type) {
		setDataNodeType(type.getName());
	}

	public void setDataNodeType(String v) {
		if (v == null)
			throw new IllegalArgumentException();
		if (!Utils.stringEquals(dataNodeType, v)) {
			dataNodeType = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.TYPE));
		}
	}

	/**
	 * The pathway datasource
	 */
	protected DataSource dataSource = null;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource v) {
		if (dataSource != v) {
			dataSource = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.DATASOURCE));
		}
	}

	/**
	 * returns GeneID and datasource combined in an Xref. Only meaningful for
	 * datanodes.
	 *
	 * Same as new Xref ( pathwayElement.getGeneID(), pathwayElement.getDataSource()
	 * );
	 */
	public Xref getXref() {
		// TODO: Store Xref by default, derive setGeneID and dataSource from it.
		return new Xref(setIdentifier, dataSource);
	}

	protected double centerX = 0;

	public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double v) {
		if (centerX != v) {
			centerX = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	protected double centerY = 0;

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double v) {
		if (centerY != v) {
			centerY = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	protected double width = 0;

	public double getWidth() {
		return width;
	}

	public void setWidth(double v) {
		if (width < 0) {
			throw new IllegalArgumentException("Tried to set dimension < 0: " + v);
		}
		if (width != v) {
			width = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	protected double height = 0;

	public double getHeight() {
		return height;
	}

	public void setHeight(double v) {
		if (width < 0) {
			throw new IllegalArgumentException("Tried to set dimension < 0: " + v);
		}
		if (height != v) {
			height = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	// starty for shapes
	public double getTop() {
		return centerY - height / 2;
	}

	public void setTop(double v) {
		centerY = v + height / 2;
		fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
	}

	// startx for shapes
	public double getLeft() {
		return centerX - width / 2;
	}

	public void setLeft(double v) {
		centerX = v + width / 2;
		fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
	}

	protected IShape shapeType = ShapeType.RECTANGLE;

	public IShape getShapeType() {
		return shapeType;
	}

	public void setShapeType(IShape v) {
		if (shapeType != v) {
			shapeType = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.SHAPETYPE));
		}
	}

	public void setOrientation(int orientation) {
		switch (orientation) {
		case OrientationType.TOP:
			setRotation(0);
			break;
		case OrientationType.LEFT:
			setRotation(Math.PI * (3.0 / 2));
			break;
		case OrientationType.BOTTOM:
			setRotation(Math.PI);
			break;
		case OrientationType.RIGHT:
			setRotation(Math.PI / 2);
			break;
		}
	}

	public int getOrientation() {
		double r = rotation / Math.PI;
		if (r < 1.0 / 4 || r >= 7.0 / 4)
			return OrientationType.TOP;
		if (r > 5.0 / 4 && r <= 7.0 / 4)
			return OrientationType.LEFT;
		if (r > 3.0 / 4 && r <= 5.0 / 4)
			return OrientationType.BOTTOM;
		if (r > 1.0 / 4 && r <= 3.0 / 4)
			return OrientationType.RIGHT;
		return 0;
	}

	protected double rotation = 0; // in radians

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double v) {
		if (rotation != v) {
			rotation = v;

			// Rotation is not stored for State, so we use a dynamic property.
			// TODO: remove after next GPML update.
//			if (objectType == ObjectType.STATE && v != 0) {
//				setDynamicProperty(VState.ROTATION_KEY, "" + v);
//			}

			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}

	}

	/**
	 * Get the rectangular bounds of the object after rotation is applied
	 */
	public Rectangle2D getRotatedBounds() {
		Rectangle2D bounds = getBounds();
		AffineTransform t = new AffineTransform();
		t.rotate(getRotation(), getCenterX(), getCenterY());
		bounds = t.createTransformedShape(bounds).getBounds2D();
		return bounds;
	}

	/**
	 * Get the rectangular bounds of the object without rotation taken into accound
	 */
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(getLeft(), getTop(), getWidth(), getHeight());
	}

	// for labels
	protected boolean fontWeight = false;

	public boolean getFontWeight() {
		return fontWeight;
	}

	public void setFontWeight(boolean v) {
		if (fontWeight != v) {
			fontWeight = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FONTWEIGHT));
		}
	}

	protected boolean fontStrikethru = false;

	public boolean getFontStrikethru() {
		return fontStrikethru;
	}

	public void setFontStrikethru(boolean v) {
		if (fontStrikethru != v) {
			fontStrikethru = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FONTSTYLE));
		}
	}

	protected boolean fontDecoration = false;

	public boolean getFontDecoration() {
		return fontDecoration;
	}

	public void setFontDecoration(boolean v) {
		if (fontDecoration != v) {
			fontDecoration = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FONTSTYLE));
		}
	}

	protected boolean fontStyle = false;

	public boolean getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(boolean v) {
		if (fontStyle != v) {
			fontStyle = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FONTSTYLE));
		}
	}

	protected String fontName = "Arial";

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String v) {
		if (v == null)
			throw new IllegalArgumentException();
		if (!Utils.stringEquals(fontName, v)) {
			fontName = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FONTNAME));
		}
	}

	protected String textLabel = "";

	public String getTextLabel() {
		return textLabel;
	}

	public void setTextLabel(String v) {
		String input = (v == null) ? "" : v;
		if (!Utils.stringEquals(textLabel, input)) {
			textLabel = input;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.TEXTLABEL));
		}
	}

	protected String href = "";

	public String getHref() {
		return href;
	}

	public void setHref(String v) {
		String input = (v == null) ? "" : v;
		if (!Utils.stringEquals(href, input)) {
			href = input;
//			if (PreferenceManager.getCurrent() == null) { TODO 
//				PreferenceManager.init();
//			}
//			setColor(PreferenceManager.getCurrent().getColor(GlobalPreference.COLOR_LINK));
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.HREF));
		}
	}

	private double lineWidth = 1.0;

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double v) {
		if (lineWidth != v) {
			lineWidth = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LINETHICKNESS));
		}
	}

	protected double fontSize = M_INITIAL_FONTSIZE;

	public double getFontSize() {
		return fontSize;
	}

	public void setFontSize(double v) {
		if (fontSize != v) {
			fontSize = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.FONTSIZE));
		}
	}

	protected String title = "untitled";

	public String getTitle() {
		return title;
	}

	public void setTitle(String v) {
		if (v == null)
			throw new IllegalArgumentException();

		if (!Utils.stringEquals(title, v)) {
			title = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.MAPINFONAME));
		}
	}

	protected String organism = null;

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String v) {
		if (!Utils.stringEquals(organism, v)) {
			organism = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.ORGANISM));
		}
	}

	protected String source = null;

	public String getSource() {
		return source;
	}

	public void setSouce(String v) {
		if (!Utils.stringEquals(source, v)) {
			source = v;
			fireObjectModifiedEvent(
					PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.MAPINFO_DATASOURCE));
		}
	}

	protected VAlignType vAlign = VAlignType.MIDDLE;

	public void setVAlign(VAlignType v) {
		if (vAlign != v) {
			vAlign = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.VALIGN));
		}
	}

	public VAlignType getVAlign() {
		return vAlign;
	}

	protected HAlignType hAlign = HAlignType.CENTER;

	public void setHAlign(HAlignType v) {
		if (hAlign != v) {
			hAlign = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.ALIGN));
		}
	}

	public HAlignType getHAlign() {
		return hAlign;
	}

	protected String version = null;

	public String getVersion() {
		return version;
	}

	public void setVersion(String v) {
		if (!Utils.stringEquals(version, v)) {
			version = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.VERSION));
		}
	}

	protected String author = null;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String v) {
		if (!Utils.stringEquals(author, v)) {
			author = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.AUTHOR));
		}
	}

	protected String maintainer = null;

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(String v) {
		if (!Utils.stringEquals(maintainer, v)) {
			maintainer = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.MAINTAINED_BY));
		}
	}

	protected String email = null;

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		if (!Utils.stringEquals(email, v)) {
			email = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.EMAIL));
		}
	}

	protected String copyright = null;

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String v) {
		if (!Utils.stringEquals(copyright, v)) {
			copyright = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LICENSE));
		}
	}

	protected String lastModified = null;

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String v) {
		if (!Utils.stringEquals(lastModified, v)) {
			lastModified = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LAST_MODIFIED));
		}
	}

	/**
	 * Calculates the drawing size on basis of the location and size of the
	 * containing pathway elements
	 * 
	 * @return The drawing size
	 */
	public double[] getBoardSize() {
		return parent.getMBoardSize();
	}

	public double getBoardWidth() {
		return getBoardSize()[0];
	}

	public double getBoardHeight() {
		return getBoardSize()[1];
	}

	/* AP20070508 */
	protected String groupId;

	protected String elementId;

	protected String groupRef;

	protected GroupType groupType;

	public String doGetElementId() {
		return elementId;
	}

	public String getGroupRef() {
		return groupRef;
	}

	public void setGroupRef(String s) {
		if (groupRef == null || !groupRef.equals(s)) {
			if (parent != null) {
				if (groupRef != null) {
					parent.removeGroupRef(groupRef, this);
				}
				// Check: move add before remove??
				if (s != null) {
					parent.addGroupRef(s, this);
				}
			}
			groupRef = s;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.GROUPREF));
		}
	}

	public String getGroupId() {
		return groupId;
	}

	public String createGroupId() {
		if (groupId == null) {
			setGroupId(parent.getUniqueGroupId());
		}
		return groupId;
	}

	public void setGroupType(GroupType gs) {
		if (groupType != gs) {
			groupType = gs;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.GROUPSTYLE));
		}
	}

	public GroupType getGroupType() {
		if (groupType == null) {
			groupType = GroupType.NONE;
		}
		return groupType;
	}

	/**
	 * Set groupId. This id must be any string unique within the Pathway object
	 *
	 * @see PathwayModel#getUniqueId(java.util.Set)
	 */
	public void setGroupId(String w) {
		if (groupId == null || !groupId.equals(w)) {
			if (parent != null) {
				if (groupId != null) {
					parent.removeGroupId(groupId);
				}
				// Check: move add before remove??
				if (w != null) {
					parent.addGroupId(w, this);
				}
			}
			groupId = w;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.GROUPID));
		}

	}

	protected String elementRef = null;

	/** graphRef property, used by Modification */
	public String getElementRef() {
		return elementRef;
	}

	/**
	 * set graphRef property, used by State The new graphRef should exist and point
	 * to an existing DataNode
	 */
	public void setElementRef(String value) {
		// TODO: check that new graphRef exists and that it points to a DataNode
		if (!(elementRef == null ? value == null : elementRef.equals(value))) {
			elementRef = value;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.GRAPHREF));
		}
	}

	private double relX;

	/**
	 * relX property, used by State. Should normally be between -1.0 and 1.0, where
	 * 1.0 corresponds to the edge of the parent object
	 */
	public double getRelX() {
		return relX;
	}

	/**
	 * See getRelX
	 */
	public void setRelX(double value) {
		if (relX != value) {
			relX = value;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	private double relY;

	/**
	 * relX property, used by State. Should normally be between -1.0 and 1.0, where
	 * 1.0 corresponds to the edge of the parent object
	 */
	public double getRelY() {
		return relY;
	}

	/**
	 * See getRelX
	 */
	public void setRelY(double value) {
		if (relY != value) {
			relY = value;
			fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(this));
		}
	}

	public String getElementId() {
		return elementId;
	}

	/**
	 * Set graphId. This id must be any string unique within the Pathway object
	 *
	 * @see PathwayModel#getUniqueId(java.util.Set)
	 */
	public void setElementId(String v) {
		GraphLink.setGraphId(v, this, parent);
		elementId = v;
		fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.GRAPHID));
	}

	public String setGeneratedElementId() {
		setElementId(parent.getUniqueGraphId());
		return elementId;
	}

	public String getStartElementRef() {
		return linePoints.get(0).getElementRef();
	}

	public void setStartElementRef(String ref) {
		LinePoint start = linePoints.get(0);
		start.setElementRef(ref);
	}

	public String getEndElementRef() {
		return linePoints.get(linePoints.size() - 1).getElementRef();
	}

	public void setEndElementRef(String ref) {
		LinePoint end = linePoints.get(linePoints.size() - 1);
		end.setElementRef(ref);
	}

	private BiopaxReferenceManager bpRefMgr;

	public BiopaxReferenceManager getBiopaxReferenceManager() {
		if (bpRefMgr == null) {
			bpRefMgr = new BiopaxReferenceManager(this);
		}
		return bpRefMgr;
	}

	protected List<String> biopaxRefs = new ArrayList<String>();

	public List<String> getBiopaxRefs() {
		return biopaxRefs;
	}

	public void setBiopaxRefs(List<String> refs) {
		if (refs != null && !biopaxRefs.equals(refs)) {
			biopaxRefs = refs;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.BIOPAXREF));
		}
	}

	public void addBiopaxRef(String ref) {
		if (ref != null && !biopaxRefs.contains(ref)) {
			biopaxRefs.add(ref);
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.BIOPAXREF));
		}
	}

	public void removeBiopaxRef(String ref) {
		if (ref != null) {
			boolean changed = biopaxRefs.remove(ref);
			if (changed) {
				fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.BIOPAXREF));
			}
		}
	}

	public PathwayObject[] splitLine() {
		double centerX = (getStartLinePointX() + getEndLinePointX()) / 2;
		double centerY = (getStartLinePointY() + getEndLinePointY()) / 2;
		PathwayObject l1 = new PathwayObject(ObjectType.LINE);
		l1.copyValuesFrom(this);
		l1.setStartLinePointX(getStartLinePointX());
		l1.setStartLinePointY(getStartLinePointY());
		l1.setEndLinePointX(centerX);
		l1.setEndLinePointY(centerY);
		PathwayObject l2 = new PathwayObject(ObjectType.LINE);
		l2.copyValuesFrom(this);
		l2.setStartLinePointX(centerX);
		l2.setStartLinePointY(centerY);
		l2.setEndLinePointX(getEndLinePointX());
		l2.setEndLinePointY(getEndLinePointY());
		return new PathwayObject[] { l1, l2 };
	}

	int noFire = 0;

	public void dontFireEvents(int times) {
		noFire = times;
	}

	private Set<PathwayObjectListener> listeners = new HashSet<PathwayObjectListener>();

	public void addListener(PathwayObjectListener v) {
		if (!listeners.contains(v))
			listeners.add(v);
	}

	public void removeListener(PathwayObjectListener v) {
		listeners.remove(v);
	}

	public void fireObjectModifiedEvent(PathwayObjectEvent e) {
		if (noFire > 0) {
			noFire -= 1;
			return;
		}
		if (parent != null)
			parent.childModified(e);
		for (PathwayObjectListener g : listeners) {
			g.gmmlObjectModified(e);
		}
	}

	/**
	 * This sets the object to a suitable default size.
	 *
	 * This method is intended to be called right after the object is placed on the
	 * drawing with a click.
	 */
	public void setInitialSize() {
		switch (objectType) {
		case SHAPE:
			if (shapeType == ShapeType.BRACE) {
				setWidth(M_INITIAL_BRACE_WIDTH);
				setHeight(M_INITIAL_BRACE_HEIGHT);
			} else if (shapeType == ShapeType.MITOCHONDRIA || lineStyle == LineStyleType.DOUBLE) {
				setWidth(M_INITIAL_CELLCOMP_WIDTH);
				setHeight(M_INITIAL_CELLCOMP_HEIGHT);
			} else if (shapeType == ShapeType.SARCOPLASMICRETICULUM || shapeType == ShapeType.ENDOPLASMICRETICULUM
					|| shapeType == ShapeType.GOLGIAPPARATUS) {
				setWidth(M_INITIAL_CELLCOMP_HEIGHT);
				setHeight(M_INITIAL_CELLCOMP_WIDTH);
			} else {
				setWidth(M_INITIAL_SHAPE_SIZE);
				setHeight(M_INITIAL_SHAPE_SIZE);
			}
			break;
		case DATANODE:
			setWidth(M_INITIAL_GENEPRODUCT_WIDTH);
			setHeight(M_INITIAL_GENEPRODUCT_HEIGHT);
			break;
		case LINE:
			setEndLinePointX(getStartLinePointX() + M_INITIAL_LINE_LENGTH);
			setEndLinePointY(getStartLinePointY() + M_INITIAL_LINE_LENGTH);
			break;
		case GRAPHLINE:
			setEndLinePointX(getStartLinePointX() + M_INITIAL_LINE_LENGTH);
			setEndLinePointY(getStartLinePointY() + M_INITIAL_LINE_LENGTH);
			break;
		case STATE:
			setWidth(M_INITIAL_STATE_SIZE);
			setHeight(M_INITIAL_STATE_SIZE);
			break;
		case LABEL:
			setWidth(M_INITIAL_LABEL_WIDTH);
			setHeight(M_INITIAL_LABEL_HEIGHT);
		}
	}

	public Set<LinkableFrom> getReferences() {
		return GraphLink.getReferences(this, parent);
	}

	public int compareTo(PathwayObject o) {
		int rez = getZOrder() - o.getZOrder();
		if (rez != 0) {
			return rez;
		}
		String a = getElementId();
		String b = o.getElementId();
		if (a == null) {
			if (b == null) {
				return 0;
			}
			return -1;
		}
		if (b == null) {
			return 1;
		}
		return a.compareTo(b);
	}

	public Point2D toAbsoluteCoordinate(Point2D p) {
		double x = p.getX();
		double y = p.getY();
		Rectangle2D bounds = getRotatedBounds();
		// Scale
		if (bounds.getWidth() != 0)
			x *= bounds.getWidth() / 2;
		if (bounds.getHeight() != 0)
			y *= bounds.getHeight() / 2;
		// Translate
		x += bounds.getCenterX();
		y += bounds.getCenterY();
		return new Point2D.Double(x, y);
	}

	/**
	 * @param mp a point in absolute model coordinates
	 * @returns the same point relative to the bounding box of this pathway element:
	 *          -1,-1 meaning the top-left corner, 1,1 meaning the bottom right
	 *          corner, and 0,0 meaning the center.
	 */
	public Point2D toRelativeCoordinate(Point2D mp) {
		double relX = mp.getX();
		double relY = mp.getY();
		Rectangle2D bounds = getRotatedBounds();
		// Translate
		relX -= bounds.getCenterX();
		relY -= bounds.getCenterY();
		// Scalebounds.getCenterX();
		if (relX != 0 && bounds.getWidth() != 0)
			relX /= bounds.getWidth() / 2;
		if (relY != 0 && bounds.getHeight() != 0)
			relY /= bounds.getHeight() / 2;
		return new Point2D.Double(relX, relY);
	}

	public void printRefsDebugInfo() {
		System.err.println(objectType + " " + getElementId());
		if (this instanceof LineElement) {
			for (LinePoint p : getLinePoints()) {
				System.err.println("  p: " + p.getElementId());
			}
			for (Anchor a : getAnchors()) {
				System.err.println("  a: " + a.getElementId());
			}
		}
		if (this instanceof State) {
			System.err.println("  " + getElementRef());
		}
	}
}
