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

import java.awt.Component;
import java.awt.Frame;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.DataNode.State;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.Interaction;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.Shape;
import org.pathvisio.gui.SwingEngine;

/**
 * This is a factory class for the PathwayElement Popup dialog, which pops up
 * after double-clicking an element in the pathway. A dialog is constructed
 * depending on the type of the element that was clicked.
 * <p>
 * It is possible to add hooks to this handler, so that plugins can register new
 * panels to be added to PathwayElement Popup dialogs.
 * 
 * @author unknown, finterly
 */
public class PopupDialogHandler {
	final private SwingEngine swingEngine;

	public PopupDialogHandler(SwingEngine swingEngine) {
		this.swingEngine = swingEngine;
	}

	/**
	 * Implement this interface if you want to add a hook to the handler.
	 */
	public interface PopupDialogHook {
		/**
		 * This method is called just before the PathwayElementDialog is shown.
		 * 
		 * @param e   the element which will be edited
		 * @param dlg A partially constructed dialog, which may be modified by the hook.
		 */
		void popupDialogHook(PathwayElement e, PathwayElementDialog dlg);
	}

	private Set<PopupDialogHook> hooks = new HashSet<PopupDialogHook>();

	/**
	 * Registers a new hook.
	 * 
	 * @param hook the hook.
	 */
	public void addHook(PopupDialogHook hook) {
		hooks.add(hook);
	}

	/**
	 * Removes the given hook.
	 * 
	 * @param hook the hook.
	 */
	public void removeHook(PopupDialogHook hook) {
		hooks.remove(hook);
	}

	/**
	 * Create a dialog for the given pathway element.
	 * 
	 * @param e        The pathway object
	 * @param readonly Whether the dialog should be read-only or not
	 * @return An instance of a subclass of PathwayElementDialog (depends on the
	 *         type attribute of the given PathwayElement, e.g. type DATANODE
	 *         returns a DataNodeDialog
	 */
	public PathwayElementDialog getInstance(PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		// TODO pathway element or pathway object?
		PathwayElementDialog result = null;
		switch (e.getObjectType()) {
		case PATHWAY:
			result = new PathwayDialog(swingEngine, (Pathway) e, readonly, frame, "Properties", locationComp);
			break;
		case DATANODE:
			result = new DataNodeDialog(swingEngine, (DataNode) e, readonly, frame, locationComp);
			break;
		case STATE:
			result = new StateDialog(swingEngine, (State) e, readonly, frame, locationComp);
			break;
		case INTERACTION:
			result = new InteractionDialog(swingEngine, (Interaction) e, readonly, frame, locationComp);
			break;
		case LABEL: 
			result = new LabelDialog(swingEngine, (Label) e, readonly, frame, locationComp);
			break;
		case SHAPE:
			result = new ShapeDialog(swingEngine, (Shape) e, readonly, frame, locationComp);
			break;
		case GROUP:
			result = new GroupDialog(swingEngine, (Group) e, readonly, frame, locationComp);
			break;
		default:
			result = new PathwayElementDialog(swingEngine, e, readonly, frame, e.getObjectType().getTag() + " properties", locationComp);
		}
		for (PopupDialogHook hook : hooks) {
			hook.popupDialogHook(e, result);
		}
		result.refresh();
		return result;
	}
}
