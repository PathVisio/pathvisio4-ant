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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.pathvisio.core.util.Resources;

import com.mammothsoftware.frwk.ddb.DropDownButton;

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
public class ActionChoiceButton extends ActionDropDownButton {

	int buttonWidth; 
	
	public ActionChoiceButton(String name, int buttonWidth) {
		super(name);
		this.buttonWidth = buttonWidth;
	}

	// remember if we already set an action
	private boolean noIconSet = true;

	/**
	 * Add a group of actions, which will be displayed in the pop-up. This can be
	 * invoked multiple times.
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
		if (noIconSet) {
			setDirectActionEnabled(false);
			noIconSet = false;
		}
	}

	/**
	 * add section label to the drop-down menu
	 */
	public void addLabel(String s) {
		JLabel title = new JLabel(s);
		title.setForeground(new Color(50, 21, 110));// UI design
		title.setFont(new Font("sanserif", Font.BOLD, 11)); // UI design}
		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(new Color(221, 231, 238)); // UI design
		titlePanel.add(title);
		addComponent(titlePanel);
	}

	/**
	 * add item buttons and section label to the drop-down menu
	 */
	public void addButtons(String label, Action[] aa) {
		if (label != null) {
			addLabel(label);
		}
		addButtons(aa);
	}
}
