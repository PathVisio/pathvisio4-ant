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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import com.mammothsoftware.frwk.ddb.RolloverButton;

/**
 * Toggle drop-down button, intended to be used in the PathVisio toolbar.
 * <p>
 * This button consists of a regular icon button on the left, and a drop-down
 * arrow on the right. When the drop-down arrow is clicked, a popup menu is
 * shown from which you can choose from a list of possible actions.
 * <p>
 * Actions are added in groups with addButtons. Between groups you can add a
 * heading with addLabel. The action added first will be the initially selected
 * action.
 */
public class ActionDropDownButton extends JButton implements ActionListener {

	private JPopupMenu popup = new JPopupMenu();
	private JToolBar tb = new ToolBar();
	private JButton mainButton;
	private JButton arrowButton;
	private boolean directActionEnabled = true;
	private ActionListener directAction = null;

	public ActionDropDownButton(String buttonText) {
		super();
		this.setBorder(null);
		mainButton = new MirrorButton(buttonText);
		arrowButton = new RolloverButton(new DownArrow(), 11, false);
		init();
	}

	public void setToolTipText(String text) {
		mainButton.setToolTipText(text);
		arrowButton.setToolTipText(text);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mainButton.setEnabled(enabled);
		arrowButton.setEnabled(enabled);
	}

	public void updateUI() {
		super.updateUI();
		setBorder(null);
	}

	protected Border getRolloverBorder() {
		return BorderFactory.createRaisedBevelBorder();
	}

	private void initRolloverListener() {
		MouseListener l = new MouseAdapter() {
			Border mainBorder = null;
			Border arrowBorder = null;

			public void mouseEntered(MouseEvent e) {
				mainBorder = mainButton.getBorder();
				arrowBorder = mainButton.getBorder();
				mainButton.setBorder(new CompoundBorder(getRolloverBorder(), mainBorder));
				arrowButton.setBorder(new CompoundBorder(getRolloverBorder(), arrowBorder));
				mainButton.getModel().setRollover(true);
				arrowButton.getModel().setRollover(true);
			}

			public void mouseExited(MouseEvent e) {
				mainButton.setBorder(mainBorder);
				arrowButton.setBorder(arrowBorder);
				mainButton.getModel().setRollover(false);
				arrowButton.getModel().setRollover(false);
			}
		};
		mainButton.addMouseListener(l);
		arrowButton.addMouseListener(l);
	}

	private void init() {
		initRolloverListener();

		mainButton.setRequestFocusEnabled(false);
		mainButton.setRolloverEnabled(true);
		int w = (int) mainButton.getPreferredSize().getWidth();
		mainButton.setMaximumSize(new Dimension(w, 100));
		mainButton.setPreferredSize(new Dimension(w, 25));

		Icon disDownArrow = new DisabledDownArrow();
		arrowButton.setDisabledIcon(disDownArrow);
		arrowButton.setMaximumSize(new Dimension(11, 100));

		mainButton.addActionListener(this);
		arrowButton.addActionListener(this);
		setMargin(new Insets(0, 0, 0, 0));

		// Windows draws border around buttons, but not toolbar buttons
		// Using a toolbar keeps the look consistent.
		tb.setBorder(null);
		tb.setMargin(new Insets(0, 0, 0, 0));
		tb.setFloatable(false);
		tb.add(mainButton);
		tb.add(arrowButton);
		add(tb);

		setFixedSize(mainButton, arrowButton);

	}

	/*
	 * Forces the width of this button to be the sum of the widths of the main
	 * button and the arrow button. The height is the max of the main button or the
	 * arrow button.
	 */
	private void setFixedSize(JButton mainButton, JButton arrowButton) {
		int width = (int) (mainButton.getPreferredSize().getWidth() + arrowButton.getPreferredSize().getWidth());
		int height = (int) Math.max(mainButton.getPreferredSize().getHeight(),
				arrowButton.getPreferredSize().getHeight());
		setMaximumSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));

	}

	/**
	 * Removes a component from the popup
	 * 
	 * @param component
	 */
	public void removeComponent(Component component) {
		popup.remove(component);
	}

	/**
	 * Adds a component to the popup
	 * 
	 * @param component
	 * @return
	 */
	public Component addComponent(Component component) {
		return popup.add(component);
	}

	/**
	 * If true, the direct action will be executed when the left button is clicked.
	 * If false, both the left and the right part of the button work the same way
	 * 
	 * @param value set toggle behaviour
	 */
	public void setDirectActionEnabled(boolean value) {
		this.directActionEnabled = value;
	}

	public void actionPerformed(ActionEvent ae) {
		// if the directAction behaviour is enabled, and a directAction is set, and
		// the source of the event is the left part,
		if (directActionEnabled && directAction != null && ae.getSource() == mainButton) {
			ae.setSource(this);
			directAction.actionPerformed(ae);
		} else {
			// otherwise just show the drop-down.
			JPopupMenu popup = getPopupMenu();
			popup.show(this, 0, this.getHeight());
		}
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	/**
	 * Sets the Action that will be executed when the main part of the dropdown
	 * button is clicked. This value is only used if set
	 * 
	 * @param defaultAction A menu item, action or other actionListener that will
	 *                      get invoked
	 */
	public void setDirectAction(ActionListener defaultAction) {
		directAction = defaultAction;
	}

	@Override
	/** sets the icon for the left part only */
	public void setIcon(Icon icon) {
		mainButton.setIcon(icon);
	}

	/**
	 * Down arrow icon.
	 */
	private static class DownArrow implements Icon {

		Color arrowColor = Color.black;

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(arrowColor);
			g.drawLine(x, y, x + 4, y);
			g.drawLine(x + 1, y + 1, x + 3, y + 1);
			g.drawLine(x + 2, y + 2, x + 2, y + 2);
		}

		public int getIconWidth() {
			return 6;
		}

		public int getIconHeight() {
			return 4;
		}

	}

	/**
	 * Disabled down arrow icon.
	 */
	private static class DisabledDownArrow extends DownArrow {

		public DisabledDownArrow() {
			arrowColor = new Color(140, 140, 140);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			g.setColor(Color.WHITE);
			g.drawLine(x + 3, y + 2, x + 4, y + 1);
			g.drawLine(x + 3, y + 3, x + 5, y + 1);
		}
	}

	/**
	 * Updates UI
	 */
	private static class ToolBar extends JToolBar {
		public void updateUI() {
			super.updateUI();
			setBorder(null);
		}
	}

	protected JButton getMainButton() {
		return mainButton;
	}

	public class MirrorButton extends JButton {

		private Shape imageShape;
		private Color imageColor;

		public MirrorButton(String text) {
			super();
			this.setText(text);
		}

		protected void setImageShape(Shape shape) {
			this.imageShape = shape;
		}

		protected void setImageColor(Color color) {
			this.imageColor = color;
		}

		/**
		 * Paints this button.
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
//			if (this.getAction().toString() == "Undefined") {
//				g2.setStroke(
//						new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] { 4, 4 }, 0));
//			}
			this.setForeground(imageColor);
			g2.setPaint(imageColor);
			AffineTransform at = new AffineTransform();
			at.translate(0, -4);
			Shape shapeToDraw = at.createTransformedShape(imageShape);
			if (shapeToDraw != null) {
				g2.draw(shapeToDraw);
			}
			g2.setColor(new Color(255, 255, 255, 0)); // Transparent
			g2.dispose();
		}

	}

}
