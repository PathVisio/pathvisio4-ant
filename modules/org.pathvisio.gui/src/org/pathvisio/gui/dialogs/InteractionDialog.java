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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.util.XrefUtils;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.completer.CompleterQueryTextField;
import org.pathvisio.gui.completer.OptionProvider;
import org.pathvisio.gui.util.PermissiveComboBox;

/**
 * 
 * @author unknown
 */
public class InteractionDialog extends PathwayElementDialog implements ItemListener {

	/**
	 * Dialog for editing Reactions/ Interactions. In addition to the standard
	 * comments and literature tabs, this has a tab for looking up accession numbers
	 * of reactions/interactions.
	 */
	private static final long serialVersionUID = 1L; // TODO?
	private CompleterQueryTextField idText;// for xref identifier
	private DataSourceModel dsm;// for xref dataSource
	private PermissiveComboBox dbCombo;
	private PermissiveComboBox startTypeCombo; // for start arrow head type
	private PermissiveComboBox endTypeCombo;// for end arrow head type

	/**
	 * Instantiates an interaction dialog.
	 * 
	 * @param swingEngine
	 * @param e
	 * @param readonly
	 * @param frame
	 * @param locationComp
	 */
	protected InteractionDialog(final SwingEngine swingEngine, final Interaction e, final boolean readonly,
			final Frame frame, final Component locationComp) {
		super(swingEngine, e, readonly, frame, "Interaction properties", locationComp);
		getRootPane().setDefaultButton(null);
		setButton.requestFocus();
		setPreferredSize(new Dimension(320, 300)); // UI Design
	}

	/**
	 * Returns the pathway element for this dialog.
	 */
	protected Interaction getInput() {
		return (Interaction) super.getInput();
	}

	/**
	 * Refresh.
	 */
	public final void refresh() {
		super.refresh();
		// sets xref
		Xref xref = getInput().getXref();
		String id = XrefUtils.getIdentifier(xref);
		DataSource ds = XrefUtils.getDataSource(xref);
		idText.setText(id);
		dsm.setSelectedItem(ds);
		// sets arrowhead types
		String startType = getInput().getStartArrowHeadType().toString();
		startTypeCombo.setSelectedItem(ArrowHeadType.fromName(startType));
		String endType = getInput().getEndArrowHeadType().toString();
		endTypeCombo.setSelectedItem(ArrowHeadType.fromName(endType));
		dsm.setInteractionFilter(true);
		pack();
	}

	/**
	 * Adds custom tabs to this dialog.
	 */
	protected final void addCustomTabs(final JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JPanel fieldPanel = new JPanel();
		fieldPanel.setBorder(BorderFactory.createTitledBorder("Manual entry"));
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.gridx = 0;
		panelConstraints.weightx = 1;
		panelConstraints.weighty = 1;
		panelConstraints.insets = new Insets(2, 2, 2, 2);
		panelConstraints.gridy = GridBagConstraints.RELATIVE;

		panel.add(fieldPanel, panelConstraints);

		GridBagConstraints searchConstraints = new GridBagConstraints();
		searchConstraints.gridx = GridBagConstraints.RELATIVE;
		searchConstraints.fill = GridBagConstraints.HORIZONTAL;

		// Manual entry panel elements
		fieldPanel.setLayout(new GridBagLayout());

		JLabel startLabel = new JLabel("Start arrow");
		JLabel endLabel = new JLabel("End arrow");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");

		idText = new CompleterQueryTextField(new OptionProvider() {
			public List<String> provideOptions(final String text) {
				if (text == null) {
					return Collections.emptyList();
				}

				IDMapperStack gdb = swingEngine.getGdbManager().getCurrentGdb();
				Set<Xref> refs = new HashSet<Xref>();
				try {
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

		dsm = new DataSourceModel();
		dsm.setPrimaryFilter(true);
		dsm.setSpeciesFilter(swingEngine.getCurrentOrganism());
		dbCombo = new PermissiveComboBox(dsm);
		startTypeCombo = new PermissiveComboBox(ArrowHeadType.getValues());
		endTypeCombo = new PermissiveComboBox(ArrowHeadType.getValues());

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		fieldPanel.add(startLabel, c);
		fieldPanel.add(endLabel, c);
		fieldPanel.add(idLabel, c);
		fieldPanel.add(dbLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;

		fieldPanel.add(startTypeCombo, c);
		fieldPanel.add(endTypeCombo, c);
		fieldPanel.add(idText, c);
		fieldPanel.add(dbCombo, c);
		// xref identifier add listener
		idText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(final DocumentEvent e) {
				setText();
			}

			public void insertUpdate(final DocumentEvent e) {
				setText();
			}

			public void removeUpdate(final DocumentEvent e) {
				setText();
			}

			private void setText() {
				// sets xref id
				DataSource ds = XrefUtils.getDataSource(getInput().getXref());// TODO
				getInput().setXref(new Xref(idText.getText(), ds));
			}
		});
		// xref datasource add listener
		dsm.addListDataListener(new ListDataListener() {

			public void contentsChanged(final ListDataEvent arg0) {
				// sets xref dataSource
				String id = XrefUtils.getIdentifier(getInput().getXref());// TODO
				getInput().setXref(new Xref(id, (DataSource) dsm.getSelectedItem()));
			}

			public void intervalAdded(final ListDataEvent arg0) {
			}

			public void intervalRemoved(final ListDataEvent arg0) {
			}
		});
		// start arrowhead add listener
		startTypeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				ArrowHeadType item = (ArrowHeadType) startTypeCombo.getSelectedItem();
				getInput().setStartArrowHeadType(item);
				refresh();
			}
		});
		// end arrowhead add listener
		endTypeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				ArrowHeadType item = (ArrowHeadType) endTypeCombo.getSelectedItem();
				getInput().setEndArrowHeadType(item);
				refresh();
			}
		});

		idText.setEnabled(!readonly);
		dbCombo.setEnabled(!readonly);
		startTypeCombo.setEnabled(!readonly);
		endTypeCombo.setEnabled(!readonly);

		parent.add(TAB_PROPERTIES, panel); // TODO
		parent.setSelectedComponent(panel);
	}

	@Override
	public void itemStateChanged(final ItemEvent arg0) {
		// TODO Auto-generated method stub
	}
}
