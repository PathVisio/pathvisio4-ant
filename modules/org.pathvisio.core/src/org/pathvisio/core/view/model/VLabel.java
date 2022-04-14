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

import java.awt.Color;

import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.libgpml.model.Label;
import org.pathvisio.libgpml.util.Utils;

/**
 * Represents the view of a PathwayElement with ObjectType.LABEL.
 * 
 * @author unknown, finterly
 */
public class VLabel extends VShapedElement {
	/**
	 * Constructor for this class
	 * 
	 * @param canvas - the VPathway this label will be part of
	 */
	public VLabel(VPathwayModel canvas, Label o) {
		super(canvas, o);
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	@Override
	public Label getPathwayObject() {
		return (Label) super.getPathwayObject();
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	@Override
	protected Color getTextColor() {
		Color textColor = getPathwayObject().getTextColor();
		/*
		 * the selection is not colored red when in edit mode it is possible to see a
		 * color change immediately
		 */
		if (isSelected() && !canvas.isEditMode()) {
			textColor = selectColor;
		}
		// if hyperlink set color
		Label o = getPathwayObject();
		if (o.getHref() != null && !Utils.stringEquals(o.getHref(), "")) {
			if (PreferenceManager.getCurrent() == null) {
				PreferenceManager.init();
			}
			Color hyperlinkColor = PreferenceManager.getCurrent().getColor(GlobalPreference.COLOR_LINK);
			o.setTextColor(hyperlinkColor);
		}
		return textColor;
	}
}
