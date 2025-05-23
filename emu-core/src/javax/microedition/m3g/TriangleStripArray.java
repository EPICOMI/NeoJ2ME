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

import java.util.Arrays;

public class TriangleStripArray extends IndexBuffer
{

	TriangleStripArray() { }

	public TriangleStripArray(int[] indices, int[] stripLengths)
	{
		/* Per JSR-184, throw NullPointerException if indices or stripLengths are null */
		if(indices == null || stripLengths == null) { throw new NullPointerException("Tried to construct a TriangleStripArray with incomplete information."); }
	
		/* Also per JSR-184, throw IllegalArgumentException if: 
		 * stripLengths is empty, any element in stripLengths is less than 3, or indices.length < sum(stripLengths). */
		if(stripLengths.length == 0 || indices.length < Arrays.stream(stripLengths).sum() || Arrays.stream(stripLengths).anyMatch(e -> e < 3))
			{ throw new IllegalArgumentException("Cannot construct TriangleStripArray, incorrect parameters received."); }

		/* also per JSR-184, throw IndexOutOfBoundsException if any element in indices is negative, or greater than 65535. */
		if(Arrays.stream(indices).anyMatch(e -> e < 0 || 65535 < e)) 
			{ throw new IndexOutOfBoundsException("Index provided to TriangleStripArray is out of bounds."); }

		/* Setup the StripArray with explicit indices. */
		this.updateFields(true, indices, stripLengths);
	}

	public TriangleStripArray(int firstIndex, int[] stripLengths)
	{
		/* As per JSR-184, throw NullPointerException if stripLengths == null. */
		if(stripLengths == null) { throw new NullPointerException("Tried to construct TriangleStripArray with null stripLengths."); }
	
		/* Also per JSR-184, throw IllegalArgumentException if stripLengths.length == 0 or any element in stripLengths is less than 3. */
		if(stripLengths.length == 0 || java.util.Arrays.stream(stripLengths).anyMatch(e -> e < 3)) 
			{ throw new IllegalArgumentException("Cannot construct TriangleStripArray, incorrect parameters received."); }

		/* Also per JSR-184, throw IndexOutOfBoundsException if any element in indices is negative, or if firstIndex + sum(stripLengths) is greater than 65535. */
		if(firstIndex < 0 || firstIndex + java.util.Arrays.stream(stripLengths).sum() > 65535)
			{ throw new IndexOutOfBoundsException("Index provided to TriangleStripArray is out of bounds."); }

		/* Setup the StripArray with implicit indices. */
		this.updateFields(false, new int[] { firstIndex }, stripLengths);
	}

	Object3D duplicateImpl() 
	{
		TriangleStripArray copy = new TriangleStripArray();
		copy.indexCount = indexCount;
		copy.indices = indices;
		return copy;
	}

	private void updateFields(boolean isExplicit, int[] indices, int[] stripLengths) 
	{
		/* Update the number of indices from the parent by mapping all valid StripLength elements. */
		super.indexCount = Arrays.stream(stripLengths).map(e -> (e - 2) * 3).sum();

		/* Update parent's indices by copying this object's indices to it. */
		super.indices = new int[this.indexCount];

		int  in_offset = 0;
		int out_offset = 0;

		for (int strip_id = 0; strip_id < stripLengths.length; strip_id++)
		{
			for (int i = 0; i < (stripLengths[strip_id] - 2); i++)
			{
				int x,y,z;
				int abs_index = in_offset + i;
				boolean swap = i % 2 == 1;

				if (isExplicit)
				{
					x = indices[abs_index + 0];
					y = indices[abs_index + 1];
					z = indices[abs_index + 2];
				}
				else
				{
					x = indices[0] + abs_index + 0;
					y = indices[0] + abs_index + 1;
					z = indices[0] + abs_index + 2;
				}

				// TODO determine correct way to swap vertices
				super.indices[out_offset + 0] = swap ? x : x;
				super.indices[out_offset + 1] = swap ? y : y;
				super.indices[out_offset + 2] = swap ? z : z;

				/* Move to the next vertex on the parent object. */
				out_offset += 3;
			}
			
			/* Move to the next vertex on this StripArray. */
			in_offset += stripLengths[strip_id];
		}
	}

}
