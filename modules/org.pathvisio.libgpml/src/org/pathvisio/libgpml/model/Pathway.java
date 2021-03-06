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

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bridgedb.Xref;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.prop.StaticProperty;
import org.pathvisio.libgpml.util.Utils;
import org.pathvisio.libgpml.util.XrefUtils;

/**
 * The Pathway class stores information or metadata for a Pathway Model.
 * <p>
 * NB:
 * <ol>
 * <li>Pathway is treated as a {@link PathwayElement} for simplicity of
 * listeners and implementation.
 * <li>There can only be one Pathway per a PathwayModel.
 * <li>A Pathway references its PathwayModel, but has no elementId (null).
 * </ol>
 *
 * @author finterly
 */
public class Pathway extends PathwayElement implements Xrefable {
	// required properties
	private String title;
	private double boardWidth;
	private double boardHeight;
	private Color backgroundColor;
	private List<Author> authors;
	// optional properties
	private String description;
	private String organism;
	private String source;
	private String version;
	private String license;
	private Xref xref;

	// ================================================================================
	// Constructors
	// ================================================================================

	/**
	 * Instantiates a pathway with default values.
	 */
	public Pathway() {
		this.title = "Click to add title";
		this.boardWidth = 0;
		this.boardHeight = 0;
		this.backgroundColor = Color.decode("#ffffff");
		this.authors = new ArrayList<Author>();
	}

	// ================================================================================
	// Accessors
	// ================================================================================
	/**
	 * Returns the object type of this pathway element.
	 *
	 * @return the object type.
	 */
	@Override
	public ObjectType getObjectType() {
		return ObjectType.PATHWAY;
	}

	/**
	 * Returns the title or name of this pathway.
	 *
	 * @return title the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title or name of this pathway.
	 *
	 * @param v the title to set.
	 */
	public void setTitle(String v) {
		if (v == null) {
			throw new IllegalArgumentException();
		}
		if (title != v) {
			title = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.TITLE));
		}
	}

	/**
	 * Returns the board width. Board width together with board height define
	 * drawing size.
	 *
	 * @return boardWidth the board width
	 */
	public double getBoardWidth() {
		return boardWidth;
	}

	/**
	 * Sets the board width.
	 *
	 * @param v the board width to set.
	 */
	public void setBoardWidth(double v) {
		if (v < 0) {
			throw new IllegalArgumentException("Tried to set dimension < 0: " + v);
		} else {
			boardWidth = v;
		}
	}

	/**
	 * Returns the board height. Board width together with board height define
	 * drawing size.
	 *
	 * @return boardHeight the board height
	 */
	public double getBoardHeight() {
		return boardHeight;
	}

	/**
	 * Sets the board height.
	 *
	 * @param v the board height to set.
	 */
	public void setBoardHeight(double v) {
		if (v < 0) {
			throw new IllegalArgumentException("Tried to set dimension < 0: " + v);
		} else {
			boardHeight = v;
		}
	}

	/**
	 * Returns the background color of this pathway.
	 *
	 * @return backgroundColor the background color.
	 */
	public Color getBackgroundColor() {
		if (backgroundColor == null) {
			this.backgroundColor = Color.decode("#ffffff");
		}
		return backgroundColor;
	}

	/**
	 * Sets the background color of this pathway.
	 *
	 * @param v the background color to set.
	 */
	public void setBackgroundColor(Color v) {
		backgroundColor = v;
	}

	/**
	 * Returns the list of authors for this pathway model.
	 *
	 * @return authors the list of authors.
	 */
	public List<Author> getAuthors() {
		return authors;
	}

	/**
	 * Adds the given author to authors list.
	 *
	 * @param author the author to add.
	 */
	public Author addAuthor(Author author) {
		if (author != null) {
			authors.add(author);
		}
		return author;
	}

	/**
	 * Creates and adds an author to authors list.
	 *
	 * @param name the name of author.
	 */
	public Author addAuthor(String name) {
		Author author = new Author(name);
		addAuthor(author);
		return author;
	}

	/**
	 * Removes the given author from authors list.
	 *
	 * @param author the author to remove.
	 */
	public void removeAuthor(Author author) {
		if (author != null && authors.contains(author)) {
			authors.remove(author);
		}
	}

	/**
	 * Returns the description of this pathway.
	 *
	 * @return description the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this pathway.
	 *
	 * @param v the description to set.
	 */
	public void setDescription(String v) {
		if (v != null) {
			description = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.DESCRIPTION));
		}
	}

	/**
	 * Returns the organism of this pathway. Organism is the scientific name (e.g.,
	 * Homo sapiens) of the species being described by this pathway.
	 *
	 * @return organism the organism.
	 */
	public String getOrganism() {
		return organism;
	}

	/**
	 * Sets the organism of this pathway. Organism is the scientific name (e.g.,
	 * Homo sapiens) of the species being described by this pathway.
	 *
	 * @param v the organism to set.
	 */
	public void setOrganism(String v) {
		if (!Utils.stringEquals(organism, v)) {
			organism = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.ORGANISM));
		}
	}

	/**
	 * Returns the source of this pathway, e.g. WikiPathways, KEGG, Cytoscape.
	 *
	 * @return source the source.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source of this pathway, e.g. WikiPathways, KEGG, Cytoscape.
	 *
	 * @param v the source to set.
	 */
	public void setSource(String v) {
		if (!Utils.stringEquals(source, v)) {
			source = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.SOURCE));
		}
	}

	/**
	 * Returns the version of this pathway.
	 *
	 * @return version the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version of this pathway.
	 *
	 * @param v the version to set.
	 */
	public void setVersion(String v) {
		if (!Utils.stringEquals(version, v)) {
			version = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.VERSION));
		}
	}

	/**
	 * Returns the license of this pathway.
	 *
	 * @return license the license.
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * Sets the license of this pathway.
	 *
	 * @param v the license to set.
	 */
	public void setLicense(String v) {
		if (!Utils.stringEquals(license, v)) {
			license = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.LICENSE));
		}
	}

	/**
	 * Returns the Xref for this pathway.
	 *
	 * @return xref the xref of this pathway.
	 */
	@Override
	public Xref getXref() {
		return xref;
	}

	/**
	 * Sets the Xref for this pathway.
	 *
	 * @param v the xref to set for this pathway.
	 */
	@Override
	public void setXref(Xref v) {
		if (v != null) {
			xref = v;
			fireObjectModifiedEvent(PathwayObjectEvent.createSinglePropertyEvent(this, StaticProperty.XREF));
		}
	}

	// ================================================================================
	// Copy Methods
	// ================================================================================
	/**
	 * Note: doesn't change parent, only fields
	 *
	 * Used by UndoAction.
	 *
	 * @param src
	 */
	public void copyValuesFrom(Pathway src) {
		super.copyValuesFrom(src);
		title = src.title;
		boardWidth = src.boardWidth;
		boardHeight = src.boardHeight;
		backgroundColor = src.backgroundColor;
		// copy authors
		for (Author c : src.authors) {
			try {
				addAuthor((Author) c.clone());
			} catch (CloneNotSupportedException e) {
				assert (false); // not going to happen
			}
		}
		description = src.description;
		organism = src.organism;
		source = src.source;
		version = src.version;
		license = src.license;
		xref = src.xref;
		fireObjectModifiedEvent(PathwayObjectEvent.createAllPropertiesEvent(this));
	}

	/**
	 * Copy Object. The object will not be part of the same Pathway object, it's
	 * parent will be set to null.
	 *
	 * No events will be sent to the parent of the original.
	 */
	@Override
	public CopyElement copy() {
		Pathway result = new Pathway();
		result.copyValuesFrom(this);
		return new CopyElement(result, this);
	}

	// ================================================================================
	// Property Methods
	// ================================================================================
	/**
	 * Returns all static properties for this pathway object.
	 *
	 * @return result the set of static property for this pathway object.
	 */
	@Override
	public Set<StaticProperty> getStaticPropertyKeys() {
		Set<StaticProperty> result = super.getStaticPropertyKeys();
		Set<StaticProperty> propsPathway = EnumSet.of(StaticProperty.TITLE, StaticProperty.ORGANISM,
				StaticProperty.DESCRIPTION, StaticProperty.SOURCE, StaticProperty.VERSION, StaticProperty.LICENSE,
				StaticProperty.AUTHOR, StaticProperty.XREF, StaticProperty.BOARDWIDTH, StaticProperty.BOARDHEIGHT,
				StaticProperty.BACKGROUNDCOLOR);
		result.addAll(propsPathway);
		return result;
	}

	/**
	 *
	 */
	@Override
	public Object getStaticProperty(StaticProperty key) {
		Object result = super.getStaticProperty(key);
		if (result == null) {
			switch (key) {
			case TITLE:
				result = getTitle();
				break;
			case ORGANISM:
				result = getOrganism();
				break;
			case DESCRIPTION:
				result = getDescription();
				break;
			case SOURCE:
				result = getSource();
				break;
			case VERSION:
				result = getVersion();
				break;
			case LICENSE:
				result = getLicense();
				break;
			case AUTHOR:
				result = getAuthors();
				break;
			case XREF:
				result = getXref();
				break;
			case BOARDWIDTH:
				result = getBoardWidth();
				break;
			case BOARDHEIGHT:
				result = getBoardHeight();
				break;
			case BACKGROUNDCOLOR:
				result = getBackgroundColor();
				break;
			default:
				// do nothing
			}
		}
		return result;
	}

	/**
	 * This works so that o.setNotes(x) is the equivalent of o.setProperty("Notes",
	 * x);
	 *
	 * Value may be null in some cases, e.g. graphRef
	 *
	 * @param key
	 * @param value
	 */
	@Override
	public void setStaticProperty(StaticProperty key, Object value) {
		super.setStaticProperty(key, value);
		switch (key) { 
		case TITLE:
			setTitle((String) value);
			break;
		case ORGANISM:
			setOrganism((String) value);
			break;
		case DESCRIPTION:
			setDescription((String) value);
			break;
		case SOURCE:
			setSource((String) value);
			break;
		case VERSION:
			setVersion((String) value);
			break;
		case LICENSE:
			setLicense((String) value);
			break;
		case AUTHOR:
			// do nothing TODO
			break;
		case XREF:
			setXref((Xref) value);
			break;
		case BOARDWIDTH:
			// ignore, board width is calculated automatically
			break;
		case BOARDHEIGHT:
			// ignore, board height is calculated automatically
			break;
		case BACKGROUNDCOLOR:
			setBackgroundColor((Color) value);
			break;
		default:
			// do nothing
		}
	}

	// ================================================================================
	// Author Class
	// ================================================================================
	/**
	 * This class stores information for an Author. An Author must have name and
	 * optionally username, order, and Xref.
	 *
	 * @author finterly
	 */
	public class Author implements Cloneable {

		private String name;
		private String username; // optional
		private int order;// optional
		private Xref xref;// optional

		// ================================================================================
		// Constructors
		// ================================================================================
		/**
		 * Instantiates an author with only required property. Use set methods for
		 * optional author properties. This private constructor is called by
		 * {@link #addAuthor(String name)}.
		 *
		 * @param name the author name.
		 */
		private Author(String name) {
			this.name = name;
		}

		// ================================================================================
		// Clone Methods
		// ================================================================================

		/**
		 * Clones this author.
		 */
		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		// ================================================================================
		// Accessors
		// ================================================================================
		/**
		 * Returns the name of this author.
		 *
		 * @return name the name of this author.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets the name of this author.
		 *
		 * @param v the name of this author.
		 */
		public void setName(String v) {
			if (!Utils.stringEquals(name, v)) {
				name = v;
				fireObjectModifiedEvent(
						PathwayObjectEvent.createSinglePropertyEvent(Pathway.this, StaticProperty.AUTHOR));
			}
		}

		/**
		 * Returns the username of this author.
		 *
		 * @return username the username of this author.
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * Sets the username of this author.
		 *
		 * @param v the username of this author.
		 */
		public void setUsername(String v) {
			if (!Utils.stringEquals(username, v)) {
				username = v;
				fireObjectModifiedEvent(
						PathwayObjectEvent.createSinglePropertyEvent(Pathway.this, StaticProperty.AUTHOR));
			}
		}

		/**
		 * Returns the authorship order of this author.
		 *
		 * @return order the authorship order.
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * Sets the authorship order of this author.
		 *
		 * @param v the authorship order.
		 */
		public void setOrder(int v) {
			if (order != v) {
				order = v;
				fireObjectModifiedEvent(
						PathwayObjectEvent.createSinglePropertyEvent(Pathway.this, StaticProperty.AUTHOR));
			}
		}

		/**
		 * Returns the Xref for the author.
		 *
		 * @return xref the xref of the author.
		 */
		public Xref getXref() {
			return xref;
		}

		/**
		 * Sets the Xref for the author.
		 *
		 * @param v the xref of the author.
		 */
		public void setXref(Xref v) {
			if (v != null) {
				xref = v;
				fireObjectModifiedEvent(
						PathwayObjectEvent.createSinglePropertyEvent(Pathway.this, StaticProperty.AUTHOR));
			}
		}

		/**
		 * Writes author out as a string.
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			// Order
			if (order != 0) {
				builder.append(order + ": ");
			}
			// Name
			if (name != null) {
				builder.append(name);
			}
			// Username
			if (username != null) {
				builder.append(" (" + username + ")");
			}
			// Xref
			if (xref != null) {
				String id = XrefUtils.getIdentifier(xref);
				String ds = XrefUtils.getDataSource(xref).getFullName();
				if (!Utils.isEmpty(id)) {
					builder.append(" ").append(ds).append(" ").append(id);
				}
			}
			return builder.toString();
		}

	}

}
