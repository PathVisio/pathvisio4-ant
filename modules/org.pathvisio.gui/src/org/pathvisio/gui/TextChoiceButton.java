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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Toggle drop-down button for Actions, added as a vertical list of buttons.
 * <p>
 * This button consists of a regular icon button on the left, and a drop-down
 * arrow on the right. When the drop-down arrow is clicked, a popup menu is
 * shown from which you can choose from a list of possible actions.
 * <p>
 * 
 * @author finterly
 */
public class TextChoiceButton extends ActionDropDownButton {

	int buttonWidth; // width of button choices (popup menu)

	/**
	 * Instantiates a TextChoice Button
	 * 
	 * @param buttonText  the text of the main button.
	 * @param buttonWidth the width of button choices (popup menu)
	 */
	public TextChoiceButton(String buttonText, int buttonWidth) {
		super(buttonText);
		this.buttonWidth = buttonWidth;
	}

	/**
	 * Adds a group of actions, which will be displayed in the pop-up as a vertical
	 * list. This can be invoked multiple times.
	 * 
	 * @param aa the array of actions.
	 */
	public void addButtons(Action[] aa) {
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
		for (final Action a : aa) {
			c.gridy = i;
			// clicking a button should cause the pop-up menu disappear, any better way?
			final JButton button = new JButton(a);
			button.setPreferredSize(new Dimension(buttonWidth, 24)); // UI Design
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					popup.setVisible(false);
					setDirectAction(a);
				}
			});
			pane.add(button, c);
			i++;
		}
		addComponent(pane);
		setDirectActionEnabled(false);
	}

}
