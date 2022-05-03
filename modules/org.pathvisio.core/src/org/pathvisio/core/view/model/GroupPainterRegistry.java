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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.pathvisio.core.util.ColorPalette;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.util.ColorUtils;

/**
 * Keeps track of all GroupPainters.
 * 
 * @author thomas
 */
public class GroupPainterRegistry {

	public static final double DEFAULT_M_MARGIN = 8; // Make the bounds slightly
	// larger than the summed bounds
	// of the containing elements
	public static final double COMPLEX_M_MARGIN = 12;

	private static Map<String, GroupPainter> painters = new HashMap<String, GroupPainter>();

	/**
	 * Register a painter that will be used for the given group style.
	 * 
	 * @param name    The name of the group style (use {@link GroupType#toString()}.
	 * @param painter The painter that will draw the group style
	 */
	public static void registerPainter(String name, GroupPainter painter) {
		painters.put(name, painter);
	}

	/**
	 * Get the registered painter for the given group style.
	 * 
	 * @param name The name of the group style (use {@link GroupType#toString()}.
	 * @return The registered painter, or the default painter if no custom painters
	 *         are registered for the given group style.
	 */
	public static GroupPainter getPainter(String name) {
		GroupPainter p = painters.get(name);
		if (p == null) {
			p = defaultPainter;
		}
		return p;
	}

	private static final int TRANSLUCENCY_LEVEL = (int) (255 * .10); // 25.5
	private static final int TRANSLUCENCY_LEVEL_HOVER = (int) (255 * .05); // 12.75
	private static final Color DEFAULT_GRAY = ColorPalette.WP_DGREY;
	private static final Color PATHWAY_GREEN = ColorPalette.WP_GREEN;
	private static final Color TRANSPARENT_BLUE = ColorPalette.WP_BLUE;
	private static final Color BORDER_GRAY = ColorPalette.WP_DGREY;

	/**
	 * Painter for {@link GroupType#GROUP}. Group appears as gray rectangle with
	 * gray dashed border.
	 * 
	 * NB: "Group" (GPML2021) replaces "None" (GPML2013a).
	 */
	private static GroupPainter defaultPainter = new GroupPainter() {
		public void drawGroup(Graphics2D g, VGroup group, int flags) {
			boolean mouseover = (flags & VGroup.FLAG_MOUSEOVER) != 0;
			boolean anchors = (flags & VGroup.FLAG_ANCHORSVISIBLE) != 0;
			boolean selected = (flags & VGroup.FLAG_SELECTED) != 0;

			// Draw group outline
			int sw = 1;
			Rectangle2D rect = group.getVBounds();

			// different alpha when selected and mouse over
			int alpha = (mouseover || anchors || selected) ? TRANSLUCENCY_LEVEL : TRANSLUCENCY_LEVEL_HOVER;

			// fill
			g.setColor(ColorUtils.makeTransparent(DEFAULT_GRAY, alpha));
			g.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
			// border
			g.setColor(BORDER_GRAY);
			g.setStroke(
					new BasicStroke(sw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[] { 4, 2 }, 0));
			g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth() - sw, (int) rect.getHeight() - sw);
		}
	};

	/**
	 * Painter for {@link GroupType#TRANSPARENT}. Group appears as transparent
	 * rectangle. When mouse over group, fill color changes to blue.
	 * 
	 * NB: "Transparent" (GPML2021) replaces "Group" (GPML2013a).
	 */
	private static GroupPainter transparentPainter = new GroupPainter() {
		public void drawGroup(Graphics2D g, VGroup group, int flags) {
			boolean mouseover = (flags & VGroup.FLAG_MOUSEOVER) != 0;
			boolean anchors = (flags & VGroup.FLAG_ANCHORSVISIBLE) != 0;
			boolean selected = (flags & VGroup.FLAG_SELECTED) != 0;

			Rectangle2D rect = group.getVBounds();

			// Group highlight, on mouseover, linkanchors display and selection
			if (mouseover || anchors || selected) {
				int sw = 1;
				// fill
				g.setColor(ColorUtils.makeTransparent(TRANSPARENT_BLUE, TRANSLUCENCY_LEVEL_HOVER));
				g.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
				// border
				g.setColor(BORDER_GRAY);
				g.setStroke(new BasicStroke(sw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[] { 4, 2 },
						0));
				g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth() - sw,
						(int) rect.getHeight() - sw);
			}
			// User hint is drawn on mouseover, if it fits within the group bounds
			if (mouseover && !anchors) {
				// Draw a hint to tell the user that click selects group
				String hint = selected ? "Drag to move group" : "Click to select group";

				Rectangle2D tb = g.getFontMetrics().getStringBounds(hint, g);

				if (tb.getWidth() <= rect.getWidth()) {
					int yoffset = (int) rect.getY();
					int xoffset = (int) rect.getX() + (int) (rect.getWidth() / 2) - (int) (tb.getWidth() / 2);
					yoffset += (int) (rect.getHeight() / 2) + (int) (tb.getHeight() / 2);
					g.drawString(hint, xoffset, yoffset);
				}
			}
		}
	};

	/**
	 * Painter for {@link GroupType#COMPLEX}. Group appears as gray octagon with
	 * gray solid border.
	 * 
	 * NB: the octagon shape is specially implemented.
	 */
	private static GroupPainter complexPainter = new GroupPainter() {
		public void drawGroup(Graphics2D g, VGroup group, int flags) {
			boolean mouseover = (flags & VGroup.FLAG_MOUSEOVER) != 0;
			boolean anchors = (flags & VGroup.FLAG_ANCHORSVISIBLE) != 0;
			boolean selected = (flags & VGroup.FLAG_SELECTED) != 0;

			// Draw group outline
			int sw = 1;
			Rectangle2D vRect = group.getVBounds();
			float vTop = (float) vRect.getMinY();
			float vLeft = (float) vRect.getMinX();
			float vBottom = (float) vRect.getMaxY() - sw;
			float vRight = (float) vRect.getMaxX() - sw;

			float vMargin = (float) Math.min(Math.min(vRect.getWidth() / 2.5, vRect.getHeight() / 2.5),
					group.vFromM(COMPLEX_M_MARGIN * 1.5));

			GeneralPath outline = new GeneralPath();
			outline.moveTo(vLeft + vMargin, vTop);
			outline.lineTo(vRight - vMargin, vTop);
			outline.lineTo(vRight, vTop + vMargin);
			outline.lineTo(vRight, vBottom - vMargin);
			outline.lineTo(vRight - vMargin, vBottom);
			outline.lineTo(vLeft + vMargin, vBottom);
			outline.lineTo(vLeft, vBottom - vMargin);
			outline.lineTo(vLeft, vTop + vMargin);
			outline.closePath();

			// different alpha when selected and mouse over
			int alpha = (mouseover || anchors || selected) ? TRANSLUCENCY_LEVEL : TRANSLUCENCY_LEVEL_HOVER;

			// fill
			g.setColor(ColorUtils.makeTransparent(DEFAULT_GRAY, alpha));
			g.fill(outline);
			// border
			g.setColor(BORDER_GRAY);
			g.setStroke(new BasicStroke());
			g.draw(outline);
		}
	};

	/**
	 * Painter for {@link GroupType#PATHWAY}. Group appears as green rectangle with
	 * gray dashed border.
	 */
	private static GroupPainter pathwayPainter = new GroupPainter() {
		public void drawGroup(Graphics2D g, VGroup group, int flags) {
			boolean mouseover = (flags & VGroup.FLAG_MOUSEOVER) != 0;
			boolean anchors = (flags & VGroup.FLAG_ANCHORSVISIBLE) != 0;
			boolean selected = (flags & VGroup.FLAG_SELECTED) != 0;

			int sw = 1;
			Rectangle2D rect = group.getVBounds();

			// different alpha when selected and mouse over
			int alpha = (mouseover || anchors || selected) ? TRANSLUCENCY_LEVEL : TRANSLUCENCY_LEVEL_HOVER;

			// fill
			g.setColor(ColorUtils.makeTransparent(PATHWAY_GREEN, alpha));
			g.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
			// border
			g.setColor(BORDER_GRAY);
			g.setStroke(
					new BasicStroke(sw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[] { 4, 2 }, 0));
			g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth() - sw, (int) rect.getHeight() - sw);
		}
	};

	// Register default painters
	static {
		registerPainter(GroupType.GROUP.toString(), defaultPainter);
		registerPainter(GroupType.TRANSPARENT.toString(), transparentPainter);
		registerPainter(GroupType.COMPLEX.toString(), complexPainter);
		registerPainter(GroupType.PATHWAY.toString(), pathwayPainter);
		registerPainter(GroupType.ANALOG.toString(), defaultPainter);
		registerPainter(GroupType.PARALOG.toString(), defaultPainter);
	}
}
