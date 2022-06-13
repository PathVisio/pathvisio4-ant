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
package org.pathvisio.core.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.pathvisio.core.Engine;
import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.io.ConverterException;
import org.pathvisio.libgpml.model.GPMLFormat;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayObject;
import org.pathvisio.libgpml.model.connector.ConnectorRestrictions;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.Group;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.CopyElement;
import org.pathvisio.libgpml.model.DataNode;
import org.pathvisio.libgpml.model.GraphLink.LinkableFrom;
import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.model.Groupable;
import org.pathvisio.libgpml.model.LineElement;
import org.pathvisio.libgpml.model.LineElement.Anchor;
import org.pathvisio.libgpml.model.LineElement.LinePoint;
import org.pathvisio.libgpml.model.type.DataNodeType;
import org.pathvisio.libgpml.model.type.ObjectType;

/**
 * This class helps transfer Pathways or bits of pathway over the clipboard.
 * 
 * @author unknown
 */
public class PathwayModelTransferable implements Transferable {
	public static final String INFO_DATASOURCE = "COPIED";

	/**
	 * DataFlavor used for transferring raw GPML code. Mimetype is 'text/xml'. Note
	 * that the equals method of DataFlavor only checks for the main mimetype
	 * ('text'), so returns true for any DataFlavor that stores text, not only xml.
	 */
	public static final DataFlavor GPML_DATA_FLAVOR = new DataFlavor(String.class, "text/xml");

	List<CopyElement> elements;
	PathwayModel pathwayModel;

	public PathwayModelTransferable(List<CopyElement> elements) {
		this(null, elements);
	}

	public PathwayModelTransferable(PathwayModel source, List<CopyElement> elements) {
		this.elements = elements;
		if (source == null) {
			source = new PathwayModel();
		}
		this.pathwayModel = source;
	}

	/**
	 *
	 */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		Object out = null;

		XMLOutputter xmlout = new XMLOutputter(Format.getPrettyFormat());

		PathwayModel pnew = new PathwayModel();

		boolean infoFound = false;
		for (CopyElement c : elements) {
			PathwayObject e = c.getNewElement();
			if (e.getClass() == Pathway.class) {
				infoFound = true;
			}
		}

		// Map stores pathway object copy information.
		BidiMap<PathwayObject, PathwayObject> newerToSource = new DualHashBidiMap<>();

		for (CopyElement copyElement : elements) {
			PathwayElement newElement = copyElement.getNewElement();
			PathwayElement srcElement = copyElement.getSourceElement();
			CopyElement copyOfCopyElement = newElement.copy();
			PathwayElement newerElement = copyOfCopyElement.getNewElement();
			pnew.add(newerElement);
			// load references
			newerElement.copyReferencesFrom(srcElement);
			// store information
			newerToSource.put(newerElement, srcElement);
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
					if (srcAliasRef != null) {
						Group newerAliasRef = (Group) newerToSource.getKey(srcAliasRef);
						// if group aliasRef was also copied
						if (newerAliasRef != null) {
							((DataNode) newerElement).setAliasRef(newerAliasRef);
						}
					}
					// otherwise aliasRef is not linked to any group
					else {
						System.out.println("Alias DataNode unlinked to group");
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
		for (LineElement o : pnew.getLineElements()) {
			o.getConnectorShape().recalculateShape(o);
		}
		// If no mappinfo, create a dummy one that we can recognize later on TODO
		if (!infoFound) {
			Pathway info = new Pathway();
			info.setSource(INFO_DATASOURCE);
			pnew.add(info);
		}

		try {
			Document doc = new GPMLFormat(GPMLFormat.GPML2021).createJdom(pnew);
			out = xmlout.outputString(doc);
		} catch (Exception e) {
			Logger.log.error("Unable to copy to clipboard", e);
		}

		return out;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.stringFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return GPML_DATA_FLAVOR.equals(flavor);
	}

	/**
	 * Gets the file url from the transferable if available. If the transferable
	 * contains a file list, the url of the first file in that list is returned.
	 * 
	 * @param t
	 * @return The {@link URL} of the file, or null if no file is available
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public static URL getFileURL(Transferable t) throws UnsupportedFlavorException, IOException {
		// Find out if there is a javaFileListFlavor, since that's the preferred type
		DataFlavor fallback = null;

		for (DataFlavor df : t.getTransferDataFlavors()) {
			if (DataFlavor.javaFileListFlavor.equals(df)) {
				// Return the first element of the file list
				return ((List<File>) t.getTransferData(df)).get(0).toURI().toURL();
			}
			// Gnome fix:
			// Check for text/uri-list mime type, an uri list separated by \n
			if (String.class.equals(df.getRepresentationClass())) {
				if ("uri-list".equalsIgnoreCase(df.getSubType())) {
					fallback = df;
				}
			}
		}
		if (fallback != null) {
			String uriList = (String) t.getTransferData(fallback);
			// Try if this is really an URL (needed, because in windows
			// raw text also gets the 'text/uri-list' mimetype (why???)
			try {
				return new URL(uriList.substring(0, uriList.indexOf("\n") - 1));
			} catch (MalformedURLException e) {
				// Not an url after all...
			}
		}
		return null;
	}

	/**
	 * Get the text in the transferable if available. There is no guarantee that the
	 * text is xml code!
	 * 
	 * @param t
	 * @return
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public static String getText(Transferable t) throws UnsupportedFlavorException, IOException {
		for (DataFlavor df : t.getTransferDataFlavors()) {
			if (DataFlavor.stringFlavor.equals(df)) {
				// Make sure this is not the gnome's uri-list
				if (!"uri-list".equalsIgnoreCase(df.getSubType())) {
					return (String) t.getTransferData(df);
				}
			}
		}
		return null;
	}

	/**
	 * Creates a pathway from the data in the provided {@link Transferable}.
	 * 
	 * @param t
	 * @return
	 * @throws ConverterException
	 * @throws MalformedURLException
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public static PathwayModel pathwayFromTransferable(Transferable t)
			throws ConverterException, MalformedURLException, UnsupportedFlavorException, IOException {
		PathwayModel pnew = new PathwayModel();
		String xml = getText(t);
		if (xml != null) {
			GPMLFormat.readFromXml(pnew, new StringReader(xml), true);

			List<PathwayObject> elements = new ArrayList<PathwayObject>();
			for (PathwayObject elm : pnew.getPathwayObjects()) { // TODO pathway object or element?
				if (elm.getClass() != Pathway.class) {
					elements.add(elm);
				} else {
					// Only add mappinfo if it's not generated by the transferable
					String source = ((Pathway) elm).getSource();
					if (!PathwayModelTransferable.INFO_DATASOURCE.equals(source)) {
						elements.add(elm);
					}
				}
			}
			return pnew;
		}

		URL url = getFileURL(t);
		if (url != null) {
			File file = new File(url.getFile());
			pnew.readFromXml(file, true);
			return pnew;
		}
		return null;
	}

	/**
	 * Opens a new pathway from the data in the {@link Transferable}, using the
	 * provided {@link Engine}. If the {@link Transferable} contains a link to a
	 * file, the pathway in this file will be opened. If the {@link Transferable}
	 * contains gpml code, a new pathway will be created, and the gpml will be
	 * loaded into this pathway.
	 * 
	 * @param t
	 * @param engine
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 * @throws ConverterException
	 */
	public static void openPathwayFromTransferable(Transferable t, Engine engine)
			throws UnsupportedFlavorException, IOException, ConverterException {
		URL url = getFileURL(t);
		if (url != null) {
			engine.openPathwayModel(url);
		}

		String xml = getText(t);
		if (xml != null) {
			engine.newPathwayModel();
			engine.getActivePathwayModel().readFromXml(new StringReader(xml), true);
		}
	}
}
