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

import java.util.Vector;

public class Mesh extends Node
{

	private VertexBuffer vertices;
	private IndexBuffer[] submeshes;
	private Appearance[] appearances;

	protected Mesh() { }

	void duplicate(Mesh copy) 
	{
		super.duplicate((Node) copy);
		copy.vertices = vertices;
		copy.submeshes = new IndexBuffer[submeshes.length];
		copy.appearances = new Appearance[appearances.length];
		System.arraycopy(submeshes, 0, copy.submeshes, 0, submeshes.length);
		System.arraycopy(appearances, 0, copy.appearances, 0, appearances.length);
	}

	Object3D duplicateImpl() 
	{
		Mesh copy = new Mesh();
		duplicate((Mesh) copy);
		return copy;
	}

	@Override
	public int applyAnimation(int time) 
	{
		int minValidity = super.applyAnimation(time);
		int validity;
		if (vertices != null && minValidity > 0) 
		{
			validity = vertices.applyAnimation(time);
			minValidity = Math.min(validity, minValidity);
		}

		if (appearances != null) 
		{
			for (int i = 0; i < submeshes.length && minValidity > 0; i++) 
			{
				Appearance app = appearances[i];
				if (app != null) 
				{
					validity = app.applyAnimation(time);
					minValidity = Math.min(validity, minValidity);
				}
			}
		}

		return minValidity;
	}

	@Override
	public Object3D findID(int userID) 
	{
		Object3D found = super.findID(userID);

		if (found == null) { found = vertices.findID(userID); }
		for (int i = 0; (found == null) && (i < submeshes.length); i++) 
		{
			if (submeshes[i] != null) { found = submeshes[i].findID(userID);}
			if ((found == null) && (appearances[i] != null)) { found = appearances[i].findID(userID);}
		}
		return found;
	}

	public Mesh(VertexBuffer vertices, IndexBuffer submesh, Appearance appearance) 
	{
		if ((vertices == null) || (submesh == null)) { throw new NullPointerException(); }

		this.vertices = vertices;
		this.submeshes = new IndexBuffer[]{submesh};
		this.appearances = new Appearance[]{appearance};
	}

	public Mesh(VertexBuffer vertices, IndexBuffer[] submeshes, Appearance[] appearances) 
	{
		if ((vertices == null) || (submeshes == null) || hasArrayNullElement(submeshes)) 
		{
			throw new NullPointerException();
		}
		if ((submeshes.length == 0) || ((appearances != null) && (appearances.length < submeshes.length))) 
		{
			throw new IllegalArgumentException();
		}

		this.vertices = vertices;
		this.submeshes = new IndexBuffer[submeshes.length];
		this.appearances = new Appearance[submeshes.length];
		System.arraycopy(submeshes, 0, this.submeshes, 0, submeshes.length);
		if (appearances != null) { System.arraycopy(appearances, 0, this.appearances, 0, appearances.length); }
	}

	public Appearance getAppearance(int index) { return appearances[index]; }

	public IndexBuffer getIndexBuffer(int index) { return submeshes[index]; }

	public int getSubmeshCount() { return submeshes.length; }

	public VertexBuffer getVertexBuffer() { return vertices; }

	public void setAppearance(int index, Appearance appearance) { appearances[index] = appearance; }

	@Override
	public int doGetReferences(Object3D[] references) 
	{
		int parentCount = super.doGetReferences(references);

		if (vertices != null) 
		{
			if (references != null) { references[parentCount] = vertices; }
			++parentCount;
		}

		for (int i = 0; i < submeshes.length; ++i) 
		{
			if (references != null) { references[parentCount] = (Object3D) submeshes[i]; }
			++parentCount;
		}

		for (int i = 0; i < appearances.length; ++i) 
		{
			if (references != null) { references[parentCount] = (Object3D) appearances[i]; }
			++parentCount;
		}

		return parentCount;
	}

	private boolean hasArrayNullElement(IndexBuffer[] buffer) 
	{
		for (int i = 0; i < buffer.length; i++) 
		{
			if (buffer[i] == null) { return true; }
		}
		return false;
	}

}
