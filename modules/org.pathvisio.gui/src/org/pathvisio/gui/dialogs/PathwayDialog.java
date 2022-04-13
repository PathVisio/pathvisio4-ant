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
package org.pathvisio.gui.dialogs;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.bridgedb.bio.Organism;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.util.PermissiveComboBox;

/**
 * Dialog to easily edit the properties of a pathway, such as the pathway title,
 * organism, etc.
 * 
 * @author unknown
 */
public class PathwayDialog extends PathwayElementDialog {

	// labels
	private final static String TITLE = "Title *";
	private final static String ORGANISM = "Organism *";
	private final static String DESCRIPTION = "Description ";

	// fields
	private JTextField titleField;
	private PermissiveComboBox organismComboBox;
	private JTextArea descriptionArea;

	protected PathwayDialog(SwingEngine swingEngine, Pathway e, boolean readonly, Frame frame, String title,
			Component locationComp) {
		super(swingEngine, e, readonly, frame, "Pathway properties", locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}

	/**
	 * Returns the pathway element for this dialog.
	 */
	@Override
	protected Pathway getInput() {
		return (Pathway) super.getInput();
	}

	@Override
	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JPanel fieldPanel = new JPanel();
		fieldPanel.setBorder(BorderFactory.createTitledBorder(""));

		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.weightx = 1;
		panelConstraints.weighty = 1;
		panel.add(fieldPanel, panelConstraints);

		fieldPanel.setLayout(new GridBagLayout());

		JLabel titleFieldLabel = new JLabel(TITLE);
		JLabel orgComboLabel = new JLabel(ORGANISM);
		JLabel descriptionLabel = new JLabel(DESCRIPTION);

		titleField = new JTextField();
		titleField.setText(swingEngine.getEngine().getActivePathwayModel().getPathway().getTitle());
		organismComboBox = new PermissiveComboBox(Organism.latinNamesArray());
		organismComboBox.setSelectedItem(swingEngine.getEngine().getActivePathwayModel().getPathway().getOrganism());
		descriptionArea = new JTextArea(swingEngine.getEngine().getActivePathwayModel().getPathway().getDescription());
		descriptionArea.setFont(new Font("Tahoma", Font.PLAIN, 10)); // UI Design

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(titleFieldLabel, c);
		fieldPanel.add(orgComboLabel, c);
		c.insets = new Insets(15, 0, 0, 0); // top padding
		fieldPanel.add(descriptionLabel, c);

		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		fieldPanel.add(titleField, c);
		fieldPanel.add(organismComboBox, c);
		c.ipady = 40; // taller text area for description
		c.insets = new Insets(15, 0, 0, 0); // top padding
		fieldPanel.add(new JScrollPane(descriptionArea), c);

		parent.add(TAB_PROPERTIES, panel);
		parent.setSelectedComponent(panel);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		swingEngine.getEngine().getActivePathwayModel().getPathway().setTitle(titleField.getText());

		String itemSelectedFromDropDown = (String) organismComboBox.getSelectedItem();
		if (itemSelectedFromDropDown != null)
			swingEngine.getEngine().getActivePathwayModel().getPathway().setOrganism(itemSelectedFromDropDown);
	}
}
