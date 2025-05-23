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

public class VertexArray extends Object3D
{

	private byte[][] inner1;
	private short[][] inner2;
	private int numVertices;
	private int numComponents;
	private int componentType;

	VertexArray() { }

	public VertexArray(int numVertices, int numComponents, int componentSize)
	{
		/* As per JSR-184, throw IllegalArgumentException if any of the parameters are outside of their allowed range. */
		if((numVertices < 1 || numVertices > 65535) || (numComponents < 2 || numComponents > 4) || (componentSize < 1 || componentSize > 2))
			{ throw new IllegalArgumentException("Vertex Array parameter is out of bounds."); }

		this.numVertices = numVertices;
		this.numComponents = numComponents;
		this.componentType = componentSize;
		switch (componentSize)
		{
			case 1:
				this.inner1 = new byte[numVertices][numComponents];
				this.inner2 = null;
				break;
			case 2:
				this.inner1 = null;
				this.inner2 = new short[numVertices][numComponents];
				break;
		}
	}

	Object3D duplicateImpl()
	 {
		VertexArray copy = new VertexArray();
		copy.numVertices = numVertices;
		copy.numComponents = numComponents;
		copy.componentType = componentType;
		System.arraycopy(this.inner1, 0, copy.inner1, 0, inner1.length);
		System.arraycopy(this.inner2, 0, copy.inner2, 0, inner2.length);
		return copy;
	}


	public void get(int firstVertex, int numVertices, byte[] values)
	{
		/* As per JSR-184, throw: 
		 * NullPointerException if values is null.
		 * IllegalStateException if the commponentType specifies usage of 16-bit vertex attributes.
		 * IllegalArgumentException if numVertices < 0 or values.length < numVertices * getComponentCount
		 * IndexOutOfBoundsException if firstVertex < 0 or firstVertex + numVertices > getVertexCount
		 */
		if(values == null) { throw new NullPointerException("Cannot return the values into a null array."); }
		if(this.componentType != 1) { throw new IllegalStateException("The set componentType is meant for 16-bit attributes, not 8-bit."); }
		if(numVertices < 0 || values.length < numVertices * this.numComponents) 
			{ throw new IllegalArgumentException("Tried using negative number of vertices or incorrect array size."); }
		if(firstVertex < 0 || firstVertex + numVertices > this.numVertices) 
			{ throw new IndexOutOfBoundsException("Tried to get a range of values that's out of bounds."); }

		for (int vid = 0; vid < numVertices; vid++)
		{
			for (int cid = 0; cid < this.numComponents; cid++)
			{
				int abs_vid = vid + firstVertex;
				int flat_id = vid * this.numComponents + cid;
				values[flat_id] = this.inner1[abs_vid][cid];
			}
		}
	}

	public void get(int firstVertex, int numVertices, short[] values)
	{
		/* As per JSR-184, throw: 
		 * NullPointerException if values is null.
		 * IllegalStateException if the commponentType specifies usage of 8-bit vertex attributes.
		 * IllegalArgumentException if numVertices < 0 or values.length < numVertices * getComponentCount
		 * IndexOutOfBoundsException if firstVertex < 0 or firstVertex + numVertices > getVertexCount
		 */
		if(values == null) { throw new NullPointerException("Cannot return the values into a null array."); }
		if(this.componentType != 2) { throw new IllegalStateException("The set componentType is meant for 8-bit attributes, not 16-bit."); }
		if(numVertices < 0 || values.length < numVertices * this.numComponents) 
			{ throw new IllegalArgumentException("Tried using negative number of vertices or incorrect array size."); }
		if(firstVertex < 0 || firstVertex + numVertices > this.numVertices) 
			{ throw new IndexOutOfBoundsException("Tried to get a range of values that's out of bounds."); }

		for (int vid = 0; vid < numVertices; vid++)
		{
			for (int cid = 0; cid < this.numComponents; cid++)
			{
				int abs_vid = vid + firstVertex;
				int flat_id = vid * this.numComponents + cid;
				values[flat_id] = this.inner2[abs_vid][cid];
			}
		}
	}

	public int getComponentCount() { return this.numComponents; }

	public int getComponentType() { return this.componentType; }

	public int getVertexCount() { return this.numVertices; }

	public void set(int firstVertex, int numVertices, byte[] values)
	{
		/* As per JSR-184, throw: 
		 * NullPointerException if values is null.
		 * IllegalStateException if the commponentType specifies usage of 16-bit vertex attributes.
		 * IllegalArgumentException if numVertices < 0 or values.length < numVertices * getComponentCount
		 * IndexOutOfBoundsException if firstVertex < 0 or firstVertex + numVertices > getVertexCount
		 */
		if(values == null) { throw new NullPointerException("Cannot return the values into a null array."); }
		if(this.componentType != 1) { throw new IllegalStateException("The set componentType is meant for 16-bit attributes, not 8-bit."); }
		if(numVertices < 0 || values.length < numVertices * this.numComponents) 
			{ throw new IllegalArgumentException("Tried using negative number of vertices or incorrect array size."); }
		if(firstVertex < 0 || firstVertex + numVertices > this.numVertices) 
			{ throw new IndexOutOfBoundsException("Tried to get a range of values that's out of bounds."); }

		for (int vid = 0; vid < numVertices; vid++)
		{
			for (int cid = 0; cid < this.numComponents; cid++)
			{
				int abs_vid = vid + firstVertex;
				int flat_id = vid * this.numComponents + cid;
				this.inner1[abs_vid][cid] = values[flat_id];
			}
		}
	}

	public void set(int firstVertex, int numVertices, short[] values)
	{
		/* As per JSR-184, throw: 
		 * NullPointerException if values is null.
		 * IllegalStateException if the commponentType specifies usage of 8-bit vertex attributes.
		 * IllegalArgumentException if numVertices < 0 or values.length < numVertices * getComponentCount
		 * IndexOutOfBoundsException if firstVertex < 0 or firstVertex + numVertices > getVertexCount
		 */
		if(values == null) { throw new NullPointerException("Cannot return the values into a null array."); }
		if(this.componentType != 2) { throw new IllegalStateException("The set componentType is meant for 8-bit attributes, not 16-bit."); }
		if(numVertices < 0 || values.length < numVertices * this.numComponents) 
			{ throw new IllegalArgumentException("Tried using negative number of vertices or incorrect array size."); }
		if(firstVertex < 0 || firstVertex + numVertices > this.numVertices) 
			{ throw new IndexOutOfBoundsException("Tried to get a range of values that's out of bounds."); }

		for (int vid = 0; vid < numVertices; vid++)
		{
			for (int cid = 0; cid < this.numComponents; cid++)
			{
				int abs_vid = vid + firstVertex;
				int flat_id = vid * this.numComponents + cid;
				this.inner2[abs_vid][cid] = values[flat_id];
			}
		}
	}

}
