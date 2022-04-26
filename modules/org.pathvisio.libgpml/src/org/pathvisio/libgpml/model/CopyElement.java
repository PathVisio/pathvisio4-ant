package org.pathvisio.libgpml.model;

/**
 * This class stores information for copied {@link PathwayElement}. Storing both
 * the new pathway element and its original source pathway element helps
 * maintain pathway element data, such as reference information, when copying
 * (to clipboard) and pasting or transferring pathway data.
 * 
 * {@link PathwayElement#copy} returns a {@link CopyElement} which stores the
 * newly created newElement and a reference to the original sourceElement.
 * 
 * @author finterly
 */
public class CopyElement {

	PathwayElement newElement;

	PathwayElement sourceElement;

	/**
	 * @param newElement
	 * @param sourceElement
	 */
	public CopyElement(PathwayElement newElement, PathwayElement sourceElement) {
		super();
		this.newElement = newElement;
		this.sourceElement = sourceElement;
	}

	/**
	 * @return
	 */
	public PathwayElement getNewElement() {
		return newElement;
	}

	/**
	 * @param newElement
	 */
	public void setNewElement(PathwayElement newElement) {
		this.newElement = newElement;
	}

	/**
	 * @return
	 */
	public PathwayElement getSourceElement() {
		return sourceElement;
	}

	/**
	 * @param sourceElement
	 */
	public void setSourceElement(PathwayElement sourceElement) {
		this.sourceElement = sourceElement;
	}

}
