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
package org.pathvisio.libgpml.model.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extensible enum pattern for defining the various possible arrow heads.
 * NB: the name LineType is slightly misleading, as it refers strictly to arrowheads and
 * other kinds of line endings.
 * A Line in PathVisio has two endings that each can have a different "LineType"
 */
public class ArrowHeadType
{
	private static Map<String, ArrowHeadType> nameMappings = new HashMap<String, ArrowHeadType>();
	private static List<ArrowHeadType> values = new ArrayList<ArrowHeadType>();
	private static List<ArrowHeadType> visible = new ArrayList<ArrowHeadType>();

	/** LineType LINE means the absence of an arrowhead */
	public static final ArrowHeadType LINE = new ArrowHeadType("Line", "Line");
	public static final ArrowHeadType ARROW = new ArrowHeadType("Arrow", "Arrow");
	public static final ArrowHeadType TBAR = new ArrowHeadType("TBar", "TBar");
	
	@Deprecated
	public static final ArrowHeadType RECEPTOR = new ArrowHeadType("Receptor", "Receptor", true);
	@Deprecated
	public static final ArrowHeadType LIGAND_SQUARE = new ArrowHeadType("LigandSquare","LigandSq", true);
	@Deprecated
	public static final ArrowHeadType RECEPTOR_SQUARE = new ArrowHeadType("ReceptorSquare", "ReceptorSq", true);
	@Deprecated
	public static final ArrowHeadType LIGAND_ROUND = new ArrowHeadType("LigandRound", "LigandRd", true);
	@Deprecated
	public static final ArrowHeadType RECEPTOR_ROUND = new ArrowHeadType("ReceptorRound", "ReceptorRd", true);
		
	/**
	   mappName may be null for new shapes that don't have a .mapp
	   equivalent.
	 */
	private ArrowHeadType (String name, String mappName)
	{
		this (name, mappName, false);
	}

	private ArrowHeadType (String name, String mappName, boolean hidden)
	{
		if (name == null) { throw new NullPointerException(); }

		this.mappName = mappName;
		this.name = name;

		nameMappings.put (name, this);

		values.add (this);		
		if (!hidden) visible.add (this);
	}
	
	/**
	   Create an object and add it to the list.

	   For extending the enum.
	 */
	public static ArrowHeadType create (String name, String mappName)
	{
		if (nameMappings.containsKey (name))
		{
			return nameMappings.get (name);
		}
		else
		{
			return new ArrowHeadType (name, mappName);
		}
	}

	private String mappName;
	private String name;

	public String getMappName() { return mappName; }

	public String getName() { return name; }

	public static ArrowHeadType fromName(String value)
	{
		return nameMappings.get (value);
	}

	static public String[] getNames()
	{
		return nameMappings.keySet().toArray(new String[nameMappings.size()]); 
	}

	static public String[] getVisibleNames()
	{
		String[] result = new String [visible.size()];
		for (int i = 0; i < visible.size(); ++i)
		{
			result[i] = visible.get(i).getName();
		}
		return result;
	}

	static public ArrowHeadType[] getValues()
	{
		return nameMappings.values().toArray (new ArrowHeadType[nameMappings.size()]);
	}

	static public ArrowHeadType[] getVisibleValues()
	{
		return visible.toArray (new ArrowHeadType[0]);
	}

	public String toString()
	{
		return name;
	}
}
