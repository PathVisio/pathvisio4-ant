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
 * This class defines some shapes. Shapes are defined and registered in the
 * static section of this class. Custom shapes are created using
 * {@link GeneralPath}.
 * 
 * <p>
 * <ol>
 * <li>BRACE
 * <li>MITOCHONDRIA
 * <li>SARCOPLASMICRETICULUM
 * <li>ENDOPLASMICRETICULUM
 * <li>GOLGIAPPARATUS
 * <li>CORONAVIRUS //TODO
 * <li>DNA //TODO
 * <li>CELL ICON //TODO
 * </ol>
 * 
 * NB: shapes were previously specific to GenMAPP, such as the GenMAPP
 * ProteinComplex, Vesicle and Ribosome.
 * 
 * @author unknown, finterly
 */
public class ShapeCatalog {

	static void registerShapes() {
	}

	/**
	 * these constants are internal, only for the switch statement below. There is
	 * no relation with the constants defined in ShapeType.
	 */
	public enum Internal {

		// Basic shapes

		// Basic line shapes
		BRACE,

		// Cellular components
		MITOCHONDRIA, SARCOPLASMIC_RETICULUM, ENDOPLASMIC_RETICULUM, GOLGI_APPARATUS,

		// Special shapes
		CORONAVIRUS, DNA, RNA, DNA2, RNA2, CELL_ICON

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
		case MITOCHONDRIA:
			path.moveTo(72.81f, 85.70f);
			path.curveTo(97.59f, 83.01f, 94.55f, 147.38f, 119.28f, 144.29f);
			path.curveTo(166.27f, 144.40f, 136.22f, 42.38f, 175.51f, 41.70f);
			path.curveTo(215.08f, 41.02f, 188.27f, 150.12f, 227.79f, 148.28f);
			path.curveTo(271.14f, 146.25f, 230.67f, 29.04f, 274.00f, 26.55f);
			path.curveTo(317.72f, 24.05f, 290.58f, 142.55f, 334.36f, 143.22f);
			path.curveTo(371.55f, 143.80f, 351.55f, 43.14f, 388.66f, 45.75f);
			path.curveTo(429.51f, 48.62f, 392.43f, 153.80f, 432.85f, 160.40f);
			path.curveTo(459.82f, 164.80f, 457.96f, 94.30f, 485.13f, 97.26f);
			path.curveTo(548.33f, 124.69f, 534.13f, 233.75f, 472.75f, 258.89f);
			path.curveTo(454.92f, 261.42f, 450.22f, 220.87f, 432.35f, 223.03f);
			path.curveTo(400.60f, 226.86f, 409.73f, 303.71f, 377.80f, 301.95f);
			path.curveTo(348.05f, 300.30f, 365.16f, 223.61f, 335.37f, 223.28f);
			path.curveTo(295.83f, 222.85f, 316.30f, 327.99f, 276.78f, 326.44f);
			path.curveTo(241.90f, 325.08f, 266.95f, 236.11f, 232.34f, 231.61f);
			path.curveTo(200.07f, 227.42f, 201.79f, 311.88f, 169.71f, 306.49f);
			path.curveTo(134.22f, 300.53f, 167.04f, 209.92f, 131.32f, 205.60f);
			path.curveTo(110.14f, 203.04f, 116.28f, 257.74f, 94.95f, 258.26f);
			path.curveTo(15.35f, 236.77f, 5.51f, 114.51f, 72.81f, 85.70f);
			path.closePath();
			path.moveTo(272.82f, 0.84f);
			path.curveTo(378.97f, 1.13f, 542.51f, 62.39f, 543.54f, 168.53f);
			path.curveTo(544.58f, 275.18f, 381.50f, 342.19f, 274.84f, 342.28f);
			path.curveTo(166.69f, 342.36f, 0.84f, 274.66f, 2.10f, 166.51f);
			path.curveTo(3.33f, 60.72f, 167.03f, 0.56f, 272.82f, 0.84f);
			path.closePath();
			break;
		case SARCOPLASMIC_RETICULUM:
			path.moveTo(118.53f, 16.63f);
			path.curveTo(34.13f, 22.00f, 23.84f, 107.76f, 49.44f, 169.22f);
			path.curveTo(73.73f, 242.63f, 0.51f, 289.88f, 56.13f, 366.83f);
			path.curveTo(99.99f, 419.32f, 176.93f, 391.26f, 192.04f, 332.54f);
			path.curveTo(207.42f, 271.52f, 163.49f, 228.38f, 183.45f, 168.61f);
			path.curveTo(211.75f, 89.03f, 181.43f, 16.01f, 118.53f, 16.63f);
			path.lineTo(118.53f, 16.63f);
			path.closePath();
			break;
		case ENDOPLASMIC_RETICULUM:
			path.moveTo(115.62f, 170.76f);
			path.curveTo(106.85f, 115.66f, 152.29f, 74.72f, 152.11f, 37.31f);
			path.curveTo(151.57f, 22.91f, 135.75f, 10.96f, 123.59f, 21.51f);
			path.curveTo(97.02f, 44.83f, 99.19f, 108.29f, 90.52f, 146.58f);
			path.curveTo(89.97f, 157.27f, 79.04f, 153.89f, 78.44f, 145.14f);
			path.curveTo(69.32f, 111.41f, 105.16f, 72.62f, 87.74f, 58.00f);
			path.curveTo(57.12f, 33.80f, 42.90f, 120.64f, 53.32f, 143.34f);
			path.curveTo(65.01f, 185.32f, 49.93f, 215.62f, 42.80f, 189.23f);
			path.curveTo(39.00f, 173.52f, 52.26f, 156.40f, 41.55f, 141.32f);
			path.curveTo(34.82f, 133.03f, 23.22f, 139.41f, 16.36f, 150.49f);
			path.curveTo(0.00f, 182.29f, 23.74f, 271.85f, 49.05f, 257.53f);
			path.curveTo(56.38f, 251.73f, 44.01f, 231.76f, 55.14f, 229.10f);
			path.curveTo(66.52f, 226.70f, 63.22f, 247.43f, 67.13f, 256.43f);
			path.curveTo(70.73f, 268.42f, 74.67f, 281.17f, 83.91f, 290.85f);
			path.curveTo(91.38f, 298.36f, 107.76f, 297.10f, 110.06f, 285.05f);
			path.curveTo(113.23f, 257.62f, 69.35f, 201.07f, 93.40f, 192.41f);
			path.curveTo(122.33f, 184.37f, 100.80f, 263.03f, 131.30f, 280.35f);
			path.curveTo(146.12f, 286.36f, 155.69f, 278.51f, 154.40f, 268.41f);
			path.curveTo(150.12f, 235.05f, 115.21f, 201.24f, 115.47f, 170.24f);
			path.lineTo(115.62f, 170.76f);
			path.closePath();
			break;
		case GOLGI_APPARATUS:
			path.moveTo(148.89f, 77.62f);
			path.curveTo(100.07f, 3.50f, 234.06f, 7.65f, 207.78f, 62.66f);
			path.curveTo(187.00f, 106.50f, 171.09f, 190.54f, 209.13f, 287.47f);
			path.curveTo(240.55f, 351.33f, 111.35f, 353.69f, 144.36f, 284.72f);
			path.curveTo(171.13f, 215.31f, 165.77f, 107.32f, 148.89f, 77.62f);
			path.lineTo(148.89f, 77.62f);
			path.closePath();
			path.moveTo(88.16f, 91.24f);
			path.curveTo(62.70f, 40.69f, 158.70f, 44.41f, 131.59f, 92.83f);
			path.curveTo(116.28f, 128.91f, 117.95f, 238.10f, 134.33f, 269.85f);
			path.curveTo(154.45f, 313.72f, 56.82f, 315.51f, 85.96f, 264.54f);
			path.curveTo(102.37f, 223.58f, 110.67f, 141.16f, 88.16f, 91.24f);
			path.lineTo(88.16f, 91.24f);
			path.closePath();
			path.moveTo(83.40f, 133.15f);
			path.curveTo(86.43f, 160.23f, 86.72f, 203.15f, 82.05f, 220.09f);
			path.curveTo(73.24f, 250.74f, 69.98f, 262.93f, 50.80f, 265.89f);
			path.curveTo(32.17f, 265.52f, 22.80f, 242.80f, 39.49f, 227.87f);
			path.curveTo(50.94f, 214.61f, 53.98f, 202.20f, 55.20f, 173.72f);
			path.curveTo(54.63f, 152.16f, 56.07f, 133.57f, 43.25f, 126.63f);
			path.curveTo(25.26f, 121.45f, 30.31f, 86.90f, 56.06f, 93.20f);
			path.curveTo(69.86f, 95.63f, 79.23f, 109.03f, 83.40f, 133.15f);
			path.lineTo(83.40f, 133.15f);
			path.closePath();
			break;
		// ========================================
		// Special shapes
		// ========================================
		case CORONAVIRUS:
			path.append(new Ellipse2D.Double(90, 90, 150, 150), false);
			double origin = 165;
			for (double angle = 0; angle < 360; angle += 30) {
				double rads = Math.toRadians(angle);
				// draw coronavirus "spikes"
				double x = origin + (Math.cos(rads) * 150) - 15;
				double y = origin + (Math.sin(rads) * 150) - 15;
				path.append(new Ellipse2D.Double(x, y, 30, 30), false);
				// draw connecting lines
				double startX = origin + (Math.cos(rads) * 75);
				double startY = origin + (Math.sin(rads) * 75);
				double endX = origin + (Math.cos(rads) * 135);
				double endY = origin + (Math.sin(rads) * 135);
				path.moveTo(startX, startY);
				path.lineTo(endX, endY);
			}	
			break;
		case DNA:
			path.moveTo(218.55, 25.04);
			path.curveTo(266.4, 20.87, 371.1, 12.3, 372, 0.25);
			path.lineTo(372, 0);
			path.curveTo(372, 0, 347.17, 0.01, 334.05, 0.08);
			path.curveTo(334.05, 0.08, 333.56, 1.76, 328.29, 3.6);
			path.lineTo(43.76, 3.6);
			path.curveTo(40.55, 1.9, 39.16, 0, 39.16, 0);
			path.curveTo(39.16, 0, 14.02, 0.1, 0, 0);
			path.curveTo(0, 12.14, 100.14, 20.26, 152.52, 25.02);
			path.curveTo(76.62, 30.13, 0, 37.83, 0, 49.99);
			path.curveTo(0, 62.14, 104.42, 70.68, 152.52, 74.98);
			path.curveTo(95.86, 79.97, 0, 87.78, 0, 99.93);
			path.curveTo(14.42, 99.93, 19.72, 99.95, 38.05, 99.95);
			path.lineTo(38.05, 99.87);
			path.curveTo(38.05, 98.32, 38.74, 98.21, 41.14, 96.57);
			path.lineTo(329.05, 96.57);
			path.curveTo(334.92, 98.19, 334.65, 98.86, 334.05, 99.83);
			path.curveTo(334.05, 99.89, 334.05, 99.93, 333.95, 99.97);
			path.curveTo(346.59, 100, 354.51, 99.93, 372, 99.93);
			path.curveTo(372, 87.78, 269.98, 79.25, 218.55, 74.98);
			path.curveTo(268.67, 70.93, 295.2, 68.84, 322.23, 65.18);
			path.curveTo(351.65, 61.19, 372, 56.32, 372, 49.99);
			path.curveTo(372, 37.83, 293.63, 29.21, 218.55, 25.04);
			path.closePath();
			path.moveTo(51.72, 7);
			path.lineTo(315, 7);
			path.curveTo(308.79, 9.61, 295.73, 11.26, 280.28, 13.4);
			path.lineTo(92.02, 13.4);
			path.curveTo(75.26, 11.57, 62.25, 9.09, 51.72, 7);
			path.closePath();
			path.moveTo(122.25, 16.8);
			path.lineTo(248.14, 16.8);
			path.curveTo(239.39, 17.53, 210.4, 20.26, 186, 22.49);
			path.curveTo(156.66, 20.26, 132.72, 17.67, 122.25, 16.8);
			path.closePath();
			path.moveTo(320.13, 93.17);
			path.lineTo(48.2, 93.17);
			path.curveTo(64.21, 89.92, 65.6, 89.4, 90.97, 86.58);
			path.lineTo(286.56, 86.58);
			path.curveTo(304.23, 89.17, 308.3, 90.3, 320.13, 93.17);
			path.closePath();
			path.moveTo(255.93, 83.38);
			path.lineTo(114.16, 83.38);
			path.curveTo(129.28, 82.09, 162.24, 78.95, 186, 77.48);
			path.curveTo(207.64, 79.14, 240.56, 82.07, 255.93, 83.38);
			path.closePath();
			path.moveTo(186, 72.48);
			path.curveTo(154.55, 70.27, 131.18, 68.27, 111.6, 66.38);
			path.lineTo(260.4, 66.38);
			path.curveTo(241.38, 68.33, 214.99, 70.4, 186, 72.48);
			path.closePath();
			path.moveTo(292.95, 62.98);
			path.lineTo(79.05, 62.98);
			path.curveTo(70.28, 61.38, 53.21, 59.22, 46.5, 56.58);
			path.lineTo(325.5, 56.58);
			path.curveTo(320.52, 58.58, 313.87, 60.73, 292.95, 62.98);
			path.closePath();
			path.moveTo(335.73, 53.19);
			path.lineTo(38.13, 53.19);
			path.curveTo(34.57, 50.5, 33.84, 48.55, 37.2, 46.39);
			path.lineTo(334.8, 46.39);
			path.curveTo(338.86, 48.84, 339.25, 50.73, 335.73, 53.19);
			path.closePath();
			path.moveTo(329.66, 43.19);
			path.lineTo(43.71, 43.19);
			path.curveTo(52.36, 41.08, 58.04, 38.75, 79.05, 36.49);
			path.lineTo(302.25, 36.49);
			path.curveTo(319.98, 39.11, 323.67, 40.79, 329.66, 43.19);
			path.closePath();
			path.moveTo(275.49, 33.39);
			path.lineTo(102.79, 33.39);
			path.curveTo(130.52, 30.82, 149.72, 29.37, 186, 27.49);
			path.curveTo(222.16, 29.4, 248.6, 30.8, 275.49, 33.39);
			path.closePath();
			break;
		case RNA:
			path.moveTo(148.27,705.27);
			path.curveTo(169.91,720.92,240.56,774.71,255.93,787.02);
			path.lineTo(205.15,787.02);
			path.lineTo(205.15,819.15);
			path.lineTo(286.56,819.15);
			path.curveTo(304.23,843.65,308.3,854.34,320.13,881.5);
			path.lineTo(205.15,881.5);
			path.lineTo(205.15,913.63);
			path.lineTo(329.05,913.63);
			path.curveTo(334.92,928.87,334.65,933.26,334.05,942.44);
			path.curveTo(334.05,942.94,334.05,943.34,333.95,943.74);
			path.curveTo(346.59,944,354.51,943.34,372,943.34);
			path.curveTo(372,828.61,269.98,748.16,218.55,707.81);
			path.lineTo(186,684.21);
			path.curveTo(154.55,663.34,131.18,648.02,111.6,630.19);
			path.lineTo(177.18,630.19);
			path.lineTo(177.18,598.06);
			path.lineTo(79.05,598.06);
			path.curveTo(70.28,582.89,53.21,560.6,46.5,535.71);
			path.lineTo(177.18,535.71);
			path.lineTo(177.18,503.58);
			path.lineTo(38.13,503.58);
			path.curveTo(34.57,478.2,33.84,461.62,37.2,441.23);
			path.lineTo(177.18,441.23);
			path.lineTo(177.18,409.1);
			path.lineTo(43.71,409.1);
			path.curveTo(52.36,389.2,58.04,368.06,79.05,346.74);
			path.lineTo(177.18,346.74);
			path.lineTo(177.18,314.62);
			path.lineTo(102.3,314.62);
			path.curveTo(128.8,287.9,163.55,264.42,204.31,234.31);
			path.curveTo(260.71,194.51,371.1,116.07,372,2.33);
			path.lineTo(372,0);
			path.curveTo(372,0,347.17,0.13,334.05,0.73);
			path.curveTo(334.05,0.73,333.56,13.83,328.29,31.18);
			path.lineTo(205.15,31.18);
			path.lineTo(205.15,63.3);
			path.lineTo(315,63.3);
			path.curveTo(308.79,87.99,295.73,105.52,280.28,125.66);
			path.lineTo(205.15,125.66);
			path.lineTo(205.15,157.78);
			path.lineTo(248.14,157.78);
			path.curveTo(241.47,163.04,184.7,206.84,160.39,223.85);
			path.curveTo(107.81,263.34,0,357.15,0,471.89);
			path.curveTo(0,586.62,100.17,664.67,148.27,705.27);
			path.closePath();
			break; 
			
		case DNA2:		
			path.moveTo(186.23,25.01);
			path.curveTo(234.14,20.84,371.55,12.3,372.45,0.25);
			path.lineTo(372.45,0);
			path.curveTo(372.45,0,14.04,0.1,0,0);
			path.curveTo(0,12.15,133.78,20.25,186.23,25.01);
			path.curveTo(110.24,30.12,0,37.86,0,50.02);
			path.curveTo(0,62.19,138.07,70.73,186.23,75.03);
			path.curveTo(129.5,80.03,0,87.84,0,100);
			path.lineTo(372.45,100);
			path.curveTo(372.45,87.84,237.72,79.31,186.23,75.03);
			path.curveTo(236.41,70.98,371.96,61.11,372.45,50.02);
			path.curveTo(373,37.86,261.4,29.18,186.23,25.01);
			path.closePath();
			path.moveTo(15, 5);
			path.lineTo(360, 5);
			path.moveTo(78, 15);
			path.lineTo(300, 15);		
			path.moveTo(68, 35);
			path.lineTo(310, 35);
			path.moveTo(7, 45);
			path.lineTo(365, 45);
			path.moveTo(15, 55);
			path.lineTo(355, 55);
			path.moveTo(80, 65);
			path.lineTo(300, 65);
			path.moveTo(75, 85);
			path.lineTo(300, 85);
			path.moveTo(18, 95);
			path.lineTo(355, 95);
			path.closePath();
			break;
		case RNA2:
			path.moveTo(186,25.01);
			path.curveTo(233.85,20.84,371.1,12.3,372,0.25);
			path.lineTo(372,0);
			path.curveTo(372,0,200.02,0.1,186,0);
			path.lineTo(186,25.01);
			path.curveTo(110.1,30.12,0,37.86,0,50.02);
			path.curveTo(0,62.19,137.9,70.73,186,75.03);
			path.curveTo(186,80.45,186,100,186,100);
			path.lineTo(372,100);
			path.curveTo(372,87.84,237.43,79.31,186,75.03);
			path.closePath();
			path.moveTo(200, 5);
			path.lineTo(360, 5);
			path.moveTo(200, 15);
			path.lineTo(300, 15);		
			path.moveTo(68, 35);
			path.lineTo(180, 35);
			path.moveTo(7, 45);
			path.lineTo(180, 45);
			path.moveTo(15, 55);
			path.lineTo(180, 55);
			path.moveTo(80, 65);
			path.lineTo(180, 65);
			path.moveTo(200, 85);
			path.lineTo(300, 85);
			path.moveTo(200, 95);
			path.lineTo(355, 95);
			path.closePath();
			break;
		case CELL_ICON:
			// cell membrane (inner)
			path.moveTo(4, 53);
			path.curveTo(7, 68, 2, 94, 23, 99);
			path.curveTo(30, 100, 42, 96, 56, 94);
			path.curveTo(84, 92, 118, 88, 129, 79);
			path.curveTo(145, 66, 129, 32, 123, 21);
			path.curveTo(117, 11, 109, 12, 98, 9);
			path.curveTo(88, 5, 76, 1, 61, 1);
			path.curveTo(46, 0, 21, 5, 12, 14);
			path.curveTo(2, 23, 0, 38, 4, 53);
			path.closePath();
			// cell membrane (outer)
			path.moveTo(4, 53);
			path.curveTo(7, 68, 2, 94, 23, 100);
			path.curveTo(30, 103, 42, 100, 56, 98);
			path.curveTo(84, 95, 118, 99, 135, 80);
			path.curveTo(145, 66, 135, 32, 125, 21);
			path.curveTo(117, 11, 109, 12, 98, 9);
			path.curveTo(88, 5, 76, 1, 61, 1);
			path.curveTo(46, 0, 21, 5, 12, 14);
			path.curveTo(2, 23, 0, 38, 4, 53);
			path.closePath();
			// nucleolus
			path.append(new Ellipse2D.Double(65, 55, 10, 10), false);
			path.append(new Ellipse2D.Double(65, 55, 10, 10), false); // for fill color
			// nucleus
//			path.append(new Ellipse2D.Double(37, 30, 50, 45), false); 
//			path.append(new Ellipse2D.Double(34.5, 27.5, 55, 50), false); // for fill color
			path.append(new Ellipse2D.Double(34.5, 27.5, 55, 50), false);
			// mitochondria (simplified version) 
			GeneralPath mito = new GeneralPath();
			mito.moveTo(2.65,2.49);
			mito.curveTo(3.56,2.41,3.45,4.3,4.36,4.21);
			mito.curveTo(6.08,4.21,6.04,1.16,7.49,1.14);
			mito.curveTo(8.94,1.12,8.93,4.09,10.54,4.11);
			mito.curveTo(11.91,4.12,11.7,1.11,13.07,1.19);
			mito.curveTo(14.57,1.27,14.4,4.48,15.89,4.68);
			mito.curveTo(16.88,4.81,16.81,2.74,17.81,2.83);
			mito.curveTo(19.49,3.41,19.44,5.24,18.43,6.48);
			mito.curveTo(18.05,6.96,17.52,7.35,16.89,7.56);
			mito.curveTo(16.24,7.63,14.93,6.58,14.28,6.64);
			mito.curveTo(13.11,6.75,13.05,9.07,11.87,9.02);
			mito.curveTo(10.78,8.97,10.79,7.09,9.51,6.96);
			mito.curveTo(8.33,6.84,8.39,9.11,7.21,8.95);
			mito.curveTo(5.9,8.78,6.11,6.13,4.8,6);
			mito.curveTo(4.02,5.92,4.25,7.52,3.46,7.54);
			mito.curveTo(0.53,6.91,0.17,3.33,2.65,2.49);
			mito.closePath();
			mito.append(new Ellipse2D.Double(0, 0, 20, 10), false);
			mito.append(new Ellipse2D.Double(0, 0, 20, 10), false);// for fill color
			mito.append(new Ellipse2D.Double(0, 0, 20, 10), false);// for fill color
			AffineTransform at = new AffineTransform();
			at.translate(100, 30);
			at.scale(1.5, 1.5);
			at.rotate(40, 50);
			Shape mitochondria = at.createTransformedShape(mito);
			path.append(mitochondria, false);
			// endoplasmic reticulum
			AffineTransform at2 = new AffineTransform();
			at2.translate(35, -5);
			at2.scale(0.2, 0.2);
			at2.rotate(50, 40);
			Shape endoplasmicReticulum = at2.createTransformedShape(getPluggableShape(Internal.ENDOPLASMIC_RETICULUM));
			path.moveTo(34.5, 27.5);
			path.append(endoplasmicReticulum, false);
			// small miscellaneous organelles
			path.append(new Ellipse2D.Double(20, 80, 5, 5), false);
			path.append(new Ellipse2D.Double(25, 65, 5, 5), false);
			path.append(new Ellipse2D.Double(30, 80, 5, 5), false);

			break;
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
