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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Toggle drop-down button for Actions, added as a panel of graphic buttons.
 * <p>
 * This button consists of a regular icon button on the left, and a drop-down
 * arrow on the right. When the drop-down arrow is clicked, a popup menu is
 * shown from which you can choose from a list of possible actions.
 * <p>
 * Actions are added in groups with addButtons. Between groups you can add a
 * heading with addLabel. The action added first will be the initially selected
 * action.
 */
public class GraphicsChoiceButton extends ActionDropDownButton {

	/**
	 * Instantiates a GraphicsChoiceButton Button
	 */
	public GraphicsChoiceButton() {
		// set icon to null for now, we'll use the icon
		// from the first action added with addButtons
		super(null);
	}

	private int numItemPerRow = 6;

	/**
	 * Sets the number of actions per row in the pop-up. Default is 6.
	 */
	public void setNumItemsPerRow(int value) {
		numItemPerRow = value;
	}

	// remember if we already set an action
	private boolean noIconSet = true;

	/**
	 * Adds a group of actions, which will be displayed in the pop-up. This can be
	 * invoked multiple times.
	 * 
	 * @param aa the array of actions.
	 */
	public void addButtons(Action[] aa, String label) {
		JPanel pane = new JPanel();
		pane.setBackground(Color.WHITE);
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.0;
		c.weighty = 1.0;

		final JPopupMenu popup = getPopupMenu();

		int i = 0;
		Map<Action, ImageTextButton> aToB = new HashMap<Action, ImageTextButton>();
		;
		for (final Action a : aa) {
			c.gridx = i % numItemPerRow;
			c.gridy = i / numItemPerRow;

			// clicking a button should cause the pop-up menu disappear, any better way?
			final ImageTextButton button = new ImageTextButton(a);
			aToB.put(a, button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button.setContentAreaFilled(false);
					popup.setVisible(false);
					Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
					if (icon != null) {
						setIcon(icon);
						setDirectAction(a);
						getMainButton().updateButton(null, null, Color.BLACK, null);
					} else {
						setIcon(null);
						ImageTextButton b = aToB.get(a);
						getMainButton().updateButton(b.getText(), b.getImageShape(), b.getImageColor(),
								b.getImageStroke());
					}
				}
			});
			pane.add(button, c);
			i++;
		}

		// fill the rest spaces using dummy button when there are less than
		// numItemPerRow items, any better way?
		for (; i < numItemPerRow; i++) {
			c.gridx = i;
			JButton dummy = new JButton();
			Dimension dim = new Dimension(25, 0);
			dummy.setPreferredSize(dim);
			dummy.setContentAreaFilled(false);
			pane.add(dummy, c);
		}

		addComponent(pane);

		// sets initial main button
		if (noIconSet) {
			Action firstAction = aa[0];
			Icon icon = (Icon) firstAction.getValue(Action.SMALL_ICON);
			if (icon != null) {
				setIcon(icon);
				getMainButton().updateButton(null, null, Color.BLACK, null);

			} else {
				ImageTextButton b = aToB.get(firstAction);
				getMainButton().updateButton(b.getText(), b.getImageShape(), b.getImageColor(), b.getImageStroke());
			}
			setDirectActionEnabled(true);
			setDirectAction(firstAction);
			this.repaint();
			noIconSet = false;
		}

	}

	/**
	 * Adds section label to the drop-down menu.
	 * 
	 * @param s the string for the label.
	 */
	public void addLabel(String s) {
		JLabel title = new JLabel(s);
		title.setForeground(new Color(50, 21, 110));// UI design
		title.setFont(new Font("sanserif", Font.BOLD, 11));
		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(new Color(221, 231, 238)); // UI design
		titlePanel.add(title);
		addComponent(titlePanel);
	}

	/**
	 * Adds item buttons and section label to the drop-down menu.
	 * 
	 * @param label the string for the label.
	 * @param aa    the array of actions.
	 */
	public void addButtons(String label, Action[] aa) {
		addLabel(label);
		addButtons(aa, label);
	}

}
