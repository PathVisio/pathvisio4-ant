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
package org.pathvisio.core.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.Shape;
import org.pathvisio.libgpml.model.shape.IShape;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.LineStyleType;
import org.pathvisio.libgpml.model.type.ShapeType;

/**
 * Color Theme class. For coloring of pathway model objects.
 * 
 * NB: Sets colors and shape of DataNode(s) to given Theme. Preserves all other
 * graphics attributes.
 * 
 * @author finterly
 */
public class Theme {
	// ================================================================================
	// Theme Names
	// ================================================================================
	public static final String WP = "WikiPathways";

	// ================================================================================
	// Instantiate Themes
	// ================================================================================
	public static final Theme WIKIPATHWAYS = new Theme(WP);

	// ================================================================================
	// Sets: also used in {@link DefaultTemplates}
	// ================================================================================
	// cellular component shapes
	public static final Set<ShapeType> CELL_COMPONENT_SET = new HashSet<>(Arrays.asList(ShapeType.CELL,
			ShapeType.NUCLEUS, ShapeType.ENDOPLASMIC_RETICULUM, ShapeType.GOLGI_APPARATUS, ShapeType.MITOCHONDRIA,
			ShapeType.SARCOPLASMIC_RETICULUM, ShapeType.ORGANELLE, ShapeType.VESICLE, ShapeType.EXTRACELLULAR_REGION));

	// miscellaneous shapes
	public static final Set<ShapeType> MISC_SHAPE_SET = new HashSet<>(
			Arrays.asList(ShapeType.CORONAVIRUS_ICON, ShapeType.DNA_ICON, ShapeType.RNA_ICON, ShapeType.CELL_ICON,
					ShapeType.MEMBRANE_ICON, ShapeType.DEGRADATION));

	// ================================================================================
	// Theme Properties
	// ================================================================================
	String name; // the name of the theme
	String description; // description of the theme

	// ================================================================================
	// Color-able Fields
	// ================================================================================
	private Color colorBackground;
	private Color colorDefault;
	private Color colorMetabolite;
	private Color colorPathway;
	private Color colorDataNodeFill;
	private Color colorLabel;
	private Color colorShapeFill;
	private Color colorShapeGrey;
	private Color colorShapeDarkGrey;
	private boolean colorShapes = false;

	// ================================================================================
	// Constructors
	// ================================================================================
	public Theme(String name) {
		this.name = name;
		if (name == WP) {
			description = "WikiPathways theme";
			colorBackground = ColorPalette.WP_WHITE;
			colorDefault = ColorPalette.WP_BLACK;
			colorMetabolite = ColorPalette.WP_BLUE;
			colorPathway = ColorPalette.WP_DGREEN;
			colorDataNodeFill = ColorPalette.WP_WHITE;
			colorLabel = ColorPalette.WP_BLACK;
			colorShapeFill = ColorPalette.TRANSPARENT;
			colorShapeGrey = ColorPalette.WP_CUSTOM_PV_MGREY;
			colorShapeDarkGrey = ColorPalette.WP_DGREY;
		}
	}

	// ================================================================================
	// Accessors
	// ================================================================================

	/**
	 * Returns the name of this Theme.
	 * 
	 * @return name the name of this theme.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of this Theme.
	 * 
	 * @return description the description of this theme.
	 */
	public String getDescription() {
		return description;
	}

	// ================================================================================
	// Color PathwayModel
	// ================================================================================
	/**
	 * @param p
	 * @param theme
	 */
	public void colorPathwayModel(PathwayModel p) {
		p.getPathway().setBackgroundColor(colorBackground);
		for (DataNode d : p.getDataNodes()) {
			setInitialShapeBorder(d);
			setInitialColors(d);
		}
		if (colorShapes) {
			for (Shape s : p.getShapes()) {
				setInitialColors(s);
			}
		}
	}

	// ================================================================================
	// DataNode Set Methods: Shape, Border, and Color
	// ================================================================================
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

	/**
	 * Sets text, border, and fill color.
	 * 
	 * @param e the data node.
	 */
	public void setInitialColors(DataNode e) {
		DataNodeType type = e.getType();
		e.setFillColor(colorDataNodeFill);
		if (type == DataNodeType.PATHWAY) {
			e.setTextColor(colorPathway);
			e.setBorderColor(colorPathway);
		} else if (type == DataNodeType.METABOLITE) {
			e.setTextColor(colorMetabolite);
			e.setBorderColor(colorMetabolite);
		} else {
			e.setTextColor(colorDefault);
			e.setBorderColor(colorDefault);
		}
	}

	// ================================================================================
	// Shape Set Methods: Color
	// ================================================================================

	/**
	 * Sets text, border, and fill color.
	 * 
	 * @param shape
	 */
	public void setInitialColors(Shape e) {
		IShape type = e.getShapeType();
		e.setFillColor(colorShapeFill);
		if (CELL_COMPONENT_SET.contains(type)) {
			e.setTextColor(colorShapeGrey);
			e.setBorderColor(colorShapeGrey);
		} else if (MISC_SHAPE_SET.contains(type)) {
			e.setTextColor(colorShapeDarkGrey);
			e.setBorderColor(colorShapeDarkGrey);
		} else {
			e.setTextColor(colorDefault);
			e.setBorderColor(colorDefault);
		}
	}

	// ================================================================================
	// Other Methods
	// ================================================================================

	/**
	 * Returns String Theme name.
	 * 
	 * @return name the name of this Theme.
	 */
	@Override
	public String toString() {
		return name;
	}

}
