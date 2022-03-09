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
package org.pathvisio.libgpml.model.shape;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * This class defines some arrow head shapes. Shapes are defined and registered in the
 * static section of this class. Custom shapes are created using
 * {@link GeneralPath}.
 * 
 * <p>
 * <ol>
 * <li>
 * </ol>
 * 
 * @author unknown, finterly
 */
public class ArrowShapeCatalog {

	static void registerShapes() {
	}

	/**
	 * these constants are internal, only for the switch statement below. There is
	 * no relation with the constants defined in ShapeType.
	 */
	public enum Internal {

		// Basic shapes

		

	}

	/**
	 * Internal, Only for general shape types that can be described as a path. The
	 * shapes are constructed as a general path with arbitrary size and then resized
	 * to fit w and h parameters.
	 */
	static public java.awt.Shape getPluggableShape(Internal st) {
		GeneralPath path = new GeneralPath();
		switch (st) {
		// ========================================
		// Basic line shapes
		// ========================================
		case BRACE:
			path.moveTo(0, 4);
			path.quadTo(0, 2, 3, 2);
			path.quadTo(6, 2, 6, 0);
			path.quadTo(6, 2, 9, 2);
			path.quadTo(12, 2, 12, 4);
			break;
		// ========================================
		// Cellular components (irregular shape)
		// ========================================
		default:
			break;
		}
		return path;
	}

	/**
	 * Returns regular polygon shape given number of sides, width, and height.
	 * 
	 * @param sides the number of sides of polygon.
	 * @param w     the width.
	 * @param h     the height.
	 * @return
	 */
	public static java.awt.Shape getRegularPolygon(int sides, double w, double h) {
		GeneralPath path = new GeneralPath();
		for (int i = 0; i < sides; ++i) {
			double angle = Math.PI * 2 * i / sides;
			double x = (w / 2) * (1 + Math.cos(angle));
			double y = (h / 2) * (1 + Math.sin(angle));
			if (i == 0) {
				path.moveTo((float) x, (float) y);
			} else {
				path.lineTo((float) x, (float) y);
			}
		}
		path.closePath();
		return path;
	}

//	static public java.awt.Shape getCircle(double xCenter, double yCenter, double r, int nPoints) {
//		GeneralPath gp = new GeneralPath();
//		for (int i = 0; i < nPoints; i++) {
//			double angle = i / (double) nPoints * Math.PI * 2;
//			double x = r * Math.cos(angle) + xCenter;
//			double y = r * Math.sin(angle) + yCenter;
//			if (i == 0)
//				gp.moveTo(x, y);
//			else
//				gp.lineTo(x, y);
//		}
//		gp.closePath();
//		return gp;
//	}

	// TODO
//	@Deprecated
//	MIM_PHOSPHORYLATED_SHAPE;
//	MIM_DEGRADATION_SHAPE;
//	MIM_INTERACTION_SHAPE;

//	/**
//	 * Internal, Only for general shape types that can be described as a path. The
//	 * shapes are constructed as a general path with arbitrary size and then resized
//	 * to fit w and h parameters.
//	 */
//	static private java.awt.Shape getPluggableShape(int st) {
//		GeneralPath path = new GeneralPath();
//		switch (st) {
//		case MIM_DEGRADATION:
//			path.moveTo(31.59f, 18.46f);
//			path.curveTo(31.59f, 25.44f, 25.72f, 31.10f, 18.50f, 31.10f);
//			path.curveTo(11.27f, 31.10f, 5.41f, 25.44f, 5.41f, 18.46f);
//			path.curveTo(5.41f, 11.48f, 11.27f, 5.82f, 18.50f, 5.82f);
//			path.curveTo(25.72f, 5.82f, 31.59f, 11.48f, 31.59f, 18.46f);
//			path.closePath();
//			path.moveTo(0.39f, 0.80f);
//			path.curveTo(34.84f, 36.07f, 35.25f, 35.67f, 35.25f, 35.67f);
//			break;
//		case MIM_PHOSPHORYLATED:
//			path.moveTo(5.79f, 4.72f);
//			path.lineTo(5.79f, 18.18f);
//			path.lineTo(13.05f, 18.18f);
//			path.curveTo(15.74f, 18.18f, 17.81f, 17.60f, 19.28f, 16.43f);
//			path.curveTo(20.75f, 15.26f, 21.48f, 13.60f, 21.48f, 11.44f);
//			path.curveTo(21.48f, 9.29f, 20.75f, 7.64f, 19.28f, 6.47f);
//			path.curveTo(17.81f, 5.30f, 15.74f, 4.72f, 13.05f, 4.72f);
//			path.lineTo(5.79f, 4.72f);
//			path.moveTo(0.02f, 0.73f);
//			path.lineTo(13.05f, 0.73f);
//			path.curveTo(17.83f, 0.73f, 21.44f, 1.65f, 23.88f, 3.47f);
//			path.curveTo(26.34f, 5.28f, 27.57f, 7.93f, 27.57f, 11.44f);
//			path.curveTo(27.57f, 14.98f, 26.34f, 17.65f, 23.88f, 19.46f);
//			path.curveTo(21.44f, 21.26f, 17.83f, 22.17f, 13.05f, 22.17f);
//			path.lineTo(5.79f, 22.17f);
//			path.lineTo(5.79f, 36.57f);
//			path.lineTo(0.02f, 36.57f);
//			path.lineTo(0.02f, 0.73f);
//			break;
//		case MIM_INTERACTION:
//			path.moveTo(30.90f, 15.20f);
//			path.curveTo(30.90f, 23.18f, 24.02f, 29.65f, 15.55f, 29.65f);
//			path.curveTo(7.08f, 29.65f, 0.20f, 23.18f, 0.20f, 15.20f);
//			path.curveTo(0.20f, 7.23f, 7.08f, 0.76f, 15.55f, 0.76f);
//			path.curveTo(24.02f, 0.76f, 30.90f, 7.23f, 30.90f, 15.20f);
//			path.closePath();
//			break;
//		default:
//			assert (false);
//		}
//		return path;
//	}

}
