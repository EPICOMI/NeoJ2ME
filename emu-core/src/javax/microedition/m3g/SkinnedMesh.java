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

public class SkinnedMesh extends Mesh
{

	public Group skeleton;

	public SkinnedMesh(VertexBuffer vertices, IndexBuffer[] submeshes, Appearance[] appearances, Group skeleton) 
	{
		super(vertices, submeshes, appearances);
		checkSkeleton(skeleton);
		this.skeleton = skeleton;
	}

	public SkinnedMesh(VertexBuffer vertices, IndexBuffer submeshes, Appearance appearances, Group skeleton) 
	{
		super(vertices, submeshes, appearances);
		checkSkeleton(skeleton);
		this.skeleton = skeleton;
	}

	private SkinnedMesh() { }

	Object3D duplicateImpl() 
	{
		Group skeleton = (Group) this.skeleton.duplicate();
		SkinnedMesh copy = new SkinnedMesh();
		super.duplicate((Mesh) copy);
		copy.skeleton = skeleton;
		return copy;
	}

	@Override
	public int doGetReferences(Object3D[] references) 
	{
		int num = super.doGetReferences(references);
		if (skeleton != null) 
		{
			if (references != null) { references[num] = skeleton; }
			num++;
		}
		return num;
	}

	@Override
	public Object3D findID(int userID) 
	{
		Object3D found = super.findID(userID);

		if ((found == null) && (skeleton != null)) { found = skeleton.findID(userID); }
		return found;
	}

	@Override
	public int applyAnimation(int time) 
	{
		int validity = super.applyAnimation(time);

		if (validity > 0) 
		{
			int validity2 = skeleton.applyAnimation(time);
			return Math.min(validity, validity2);
		}
		return 0;
	}

	public void addTransform(Node bone, int weight, int firstVertex, int numVertices) 
	{
		if (bone == null) { throw new NullPointerException(); }
		if ((weight <= 0) || (numVertices <= 0)) { throw new IllegalArgumentException(); }
		if ((firstVertex < 0) || (firstVertex + numVertices > 65535)) { throw new IndexOutOfBoundsException(); }
	}

	public void getBoneTransform(Node bone, Transform transform) 
	{
		if ((bone == null) || (transform == null)) { throw new NullPointerException(); }
	}

	public int getBoneVertices(Node bone, int[] indices, float[] weights) 
	{
		if (bone == null) { throw new NullPointerException(); }
		return 0;
	}

	public Group getSkeleton() { return skeleton; }

	private void checkSkeleton(Group skeleton) 
	{
		if (skeleton == null) { throw new NullPointerException(); }
		if (skeleton.getParent() != null) { throw new IllegalArgumentException("Skeleton already has a parent"); }
	}

}
