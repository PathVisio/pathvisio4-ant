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

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class ImageButtonShapes {

	static void registerShapes() {
	}

	/**
	 * these constants are internal, only for the switch statement below. There is
	 * no relation with the constants defined in ShapeType.
	 */
	public enum ButtonList {
		MITOCHONDRIA_BUTTON, CELL_ICON_BUTTON
	}

	/**
	 * Internal, Only for general shape types that can be described as a path. The
	 * shapes are constructed as a general path with arbitrary size and then resized
	 * to fit w and h parameters.
	 */
	static public java.awt.Shape getButtonShape(ButtonList st) {
		GeneralPath path = new GeneralPath();
		switch (st) {
		case MITOCHONDRIA_BUTTON:
			path.moveTo(2.6, 2.5);
			path.curveTo(3.56, 2.41, 3.45, 4.3, 4.36, 4.21);
			path.curveTo(6, 4.21, 6.04, 1.16, 7.5, 1.14);
			path.curveTo(9, 1.12, 9, 4, 10.5, 4);
			path.curveTo(11.91, 4.12, 11.7, 1.11, 13.07, 1.19);
			path.curveTo(14.57, 1.27, 14.4, 4.48, 15.89, 4.68);
			path.curveTo(16.88, 4.81, 16.81, 2.74, 17.81, 2.83);
			path.curveTo(19.49, 3.41, 19.44, 5.24, 18.43, 6.48);
			path.curveTo(18.05, 6.96, 17.52, 7.35, 16.89, 7.56);
			path.curveTo(16.24, 7.63, 14.93, 6.58, 14.28, 6.64);
			path.curveTo(13.11, 6.75, 13.05, 9.07, 11.87, 9.02);
			path.curveTo(10.78, 8.97, 10.79, 7.09, 9.51, 6.96);
			path.curveTo(8.33, 6.84, 8.39, 9.11, 7.21, 8.95);
			path.curveTo(5.9, 8.78, 6.11, 6.13, 4.8, 6);
			path.curveTo(4.02, 5.92, 4.25, 7.52, 3.46, 7.54);
			path.curveTo(0.53, 6.91, 0.17, 3.33, 2.65, 2.49);
			path.closePath();
			path.append(new Ellipse2D.Double(0, 0, 20, 10), false);
			break;
		case CELL_ICON_BUTTON:
			// cell membrane
			path.moveTo(0.87, 42.66);
			path.curveTo(0, 58.6, 1.29, 79.48, 14.32, 88.6);
			path.curveTo(22.71, 94.47, 34.73, 87.46, 44.9, 86.47);
			path.curveTo(67.63, 84.26, 97.85, 95.25, 112.88, 77.96);
			path.curveTo(125.2, 63.79, 124.12, 36.12, 112.34, 21.5);
			path.curveTo(102.41, 9.17, 81.8, 13.21, 66.16, 11.2);
			path.curveTo(49.15, 9, 29.21, 0, 14.77, 9.29);
			path.curveTo(4.67, 15.78, 1.54, 30.62, 0.87, 42.66);
			path.closePath();
			path.moveTo(112.37, 21.48); // for "3D"
			path.curveTo(124.15, 36.1, 125.22, 63.78, 112.91, 77.94);
			path.curveTo(97.87, 95.24, 67.65, 84.24, 44.92, 86.45);
			path.curveTo(34.75, 87.44, 22.73, 94.45, 14.34, 88.58);
			path.curveTo(23.57, 96.12, 38, 91.78, 49.88, 91.44);
			path.curveTo(72.58, 90.81, 103.16, 100, 117.37, 82.18);
			path.curveTo(130, 66.35, 125.64, 36.78, 112.37, 21.48);
			path.closePath();
			// nucleolus
			path.append(new Ellipse2D.Double(65, 55, 10, 10), false);
			// nucleus
			path.append(new Ellipse2D.Double(34.5, 27.5, 55, 50), false);
			break;
		default:
			break;
		}
		return path;
	}

}
