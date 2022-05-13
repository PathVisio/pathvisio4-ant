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

import java.awt.Color;
import java.net.URL;

import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.LineStyleType;
import org.pathvisio.libgpml.model.type.ShapeType;
import org.pathvisio.libgpml.model.type.AnchorShapeType;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.DataNode.State;
import org.pathvisio.libgpml.model.GraphicalLine;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.Shape;
import org.pathvisio.libgpml.model.shape.IShape;
import org.pathvisio.libgpml.model.LineElement;
import org.pathvisio.libgpml.model.LineElement.Anchor;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.type.StateType;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ColorPalette;
import org.pathvisio.core.util.Resources;
import org.pathvisio.core.util.Theme;

/**
 * Contains a set of templates, patterns of PathwayElements that can be added to
 * a Pathway, including default values.
 */
public abstract class DefaultTemplates {

	private final static Theme theme = Theme.WIKIPATHWAYS; // default theme

	/* Some default colors */
	private final static Color COLOR_LABEL = ColorPalette.WP_BLACK;

	/* Initial sizes */
	private static final double DATANODE_WIDTH = 90; // NB: "DATANODE" used to be named "GENEPRODUCT"
	private static final double DATANODE_HEIGHT = 25;
	private static final double STATE_SIZE = 15;
	private static final double LABEL_WIDTH = 90;
	private static final double LABEL_HEIGHT = 25;
	private static final double LINE_LENGTH = 30;

	/* Initial Shape sizes */
	private static final double BRACE_HEIGHT = 15;
	private static final double BRACE_WIDTH = 60;
	private static final double SHAPE_SIZE_30 = 30;
	private static final double SHAPE_SIZE_50 = 50;
	private static final double SHAPE_SIZE_100 = 100;
	private static final double SHAPE_SIZE_120 = 120;
	private static final double SHAPE_SIZE_150 = 150;
	private static final double SHAPE_SIZE_185 = 185;
	private static final double SHAPE_SIZE_200 = 200;

	/* Default Z-order values */
	public static final int Z_ORDER_GROUP = 0x1000;
	public static final int Z_ORDER_DATANODE = 0x8000;
	public static final int Z_ORDER_STATE = 0x8000 + 10;
	public static final int Z_ORDER_LABEL = 0x7000;
	public static final int Z_ORDER_SHAPE = 0x4000;
	public static final int Z_ORDER_LINE = 0x3000;
	public static final int Z_ORDER_DEFAULT = 0x0000; // default order of uninteresting elements.

	// ================================================================================
	// Common Methods
	// ================================================================================
	/**
	 * This sets the object to a suitable default size.
	 *
	 * This method is intended to be called right after the object is placed on the
	 * drawing with a click.
	 */
	public static void setInitialSize(PathwayElement o) {
		switch (o.getObjectType()) {
		case DATANODE:
			((DataNode) o).setWidth(DATANODE_WIDTH);
			((DataNode) o).setHeight(DATANODE_HEIGHT);
			break;
		case STATE:
			((State) o).setWidth(STATE_SIZE);
			((State) o).setHeight(STATE_SIZE);
			break;
		case LABEL:
			((Label) o).setWidth(LABEL_WIDTH);
			((Label) o).setHeight(LABEL_HEIGHT);
			break;
		case SHAPE:
			IShape type = ((Shape) o).getShapeType();
			if (type == ShapeType.BRACE) {
				((Shape) o).setWidth(BRACE_WIDTH);
				((Shape) o).setHeight(BRACE_HEIGHT);
			} else if (type == ShapeType.MITOCHONDRIA || type == ShapeType.CELL || type == ShapeType.NUCLEUS
					|| type == ShapeType.ORGANELLE || type == ShapeType.EXTRACELLULAR) {
				((Shape) o).setWidth(SHAPE_SIZE_200);
				((Shape) o).setHeight(SHAPE_SIZE_100);
			} else if (type == ShapeType.SARCOPLASMIC_RETICULUM || type == ShapeType.ENDOPLASMIC_RETICULUM
					|| type == ShapeType.GOLGI_APPARATUS) {
				((Shape) o).setWidth(SHAPE_SIZE_100);
				((Shape) o).setHeight(SHAPE_SIZE_200);
			} else if (type == ShapeType.CORONAVIRUS_ICON) {
				((Shape) o).setWidth(SHAPE_SIZE_50);
				((Shape) o).setHeight(SHAPE_SIZE_50);
			} else if (type == ShapeType.DNA_ICON || type == ShapeType.RNA_ICON) {
				((Shape) o).setWidth(SHAPE_SIZE_30);
				((Shape) o).setHeight(SHAPE_SIZE_100);
			} else if (type == ShapeType.CELL_ICON) {
				((Shape) o).setWidth(SHAPE_SIZE_150);
				((Shape) o).setHeight(SHAPE_SIZE_120);
			} else if (type == ShapeType.MEMBRANE_ICON) {
				((Shape) o).setWidth(SHAPE_SIZE_185);
				((Shape) o).setHeight(SHAPE_SIZE_120);
			} else {
				((Shape) o).setWidth(SHAPE_SIZE_30);
				((Shape) o).setHeight(SHAPE_SIZE_30);
			}
			break;
		case INTERACTION: // if Interaction OR Graphical Line
		case GRAPHLINE:
			((LineElement) o).setEndLinePointX(((LineElement) o).getStartLinePointX() + LINE_LENGTH);
			((LineElement) o).setEndLinePointY(((LineElement) o).getStartLinePointY() + LINE_LENGTH);
			break;
		default:
			break;
		}
	}

	// ================================================================================
	// SingleElementTemplate
	// ================================================================================
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
		@Override
		public VElement getDragElement(VPathwayModel vp) {
			if (lastAdded != null) {
				VPathwayObject g = vp.getPathwayElementView(lastAdded);
				if (g == null) {
					throw new IllegalArgumentException("Given VPathway doesn't contain last added element");
				}
				return g;
			}
			return null; // No last object
		}

		@Override
		public String getDescription() {
			return "Draw new " + getName();
		}

		@Override
		public URL getIconLocation() {
			return Resources.getResourceURL(
					getClass().getSimpleName().toLowerCase() + "/new" + getName().toLowerCase() + ".gif"); // TODO
		}

		@Override
		public void postInsert(PathwayElement[] newElements) {
		}
	}

	// ================================================================================
	// DataNodeTemplate
	// ================================================================================
	/**
	 * Template for adding a DataNode to a Pathway. Pass a DataNodeType upon
	 * creation.
	 */
	public static class DataNodeTemplate extends SingleElementTemplate {
		DataNodeType type;

		public DataNodeTemplate(DataNodeType type) {
			this.type = type;
		}

		@Override
		public DataNode[] addElements(PathwayModel p, double mx, double my) {
			// instantiate data node
			DataNode e = new DataNode(type.toString(), type);
			// set graphics
			theme.setInitialColors(e);
			setInitialShapeBorder(e);
			e.setCenterX(mx);
			e.setCenterY(my);
			setInitialSize(e);
			e.setZOrder(Z_ORDER_DATANODE);
			if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.DATANODES_ROUNDED)) {
				e.setShapeType(ShapeType.ROUNDED_RECTANGLE);// TODO what is this for???
			}
			// use addElement TODO
			addElement(e, p);
			return new DataNode[] { e };
		}

		@Override
		public VElement getDragElement(VPathwayModel vp) {
			VDataNode g = (VDataNode) super.getDragElement(vp);
			return g.handleSE;
		}

		@Override
		public String getName() {
			return type.toString();
		}

		/**
		 * Sets shape and border.
		 * 
		 * @param e the data node.
		 */
		public void setInitialShapeBorder(DataNode e) {
			DataNodeType type = e.getType();
			// concept datanodes
			if (type == DataNodeType.PATHWAY || type == DataNodeType.DISEASE || type == DataNodeType.PHENOTYPE
					|| type == DataNodeType.EVENT || type == DataNodeType.CELL_NODE || type == DataNodeType.ORGAN) {
				e.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			} else if (type == DataNodeType.ALIAS) {
				e.setShapeType(ShapeType.OVAL);
			} else if (type == DataNodeType.UNDEFINED) {
				e.setShapeType(ShapeType.ROUNDED_RECTANGLE);
				e.setBorderStyle(LineStyleType.DASHED);
			} else { // molecule datanodes
				e.setShapeType(ShapeType.RECTANGLE);
			}
		}

		@Override
		public URL getIconLocation() {
			// icons for data nodes are painted instead.
			return null;
		}

	}

	// ================================================================================
	// LabelTemplate
	// ================================================================================
	/**
	 * Template for adding a Label to a Pathway
	 */
	public static class LabelTemplate extends SingleElementTemplate {

		@Override
		public Label[] addElements(PathwayModel p, double mx, double my) {
			// instantiate a label
			Label e = new Label("Label");
			// set graphics
			e.setCenterX(mx);
			e.setCenterY(my);
			setInitialSize(e);
			// default font-Name/Style/Decoration/StrikeThru/Size, hAlign, vAlign
			e.setTextColor(COLOR_LABEL);
			// default borderColor, borderStyle, borderWidth, fillColor
			e.setShapeType(ShapeType.NONE);
			e.setZOrder(Z_ORDER_LABEL);
			// use addElement TODO
			addElement(e, p);
			return new Label[] { e };
		}

		@Override
		public VElement getDragElement(VPathwayModel vp) {
			return null; // Don't drag label on insert
		}

		@Override
		public String getName() {
			return "Label";
		}

		@Override
		public URL getIconLocation() {
			// icons for data nodes are painted instead.
			return null;
		}
	}

	// ================================================================================
	// ShapeTemplate
	// ================================================================================
	/**
	 * Template for adding a Shape to a Pathway. Pass a ShapeType upon creation.
	 */
	public static class ShapeTemplate extends SingleElementTemplate {
		ShapeType shapeType;

		public ShapeTemplate(ShapeType shapeType) {
			this.shapeType = shapeType;
		}

		public Shape[] addElements(PathwayModel p, double mx, double my) {
			// instantiate a shape (pathway element)
			Shape e = new Shape();
			e.setShapeType(shapeType);
			// set graphics
			e.setCenterX(mx);
			e.setCenterY(my);
			setInitialSize(e);
			setInitialBorderStyle(e);
			setInitialBorderWidth(e);
			theme.setInitialColors(e);
			e.setZOrder(Z_ORDER_SHAPE);
			addElement(e, p);
			return new Shape[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			VShapedElement s = (VShapedElement) super.getDragElement(vp);
			return s.handleSE;
		}

		public String getName() {
			return shapeType.toString();
		}

		/**
		 * @param shape
		 */
		public void setInitialBorderStyle(Shape e) {
			IShape type = e.getShapeType();
			if (shapeType == ShapeType.CELL || type == ShapeType.NUCLEUS || type == ShapeType.ORGANELLE) {
				e.setBorderStyle(LineStyleType.DOUBLE);
			} else if (type == ShapeType.CYTOSOL || type == ShapeType.EXTRACELLULAR || type == ShapeType.MEMBRANE) {
				e.setBorderStyle(LineStyleType.DASHED); // TODO membrane/cytosol never implemented?
			} else {
				e.setBorderStyle(LineStyleType.SOLID);
			}
		}

		/**
		 * @param shape
		 */
		public void setInitialBorderWidth(Shape e) {
			IShape type = e.getShapeType();
			if (Theme.CELL_COMPONENT_SET.contains(type)) {
				e.setBorderWidth(3);
			} else {
				e.setBorderWidth(1);
			}
		}

	}

	// ================================================================================
	// InteractionTemplate
	// ================================================================================
	/**
	 * Template for adding a single line denoting an interaction to a Pathway.
	 */
	public static class InteractionTemplate extends SingleElementTemplate {
		LineStyleType lineStyle;
		ArrowHeadType startType;
		ArrowHeadType endType;
		ConnectorType connectorType;
		String name;

		public InteractionTemplate(String name, LineStyleType lineStyle, ArrowHeadType startType, ArrowHeadType endType,
				ConnectorType connectorType) {
			this.lineStyle = lineStyle;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public Interaction[] addElements(PathwayModel p, double mx, double my) {
			// instantiates an interaction
			Interaction e = new Interaction();
			e.setStartLinePointX(mx);
			e.setStartLinePointY(my);
			e.setEndLinePointX(mx);
			e.setEndLinePointY(my);
			e.setStartArrowHeadType(startType);
			e.setEndArrowHeadType(endType);
			setInitialSize(e);
			// default lineColor, lineWidth
			e.setLineStyle(lineStyle);
			e.setConnectorType(connectorType);
			e.setZOrder(Z_ORDER_LINE);
			// use addElement TODO
			addElement(e, p);
			return new Interaction[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			VLineElement l = (VLineElement) super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName() {
			return name;
		}
	}

	// ================================================================================
	// GraphicalLineTemplate
	// ================================================================================
	/**
	 * Template for adding a Graphical line to a Pathway.
	 */
	public static class GraphicalLineTemplate extends SingleElementTemplate {
		LineStyleType lineStyle;
		ArrowHeadType startType;
		ArrowHeadType endType;
		ConnectorType connectorType;
		String name;

		public GraphicalLineTemplate(String name, LineStyleType lineStyle, ArrowHeadType startType,
				ArrowHeadType endType, ConnectorType connectorType) {
			this.lineStyle = lineStyle;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public GraphicalLine[] addElements(PathwayModel p, double mx, double my) {
			// instantiates a graphical line
			GraphicalLine e = new GraphicalLine();
			e.setStartLinePointX(mx);
			e.setStartLinePointY(my);
			e.setEndLinePointX(mx);
			e.setEndLinePointY(my);
			e.setStartArrowHeadType(startType);
			e.setEndArrowHeadType(endType);
			setInitialSize(e);
			// default lineColor, lineWidth
			e.setLineStyle(lineStyle);
			e.setConnectorType(connectorType);
			e.setZOrder(Z_ORDER_LINE);
			// use addElement TODO
			addElement(e, p);
			return new GraphicalLine[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			VLineElement l = (VLineElement) super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName() {
			return name;
		}
	}

	// ================================================================================
	// DataNodeInteractionTemplate
	// ================================================================================
	/**
	 * Template for an interaction, two datanodes with a connecting line.
	 */
	public static class DataNodeInteractionTemplate implements Template {
		final static int OFFSET_LINE = 5;
		DataNode lastStartNode;
		DataNode lastEndNode;
		Interaction lastLine;

		ArrowHeadType endType;
		ArrowHeadType startType;

		LineStyleType lineStyle;

		public DataNodeInteractionTemplate() {
			endType = ArrowHeadType.UNDIRECTED;
			startType = ArrowHeadType.UNDIRECTED;
			lineStyle = LineStyleType.SOLID;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			// Add two GeneProduct DataNodes, connected by a line
			DataNodeTemplate dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastStartNode = dnt.addElements(p, mx, my)[0];
//			setInitialSize(lastStartNode); TODO 
			lastEndNode = dnt.addElements(p, mx + 2 * lastStartNode.getWidth(), my)[0];
//			setInitialSize(lastEndNode); TODO 

			InteractionTemplate lnt = new InteractionTemplate("defaultline", lineStyle, startType, endType,
					ConnectorType.STRAIGHT);
			lastLine = lnt.addElements(p, mx, my)[0];
			lastLine.getStartLinePoint().linkTo(lastStartNode, 1, 0);
			lastLine.getEndLinePoint().linkTo(lastEndNode, -1, 0);

			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		public VElement getDragElement(VPathwayModel vp) {
			return null;
		}

		public String getName() {
			return "interaction";
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return Resources.getResourceURL("multitemplate/new" + getName().toLowerCase() + ".gif"); // TODO
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}

	// ================================================================================
	// InhibitionInteractionTemplate
	// ================================================================================
	/**
	 * Template for an inhibition interaction, two datanodes with a MIM_INHIBITION
	 * line.
	 */
	public static class InhibitionInteractionTemplate extends DataNodeInteractionTemplate {
		@Override
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndArrowHeadType(ArrowHeadType.INHIBITION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		@Override
		public String getName() {
			return "inhibition interaction";
		}
	}

	// ================================================================================
	// StimulationInteractionTemplate
	// ================================================================================
	/**
	 * Template for a stimulation interaction, two datanodes with a MIM_STIMULATION
	 * line.
	 */
	public static class StimulationInteractionTemplate extends DataNodeInteractionTemplate {
		@Override
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndArrowHeadType(ArrowHeadType.STIMULATION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		@Override
		public String getName() {
			return "stimulation interaction";
		}
	}

	// ================================================================================
	// PhosphorylationTemplate
	// ================================================================================
	/**
	 * Template for a phosphorylation interaction, two Protein Datanodes with a
	 * MIM_MODIFICATION line.
	 */

	public static class PhosphorylationTemplate extends DataNodeInteractionTemplate {
		// static final double OFFSET_CATALYST = 50;
		PathwayElement lastPhosphorylation;
		// PathwayElement lastPhosLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastStartNode.setType(DataNodeType.PROTEIN);
			lastEndNode.setType(DataNodeType.PROTEIN);
			lastStartNode.setTextLabel("Protein");
			lastEndNode.setTextLabel("P-Protein");
			lastLine.setEndArrowHeadType(ArrowHeadType.CONVERSION);
			// instantiates and adds a state to data node and pathway model
			State e = lastEndNode.addState("P", StateType.PROTEIN_MODIFICATION, 1.0, 1.0);
			// set graphics
			setInitialSize(e);
			// default textColor, font-Name/tWeight/Style/Decoration/Strikethru/Size,
			// hAlign, vAlign
			// default borderColor, borderStyle, borderWidth, fillColor
			e.setShapeType(ShapeType.OVAL);
			e.setZOrder(Z_ORDER_STATE);
			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine };
		}

		public String getName() {
			return "Phosphorylation";
		}
	}

	// ================================================================================
	// ReactionTemplate
	// ================================================================================
	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a
	 * GeneProduct (enzyme) pointing to an anchor on that arrow.
	 */
	public static class ReactionTemplate extends DataNodeInteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		DataNode lastCatalyst;
		Interaction lastCatLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			DataNodeTemplate dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getWidth(), my - OFFSET_CATALYST)[0];
//			setInitialSize(lastCatalyst); TODO 
			lastCatalyst.setTextLabel("Catalyst");

			lastStartNode.setType(DataNodeType.METABOLITE);
			theme.setInitialColors(lastStartNode);
			lastStartNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastStartNode.setTextLabel("Substrate");

			lastEndNode.setType(DataNodeType.METABOLITE);
			theme.setInitialColors(lastEndNode);
			lastEndNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastEndNode.setTextLabel("Product");

			lastLine.setEndArrowHeadType(ArrowHeadType.CONVERSION);
			Anchor anchor = lastLine.addAnchor(0.5, AnchorShapeType.NONE);

			InteractionTemplate lnt = new InteractionTemplate("undirected", LineStyleType.SOLID,
					ArrowHeadType.UNDIRECTED, ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

			lastCatLine.getStartLinePoint().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getEndLinePoint().linkTo(anchor, 0, 0);
			lastCatLine.setEndArrowHeadType(ArrowHeadType.CATALYSIS);

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst };
		}

		public String getName() {
			return "reaction";
		}
	}

	// ================================================================================
	// ReversibleReactionTemplate
	// ================================================================================
	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a
	 * GeneProduct (enzyme) pointing to an anchor on that arrow.
	 */
	public static class ReversibleReactionTemplate extends DataNodeInteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		DataNode lastCatalyst;
		DataNode lastCatalyst2;
		Interaction lastCatLine;
		Interaction lastCatLine2;
		Interaction lastReverseLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			DataNodeTemplate dnt = new DataNodeTemplate(DataNodeType.PROTEIN);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getWidth(), my - OFFSET_CATALYST)[0];
//			setInitialSize(lastCatalyst); TODO
			lastCatalyst.setTextLabel("Catalyst 1");

			lastCatalyst2 = dnt.addElements(p, mx + lastStartNode.getWidth(), my + OFFSET_CATALYST)[0];
//			setInitialSize(lastCatalyst2); TODO
			lastCatalyst2.setTextLabel("Catalyst 2");

			lastStartNode.setType(DataNodeType.METABOLITE);
			theme.setInitialColors(lastStartNode);
			lastStartNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastStartNode.setTextLabel("Metabolite 1");

			lastEndNode.setType(DataNodeType.METABOLITE);
			theme.setInitialColors(lastEndNode);
			lastEndNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastEndNode.setTextLabel("Metabolite 2");
			lastLine.setEndArrowHeadType(ArrowHeadType.CONVERSION);

			Anchor anchor = lastLine.addAnchor(0.5, AnchorShapeType.NONE);

			InteractionTemplate lnt = new InteractionTemplate("undirected", LineStyleType.SOLID,
					ArrowHeadType.UNDIRECTED, ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0]; // TODO Cast?

			lastCatLine.getStartLinePoint().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getEndLinePoint().linkTo(anchor, 0, 0);
			lastCatLine.setEndArrowHeadType(ArrowHeadType.CATALYSIS);

			InteractionTemplate rev = new InteractionTemplate("undirected", LineStyleType.SOLID,
					ArrowHeadType.UNDIRECTED, ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastReverseLine = rev.addElements(p, mx, my)[0]; // TODO Cast?

			lastReverseLine.getStartLinePoint().linkTo(lastEndNode, -1, 0.5);
			lastReverseLine.getEndLinePoint().linkTo(lastStartNode, 1, 0.5);
			lastReverseLine.setEndArrowHeadType(ArrowHeadType.CONVERSION);

			Anchor anchor2 = lastReverseLine.addAnchor(0.5, AnchorShapeType.NONE);

			InteractionTemplate lnt2 = new InteractionTemplate("undirected", LineStyleType.SOLID,
					ArrowHeadType.UNDIRECTED, ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastCatLine2 = lnt2.addElements(p, mx, my)[0]; // TODO Cast?

			lastCatLine2.getStartLinePoint().linkTo(lastCatalyst2, 0, -1);
			lastCatLine2.getEndLinePoint().linkTo(anchor2, 0, 0);
			lastCatLine2.setEndArrowHeadType(ArrowHeadType.CATALYSIS);
			// These elements are selected in PV, so users can move them around.
			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst, lastCatalyst2 };
		}

		public String getName() {
			return "ReversibleReaction";
		}
	}

}
