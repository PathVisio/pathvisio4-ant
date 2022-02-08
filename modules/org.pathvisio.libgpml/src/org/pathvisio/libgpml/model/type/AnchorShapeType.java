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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Extensible enum for Anchor types
 */
public class AnchorShapeType implements Comparable<AnchorShapeType> {
	private static Map<String, AnchorShapeType> nameMappings = new HashMap<String, AnchorShapeType>();
	private static Set<AnchorShapeType> values = new TreeSet<AnchorShapeType>();
	
	public static final AnchorShapeType NONE = new AnchorShapeType("None");
	public static final AnchorShapeType CIRCLE = new AnchorShapeType("Circle");
	
	private String name;
    private boolean disallowLinks;

    private AnchorShapeType (String name)
	{
		this(name, false);
	}

    private AnchorShapeType (String name, final boolean disallowLinks)
	{
		if (name == null) { throw new NullPointerException(); }
		this.disallowLinks = disallowLinks;
		this.name  = name;
		values.add(this);
		nameMappings.put (name, this);
	}

	/**
	   Create an object and add it to the list.

	   For extending the enum.
	 */
	public static AnchorShapeType create (String name)
	{
		return new AnchorShapeType(name, false);
	}

    /**
     * Create an object and add it to the list
     * @param name - identifier for anchor type
     * @param disallowLinks - boolean if set to true nothing will be able to attach to this anchor
     * @return a new AnchorType object
     */
    public static AnchorShapeType create (String name, final boolean disallowLinks)
    {
        return new AnchorShapeType(name, disallowLinks);
    }


    /**
	   looks up the AnchorType corresponding to that name.
	 */
	public static AnchorShapeType fromName (String value)
	{
		return nameMappings.get(value);
	}

	/**
	   Stable identifier for this AnchorType.
	 */
	public String getName ()
	{
		return name;
	}

	static public AnchorShapeType[] getValues()
	{
		return values.toArray(new AnchorShapeType[0]);
	}

	public String toString()
	{
		return name;
	}

    public boolean isDisallowLinks() {
        return disallowLinks;
    }

    public int compareTo(AnchorShapeType o) {
		return toString().compareTo(o.toString());
	}
}
