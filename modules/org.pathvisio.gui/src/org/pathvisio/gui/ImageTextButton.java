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
package org.pathvisio.gui;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics2D;
import javax.swing.Action;
import javax.swing.JButton;
import org.pathvisio.core.util.ColorPalette;

/**
 * Class for the Object buttons on the Object panel and Drop-down panel. The
 * buttons are a mix of painted (with text) and icons (.gif files).
 *
 * <p>
 * NB:
 * <ol>
 * <li>Paints button for Molecule and Concept DataNodes, as well as Label.
 * <li>Mainly buttons with text are painted, because icons are too small
 * (pixel-wise) for visible text.
 * <li>Icons are used for most other pathway elements, such as Interactions and
 * Shapes.
 * <li>The .gif icons vary in size, many are 17x17 and all are 24x24 or smaller.
 * </ol>
 * 
 * @author bing, finterly
 */
public class ImageTextButton extends JButton {

	Shape imageShape; // shape to be painted on button
	Color imageColor; // paint color of button
	BasicStroke imageStroke; // paint stroke
	String category; // button category (e.g. Molecules, Concepts, Interactions...)

	// ================================================================================
	// Constructor
	// ================================================================================
	/**
	 * Instantiates an ImageTextButton.
	 * 
	 * @param a        the action.
	 * @param category the button category (e.g. Molecules, Concepts,
	 *                 Interactions...).
	 */
	public ImageTextButton(Action a, String category) {
		super();
		this.setAction(a);
		this.setRolloverEnabled(true);
		initRolloverListener();
		this.category = category; // to differentiate between datanode and shape "Cell"
		this.setImageShape(); // set shape
		this.setImageColor(); // set color
		this.setImageStroke(); // set stroke
		this.setTextString(); // set text
		// UI Design
		Dimension dim = new Dimension(33, 33); // size includes border around icons(24x24 or smaller)
		this.setSize(dim);
		this.setPreferredSize(dim);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setContentAreaFilled(false);
		this.setFont(new Font("Tahoma", Font.PLAIN, 9)); // UI Design
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the painted shape of this button.
	 * 
	 * @return imageShape the shape.
	 */
	protected Shape getImageShape() {
		return imageShape;
	}

	/**
	 * Returns the paint color of this button.
	 * 
	 * @return imageColor the color.
	 */
	protected Color getImageColor() {
		return imageColor;
	}

	/**
	 * Returns the paint stroke of this button.
	 * 
	 * @return imageStroke the stroke.
	 */
	protected BasicStroke getImageStroke() {
		return imageStroke;
	}

	/**
	 * Sets the shape to be painted on this button.
	 * 
	 * <p>
	 * NB:
	 * <ol>
	 * <li>Molecule datanodes have a rectangle border.
	 * <li>Most Concept datanodes have a rounded rectangle border.
	 * <li>Alias (Concept) datanode has an oval border.
	 * </ol>
	 */
	private void setImageShape() {
		Shape sh = null;
		switch (getText()) {
		case "GeneProduct": // Molecules
		case "Metabolite":
		case "Protein":
		case "DNA":
		case "RNA":
			sh = new Rectangle(4, 6, 24, 20);
			break;
		case "Pathway": // Concepts
		case "Disease":
		case "Phenotype":
		case "Event":
		case "Undefined":
		case "Organ":
			sh = new RoundRectangle2D.Double(4, 6, 24, 20, 8, 8);
			break;
		case "Cell":
			if (category == "Concepts") { // to differentiate from Shape "Cell"
				sh = new RoundRectangle2D.Double(4, 6, 24, 20, 8, 8);
			} else {
				sh = null;
			}
			break;
		case "Alias":
			sh = new Ellipse2D.Double(4, 6, 24, 20);
			break;
		case "Label":
			break;
		default:
			sh = null;
			break;
		}
		this.imageShape = sh;
	}

	/**
	 * Sets the paint color for this button.
	 * 
	 * <p>
	 * NB:
	 * <ol>
	 * <li>Metabolite datanodes are blue text and border.
	 * <li>Pathway datanodes are green text and border.
	 * </ol>
	 */
	private void setImageColor() {
		Color c = null;
		switch (getText()) {
		case "Metabolite":
			c = ColorPalette.WP_BLUE;
			break;
		case "Pathway":
			c = ColorPalette.WP_DGREEN;
			break;
		default:
			c = ColorPalette.WP_BLACK;
			break;
		}
		this.imageColor = c;
	}

	/**
	 * Sets the paint stroke for this button.
	 * 
	 * <p>
	 * NB:
	 * <ol>
	 * <li>Metabolite datanodes are blue text and border.
	 * <li>Pathway datanodes are green text and border.
	 * </ol>
	 */
	private void setImageStroke() {
		BasicStroke stroke = null;
		switch (getText()) {
		case "Undefined":
			stroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] { 4, 4 }, 0);
			break;
		default:
			// default solid
			break;
		}
		this.imageStroke = stroke;
	}

	/**
	 * Sets the text String to be printed on this button.
	 * 
	 * <p>
	 * NB:
	 * <ol>
	 * <li>Molecule and Concept datanodes text are painted.
	 * <li>Label text is painted.
	 * <li>Text is set to null for other pathway elements.
	 * </ol>
	 */
	private void setTextString() {
		String text = getText();
		String t;
		switch (text) {
		case "Undefined":
			t = "?";
			break;
		case "GeneProduct": // Molecules
		case "Metabolite":
		case "Protein":
		case "DNA":
		case "RNA":
		case "Pathway": // Concepts
		case "Disease":
		case "Phenotype":
		case "Event":
		case "Organ":
		case "Alias":
		case "Label":
			if (text.length() > 5) {
				t = text.substring(0, 4);
			} else {
				t = text;
			}
			break;
		case "Cell":
			if (category == "Concepts") {
				t = text;
			} else {
				t = null;
			}
			break;
		default:
			t = null;
			break;
		}
		this.setText(t);
	}

	// ================================================================================
	// Paint Methods
	// ================================================================================
	/**
	 * Paints this button.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		this.setForeground(imageColor);
		g2.setPaint(imageColor);
		if (imageStroke != null) {
			g2.setStroke(imageStroke);
		}
		if (imageShape != null) {
			g2.draw(imageShape);
		}
		g2.setColor(ColorPalette.TRANSPARENT);
		g2.dispose();
	}

	/**
	 * Initiates rollover listener.
	 */
	protected void initRolloverListener() {
		MouseListener l = new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {
				setContentAreaFilled(true);
				getModel().setRollover(true);
			}

			public void mouseExited(MouseEvent e) {
				setContentAreaFilled(false);
			}

		};
		addMouseListener(l);
	}

}
