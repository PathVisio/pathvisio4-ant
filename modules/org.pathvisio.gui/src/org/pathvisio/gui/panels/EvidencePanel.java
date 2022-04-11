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
import org.pathvisio.libgpml.model.Evidence;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.PathwayElement.EvidenceRef;
import org.pathvisio.libgpml.model.Referenceable.Citable;
import org.pathvisio.libgpml.util.Utils;
import org.pathvisio.libgpml.util.XrefUtils;
import org.bridgedb.Xref;
import org.pathvisio.core.util.Resources;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.dialogs.CitationDialog;

/**
 * 
 * @author unknown
 */
public class EvidencePanel extends PathwayElementPanel implements ActionListener {
	
	private static final String ADD = "New evidence";
	private static final String REMOVE = "Remove";
	private static final String EDIT = "Edit";
	private static final URL IMG_EDIT = Resources.getResourceURL("edit.gif");
	private static final URL IMG_REMOVE = Resources.getResourceURL("cancel.gif");

	List<EvidenceRef> evidenceRefs;

	JScrollPane refPanel;
	JButton addBtn;

	final private SwingEngine swingEngine;
	boolean readOnly = false;

	/**
	 * Instantiates evidenceRef panel
	 * 
	 * @param swingEngine
	 */
	public EvidencePanel(SwingEngine swingEngine) {
		this.swingEngine = swingEngine;
		setLayout(new BorderLayout(5, 5));
		evidenceRefs = new ArrayList<EvidenceRef>();
		addBtn = new JButton(ADD);
		addBtn.setActionCommand(ADD);
		addBtn.addActionListener(this);
		JPanel addPnl = new JPanel();
		addPnl.add(addBtn);
		add(addPnl, BorderLayout.SOUTH);
	}

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

	/**
	 * Refresh.
	 */
	public void refresh() {
		if (refPanel != null)
			remove(refPanel);

		// TODO
		evidenceRefs = getInput().getEvidenceRefs();

		DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("fill:pref:grow"));
		for (EvidenceRef evidenceRef : evidenceRefs) {
			b.append(new EvidenceRefPanel(evidenceRef));
			b.nextLine();
		}
		JPanel p = b.getPanel();
		p.setBackground(Color.WHITE);
		refPanel = new JScrollPane(p);
		add(refPanel, BorderLayout.CENTER);
		validate();
	}

//	/**
//	 * Sets the current pathway element.
//	 * 
//	 * @param e
//	 */
//	public void setInput(PathwayElement e) {
////		if (e != getInput()) { //TODO 
////			elmMgr = e.getParent().getBiopaxElementManager();
////			refMgr = e.getBiopaxReferenceManager();
////		}
//		super.setInput(e);
//	}

	/**
	 * Panel which displays EvidenceRef
	 * 
	 * @author unknown
	 */
	private class EvidenceRefPanel extends JPanel implements HyperlinkListener, ActionListener {
		EvidenceRef evidenceRef;
		JPanel btnPanel;

		/**
		 * Instantiates panel
		 * 
		 * @param evidenceRef
		 */
		public EvidenceRefPanel(EvidenceRef evidenceRef) {
			this.evidenceRef = evidenceRef;
			setBackground(Color.WHITE);
			setLayout(new FormLayout("2dlu, fill:[100dlu,min]:grow, 1dlu, pref, 2dlu", "2dlu, pref, 2dlu"));
			JTextPane txt = new JTextPane();
			txt.setContentType("text/html");
			txt.setEditable(false);
			Evidence evidence = evidenceRef.getEvidence();
			// index starts from 1
			int ordinal = getInput().getPathwayModel().getEvidences().indexOf(evidence) + 1;
			txt.setText("<html>" + "<B>" + ordinal + ":</B> " + xrefToString(evidence.getXref()) + "</html>");
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
			btnEdit.setToolTipText("Edit literature reference");

			JButton btnRemove = new JButton();
			btnRemove.setActionCommand(REMOVE);
			btnRemove.addActionListener(this);
			btnRemove.setIcon(new ImageIcon(IMG_REMOVE));
			btnRemove.setBackground(Color.WHITE);
			btnRemove.setBorder(null);
			btnRemove.setToolTipText("Remove literature reference");

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

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				swingEngine.openUrl(e.getURL());
			}
		}

		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if (EDIT.equals(action)) {
				edit(evidenceRef);
			} else if (REMOVE.equals(action)) {
				EvidencePanel.this.remove(evidenceRef);
			}
		}

		static final String PUBMED_URL = "http://www.ncbi.nlm.nih.gov/pubmed/";

		// TODO need a solution for more...
		public String xrefToString(Xref xref) {
			StringBuilder builder = new StringBuilder();
			String pmid = XrefUtils.getIdentifier(xref);
			String ds = XrefUtils.getDataSource(xref).getFullName();
			if (!Utils.isEmpty(pmid)) {
				builder.append("<A href='" + xref.getKnownUrl()).append("'>").append(ds).append(" ").append(pmid)
						.append("</A>.");
			}
			System.out.println(xref.getKnownUrl());
			System.out.println(ds);
			System.out.println(builder.toString());
			return builder.toString();
		}

	}

	/**
	 * Action for when ADD "New reference" button is pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ADD)) {
			addPressed();
		}
	}

	/**
	 * When "Edit" button is pressed. A EvidenceDialog is opened for given
	 * evidenceRef.
	 * 
	 * @param xref
	 */
	private void edit(EvidenceRef evidenceRef) {
		if (evidenceRef != null) {
			CitationDialog d = new CitationDialog(getInput(), null, null, this, false);
			d.setVisible(true);
		}
		refresh();
	}

	/**
	 * When "Remove" button is pressed. The given evidenceRef is removed.
	 * 
	 * @param evidenceRef
	 */
	private void remove(EvidenceRef evidenceRef) {
		((PathwayElement) getInput()).removeEvidenceRef(evidenceRef); // TODO
		refresh();
	}

	/**
	 * When ADD "New reference" button is pressed. A EvidenceDialog is opened.
	 */
	private void addPressed() {
		EvidenceRef evidenceRef = null; // new evidenceRef
		final CitationDialog d = new CitationDialog(getInput(), null, null, this);
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
		if (d.getExitCode().equals(CitationDialog.OK)) {
			// TODO seems weird but ok for now...
//			getInput().addEvidence(evidenceRef.getEvidence());
			refresh();
		} else {

		}
	}
}