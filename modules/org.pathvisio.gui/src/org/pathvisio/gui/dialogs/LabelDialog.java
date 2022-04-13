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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.ShapedElement;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.util.FontChooser;

/**
 * Dialog to modify label specific properties
 * 
 * @author thomas, finterly
 *
 */
public class LabelDialog extends PathwayElementDialog {

	// labels
	private final static String TEXTLABEL = "Text label * ";
	private final static String HREF = "Hyperlink";
	private final static String FONT = "Font: ";

	// fields
	private JTextArea text;
	private JTextField href;
	private JLabel fontPreview;

	protected LabelDialog(SwingEngine swingEngine, Label e, boolean readonly, Frame frame, Component locationComp) {
		super(swingEngine, e, readonly, frame, "Label properties", locationComp);
		text.requestFocus();
	}

	/**
	 * Get the pathway element for this dialog
	 */
	protected Label getInput() {
		return (Label) super.getInput();
	}

	protected void refresh() {
		super.refresh();
		if (getInput() != null) {
			// set text label
			Label input = getInput();
			text.setText(input.getTextLabel());
			int style = input.getFontWeight() ? Font.BOLD : Font.PLAIN;
			style |= input.getFontStyle() ? Font.ITALIC : Font.PLAIN;
			Font f = new Font(input.getFontName(), style, (int) (input.getFontSize()));
			text.setFont(f); // UI Design
			fontPreview.setFont(f);
			fontPreview.setText(f.getName());
			// set href
			href.setText(input.getHref());
			href.setFont(new Font("Tahoma", Font.PLAIN, 10));// UI Design
		} else {
			text.setText("");
			fontPreview.setText("");
		}
	}

	protected void addCustomTabs(JTabbedPane parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		JPanel textPanel = new JPanel();
		JPanel hrefPanel = new JPanel();
		textPanel.setBorder(BorderFactory.createTitledBorder(TEXTLABEL));
		hrefPanel.setBorder(BorderFactory.createTitledBorder(HREF));
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.gridx = 0;
		panelConstraints.weightx = 1;
		panelConstraints.weighty = 1;
		panelConstraints.insets = new Insets(2, 2, 2, 2);
		panelConstraints.gridy = GridBagConstraints.RELATIVE;
		panel.add(textPanel, panelConstraints);
		panel.add(hrefPanel, panelConstraints);

		// Search panel elements
		textPanel.setLayout(new FormLayout("4dlu, pref, 4dlu, pref, 4dlu, pref, pref:grow, 4dlu",
				"4dlu, pref, 4dlu, fill:pref:grow, 4dlu, pref, 4dlu"));

		// ========================================
		// TextLabel Panel
		// ========================================
		text = new JTextArea();
		fontPreview = new JLabel(getFont().getFamily());

		final JButton font = new JButton("...");
		font.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font f = FontChooser.showDialog(null, (Component) e.getSource(), fontPreview.getFont());
				if (f != null) {
					ShapedElement input = getInput();
					if (input != null) {
						input.setFontName(f.getFamily());
						input.setFontWeight(f.isBold());
						input.setFontStyle(f.isItalic());
						input.setFontSize(f.getSize());
						text.setFont(f); // UI Design
						fontPreview.setText(f.getFamily());
						fontPreview.setFont(f);
					}
				}
			}
		});

		CellConstraints cc = new CellConstraints();
		textPanel.add(new JLabel(""), cc.xy(2, 2));// dummy component
		textPanel.add(new JScrollPane(text), cc.xyw(2, 4, 6));
		textPanel.add(new JLabel(FONT), cc.xy(2, 6));
		textPanel.add(fontPreview, cc.xy(4, 6));
		textPanel.add(font, cc.xy(6, 6));

		text.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				saveText();
			}

			public void insertUpdate(DocumentEvent e) {
				saveText();
			}

			public void removeUpdate(DocumentEvent e) {
				saveText();
			}

			private void saveText() {
				if (getInput() != null)
					getInput().setTextLabel(text.getText());
			}
		});

		// ========================================
		// Href Panel
		// ========================================
		href = new JTextField();
		hrefPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		hrefPanel.add(href, c);
		href.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				saveHref();
			}

			public void insertUpdate(DocumentEvent e) {
				saveHref();
			}

			public void removeUpdate(DocumentEvent e) {
				saveHref();
			}

			private void saveHref() {
				if (getInput() != null)
					getInput().setHref(href.getText());
			}
		});

		// ========================================
		// Etc
		// ========================================
		text.setEnabled(!readonly);
		href.setEnabled(!readonly);

		parent.add(TAB_PROPERTIES, panel);
		parent.setSelectedComponent(panel);
	}
}
