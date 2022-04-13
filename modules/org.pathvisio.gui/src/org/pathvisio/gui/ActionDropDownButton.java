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
import java.awt.BasicStroke;

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
 * 
 * Adapted from DropDownButton.java @author m. bangham Copyright 2005 Mammoth
 * Software LLC
 * 
 * @author finterly
 */
public class ActionDropDownButton extends JButton implements ActionListener {

	private JPopupMenu popup = new JPopupMenu();
	private JToolBar tb = new ToolBar();
	private MainButton mainButton;
	private JButton arrowButton;
	private boolean directActionEnabled = true;
	private ActionListener directAction = null;

	// ================================================================================
	// Constructor and Initiation Methods
	// ================================================================================
	/**
	 * Instantiates a ActionDropDown Button
	 * 
	 * @param buttonText the text to set for this button.
	 */
	public ActionDropDownButton(String buttonText) {
		super();
		this.setBorder(null);
		mainButton = new MainButton(buttonText);
		arrowButton = new RolloverButton(new DownArrow(), 11, false);
		init();
	}

	/**
	 * Initiates this button.
	 */
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

	/**
	 * Forces the width of this button to be the sum of the widths of the main
	 * button and the arrow button. The height is the max of the main button or the
	 * arrow button.
	 * 
	 * @param mainButton  the main button (left).
	 * @param arrowButton the arrow drop down button (right).
	 */
	private void setFixedSize(JButton mainButton, JButton arrowButton) {
		int width = (int) (mainButton.getPreferredSize().getWidth() + arrowButton.getPreferredSize().getWidth());
		int height = (int) Math.max(mainButton.getPreferredSize().getHeight(),
				arrowButton.getPreferredSize().getHeight());
		setMaximumSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the main button of this drop down button.
	 * 
	 * @return mainButton the main button.
	 */
	protected MainButton getMainButton() {
		return mainButton;
	}

	/**
	 * Returns the popup menu.
	 * 
	 * @return popup
	 */
	public JPopupMenu getPopupMenu() {
		return popup;
	}

	/**
	 * Sets the icon for the main button (left part) only.
	 */
	@Override
	public void setIcon(Icon icon) {
		mainButton.setIcon(icon);
	}

	/**
	 * Returns the border for this button.
	 * 
	 * @return the raise bevel border.
	 */
	protected Border getRolloverBorder() {
		return BorderFactory.createRaisedBevelBorder();
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
	 * Sets tool tip text.
	 */
	@Override
	public void setToolTipText(String text) {
		mainButton.setToolTipText(text);
		arrowButton.setToolTipText(text);
	}

	/**
	 * Sets enabled boolean.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mainButton.setEnabled(enabled);
		arrowButton.setEnabled(enabled);
	}

	/**
	 * Updates UI.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		setBorder(null);
	}

	// ================================================================================
	// Listener Methods
	// ================================================================================
	/**
	 * Initiates rollover listener.
	 */
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

	// ================================================================================
	// Action Methods
	// ================================================================================
	/**
	 * If true, the direct action will be executed when the left button is clicked.
	 * If false, both the left and the right part of the button work the same way
	 * 
	 * @param value set toggle behaviour
	 */
	public void setDirectActionEnabled(boolean value) {
		this.directActionEnabled = value;
	}

	/**
	 * Performs action
	 *
	 * @param ae the action event.
	 */
	@Override
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

	// ================================================================================
	// DownArrow Icon Class
	// ================================================================================
	/**
	 * Down arrow icon.
	 */
	private static class DownArrow implements Icon {

		Color arrowColor = Color.black;

		/**
		 * Paints this icon.
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(arrowColor);
			g.drawLine(x, y, x + 4, y);
			g.drawLine(x + 1, y + 1, x + 3, y + 1);
			g.drawLine(x + 2, y + 2, x + 2, y + 2);
		}

		/**
		 * Returns icon width.
		 */
		@Override
		public int getIconWidth() {
			return 6;
		}

		/**
		 * Returns icon height.
		 */
		@Override
		public int getIconHeight() {
			return 4;
		}
	}

	// ================================================================================
	// Disabled DownArrow Icon Class
	// ================================================================================
	/**
	 * Disabled down arrow icon.
	 */
	private static class DisabledDownArrow extends DownArrow {

		/**
		 * Constructor for disabled down arrow icon.
		 */
		public DisabledDownArrow() {
			arrowColor = new Color(140, 140, 140);
		}

		/**
		 * Paints this icon.
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			g.setColor(Color.WHITE);
			g.drawLine(x + 3, y + 2, x + 4, y + 1);
			g.drawLine(x + 3, y + 3, x + 5, y + 1);
		}
	}

	// ================================================================================
	// ToolBar Class
	// ================================================================================
	/**
	 * ToolBar.
	 */
	private static class ToolBar extends JToolBar {

		/**
		 * Updates UI.
		 */
		@Override
		public void updateUI() {
			super.updateUI();
			setBorder(null);
		}
	}

	// ================================================================================
	// MainButton Class
	// ================================================================================
	/**
	 * The MainButton of the drop down button. The mainButton stores properties
	 * shape, color, and stroke. This button class allows the main button to mirror
	 * the appearance of a corresponding ImageTextButton.
	 * 
	 * NB: used by {@link GraphicsChoiceButton}.
	 * 
	 * @author finterly
	 */
	public class MainButton extends JButton {

		private Shape imageShape;
		private Color imageColor;
		private BasicStroke imageStroke;

		public MainButton(String text) {
			super();
			this.setText(text);
		}

		/**
		 * Updates the paint properties of this button.
		 * 
		 * @param text   the text of this button.
		 * @param shape  the shape to be painted on button.
		 * @param color  the paint color.
		 * @param stroke the paint stroke.
		 */
		protected void updateButton(String text, Shape shape, Color color, BasicStroke stroke) {
			this.setText(text);
			this.imageShape = shape;
			this.imageColor = color;
			this.imageStroke = stroke;
		}

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
