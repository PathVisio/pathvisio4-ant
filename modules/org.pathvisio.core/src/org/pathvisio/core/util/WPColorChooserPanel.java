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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Color Panel for WikiPathways Theme.
 * 
 * @author finterly
 */
public class WPColorChooserPanel extends AbstractColorChooserPanel {

	public void buildChooser() {
		setLayout(new BorderLayout(10, 10));
		JPanel top = new JPanel(new GridLayout(1, 0, 0, 0));
		top.add(makeButton("WikiPathways Black", ColorPalette.WP_BLACK));
		top.add(makeButton("WikiPathways Dark Grey", ColorPalette.WP_DGREY));
		top.add(makeButton("WikiPathways Custom PathVisio Grey", ColorPalette.WP_CUSTOM_PV_MGREY));
		top.add(makeButton("WikiPathways Grey", ColorPalette.WP_GREY));
		top.add(makeButton("WikiPathways White", ColorPalette.WP_WHITE));
		add(top, BorderLayout.PAGE_START);

		JPanel center = new JPanel(new GridLayout(0, 4, 10, 10));
		center.add(makeButton("WikiPathways Blue", ColorPalette.WP_BLUE));
		center.add(makeButton("WikiPathways Green", ColorPalette.WP_GREEN));
		center.add(makeButton("WikiPathways Purple", ColorPalette.WP_PURPLE));
		center.add(makeButton("WikiPathways Orange", ColorPalette.WP_ORANGE));
		center.add(makeButton("WikiPathways Dark Blue", ColorPalette.WP_DBLUE));
		center.add(makeButton("WikiPathways Dark Green", ColorPalette.WP_DGREEN));
		center.add(makeButton("WikiPathways Dark Purple", ColorPalette.WP_DPURPLE));
		center.add(makeButton("WikiPathways Dark Orange", ColorPalette.WP_DORANGE));
		add(center, BorderLayout.CENTER);
	}

	public void updateChooser() {
	}

	public String getDisplayName() {
		return "WikiPathways";
	}

	public Icon getSmallDisplayIcon() {
		return null;
	}

	public Icon getLargeDisplayIcon() {
		return null;
	}

	private ColorChooserButton makeButton(String name, Color color) {
		ColorChooserButton button = new ColorChooserButton(name, 25, 25);
		button.setBackground(color);
		button.setBorderPainted(false);
		button.setOpaque(true);
		button.setAction(setColorAction);
		return button;
	}

	Action setColorAction = new AbstractAction() {
		public void actionPerformed(ActionEvent evt) {
			JButton button = (JButton) evt.getSource();
			getColorSelectionModel().setSelectedColor(button.getBackground());
		}
	};

	/**
	 * @author finterly
	 *
	 */
	public class ColorChooserButton extends JButton {
		int width;
		int height;

		public ColorChooserButton(String name, int width, int height) {
			super();
			this.width = width;
			this.height = height;
			Dimension dim = new Dimension(width, height); // UI Design
			this.setSize(dim);
			this.setPreferredSize(dim);
			this.setMinimumSize(dim);
			this.setMaximumSize(dim);
		}

		/**
		 * Paints this button.
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(Color.BLACK);
			g2.draw(new Rectangle(-1, -1, width, height)); // so that border appears as "shadow"
			g2.dispose();
		}
	}

}