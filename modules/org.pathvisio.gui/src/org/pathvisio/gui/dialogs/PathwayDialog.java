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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.util.XrefUtils;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.completer.CompleterQueryTextArea;
import org.pathvisio.gui.completer.CompleterQueryTextField;
import org.pathvisio.gui.completer.OptionProvider;
import org.pathvisio.gui.util.PermissiveComboBox;

/**
 * Dialog to easily edit the properties of a pathway, such as the pathway title,
 * organism, etc.
 * 
 * @author unknown, finterly
 */
public class PathwayDialog extends PathwayElementDialog {

	// labels
	private final static String TITLE = "Title *";
	private final static String ORGANISM = "Organism *";
	private final static String DESCRIPTION = "Description ";
	private final static String XREF_IDENTIFIER = "Identifier  ";
	private final static String XREF_DATASOURCE = "Database";

	// fields
	private JTextField titleField;
	private PermissiveComboBox organismComboBox;
	private JTextArea descriptionArea;
	private CompleterQueryTextField idText;// for xref identifier
	private DataSourceModel dsm;// for xref dataSource
	private PermissiveComboBox dbCombo; // all registered datasource

	// ================================================================================
	// Constructor
	// ================================================================================
	protected PathwayDialog(SwingEngine swingEngine, Pathway e, boolean readonly, Frame frame, String title,
			Component locationComp) {
		super(swingEngine, e, readonly, frame, "Pathway properties", locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the pathway element for this dialog.
	 */
	@Override
	protected Pathway getInput() {
		return (Pathway) super.getInput();
	}

	// ================================================================================
	// OK Pressed Method
	// ================================================================================
	@Override
	protected void okPressed() {
		boolean done = true;
		// ========================================
		// New information
		// ========================================
		swingEngine.getEngine().getActivePathwayModel().getPathway().setTitle(titleField.getText());

		String itemSelectedFromDropDown = (String) organismComboBox.getSelectedItem();
		if (itemSelectedFromDropDown != null) {
			swingEngine.getEngine().getActivePathwayModel().getPathway().setOrganism(itemSelectedFromDropDown);
		}
		swingEngine.getEngine().getActivePathwayModel().getPathway().setDescription(descriptionArea.getText());

		String newId = idText.getText().trim();
		DataSource newDs = (DataSource) dsm.getSelectedItem();
		// ========================================
		// Check requirements
		// ========================================
		if (!newId.equals("") && newDs == null) {
			done = false;
			JOptionPane.showMessageDialog(this,
					"You annotated this pathway element with an identifier but no database.\nPlease specify a database system.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else if (newId.equals("") && newDs != null) {
			done = false;
			JOptionPane.showMessageDialog(this,
					"You annotated this pathway element with a database but no identifier.\nPlease specify an identifier.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		// ========================================
		// done
		// ========================================
		if (done) {
			super.okPressed();
		}
	}

	// ================================================================================
	// Dialog and Panels
	// ================================================================================
	@Override
	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		// ========================================
		// Two Panels: Properties and Xref
		// ========================================

		JPanel fieldPanel = new JPanel();
		JPanel xrefPanel = new JPanel();
		fieldPanel.setBorder(BorderFactory.createTitledBorder(""));
		xrefPanel.setBorder(BorderFactory.createTitledBorder(""));
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.gridx = 0;
		panelConstraints.weightx = 1;
		panelConstraints.weighty = 1;
		panelConstraints.insets = new Insets(2, 2, 2, 2);
		panelConstraints.gridy = GridBagConstraints.RELATIVE;
		panel.add(fieldPanel, panelConstraints);
		panel.add(xrefPanel, panelConstraints);

		// ========================================
		// Properties Panel
		// ========================================
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

		// ========================================
		// Manual Entry Panel
		// ========================================
		xrefPanel.setLayout(new GridBagLayout());

		JLabel idLabel = new JLabel(XREF_IDENTIFIER);
		JLabel dbLabel = new JLabel(XREF_DATASOURCE);

		// xref identifier
		idText = new CompleterQueryTextField(new OptionProvider() {
			public List<String> provideOptions(String text) {
				if (text == null)
					return Collections.emptyList();

				IDMapperStack gdb = swingEngine.getGdbManager().getCurrentGdb();
				Set<Xref> refs = new HashSet<Xref>();
				try {
					if (gdb.getMappers().size() > 0)
						refs = gdb.freeSearch(text, 100);
				} catch (IDMapperException ignore) {
				}

				// Only take identifiers
				List<String> ids = new ArrayList<String>();
				for (Xref ref : refs)
					ids.add(ref.getId());
				return ids;
			}
		}, true);
		idText.setCorrectCase(false);
		// xref datasource
		dsm = new DataSourceModel();
		dsm.setPrimaryFilter(true);
		dsm.setSpeciesFilter(swingEngine.getCurrentOrganism());
		dbCombo = new PermissiveComboBox(dsm);
		GridBagConstraints c1 = new GridBagConstraints();
		c1.ipadx = c1.ipady = 5;
		c1.anchor = GridBagConstraints.FIRST_LINE_START;
		c1.weightx = 0;
		c1.gridx = 0;
		c1.gridy = GridBagConstraints.RELATIVE;
		xrefPanel.add(idLabel, c1);
		xrefPanel.add(dbLabel, c1);
		c1.gridx = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.weightx = 1;
		xrefPanel.add(idText, c1);
		xrefPanel.add(dbCombo, c1);
		// xref identifier add listener
		idText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setText();
			}

			public void insertUpdate(DocumentEvent e) {
				setText();
			}

			public void removeUpdate(DocumentEvent e) {
				setText();
			}

			private void setText() {
				getInput().setXref(XrefUtils.createXref(idText.getText(), (DataSource) dsm.getSelectedItem()));
			}
		});
		// xref datasource add listener
		dsm.addListDataListener(new ListDataListener() {

			public void contentsChanged(ListDataEvent arg0) {
				getInput().setXref(XrefUtils.createXref(idText.getText(), (DataSource) dsm.getSelectedItem()));
			}

			public void intervalAdded(ListDataEvent arg0) {
			}

			public void intervalRemoved(ListDataEvent arg0) {
			}
		});
		// ========================================
		// Etc
		// ========================================
		parent.add(TAB_PROPERTIES, panel);
		parent.setSelectedComponent(panel);
	}

}
