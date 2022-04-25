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
package org.pathvisio.core.view.model;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.libgpml.model.connector.ConnectorShape;
import org.pathvisio.libgpml.model.connector.FreeConnectorShape;
import org.pathvisio.libgpml.model.shape.ShapeType;
import org.pathvisio.libgpml.model.type.AnchorShapeType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.model.LineElement;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.DataNode.State;
import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.model.GraphicalLine;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.Shape;
import org.pathvisio.libgpml.model.ShapedElement;
import org.pathvisio.libgpml.model.LineElement.LinePoint;
import org.pathvisio.libgpml.model.type.StateType;
import org.pathvisio.core.util.Resources;
import org.pathvisio.libgpml.util.Utils;
import org.pathvisio.core.view.model.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.model.SelectionBox.SelectionListener;

/**
 * A collection of {@link Action}s related to the pathway view. An instance of
 * this class contains actions bound to one instance of a {@link VPathwayModel}
 * (non-static fields). The static inner classes are not bound to a particular
 * {@link VPathwayModel}, but act on the currently active pathway by calling
 * {@link Engine#getActiveVPathwayModel()}.
 *
 * Instances of actions may be registered to one or more groups, which changes
 * the action's property on certain events (see GROUP* constants). The
 * {@link Action} instances that are fields of this class are already registered
 * to the proper groups.
 *
 * An instance of this class belonging to a {@link VPathwayModel} can be
 * obtained using {@link VPathwayModel#getViewActions()}.
 *
 * @author thomas, finterly
 */
public class ViewActions implements VPathwayModelListener, SelectionListener {
	private static final URL IMG_COPY = Resources.getResourceURL("copy.gif");
	private static final URL IMG_PASTE = Resources.getResourceURL("paste.gif");
	private static final URL IMG_UNDO = Resources.getResourceURL("undo.gif");

	/**
	 * The group of actions that will be enabled when the VPathway is in edit mode
	 * and disabled when not
	 */
	public static final String GROUP_ENABLE_EDITMODE = "editmode";

	/**
	 * The group of actions that will be enabled when a VPathway is loaded and
	 * disabled when not
	 */
	public static final String GROUP_ENABLE_VPATHWAY_LOADED = "vpathway";

	/**
	 * The group of actions that will be enabled when the selection isn't empty
	 */
	public static final String GROUP_ENABLE_WHEN_SELECTION = "selection";

	static final int SMALL_INCREMENT = 2;
	static final int LARGE_INCREMENT = 20;

	VPathwayModel vPathwayModel;

	// ========================================
	// Selection Actions
	// ========================================
	public final SelectClassAction selectDataNodes;
	public final SelectObjectAction selectInteractions;
	public final SelectObjectAction selectLines;
	public final SelectObjectAction selectShapes;
	public final SelectObjectAction selectLabels;
	public final SelectAllAction selectAll;

	// ========================================
	// DataNode Actions
	// ========================================
	public final AddAliasAction addAlias;
	public final LinkAliasRefAction linkAliasRef;
	public final UnlinkAliasRefAction unlinkAliasRef;
	public final AddState addStateProteinModification;
	public final AddState addStateGeneticVariant;
	public final AddState addStateEpigeneticModification;
	public final AddState addStateUndefined;

	// ========================================
	// State Actions
	// ========================================
	public final RemoveState removeState;

	// ========================================
	// LineElement Actions
	// ========================================
	public final AddAnchorAction addAnchor;
	public final WaypointAction addWaypoint;
	public final WaypointAction removeWaypoint;
	public final ShowUnlinkedAction showUnlinked;

	// ========================================
	// Group Actions
	// ========================================
	public final GroupAction toggleGroup;
	public final GroupAction toggleComplex;
	public final GroupAction togglePathway;
	public final GroupAction toggleAnalog;
	public final GroupAction toggleParalog;
	public final GroupAction toggleTransparent;

	// ========================================
	// Basic Actions
	// ========================================
	public final DeleteAction delete1;
	public final DeleteAction delete2;
	public final CopyAction copy;
	public final PasteAction paste;
	public final PositionPasteAction positionPaste;
	public final KeyMoveAction keyMove;
	public final UndoAction undo;

	// ========================================
	// Z-Order Actions
	// ========================================
	public final OrderBottomAction orderSendToBack;
	public final OrderTopAction orderBringToFront;
	public final OrderUpAction orderUp;
	public final OrderDownAction orderDown;

	// ========================================
	// Etc. Actions
	// ========================================
	public final TextFormattingAction formatText;

	private final Engine engine;

	/**
	 * Constructor for this class
	 * 
	 * @param engine the engine.
	 * @param vp     the VPathwayModel.
	 */
	ViewActions(Engine engine, VPathwayModel vp) {
		this.engine = engine;
		vPathwayModel = vp;

		vp.addSelectionListener(this);
		vp.addVPathwayListener(this);

		// ================================================================================
		// Selection Actions
		// ================================================================================
		selectDataNodes = new SelectClassAction("DataNode", VDataNode.class);
		selectInteractions = new SelectObjectAction("Interactions", Interaction.class);
		selectLines = new SelectObjectAction("Graphical Lines", GraphicalLine.class);
		selectShapes = new SelectObjectAction("Shapes", Shape.class);
		selectLabels = new SelectObjectAction("Labels", Label.class);
		selectAll = new SelectAllAction();

		// ================================================================================
		// DataNode Actions
		// ================================================================================
		addAlias = new AddAliasAction();
		linkAliasRef = new LinkAliasRefAction();
		unlinkAliasRef = new UnlinkAliasRefAction();
		addStateProteinModification = new AddState(StateType.PROTEIN_MODIFICATION);
		addStateGeneticVariant = new AddState(StateType.GENETIC_VARIANT);
		addStateEpigeneticModification = new AddState(StateType.EPIGENETIC_MODIFICATION);
		addStateUndefined = new AddState(StateType.UNDEFINED);

		// ================================================================================
		// State Actions
		// ================================================================================
		removeState = new RemoveState();

		// ================================================================================
		// LineElement Actions
		// ================================================================================
		addAnchor = new AddAnchorAction();
		addWaypoint = new WaypointAction(true);
		removeWaypoint = new WaypointAction(false);
		showUnlinked = new ShowUnlinkedAction(engine);

		// ================================================================================
		// Group Actions
		// ================================================================================
		toggleGroup = new GroupAction(GroupType.GROUP);
		toggleComplex = new GroupAction(GroupType.COMPLEX);
		togglePathway = new GroupAction(GroupType.PATHWAY);
		toggleAnalog = new GroupAction(GroupType.ANALOG);
		toggleParalog = new GroupAction(GroupType.PARALOG);
		toggleTransparent = new GroupAction(GroupType.TRANSPARENT);

		// ================================================================================
		// Basic Actions
		// ================================================================================
		delete1 = new DeleteAction(java.awt.event.KeyEvent.VK_DELETE);
		delete2 = new DeleteAction(java.awt.event.KeyEvent.VK_BACK_SPACE);
		copy = new CopyAction(engine);
		paste = new PasteAction(engine);
		positionPaste = new PositionPasteAction(engine);
		keyMove = new KeyMoveAction(engine, null);
		undo = new UndoAction(engine);

		// ================================================================================
		// Z-Order Actions
		// ================================================================================
		orderSendToBack = new OrderBottomAction(engine);
		orderBringToFront = new OrderTopAction(engine);
		orderUp = new OrderUpAction(engine);
		orderDown = new OrderDownAction(engine);

		// ========================================
		// Etc.
		// ========================================
		formatText = new TextFormattingAction(engine, null);

		// ================================================================================
		// Register Actions //TODO
		// ================================================================================
		registerToGroup(selectDataNodes, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(selectAll, GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(toggleGroup, GROUP_ENABLE_EDITMODE);
		registerToGroup(toggleGroup, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(toggleComplex, GROUP_ENABLE_EDITMODE);
		registerToGroup(toggleComplex, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(delete1, GROUP_ENABLE_EDITMODE);
		registerToGroup(delete1, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(copy, ViewActions.GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(paste, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(paste, ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(positionPaste, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
		registerToGroup(positionPaste, ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(keyMove, ViewActions.GROUP_ENABLE_EDITMODE);
		registerToGroup(addAnchor, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(addWaypoint, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(removeWaypoint, GROUP_ENABLE_WHEN_SELECTION);
		registerToGroup(showUnlinked, GROUP_ENABLE_VPATHWAY_LOADED);

		resetGroupStates();
	}

	// ================================================================================
	// Maps
	// ================================================================================
	private Map<String, Set<Action>> actionGroups = new HashMap<String, Set<Action>>();
	private Map<Action, Set<String>> groupActions = new HashMap<Action, Set<String>>();

	// ================================================================================
	// Register Methods
	// ================================================================================
	/**
	 * Register the given action to a group (one of the GROUP* constants)
	 * 
	 * @param a     The action to register
	 * @param group The group to register the action to
	 */
	public void registerToGroup(Action a, String group) {
		Utils.multimapPut(actionGroups, group, a);
		Utils.multimapPut(groupActions, a, group);
	}

	/**
	 * Register the given actions to a group (one of the GROUP* constants)
	 * 
	 * @param actions The actions to register
	 * @param group   The group to register the actions to
	 */
	public void registerToGroup(Action[] actions, String group) {
		for (Action a : actions)
			registerToGroup(a, group);
	}

	/**
	 * Register the given actions to a group (one of the GROUP* constants)
	 * 
	 * @param actions The actions to register
	 * @param group   The group to register the actions to
	 */
	public void registerToGroup(Action[][] actions, String group) {
		for (Action[] aa : actions) {
			for (Action a : aa)
				registerToGroup(a, group);
		}
	}

	// ================================================================================
	// Group State Methods
	// ================================================================================
	/**
	 * Resets the group state for the registered actions to the VPathway's state
	 * e.g. all actions in GROUP_ENABLE_EDITMODE will be enabled when the pathway is
	 * in edit mode, and disabled when not.
	 */
	public void resetGroupStates() {
		resetGroupStates(vPathwayModel);
	}

	Map<String, Boolean> groupState = new HashMap<String, Boolean>();

	/**
	 * Resets the group state for the registered actions to the given VPathway's
	 * state e.g. all actions in GROUP_ENABLE_EDITMODE will be enabled when the
	 * pathway is in edit mode, and disabled when not.
	 * 
	 * @param v The VPathway of which the state will be determined
	 */
	private void resetGroupStates(VPathwayModel v) {
		groupState.put(GROUP_ENABLE_VPATHWAY_LOADED, true);
		groupState.put(GROUP_ENABLE_EDITMODE, vPathwayModel.isEditMode());
		groupState.put(GROUP_ENABLE_WHEN_SELECTION, vPathwayModel.getSelectedPathwayElements().size() > 0);

		for (Action a : groupActions.keySet()) {
			Set<String> groups = groupActions.get(a);
			boolean enable = true;
			for (String g : groups) {
				enable &= groupState.get(g);
			}
			a.setEnabled(enable);
		}
	}

//	public void applicationEvent(ApplicationEvent e) {
//	if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
//	VPathway vp = (VPathway)e.getSource();
//	vp.addSelectionListener(this);
//	vp.addVPathwayListener(this);
//	setGroupEnabled(true, GROUP_ENABLE_VPATHWAY_LOADED);
//	setGroupEnabled(vp.getSelectedGraphics().size() > 0, GROUP_ENABLE_WHEN_SELECTION);
//	setGroupEnabled(vp.isEditMode(), GROUP_ENABLE_EDITMODE);
//	}
//	}

	// ================================================================================
	// Event Methods
	// ================================================================================
	public void vPathwayModelEvent(VPathwayModelEvent e) {
		VPathwayModel vp = (VPathwayModel) e.getSource();
		// Don't refresh at object redraw / move
		switch (e.getType()) {
		case EDIT_MODE_OFF:
		case EDIT_MODE_ON:
		case ELEMENT_ADDED:
		case MODEL_LOADED:
		case ELEMENT_CLICKED_UP:
			resetGroupStates(vp);
		default:
			break;
		}
	}

	public void selectionEvent(SelectionEvent e) {
		VPathwayModel vp = ((SelectionBox) e.getSource()).getDrawing();
		resetGroupStates(vp);
	}

//	private abstract class EnableOnSelectAction extends AbstractAction implements SelectionListener {
//	public EnableOnSelectAction() {
//	vPathway.addSelectionListener(this);
//	}

//	public void selectionEvent(SelectionEvent e) {
//	setEnabled(vPathway.getSelectedGraphics().size() > 0);
//	}
//	}

	// ================================================================================
	//
	// Classes for Abstract Actions Below:
	//
	// ================================================================================

	/**
	 * Selects objects.
	 * 
	 * @author unknown
	 */
	private class SelectClassAction extends AbstractAction {

		Class<?> c;

		public SelectClassAction(String name, Class<?> c) {
			super("Select all " + name + "s");
			this.c = c;
		}

		public void actionPerformed(ActionEvent e) {
			vPathwayModel.selectObjects(c);
		}
	}

	/**
	 * Selects all objects of a given ObjectType
	 * 
	 * @author anwesha
	 */
	private class SelectObjectAction extends AbstractAction {

		private Class objectClass;

		public SelectObjectAction(String name, Class objectClass) {
			super("Select all " + name);
			this.objectClass = objectClass;
		}

		public void actionPerformed(ActionEvent e) {
			vPathwayModel.selectObjectsByObjectType(objectClass);
		}
	}

	// private abstract class EnableOnSelectAction extends AbstractAction implements
	// SelectionListener {
	// public EnableOnSelectAction() {
	// vPathway.addSelectionListener(this);
	// }

	// public void selectionEvent(SelectionEvent e) {
	// setEnabled(vPathway.getSelectedGraphics().size() > 0);
	// }
	// }

	// ================================================================================
	//
	// Classes for Abstract Actions Below:
	//
	// ================================================================================

	/**
	 * Selects all objects.
	 * 
	 * @author unknown
	 */
	private class SelectAllAction extends AbstractAction {

		public SelectAllAction() {
			super("Select all");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			vPathwayModel.selectAll();
		}
	}

	/**
	 * Adds Alias DataNode for a Group.
	 * 
	 * @author finterly
	 */
	private class AddAliasAction extends AbstractAction {

		// alias is added to the right of its group aliasRef
		private final static int POSITION_OFFSET = 65;

		AddAliasAction() {
			super("Add Alias...");
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent arg0) {
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Add Alias");
				for (VDrawable g : selection) {
					if (g instanceof VGroup) {
						Group gp = (Group) g.getPathwayObject();
						// default alias
						DataNode elt = gp.addAlias("Alias");
						DefaultTemplates.setInitialSize(elt);
						elt.setCenterX(gp.getCenterX() + (gp.getWidth() / 2) + POSITION_OFFSET);
						elt.setCenterY(gp.getCenterY());
						elt.setShapeType(ShapeType.OVAL);
						elt.setZOrder(vPathwayModel.getMaxZOrder() + 1);
					}
				}
			}
		}

	}

	/**
	 * Links an AliasRef Group to an Alias DataNode.
	 * 
	 * @author finterly
	 */
	public class LinkAliasRefAction extends AbstractAction {

		SelectionBox currentSelection;

		LinkAliasRefAction() {
			super("Link to Group...");
			this.currentSelection = vPathwayModel.selection;
		}

		/**
		 * Requests linking of the selected alias data node to a group.
		 * 
		 * <p>
		 * NB:
		 * <ol>
		 * <li>Calls {@link VPathwayModel#startLinkAliasRef} to initiate process.
		 * <li>Message dialog instructs user to click on a group to link this alias data
		 * node.
		 * <li>When user clicks/mouse-up {@link VPathwayModel#handleLinkAliasRef} links
		 * this alias dataNode to a group.
		 * </ol>
		 */
		public void actionPerformed(ActionEvent arg0) {
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Add AliasRef");
				for (VDrawable d : selection) {
					if (d instanceof VDataNode) {
						DataNode dn = (DataNode) d.getPathwayObject();
						if (dn.getType() == DataNodeType.ALIAS) { // precautionary check if type Alias
							vPathwayModel.startLinkAliasRef(dn);
							JOptionPane.showConfirmDialog(null, "Please click on a Group to Link Alias DataNode",
									"Message", JOptionPane.PLAIN_MESSAGE);
						}
					}
				}
			}
		}
	}

	/**
	 * UnLinks an AliasRef Group from an Alias DataNode.
	 * 
	 * @author finterly
	 */
	private class UnlinkAliasRefAction extends AbstractAction {

		UnlinkAliasRefAction() {
			super("Unlink from Group...");
		}

		/**
		 * Unlinks the selected alias data node from its aliasRef group.
		 */
		public void actionPerformed(ActionEvent arg0) {
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Unlink AliasRef");
				for (VDrawable d : selection) {
					if (d instanceof VDataNode) {
						DataNode dn = (DataNode) d.getPathwayObject();
						dn.unsetAliasRef();
					}
				}
			}
		}
	}

	/**
	 * Adds a State to the current DataNode.
	 * 
	 * @author unknown
	 */
	private class AddState extends AbstractAction {

		private StateType type;

		AddState(StateType type) {
			super(type.toString());
			this.type = type;

		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent arg0) {
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Add State");
				for (VDrawable g : selection) {
					if (g instanceof VDataNode) {
						VDataNode gp = (VDataNode) g;
						// default state
						State elt = gp.getPathwayObject().addState("", type, 1.0, 1.0);
						DefaultTemplates.setInitialSize(elt);
						elt.setShapeType(ShapeType.OVAL);
//						engine.getActivePathwayModel().add(elt); TODO 
					}
				}
			}
		}
	}

	/**
	 * Removes the current State from its DataNode.
	 * 
	 * @author unknown
	 */
	private class RemoveState extends AbstractAction {
		RemoveState() {
			super("Remove State...");
		}

		public void actionPerformed(ActionEvent arg0) {
			vPathwayModel.getUndoManager().newAction("Remove State");
			List<VElement> toRemove = new ArrayList<VElement>();
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() > 0) {
				for (VDrawable g : selection) {
					if (g instanceof VState) {
						toRemove.add((VElement) g);
					}
				}
			}
			if (toRemove.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Remove State(s)");
				vPathwayModel.removeDrawingObjects(toRemove, true);
			}

		}
	}

	/**
	 * Add/remove waypoints on a segmented line.
	 * 
	 * @author thomas
	 */
	private class WaypointAction extends AbstractAction implements SelectionListener {
		boolean add;

		public WaypointAction(boolean add) {
			this.add = add;

			if (add) {
				putValue(NAME, "Add Waypoint");
				putValue(SHORT_DESCRIPTION, "Add a waypoint to the selected line");
			} else {
				putValue(NAME, "Remove Last Waypoint");
				putValue(SHORT_DESCRIPTION, "Removes the last waypoint from the selected line");
			}
			vPathwayModel.addSelectionListener(this);
			setEnabled(false);
		}

		public void selectionEvent(SelectionEvent e) {
			boolean enable = false;
			if (e.selection.size() == 1) {
				VElement ve = e.selection.iterator().next();
				if (ve instanceof VLineElement) {
					ConnectorShape s = ((LineElement) ((VLineElement) ve).getPathwayObject()).getConnectorShape();
					enable = s instanceof FreeConnectorShape;
				} else {
					enable = false;
				}
			}
			setEnabled(enable);
		}

		public void actionPerformed(ActionEvent e) {
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() == 1) {
				VDrawable g = selection.get(0);
				if (g instanceof VLineElement) {
					VLineElement l = (VLineElement) g;
					ConnectorShape s = ((LineElement) l.getPathwayObject()).getConnectorShape();
					if (s instanceof FreeConnectorShape) {
						vPathwayModel.getUndoManager().newAction("" + getValue(NAME));
						if (add) {
							addWaypoint((FreeConnectorShape) s, (LineElement) l.getPathwayObject());
						} else {
							removeWaypoint((LineElement) l.getPathwayObject());
						}
					}
				}
			}
		}

		private void removeWaypoint(LineElement l) {
			// TODO: Instead of removing the last point, it would be better to adjust the
			// context
			// menu to remove a specific point (like with anchors). This could be done by
			// making
			// VPoint extend VPathwayElement so we can directly get the selected waypoint
			// here.
			List<LinePoint> newPoints = new ArrayList<LinePoint>(l.getLinePoints());
			newPoints.remove(newPoints.size() - 2);
			l.setLinePoints(newPoints);
		}

		private void addWaypoint(FreeConnectorShape s, LineElement l) {
			// TODO: It would be nice to have access to the mouse position here, so
			// we can add the waypoint to where the user clicked
			// Point2D mp = new Point2D.Double(vPathway.mFromV(p.getX()),
			// vPathway.mFromV(p.getY()));
			// WayPoint nwp = new WayPoint(mp);
			// double c = s.toLineCoordinate(p);

			// We don't have the mouse position, just add the waypoint in the center
			// with an offset if needed
			List<LinePoint> oldPoints = l.getLinePoints();
			List<LinePoint> newPoints = new ArrayList<LinePoint>(oldPoints);

			int i = oldPoints.size() - 1; // MPoints size always >= 2
			LinePoint mp = oldPoints.get(i);
			LinePoint mp2 = oldPoints.get(i - 1);
			double mc = s.toLineCoordinate(mp.toPoint2D());
			double mc2 = s.toLineCoordinate(mp2.toPoint2D());
			double c = mc2 + (mc - mc2) / 2.0; // Add new waypoint on center of last segment
			Point2D p = s.fromLineCoordinate(c);
			newPoints.add(i, l.new LinePoint(p.getX(), p.getY()));
			l.setLinePoints(newPoints);
		}
	}

	/**
	 * Adds an Anchor to the current LineElement.
	 * 
	 * @author unknown
	 */
	private class AddAnchorAction extends AbstractAction implements SelectionListener {

		/**
		 * 
		 */
		public AddAnchorAction() {
			vPathwayModel.addSelectionListener(this);
			putValue(NAME, "Add Anchor");
			putValue(SHORT_DESCRIPTION, "Add an anchor point to the selected line");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			setEnabled(false);
		}

		/**
		 *
		 */
		public void selectionEvent(SelectionEvent e) {
			boolean enable = false;
			for (VElement ve : e.selection) {
				if (ve instanceof VLineElement) {
					enable = true;
				} else {
					enable = false;
					break;
				}
			}
			setEnabled(enable);
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			if (selection.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Add Anchor");
				for (VDrawable g : selection) {
					if (g instanceof VLineElement) {
						VLineElement l = (VLineElement) g;
						l.getPathwayObject().addAnchor(0.4, AnchorShapeType.SQUARE);
					}
				}
			}
		}
	}

	/**
	 * Action that toggles highlight of Interaction points that are not linked to an
	 * object.
	 *
	 * @author unknown
	 */
	public static class ShowUnlinkedAction extends AbstractAction {

		Engine engine;

		public ShowUnlinkedAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Highlight Unlinked Interactions");
			putValue(SHORT_DESCRIPTION, "Highlight all interactions that are not linked");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			vp.resetHighlight();
			for (PathwayElement pe : vp.getPathwayModel().getPathwayElements()) {
				if (pe.getObjectType() == ObjectType.INTERACTION) { // check interactions only
					VLineElement vl = (VLineElement) vp.getPathwayElementView(pe);
					LinkableTo grs = ((LineElement) pe).getStartElementRef();
					LinkableTo gre = ((LineElement) pe).getEndElementRef();
					if (grs == null) {
						vl.getStart().highlight();
					}
					if (gre == null) {
						vl.getEnd().highlight();
					}
				}
			}
		}
	}

	/**
	 * Group actions.
	 * 
	 * @author unknown
	 */
	private class GroupAction extends GroupActionBase {
		public GroupAction(GroupType type) {
			super(type.getName(), "Break group", "Group selected elements", "Ungroup selected group", type);
		}
	}

	/**
	 * Group actions base.
	 * 
	 * @author unknown
	 */
	private class GroupActionBase extends AbstractAction implements SelectionListener {
		private String groupLbl, ungroupLbl, groupTt, ungroupTt;
		private GroupType type;

		/**
		 * @param groupLbl
		 * @param ungroupLbl
		 * @param groupTt
		 * @param ungroupTt
		 * @param type
		 * @param keyStroke
		 */
		public GroupActionBase(String groupLbl, String ungroupLbl, String groupTt, String ungroupTt, GroupType type) {
			super();
			this.type = type;
			this.groupLbl = groupLbl;
			this.ungroupLbl = ungroupLbl;
			this.groupTt = groupTt;
			this.ungroupTt = ungroupTt;
			vPathwayModel.addSelectionListener(this);
			putValue(NAME, groupLbl);
			putValue(SHORT_DESCRIPTION, groupTt);
			setLabel();
		}

		/**
		 * Action creates Group and sets type.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!isEnabled()) {
				return; // Don't perform action if not enabled
			}
			VGroup g = vPathwayModel.toggleGroup(vPathwayModel.getSelectedGraphics());
			if (g != null) {
				g.getPathwayObject().setType(type);
				// TODO !!!!
				if (vPathwayModel != null) {
					vPathwayModel.getUndoManager().newAction("Change Group");
				}
			}
		}

		/**
		 *
		 */
		@Override
		public void selectionEvent(SelectionEvent e) {
			switch (e.type) {
			case SelectionEvent.OBJECT_ADDED:
			case SelectionEvent.OBJECT_REMOVED:
			case SelectionEvent.SELECTION_CLEARED:
				setLabel();
			}
		}

		/**
		 * 
		 */
		private void setLabel() {
			int unGrouped = 0;
			List<VDrawable> selection = vPathwayModel.getSelectedGraphics();
			for (VDrawable g : selection) {
				// TODO group only?
				if (((VGroupable) g).getPathwayObject().getGroupRef() == null) {
					unGrouped++;
				}
			}
			setEnabled(true);
			if (unGrouped >= 2) {
				putValue(Action.NAME, groupLbl);
				putValue(SHORT_DESCRIPTION, groupTt);
			} else {
				putValue(Action.NAME, ungroupLbl);
				putValue(SHORT_DESCRIPTION, ungroupTt);
			}
		}
	}

	/**
	 * Delete action.
	 * 
	 * @author unknown
	 */
	private class DeleteAction extends AbstractAction {

		public DeleteAction(int ke) {
			super();
			putValue(NAME, "Delete");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ke, 0));
		}

		public void actionPerformed(ActionEvent e) {
			if (!isEnabled())
				return; // Don't perform action if not enabled

			List<VElement> toRemove = new ArrayList<VElement>();
			for (VElement o : vPathwayModel.getDrawingObjects()) {
				if (!o.isSelected() || o == vPathwayModel.selection || o == vPathwayModel.getVInfoBox())
					continue; // Object not selected, skip
				toRemove.add(o);
			}
			if (toRemove.size() > 0) {
				vPathwayModel.getUndoManager().newAction("Delete Element(s)");
				vPathwayModel.removeDrawingObjects(toRemove, true);
			}
		}
	}

	/**
	 * Copy command in the menu / toolbar, copies selected pathway elements to the
	 * clipboard
	 * 
	 * @author unknown.
	 */
	public static class CopyAction extends AbstractAction {
		Engine engine;

		public CopyAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Copy");
			putValue(SMALL_ICON, new ImageIcon(IMG_COPY));
			String descr = "Copy selected pathway objects to clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null)
				vp.copyToClipboard();
		}
	}

	/**
	 * Paste command in the menu / toolbar, pastes from clipboard.
	 * 
	 * @author unknown.
	 */
	public static class PasteAction extends AbstractAction {
		Engine engine;

		public PasteAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Paste");
			putValue(SMALL_ICON, new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway objects from clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (isEnabled() && vp != null)
				vp.pasteFromClipboard();
		}
	}

	/**
	 * Paste command from the right click menu, pastes from clipboard.
	 * 
	 * @author unknown.
	 */
	public static class PositionPasteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		Engine engine;
		private Point position;

		public PositionPasteAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Paste");
			putValue(SMALL_ICON, new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway objects from clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			// putValue(Action.ACCELERATOR_KEY,
			// KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
			// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (isEnabled() && vp != null) {
				vp.positionPasteFromClipboard(position);
			}
			resetPosition();
		}

		public void setPosition(Point position) {
			this.position = position;
		}

		private void resetPosition() {
			position = null;
		}
	}

	/**
	 * Nudge action, moves selected element(s) a bit in the direction of the cursor
	 * key pressed.
	 * 
	 * @author unknown.
	 */
	public static class KeyMoveAction extends AbstractAction {
		Engine engine;
		KeyStroke key;

		public KeyMoveAction(Engine engine, KeyStroke key) {
			this.engine = engine;
			this.key = key;
		}

		public void actionPerformed(ActionEvent e) {

			int moveIncrement = 0;

			if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
				moveIncrement = LARGE_INCREMENT;
			} else {
				moveIncrement = SMALL_INCREMENT;
			}

			VPathwayModel vp = engine.getActiveVPathwayModel();
			vp.moveByKey(key, moveIncrement);
		}
	}

	/**
	 * Undo command in the menu / toolbar
	 * 
	 * @author unknown
	 */
	public static class UndoAction extends AbstractAction implements UndoManagerListener, ApplicationEventListener {
		Engine engine;

		public UndoAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(NAME, "Undo");
			putValue(SHORT_DESCRIPTION, "Undo last action");
			putValue(SMALL_ICON, new ImageIcon(IMG_UNDO));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			engine.addApplicationEventListener(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null) {
				vp.undo();
			}
		}

		public void undoManagerEvent(UndoManagerEvent e) {
			String msg = e.getMessage();
			putValue(NAME, "Undo: " + msg);
			setEnabled(!msg.equals(UndoManager.CANT_UNDO));
		}

		public void applicationEvent(ApplicationEvent e) {
			switch (e.getType()) {
			case VPATHWAY_CREATED:
				((VPathwayModel) e.getSource()).getUndoManager().addListener(this);
				break;
			case VPATHWAY_DISPOSED:
				((VPathwayModel) e.getSource()).getUndoManager().removeListener(this);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 * 
	 * @author unknown
	 */
	public static class OrderTopAction extends AbstractAction {
		Engine engine;

		public OrderTopAction(Engine engine) {
			this.engine = engine;
			putValue(NAME, "Bring to Front");
			putValue(SHORT_DESCRIPTION, "Bring the element in front of all other elements");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_CLOSE_BRACKET,
					InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null) {
				vp.moveGraphicsTop(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 * 
	 * @author unknown
	 */
	public static class OrderBottomAction extends AbstractAction {
		Engine engine;

		public OrderBottomAction(Engine engine) {
			this.engine = engine;
			putValue(NAME, "Send to Back");
			putValue(SHORT_DESCRIPTION, "Send the element behind all other elements");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_OPEN_BRACKET,
					InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null) {
				vp.moveGraphicsBottom(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 * 
	 * @author unknown
	 */
	public static class OrderUpAction extends AbstractAction {
		Engine engine;

		public OrderUpAction(Engine engine) {
			this.engine = engine;
			putValue(NAME, "Bring Forward");
			putValue(SHORT_DESCRIPTION, "Bring Forward");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_CLOSE_BRACKET,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null) {
				vp.moveGraphicsUp(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action to change the order of the selected object
	 * 
	 * @author unknown
	 */
	public static class OrderDownAction extends AbstractAction {
		Engine engine;

		public OrderDownAction(Engine engine) {
			this.engine = engine;
			putValue(NAME, "Send Backward");
			putValue(SHORT_DESCRIPTION, "Send Backward");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_OPEN_BRACKET,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null) {
				vp.moveGraphicsDown(vp.getSelectedGraphics());
				vp.redraw();
			}
		}
	}

	/**
	 * Action for toggling bold or italic flags on selected elements.
	 * 
	 * @author unknown
	 */
	public static class TextFormattingAction extends AbstractAction {
		Engine engine;
		KeyStroke key;

		/**
		 * @param engine
		 * @param key
		 */
		public TextFormattingAction(Engine engine, KeyStroke key) {
			this.engine = engine;
			this.key = key;
		}

		/**
		 * TODO ShapedElement?
		 */
		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			Set<VElement> changeTextFormat = new HashSet<VElement>();
			changeTextFormat = vp.getSelectedPathwayElements();
			for (VElement velt : changeTextFormat) {
				if (velt instanceof VShapedElement) {
					ShapedElement o = ((VShapedElement) velt).getPathwayObject();
					if (key.equals(VPathwayModel.KEY_BOLD)) {
						if (o.getFontWeight()) // TODO logic???
							o.setFontWeight(false);
						else
							o.setFontWeight(true);
					} else if (key.equals(VPathwayModel.KEY_ITALIC)) {
						if (o.getFontStyle())
							o.setFontStyle(false);
						else
							o.setFontStyle(true);
					}
				}
			}

			vp.redraw();
		}
	}
}
