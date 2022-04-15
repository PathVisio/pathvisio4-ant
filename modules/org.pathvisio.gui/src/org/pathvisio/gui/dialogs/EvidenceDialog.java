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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.ParserConfigurationException;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.pathvisio.core.data.PubMedQuery;
import org.pathvisio.core.data.PubMedResult;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.gui.util.PermissiveComboBox;
import org.pathvisio.libgpml.model.PathwayElement.EvidenceRef;
import org.pathvisio.libgpml.model.Referenceable.Evidenceable;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.util.XrefUtils;
import org.xml.sax.SAXException;

/**
 * Dialog for entering evidences. For convenience, you can enter a pubmed id and
 * query the details from pubmed.
 * 
 * @author unknown, finterly
 */
public class EvidenceDialog extends ReferenceDialog {

	// labels
	private final static String QUERY = "Query PubMed"; // TODO button
	private final static String VALUE = "Value";
	private final static String XREF_IDENTIFIER = "Identifier *";
	private final static String XREF_DATASOURCE = "Database *";
	private final static String URL_LINK = "Url link";

	// fields
	private JTextField valueText;
	private JTextField xrefIdentifier;
	private DataSourceModel dsm; // for xref dataSource
	private PermissiveComboBox dbCombo; // all registered datasource
	private JTextField urlLinkText;

	private Evidenceable evidenceable;
	private EvidenceRef evidenceRef;

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * Instantiates a evidence dialog.
	 * 
	 * @param evidenceRef
	 * @param frame
	 * @param locationComp
	 * @param cancellable
	 */
	public EvidenceDialog(Evidenceable evidenceable, EvidenceRef evidenceRef, Frame frame, Component locationComp,
			boolean cancellable) {
		super(frame, "Evidence properties", locationComp, true, cancellable);
		this.evidenceable = evidenceable;
		this.evidenceRef = evidenceRef;
		setDialogComponent(createDialogPane());
		setSize(300, 250);// UI Design
		refresh();
	}

	/**
	 * Instantiates a evidence dialog with boolean cancellable true.
	 */
	public EvidenceDialog(Evidenceable evidenceable, EvidenceRef evidenceRef, Frame frame, Component locationComp) {
		this(evidenceable, evidenceRef, frame, locationComp, true);
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Sets text in text field.
	 * 
	 * @param text
	 * @param field
	 */
	private void setText(String text, JTextComponent field) {
		if (text != null && text.length() > 0)
			field.setText(text);
	}

	// ================================================================================
	// Refresh
	// ================================================================================
	/**
	 * Refresh.
	 */
	protected void refresh() {
		if (evidenceRef != null) {
			// sets value
			valueText.setText(evidenceRef.getEvidence().getValue());
			valueText.setFont(new Font("Tahoma", Font.PLAIN, 10));// UI Design
			// sets xref
			String id = XrefUtils.getIdentifier(evidenceRef.getEvidence().getXref());
			setText(id, xrefIdentifier);
			DataSource ds = XrefUtils.getDataSource(evidenceRef.getEvidence().getXref());
			dsm.setSelectedItem(ds);
			dsm.setObjectTypeFilter(ObjectType.EVIDENCE);
			// sets urlLink
			urlLinkText.setText(evidenceRef.getEvidence().getUrlLink());
			urlLinkText.setFont(new Font("Tahoma", Font.PLAIN, 10));// UI Design
		}
	}

	// ================================================================================
	// OK Pressed Method
	// ================================================================================
	/**
	 * When "Ok" button is pressed. The evidenceRef is created or updated.
	 */
	protected void okPressed() {
		boolean done = true;
		// ========================================
		// Old information
		// ========================================
		String oldValue = null;
		String oldId = null;
		DataSource oldDs = null;
		String oldUrl = null;
		if (evidenceRef != null) {
			oldValue = evidenceRef.getEvidence().getValue();
			oldId = evidenceRef.getEvidence().getXref().getId();
			oldDs = evidenceRef.getEvidence().getXref().getDataSource();
			oldUrl = evidenceRef.getEvidence().getUrlLink();
		}
		// ========================================
		// New information
		// ========================================
		String newValue = valueText.getText();
		String newId = xrefIdentifier.getText().trim();
		DataSource newDs = (DataSource) dsm.getSelectedItem();
		String newUrl = urlLinkText.getText();
		// ========================================
		// Check requirements
		// ========================================
		if ((newId.equals("") || newDs == null)) {
			done = false;
			JOptionPane.showMessageDialog(this,
					"An Evidence requires a valid Database:id.\nPlease input more information.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		if (!newId.equals("") && newDs == null) {
			done = false;
			JOptionPane.showMessageDialog(this,
					"This Evidence has an identifier but no database.\nPlease specify a database system.", "Error",
					JOptionPane.ERROR_MESSAGE);
		} else if (newId.equals("") && newDs != null) {
			done = false;
			JOptionPane.showMessageDialog(this,
					"This Evidence has a database but no identifier.\nPlease specify an identifier.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		// ========================================
		// New EvidenceRef
		// ========================================
		Xref newXref = new Xref(newId, newDs);
		if (newXref != null) {
			evidenceable.removeEvidenceRef(evidenceRef); // remove old first
			evidenceable.addEvidence(newValue, newXref, newUrl); // "replace" with new
		}
		if (done) { // TODO
			super.okPressed();
		}
	}

	// ================================================================================
	// Query Methods
	// ================================================================================
	/**
	 * When "Query" button is pressed.
	 */
	protected void queryPressed() {
		final PubMedQuery pmq = new PubMedQuery(xrefIdentifier.getText().trim());
		final ProgressKeeper pk = new ProgressKeeper();
		ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(this), "", pk, true, true);

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws SAXException, IOException, ParserConfigurationException {
				pk.setTaskName("Querying PubMed");
				pmq.execute();
				pk.finished();
				return null;
			}
		};

		sw.execute();
		d.setVisible(true);

		PubMedResult pmr = pmq.getResult();
		if (pmr != null) {
			xrefIdentifier.setText(pmr.getId()); // write the trimmed pmid to the dialog
			dsm.setSelectedItem(dsm); // TODO
		}
	}

	/**
	 * Action for when "Query" button is pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		if (QUERY.equals(e.getActionCommand())) {
			queryPressed();
		}
		super.actionPerformed(e);
	}

	// ================================================================================
	// Dialog and Panels
	// ================================================================================
	/**
	 * Creates Dialog pane.
	 * 
	 * @return
	 */
	protected Component createDialogPane() {
		JPanel contents = new JPanel();
		contents.setLayout(new GridBagLayout());

		JLabel lblValue = new JLabel(VALUE);
		JLabel lblXrefIdentifier = new JLabel(XREF_IDENTIFIER);
		JLabel lblXrefDataSource = new JLabel(XREF_DATASOURCE);
		JLabel lblUrlLink = new JLabel(URL_LINK);

		valueText = new JTextField();
		xrefIdentifier = new JTextField();
		dsm = new DataSourceModel();
		dsm.setPrimaryFilter(true);
		dsm.setObjectTypeFilter(ObjectType.EVIDENCE);
		dbCombo = new PermissiveComboBox(dsm);
		urlLinkText = new JTextField();

		final DefaultStyledDocument doc = new DefaultStyledDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				super.insertString(fb, offset, string, attr);
				highlight((StyledDocument) fb.getDocument());
			}

			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				super.replace(fb, offset, length, text, attrs);
				highlight((StyledDocument) fb.getDocument());
			}

			void highlight(StyledDocument doc) {
				SimpleAttributeSet clean = new SimpleAttributeSet();
				doc.setCharacterAttributes(0, doc.getLength(), clean, true);
				SimpleAttributeSet sep = new SimpleAttributeSet();
				sep.addAttribute(StyleConstants.ColorConstants.Foreground, Color.RED);
				sep.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
			}

		});

		JButton query = new JButton(QUERY);
		query.addActionListener(this);
		query.setToolTipText("Query publication information from PubMed");

		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 0;
		contents.add(lblValue, c);
		contents.add(lblXrefIdentifier, c);
		contents.add(lblXrefDataSource, c);
		contents.add(lblUrlLink, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		contents.add(valueText, c);
		contents.add(xrefIdentifier, c);
		contents.add(dbCombo, c);
		contents.add(urlLinkText, c);

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		contents.add(query);

		return contents;
	}
}
