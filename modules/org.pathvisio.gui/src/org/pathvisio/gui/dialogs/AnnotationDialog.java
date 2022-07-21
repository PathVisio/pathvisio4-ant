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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.util.PermissiveComboBox;
import org.pathvisio.libgpml.model.PathwayElement.AnnotationRef;
import org.pathvisio.libgpml.model.Referenceable.Annotatable;
import org.pathvisio.libgpml.model.type.AnnotationType;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.util.XrefUtils;

/**
 * Dialog for entering annotations. For convenience, you can enter a pubmed id
 * and query the details from pubmed.
 * 
 * @author finterly
 */
public class AnnotationDialog extends ReferenceDialog {

	// labels
	private final static String VALUE = "Value *";
	private final static String TYPE = "Type *";
	private final static String XREF_IDENTIFIER = "Identifier";
	private final static String XREF_DATASOURCE = "Database";
	private final static String URL_LINK = "URL link";

	// fields
	private JTextField valueText;
	private PermissiveComboBox typeCombo;
	private JTextField xrefIdentifier;
	private DataSourceModel dsm; // for xref dataSource
	private PermissiveComboBox dbCombo; // all registered datasource
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
		super(frame, "Annotation properties", locationComp, true, cancellable);
		this.annotatable = annotatable;
		this.annotationRef = annotationRef;
		setDialogComponent(createDialogPane());
//		setSize(300, 250);// UI Design
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
			valueText.setFont(new JLabel().getFont());// UI Design default font
			// sets type
			String type = annotationRef.getAnnotation().getType().toString();
			typeCombo.setSelectedItem(AnnotationType.fromName(type));
			// sets xref
			String id = XrefUtils.getIdentifier(annotationRef.getAnnotation().getXref());
			setText(id, xrefIdentifier);
			DataSource ds = XrefUtils.getDataSource(annotationRef.getAnnotation().getXref());
			dsm.setSelectedItem(ds);
			dsm.setObjectTypeFilter(ObjectType.ANNOTATION);
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
		// New information
		// ========================================
		String newValue = valueText.getText();
		AnnotationType newType = (AnnotationType) typeCombo.getSelectedItem();
		String newId = xrefIdentifier.getText().trim();
		DataSource newDs = (DataSource) dsm.getSelectedItem();
		String newUrl = urlLinkText.getText();
		// ========================================
		// Check requirements
		// ========================================
		if (newValue.equals("")) {
			done = false;
			JOptionPane.showMessageDialog(this, "An Annotation requires a value.\nPlease input more information.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		if (newType == null) {
			done = false;
			JOptionPane.showMessageDialog(this, "An Annotation must have an annotation type.\\nPlease select a type.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		// ========================================
		// New AnnotationRef
		// ========================================
		if (done) { // TODO
			Xref newXref = XrefUtils.createXref(newId, newDs);
			if (newValue != null && newType != null) {
				AnnotationRef newA = annotatable.addAnnotation(newValue, newType, newXref, newUrl); // add new info
				copyRefsOldToNew(annotationRef, newA);
				annotatable.removeAnnotationRef(annotationRef); // remove old info
			}
			super.okPressed();
		}
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
		dsm = new DataSourceModel();
		dsm.setPrimaryFilter(true);
		dsm.setObjectTypeFilter(ObjectType.ANNOTATION);
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

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 15, 0, 0);
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

		c.insets = new Insets(0, 0, 0, 50);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		contents.add(valueText, c);
		contents.add(typeCombo, c);
		contents.add(xrefIdentifier, c);
		contents.add(dbCombo, c);
		contents.add(urlLinkText, c);

		return contents;
	}
}
