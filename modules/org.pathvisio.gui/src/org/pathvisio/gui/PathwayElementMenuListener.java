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
package org.pathvisio.gui;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import org.pathvisio.core.view.MouseEvent;
import org.pathvisio.core.view.model.Handle;
import org.pathvisio.core.view.model.VAnchor;
import org.pathvisio.core.view.model.VCitation;
import org.pathvisio.core.view.model.VDataNode;
import org.pathvisio.core.view.model.VDrawable;
import org.pathvisio.core.view.model.VElement;
import org.pathvisio.core.view.model.VGroup;
import org.pathvisio.core.view.model.VInfoBox;
import org.pathvisio.core.view.model.VLabel;
import org.pathvisio.core.view.model.VLineElement;
import org.pathvisio.core.view.model.VPathwayElement;
import org.pathvisio.core.view.model.VPathwayModel;
import org.pathvisio.core.view.model.VPathwayModelEvent;
import org.pathvisio.core.view.model.VPathwayModelListener;
import org.pathvisio.core.view.model.VPathwayObject;
import org.pathvisio.core.view.model.VState;
import org.pathvisio.core.view.model.ViewActions;
import org.pathvisio.core.view.model.ViewActions.PositionPasteAction;
import org.pathvisio.gui.CommonActions.AddCitationAction;
import org.pathvisio.gui.CommonActions.EditCitationAction;
import org.pathvisio.gui.CommonActions.PropertiesAction;
import org.pathvisio.gui.dialogs.PathwayElementDialog;
import org.pathvisio.gui.view.VPathwayModelSwing;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.type.AnchorShapeType;
import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.DataNodeType;

/**
 * Implementation of {@link VPathwayModelListener} that handles right-click
 * events to show a pop-up menu when a {@link VElement} is clicked.
 *
 * This class is responsible for maintaining a list of
 * {@link PathwayElementMenuHook}'s, There should be a single Listener per
 * MainPanel, possibly listening to multiple {@link VPathwayModel}'s.
 */
public class PathwayElementMenuListener implements VPathwayModelListener {

	private List<PathwayElementMenuHook> hooks = new ArrayList<PathwayElementMenuHook>();

	public void addPathwayElementMenuHook(PathwayElementMenuHook hook) {
		hooks.add(hook);
	}

	public void removePathwayElementMenuHook(PathwayElementMenuHook hook) {
		hooks.remove(hook);
	}

	/**
	 * This should be implemented by plug-ins that wish to hook into the Pathway
	 * Element Menu
	 */
	public interface PathwayElementMenuHook {
		public void pathwayElementMenuHook(VElement e, JPopupMenu menu);
	}

	/**
	 * Get an instance of a {@link JPopupMenu} for a given {@link VElement}
	 * 
	 * @param e The {@link VElement} to create the popup menu for. If e is an
	 *          instance of {@link Handle}, the menu is based on the parent element.
	 * @return The {@link JPopupMenu} for the given pathway element
	 */
	private JPopupMenu getMenuInstance(SwingEngine swingEngine, VElement e) {
		if (e instanceof VCitation)
			return null;

		JMenu pathLitRef = null;
		if (e instanceof Handle) {
			e = ((Handle) e).getParent();
			pathLitRef = new JMenu("Literature for pathway");
		}

		VPathwayModel vp = e.getDrawing();
		VPathwayModelSwing component = (VPathwayModelSwing) vp.getWrapper();
		ViewActions vActions = vp.getViewActions();

		JPopupMenu menu = new JPopupMenu();

		// Don't show delete if the element cannot be deleted
		if (!(e instanceof VInfoBox)) {
			menu.add(vActions.delete1);
		}

		JMenu selectMenu = new JMenu("Select");
		selectMenu.add(vActions.selectAll);
		selectMenu.add(vActions.selectDataNodes);
		selectMenu.add(vActions.selectInteractions);
		selectMenu.add(vActions.selectLines);
		selectMenu.add(vActions.selectShapes);
		selectMenu.add(vActions.selectLabels);
		menu.add(selectMenu);
		menu.addSeparator();

		// new feature to copy and paste with the right-click menu
		menu.add(vActions.copy);

		PositionPasteAction a = vActions.positionPaste;
		Point loc = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(loc, component);
		a.setPosition(loc);

		menu.add(a);
		menu.addSeparator();

		// ========================================
		// Group View Actions
		// ========================================
		JMenu groupMenu = new JMenu("Create Group");
		groupMenu.add(vActions.toggleGroup); // Group (default)
		groupMenu.add(vActions.toggleComplex); // Complex
		groupMenu.add(vActions.togglePathway); // Pathway
		groupMenu.add(vActions.toggleAnalog); // Analog
		groupMenu.add(vActions.toggleParalog); // Paralog
		groupMenu.add(vActions.toggleTransparent); // Transparent

		// Show group/ungroup when multiple objects or a group are selected
		if ((e instanceof VGroup)) {
			menu.add(vActions.toggleGroup);
			menu.add(vActions.addAlias); // show "add alias" if a group is selected
			menu.addSeparator();
		} else if (vp.getSelectedGraphics().size() > 1) {
			boolean includesGroup = false;
			for (VDrawable p : vp.getSelectedGraphics()) {
				if (p instanceof VGroup) {
					includesGroup = true;
				}
			}
			if (includesGroup) {
				menu.add(vActions.toggleGroup);
				menu.add(vActions.addAlias); // show "add alias" if a group is selected
			} else {
				menu.add(groupMenu);
			}
		}

		// ========================================
		// DataNode View Actions
		// ========================================
		JMenu stateMenu = new JMenu("Add State...");
		stateMenu.add(vActions.addStateProteinModification);
		stateMenu.add(vActions.addStateGeneticVariant);
		stateMenu.add(vActions.addStateEpigeneticModification);
		stateMenu.add(vActions.addStateUndefined);
		if (e instanceof VDataNode) {
			menu.add(stateMenu);
			DataNode dn = ((VDataNode) e).getPathwayObject();
			if (dn.getType() == DataNodeType.ALIAS) {
				if (dn.getAliasRef() != null) {
					menu.add(vActions.unlinkAliasRef);
				} else {
					menu.add(vActions.linkAliasRef); //TODO 
				}
			}
		}
		// ========================================
		// State View Actions
		// ========================================
		if (e instanceof VState) {
			menu.add(vActions.removeState);
		}

		// ========================================
		// Line Element View Actions
		// ========================================
		if ((e instanceof VLineElement)) {
			final VLineElement line = (VLineElement) e;

			menu.add(vActions.addAnchor);

			if (line.getPathwayObject().getConnectorType() == ConnectorType.SEGMENTED) {
				menu.add(vActions.addWaypoint);
				menu.add(vActions.removeWaypoint);
			}

			JMenu typeMenu = new JMenu("Connector Type");

			ButtonGroup buttons = new ButtonGroup();

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					line.getPathwayObject().setConnectorType(ConnectorType.fromName(e.getActionCommand()));
				}
			};
			for (ConnectorType t : ConnectorType.getValues()) {
				JRadioButtonMenuItem mi = new JRadioButtonMenuItem(t.getName());
				mi.setActionCommand(t.getName());
				mi.setSelected(t.equals(line.getPathwayObject().getConnectorType()));
				mi.addActionListener(listener);
				typeMenu.add(mi);
				buttons.add(mi);
			}
			menu.add(typeMenu);
		}

		if ((e instanceof VAnchor)) {
			final VAnchor anchor = ((VAnchor) e);

			JMenu anchorMenu = new JMenu("Anchor type");
			ButtonGroup buttons = new ButtonGroup();

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					anchor.getAnchor().setShapeType(AnchorShapeType.fromName(e.getActionCommand()));
				}
			};

			for (AnchorShapeType at : AnchorShapeType.getValues()) {
				JRadioButtonMenuItem mi = new JRadioButtonMenuItem(at.getName());
				mi.setActionCommand(at.getName());
				mi.setSelected(at.equals(anchor.getAnchor().getShapeType()));
				mi.addActionListener(listener);
				anchorMenu.add(mi);
				buttons.add(mi);
			}

			menu.add(anchorMenu);
		}

		// ========================================
		// Z-Order View Actions
		// ========================================
		JMenu orderMenu = new JMenu("Order");
		orderMenu.add(vActions.orderBringToFront);
		orderMenu.add(vActions.orderSendToBack);
		orderMenu.add(vActions.orderUp);
		orderMenu.add(vActions.orderDown);
		menu.add(orderMenu);

		// ========================================
		// Literature View Actions TODO
		// ========================================
		if (e instanceof VPathwayElement) {
			JMenu litMenu = new JMenu("References");
			litMenu.add(new AddCitationAction(swingEngine, component, e));
			litMenu.add(new EditCitationAction(swingEngine, component, e));
			menu.add(litMenu);

			menu.addSeparator();
			menu.add(new PropertiesAction(swingEngine, component, e));
		}

		if (pathLitRef != null) {
			menu.addSeparator();
			pathLitRef.add(new AddCitationAction(swingEngine, component,
					swingEngine.getEngine().getActiveVPathwayModel().getVInfoBox()));
			pathLitRef.add(new EditCitationAction(swingEngine, component,
					swingEngine.getEngine().getActiveVPathwayModel().getVInfoBox()));
			menu.add(pathLitRef);
		}

		// ========================================
		// Label View Actions
		// ========================================
		if (e instanceof VLabel) {
			menu.addSeparator();
			menu.add(new CommonActions.AddHrefAction(e, swingEngine));
		}

		// ========================================
		// Etc.
		// ========================================
		menu.addSeparator();

		// give plug-ins a chance to add menu items.
		for (PathwayElementMenuHook hook : hooks) {
			hook.pathwayElementMenuHook(e, menu);
		}
		return menu;
	}

	private SwingEngine swingEngine;

	PathwayElementMenuListener(SwingEngine swingEngine) {
		this.swingEngine = swingEngine;
	}

	public void vPathwayModelEvent(VPathwayModelEvent e) {
		switch (e.getType()) { // TODO
		case ELEMENT_CLICKED_DOWN:
			if (e.getAffectedElement() instanceof VCitation) {
				VCitation c = (VCitation) e.getAffectedElement();
				PathwayElementDialog d = swingEngine.getPopupDialogHandler()
						.getInstance(c.getParent().getPathwayObject(), false, null, null);
				d.selectPathwayElementPanel(PathwayElementDialog.TAB_CITATIONS);
				d.setVisible(true);
				break;
			}
		case ELEMENT_CLICKED_UP:
			assert (e.getVPathwayModel() != null);
			assert (e.getVPathwayModel().getWrapper() instanceof VPathwayModelSwing);

			if (e.getMouseEvent().isPopupTrigger()) {
				Component invoker = (VPathwayModelSwing) e.getVPathwayModel().getWrapper();
				MouseEvent me = e.getMouseEvent();
				JPopupMenu m = getMenuInstance(swingEngine, e.getAffectedElement());
				if (m != null) {
					m.show(invoker, me.getX(), me.getY());
				}
			}
			break;
		}
	}
}
