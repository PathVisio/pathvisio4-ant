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
package org.pathvisio.libgpml.model;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bridgedb.Xref;
//import org.pathvisio.core.model.BatikImageExporter; TODO 
//import org.pathvisio.core.model.ImageExporter; TODO 
import org.pathvisio.libgpml.biopax.BiopaxElement;
import org.pathvisio.libgpml.biopax.BiopaxNode;
import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.io.ConverterException;
import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.model.GraphLink.LinkableFrom;
import org.pathvisio.libgpml.model.PathwayObject.Anchor;
import org.pathvisio.libgpml.model.PathwayObject.LinePoint;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.prop.StaticProperty;
import org.pathvisio.libgpml.util.Utils;

/**
 * This class is the model for pathway data. It is responsible for storing all
 * information necessary for maintaining, loading and saving pathway data.
 *
 * Pathway contains multiple PathwayElements. Pathway is guaranteed to always
 * have exactly one object of the type MAPPINFO and exactly one object of the
 * type INFOBOX.
 */
public class PathwayModel {
	private boolean changed = true;

	/**
	 * The "changed" flag tracks if the Pathway has been changed since the file was
	 * opened or last saved. New pathways start changed.
	 */
	public boolean hasChanged() {
		return changed;
	}

	/**
	 * clearChangedFlag should be called after when the current pathway is known to
	 * be the same as the one on disk. This happens when you just opened it, or when
	 * you just saved it.
	 */
	public void clearChangedFlag() {
		if (changed) {
			changed = false;
			fireStatusFlagEvent(new StatusFlagEvent(changed));
			// System.out.println ("Changed flag is cleared");
		}
	}

	/**
	 * To be called after each edit operation
	 */
	private void markChanged() {
		if (!changed) {
			changed = true;
			fireStatusFlagEvent(new StatusFlagEvent(changed));
			// System.out.println ("Changed flag is set");
		}
	}

	/**
	 * List of contained dataObjects
	 */
	private List<PathwayObject> dataObjects = new ArrayList<PathwayObject>();

	/**
	 * Getter for dataobjects contained. There is no setter, you have to add
	 * dataobjects individually
	 * 
	 * @return List of dataObjects contained in this pathway
	 */
	public List<PathwayObject> getDataObjects() {
		return dataObjects;
	}

	/**
	 * Get a pathway element by it's GraphId
	 * 
	 * @param graphId The graphId of the element
	 * @return The pathway element with the given id, or null when no element was
	 *         found
	 */
	public PathwayObject getElementById(String graphId) {
		// TODO: dataobject should be stored in a hashmap, with the graphId as key!
		if (graphId != null) {
			for (PathwayObject e : dataObjects) {
				if (graphId.equals(e.getElementId())) {
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * Takes the Xref of all DataNodes in this pathway and returns them as a List.
	 *
	 * returns an empty arraylist if there are no datanodes in this pathway.
	 */
	public List<Xref> getDataNodeXrefs() {
		List<Xref> result = new ArrayList<Xref>();
		for (PathwayObject e : dataObjects) {
			if (e.getObjectType() == ObjectType.DATANODE) {
				result.add(e.getXref());
			}
		}
		return result;
	}

	/**
	 * Takes the Xref of all Lines in this pathway and returns them as a List.
	 *
	 * returns an empty arraylist if there are no lines in this pathway.
	 */
	public List<Xref> getLineXrefs() {
		List<Xref> result = new ArrayList<Xref>();
		for (PathwayObject e : dataObjects) {
			if (e.getObjectType() == ObjectType.LINE) {
				result.add(e.getXref());
			}
		}
		return result;
	}

	private PathwayObject mappInfo = null;
	private PathwayObject infoBox = null;
	private BiopaxElement biopax = null;
	private PathwayObject legend = null;

	/**
	 * get the one and only MappInfo object.
	 *
	 * @return a PathwayElement with ObjectType set to mappinfo.
	 */
	public PathwayObject getMappInfo() {
		return mappInfo;
	}

	/**
	 * get the one and only InfoBox object.
	 *
	 * @return a PathwayElement with ObjectType set to mappinfo.
	 */
	public PathwayObject getInfoBox() {
		return infoBox;
	}

	/**
	 * @returns the BioPAX element of this pathway, containing literature references
	 *          and other optional biopax elements. Guaranteed to not return null.
	 *          If a BioPAX element does not yet exist, it is automatically created.
	 */
	public BiopaxElement getBiopax() {
		if (biopax == null) {
			PathwayObject tmp = PathwayObject.createPathwayElement(ObjectType.BIOPAX);
			this.add(tmp); // biopax will now be set.
		}
		return biopax;
	}

	/** @deprecated use getBiopax() instead */
	public BiopaxElement getBiopaxElementManager() {
		return getBiopax();
	}

	/**
	 * Add a PathwayElement to this Pathway. takes care of setting parent and
	 * removing from possible previous parent.
	 *
	 * fires PathwayEvent.ADDED event <i>after</i> addition of the object
	 *
	 * @param o The object to add
	 */
	public void add(PathwayObject o) {
		assert (o != null);
		// There can be only one mappInfo object, so if we're trying to add it, remove
		// the old one.
		if (o.getObjectType() == ObjectType.MAPPINFO && o != mappInfo) {
			if (mappInfo != null) {
				replaceUnique(mappInfo, o);
				mappInfo = o;
				return;
			}
			mappInfo = o;
		}
		// There can be only one InfoBox object, so if we're trying to add it, remove
		// the old one.
		if (o.getObjectType() == ObjectType.INFOBOX && o != infoBox) {
			if (infoBox != null) {
				replaceUnique(infoBox, o);
				infoBox = o;
				return;
			}
			infoBox = o;
		}
		// There can be zero or one Biopax object, so if we're trying to add it, remove
		// the old one.
		if (o instanceof BiopaxElement && o != biopax) {
			if (biopax != null) {
				replaceUnique(biopax, o);
				biopax = (BiopaxElement) o;
				return;
			}
			biopax = (BiopaxElement) o;
		}
		// There can be only one Legend object, so if we're trying to add it, remove the
		// old one.
		if (o.getObjectType() == ObjectType.LEGEND && o != legend) {
			if (legend != null) {
				replaceUnique(legend, o);
				legend = o;
				return;
			}
			legend = o;
		}
		if (o.getParent() == this)
			return; // trying to re-add the same object
		forceAddObject(o);
	}

	private void forceAddObject(PathwayObject o) {
		if (o.getParent() != null) {
			o.getParent().remove(o);
		}
		dataObjects.add(o);
		o.setParent(this);
		for (LinePoint p : o.getLinePoints()) {
			if (p.getElementRef() != null) {
				addGraphRef(p.getElementRef(), p);
			}
		}
		if (o.getGroupRef() != null) {
			addGroupRef(o.getGroupRef(), o);
		}
		for (Anchor a : o.getAnchors()) {
			if (a.getElementId() != null) {
				addGraphId(a.getElementId(), a);
			}
		}
		if (o.getElementId() != null) {
			addGraphId(o.getElementId(), o);
		}
		if (o.getGroupId() != null) {
			addGroupId(o.getGroupId(), o);
		}
		if (o.getElementRef() != null) {
			addGraphRef(o.getElementRef(), (LinkableFrom) o);
		}
		fireObjectModifiedEvent(new PathwayModelEvent(o, PathwayModelEvent.ADDED));
		checkMBoardSize(o);
	}

	/**
	 * get the highest z-order of all objects
	 */
	public int getMaxZOrder() {
		if (dataObjects.size() == 0)
			return 0;

		int zmax = dataObjects.get(0).getZOrder();
		for (PathwayObject e : dataObjects) {
			if (e.getZOrder() > zmax)
				zmax = e.getZOrder();
		}
		return zmax;
	}

	/**
	 * get the lowest z-order of all objects
	 */
	public int getMinZOrder() {
		if (dataObjects.size() == 0)
			return 0;

		int zmin = dataObjects.get(0).getZOrder();
		for (PathwayObject e : dataObjects) {
			if (e.getZOrder() < zmin)
				zmin = e.getZOrder();
		}
		return zmin;
	}

	/**
	 * only used by children of this Pathway to notify the parent of modifications
	 */
	void childModified(PathwayObjectEvent e) {
		markChanged();
		// a coordinate change could trigger dependent objects such as states,
		// groups and connectors to be updated as well.
		if (e.isCoordinateChange()) {

			PathwayObject elt = e.getModifiedPathwayElement();
			for (LinkableFrom refc : getReferringObjects(elt.getElementId())) {
				refc.refeeChanged();
			}

			String ref = elt.getGroupRef();
			if (ref != null && getGroupById(ref) != null) {
				// identify group object and notify model change to trigger view update
				PathwayObject group = getGroupById(ref);
				group.fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(group));
			}

			checkMBoardSize(e.getModifiedPathwayElement());
		}
	}

	/**
	 * called for biopax, infobox and mappInfo upon addition.
	 */
	private void replaceUnique(PathwayObject oldElt, PathwayObject newElt) {
		assert (oldElt.getParent() == this);
		assert (oldElt.getObjectType() == newElt.getObjectType());
		assert (newElt.getParent() == null);
		assert (oldElt != newElt);
		forceRemove(oldElt);
		forceAddObject(newElt);
	}

	/**
	 * removes object sets parent of object to null fires PathwayEvent.DELETED event
	 * <i>before</i> removal of the object
	 *
	 * @param o the object to remove
	 */
	public void remove(PathwayObject o) {
		assert (o.getParent() == this); // can only remove direct child objects
		if (o.getObjectType() == ObjectType.MAPPINFO)
			throw new IllegalArgumentException("Can't remove mappinfo object!");
		if (o.getObjectType() == ObjectType.INFOBOX)
			throw new IllegalArgumentException("Can't remove infobox object!");
		forceRemove(o);
	}

	/**
	 * removes object, regardless whether the object may be removed or not sets
	 * parent of object to null fires PathwayEvent.DELETED event <i>before</i>
	 * removal of the object
	 *
	 * @param o the object to remove
	 */
	private void forceRemove(PathwayObject o) {
		dataObjects.remove(o);
		for (LinkableFrom refc : getReferringObjects(o.getElementId())) {
			refc.unlink();
		}
		String groupRef = o.getGroupRef();
		if (groupRef != null) {
			removeGroupRef(groupRef, o);
		}
		// Add one or multiples literature(s) reference(s) to the list to deletion
		if (o.getBiopaxRefs() != null) {
			for (String ref : o.getBiopaxRefs()) {
				BiopaxNode node = getBiopax().getElement(ref);
				// if no an another pathway element use this literature reference
				// add to the list to deletion
				if (!getBiopax().hasReferences(node))
					biopaxReferenceToDelete.add(ref);
			}
		}
		for (Anchor a : o.getAnchors()) {
			if (a.getElementId() != null) {
				removeGraphId(a.getElementId());
			}
		}
		if (o.getElementId() != null) {
			removeGraphId(o.getElementId());
		}
		if (o.getGroupId() != null) {
			removeGroupId(o.getGroupId());
		}
		if (o.getElementRef() != null) {
			removeGraphRef(o.getElementRef(), (LinkableFrom) o);
		}
		fireObjectModifiedEvent(new PathwayModelEvent(o, PathwayModelEvent.DELETED));
		o.setParent(null);
	}

	/**
	 * Stores references of graph ids to other GraphRefContainers
	 */
	private Map<String, Set<LinkableFrom>> graphRefs = new HashMap<String, Set<LinkableFrom>>();
	private Map<String, LinkableTo> graphIds = new HashMap<String, LinkableTo>();

	public Set<String> getGraphIds() {
		return graphIds.keySet();
	}

	public LinkableTo getGraphIdContainer(String id) {
		return graphIds.get(id);
	}

	/**
	 * Returns all GraphRefContainers that refer to an object with a particular
	 * graphId.
	 */
	public Set<LinkableFrom> getReferringObjects(String id) {
		Set<LinkableFrom> refs = graphRefs.get(id);
		if (refs != null) {
			// create defensive copy to prevent problems with ConcurrentModification.
			return new HashSet<LinkableFrom>(refs);
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Register a link from a graph id to a graph ref
	 * 
	 * @param id     The graph id
	 * @param target The target GraphRefContainer
	 */
	public void addGraphRef(String id, LinkableFrom target) {
		Utils.multimapPut(graphRefs, id, target);
	}

	/**
	 * Remove a reference to another Id.
	 * 
	 * @param id
	 * @param target
	 */
	void removeGraphRef(String id, LinkableFrom target) {
		if (!graphRefs.containsKey(id))
			throw new IllegalArgumentException();

		graphRefs.get(id).remove(target);
		if (graphRefs.get(id).size() == 0)
			graphRefs.remove(id);
	}

	/**
	 * Registers an id that can subsequently be used for referral. It is tested for
	 * uniqueness.
	 * 
	 * @param id
	 */
	public void addGraphId(String id, LinkableTo idc) {
		if (idc == null || id == null) {
			throw new IllegalArgumentException("unique id can't be null");
		}
		if (graphIds.containsKey(id)) {
			throw new IllegalArgumentException("id '" + id + "' is not unique");
		}
		graphIds.put(id, idc);
	}

	void removeGraphId(String id) {
		graphIds.remove(id);
	}

	private Map<String, PathwayObject> groupIds = new HashMap<String, PathwayObject>();
	private Map<String, Set<PathwayObject>> groupRefs = new HashMap<String, Set<PathwayObject>>();

	public Set<String> getGroupIds() {
		return groupIds.keySet();
	}

	void addGroupId(String id, PathwayObject group) {
		if (id == null) {
			throw new IllegalArgumentException("unique id can't be null");
		}
		if (groupIds.containsKey(id)) {
			throw new IllegalArgumentException("id '" + id + "' is not unique");
		}
		groupIds.put(id, group);
	}

	void removeGroupId(String id) {
		groupIds.remove(id);
		Set<PathwayObject> elts = groupRefs.get(id);
		if (elts != null)
			for (PathwayObject elt : elts) {
				elt.groupRef = null;
				elt.fireObjectModifiedEvent(
						PathwayObjectEvent.createSinglePropertyEvent(elt, StaticProperty.GROUPREF));
			}
		groupRefs.remove(id);
	}

	public PathwayObject getGroupById(String id) {
		return groupIds.get(id);
	}

	void addGroupRef(String ref, PathwayObject child) {
		Utils.multimapPut(groupRefs, ref, child);
	}

	void removeGroupRef(String id, PathwayObject child) {
		if (!groupRefs.containsKey(id))
			throw new IllegalArgumentException();

		groupRefs.get(id).remove(child);

		// Find out if this element is the last one in a group
		// If so, remove the group as well
		if (groupRefs.get(id).size() == 0) {
			groupRefs.remove(id);
			PathwayObject group = getGroupById(id);
			if (group != null)
				forceRemove(group);
		} else {
			// redraw group outline
			if (getGroupById(id) != null) {
				Group group = (Group) getGroupById(id);
				group.fireObjectModifiedEvent(PathwayObjectEvent.createCoordinatePropertyEvent(group));
			}
		}
	}

	/**
	 * Get the pathway elements that are part of the given group
	 * 
	 * @param id The id of the group
	 * @return The set of pathway elements part of the group
	 */
	public Set<PathwayObject> getGroupElements(String id) {
		Set<PathwayObject> result = groupRefs.get(id);
		// Return an empty set if the group is empty
		return result == null ? new HashSet<PathwayObject>() : result;
	}

	public String getUniqueGraphId() {
		return getUniqueId(graphIds.keySet());
	}

	public String getUniqueGroupId() {
		return getUniqueId(groupIds.keySet());
	}

	/**
	 * Generate random ids, based on strings of hex digits (0..9 or a..f) Ids are
	 * unique across both graphIds and groupIds per pathway
	 * 
	 * @param ids The collection of already existing ids
	 * @return an Id unique for this pathway
	 */
	public String getUniqueId(Set<String> ids) {
		String result;
		Random rn = new Random();
		int mod = 0x60000; // 3 hex letters
		int min = 0xa0000; // has to start with a letter
		// in case this map is getting big, do more hex letters
		if ((ids.size()) > 0x10000) {
			mod = 0x60000000;
			min = 0xa0000000;
		}

		do {
			result = Integer.toHexString(Math.abs(rn.nextInt()) % mod + min);
		} while (ids.contains(result));

		return result;
	}

	double mBoardWidth = 0;
	double mBoardHeight = 0;

	private static final int BORDER_SIZE = 30;

	/**
	 * Checks whether the board size is still large enough for the given
	 * {@link PathwayObject} and increases the size if not
	 * 
	 * @param elm The element to check the board size for
	 */
	private void checkMBoardSize(PathwayObject e) {
		double mw = mBoardWidth;
		double mh = mBoardHeight;

		switch (e.getObjectType()) {
		case LINE:
			mw = Math.max(mw, BORDER_SIZE + Math.max(e.getStartLinePointX(), e.getEndLinePointX()));
			mh = Math.max(mh, BORDER_SIZE + Math.max(e.getStartLinePointY(), e.getEndLinePointY()));
			break;
		case GRAPHLINE:
			mw = Math.max(mw, BORDER_SIZE + Math.max(e.getStartLinePointX(), e.getEndLinePointX()));
			mh = Math.max(mh, BORDER_SIZE + Math.max(e.getStartLinePointY(), e.getEndLinePointY()));
			break;
		default:
			mw = Math.max(mw, BORDER_SIZE + e.getLeft() + e.getWidth());
			mh = Math.max(mh, BORDER_SIZE + e.getTop() + e.getHeight());
			break;
		}

		if (Math.abs(mBoardWidth - mw) + Math.abs(mBoardHeight - mh) > 0.01) {
			mBoardWidth = mw;
			mBoardHeight = mh;
			fireObjectModifiedEvent(new PathwayModelEvent(mappInfo, PathwayModelEvent.RESIZED));
		}
	}

	public double[] getMBoardSize() {
		return new double[] { mBoardWidth, mBoardHeight };
	}

	private File sourceFile = null;

	/**
	 * Gets the xml file containing the Gpml/mapp pathway currently displayed
	 * 
	 * @return current xml file
	 */
	public File getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(File file) {
		sourceFile = file;
	}

	/**
	 * Contructor for this class, creates a new gpml document
	 */
	public PathwayModel() {
		mappInfo = PathwayObject.createPathwayElement(ObjectType.MAPPINFO);
		this.add(mappInfo);
		infoBox = PathwayObject.createPathwayElement(ObjectType.INFOBOX);
		this.add(infoBox);
	}

	/*
	 * Call when making a new mapp.
	 */
	public void initMappInfo() {
		String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
		mappInfo.setVersion(dateString);
		mappInfo.setTitle("New Pathway");
	}

	/**
	 * Writes the JDOM document to the file specified
	 * 
	 * @param file     the file to which the JDOM document should be saved
	 * @param validate if true, validate the dom structure before writing to file.
	 *                 If there is a validation error, or the xsd is not in the
	 *                 classpath, an exception will be thrown.
	 */
	public void writeToXml(File file, boolean validate) throws ConverterException {
		GpmlFormat.writeToXml(this, file, validate);
		setSourceFile(file);
		clearChangedFlag();

	}

	public void readFromXml(Reader in, boolean validate) throws ConverterException {
		GpmlFormat.readFromXml(this, in, validate);
		setSourceFile(null);
		clearChangedFlag();
	}

	public void readFromXml(InputStream in, boolean validate) throws ConverterException {
		GpmlFormat.readFromXml(this, in, validate);
		setSourceFile(null);
		clearChangedFlag();
	}

	public void readFromXml(File file, boolean validate) throws ConverterException {
		Logger.log.info("Start reading the XML file: " + file);
		GpmlFormat.readFromXml(this, file, validate);
		setSourceFile(file);
		clearChangedFlag();
	}

	/**
	 * Implement this interface if you want to be notified when the "changed" status
	 * changes. This happens e.g. when the user makes a change to an unchanged
	 * pathway, or when a changed pathway is saved.
	 */
	public interface StatusFlagListener extends EventListener {
		public void statusFlagChanged(StatusFlagEvent e);
	}

	/**
	 * Event for a change in the "changed" status of this Pathway
	 */
	public static class StatusFlagEvent {
		private boolean newStatus;

		public StatusFlagEvent(boolean newStatus) {
			this.newStatus = newStatus;
		}

		public boolean getNewStatus() {
			return newStatus;
		}
	}

	private List<StatusFlagListener> statusFlagListeners = new ArrayList<StatusFlagListener>();

	/**
	 * Register a status flag listener
	 */
	public void addStatusFlagListener(StatusFlagListener v) {
		if (!statusFlagListeners.contains(v))
			statusFlagListeners.add(v);
	}

	/**
	 * Remove a status flag listener
	 */
	public void removeStatusFlagListener(StatusFlagListener v) {
		statusFlagListeners.remove(v);
	}

	// TODO: make private
	public void fireStatusFlagEvent(StatusFlagEvent e) {
		for (StatusFlagListener g : statusFlagListeners) {
			g.statusFlagChanged(e);
		}
	}

	private List<PathwayModelListener> listeners = new ArrayList<PathwayModelListener>();

	public void addListener(PathwayModelListener v) {
		if (!listeners.contains(v))
			listeners.add(v);
	}

	public void removeListener(PathwayModelListener v) {
		listeners.remove(v);
	}

	/**
	 * Firing the ObjectModifiedEvent has the side effect of marking the Pathway as
	 * changed.
	 */
	public void fireObjectModifiedEvent(PathwayModelEvent e) {
		markChanged();
		for (PathwayModelListener g : listeners) {
			g.pathwayModified(e);
		}
	}

	public PathwayModel clone() {
		PathwayModel result = new PathwayModel();
		for (PathwayObject pe : dataObjects) {
			result.add(pe.copy());
		}
		result.changed = changed;
		if (sourceFile != null) {
			result.sourceFile = new File(sourceFile.getAbsolutePath());
		}
		// do not copy status flag listeners
//		for(StatusFlagListener l : statusFlagListeners) {
//			result.addStatusFlagListener(l);
//		}
		return result;
	}

	public String summary() {
		String result = "    " + toString() + "\n    with Objects:";
		for (PathwayObject pe : dataObjects) {
			String code = pe.toString();
			code = code.substring(code.lastIndexOf('@'), code.length() - 1);
			result += "\n      " + code + " " + pe.getObjectType().getTag() + " " + pe.getParent();
		}
		return result;
	}

	/**
	 * Check for any dangling references, and fix them if found This is called just
	 * before writing out a pathway.
	 *
	 * This is a fallback solution for problems elsewhere in the reference handling
	 * code. Theoretically, if the rest of the code is bug free, this should always
	 * return 0.
	 *
	 * @return number of references fixed. Should be 0 under normal circumstances.
	 */
	public int fixReferences() {
		int result = 0;
		Set<String> graphIds = new HashSet<String>();
		for (PathwayObject pe : dataObjects) {
			String id = pe.getElementId();
			if (id != null) {
				graphIds.add(id);
			}
			for (PathwayObject.Anchor pp : pe.getAnchors()) {
				String pid = pp.getElementId();
				if (pid != null) {
					graphIds.add(pid);
				}
			}
		}
		for (PathwayObject pe : dataObjects) {
			if (pe.getObjectType() == ObjectType.LINE) {
				String ref = pe.getStartElementRef();
				if (ref != null && !graphIds.contains(ref)) {
					pe.setStartElementRef(null);
					result++;
				}

				ref = pe.getEndElementRef();
				if (ref != null && !graphIds.contains(ref)) {
					pe.setEndElementRef(null);
					result++;
				}
			}
		}
		if (result > 0) {
			Logger.log.warn("Pathway.fixReferences fixed " + result + " reference(s)");
		}
		for (String ref : biopaxReferenceToDelete) {
			getBiopax().removeElement(getBiopax().getElement(ref));
		}
		return result;
	}

	/**
	 * Transfer statusflag listeners from one pathway to another. This is used
	 * needed when copies of the pathway are created / returned by UndoManager. The
	 * status flag listeners are only interested in status flag events of the active
	 * copy.
	 */
	public void transferStatusFlagListeners(PathwayModel dest) {
		for (Iterator<StatusFlagListener> i = statusFlagListeners.iterator(); i.hasNext();) {
			StatusFlagListener l = i.next();
			dest.addStatusFlagListener(l);
			i.remove();
		}
	}

	public void printRefsDebugInfo() {
		for (PathwayObject elt : dataObjects) {
			elt.printRefsDebugInfo();
		}
	}

	List<OntologyTag> ontologyTags = new ArrayList<OntologyTag>();

	public void addOntologyTag(String id, String term, String ontology) {
		ontologyTags.add(new OntologyTag(id, term, ontology));
	}

	public List<OntologyTag> getOntologyTags() {
		return ontologyTags;
	}

	/**
	 * List of Biopax references to be deleted. The deletion is done before to save
	 * the pathway.
	 */
	private List<String> biopaxReferenceToDelete = new ArrayList<String>();
}
