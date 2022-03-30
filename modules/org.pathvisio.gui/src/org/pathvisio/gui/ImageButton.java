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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Action;
import javax.swing.JButton;

import org.pathvisio.libgpml.model.shape.ShapeCatalog;
import org.pathvisio.libgpml.model.shape.ShapeCatalog.Internal;

/**
 * this is the buttons on the drop-down panel
 *
 * @author bing
 */
public class ImageButton extends JButton {

//	Color oldFColor, oldBColor;
//
//	private FocusListener focusListener = new FocusListener() {
//
//		@Override
//		public void focusGained(FocusEvent e) {
//			setBackground(Color.YELLOW);
//			setForeground(Color.BLUE);
//		}
//
//		@Override
//		public void focusLost(FocusEvent e) {
//			setBackground(oldBColor);
//			setForeground(oldFColor);
//		}
//	};

	public ImageButton(Action a) {
		super();
		this.setRolloverEnabled(true);
		initRolloverListener();
		Dimension dim = new Dimension(33, 33); // UI Design
		this.setAction(a);
		this.setSize(dim);
		this.setPreferredSize(dim);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setText(null);
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setContentAreaFilled(false);

		// UI Design
//		oldFColor = getForeground();
//		oldBColor = getBackground();
//		setFocusPainted(false);
//		addFocusListener(focusListener);
	}

	/**
	 * 
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
