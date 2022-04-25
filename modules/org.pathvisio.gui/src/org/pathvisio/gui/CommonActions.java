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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Globals;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.libgpml.model.type.ConnectorType;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.LineStyleType;
import org.pathvisio.libgpml.model.type.ArrowHeadType;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayModel.StatusFlagEvent;
import org.pathvisio.libgpml.model.PathwayModel.StatusFlagListener;
import org.pathvisio.libgpml.model.shape.ShapeType;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.core.util.Resources;
import org.pathvisio.core.util.Theme;
import org.pathvisio.core.view.LayoutType;
import org.pathvisio.core.view.model.DefaultTemplates;
import org.pathvisio.core.view.model.Handle;
import org.pathvisio.core.view.model.SelectionBox;
import org.pathvisio.core.view.model.Template;
import org.pathvisio.core.view.model.VElement;
import org.pathvisio.core.view.model.VLabel;
import org.pathvisio.core.view.model.VPathwayElement;
import org.pathvisio.core.view.model.VPathwayModel;
import org.pathvisio.core.view.model.ViewActions;
import org.pathvisio.gui.dialogs.AboutDlg;
import org.pathvisio.gui.dialogs.PathwayElementDialog;
import org.pathvisio.gui.dialogs.CitationDialog;

/**
 * A collection of {@link Action}s that may be used throughout the program (e.g.
 * in toolbars, menubars and right-click menu). These actions are registered to
 * the proper group in {@ViewActions} when a new {@link VPathwayModel} is
 * created.
 * 
 * @author thomas, finterly
 * @see {@link ViewActions}
 */
public class CommonActions implements ApplicationEventListener {

	private static final URL IMG_SAVE = Resources.getResourceURL("save.gif");
	private static final URL IMG_SAVEAS = Resources.getResourceURL("saveas.gif");
	private static final URL IMG_IMPORT = Resources.getResourceURL("import.gif");
	private static final URL IMG_EXPORT = Resources.getResourceURL("export.gif");

	public void applicationEvent(ApplicationEvent e) {
		if (e.getType() == ApplicationEvent.Type.VPATHWAY_CREATED) {
			ViewActions va = ((VPathwayModel) e.getSource()).getViewActions();
			va.registerToGroup(saveAction, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(saveAsAction, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(importAction, ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(exportAction, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(copyAction, ViewActions.GROUP_ENABLE_WHEN_SELECTION);
			va.registerToGroup(pasteAction, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(pasteAction, ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(applyThemeActions, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED); // TODO
			va.registerToGroup(zoomActions, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(layoutActions, ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(layoutActions, ViewActions.GROUP_ENABLE_WHEN_SELECTION);

			va.resetGroupStates();
		}
	}

	public final AboutAction aboutAction;
	public final SaveAction saveAction;
	public final SaveAction saveAsAction;
	public final SaveAction standaloneSaveAction;
	public final SaveAction standaloneSaveAsAction;

	public final Action importAction;
	public final Action exportAction;

	public final ViewActions.UndoAction undoAction;
	public final Action copyAction;
	public final Action pasteAction;
	public final Action[] applyThemeActions; // TODO
	public final Action showUnlinkedAction;

	public final Action exitAction;

	public final Action[] zoomActions;

	public final Action[] layoutActions;

	// Objects Side Panel
	public final Action[] newMoleculeDatanodeActions;
	public final Action[] newConceptDatanodeActions;
	public final Action[] newInteractionPanelActions;
	public final Action[] newRLInteractionActions; // TODO
	public final Action[] newLabelActions;
	public final Action[] newShapeActions;
	public final Action[] newCellularComponentActions;
	public final Action[] newMiscShapeActions;

	public final Action[] newTemplateActions;

	private final SwingEngine swingEngine;

	public CommonActions(SwingEngine se) {
		swingEngine = se;
		Engine e = se.getEngine();
		e.addApplicationEventListener(this);

		// ================================================================================
		// Actions (Top Action Bar)
		// ================================================================================
		saveAction = new SaveAction(se, true, false);
		saveAsAction = new SaveAction(se, true, true);
		standaloneSaveAction = new SaveAction(se, false, false);
		standaloneSaveAsAction = new SaveAction(se, false, true);

		undoAction = new ViewActions.UndoAction(se.getEngine());
		copyAction = new ViewActions.CopyAction(se.getEngine());
		pasteAction = new ViewActions.PasteAction(se.getEngine());
		applyThemeActions = new Action[] { new ApplyThemeAction(se.getEngine(), Theme.WIKIPATHWAYS),
				new ApplyThemeAction(se.getEngine(), Theme.WIKIPATHWAYS_MIN) }; // TODO
		showUnlinkedAction = new ViewActions.ShowUnlinkedAction(se.getEngine());

		exportAction = new ExportAction(se);
		importAction = new ImportAction(se);
		aboutAction = new AboutAction(se);

		exitAction = new ExitAction(se);

		// ================================================================================
		// Zoom (Top Action Bar)
		// ================================================================================
		zoomActions = new Action[] { new ZoomToFitAction(e), new ZoomAction(e, 10), new ZoomAction(e, 25),
				new ZoomAction(e, 50), new ZoomAction(e, 75), new ZoomAction(e, 100), new ZoomAction(e, 150),
				new ZoomAction(e, 200), new ZoomAction(e, 400) };

		// ================================================================================
		// Layout (Top Action Bar)
		// ================================================================================
		layoutActions = new Action[] { new LayoutAction(e, LayoutType.ALIGN_CENTERX),
				new LayoutAction(e, LayoutType.ALIGN_CENTERY),
//					new LayoutAction(e, LayoutType.ALIGN_LEFT),
//					new LayoutAction(e, LayoutType.ALIGN_RIGHT),
//					new LayoutAction(e, LayoutType.ALIGN_TOP),
//					new LayoutAction(e, LayoutType.ALIGN_BOTTOM),
				new LayoutAction(e, LayoutType.COMMON_WIDTH), new LayoutAction(e, LayoutType.COMMON_HEIGHT),

				new LayoutAction(e, LayoutType.STACK_CENTERX), new LayoutAction(e, LayoutType.STACK_CENTERY),
//					new LayoutAction(e, LayoutType.STACK_LEFT),
//					new LayoutAction(e, LayoutType.STACK_RIGHT),
//					new LayoutAction(e, LayoutType.STACK_TOP),
//					new LayoutAction(e, LayoutType.STACK_BOTTOM)
		};

		// ================================================================================
		// New Molecule DataNode Actions
		// ================================================================================
		newMoleculeDatanodeActions = new Action[] {
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.GENEPRODUCT)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.METABOLITE)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.PROTEIN)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.DNA)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.RNA)), };

		// ================================================================================
		// New Concept DataNode Actions
		// ================================================================================
		newConceptDatanodeActions = new Action[] {
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.PATHWAY)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.DISEASE)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.PHENOTYPE)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.ALIAS)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.EVENT)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.CELL_NODE)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.ORGAN)),
				new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.UNDEFINED)), };

		// ================================================================================
		// New Label Actions
		// ================================================================================
		newLabelActions = new Action[] { new NewElementAction(e, new DefaultTemplates.LabelTemplate()), };

		// ================================================================================
		// New Basic Shapes Actions
		// ================================================================================
		newShapeActions = new Action[] { new NewElementAction(e, new DefaultTemplates.LabelTemplate()),
				new NewElementAction(e, new DefaultTemplates.GraphicalLineTemplate("Undirected", LineStyleType.SOLID,
						ArrowHeadType.UNDIRECTED, ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT)),
//				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.EDGE)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ARC)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.BRACE)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.OVAL)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ROUNDED_RECTANGLE)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.TRIANGLE)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.RECTANGLE)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.PENTAGON)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.HEXAGON)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.OCTAGON)),

//				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(MIMShapes.MIM_DEGRADATION_SHAPE)), 
		};

		// actions for "Receptor/ligand interactions" section
		newRLInteractionActions = new Action[] {
//				new NewElementAction(e,
//						new DefaultTemplates.InteractionTemplate("ligandround", LineStyleType.SOLID,
//								ArrowHeadType.UNDIRECTED, ArrowHeadType.LIGAND_ROUND, ConnectorType.STRAIGHT)),
//				new NewElementAction(e,
//						new DefaultTemplates.InteractionTemplate("ligandsquare", LineStyleType.SOLID,
//								ArrowHeadType.UNDIRECTED, ArrowHeadType.LIGAND_SQUARE, ConnectorType.STRAIGHT)),
//				new NewElementAction(e,
//						new DefaultTemplates.InteractionTemplate("receptorround", LineStyleType.SOLID,
//								ArrowHeadType.UNDIRECTED, ArrowHeadType.RECEPTOR_ROUND, ConnectorType.STRAIGHT)),
//				new NewElementAction(e, new DefaultTemplates.InteractionTemplate("receptorsquare", LineStyleType.SOLID,
//						ArrowHeadType.UNDIRECTED, ArrowHeadType.RECEPTOR_SQUARE, ConnectorType.STRAIGHT)),
		};

		// ================================================================================
		// New Interaction Panel Actions
		// ================================================================================
		newInteractionPanelActions = new Action[] {
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Undirected", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Directed", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.DIRECTED, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Conversion", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.CONVERSION, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Inhibition", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.INHIBITION, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Catalysis", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.CATALYSIS, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Stimulation", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.STIMULATION, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Binding", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.BINDING, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Translocation", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.TRANSLOCATION, ConnectorType.STRAIGHT)),
				new NewElementAction(e,
						new DefaultTemplates.InteractionTemplate("Transcription-translation", LineStyleType.SOLID,
								ArrowHeadType.UNDIRECTED, ArrowHeadType.TRANSCRIPTION_TRANSLATION,
								ConnectorType.STRAIGHT)), };

		// ================================================================================
		// New Cellular Compartment Shape Actions
		// ================================================================================
		newCellularComponentActions = new Action[] {
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.CELL)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.NUCLEUS)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ENDOPLASMIC_RETICULUM)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.GOLGI_APPARATUS)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.MITOCHONDRIA)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.SARCOPLASMIC_RETICULUM)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ORGANELLE)),
				// new NewElementAction(e, new
				// DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL,
				// CellularComponentType.LYSOSOME)),
				// new NewElementAction(e, new
				// DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL,
				// CellularComponentType.NUCLEOLUS)),
				// new NewElementAction(e, new
				// DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL,
				// CellularComponentType.VACUOLE)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.VESICLE)),
				// new NewElementAction(e, new
				// DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE,
				// CellularComponentType.CYTOSOL)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.EXTRACELLULAR)),
				// new NewElementAction(e, new
				// DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE,
				// CellularComponentType.MEMBRANE))
		};

		// ================================================================================
		// New Miscellaneous Shapes Actions
		// ================================================================================
		newMiscShapeActions = new Action[] {
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.CORONAVIRUS_ICON)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.DNA_ICON)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.RNA_ICON)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.CELL_ICON)),
				new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.MEMBRANE_ICON)), };

		// ================================================================================
		// New DataNode-Interaction Template Actions
		// ================================================================================
		newTemplateActions = new Action[] {
				new NewElementAction(e, new DefaultTemplates.InhibitionInteractionTemplate()),
				new NewElementAction(e, new DefaultTemplates.StimulationInteractionTemplate()),
				new NewElementAction(e, new DefaultTemplates.ReactionTemplate()),
				new NewElementAction(e, new DefaultTemplates.PhosphorylationTemplate()),
				new NewElementAction(e, new DefaultTemplates.ReversibleReactionTemplate()), };
	}

	/**
	 * Applies theme (color) to the current pathway.
	 * 
	 * @author finterly
	 */
	public static class ApplyThemeAction extends AbstractAction {
		Engine engine;
		Theme theme = null;

		public ApplyThemeAction(Engine engine, Theme theme) {
			super();
			this.engine = engine;
			this.theme = theme;
			putValue(NAME, theme.getName());
			putValue(SHORT_DESCRIPTION, "Apply " + theme.getDescription());
			// engine.addApplicationEventListener(this);
			// setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			PathwayModel p = engine.getActivePathwayModel();
			if (p != null) {
				engine.getActiveVPathwayModel().getUndoManager().newAction("Apply Theme"); // TODO
				theme.colorPathwayModel(p);
				engine.getActiveVPathwayModel().redraw();
				int n = JOptionPane.showConfirmDialog(null,
						"Warning: Applying theme will overwrite pathway model\ngraphics properties. Are you sure you want to continue?",
						"Message", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) { // yes
					JOptionPane.showConfirmDialog(null, theme.getName() + " theme applied.", "Message",
							JOptionPane.PLAIN_MESSAGE);
				}
				if (n == JOptionPane.NO_OPTION) { // no
					engine.getActiveVPathwayModel().undo();
					JOptionPane.showConfirmDialog(null, "Theme not applied.", "Message", JOptionPane.PLAIN_MESSAGE);
				}
			}
		}
	}

	/**
	 * When triggered, zoom percentage is set so that the entire pathway fits in the
	 * view
	 * 
	 * @author unknown
	 */
	public static class ZoomToFitAction extends AbstractAction {

		Component parent;
		Engine engine;

		public ZoomToFitAction(Engine engine) {
			super();
			this.engine = engine;
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, "Make the pathway fit in the window");
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vPathway = engine.getActiveVPathwayModel();
			if (vPathway != null) {
				double zoomFactor = vPathway.getFitZoomFactor();
				vPathway.setPctZoom(zoomFactor);
			}
		}

		public String toString() {
			return "Fit to window";
		}
	}

	/**
	 * Zooms the view to a fixed percentage. The zoom percentage is decided at
	 * creation time
	 * 
	 * @author unknown
	 */
	public static class ZoomAction extends AbstractAction {

		Component parent;
		double zoomFactor;

		Engine engine;

		public ZoomAction(Engine e, double zf) {
			super();
			this.engine = e;
			zoomFactor = zf;
			String descr = "Set zoom to " + (int) zf + "%";
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, descr);
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vPathway = engine.getActiveVPathwayModel();
			if (vPathway != null) {
				vPathway.centeredZoom(zoomFactor);
			}
		}

		public String toString() {
			return (int) zoomFactor + "%";
		}
	}

	/**
	 * This action constitutes both the save and save as menu items, and can save
	 * both to the wiki in the case of the applet, or to file in the case of the
	 * standalone application
	 * 
	 * @author unknown
	 */
	public static class SaveAction extends AbstractAction implements StatusFlagListener, ApplicationEventListener {
		boolean isSaveAs; // is either save... or save as...

		SwingEngine swingEngine;

		public SaveAction(SwingEngine swingEngine, boolean wiki, boolean isSaveAs) {
			super();
			this.isSaveAs = isSaveAs;
			this.swingEngine = swingEngine;
			if (isSaveAs) {
				putValue(Action.NAME, "Save as");
				putValue(Action.SMALL_ICON, new ImageIcon(IMG_SAVEAS));
				putValue(Action.SHORT_DESCRIPTION,
						wiki ? "Save the pathway under a new name" : "Save a local copy of the pathway");
				putValue(Action.LONG_DESCRIPTION,
						wiki ? "Save the pathway under a new name" : "Save a local copy of the pathway");
			} else {
				putValue(Action.NAME, "Save");
				putValue(Action.SMALL_ICON, new ImageIcon(IMG_SAVE));
				putValue(Action.SHORT_DESCRIPTION, wiki ? "Save a local copy of the pathway" : "Save the pathway");
				putValue(Action.LONG_DESCRIPTION, wiki ? "Save a local copy of the pathway" : "Save the pathway");
				putValue(Action.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			}
			swingEngine.getEngine().addApplicationEventListener(this);
			PathwayModel p = swingEngine.getEngine().getActivePathwayModel();
			if (p != null) {
				p.addStatusFlagListener(this);
				handleStatus(p.hasChanged());
			} else {
				setEnabled(false);
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (isSaveAs)
				swingEngine.savePathwayModelAs();
			else
				swingEngine.savePathwayModel();
		}

		private void handleStatus(boolean status) {
			if (isSaveAs) {
				setEnabled(true);
			} else {
				setEnabled(status);
			}
		}

		public void statusFlagChanged(StatusFlagEvent e) {
			handleStatus(e.getNewStatus());
		}

		public void applicationEvent(ApplicationEvent e) {
			switch (e.getType()) {
			case PATHWAY_NEW:
			case PATHWAY_OPENED:
				PathwayModel p = swingEngine.getEngine().getActivePathwayModel();
				p.addStatusFlagListener(this);
				handleStatus(p.hasChanged());
				break;
			}
		}
	}

	/**
	 * Imports a Pathway from a different format than GPML, usually that means
	 * GenMAPP format.
	 * 
	 * @author unknown
	 */
	public static class ImportAction extends AbstractAction {

		SwingEngine swingEngine;

		public ImportAction(SwingEngine se) {
			super();
			this.swingEngine = se;
			putValue(NAME, "Import");
			putValue(SMALL_ICON, new ImageIcon(IMG_IMPORT));
			putValue(Action.SHORT_DESCRIPTION, "Import pathway from a file on your computer");
			putValue(Action.LONG_DESCRIPTION, "Import a pathway from various file formats on your computer");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			if (swingEngine.canDiscardPathwayModel()) {
				swingEngine.importPathwayModel();
			}
		}
	}

	/**
	 * Exports a pathway to a different pathway, raster or vector image format
	 * 
	 * @author unknown
	 */
	public static class ExportAction extends AbstractAction {

		SwingEngine swingEngine;

		public ExportAction(SwingEngine swingEngine) {
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Export");
			putValue(SMALL_ICON, new ImageIcon(IMG_EXPORT));
			putValue(SHORT_DESCRIPTION, "Export pathway to a file on your computer");
			putValue(LONG_DESCRIPTION, "Export the pathway to various file formats on your computer");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			swingEngine.exportPathwayModel();
		}

		public void setEnabled(boolean newValue) {
			super.setEnabled(newValue);
		}
	}

	/**
	 * Creates a new pathway element or elements based on a {@link Template}
	 * 
	 * @author unknown
	 */
	public static class NewElementAction extends AbstractAction {
		Template template;

		Engine engine;

		public NewElementAction(Engine engine, Template template) {
			this.template = template;
			this.engine = engine;
			putValue(Action.NAME, template.getName());
			putValue(Action.SHORT_DESCRIPTION, template.getDescription());
			putValue(Action.LONG_DESCRIPTION, template.getDescription());
			if (template.getIconLocation() != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(template.getIconLocation()));
			}
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null) {
//				vp.addVPathwayListener(this);
				vp.setNewTemplate(template);
			}
		}

//		public void vPathwayEvent(VPathwayEvent e) {
//			if(e.getType() == VPathwayEvent.ELEMENT_ADDED) {
//				e.getVPathway().setNewTemplate(null);
//			}
//		}

		@Override
		public String toString() {
			return template.getName();
		}
	}

	/**
	 * Perform simple layout operations such as aligning, setting common size and
	 * distributing evenly. Note that this doesn't include graph layout algorithms.
	 * see {@link LayoutType} for a list of possible layouts
	 * 
	 * @author unknown
	 */
	public static class LayoutAction extends AbstractAction {
		LayoutType type;

		Engine engine;

		public LayoutAction(Engine engine, LayoutType t) {
			super();
			this.engine = engine;
			putValue(NAME, t.getLabel());
			putValue(SMALL_ICON, new ImageIcon(Resources.getResourceURL(t.getIcon())));
			putValue(SHORT_DESCRIPTION, t.getDescription());
			type = t;
		}

		public void actionPerformed(ActionEvent e) {
			VPathwayModel vp = engine.getActiveVPathwayModel();
			if (vp != null)
				vp.layoutSelected(type);
		}
	}

	/**
	 * This is an abstract base class for actions that are triggered from the
	 * right-click menu on a PathwayElement. When the action is triggered, the
	 * PathwayElementDialog is shown, but which tab is shown depends on the
	 * implementation of getSelectedPanel
	 * 
	 * @author unknown
	 */
	private static abstract class PathwayElementDialogAction extends AbstractAction {
		// TODO: use parameterization instead of inheritance to create different
		// PathwayElementDialogActions
		// inheritance is overkill because behaviour of classes is not changed
		VElement element;
		Component parent;

		SwingEngine swingEngine;

		public PathwayElementDialogAction(SwingEngine swingEngine, Component parent, VElement e) {
			super();
			this.parent = parent;
			this.swingEngine = swingEngine;
			element = e;
			// If the element is an empty selectionbox,
			// the an empty space on the drawing is clicked
			// Set element to mappinfo so the pathway properties
			// will show up
			if (element instanceof SelectionBox) {
				SelectionBox s = (SelectionBox) element;
				if (s.getSelection().size() == 0) {
					element = element.getDrawing().getVInfoBox();
				}
			}
			// If handle, select parent
			if (element instanceof Handle) {
				element = ((Handle) element).getParent();
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (element instanceof VPathwayElement) {
				PathwayElement p = ((VPathwayElement) element).getPathwayObject();
				PathwayElementDialog pd = swingEngine.getPopupDialogHandler().getInstance(p,
						!element.getDrawing().isEditMode(), null, parent);
				if (pd != null) {
					pd.selectPathwayElementPanel(getSelectedPanel());
					pd.setVisible(true);
				}
			}
		}

		/**
		 * implement this to determine which tab is selected first when the dialog is
		 * shown
		 */
		protected abstract String getSelectedPanel();
	}

	/**
	 * Provides direct access to the citation reference dialog
	 * ({@link CitationDialog}) from the right click menu.
	 * 
	 * @author unknown, finterly
	 */
	public static class AddCitationAction extends PathwayElementDialogAction {
		public AddCitationAction(SwingEngine swingEngine, Component parent, VElement e) {
			super(swingEngine, parent, e);
			putValue(NAME, "Add Citation");
			putValue(SHORT_DESCRIPTION, "Add a Citation to this element");
			setEnabled(e.getDrawing().isEditMode());
		}

		public void actionPerformed(ActionEvent e) {
			if (element instanceof VPathwayElement) {
				PathwayElement pwElm = ((VPathwayElement) element).getPathwayObject();
				CitationDialog d = new CitationDialog(pwElm, null, null, parent);
				d.setVisible(true);
				if (d.getExitCode().equals(CitationDialog.OK)) {
					// citationRef was added
				} else {
					// nothing
				}
			}
		}

		protected String getSelectedPanel() {
			return null;
		}
	}

	/**
	 * Pops up the pathway element dialog directly on the citations tab.
	 * 
	 * @author unknown, finterly
	 */
	public static class EditCitationAction extends PathwayElementDialogAction {

		public EditCitationAction(SwingEngine swingEngine, Component parent, VElement e) {
			super(swingEngine, parent, e);
			putValue(NAME, "Edit Citations");
			putValue(SHORT_DESCRIPTION, "Edit the Citations of this element");
			setEnabled(e.getDrawing().isEditMode());
		}

		protected String getSelectedPanel() {
			return PathwayElementDialog.TAB_CITATIONS;
		}
	}

	/**
	 * Adds action to Label hyperlink (href).
	 * 
	 * @author unknown
	 */
	public static class AddHrefAction extends AbstractAction {

		SwingEngine se;
		VElement vpe;

		public AddHrefAction(VElement selected, SwingEngine engine) {
			super("Hyperlink");
			se = engine;
			vpe = selected;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (vpe instanceof VLabel) {
				Label pe = ((VLabel) vpe).getPathwayObject();
				String currentHref = pe.getHref();
				String userInput = JOptionPane.showInputDialog(se.getFrame(), "Label hyperlink", currentHref);
				if (userInput != null) {
					try {
						new URL(userInput);
						pe.setHref(userInput);
					} catch (MalformedURLException e) {
						se.handleMalformedURLException("The specified link address is not valid", se.getFrame(), e);
					}
				}
			}
		}
	}

	/**
	 * Pops up the pathway element dialog directly on the comments tab
	 * 
	 * @author unknown
	 */
	public static class PropertiesAction extends PathwayElementDialogAction {

		public PropertiesAction(SwingEngine swingEngine, Component parent, VElement e) {
			super(swingEngine, parent, e);
			putValue(NAME, "Properties");
			putValue(SHORT_DESCRIPTION, "View this element's properties");
		}

		protected String getSelectedPanel() {
			return PathwayElementDialog.TAB_COMMENTS;
		}
	}

	/**
	 * Pops up the @{link AboutDlg}.
	 * 
	 * @author unknown
	 */
	public static class AboutAction extends AbstractAction {

		SwingEngine swingengine;

		public AboutAction(SwingEngine swingengine) {
			super();
			this.swingengine = swingengine;
			putValue(NAME, "About");
			putValue(SHORT_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
			putValue(LONG_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
		}

		public void actionPerformed(ActionEvent e) {
			AboutDlg dlg = new AboutDlg(swingengine);
			dlg.createAndShowGUI();
		}
	}

	/**
	 * Exit menu item. Quit the program with System.exit after checking for unsaved
	 * changes
	 * 
	 * @author unknown
	 */
	public static class ExitAction extends AbstractAction {

		SwingEngine swingEngine;

		public ExitAction(SwingEngine swingEngine) {
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Exit pathvisio");
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
			if (swingEngine.canDiscardPathwayModel()) {
				swingEngine.getFrame().dispose();
			}
		}
	}
}
