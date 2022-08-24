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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.pathvisio.core.Engine;
import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.GroupType;
import org.pathvisio.libgpml.model.type.HAlignType;
import org.pathvisio.libgpml.model.CopyElement;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.DataNode.State;
import org.pathvisio.libgpml.model.Drawable;
import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.model.GraphicalLine;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Groupable;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.LineElement;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayModel.StatusFlagEvent;
import org.pathvisio.libgpml.model.PathwayObject;
import org.pathvisio.libgpml.model.Shape;
import org.pathvisio.libgpml.model.ShapedElement;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.LineElement.Anchor;
import org.pathvisio.libgpml.model.LineElement.LinePoint;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayModelEvent;
import org.pathvisio.libgpml.model.PathwayModelListener;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.model.type.VAlignType;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.libgpml.util.Utils;
import org.pathvisio.core.view.KeyEvent;
import org.pathvisio.core.view.LayoutType;
import org.pathvisio.core.view.MouseEvent;
import org.pathvisio.core.view.VElementMouseEvent;
import org.pathvisio.core.view.VElementMouseListener;
import org.pathvisio.core.view.model.SelectionBox.SelectionListener;
import org.pathvisio.core.view.model.VPathwayModelEvent.VPathwayModelEventType;
import org.pathvisio.core.view.model.ViewActions.KeyMoveAction;
import org.pathvisio.core.view.model.ViewActions.TextFormattingAction;

/**
 * This class implements and handles a drawing. Graphics objects are stored in
 * the drawing and can be visualized. The class also provides methods for mouse
 * and key event handling.
 *
 * It's necessary to call PreferenceManager.init() before you can instantiate
 * this class.
 * 
 * @author unknown, finterly
 */
public class VPathwayModel implements PathwayModelListener {

	private static final double FUZZY_SIZE = 8; // fuzz-factor around mouse cursor
	static final int ZORDER_SELECTIONBOX = Integer.MAX_VALUE;
	static final int ZORDER_HANDLE = Integer.MAX_VALUE - 1;

	private PathwayModel data; // the associated {@link PathwayModel}.
	private PathwayModel temporaryCopy = null;
	private VPathwayModelWrapper parent; // may be null, optional gui-specific wrapper for this VPathwayModel.
	private VInfoBox vInfoBox;
	private List<VElement> drawingObjects;// All visible objects (incl. handles; excl. selectionBox objects)
	SelectionBox selection;
	private List<VElement> toAdd = new ArrayList<VElement>();
	private VElement lastEnteredElement = null;
	private VElement pressedObject = null; // VElement that is pressed last mouseDown event
	Template newTemplate = null;

	private boolean editMode = true;
	private boolean selectionEnabled = true;
	private boolean stateCtrl = false; // flags for cursor change if mouse is over a label with href
	private boolean stateEntered = false;// and ctrl button is pressed

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * Constructor for this class.
	 *
	 * @param parent Optional gui-specific wrapper for this VPathway
	 */
	public VPathwayModel(VPathwayModelWrapper parent) {
		// NOTE: you need to call PreferenceManager.init() at application start,
		// before instantiating a VPathway
		// This used to be called by Engine.init(), but not anymore.
		// TODO: make preferencemanager a non-static object, so this check is obsolete.
		if (PreferenceManager.getCurrent() == null) {
			throw new InstantiationError("Please call PreferenceManager.init() before instantiating a VPathway");
		}
		this.parent = parent;

		drawingObjects = new ArrayList<VElement>();

		selection = new SelectionBox(this);

		// Code that uses VPathway have to initialize
		// the keyboard actions explicitly, if necessary.
		// registerKeyboardActions();
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the associated {@link PathwayModel}.
	 * 
	 * @return data
	 */
	public PathwayModel getPathwayModel() {
		return data;
	}

	/**
	 * Returns the optional gui-specific wrapper for this VPathwayModel.
	 * 
	 * @return parent the optional gui-specific wrapper.
	 */
	public VPathwayModelWrapper getWrapper() {
		return parent;
	}

	/**
	 * Returns the VInfoBox containing information on the pathway
	 * 
	 * @return vInfoBox the view of infobox.
	 */
	public VInfoBox getVInfoBox() {
		return vInfoBox;
	}

	/**
	 * Sets the VInfoBox containing information on the pathway
	 *
	 * @param v the view infobox to set.
	 */
	public void setVInfoBox(VInfoBox v) {
		this.vInfoBox = v;
	}

	/**
	 * Returns all VElement on this VPathwayModel.
	 * 
	 * @return drawingObjects
	 */
	public List<VElement> getDrawingObjects() {
		return drawingObjects;
	}

	public void setPressedObject(VElement o) {
		pressedObject = o;
	}

	/**
	 * Method to set the template that provides the new graphics type that has to be
	 * added next time the user clicks on the drawing.
	 *
	 * @param t A template that provides the elements to be added
	 */
	public void setNewTemplate(Template t) {
		newTemplate = t;
	}

	/**
	 * Checks if this drawing is in edit mode.
	 *
	 * @return true if in edit mode, false otherwise. //TODO
	 */
	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Set this drawing to edit mode.
	 *
	 * @param editMode true if edit mode has to be enabled, false if disabled (view
	 *                 mode).
	 */
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		if (!editMode) {
			clearSelection();
		}

		redraw();
		VPathwayModelEventType type = editMode ? VPathwayModelEventType.EDIT_MODE_ON
				: VPathwayModelEventType.EDIT_MODE_OFF;
		fireVPathwayEvent(new VPathwayModelEvent(this, type));
	}

	/**
	 * Returns true if the selection capability of this VPathway is enabled
	 * 
	 * @return true if selection capability is enabled.
	 */
	public boolean getSelectionEnabled() {
		return selectionEnabled;
	}

	/**
	 * You can disable the selection capability of this VPathway by passing false.
	 * This is not used within Pathvisio, but it is meant for embedding VPathway in
	 * other applications, where selections may not be needed.
	 */
	public void setSelectionEnabled(boolean value) {
		selectionEnabled = value;
	}

	// ================================================================================
	// Miscellaneous Methods
	// ================================================================================
	/**
	 * Calculate the board size. Calls {@link VElement#getVBounds()} for every
	 * element and adds all results together to obtain the board size
	 */
	public Dimension calculateVSize() {
		Rectangle2D bounds = new Rectangle2D.Double();
		for (VElement e : drawingObjects) {
			bounds.add(e.getVBounds());
		}
		return new Dimension((int) bounds.getWidth() + 10, (int) bounds.getHeight() + 10);
	}

	/**
	 * Returns true if snap to anchors is enabled.
	 * 
	 * @return true if snap to anchors is enabled.
	 */
	public boolean isSnapToAnchors() {
		return PreferenceManager.getCurrent().getBoolean(GlobalPreference.SNAP_TO_ANCHOR);
	}

	// ================================================================================
	// Model to View Methods
	// ================================================================================
	Map<LinePoint, VPoint> pointsMtoV = new HashMap<LinePoint, VPoint>();

	/**
	 * Returns corresponding {@link VPoint} for given model {@link LinePoint}.
	 * 
	 * @param linePoint the model line point.
	 * @return the view line point for the given model line point.
	 */
	protected VPoint getPoint(LinePoint linePoint) {
		return pointsMtoV.get(linePoint);
	}

	/**
	 * Creates a new {@link VPoint} from given model {@link LinePoint} for given
	 * {@link VLineElement}.
	 * 
	 * @param linePoint    the model line point.
	 * @param vLineElement the view line element.
	 * @return
	 */
	public VPoint newPoint(LinePoint linePoint, VLineElement vLineElement) {
		VPoint p = pointsMtoV.get(linePoint);
		if (p == null) {
			p = new VPoint(this, linePoint, vLineElement);
			pointsMtoV.put(linePoint, p);
		}
		return p;
	}

	/**
	 * Returns the view representation {@link VPathwayObject} of the given model
	 * element {@link PathwayElement}
	 *
	 * @param e
	 * @return the {@link VPathwayObject} representing the given
	 *         {@link PathwayElement} or <code>null</code> if no view is available
	 */
	public VPathwayObject getPathwayElementView(PathwayObject e) {
		// TODO: store Graphics in a hashmap to improve speed
		for (VElement ve : drawingObjects) {
			if (ve instanceof VPathwayObject) {
				VPathwayObject ge = (VPathwayObject) ve;
				if (ge.getPathwayObject() == e)
					return ge;
			}
		}
		return null;
	}

	/**
	 * Maps the contents of a pathway model to this VPathway.
	 * 
	 * @param pathwayModel the pathway model.
	 */
	public void fromModel(PathwayModel pathwayModel) {
		Logger.log.trace("Create view structure");
		data = pathwayModel;
//		fromModelElement(data.getPathway()); // pathway TODO part of pathway elements?
		for (PathwayElement o : data.getPathwayElements()) {
			fromModelElement(o);
			if (o.getObjectType() == ObjectType.DATANODE) {
				for (State st : ((DataNode) o).getStates()) { // states
					fromModelElement(st);
				}
			}
		}
		// data.fireObjectModifiedEvent(new PathwayEvent(null,
		// PathwayEvent.MODIFIED_GENERAL));
		fireVPathwayEvent(new VPathwayModelEvent(this, VPathwayModelEventType.MODEL_LOADED));
		data.addListener(this);
		undoManager.setPathwayModel(data);
		addScheduled();
		Logger.log.trace("Done creating view structure");
	}

	/**
	 * Maps the contents of a single data object to this VPathway.
	 * 
	 * @param o the model pathway element.
	 */
	private VPathwayObject fromModelElement(PathwayObject o) {
		VPathwayObject result = null;
		switch (o.getObjectType()) {
		case PATHWAY:
			result = fromModelPathway((Pathway) o);
			break;
		case DATANODE:
			result = fromModelDataNode((DataNode) o);
			break;
		case STATE:
			result = fromModelState((State) o);
			break;
		case INTERACTION:
			result = fromModelLineElement((Interaction) o);
			break;
		case GRAPHLINE:
			result = fromModelLineElement((GraphicalLine) o);
			break;
		case LABEL:
			result = fromModelLabel((Label) o);
			break;
		case SHAPE:
			result = fromModelShape((Shape) o);
			break;
		case GROUP:
			result = fromModelGroup((Group) o);
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * Maps the contents of a single {@link DataNode} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelDataNode(DataNode o) {
		return new VDataNode(this, o);
	}

	/**
	 * Maps the contents of a single {@link State} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelState(State o) {
		return new VState(this, o);
	}

	/**
	 * Maps the contents of a single {@link LineElement} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelLineElement(LineElement o) {
		return new VLineElement(this, o);
	}

	/**
	 * Maps the contents of a single {@link Label} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelLabel(Label o) {
		return new VLabel(this, o);
	}

	/**
	 * Maps the contents of a single {@link Shape} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelShape(Shape o) {
		return new VShape(this, o);
	}

	/**
	 * Maps the contents of a single {@link Group} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelGroup(Group o) {
		return new VGroup(this, o);
	}

	/**
	 * Maps the contents of a single {@link Pathway} to this VPathwayModel.
	 * 
	 * @param o the model pathway element.
	 * @return
	 */
	private VPathwayObject fromModelPathway(Pathway o) {
		VInfoBox mi = new VInfoBox(this, o);
		mi.markDirty();
		return mi;
	}

	/**
	 * Used by undo manager.
	 */
	public void replacePathwayModel(PathwayModel originalState) {

		boolean changed = data.hasChanged();

		clearSelection();
		drawingObjects = new ArrayList<VElement>();
		// transfer selectionBox with corresponding listeners
		SelectionBox newSelection = new SelectionBox(this);
		for (Iterator<SelectionListener> i = selection.getListeners().iterator(); i.hasNext();) {
			SelectionListener l = i.next();
			newSelection.addListener(l);
			i.remove();
		}
		selection = newSelection;
		data.removeListener(this);
		pressedObject = null;
		data.transferStatusFlagListeners(originalState);
		data = null;
		pointsMtoV = new HashMap<LinePoint, VPoint>();
		fromModel(originalState);

		if (changed != originalState.hasChanged()) {
			data.fireStatusFlagEvent(new StatusFlagEvent(originalState.hasChanged()));
		}
	}

	// ================================================================================
	// Draw Methods
	// ================================================================================
	/**
	 * Checks if draw is allowed for the given VElement.
	 * 
	 * @param o the VElement.
	 * @return
	 */
	boolean checkDrawAllowed(VElement o) {
		if (isEditMode())
			return true;
		else
			return !(o instanceof Handle || (o == selection && !isDragging));
	}

	/**
	 * Paints all components in the drawing. This method is called automatically in
	 * the painting process. This method will draw opaquely, meaning it will erase
	 * the background.
	 * 
	 * @param g2d the graphics device to draw on. The method will not draw outside
	 *            the clipping area.
	 */
	public void draw(Graphics2D g2d) {
		addScheduled();
		cleanUp();

		try {
			// save original, non-clipped, to pass on to VPathwayEvent
			Graphics2D g2dFull = (Graphics2D) g2d.create();

			// we only redraw the part within the clipping area.
			Rectangle area = g2d.getClipBounds();
			if (area == null) {
				Dimension size = parent == null ? new Dimension(getVWidth(), getVHeight())
						: parent.getViewRect().getSize(); // Draw the visible area
				area = new Rectangle(0, 0, size.width, size.height);
			}

			// erase the background
			g2d.setColor(data.getPathway().getBackgroundColor());
			g2d.fillRect(area.x, area.y, area.width, area.height);

			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			g2d.clip(area);
			g2d.setColor(java.awt.Color.BLACK);
			Collections.sort(drawingObjects);
			cleanUp();
			for (VElement o : drawingObjects) {
				if (o.vIntersects(area)) {
					if (checkDrawAllowed(o)) {
						o.draw((Graphics2D) g2d.create());
						fireVPathwayEvent(new VPathwayModelEvent(this, o, (Graphics2D) g2dFull.create(),
								VPathwayModelEventType.ELEMENT_DRAWN));
					}
				}
			}
		} catch (ConcurrentModificationException ex) {
			// guard against messing up repaint event completely
			Logger.log.error("Concurrent modification", ex);
		}
	}

	/**
	 * This will cause a complete redraw of the pathway to be scheduled. The redraw
	 * will happen as soon as all other swing events are processed.
	 * <p>
	 * Use this only after large changes (e.g. loading a new pathway, applying a new
	 * visualization method) as it is quite slow.
	 */
	public void redraw() {
		if (parent != null)
			parent.redraw();
	}

	/**
	 * Adds object boundaries to the "dirty" area, the area which needs to be
	 * redrawn. The redraw will not happen immediately, but will be scheduled on the
	 * event dispatch thread.
	 */
	void addDirtyRect(Rectangle2D ar) {
		if (parent != null)
			parent.redraw(ar.getBounds());
	}

	// ================================================================================
	// Selection Methods
	// ================================================================================

	/**
	 * Selects the given object.
	 * 
	 * @param o the object.
	 */
	public void selectObject(VElement o) {
		clearSelection();
		selection.addToSelection(o);
	}

	/**
	 * Selects all objects of the pathway.
	 */
	void selectAll() {
		selectObjects(null);
	}

	/**
	 * Deselects all elements on the drawing and resets the selectionbox.
	 */
	public void clearSelection() {
		clearSelection(0, 0);
	}

	/**
	 * Deselects all elements on the drawing and resets the selectionbox to the
	 * given coordinates Equivalent to {@link SelectionBox#reset(double, double))}
	 * 
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
	private void clearSelection(double x, double y) {
		for (VElement e : drawingObjects) {
			e.deselect();
		}
		selection.reset(x, y);
	}

	/**
	 * Select all objects of the given class.
	 *
	 * @param c The class of the objects to be selected. For example: DataNode,
	 *          Line, etc. May be null, in which case everything is selected.
	 */
	void selectObjects(Class<?> c) {
		clearSelection();
		selection.startSelecting();
		for (VElement vpe : getDrawingObjects()) {
			if (c == null || c.isInstance(vpe)) {
				selection.addToSelection(vpe);
			}

		}
		selection.stopSelecting();
	}

	/**
	 * Selects objects of a given Class.
	 * 
	 * @param c the class.
	 */
	public void selectObjectsByObjectType(Class<?> c) {
		clearSelection();
		selection.startSelecting();
		if (c == DataNode.class) {
			for (DataNode pe : getPathwayModel().getDataNodes()) {
				selection.addToSelection(getPathwayElementView(pe));
			}
		} else if (c == Interaction.class) {
			for (Interaction pe : getPathwayModel().getInteractions()) {
				selection.addToSelection(getPathwayElementView(pe));
			}
		} else if (c == GraphicalLine.class) {
			for (GraphicalLine pe : getPathwayModel().getGraphicalLines()) {
				selection.addToSelection(getPathwayElementView(pe));
			}
		} else if (c == Label.class) {
			for (Label pe : getPathwayModel().getLabels()) {
				selection.addToSelection(getPathwayElementView(pe));
			}
		} else if (c == Shape.class) {
			for (Shape pe : getPathwayModel().getShapes()) {
				selection.addToSelection(getPathwayElementView(pe));
			}
		} else if (c == Group.class) {
			for (Group pe : getPathwayModel().getGroups()) {
				selection.addToSelection(getPathwayElementView(pe));
			}
		} else {
			// TODO???? Citations, Annotations... other???
		}
		selection.stopSelecting();
	}

	/**
	 * Get all selected elements (includes non-Graphics, e.g. Handles).
	 * 
	 * @return
	 */
	public Set<VElement> getSelectedPathwayElements() {
		return selection.getSelection();
	}

	/**
	 * Get all elements of the class Graphics that are currently selected. TODO
	 *
	 * @return
	 */
	public List<VDrawable> getSelectedGraphics() {
		List<VDrawable> result = new ArrayList<VDrawable>();
		for (VElement g : drawingObjects) {
			if (g.isSelected() && g instanceof VDrawable && !(g instanceof SelectionBox)) {
				result.add((VDrawable) g);
			}
		}
		return result;
	}

	/**
	 * Get all elements of the class Graphics that are currently selected, excluding
	 * Groups.
	 *
	 * @return
	 */
	public List<VGroupable> getSelectedNonGroupGraphics() {
		List<VGroupable> result = new ArrayList<VGroupable>();
		for (VElement g : drawingObjects) {
			if (g.isSelected() && g instanceof VGroupable && !(g instanceof SelectionBox) && !((g instanceof VGroup))) {
				result.add((VGroupable) g);
			}
		}
		return result;
	}

	/**
	 * Responds to ctrl/command-G. First checks for current status of selection with
	 * respect to grouping. If selection is already grouped (members of the same
	 * parent group), then the highest-level (parent) group is removed along with
	 * all references to the group. If the selection is not a uniform group, then a
	 * new group is created and each member or groups of members is set to reference
	 * the new group.
	 *
	 * @param selection
	 * @return If a group is created, or if the items were added to a new group,
	 *         this group is returned. If a group is removed, this method will
	 *         return <code>null</code>.
	 */
	public VGroup toggleGroup(List<VDrawable> selection) {
		// groupSelection will be set to true if we are going to add / expand a group,
		// false if we're going to remove a group.
		boolean groupSelection = false;
		Set<Group> groupRefList = new HashSet<Group>();
		/**
		 * Check group status of current selection
		 */
		for (VDrawable g : selection) {
			Drawable pe = g.getPathwayObject();
			Group ref = null;
			if (pe instanceof Groupable) {
				ref = ((Groupable) pe).getGroupRef();
			}
			// If not a group
			if (pe.getObjectType() != ObjectType.GROUP) {
				// and not a member of a group, then selection needs to be grouped
				if (ref == null) {
					groupSelection = true;
				}
				// and is a member of a group, recursively get all parent group references.
				else {
					while (ref != null) {
						groupRefList.add(ref);
						ref = ref.getGroupRef();
					}
				}
			}
		}
		// If more than one group is present in selection, then selection needs to be
		// grouped
		if (groupRefList.size() > 1) {
			groupSelection = true;
		}

		// In all cases, any old groups in selection should be dissolved.
		for (Group e : groupRefList) {
			if (e != null)
				data.remove(e);
		}

		// If selection was defined as a single group, then we're done.
		// clear the selection from view
		if (!groupSelection) {
			clearSelection();
		}
		// Otherwise, a new group will be formed, replacing any former groups.
		// No more nested or overlapping groups!
		else {
			// Form new group with all selected elementsselectPathwayObjects
			Group group = new Group(GroupType.GROUP);
			group.setHAlign(HAlignType.CENTER); // textLabel by default top center
			group.setVAlign(VAlignType.TOP);
			data.add(group);
			for (VDrawable g : selection) {
				PathwayElement pe = (PathwayElement) g.getPathwayObject();
				if (pe instanceof Groupable) {
					((Groupable) pe).setGroupRefTo(group);
				}
			}
			// Select new group in view
			VPathwayObject vg = getPathwayElementView(group);
			if (vg != null) {
				clearSelection();
				selectObject(vg);
			}
			return (VGroup) vg;
		}
		return null;
	}

	// ================================================================================
	// Modify Methods
	// ================================================================================
	private VPathwayObject lastAdded = null;

	/**
	 *
	 */
	public void pathwayModified(PathwayModelEvent e) {
		switch (e.getType()) {
		case PathwayModelEvent.DELETED:
			VPathwayObject deleted = getPathwayElementView(e.getAffectedData());
			if (deleted != null) {
				deleted.markDirty();
				removeDrawingObject(deleted, false);
			}
			break;
		case PathwayModelEvent.ADDED:
			lastAdded = fromModelElement(e.getAffectedData());
			if (lastAdded != null) {
				lastAdded.markDirty();
			}
			break;
		case PathwayModelEvent.RESIZED:
			if (parent != null) {
				parent.resized();
			}
			break;
		}
		addScheduled();
		cleanUp();
	}

	// ================================================================================
	// Add Methods
	// ================================================================================
	/** newly placed object, is set to null again when mouse button is released */
	private PathwayElement newObject = null;

	/**
	 * Adds an element to the drawing.
	 *
	 * @param o the element to add.
	 */
	public void addObject(VElement o) {
		toAdd.add(o);
	}

	/**
	 * When adding elements to a pathway, they are not added immediately but placed
	 * in a temporary array. This to prevent concurrent modification of the main
	 * elements array. This method adds the elements that are scheduled to be added.
	 */
	void addScheduled() {
		for (VElement elt : toAdd) {
			if (!drawingObjects.contains(elt)) { // Don't add duplicates!
				drawingObjects.add(elt);
			}
		}
		toAdd.clear();
	}

	/**
	 * Adds a new object to the drawing {@see VPathway#setNewGraphics(int)}
	 *
	 * @param ve the point where the user clicked on the drawing to add a new
	 *           graphics
	 */
	private void newObject(Point ve) {
		undoManager.newAction("New Object");
		double mx = mFromV((double) ve.x);
		double my = mFromV((double) ve.y);

		PathwayElement[] newObjects = newTemplate.addElements(data, mx, my);

		addScheduled();
		if (newObjects != null && newObjects.length > 0) {
			isDragging = true;
			dragUndoState = DRAG_UNDO_NOT_RECORDING;

			if (newObjects.length > 1) {
				clearSelection();
				// Multiple objects: select all and use selectionbox as dragging object
				for (PathwayElement pwe : newObjects) {
					VPathwayObject g = getPathwayElementView(pwe);
					selection.addToSelection(g);
				}
				pressedObject = selection;
			} else {
				// Single object: select object and use drag element specified by template
				selectObject(lastAdded);
				pressedObject = newTemplate.getDragElement(this);
			}

			newObject = newTemplate.getDragElement(this) == null ? null : newObjects[0];
			vPreviousX = ve.x;
			vPreviousY = ve.y;

			fireVPathwayEvent(new VPathwayModelEvent(this, lastAdded, VPathwayModelEventType.ELEMENT_ADDED));
			newTemplate.postInsert(newObjects);
		}
		setNewTemplate(null);
	}

	// ================================================================================
	// Remove Methods
	// ================================================================================
	/**
	 * Removes the GmmlDrawingObjects in the ArrayList from the drawing.
	 * 
	 * NB: Does not remove the model representation!
	 *
	 * @param toRemove The List containing the objects to be removed
	 */
	public void removeDrawingObjects(List<VElement> toRemove) {
		removeDrawingObjects(toRemove, false);
	}

	/**
	 * Removes the GmmlDrawingObjects in the ArrayList from the drawing.
	 *
	 * @param toRemove        The List containing the objects to be removed
	 * @param removeFromModel Whether to remove the model representation or not
	 */
	public void removeDrawingObjects(List<VElement> toRemove, boolean removeFromModel) {
		// first remove view citations (to workaround a bug)
		for (VElement o : toRemove) {
			if (o.getClass() == VCitation.class) {
				removeDrawingObject(o, removeFromModel);
			}
		}
		// then remove other view elements
		for (VElement o : toRemove) {
			if (o.getClass() != VCitation.class) {
				removeDrawingObject(o, removeFromModel);
			}
		}
		selection.fitToSelection();
		cleanUp();
	}

	/**
	 * @param toRemove
	 * @param removeFromModel
	 */
	public void removeDrawingObject(VElement toRemove, boolean removeFromModel) {
		if (toRemove != null) {
			selection.removeFromSelection(toRemove); // Remove from selection
			toRemove.destroy(); // Object will remove itself from the drawing
			if (removeFromModel) {
				if (toRemove instanceof VPathwayElement) {
					// Remove the model object
					data.remove(((VPathwayElement) toRemove).getPathwayObject());
				}
			}
			cleanUp();
		}
	}

	// ================================================================================
	// Zoom Methods
	// ================================================================================
	private double zoomFactor = 1.0;

	/**
	 * Get the current zoomfactor used. 1 means 100%, 1 gpml unit = 1 pixel 2 means
	 * 200%, 0.5 gpml unit = 1 pixel
	 *
	 * To distinguish between model coordinates and view coordinates, we prefix all
	 * coordinates with either v or m (or V or M). For example:
	 *
	 * mTop = gdata.getMTop(); vTop = GeneProduct.getVTop();
	 *
	 * Calculations done on M's and V's should always match. The only way to convert
	 * is to use the functions mFromV and vFromM.
	 *
	 * Correct: mRight = mLeft + mWidth; Wrong: mLeft += vDx; Fixed: mLeft +=
	 * mFromV(vDx);
	 *
	 * @return the current zoomfactor
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}

	/**
	 * same as getZoomFactor, but in %
	 *
	 * @return
	 */
	public double getPctZoom() {
		return zoomFactor * 100;
	}

	/**
	 * Sets the drawings zoom in percent
	 *
	 * @param pctZoomFactor zoomfactor in percent
	 * @see #getFitZoomFactor() for fitting the pathway inside the viewport.
	 */
	public void setPctZoom(double pctZoomFactor) {
		zoomFactor = pctZoomFactor / 100.0;
		for (VElement vpe : drawingObjects) {
			vpe.zoomChanged();
		}
		if (parent != null)
			parent.resized();
	}

	public void centeredZoom(double pctZoomFactor) {
		if (parent != null) {
			Rectangle vr = parent.getViewRect();

			double mx = mFromV(vr.getCenterX());
			double my = mFromV(vr.getCenterY());
			Logger.log.info("center: " + mx + ", " + my);
			setPctZoom(pctZoomFactor);
			parent.scrollCenterTo((int) vFromM(mx), (int) vFromM(my));
		} else
			setPctZoom(pctZoomFactor);
	}

	public void zoomToCursor(double pctZoomFactor, Point cursor) {
		if (parent == null)
			return;

		// offset between mouse and center of the viewport
		double vDeltax = cursor.getX() - parent.getViewRect().getCenterX();
		double vDeltay = cursor.getY() - parent.getViewRect().getCenterY();
		;

		// model coordinates where the mouse is pointing at
		double mx = mFromV(cursor.getX());
		double my = mFromV(cursor.getY());

		// adjust zoom
		setPctZoom(pctZoomFactor);

		// put mx, my back under the mouse
		parent.scrollCenterTo((int) (vFromM(mx) - vDeltax), (int) (vFromM(my) - vDeltay));
	}

	/**
	 * Calculate the zoom factor that would make the pathway fit in the viewport.
	 */
	public double getFitZoomFactor() {
		double result;
		Dimension drawingSize = new Dimension(getVWidth(), getVHeight());
		Dimension viewportSize = getWrapper().getViewRect().getSize();
		result = (int) Math.min(getPctZoom() * (double) viewportSize.width / drawingSize.width,
				getPctZoom() * (double) viewportSize.height / drawingSize.height);
		return result;
	}

	// ================================================================================
	// Link Methods
	// ================================================================================
	private LinkAnchor currentLinkAnchor;

	/**
	 * Links a given point to an {@link VLinkableTo} object.
	 * 
	 * @param p2d the point where mouse is at
	 * @param g   the handle
	 */
	private void linkPointToObject(Point2D p2d, Handle g) {
		if (dragUndoState == DRAG_UNDO_CHANGE_START) {
			dragUndoState = DRAG_UNDO_CHANGED;
		}
		hideLinkAnchors();
		VPoint vPoint = (VPoint) g.getAdjustable();
		VLineElement vLine = vPoint.getLine();
		LineElement line = vLine.getPathwayObject();
		// get linkproviders for given location
		List<LinkProvider> linkProviders = getLinkProvidersAt(p2d);
		/*
		 * Fix for preventing grouped line to link to its own group: Remove the group
		 * from the list of linkproviders. Also remove the line anchors to prevent
		 * linking a line to it's own anchors.
		 */
		if (g.getAdjustable() instanceof VPoint) {
			Group group = line.getGroupRef();
			if (group != null) {
				linkProviders.remove((LinkProvider) getPathwayElementView(group));
			}
			for (VAnchor va : vLine.getVAnchors()) {
				linkProviders.remove(va);
			}
		}
		LinkableTo linkableTo = null;
		for (LinkProvider linkProvider : linkProviders) {
			// do nothing if linkprovider is an anchor with disallowlinks true
			if (linkProvider instanceof VAnchor
					&& ((VAnchor) linkProvider).getAnchor().getShapeType().isDisallowLinks()) {
				break;
			}
			linkProvider.showLinkAnchors();
			LinkAnchor linkAnchor = linkProvider.getLinkAnchorAt(p2d);
			// if link anchor valid
			if (linkAnchor != null) {
				// link point to the linkAnchor of linkableTo
				linkAnchor.link(vPoint.getLinePoint());
				linkableTo = linkAnchor.getLinkableTo();
				if (currentLinkAnchor != null) {
					currentLinkAnchor.unhighlight();
				}
				linkAnchor.highlight();
				currentLinkAnchor = linkAnchor;
				break;
			}
		}
		// no link anchor found, nothing to link to, unlink point if applicable
		if (linkableTo == null && vPoint.getLinePoint().getElementRef() != null) {
			vPoint.getLinePoint().unlink();
			if (currentLinkAnchor != null) {
				currentLinkAnchor.unhighlight();
			}
		}
	}

	/**
	 * Returns link providers at a particular location on the drawing.
	 * 
	 * @param p2d the point2d of a particular location.
	 * @return the list of link providers at the particular location.
	 */
	private List<LinkProvider> getLinkProvidersAt(Point2D p2d) {
		List<LinkProvider> result = new ArrayList<LinkProvider>();
		for (VElement o : drawingObjects) {
			if (o instanceof LinkProvider && o.getVBounds().contains(p2d)) {
				result.add((LinkProvider) o);
			}
		}
		return result;
	}

	/**
	 * 
	 */
	private void hideLinkAnchors() {
		for (VElement pe : getDrawingObjects()) {
			if (pe instanceof LinkProvider) {
				((LinkProvider) pe).hideLinkAnchors();
			}
		}
	}

	// ================================================================================
	// Snap Modifier Methods
	// ================================================================================
	private boolean snapModifierPressed;

	/**
	 * Checks whether the key is pressed to restrict handle movement. When the key
	 * is down:
	 * <li>lines snap to certain angles (but only when preference is on).
	 * <li>rotation handles on shapes snap to certain angles
	 * <li>shape snaps to a fixed aspect ratio
	 * <p>
	 * 
	 * @see GlobalPreference#SNAP_TO_ANGLE for the global setting
	 * @see GlobalPreference#SNAP_TO_ANGLE_STEP for the angle step to be used
	 * @return
	 */
	public boolean isSnapModifierPressed() {
		return snapModifierPressed;
	}

	// ================================================================================
	// Mouse Event Methods
	// ================================================================================
	private int vPreviousX;
	private int vPreviousY;
	private boolean isDragging;

	/**
	 * Handles mouse movement.
	 * 
	 * @param e the mouse event.
	 */
	public void mouseMove(MouseEvent e) {
		snapModifierPressed = e.isKeyDown(MouseEvent.M_SHIFT);
		// If dragging, drag the pressed object.
		// And only when the right button isn't clicked
		if (pressedObject != null && isDragging && !e.isKeyDown(java.awt.event.MouseEvent.BUTTON3_DOWN_MASK)) {
			if (dragUndoState == DRAG_UNDO_CHANGE_START) {
				dragUndoState = DRAG_UNDO_CHANGED;
			}
			double vdx = e.getX() - vPreviousX;
			double vdy = e.getY() - vPreviousY;
			if (pressedObject instanceof Handle) {
				((Handle) (pressedObject)).vMoveTo(e.getX(), e.getY());
			} else {
				pressedObject.vMoveBy(vdx, vdy);
			}

			vPreviousX = e.getX();
			vPreviousY = e.getY();

			if (pressedObject instanceof Handle && newTemplate == null
					&& ((Handle) pressedObject).getAdjustable() instanceof VPoint) {
				linkPointToObject(new Point2D.Double(e.getX(), e.getY()), (Handle) pressedObject);
			}
		} else {
			List<VElement> objects = getObjectsAt(new Point2D.Double(e.getX(), e.getY()));

			// Process mouse-exit events
			processMouseExitEvents(e, objects);

			// Process mouse-enter events
			processMouseEnterEvents(e, objects);
		}

		hoverManager.reset(e);
	}

	/**
	 * @param e       the mouse event.
	 * @param objects
	 */
	private void processMouseEnterEvents(MouseEvent e, List<VElement> objects) {
		for (VElement vpe : objects) {
			if (!lastMouseOver.contains(vpe)) {
				lastMouseOver.add(vpe);
				stateEntered = true;
				if (vpe instanceof VLabel) {
					String href = ((VLabel) vpe).getPathwayObject().getHref();
					if (href != null && !Utils.stringEquals(href, "")) {
						lastEnteredElement = vpe;
					}
				} else {
					fireVElementMouseEvent(new VElementMouseEvent(this, VElementMouseEvent.TYPE_MOUSEENTER, vpe, e));
				}
			}
		}
		if (lastEnteredElement != null) {
			fireHyperlinkUpdate(lastEnteredElement);
		}
	}

	/**
	 * 
	 * @param e       the mouse event.
	 * @param objects
	 */
	private void processMouseExitEvents(MouseEvent e, List<VElement> objects) {
		Set<VElement> toRemove = new HashSet<VElement>();

		for (VElement vpe : lastMouseOver) {
			if (!objects.contains(vpe)) {
				toRemove.add(vpe);
				stateEntered = false;
				if (lastEnteredElement == vpe) {
					fireHyperlinkUpdate(lastEnteredElement);
					lastEnteredElement = null;
				} else {
					fireVElementMouseEvent(new VElementMouseEvent(this, VElementMouseEvent.TYPE_MOUSEEXIT, vpe, e));
				}

			}
		}

		lastMouseOver.removeAll(toRemove);
	}

	// ================================================================================
	// Mouse Event Methods: HOVER
	// ================================================================================
	private Set<VElement> lastMouseOver = new HashSet<VElement>();
	private HoverManager hoverManager = new HoverManager();

	/**
	 * This class is for managing hover. TODO
	 * 
	 * @author unknown
	 */
	private class HoverManager implements ActionListener {
		static final int DELAY = 1000; // tooltip delay in ms
		boolean tooltipDisplayed = false;

		MouseEvent lastEvent = null;

		Timer timer;

		public HoverManager() {
			timer = new Timer(DELAY, this);
		}

		public void actionPerformed(ActionEvent e) {
			if (!tooltipDisplayed) {
				fireVPathwayEvent(new VPathwayModelEvent(VPathwayModel.this, getObjectsAt(lastEvent.getLocation()),
						lastEvent, VPathwayModelEventType.ELEMENT_HOVER));
				tooltipDisplayed = true;
			}
		}

		void reset(MouseEvent e) {
			lastEvent = e;
			tooltipDisplayed = false;
			timer.restart();
		}

		void stop() {
			timer.stop();
		}
	}

	/**
	 * Opens href of a Label with ctrl + click.
	 * 
	 * @param e
	 * @param o
	 * @return
	 */
	private boolean openHref(MouseEvent e, VElement o) {
		if (e.isKeyDown(128) && o != null && o instanceof VLabel) {
			String href = ((VLabel) o).getPathwayObject().getHref();
			if (selection.getSelection().size() < 1 && !href.equals("")) {
				fireVPathwayEvent(new VPathwayModelEvent(this, o, VPathwayModelEventType.HREF_ACTIVATED));
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles mouse pressed input.
	 * 
	 * @param e the mouse event.
	 */
	public void mouseDown(MouseEvent e) {
		VElement vpe = getObjectAt(e.getLocation());
		if (!openHref(e, vpe)) {
			// setFocus();
			vDragStart = new Point(e.getX(), e.getY());
			temporaryCopy = (PathwayModel) data.clone();
			if (editMode) {
				if (newTemplate != null) {
					newObject(e.getLocation());
					// SwtGui.getCurrent().getWindow().deselectNewItemActions();
				} else {
					pressedObject = vpe;
					editObject(e);
				}
			} else {
				mouseDownViewMode(e);
			}
			if (pressedObject != null) {
				fireVPathwayEvent(
						new VPathwayModelEvent(this, pressedObject, e, VPathwayModelEventType.ELEMENT_CLICKED_DOWN));
			}
		}
	}

	/**
	 * Handles mouse released input.
	 * 
	 * @param e the mouse event.
	 */
	public void mouseUp(MouseEvent e) {
		if (isDragging) {
			if (dragUndoState == DRAG_UNDO_CHANGED) {
				assert (temporaryCopy != null);
				// further specify the type of undo event,
				// depending on the type of object being dragged
				String message = "Drag Object";
				if (pressedObject instanceof Handle) {
					if (((Handle) pressedObject).getFreedom() == Handle.Freedom.ROTATION) {
						message = "Rotate Object";
					} else {
						message = "Resize Object";
					}
				}
				undoManager.newAction(new UndoAction(message, temporaryCopy));
				temporaryCopy = null;
			}
			resetHighlight();
			hideLinkAnchors();
			if (selection.isSelecting()) { // If we were selecting, stop it
				selection.stopSelecting();
			}
			// check if we placed a new object by clicking or dragging
			// if it was a click, give object the initial size.
			else if (newObject != null && Math.abs(vDragStart.x - e.getX()) <= MIN_DRAG_LENGTH
					&& Math.abs(vDragStart.y - e.getY()) <= MIN_DRAG_LENGTH) {
				DefaultTemplates.setInitialSize(newObject);
			}
			newObject = null;
			setNewTemplate(null);
		}
		isDragging = false;
		dragUndoState = DRAG_UNDO_NOT_RECORDING;
		if (pressedObject != null) {
			fireVPathwayEvent(
					new VPathwayModelEvent(this, pressedObject, e, VPathwayModelEventType.ELEMENT_CLICKED_UP));
		}
		handleLinkAliasRef();// TODO
	}

	/**
	 * Handles mouse entered input.
	 * 
	 * @param e the mouse event.
	 */
	public void mouseDoubleClick(MouseEvent e) {
		VElement o = getObjectAt(e.getLocation());
		if (o != null) {
			Logger.log.trace("Fire double click event to " + listeners.size());
			for (VPathwayModelListener l : listeners) {
				Logger.log.trace("\t " + l.hashCode() + ", " + l);
			}
			fireVPathwayEvent(new VPathwayModelEvent(this, o, VPathwayModelEventType.ELEMENT_DOUBLE_CLICKED));
		}
	}

	static final int MULTI_SELECT_MASK = MouseEvent.M_SHIFT
			| (Utils.getOS() == Utils.OS_MAC ? MouseEvent.M_META : MouseEvent.M_CTRL);

	/**
	 * Handles event when on mouseDown in case the drawing is in view mode (does
	 * nothing yet)
	 *
	 * @param e the mouse event to handle.
	 */
	private void mouseDownViewMode(MouseEvent e) {
		Point2D p2d = new Point2D.Double(e.getX(), e.getY());

		pressedObject = getObjectAt(p2d);

		if (pressedObject != null) {
			// Shift or Ctrl or Meta pressed, add/remove from selection
			boolean modifierPressed = e.isKeyDown(MULTI_SELECT_MASK);
			doClickSelect(p2d, modifierPressed);
		} else
			startSelecting(p2d);
	}

	/**
	 * Initializes selection, resetting the selectionbox and then setting it to the
	 * position specified.
	 *
	 * @param vp the point to start with the selection.
	 */
	void startSelecting(Point2D vp) {
		if (!selectionEnabled)
			return;

		vPreviousX = (int) vp.getX();
		vPreviousY = (int) vp.getY();
		isDragging = true;
		dragUndoState = DRAG_UNDO_NOT_RECORDING;

		clearSelection(vp.getX(), vp.getY());
		selection.startSelecting();
		pressedObject = selection.getCornerHandle();
	}

	/**
	 * Resets highlighting, unhighlights all GmmlDrawingObjects.
	 */
	public void resetHighlight() {
		for (VElement o : drawingObjects)
			o.unhighlight();
		redraw();
	}

	/**
	 * Called by MouseDown, when we're in editing mode and we're not adding new
	 * objects prepares for dragging the object.
	 * 
	 * @param e the mouse event.
	 */
	private void editObject(MouseEvent e) {
		// if we clicked on an object
		if (pressedObject != null) {
			// Shift pressed, add/remove from selection
			boolean modifierPressed = e.isKeyDown(MULTI_SELECT_MASK);
			// if our object is an handle, select also it's parent.
			if (pressedObject instanceof Handle) {
				VElement parent = ((Handle) pressedObject).getParent();
				parent.select();
				// Special treatment for anchor
				if (parent instanceof VAnchor) {
					doClickSelect(e.getLocation(), modifierPressed);
				}
			} else {
				doClickSelect(e.getLocation(), modifierPressed);
			}

			// start dragging
			vPreviousX = e.getX();
			vPreviousY = e.getY();

			isDragging = true;
			dragUndoState = DRAG_UNDO_CHANGE_START;
		} else {
			// start dragging selectionbox
			startSelecting(e.getLocation());
		}
	}

	/**
	 * Finds the object at a particular location on the drawing
	 *
	 * NB: If you want to get more than one, use {@link #getObjectsAt(Point2D)}
	 * 
	 * @param p2d the point2d of a particular location.
	 * @return the object at the particular location.
	 */
	public VElement getObjectAt(Point2D p2d) {
		int zmax = Integer.MIN_VALUE;
		VElement probj = null;
		for (VElement o : drawingObjects) {
			// first we use vContains, which is good for detecting (non-transparent) shapes
			if (o.vContains(p2d) && o.getZOrder() > zmax) {
				probj = o;
				zmax = o.getZOrder();
			}
		}
		if (probj == null) {
			// if there is nothing at that point, we use vIntersects with a fuzz area,
			// which is good for detecting lines and transparent shapes.
			Rectangle2D fuzz = new Rectangle2D.Double(p2d.getX() - FUZZY_SIZE, p2d.getY() - FUZZY_SIZE, FUZZY_SIZE * 2,
					FUZZY_SIZE * 2);
			for (VElement o : drawingObjects) {
				if (o.vIntersects(fuzz) && o.getZOrder() > zmax) {
					probj = o;
					zmax = o.getZOrder();
				}
			}
		}
		return probj;
	}

	/**
	 * Find all objects at a particular location on the drawing.
	 *
	 * NB: If you only need the top object, use {@link #getObjectAt(Point2D)}
	 * 
	 * @param p2d the point2d of a particular location.
	 * @return the list of all objects at the particular location.
	 */
	public List<VElement> getObjectsAt(Point2D p2d) {
		List<VElement> result = new ArrayList<VElement>();
		for (VElement o : drawingObjects) {
			if (o.vContains(p2d)) {
				result.add(o);
			}
		}
		return result;
	}

	/**
	 * If modifierPressed is true, the selected object will be added to the
	 * selection, rather than creating a new selection with just one object. if
	 * modifierPressed is true when selecting a Group object, then a new selection
	 * is made of the children, allowing selection into groups.
	 *
	 * modifierPressed should be true when either SHIFT or CTRL/COMMAND is pressed.
	 * 
	 * @param p2d
	 * @param modifierPressed
	 */
	void doClickSelect(Point2D p2d, boolean modifierPressed) {
		if (!selectionEnabled)
			return;

		if (modifierPressed) {
			if (pressedObject instanceof SelectionBox) { // Object inside selectionbox clicked:
															// pass to selectionbox
				selection.objectClicked(p2d);
			} else if (pressedObject.isSelected()) { // Already in selection:
														// remove
				selection.removeFromSelection(pressedObject);
			} else { // Not in selection:
						// add
				selection.addToSelection(pressedObject);
			}
			pressedObject = selection; // Set dragging to selectionbox
		} else
		// Shift or Ctrl not pressed
		{
			// If pressed object is not selectionbox:
			// Clear current selection and select pressed object
			if (!(pressedObject instanceof SelectionBox)) {
				clearSelection();
				// If the object is a handle, select the parent instead
				if (pressedObject instanceof Handle) {
					VElement parent = ((Handle) pressedObject).getParent();
					selection.addToSelection((VElement) parent);
				} else {
					selection.addToSelection(pressedObject);
				}
			} else { // Check if clicked object inside selectionbox
				if (selection.getChild(p2d) == null)
					clearSelection();
			}
		}
		redraw();
	}

	/**
	 * Pathvisio distinguishes between placing objects with a click or with a drag.
	 * If you don't move the cursor in between the mousedown and mouseup event, the
	 * object is placed with a default initial size.
	 *
	 * vDragStart is used to determine the mousemovement during the click.
	 */
	private Point vDragStart;

	/**
	 * dragUndoState determines what should be done when you release the mouse
	 * button after dragging an object.
	 *
	 * if it is DRAG_UNDO_NOT_RECORDING, it's not necessary to record an event. This
	 * is the case when we were dragging a selection rectangle, or a new object (in
	 * which case the change event was already recorded)
	 *
	 * in other cases, it is set to DRAG_UNDO_CHANGE_START at the start of the drag.
	 * If additional move events occur, the state is changed to DRAG_UNDO_CHANGED.
	 * The latter will lead to recording of the undo event.
	 */
	private static final int DRAG_UNDO_NOT_RECORDING = 0;
	private static final int DRAG_UNDO_CHANGE_START = 1;
	private static final int DRAG_UNDO_CHANGED = 2;

	private int dragUndoState = DRAG_UNDO_NOT_RECORDING;

	/** minimum drag length for it to be considered a drag and not a click */
	private static final int MIN_DRAG_LENGTH = 3;

	public void mouseEnter(MouseEvent e) {
		hoverManager.reset(e);
	}

	public void mouseExit(MouseEvent e) {
		hoverManager.stop();
	}

	/**
	 * @param vpe
	 */
	private void fireHyperlinkUpdate(VElement vpe) {
		int type;
		if (stateEntered && stateCtrl) {
			type = VElementMouseEvent.TYPE_MOUSE_SHOWHAND;
		} else {
			type = VElementMouseEvent.TYPE_MOUSE_NOTSHOWHAND;
		}
		fireVElementMouseEvent(new VElementMouseEvent(this, type, vpe));
	}

	// ================================================================================
	// KeyEvent and KeyStroke Methods
	// ================================================================================
	// TODO: should use Toolkit.getMenuShortcutKeyMask(), but
	// that doesn't work in headless mode so screws up automated testing.
	// solution: define keyboard shortcuts elsewhere
	public static final KeyStroke KEY_SELECT_DATA_NODES = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D,
			java.awt.Event.CTRL_MASK);

	public static final KeyStroke KEY_SELECT_INTERACTIONS = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E,
			java.awt.Event.CTRL_MASK);

	public static final KeyStroke KEY_BOLD = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
			java.awt.Event.CTRL_MASK);

	public static final KeyStroke KEY_ITALIC = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I,
			java.awt.Event.CTRL_MASK);

	public static final KeyStroke KEY_MOVERIGHT = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0);

	public static final KeyStroke KEY_MOVELEFT = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0);

	public static final KeyStroke KEY_MOVEUP = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0);

	public static final KeyStroke KEY_MOVEDOWN = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0);

	public static final KeyStroke KEY_MOVERIGHT_SHIFT = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,
			java.awt.Event.SHIFT_MASK);

	public static final KeyStroke KEY_MOVELEFT_SHIFT = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,
			java.awt.Event.SHIFT_MASK);

	public static final KeyStroke KEY_MOVEUP_SHIFT = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP,
			java.awt.Event.SHIFT_MASK);

	public static final KeyStroke KEY_MOVEDOWN_SHIFT = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN,
			java.awt.Event.SHIFT_MASK);

	/**
	 * Several {@link Action}s related to the view
	 */
	private ViewActions viewActions;

	/**
	 * Get the view actions, a class where several actions related to the view are
	 * stored (delete, select) and where other actions can be registered to a group
	 * (e.g. a group that will be disabled when edit-mode is turned off)
	 *
	 * @return an instance of the {@link ViewActions} class
	 */
	public ViewActions getViewActions() {
		return viewActions;
	}

	/**
	 * Convenience method to register an action that has an accelerator key.
	 * 
	 * @param a
	 */
	private void registerKeyboardAction(Action a) {
		if (parent == null)
			return;
		KeyStroke key = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
		if (key == null)
			throw new RuntimeException("Action " + a + " must have value ACCELERATOR_KEY set");
		parent.registerKeyboardAction(key, a);
	}

	public void registerKeyboardActions(Engine engine) {
		viewActions = new ViewActions(engine, this);

		if (parent != null) {
			registerKeyboardAction(viewActions.copy);
			registerKeyboardAction(viewActions.paste);
			parent.registerKeyboardAction(KEY_SELECT_DATA_NODES, viewActions.selectDataNodes);
			parent.registerKeyboardAction(KEY_SELECT_INTERACTIONS, viewActions.selectInteractions);
			registerKeyboardAction(viewActions.selectAll);
			registerKeyboardAction(viewActions.delete1);
			registerKeyboardAction(viewActions.delete2);
			registerKeyboardAction(viewActions.undo);
			registerKeyboardAction(viewActions.addAnchor);
			registerKeyboardAction(viewActions.orderBringToFront);
			registerKeyboardAction(viewActions.orderSendToBack);
			registerKeyboardAction(viewActions.orderUp);
			registerKeyboardAction(viewActions.orderDown);
			registerKeyboardAction(viewActions.showUnlinked);
			parent.registerKeyboardAction(KEY_MOVERIGHT, new KeyMoveAction(engine, KEY_MOVERIGHT));
			parent.registerKeyboardAction(KEY_MOVERIGHT_SHIFT, new KeyMoveAction(engine, KEY_MOVERIGHT_SHIFT));
			parent.registerKeyboardAction(KEY_MOVELEFT, new KeyMoveAction(engine, KEY_MOVELEFT));
			parent.registerKeyboardAction(KEY_MOVELEFT_SHIFT, new KeyMoveAction(engine, KEY_MOVELEFT_SHIFT));
			parent.registerKeyboardAction(KEY_MOVEUP, new KeyMoveAction(engine, KEY_MOVEUP));
			parent.registerKeyboardAction(KEY_MOVEUP_SHIFT, new KeyMoveAction(engine, KEY_MOVEUP_SHIFT));
			parent.registerKeyboardAction(KEY_MOVEDOWN, new KeyMoveAction(engine, KEY_MOVEDOWN));
			parent.registerKeyboardAction(KEY_MOVEDOWN_SHIFT, new KeyMoveAction(engine, KEY_MOVEDOWN_SHIFT));
			parent.registerKeyboardAction(KEY_BOLD, new TextFormattingAction(engine, KEY_BOLD));
			parent.registerKeyboardAction(KEY_ITALIC, new TextFormattingAction(engine, KEY_ITALIC));
		}
	}

	public void keyPressed(KeyEvent e) {
		// Use registerKeyboardActions
		if (KeyEvent.CTRL == e.getKeyCode()) {
			stateCtrl = true;
			if (lastEnteredElement != null) {
				fireHyperlinkUpdate(lastEnteredElement);
			}
		}
	}

	/**
	 * @param e the key event.
	 */
	public void keyReleased(KeyEvent e) {
		// use registerKeyboardActions
		if (KeyEvent.CTRL == e.getKeyCode()) {
			stateCtrl = false;
			if (lastEnteredElement != null) {
				fireHyperlinkUpdate(lastEnteredElement);
			}
		}
	}

	/**
	 * Handles movement of objects with the arrow keys.
	 *
	 * @param ks  the key stroke.
	 * @param int the increment.
	 */
	public void moveByKey(KeyStroke ks, int increment) {
		List<VGroupable> selectedGraphics = getSelectedNonGroupGraphics();

		if (selectedGraphics.size() > 0) {

			switch (ks.getKeyCode()) {
			case 37:
				undoManager.newAction("Move object");
				selection.vMoveBy(-increment, 0);
				break;
			case 39:
				undoManager.newAction("Move object");
				selection.vMoveBy(increment, 0);
				break;
			case 38:
				undoManager.newAction("Move object");
				selection.vMoveBy(0, -increment);
				break;
			case 40:
				undoManager.newAction("Move object");
				selection.vMoveBy(0, increment);
			}
		}
	}

	// ================================================================================
	// Move Methods
	// ================================================================================
	/**
	 * Move multiple elements together (either a group or a selection).
	 * <p>
	 * This method makes sure that elements are not moved twice if they are part of
	 * another element that is being moved. For example: If a State is moved at the
	 * same time as its parent DataNode, then the state is not moved. If a group
	 * member is moved together with the parent group, then the member is not moved.
	 * 
	 * @param toMove
	 * @param vdx
	 * @param vdy
	 */
	public void moveMultipleElements(Collection<? extends VElement> toMove, double vdx, double vdy) {
		// collect all elementIds in selection
		Set<PathwayObject> elts = new HashSet<PathwayObject>();
		for (VElement o : toMove) {
			if (o instanceof VPathwayObject) {
				PathwayObject elt = ((VPathwayObject) o).getPathwayObject();
				if (elt != null) {
					elts.add(elt);
				}
			}
		}
		for (VElement o : toMove) {
			// skip if parent of state is also in selection.
			if (o instanceof VState) {
				if (elts.contains(((VState) o).getPathwayObject().getDataNode()))
					continue;
			}
			if (o instanceof VPathwayElement) {
				if (o instanceof VGroupable) {
					// skip if parent group is also in selection
					if (elts.contains(((VGroupable) o).getPathwayObject().getGroupRef())) {
						continue;
					}
				}
				o.vMoveBy(vdx, vdy);
			}
		}
	}

	// ================================================================================
	// LinkAliasRef Methods
	// ================================================================================
	private boolean doLinkAliasRef = false; // indicates if alias is to be linked to aliasRef
	private DataNode alias = null; // the current alias to be linked

	/**
	 * Initiates process for alias data node to be linked to an aliasRef group.
	 * 
	 * @param dn the alias datanode to be linked.
	 */
	public void startLinkAliasRef(DataNode dn) {
		if (dn.getType() == DataNodeType.ALIAS) {// precautionary check if type Alias
			alias = dn;
			doLinkAliasRef = true;
		}
	}

	/**
	 * Stops the aliasRef linking process. Called when linking task is cancelled or
	 * when alias datanode is successfully linked.
	 */
	public void stopLinkAliasRef() {
		alias = null;
		doLinkAliasRef = false;
	}

	/**
	 * Called when user fails to click on a valid VGroup/Group to link Alias to.
	 * 
	 * <p>
	 * NB:
	 * <ol>
	 * <li>Message dialog again instructs user to click on a group for linking.
	 * <li>If cancel is selected, the linking task is stopped.
	 * </ol>
	 */
	public void failLinkAliasRef() {
		Object[] options = { "Try Again", "Cancel" };
		int n = JOptionPane.showOptionDialog(null, "Please click on a Group to Link Alias DataNode.", "Message",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
		if (n == 1) { // cancel linking aliasRef
			JOptionPane.showConfirmDialog(null, "Alias DataNode was Not Linked to a Group.", "Warning",
					JOptionPane.PLAIN_MESSAGE);
			stopLinkAliasRef();
		}
	}

	/**
	 * Links alias data node to aliasRef group if new selection is a VGroup,
	 * otherwise prompts user to try again or cancel {@link #failLinkAliasRef()}.
	 */
	public void handleLinkAliasRef() {
		if (doLinkAliasRef) {
			if (selection.getSelection().size() != 1) {
				failLinkAliasRef();
			} else if (selection.getSelection().iterator().next() instanceof VGroup) {
				alias.setAliasRef(((VGroup) selection.getSelection().iterator().next()).getPathwayObject());
				JOptionPane.showConfirmDialog(null, "Alias DataNode successfully Linked to Group.", "Message",
						JOptionPane.PLAIN_MESSAGE);
				stopLinkAliasRef();
			} else {
				failLinkAliasRef();
			}
		}
	}

	// ================================================================================
	// Alignment Methods
	// ================================================================================
	/**
	 * Handles aligning layoutTypes ALIGN_*
	 * 
	 * @param alignType
	 * @param gs
	 */
	private void alignGraphics(LayoutType alignType, List<VGroupable> gs) {
		// first sort either horizontally or vertically
		switch (alignType) {
		case ALIGN_CENTERY:
		case ALIGN_TOP:
			Collections.sort(gs, new YComparator());
			break;
		case ALIGN_LEFT:
		case ALIGN_CENTERX:
			Collections.sort(gs, new XComparator());
			break;
		case ALIGN_BOTTOM:
			Collections.sort(gs, new YComparator());
			Collections.reverse(gs);
			break;
		case ALIGN_RIGHT:
			Collections.sort(gs, new XComparator());
			Collections.reverse(gs);
			break;
		default:
			throw new IllegalArgumentException("This method only handles ALIGN_* layoutTypes");
		}

		// The bounds of the model to view
		// translated shape
		Rectangle2D vBoundsFirst = ((VPathwayObject) gs.get(0)).getVShape(true).getBounds2D();

		for (int i = 1; i < gs.size(); i++) {
			VPathwayObject g = (VPathwayObject) gs.get(i);
			Rectangle2D vBounds = ((VPathwayObject) g).getVShape(true).getBounds2D();

			switch (alignType) {
			case ALIGN_CENTERX:
				g.vMoveBy(vBoundsFirst.getCenterX() - vBounds.getCenterX(), 0);
				break;
			case ALIGN_CENTERY:
				g.vMoveBy(0, vBoundsFirst.getCenterY() - vBounds.getCenterY());
				break;
			case ALIGN_LEFT:
				g.vMoveBy(vBoundsFirst.getX() - vBounds.getX(), 0);
				break;
			case ALIGN_RIGHT:
				g.vMoveBy(vBoundsFirst.getMaxX() - vBounds.getMaxX(), 0);
				break;
			case ALIGN_TOP:
				g.vMoveBy(0, vBoundsFirst.getY() - vBounds.getY());
				break;
			case ALIGN_BOTTOM:
				g.vMoveBy(0, vBoundsFirst.getMaxY() - vBounds.getMaxY());
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Align, stack or scale selected objects based on user-selected layout type
	 * 
	 * @param layoutType the layout type.
	 */
	public void layoutSelected(LayoutType layoutType) {
		List<VGroupable> selectedGraphics = getSelectedNonGroupGraphics();

		if (selectedGraphics.size() > 0) {
			undoManager.newAction(layoutType.getDescription());
			switch (layoutType) {
			case COMMON_WIDTH:
				scaleWidth(selectedGraphics);
				break;
			case COMMON_HEIGHT:
				scaleHeight(selectedGraphics);
				break;
			case ALIGN_CENTERX:
			case ALIGN_CENTERY:
			case ALIGN_TOP:
			case ALIGN_LEFT:
			case ALIGN_RIGHT:
			case ALIGN_BOTTOM:
				alignGraphics(layoutType, selectedGraphics);
				break;
			case STACK_BOTTOM:
			case STACK_TOP:
			case STACK_LEFT:
			case STACK_RIGHT:
			case STACK_CENTERX:
			case STACK_CENTERY:
				stackGraphics(layoutType, selectedGraphics);
				break;
			}

			selection.fitToSelection();
			redraw();
		}
	}

	/**
	 * Stacks a set of objects based on user-selected stack type
	 * 
	 * @param stackType the layout type.
	 * @param gs        the list of elements.
	 */
	private void stackGraphics(LayoutType stackType, List<VGroupable> gs) {
		// first we sort the selected graphics, either horizontally or vertically
		switch (stackType) {
		case STACK_CENTERX:
		case STACK_LEFT:
		case STACK_RIGHT:
			Collections.sort(gs, new YComparator());
			break;
		case STACK_CENTERY:
		case STACK_TOP:
		case STACK_BOTTOM:
			Collections.sort(gs, new XComparator());
			break;
		default:
			throw new IllegalArgumentException("This method only handles STACK_* layoutTypes");
		}

		for (int i = 1; i < gs.size(); i++) {
			// Get the current and previous graphics objects
			VGroupable eCurr = gs.get(i);
			VGroupable ePrev = gs.get(i - 1);

			// Get the bounds of the model to view translated shapes
			Rectangle2D vBoundsPrev = ((VPathwayObject) ePrev).getVShape(true).getBounds2D();
			Rectangle2D vBoundsCurr = ((VPathwayObject) eCurr).getVShape(true).getBounds2D();
			switch (stackType) {
			case STACK_CENTERX:
				((VElement) eCurr).vMoveBy(vBoundsPrev.getCenterX() - vBoundsCurr.getCenterX(),
						vBoundsPrev.getMaxY() - vBoundsCurr.getY());
				break;
			case STACK_CENTERY:
				((VElement) eCurr).vMoveBy(vBoundsPrev.getMaxX() - vBoundsCurr.getX(),
						vBoundsPrev.getCenterY() - vBoundsCurr.getCenterY());
				break;
			case STACK_LEFT:
				((VElement) eCurr).vMoveBy(vBoundsPrev.getX() - vBoundsCurr.getX(),
						vBoundsPrev.getMaxY() - vBoundsCurr.getY());
				break;
			case STACK_RIGHT:
				((VElement) eCurr).vMoveBy(vBoundsPrev.getMaxX() - vBoundsCurr.getMaxX(),
						vBoundsPrev.getMaxY() - vBoundsCurr.getY());
				break;
			case STACK_TOP:
				((VElement) eCurr).vMoveBy(vBoundsPrev.getMaxX() - vBoundsCurr.getX(),
						vBoundsPrev.getY() - vBoundsCurr.getY());
				break;
			case STACK_BOTTOM:
				((VElement) eCurr).vMoveBy(vBoundsPrev.getMaxX() - vBoundsCurr.getX(),
						vBoundsPrev.getMaxY() - vBoundsCurr.getMaxY());
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Scales a set of objects by max width
	 * 
	 * @param gs the list of elements.
	 */
	private void scaleWidth(List<VGroupable> gs) {
		double maxW = 0;
		VGroupable gMax = null;
		for (VGroupable g : gs) {
			Rectangle2D r = ((VPathwayObject) g).getVShape(true).getBounds2D();
			double w = Math.abs(r.getWidth());
			if (w > maxW) {
				gMax = g;
				maxW = w;
			}
		}
		for (VGroupable g : gs) {
			if (g == gMax)
				continue;

			Rectangle2D r = ((VPathwayObject) g).getVShape(true).getBounds2D();
			double oldWidth = r.getWidth();
			if (oldWidth < 0) {
				r.setRect(r.getX(), r.getY(), -(maxW), r.getHeight());
				((VElement) g).setVScaleRectangle(r);
				((VElement) g).vMoveBy((oldWidth + maxW) / 2, 0);
			} else {
				r.setRect(r.getX(), r.getY(), maxW, r.getHeight());
				((VPathwayObject) g).setVScaleRectangle(r);
				((VElement) g).vMoveBy((oldWidth - maxW) / 2, 0);
			}
		}
	}

	/**
	 * Scales selected objects by max height
	 * 
	 * @param gs the list of elements.
	 */
	private void scaleHeight(List<VGroupable> gs) {
		double maxH = 0;
		VGroupable gMax = null;
		for (VGroupable g : gs) {
			Rectangle2D r = ((VPathwayObject) g).getVShape(true).getBounds2D();
			double h = Math.abs(r.getHeight());
			if (h > maxH) {
				gMax = g;
				maxH = h;
			}
		}
		for (VGroupable g : gs) {
			if (g == gMax)
				continue;

			Rectangle2D r = ((VPathwayObject) g).getVShape(true).getBounds2D();
			double oldHeight = r.getHeight();
			if (oldHeight < 0) {
				r.setRect(r.getX(), r.getY(), r.getWidth(), -(maxH));
				((VPathwayObject) g).setVScaleRectangle(r);
				((VElement) g).vMoveBy(0, (maxH + oldHeight) / 2);
			} else {
				r.setRect(r.getX(), r.getY(), r.getWidth(), maxH);
				((VPathwayObject) g).setVScaleRectangle(r);
				((VElement) g).vMoveBy(0, (oldHeight - maxH) / 2);
			}
		}
	}

	// ================================================================================
	// Z-Order Methods
	// ================================================================================
	/**
	 * Returns the highest z-order of all pathway model objects with z-order.
	 * 
	 * @param pathwayModel the pathway model.
	 * @return the highest z-order of all pathway model objects.
	 */
	public int getMaxZOrder() {
		List<PathwayElement> dataObjects = data.getPathwayElements();
		if (dataObjects.size() == 0)
			return 0;
		int zMax = 0;
		for (DataNode e : data.getDataNodes()) {
			zMax = Math.max(e.getZOrder(), zMax);
			for (State st : e.getStates())
				zMax = Math.max(st.getZOrder(), zMax);
		}
		for (ShapedElement e : data.getShapedElements()) {
			zMax = Math.max(e.getZOrder(), zMax);
		}
		for (LineElement e : data.getLineElements()) {
			zMax = Math.max(e.getZOrder(), zMax);
		}
		return zMax;
	}

	/**
	 * Returns the lowest z-order of all pathway model objects with z-order.
	 * 
	 * @param the pathwayModel.
	 */
	public int getMinZOrder() {
		List<PathwayElement> dataObjects = data.getPathwayElements();
		if (dataObjects.size() == 0)
			return 0;
		int zMin = 0;
		// no need to check z-order of states, z-order of datanode is always lower.
		for (ShapedElement e : data.getShapedElementsExclStates()) {
			zMin = Math.min(e.getZOrder(), zMin);
		}
		for (LineElement e : data.getLineElements()) {
			zMin = Math.min(e.getZOrder(), zMin);
		}
		return zMin;
	}

	/**
	 * Moves a set of graphics to the top in the z-order stack.
	 *
	 * @param gs the set of graphics to move.
	 */
	public void moveGraphicsTop(List<VDrawable> gs) {
		Collections.sort(gs, new ZComparator());
		int base = getMaxZOrder() + 1;
		for (VDrawable g : gs) {
			g.getPathwayObject().setZOrder(base++);
		}
	}

	/**
	 * Moves a set of graphics to the bottom in the z-order stack.
	 *
	 * @param gs the set of graphics to move.
	 */
	public void moveGraphicsBottom(List<VDrawable> gs) {
		Collections.sort(gs, new ZComparator());
		int base = getMinZOrder() - gs.size() - 1;
		for (VDrawable g : gs) {
			g.getPathwayObject().setZOrder(base++);
		}
	}

	/**
	 * Looks for overlapping graphics with a higher z-order and moves g on top of
	 * that.
	 * 
	 * @param gs the set of graphics to move.
	 */
	public void moveGraphicsUp(List<VDrawable> gs) {
		// TODO: Doesn't really work very well with multiple selections
		for (VDrawable g : gs) {
			// make sure there is enough space between g and the next
			autoRenumberZOrder();

			int order = g.getPathwayObject().getZOrder();
			VDrawable nextGraphics = null;
			int nextZ = order;
			for (VDrawable i : getOverlappingGraphics(g)) {
				int iorder = i.getPathwayObject().getZOrder();
				if (nextGraphics == null && iorder > nextZ) {
					nextZ = iorder;
					nextGraphics = i;
				} else if (nextGraphics != null && iorder < nextZ && iorder > order) {
					nextZ = iorder;
					nextGraphics = i;
				}
			}
			g.getPathwayObject().setZOrder(nextZ + 1);
		}
	}

	/**
	 * Makes sure there is always a minimum spacing of two between two consecutive
	 * elements, so that we can freely move items in between.
	 */
	private void autoRenumberZOrder() {
		List<VGroupable> elts = new ArrayList<VGroupable>();
		for (VElement vp : drawingObjects) {
			if (vp instanceof VGroupable) {
				elts.add((VGroupable) vp);
			}
		}
		if (elts.size() < 2)
			return; // nothing to renumber
		Collections.sort(elts, new ZComparator());

		final int spacing = 2;

		int waterLevel = elts.get(0).getPathwayObject().getZOrder();
		for (int i = 1; i < elts.size(); ++i) {
			VGroupable curr = elts.get(i);
			if (curr.getPathwayObject().getZOrder() - waterLevel < spacing) {
				curr.getPathwayObject().setZOrder(waterLevel + spacing);
			}
			waterLevel = curr.getPathwayObject().getZOrder();
		}
	}

	/**
	 * Looks for overlapping graphics with a lower z-order and moves g on under
	 * that.
	 * 
	 * @param gs the set of graphics to move.
	 */
	public void moveGraphicsDown(List<VDrawable> gs) {
		// TODO: Doesn't really work very well with multiple selections
		for (VDrawable g : gs) {
			// make sure there is enough space between g and the previous
			autoRenumberZOrder();

			int order = g.getPathwayObject().getZOrder();
			VDrawable nextGraphics = null;
			int nextZ = order;
			for (VDrawable i : getOverlappingGraphics(g)) {
				int iorder = i.getPathwayObject().getZOrder();
				if (nextGraphics == null && iorder < nextZ) {
					nextZ = iorder;
					nextGraphics = i;
				} else if (nextGraphics != null && iorder > nextZ && iorder < order) {
					nextZ = iorder;
					nextGraphics = i;
				}
			}
			g.getPathwayObject().setZOrder(nextZ - 1);
		}
	}

	/**
	 * return a list of Graphics that overlap g. Note that the intersection of
	 * bounding rectangles is used, so the returned list is only an approximation
	 * for rounded shapes.
	 * 
	 * @param g the graphics.
	 */
	public List<VDrawable> getOverlappingGraphics(VDrawable g) {
		List<VDrawable> result = new ArrayList<VDrawable>();
		Rectangle2D r1 = ((VElement) g).getVBounds();

		for (VElement ve : drawingObjects) {
			if (ve instanceof VDrawable && ve != g) {
				VDrawable i = (VDrawable) ve;
				if (r1.intersects(ve.getVBounds())) {
					result.add(i);
				}
			}
		}
		return result;
	}

	// ================================================================================
	// Copy and Paste Methods
	// ================================================================================
	/**
	 * Makes a copy of all PathwayElements in current selection, and puts them in
	 * the global clipboard.
	 */
	public void copyToClipboard() {
		List<CopyElement> result = new ArrayList<CopyElement>();
		for (VElement g : drawingObjects) {
			if (g.isSelected() && g instanceof VPathwayElement && !(g instanceof SelectionBox)) {
				result.add(((VPathwayElement) g).getPathwayObject().copy());
			}
		}
		if (result.size() > 0) {
			if (parent != null)
				parent.copyToClipboard(getPathwayModel(), result);
		}
	}

	/**
	 * @param elements the list of elements.
	 */
	public void paste(List<CopyElement> elements) {
		paste(elements, 0, 0);
	}

	/**
	 * @param elements the list of elements.
	 * @param xShift
	 * @param yShift
	 */
	public void paste(List<CopyElement> elements, double xShift, double yShift) {
		undoManager.newAction("Paste");
		clearSelection();
		/*
		 * This map provides PathwayObject "relationship" reference information. For
		 * example, if both a LineElement and the DataNode it is pointing to are copied,
		 * then they need to be reconnected.
		 */
		BidiMap<PathwayObject, PathwayObject> newerToSource = new DualHashBidiMap<>();
		boolean showWarning = true;
		// Copy pathway objects of given list
		for (CopyElement copyElement : elements) {
			PathwayElement newElement = copyElement.getNewElement();
			PathwayElement srcElement = copyElement.getSourceElement();
			lastAdded = null;
			// shift location of pathway element for pasting
			if (newElement instanceof LineElement) {
				// if line element, shift position of its points
				for (LinePoint mp : ((LineElement) newElement).getLinePoints()) {
					mp.setX(mp.getX() + xShift);
					mp.setY(mp.getY() + yShift);
				}
			} else if (newElement instanceof ShapedElement) {
				((ShapedElement) newElement).setLeft(((ShapedElement) newElement).getLeft() + xShift);
				((ShapedElement) newElement).setTop(((ShapedElement) newElement).getTop() + yShift);
				// if datanode, also shift position of its states
				if (newElement.getObjectType() == ObjectType.DATANODE) {
					for (State state : ((DataNode) newElement).getStates()) {
						state.setLeft(state.getLeft() + xShift);
						state.setTop(state.getTop() + xShift);
					}
				}
			}
			// prepare for paste
			CopyElement copyOfCopyElement = newElement.copy();
			PathwayElement newerElement = copyOfCopyElement.getNewElement();
			data.add(newerElement); // causes lastAdded to be set
			// load references
			newerElement.copyReferencesFrom(srcElement);
			// print message if references copied
			if (showWarning) {
				showWarning = pasteReferencesMessage(newerElement);
			}
			// skip these steps if pathway
			if (newerElement.getObjectType() != ObjectType.PATHWAY) {
				// store information
				newerToSource.put(newerElement, srcElement);
				// specially store anchor information
				if (newerElement instanceof LineElement) {
					Iterator<Anchor> it1 = ((LineElement) newerElement).getAnchors().iterator();
					Iterator<Anchor> it2 = ((LineElement) srcElement).getAnchors().iterator();
					while (it1.hasNext() && it2.hasNext()) {
						Anchor na = it1.next();
						Anchor sa = it2.next();
						if (na != null && sa != null) {
							newerToSource.put(na, sa);
						}
					}
				}
				lastAdded.select();
				selection.addToSelection(lastAdded);
			}
		}
		for (PathwayObject newerElement : newerToSource.keySet()) {
			PathwayObject srcElement = newerToSource.get(newerElement);
			// add group members in new Group
			if (newerElement.getObjectType() == ObjectType.GROUP && srcElement.getObjectType() == ObjectType.GROUP) {
				for (Groupable srcMember : ((Group) srcElement).getPathwayElements()) {
					Groupable newerMember = (Groupable) newerToSource.getKey(srcMember);
					if (newerMember != null) {
						((Group) newerElement).addPathwayElement(newerMember);
					}
				}
				((Group) newerElement).updateDimensions();
			}
			// set aliasRef if any, and link to group if group also copied
			else if (newerElement.getObjectType() == ObjectType.DATANODE
					&& srcElement.getObjectType() == ObjectType.DATANODE) {
				if (((DataNode) newerElement).getType() == DataNodeType.ALIAS
						&& ((DataNode) srcElement).getType() == DataNodeType.ALIAS) {
					Group srcAliasRef = ((DataNode) srcElement).getAliasRef();
					// if group aliasRef was also copied
					if (srcAliasRef != null) {
						Group newerAliasRef = (Group) newerToSource.getKey(srcAliasRef);
						if (newerAliasRef != null) {
							((DataNode) newerElement).setAliasRef(newerAliasRef);
							JOptionPane.showConfirmDialog(null,
									"Copy of alias data node linked to Group " + newerAliasRef.getElementId() + ".",
									"Warning", JOptionPane.PLAIN_MESSAGE);
						}
					}
					// otherwise aliasRef is not linked to any group
					else {
						JOptionPane.showConfirmDialog(null, "Copy of alias data node not linked to any group.",
								"Warning", JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
			// link LineElement linePoint elementRefs
			else if (newerElement instanceof LineElement && srcElement instanceof LineElement) {
				// set start elementRef
				LinkableTo srcStartElementRef = ((LineElement) srcElement).getStartElementRef();
				if (srcStartElementRef != null) {
					LinkableTo newerStartElementRef = (LinkableTo) newerToSource.getKey(srcStartElementRef);
					if (newerStartElementRef != null) {
						LinePoint startPoint = ((LineElement) newerElement).getStartLinePoint();
						LinePoint srcPoint = ((LineElement) srcElement).getStartLinePoint();
						startPoint.linkTo(newerStartElementRef, srcPoint.getRelX(), srcPoint.getRelY());
					}
				}
				// set end elementRef
				LinkableTo srcEndElementRef = ((LineElement) srcElement).getEndElementRef();
				if (srcEndElementRef != null) {
					LinkableTo newerEndElementRef = (LinkableTo) newerToSource.getKey(srcEndElementRef);
					if (newerEndElementRef != null) {
						LinePoint endPoint = ((LineElement) newerElement).getEndLinePoint();
						LinePoint srcPoint = ((LineElement) srcElement).getEndLinePoint();
						endPoint.linkTo(newerEndElementRef, srcPoint.getRelX(), srcPoint.getRelY());
					}
				}
			}
		}
		// refresh connector shapes
		for (LineElement o : data.getLineElements()) {
			o.getConnectorShape().recalculateShape(o);
		}
		moveGraphicsTop(getSelectedGraphics());
		redraw();
	}

	/**
	 * Prints message if references copied for pasted element.
	 * 
	 * @param e the pathway element
	 */
	public boolean pasteReferencesMessage(PathwayElement e) {
		// print message if references copied
		String refStr = null;
		boolean hasAnnt = !e.getAnnotationRefs().isEmpty();
		boolean hasCit = !e.getCitationRefs().isEmpty();
		boolean hasEvid = !e.getEvidenceRefs().isEmpty();
		if (hasAnnt && hasCit && hasEvid) {
			refStr = "References";
		} else if (hasAnnt && hasCit) {
			refStr = "Annotations and Citations";
		} else if (hasAnnt && hasEvid) {
			refStr = "Annotations and Evidences";
		} else if (hasCit && hasEvid) {
			refStr = "Citations and Evidences";
		} else if (hasAnnt) {
			refStr = "Annotations";
		} else if (hasCit) {
			refStr = "Citations";
		} else if (hasEvid) {
			refStr = "Evidences";
		}
		if (refStr != null) {
			Object[] options = { "Ok", "Ok All" };
			int n = JOptionPane.showOptionDialog(null, refStr + " copied for " + e.getObjectType().getTag() + ".",
					"Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (n == 1) { // do not show this dialog again for pasting session
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	public void pasteFromClipboard() {
		if (isEditMode()) { // Only paste in edit mode
			if (parent != null)
				parent.pasteFromClipboard();
		}
	}

	/**
	 * Paste from clip board at the current cursor position.
	 */
	public void positionPasteFromClipboard(Point cursorPosition) {
		if (isEditMode()) {
			if (parent != null)
				parent.positionPasteFromClipboard(cursorPosition);
		}
	}

	// ================================================================================
	// Listening and Firing Methods
	// ================================================================================
	private List<VPathwayModelListener> listeners = new ArrayList<VPathwayModelListener>();

	public void addVPathwayListener(VPathwayModelListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	public void removeVPathwayListener(VPathwayModelListener l) {
		Logger.log.trace(listeners.remove(l) + ": " + l);
	}

	private List<VElementMouseListener> elementListeners = new ArrayList<VElementMouseListener>();

	public void addVElementMouseListener(VElementMouseListener l) {
		if (!elementListeners.contains(l)) {
			elementListeners.add(l);
		}
	}

	public void removeVElementMouseListener(VElementMouseListener l) {
		elementListeners.remove(l);
	}

	protected void fireVElementMouseEvent(VElementMouseEvent e) {
		for (VElementMouseListener l : elementListeners) {
			l.vElementMouseEvent(e);
		}
	}

	/**
	 * Adds a {@link SelectionListener} to the SelectionBox of this VPathway
	 *
	 * @param listener The SelectionListener to add
	 */
	public void addSelectionListener(SelectionListener listener) {
		selection.addListener(listener);
	}

	/**
	 * Removes a {@link SelectionListener} from the SelectionBox of this VPathway
	 *
	 * @param listenerl The SelectionListener to remove
	 */
	public void removeSelectionListener(SelectionListener listener) {
		selection.removeListener(listener);
	}

	protected void fireVPathwayEvent(VPathwayModelEvent e) {
		for (VPathwayModelListener listener : listeners) {
			listener.vPathwayModelEvent(e);
		}
	}

	// ================================================================================
	// Transformation Methods
	// ================================================================================
	private AffineTransform vFromM = new AffineTransform();

	/**
	 * Helper method to convert view {@link VCoordinate} to model {@link Coordinate}
	 * accounting for canvas zoomFactor.
	 * 
	 * @param v the view coordinate.
	 */
	public double mFromV(double v) {
		return v / zoomFactor;
	}

	/**
	 * Helper method to convert model {@link Coordinate} to view {@link VCoordinate}
	 * accounting for canvas zoomFactor.
	 * 
	 * @param m the model coordinate.
	 */
	public double vFromM(double m) {
		return m * zoomFactor;
	}

	/**
	 * @param s
	 * @return
	 */
	public java.awt.Shape vFromM(java.awt.Shape s) {
		vFromM.setToScale(zoomFactor, zoomFactor);
		return vFromM.createTransformedShape(s);
	}

	/**
	 * Returns of entire PathwayModel view (taking into account zoom).
	 */
	public int getVWidth() {
		return data == null ? 0 : (int) vFromM(data.getPathway().getBoardWidth());
	}

	/**
	 * Returns height of entire PathwayModel view (taking into account zoom).
	 */
	public int getVHeight() {
		return data == null ? 0 : (int) vFromM(data.getPathway().getBoardHeight());
	}

	/**
	 * This class sorts graphics by VCenterY.
	 * 
	 * @author unknown
	 */
	public static class YComparator implements Comparator<VGroupable> {
		public int compare(VGroupable g1, VGroupable g2) {
			if (g1.getVCenterY() == g2.getVCenterY())
				return 0;
			else if (g1.getVCenterY() < g2.getVCenterY())
				return -1;
			else
				return 1;
		}
	}

	/**
	 * This class sorts graphics by VCenterX.
	 * 
	 * @author unknown
	 */
	public static class XComparator implements Comparator<VGroupable> {

		/**
		 *
		 */
		public int compare(VGroupable g1, VGroupable g2) {
			if (g1.getVCenterX() == g2.getVCenterX())
				return 0;
			else if (g1.getVCenterX() < g2.getVCenterX())
				return -1;
			else
				return 1;
		}
	}

	/**
	 * This class sorts VPathwayObject by ZOrder.
	 * 
	 * @author unknown
	 */
	public static class ZComparator implements Comparator<VDrawable> {
		public int compare(VDrawable g1, VDrawable g2) {
			return g1.getPathwayObject().getZOrder() - g2.getPathwayObject().getZOrder();
		}
	}

	// ================================================================================
	// Undo Methods
	// ================================================================================
	private UndoManager undoManager = new UndoManager();

	/**
	 * Activates the undo manager by providing an engine to which the undo actions
	 * will be applied. If this method is not called, the undo manager will not
	 * record any undo actions.
	 */
	public void activateUndoManager(Engine engine) {
		undoManager.activate(engine);
	}

	/**
	 * Returns undoManager owned by this instance of VPathway.
	 */
	public UndoManager getUndoManager() {
		return undoManager;
	}

	/*
	 * To be called only by undo.
	 */
	/*
	 * public void setUndoManager(UndoManager value) { undoManager = value; }
	 */
	public void undo() {
		undoManager.undo();
	}

	// ================================================================================
	// Clean-Up and Dispose Methods
	// ================================================================================
	private boolean disposed = false;

	/**
	 * free all resources (such as listeners) held by this class. Owners of this
	 * class must explicitly dispose of it to clean up.
	 */
	public void dispose() {
		assert (!disposed);
		for (int i = getDrawingObjects().size() - 1; i >= 0; i--) {
			getDrawingObjects().get(i).destroy();
		}
		// TODO to avoid concurrent modification issue?
//		for (VElement elt : getDrawingObjects()) {
//			elt.destroy();
//		}
		cleanUp();
		if (data != null) {
			data.removeListener(this);
		}
		listeners.clear();
		selection.getListeners().clear();
		viewActions = null;
		if (parent != null) {
			parent.dispose();
		}
		parent = null; // disconnect from VPathwaySwing
		undoManager.dispose();
		undoManager = null;
		hoverManager.stop();
		disposed = true;
	}

	private void cleanUp() {
		for (Iterator<VElement> i = drawingObjects.iterator(); i.hasNext();) {
			VElement elt = i.next();
			if (elt.toBeRemoved()) {
				i.remove();
			}
		}
	}

}
