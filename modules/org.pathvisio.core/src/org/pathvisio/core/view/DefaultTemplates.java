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

import java.awt.Color;
import java.net.URL;

import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.Resources;
import org.pathvisio.libgpml.model.State;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.PathwayElement.Anchor;
import org.pathvisio.libgpml.model.shape.IShape;
import org.pathvisio.libgpml.model.shape.MIMShapes;
import org.pathvisio.libgpml.model.type.CellularComponentType;
import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.LineStyleType;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.type.ShapeType;

/**
 * Contains a set of templates, patterns of PathwayElements that can be added to
 * a Pathway, including default values.
 */
public abstract class DefaultTemplates {
	public final static Color COLOR_METABOLITE = Color.BLUE;
	public final static Color COLOR_PATHWAY = new Color(20, 150, 30);
	public final static Color COLOR_LABEL = Color.DARK_GRAY;

	/**
	 * Abstract base for templates that only add a single PathwayElement to a
	 * Pathway
	 */
	static abstract class SingleElementTemplate implements Template {
		PathwayElement lastAdded;

		protected void addElement(PathwayElement e, PathwayModel p) {
			p.add(e);
			lastAdded = e;
		}

		/**
		 * Default implementation returns the view of the last added object
		 */
		public VPathwayElement getDragElement(VPathwayModel vp) {
			if (lastAdded != null) {
				Graphics g = vp.getPathwayElementView(lastAdded);
				if (g == null) {
					throw new IllegalArgumentException("Given VPathway doesn't contain last added element");
				}
				return g;
			}
			return null; // No last object
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return Resources.getResourceURL("new" + getName().toLowerCase() + ".gif");
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}

	/**
	 * Template for adding a single line denoting an interaction to a Pathway.
	 */
	public static class LineTemplate extends SingleElementTemplate {
		int style;
		ArrowHeadType startType;
		ArrowHeadType endType;
		ConnectorType connectorType;
		String name;

		public LineTemplate(String name, int style, ArrowHeadType startType, ArrowHeadType endType, ConnectorType connectorType) {
			this.style = style;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.LINE);
			e.setStartLinePointX(mx);
			e.setStartLinePointY(my);
			e.setEndLinePointX(mx);
			e.setEndLinePointY(my);
			e.setLineStyle(style);
			e.setStartLineType(startType);
			e.setEndLineType(endType);
			e.setConnectorType(connectorType);
			addElement(e, p);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			VLineElement l = (VLineElement) super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Template for adding a Label to a Pathway
	 */
	public static class LabelTemplate extends SingleElementTemplate {
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.LABEL);
			e.setCenterX(mx);
			e.setCenterY(my);
			e.setInitialSize();
			e.setElementId(p.getUniqueGraphId());
			e.setTextLabel("Label");
			e.setColor(COLOR_LABEL);
			addElement(e, p);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			return null; // Don't drag label on insert
		}

		public String getName() {
			return "Label";
		}
	}

	/**
	 * Template for adding a DataNode to a Pathway. Pass a DataNodeType upon
	 * creation
	 */
	public static class DataNodeTemplate extends SingleElementTemplate {
		DataNodeType type;

		public DataNodeTemplate(DataNodeType type) {
			this.type = type;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			e.setCenterX(mx);
			e.setCenterY(my);
			e.setWidth(1);
			e.setHeight(1);
			e.setRotation(0);
			if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.DATANODES_ROUNDED)) {
				e.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			}
			e.setElementId(p.getUniqueGraphId());
			e.setDataNodeType(type);

			// Default colors for different types
			if (type.equals(DataNodeType.METABOLITE)) {
				e.setColor(COLOR_METABOLITE);
			} else if (type.equals(DataNodeType.PATHWAY)) {
				e.setColor(COLOR_PATHWAY);
				e.setFontSize(12);
				e.setFontWeight(true);
				e.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			}

			e.setTextLabel(type.toString());
			addElement(e, p);
			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			VDataNode g = (VDataNode) super.getDragElement(vp);
			return g.handleSE;
		}

		public String getName() {
			return type.toString();
		}
	}

	/**
	 * Template for adding a Shape to a Pathway. Pass a ShapeType upon creation.
	 */
	public static class ShapeTemplate extends SingleElementTemplate {
		IShape type;

		public ShapeTemplate(IShape type) {
			this.type = type;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.SHAPE);
			e.setShapeType(type);
			e.setCenterX(mx);
			e.setCenterY(my);
			e.setWidth(1);
			e.setHeight(1);
			e.setRotation(0);
			e.setElementId(p.getUniqueGraphId());
			addElement(e, p);

			// brace
//			gdata.setOrientation(OrientationType.RIGHT);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			GraphicsShape s = (GraphicsShape) super.getDragElement(vp);
			return s.handleSE;
		}

		public String getName() {
			return type.toString();
		}
	}

	/**
	 * Template for adding a Graphical line to a Pathway.
	 */
	public static class GraphicalLineTemplate extends SingleElementTemplate {
		int style;
		ArrowHeadType startType;
		ArrowHeadType endType;
		ConnectorType connectorType;
		String name;

		public GraphicalLineTemplate(String name, int style, ArrowHeadType startType, ArrowHeadType endType,
				ConnectorType connectorType) {
			this.style = style;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.GRAPHLINE);
			e.setStartLinePointX(mx);
			e.setStartLinePointY(my);
			e.setEndLinePointX(mx);
			e.setEndLinePointY(my);
			e.setLineStyle(style);
			e.setStartLineType(startType);
			e.setEndLineType(endType);
			e.setConnectorType(connectorType);
			addElement(e, p);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			VLineElement l = (VLineElement) super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Template for adding a Cellular Compartment Shape to a Pathway. Pass a
	 * ShapeType upon creation.
	 */
	public static class CellularComponentTemplate extends SingleElementTemplate {
		ShapeType type;
		CellularComponentType ccType;

		public CellularComponentTemplate(ShapeType type, CellularComponentType ccType) {
			this.type = type;
			this.ccType = ccType;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.SHAPE);
			e.setShapeType(type);
			e.setCenterX(mx);
			e.setCenterY(my);
			e.setWidth(1);
			e.setHeight(1);
			e.setRotation(0);
			e.setColor(Color.LIGHT_GRAY);
			e.setLineWidth(3.0);
			if (ccType.equals(CellularComponentType.CELL) || ccType.equals(CellularComponentType.NUCLEUS)
					|| ccType.equals(CellularComponentType.ORGANELLE)) {
				e.setLineStyle(LineStyleType.DOUBLE);
			} else if (ccType.equals(CellularComponentType.CYTOSOL)
					|| ccType.equals(CellularComponentType.EXTRACELLULAR)
					|| ccType.equals(CellularComponentType.MEMBRANE)) {
				e.setLineStyle(LineStyleType.DASHED);
				e.setLineWidth(1.0);
			}
			e.setElementId(p.getUniqueGraphId());
			e.setDynamicProperty(CellularComponentType.CELL_COMPONENT_KEY, ccType.toString());
			addElement(e, p);
			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			GraphicsShape s = (GraphicsShape) super.getDragElement(vp);
			return s.handleSE;
		}

		public String getName() {
			return ccType.getGpmlName();
		}
	}

	/**
	 * Template for an interaction, two datanodes with a connecting line.
	 */
	public static class InteractionTemplate implements Template {
		final static int OFFSET_LINE = 5;
		PathwayElement lastStartNode;
		PathwayElement lastEndNode;
		PathwayElement lastLine;

		ArrowHeadType endType;
		ArrowHeadType startType;

		int lineStyle;

		public InteractionTemplate() {
			endType = ArrowHeadType.LINE;
			startType = ArrowHeadType.LINE;
			lineStyle = LineStyleType.SOLID;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			// Add two GeneProduct DataNodes, connected by a line
			Template dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastStartNode = dnt.addElements(p, mx, my)[0];
			lastStartNode.setInitialSize();
			lastEndNode = dnt.addElements(p, mx + 2 * lastStartNode.getWidth(), my)[0];
			lastEndNode.setInitialSize();

			Template lnt = new LineTemplate("defaultline", lineStyle, startType, endType, ConnectorType.STRAIGHT);
			lastLine = lnt.addElements(p, mx, my)[0];
			lastLine.getStartLinePoint().linkTo(lastStartNode, 1, 0);
			lastLine.getEndLinePoint().linkTo(lastEndNode, -1, 0);

			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		public VPathwayElement getDragElement(VPathwayModel vp) {
			return null;
		}

		public String getName() {
			return "interaction";
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return Resources.getResourceURL("new" + getName().toLowerCase() + ".gif");
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}

	/**
	 * Template for an inhibition interaction, two datanodes with a MIM_INHIBITION
	 * line.
	 */
	public static class InhibitionInteractionTemplate extends InteractionTemplate {
		@Override
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndLineType(MIMShapes.MIM_INHIBITION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		@Override
		public String getName() {
			return "inhibition interaction";
		}
	}

	/**
	 * Template for a stimulation interaction, two datanodes with a MIM_STIMULATION
	 * line.
	 */
	public static class StimulationInteractionTemplate extends InteractionTemplate {
		@Override
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndLineType(MIMShapes.MIM_STIMULATION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		@Override
		public String getName() {
			return "stimulation interaction";
		}
	}

	/**
	 * Template for a phosphorylation interaction, two Protein Datanodes with a
	 * MIM_MODIFICATION line.
	 */

	public static class PhosphorylationTemplate extends InteractionTemplate {
		// static final double OFFSET_CATALYST = 50;
		PathwayElement lastPhosphorylation;
		// PathwayElement lastPhosLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastStartNode.setDataNodeType(DataNodeType.PROTEIN);
			lastEndNode.setDataNodeType(DataNodeType.PROTEIN);
			lastStartNode.setTextLabel("Protein");
			lastEndNode.setTextLabel("P-Protein");
			lastLine.setEndLineType(MIMShapes.MIM_MODIFICATION);

			PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.STATE);
			elt.setInitialSize();
			elt.setTextLabel("P");
			((State) elt).linkTo(lastEndNode, 1.0, 1.0);
			elt.setShapeType(ShapeType.OVAL);
			p.add(elt);
			elt.setGeneratedElementId();

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine };
		}

		public String getName() {
			return "Phosphorylation";
		}
	}

	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a
	 * GeneProduct (enzyme) pointing to an anchor on that arrow.
	 */
	public static class ReactionTemplate extends InteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		PathwayElement lastCatalyst;
		PathwayElement lastCatLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			Template dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getWidth(), my - OFFSET_CATALYST)[0];
			lastCatalyst.setInitialSize();
			lastCatalyst.setTextLabel("Catalyst");

			lastStartNode.setDataNodeType(DataNodeType.METABOLITE);
			lastStartNode.setColor(COLOR_METABOLITE);
			lastStartNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastStartNode.setTextLabel("Substrate");

			lastEndNode.setDataNodeType(DataNodeType.METABOLITE);
			lastEndNode.setColor(COLOR_METABOLITE);
			lastEndNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastEndNode.setTextLabel("Product");

			lastLine.setEndLineType(MIMShapes.MIM_CONVERSION);
			Anchor anchor = lastLine.addAnchor(0.5);

			Template lnt = new LineTemplate("line", LineStyleType.SOLID, ArrowHeadType.LINE, ArrowHeadType.LINE,
					ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

			lastCatLine.getStartLinePoint().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getEndLinePoint().linkTo(anchor, 0, 0);
			lastCatLine.setEndLineType(MIMShapes.MIM_CATALYSIS);

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst };
		}

		public String getName() {
			return "reaction";
		}
	}

	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a
	 * GeneProduct (enzyme) pointing to an anchor on that arrow.
	 */
	public static class ReversibleReactionTemplate extends InteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		PathwayElement lastCatalyst;
		PathwayElement lastCatalyst2;
		PathwayElement lastCatLine;
		PathwayElement lastCatLine2;
		PathwayElement lastReverseLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			Template dnt = new DataNodeTemplate(DataNodeType.PROTEIN);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getWidth(), my - OFFSET_CATALYST)[0];
			lastCatalyst.setInitialSize();
			lastCatalyst.setTextLabel("Catalyst 1");

			lastCatalyst2 = dnt.addElements(p, mx + lastStartNode.getWidth(), my + OFFSET_CATALYST)[0];
			lastCatalyst2.setInitialSize();
			lastCatalyst2.setTextLabel("Catalyst 2");

			lastStartNode.setDataNodeType(DataNodeType.METABOLITE);
			lastStartNode.setColor(COLOR_METABOLITE);
			lastStartNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastStartNode.setTextLabel("Metabolite 1");

			lastEndNode.setDataNodeType(DataNodeType.METABOLITE);
			lastEndNode.setColor(COLOR_METABOLITE);
			lastEndNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastEndNode.setTextLabel("Metabolite 2");
			lastLine.setEndLineType(MIMShapes.MIM_CONVERSION);

			Anchor anchor = lastLine.addAnchor(0.5);

			Template lnt = new LineTemplate("line", LineStyleType.SOLID, ArrowHeadType.LINE, ArrowHeadType.LINE,
					ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

			lastCatLine.getStartLinePoint().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getEndLinePoint().linkTo(anchor, 0, 0);
			lastCatLine.setEndLineType(MIMShapes.MIM_CATALYSIS);

			Template rev = new LineTemplate("line", LineStyleType.SOLID, ArrowHeadType.LINE, ArrowHeadType.LINE,
					ConnectorType.STRAIGHT);
			lastReverseLine = rev.addElements(p, mx, my)[0];

			lastReverseLine.getStartLinePoint().linkTo(lastEndNode, -1, 0.5);
			lastReverseLine.getEndLinePoint().linkTo(lastStartNode, 1, 0.5);
			lastReverseLine.setEndLineType(MIMShapes.MIM_CONVERSION);

			Anchor anchor2 = lastReverseLine.addAnchor(0.5);

			Template lnt2 = new LineTemplate("line", LineStyleType.SOLID, ArrowHeadType.LINE, ArrowHeadType.LINE,
					ConnectorType.STRAIGHT);
			lastCatLine2 = lnt2.addElements(p, mx, my)[0];

			lastCatLine2.getStartLinePoint().linkTo(lastCatalyst2, 0, -1);
			lastCatLine2.getEndLinePoint().linkTo(anchor2, 0, 0);
			lastCatLine2.setEndLineType(MIMShapes.MIM_CATALYSIS);

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst, lastCatalyst2 }; // These
																												// elements
																												// are
																												// selected
																												// in
																												// PV,
																												// so
																												// users
																												// can
																												// move
																												// them
																												// around.
		}

		public String getName() {
			return "ReversibleReaction";
		}
	}

}
