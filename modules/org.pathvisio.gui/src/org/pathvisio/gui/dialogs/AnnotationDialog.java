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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.pathvisio.gui.util.PermissiveComboBox;
import org.pathvisio.libgpml.model.Annotation;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.PathwayElement.AnnotationRef;
import org.pathvisio.libgpml.model.Referenceable.Annotatable;
import org.pathvisio.libgpml.model.type.AnnotationType;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.util.XrefUtils;
import org.xml.sax.SAXException;

/**
 * Dialog for entering annotations. For convenience, you can enter a pubmed id
 * and query the details from pubmed.
 * 
 * @author unknown, finterly
 */
public class AnnotationDialog extends OkCancelDialog {

	// labels
	private final static String QUERY = "Query PubMed"; // TODO button
	private final static String VALUE = "Value *";
	private final static String TYPE = "Type *";
	private final static String XREF_IDENTIFIER = "Identifier";
	private final static String XREF_DATASOURCE = "Database";
	private final static String URL_LINK = "Url link";

	// fields
	private JTextField valueText;
	private PermissiveComboBox typeCombo;
	private JTextField xrefIdentifier;
	private JTextField xrefDataSource;
	private JTextField urlLinkText;

	private Annotatable annotatable;
	private AnnotationRef annotationRef;

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * Instantiates a annotation dialog.
	 * 
	 * @param annotationRef
	 * @param frame
	 * @param locationComp
	 * @param cancellable
	 */
	public AnnotationDialog(Annotatable annotatable, AnnotationRef annotationRef, Frame frame, Component locationComp,
			boolean cancellable) {
		super(frame, "Literature reference properties", locationComp, true, cancellable);
		this.annotatable = annotatable;
		this.annotationRef = annotationRef;
		setDialogComponent(createDialogPane());
		setSize(300, 250);// UI Design
		refresh();
	}

	/**
	 * Instantiates a annotation dialog with boolean cancellable true.
	 */
	public AnnotationDialog(Annotatable annotatable, AnnotationRef annotationRef, Frame frame, Component locationComp) {
		this(annotatable, annotationRef, frame, locationComp, true);
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
		if (annotationRef != null) {
			// sets value
			valueText.setText(annotationRef.getAnnotation().getValue());
			valueText.setFont(new Font("Tahoma", Font.PLAIN, 10));// UI Design
			// sets type
			String type = annotationRef.getAnnotation().getType().toString();
			typeCombo.setSelectedItem(AnnotationType.fromName(type));
			// sets xref
			String id = XrefUtils.getIdentifier(annotationRef.getAnnotation().getXref());
			setText(id, xrefIdentifier);
			DataSource ds = XrefUtils.getDataSource(annotationRef.getAnnotation().getXref());
			setText(ds.getCompactIdentifierPrefix(), xrefDataSource);
			// sets urlLink
			urlLinkText.setText(annotationRef.getAnnotation().getUrlLink());
		}
	}

	// ================================================================================
	// OK Pressed Method
	// ================================================================================
	/**
	 * When "Ok" button is pressed. The annotationRef is created or updated.
	 */
	protected void okPressed() {
		boolean done = true;
		// ========================================
		// Old information
		// ========================================
		String oldValue = null;
		AnnotationType oldType = null;
		String oldId = null;
		DataSource oldDs = null;
		String oldUrl = null;
		if (annotationRef != null) {
			oldValue = annotationRef.getAnnotation().getValue();
			oldType = annotationRef.getAnnotation().getType();
			oldId = annotationRef.getAnnotation().getXref().getId();
			oldDs = annotationRef.getAnnotation().getXref().getDataSource();
			oldUrl = annotationRef.getAnnotation().getUrlLink();
		}
		// ========================================
		// New information
		// ========================================
		String newValue = valueText.getText();
		AnnotationType newType = (AnnotationType) typeCombo.getSelectedItem();
		String newId = xrefIdentifier.getText().trim();
		DataSource newDs = XrefUtils.getXrefDataSource(xrefDataSource.getText());
		String newUrl = urlLinkText.getText();
		// ========================================
		// Check requirements
		// ========================================
		if (newValue.equals("")) {
			done = false;
			JOptionPane.showMessageDialog(this, "An annotation requires a value.\n Please input more information.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		if (newType == null) {
			done = false;
			JOptionPane.showMessageDialog(this, "An annotation must have an annotation type.\\n Please select a type.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		// ========================================
		// New AnnotationRef
		// ========================================
		Xref newXref = new Xref(newId, newDs);
		if (newValue != null && newType != null) {
			annotatable.removeAnnotationRef(annotationRef); // remove old first
			annotatable.addAnnotation(newValue, newType, newXref, newUrl); // "replace" with new
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
		JLabel lblType = new JLabel(TYPE);
		JLabel lblXrefIdentifier = new JLabel(XREF_IDENTIFIER);
		JLabel lblXrefDataSource = new JLabel(XREF_DATASOURCE);
		JLabel lblUrlLink = new JLabel(URL_LINK);

		valueText = new JTextField();
		typeCombo = new PermissiveComboBox(AnnotationType.getValues());
		xrefIdentifier = new JTextField();
		xrefDataSource = new JTextField();
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
		contents.add(lblType, c);
		contents.add(lblXrefIdentifier, c);
		contents.add(lblXrefDataSource, c);
		contents.add(lblUrlLink, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		contents.add(valueText, c);
		contents.add(typeCombo, c);
		contents.add(xrefIdentifier, c);
		contents.add(xrefDataSource, c);
		contents.add(urlLinkText, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		contents.add(query);

		return contents;
	}
}
