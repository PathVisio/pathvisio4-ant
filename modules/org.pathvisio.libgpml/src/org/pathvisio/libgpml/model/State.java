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

import org.pathvisio.libgpml.model.GraphLink.LinkableTo;
import org.pathvisio.libgpml.model.type.ObjectType;
import org.pathvisio.libgpml.model.GraphLink.LinkableFrom;
import org.pathvisio.libgpml.prop.StaticProperty;
import org.pathvisio.libgpml.util.Utils;

/**
 * State-specific implementation of methods that calculate derived
 * coordinates that are not stored in GPML directly
 */
public class State extends PathwayElement implements LinkableFrom
{
	protected State()
	{
		super(ObjectType.STATE);
	}

	@Override
	public int getZOrder()
	{
		PathwayElement dn = getParentDataNode();
		if (dn == null) return 0; //TODO: must be cached like centerX etc.
		return dn.getZOrder() + 1;
	}
	
	public PathwayElement getParentDataNode()
	{
		PathwayModel parent = getParent();
		if (parent == null) 
			return null;

		return parent.getElementById(getElementRef());
	}
	
	private void updateCoordinates()
	{
		PathwayElement dn = getParentDataNode();
		if (dn != null)
		{
			double centerx = dn.getCenterX() + (getRelX() * dn.getWidth() / 2);
			double centery = dn.getCenterY() + (getRelY() * dn.getHeight() / 2);
			setCenterY(centery);
			setCenterX(centerx);
		}
	}

	public void linkTo(LinkableTo idc, double relX, double relY)
	{
		String id = idc.getElementId();
		if(id == null) id = idc.setGeneratedElementId();
		setElementRef(idc.getElementId());
		setRelX(relX);
		setRelY(relY);
	}

	public void unlink()
	{
		// called when referred object is being destroyed.
		// destroy self.
		parent.remove(this);
	}

	@Override
	public void setRelX(double value)
	{
		super.setRelX(value);
		updateCoordinates();
	}
	
	@Override
	public void setRelY(double value)
	{
		super.setRelY(value);
		updateCoordinates();
	}
	
	public void refeeChanged()
	{
		updateCoordinates();
	}

	public void setElementRef(String v)
	{
		if (!Utils.stringEquals(elementRef, v))
		{
			if (parent != null)
			{
				if (elementRef != null)
				{
					parent.removeGraphRef(elementRef, this);
				}
				if (v != null)
				{
					parent.addGraphRef(v, this);
					updateCoordinates();
				}
			}
			elementRef = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GRAPHREF));
		}
	}

	@Override
	public void setParent(PathwayModel v)
	{
		if (parent != v)
		{
			super.setParent(v);
			if (parent != null && elementRef != null)
			{
				updateCoordinates();
			}
		}
	}

}
