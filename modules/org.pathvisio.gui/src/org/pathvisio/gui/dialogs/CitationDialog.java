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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.PathwayElement.CitationRef;
import org.pathvisio.libgpml.model.Referenceable.Citable;
import org.pathvisio.libgpml.util.XrefUtils;
import org.xml.sax.SAXException;

/**
 * Dialog for entering citations. For convenience, you can enter a pubmed id and
 * query the details from pubmed.
 * 
 * @author unknown, finterly
 */
public class CitationDialog extends OkCancelDialog {

	// labels
//	final static String ADD = "Add";
//	final static String REMOVE = "Remove";
	private final static String QUERY = "Query PubMed"; // button
	private final static String XREF_IDENTIFIER = "Identifier *";
	private final static String XREF_DATASOURCE = "Database *";
	private final static String INSTRUCTION = "Database:Id And/Or Url link required ";
	private final static String URL_LINK = "Url link*";

	// fields
	private JTextField xrefIdentifier;
	private JTextField xrefDataSource;
	private JTextField urlLinkText;

	private Citable citable;
	private CitationRef citationRef;

	/**
	 * Instantiates a citation dialog.
	 * 
	 * @param citationRef
	 * @param frame
	 * @param locationComp
	 * @param cancellable
	 */
	public CitationDialog(Citable citable, CitationRef citationRef, Frame frame, Component locationComp,
			boolean cancellable) {
		super(frame, "Literature reference properties", locationComp, true, cancellable);
		this.citable = citable;
		this.citationRef = citationRef;
		setDialogComponent(createDialogPane());
		setSize(300, 250);// UI Design
		refresh();
	}

	/**
	 * Instantiates a citation dialog with boolean cancellable true.
	 */
	public CitationDialog(Citable citable, CitationRef citationRef, Frame frame, Component locationComp) {
		this(citable, citationRef, frame, locationComp, true);
	}

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

	/**
	 * Refresh.
	 */
	protected void refresh() {
		if (citationRef != null) {
			// sets xref
			String id = XrefUtils.getIdentifier(citationRef.getCitation().getXref());
			setText(id, xrefIdentifier);
			DataSource ds = XrefUtils.getDataSource(citationRef.getCitation().getXref());
			setText(ds.getCompactIdentifierPrefix(), xrefDataSource);
			// sets urlLink
			setText(citationRef.getCitation().getUrlLink(), urlLinkText);
		}
	}

	/**
	 * When "Ok" button is pressed. The citationRef is created or updated.
	 */
	protected void okPressed() {
		// old information
		String oldIdentifier = null;
		DataSource oldDataSource = null;
		String oldUrlLink = null;
		if (citationRef != null) {
			oldIdentifier = citationRef.getCitation().getXref().getId();
			oldDataSource = citationRef.getCitation().getXref().getDataSource();
			oldUrlLink = citationRef.getCitation().getUrlLink();
		}
		// new information
		String newIdentifier = xrefIdentifier.getText().trim();
		String newDataSourceStr = xrefDataSource.getText();
		DataSource newDataSource = XrefUtils.getXrefDataSource(newDataSourceStr);
		String newUrlLink = urlLinkText.getText();
		// if changed
		if (oldIdentifier != newIdentifier || oldDataSource != newDataSource || oldUrlLink != newUrlLink) {
			Xref newXref = new Xref(newIdentifier, newDataSource);
			if (newXref != null || newUrlLink != null) { // xref or urlLink required
				if (citationRef != null) {// if citationRef exists, remove first
					citable.removeCitationRef(citationRef);
				}
				citable.addCitation(newXref, newUrlLink);
			}
		}
		super.okPressed();
	}

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
			xrefDataSource.setText("PubMed");
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

	/**
	 * Creates Dialog pane. // TODO removed author stuff
	 * 
	 * @return
	 */
	protected Component createDialogPane() {
		JPanel contents = new JPanel();
		contents.setLayout(new GridBagLayout());
		JPanel xrefPanel = new JPanel();
		JPanel urlPanel = new JPanel();
//		xrefPanel.setBorder(BorderFactory.createTitledBorder(TEXTLABEL));
//		urlPanel.setBorder(BorderFactory.createTitledBorder(HREF));
		GridBagConstraints pc = new GridBagConstraints();
		pc.fill = GridBagConstraints.BOTH;
		pc.gridx = 0;
		pc.weightx = 1;
		pc.weighty = 1;
		pc.insets = new Insets(2, 2, 2, 2);
		pc.gridy = GridBagConstraints.RELATIVE;
		contents.add(xrefPanel, pc);
		pc.fill = GridBagConstraints.CENTER;
		contents.add(new JLabel(INSTRUCTION), pc);
		pc.fill = GridBagConstraints.BOTH;
		contents.add(urlPanel, pc);

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

		// ========================================
		// Xref Panel
		// ========================================
		JLabel lblXrefIdentifier = new JLabel(XREF_IDENTIFIER);
		JLabel lblXrefDataSource = new JLabel(XREF_DATASOURCE);
		xrefIdentifier = new JTextField();
		xrefDataSource = new JTextField();
		JButton query = new JButton(QUERY);
		query.addActionListener(this);
		query.setToolTipText("Query publication information from PubMed");

		xrefPanel.setLayout(new GridBagLayout());
		GridBagConstraints xc = new GridBagConstraints();
		xc.anchor = GridBagConstraints.FIRST_LINE_START;
		xc.gridx = 0;
		xc.gridy = GridBagConstraints.RELATIVE;
		xc.weightx = 0;
		xrefPanel.add(lblXrefIdentifier, xc);
		xrefPanel.add(lblXrefDataSource, xc);
		xc.gridx = 1;
		xc.fill = GridBagConstraints.HORIZONTAL;
		xc.weightx = 1;
		xrefPanel.add(xrefIdentifier, xc);
		xrefPanel.add(xrefDataSource, xc);
		xc.gridx = 2;
		xc.fill = GridBagConstraints.NONE;
		xrefPanel.add(query);

		// ========================================
		// UrlLink Panel
		// ========================================
		JLabel lblUrlLink = new JLabel(URL_LINK);
		urlLinkText = new JTextField();

		urlPanel.setLayout(new GridBagLayout());
		GridBagConstraints uc = new GridBagConstraints();
		uc.anchor = GridBagConstraints.FIRST_LINE_START;
		uc.gridx = 0;
		uc.gridy = GridBagConstraints.RELATIVE;
		uc.weightx = 0;
		urlPanel.add(lblUrlLink, uc);
		uc.gridx = 1;
		uc.fill = GridBagConstraints.HORIZONTAL;
		uc.weightx = 1;
		urlPanel.add(urlLinkText, uc);

		return contents;
	}
}
