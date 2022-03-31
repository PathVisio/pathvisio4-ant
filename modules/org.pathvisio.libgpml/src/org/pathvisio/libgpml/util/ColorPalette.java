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
package org.pathvisio.libgpml.util;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.pathvisio.libgpml.debug.Logger;

/**
 * This utils class contains pathway model colors for Color Palette (s).
 * 
 * @author finterly
 */
public abstract class ColorPalette {

	// Wikipathways Color Palette
	public static final Color WP_BLACK = Color.BLACK;
	public static final Color WP_WHITE = Color.WHITE;
	public static final Color WP_GREY = Color.decode("#F5F6F6");
	public static final Color WP_DGREY = Color.decode("#94A6A8");
	public static final Color WP_BLUE = Color.decode("#3955E7");
	public static final Color WP_DBLUE = Color.decode("#1E3199");
	public static final Color WP_GREEN = Color.decode("#00CC9E");
	public static final Color WP_DGREEN = Color.decode("#028F6F");
	public static final Color WP_PURPLE = Color.decode("#880BC8");
	public static final Color WP_DPURPLE = Color.decode("#620492");
	public static final Color WP_ORANGE = Color.decode("#FF8120");
	public static final Color WP_DORANGE = Color.decode("#D16919");
}
