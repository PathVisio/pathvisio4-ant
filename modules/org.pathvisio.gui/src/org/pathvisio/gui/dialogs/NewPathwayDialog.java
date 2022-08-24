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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.bridgedb.bio.Organism;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.util.PermissiveComboBox;

/**
 * Dialog asks user for pathway title and organism when new pathways is created
 * 
 * @author mkutmon, dslenter, finterly
 */
public class NewPathwayDialog extends OkCancelDialog {

	// labels
	private final static String TITLE = "Title *";
	private final static String ORGANISM = "Organism *";
	private final static String DESCRIPTION = "Description ";

	// fields
	private JTextField titleField;
	private PermissiveComboBox organismComboBox;
	private JTextArea descriptionArea;

	private SwingEngine swingEngine;

	public NewPathwayDialog(SwingEngine swingEngine, String title) {
		super(swingEngine.getFrame(), title, swingEngine.getFrame(), true, false);
		this.swingEngine = swingEngine;
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));
		addContent(panel);
		setDialogComponent(panel);
		setSize(300, 210);
	}

	protected void addContent(JPanel panel) {
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
		organismComboBox = new PermissiveComboBox(Organism.latinNamesArray());
		organismComboBox.setSelectedItem("Homo sapiens");
		descriptionArea = new JTextArea();
		descriptionArea.setFont(new JLabel().getFont());// UI Design default font

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 5, 0, 5); // top padding
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(titleFieldLabel, c);
		fieldPanel.add(orgComboLabel, c);
		c.insets = new Insets(15, 5, 0, 5); // top padding
		fieldPanel.add(descriptionLabel, c);

		c.insets = new Insets(0, 5, 0, 5);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		fieldPanel.add(titleField, c);
		fieldPanel.add(organismComboBox, c);
		c.ipady = 40; // taller text area for description
		c.insets = new Insets(15, 5, 0, 5); // top padding
		fieldPanel.add(new JScrollPane(descriptionArea), c);

	}

	protected void okPressed() {
		if (titleField.getText().equals("")) {
			// pathway title is required
			JOptionPane.showMessageDialog(this, "The Title is a mandatory field.", "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			super.okPressed();
			swingEngine.getEngine().getActivePathwayModel().getPathway().setTitle(titleField.getText());
			swingEngine.getEngine().getActivePathwayModel().getPathway().setDescription(descriptionArea.getText());
			String itemSelectedFromDropDown = (String) organismComboBox.getSelectedItem();
			if (itemSelectedFromDropDown != null)
				swingEngine.getEngine().getActivePathwayModel().getPathway().setOrganism(itemSelectedFromDropDown);
		}
	}
}
