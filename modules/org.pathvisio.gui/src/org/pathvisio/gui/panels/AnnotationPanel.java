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
package org.pathvisio.gui.panels;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.model.Annotation;
import org.pathvisio.libgpml.model.Citation;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.PathwayElement.AnnotationRef;
import org.pathvisio.libgpml.model.type.AnnotationType;
import org.pathvisio.libgpml.util.Utils;
import org.pathvisio.libgpml.util.XrefUtils;
import org.bridgedb.Xref;
import org.pathvisio.core.util.Resources;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.dialogs.AnnotationDialog;

/**
 * 
 * @author unknown
 */
public class AnnotationPanel extends PathwayElementPanel implements ActionListener {

	// labels
	private static final String ADD = "New annotation";
	private static final String REMOVE = "Remove";
	private static final String EDIT = "Edit";
	private static final URL IMG_EDIT = Resources.getResourceURL("edit.gif");
	private static final URL IMG_REMOVE = Resources.getResourceURL("cancel.gif");

	// fields
	List<AnnotationRef> annotationRefs;

	JScrollPane refPanel;
	JButton addBtn;

	final private SwingEngine swingEngine;
	boolean readOnly = false;

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * Instantiates annotationRef panel
	 * 
	 * @param swingEngine
	 */
	public AnnotationPanel(SwingEngine swingEngine) {
		this.swingEngine = swingEngine;
		setLayout(new BorderLayout(5, 5));
		annotationRefs = new ArrayList<AnnotationRef>();
		addBtn = new JButton(ADD);
		addBtn.setActionCommand(ADD);
		addBtn.addActionListener(this);
		JPanel addPnl = new JPanel();
		addPnl.add(addBtn);
		add(addPnl, BorderLayout.SOUTH);
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Sets read only,
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		this.readOnly = readOnly;
		addBtn.setEnabled(!readOnly);
	}

	/**
	 * Returns the current pathway element.
	 */
	@Override
	protected PathwayElement getInput() {
		return (PathwayElement) super.getInput();
	}

	// ================================================================================
	// Refresh
	// ================================================================================
	/**
	 * Refresh.
	 */
	public void refresh() {
		if (refPanel != null) {
			remove(refPanel);
		}
		annotationRefs = getInput().getAnnotationRefs();
		DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("fill:pref:grow"));
		for (AnnotationRef annotationRef : annotationRefs) {
			b.append(new AnnotationRefPanel(annotationRef));
			b.nextLine();
		}
		JPanel p = b.getPanel();
		p.setBackground(Color.WHITE);
		refPanel = new JScrollPane(p);
		add(refPanel, BorderLayout.CENTER);
		validate();
	}

	// ================================================================================
	// ADD, EDIT, and REMOVE Methods
	// ================================================================================
	/**
	 * Action for when ADD "New reference" button is pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ADD)) {
			addPressed();
		}
	}

	/**
	 * When "Edit" button is pressed. A AnnotationDialog is opened for given
	 * annotationRef.
	 * 
	 * @param xref
	 */
	private void edit(AnnotationRef annotationRef) {
		if (annotationRef != null) {
			AnnotationDialog d = new AnnotationDialog(getInput(), annotationRef, null, this, false);
			d.setVisible(true);
		}
		refresh();
	}

	/**
	 * When "Remove" button is pressed. The given annotationRef is removed.
	 * 
	 * @param annotationRef
	 */
	private void remove(AnnotationRef annotationRef) {
		((PathwayElement) getInput()).removeAnnotationRef(annotationRef); // TODO
		refresh();
	}

	/**
	 * When ADD "New reference" button is pressed. A AnnotationDialog is opened.
	 */
	private void addPressed() {
		final AnnotationDialog d = new AnnotationDialog(getInput(), null, null, this);
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						d.setVisible(true);
					}
				});
			} catch (Exception e) {
				Logger.log.error("Unable to open dialog");
			}
		} else {
			d.setVisible(true);
		}
		if (d.getExitCode().equals(AnnotationDialog.OK)) {
			refresh();
		} else {

		}
	}

	// ================================================================================
	// AnnotationRefPanel Class
	// ================================================================================
	/**
	 * Panel which displays AnnotationRef
	 * 
	 * @author unknown
	 */
	private class AnnotationRefPanel extends JPanel implements HyperlinkListener, ActionListener {

		static final String PUBMED_URL = "http://www.ncbi.nlm.nih.gov/pubmed/";
		AnnotationRef annotationRef;
		JPanel btnPanel;

		// ================================================================================
		// Display Panel
		// ================================================================================
		/**
		 * Instantiates panel
		 * 
		 * @param annotationRef
		 */
		public AnnotationRefPanel(AnnotationRef annotationRef) {
			this.annotationRef = annotationRef;
			setBackground(Color.WHITE);
			setLayout(new FormLayout("2dlu, fill:[100dlu,min]:grow, 1dlu, pref, 2dlu", "2dlu, pref, 2dlu"));
			JTextPane txt = new JTextPane();
			txt.setContentType("text/html");
			txt.setEditable(false);
			Annotation annotation = annotationRef.getAnnotation();
			// index starts from 1
			int ordinal = getInput().getPathwayModel().getAnnotations().indexOf(annotation) + 1;
			txt.setText("<html>" + "<B>" + ordinal + ":</B> " + annotationRef.toString() + "</html>");
			txt.addHyperlinkListener(this);
			CellConstraints cc = new CellConstraints();
			add(txt, cc.xy(2, 2));

			btnPanel = new JPanel(new FormLayout("pref", "pref, pref"));
			JButton btnEdit = new JButton();
			btnEdit.setActionCommand(EDIT);
			btnEdit.addActionListener(this);
			btnEdit.setIcon(new ImageIcon(IMG_EDIT));
			btnEdit.setBackground(Color.WHITE);
			btnEdit.setBorder(null);
			btnEdit.setToolTipText("Edit annotation");

			JButton btnRemove = new JButton();
			btnRemove.setActionCommand(REMOVE);
			btnRemove.addActionListener(this);
			btnRemove.setIcon(new ImageIcon(IMG_REMOVE));
			btnRemove.setBackground(Color.WHITE);
			btnRemove.setBorder(null);
			btnRemove.setToolTipText("Remove annotation");

			MouseAdapter maHighlight = new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					e.getComponent().setBackground(new Color(200, 200, 255));
				}

				public void mouseExited(MouseEvent e) {
					e.getComponent().setBackground(Color.WHITE);
				}
			};
			btnEdit.addMouseListener(maHighlight);
			btnRemove.addMouseListener(maHighlight);

			btnPanel.add(btnEdit, cc.xy(1, 1));
			btnPanel.add(btnRemove, cc.xy(1, 2));

			add(btnPanel, cc.xy(4, 2));
			btnPanel.setVisible(false);

			MouseAdapter maHide = new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					if (!readOnly)
						btnPanel.setVisible(true);
				}

				public void mouseExited(MouseEvent e) {
					if (!contains(e.getPoint())) {
						btnPanel.setVisible(false);
					}
				}
			};
			addMouseListener(maHide);
			txt.addMouseListener(maHide);
		}

		/**
		 * Updates hyperlink.
		 */
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				swingEngine.openUrl(e.getURL());
			}
		}

		/**
		 * Edit and Remove actions.
		 */
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if (EDIT.equals(action)) {
				edit(annotationRef);
			} else if (REMOVE.equals(action)) {
				AnnotationPanel.this.remove(annotationRef);
			}
		}

	}
}
