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

public class RayIntersection
{

	private Node intersected = null;
	private float distance = 0.f;
	private int submeshIndex = 0;
	private float[] textureS = new float[Graphics3D.getTextureUnitCount()];
	private float[] textureT = new float[Graphics3D.getTextureUnitCount()];
	private float[] normal = new float[3];
	private float[] ray = new float[6];

	public RayIntersection() 
	{
		normal[0] = 0.f;
		normal[1] = 0.f;
		normal[2] = 1.f;

		ray[0] = 0.f;
		ray[1] = 0.f;
		ray[2] = 0.f;
		ray[3] = 0.f;
		ray[4] = 0.f;
		ray[5] = 1.f;
	}

	public Node getIntersected() { return intersected; }

	public float getDistance() { return distance; }

	public int getSubmeshIndex() { return submeshIndex; }

	public float getTextureS(int index) 
	{
		if (index < 0 || index >= textureS.length) { throw new IndexOutOfBoundsException(); }

		return textureS[index];
	}

	public float getTextureT(int index) 
	{
		if (index < 0 || index >= textureT.length) { throw new IndexOutOfBoundsException(); }

		return textureT[index];
	}

	public float getNormalX() { return normal[0]; }

	public float getNormalY() { return normal[1]; }

	public float getNormalZ() { return normal[2]; }

	public void getRay(float[] ray) 
	{
		if (ray.length < 6) { throw new IllegalArgumentException(); }

		ray[0] = this.ray[0];
		ray[1] = this.ray[1];
		ray[2] = this.ray[2];
		ray[3] = this.ray[3];
		ray[4] = this.ray[4];
		ray[5] = this.ray[5];
	}

	static float[] createResult() 
	{
		return new float[1 + 1 + 2 * Graphics3D.getTextureUnitCount() + 3 + 6];
	}

}
