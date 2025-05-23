/*
	This file is part of FreeJ2ME.

	FreeJ2ME is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	FreeJ2ME is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with FreeJ2ME.  If not, see http://www.gnu.org/licenses/
*/
package javax.microedition.m3g;

public class World extends Group
{

	private Camera activeCamera;
	private Background background;

	public World() { }

	Object3D duplicateImpl() 
	{
		World copy = new World();
		super.duplicate((Group) copy);
		copy.activeCamera = (Camera) activeCamera.duplicate();
		copy.background = (Background) background.duplicate();
		return copy;
	}

	public Camera getActiveCamera() { return activeCamera; }

	public void setActiveCamera(Camera camera) { activeCamera = camera; }

	public Background getBackground() { return background; }

	public void setBackground(Background background) 
	{
		this.background = background;
	}

	@Override
	public int doGetReferences(Object3D[] references) 
	{
		int parentCount = super.doGetReferences(references);

		if (activeCamera != null) 
		{
			if (references != null) { references[parentCount] = activeCamera; }
			++parentCount;
		}

		if (background != null) 
		{
			if (references != null) { references[parentCount] = background; }
			++parentCount;
		}

		return parentCount;
	}

	@Override
	public Object3D findID(int userID) 
	{
		Object3D found = super.findID(userID);

		if ((found == null) && (activeCamera != null)) { found = activeCamera.findID(userID); }
		if ((found == null) && (background != null)) { found = background.findID(userID); }
		return found;
	}

	@Override
	public int applyAnimation(int time) 
	{
		int minValidity = super.applyAnimation(time);
		if ((background != null) && (minValidity > 0)) 
		{
			int validity = background.applyAnimation(time);
			minValidity = Math.min(validity, minValidity);
		}
		return minValidity;
	}

}
