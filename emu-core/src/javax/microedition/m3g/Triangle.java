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
import java.util.stream.Stream;

// package-private
class Triangle
{
	static float[][] xp = new float[][] {{ 0, 0, 0, 0 }, {-1, 0, 0, 1 }};
	static float[][] xn = new float[][] {{ 0, 0, 0, 0 }, { 1, 0, 0, 1 }};
	static float[][] yp = new float[][] {{ 0, 0, 0, 0 }, { 0,-1, 0, 1 }};
	static float[][] yn = new float[][] {{ 0, 0, 0, 0 }, { 0, 1, 0, 1 }};
	static float[][] zp = new float[][] {{ 0, 0, 0, 0 }, { 0, 0,-1, 1 }};
	static float[][] zn = new float[][] {{ 0, 0, 0, 0 }, { 0, 0, 1, 1 }};

	// Let's reuse this when clipping, quite a bit faster than creating ArrayLists each time
	static final float[][] vert = new float[3][4];
	static final float[][] tex = new float[3][4];

	float[] v;
		// xA, yA, zA, wA,
		// xB, yB, zB, wB,
		// xC, yC, zC, wC;
		// 0   1   2   3

	float[] t;
		// sA, tA, rA, qA,
		// sB, tB, rB, qB,
		// sC, tC, rC, qC;
		// 0   1   2   3

	int[] bufIndex;

	int orientation = 1; // will be -1 in cases where triangles in a strip array have inverted winding to save memory by reusing vertices

	Triangle(float[] vertices, float[] texcoords, int[] indices)
	{
		this.v = vertices;
		this.t = texcoords;
		this.bufIndex = indices;
	}

	static Triangle[] fromVertAndTris(float[] vert, float[] texc, int[] tris)
	{
		Triangle[] result = new Triangle[tris.length / 3];

		for (int tri_id = 0; tri_id < tris.length / 3; tri_id++) 
		{
			result[tri_id] = new Triangle(new float[] //Vertex positions
			{
				vert[4 * tris[3 * tri_id + 0] + 0], // xA
				vert[4 * tris[3 * tri_id + 0] + 1], // yA
				vert[4 * tris[3 * tri_id + 0] + 2], // zA
				vert[4 * tris[3 * tri_id + 0] + 3], // wA
				vert[4 * tris[3 * tri_id + 1] + 0], // xB
				vert[4 * tris[3 * tri_id + 1] + 1], // yB
				vert[4 * tris[3 * tri_id + 1] + 2], // zB
				vert[4 * tris[3 * tri_id + 1] + 3], // wB
				vert[4 * tris[3 * tri_id + 2] + 0], // xC
				vert[4 * tris[3 * tri_id + 2] + 1], // yC
				vert[4 * tris[3 * tri_id + 2] + 2], // zC
				vert[4 * tris[3 * tri_id + 2] + 3]  // wC
			}, 
			texc == null ? null : new float[] // Tex Coordinates
			{
				texc[4 * tris[3 * tri_id + 0] + 0],
				texc[4 * tris[3 * tri_id + 0] + 1],
				texc[4 * tris[3 * tri_id + 0] + 2],
				texc[4 * tris[3 * tri_id + 0] + 3],
				texc[4 * tris[3 * tri_id + 1] + 0],
				texc[4 * tris[3 * tri_id + 1] + 1],
				texc[4 * tris[3 * tri_id + 1] + 2],
				texc[4 * tris[3 * tri_id + 1] + 3],
				texc[4 * tris[3 * tri_id + 2] + 0],
				texc[4 * tris[3 * tri_id + 2] + 1],
				texc[4 * tris[3 * tri_id + 2] + 2],
				texc[4 * tris[3 * tri_id + 2] + 3]
			},
			new int[] // IndexBuffer Indices
			{
				tris[3* tri_id + 0], //vA
				tris[3* tri_id + 1], //vB
				tris[3* tri_id + 2]  //vC
			});
		}

		// If a triangle reuses the last triangle's vertices for better memory efficiency, we'll have to handle them accordingly by inverting its orientation for culling.
		for (int tri_id = 1; tri_id < result.length; tri_id++) 
		{
			boolean sharesVertices = (result[tri_id].bufIndex[0] == result[tri_id - 1].bufIndex[1] &&
									  result[tri_id].bufIndex[1] == result[tri_id - 1].bufIndex[2]) ||
									 (result[tri_id].bufIndex[1] == result[tri_id - 1].bufIndex[0] &&
									  result[tri_id].bufIndex[2] == result[tri_id - 1].bufIndex[1]);
	
			// Invert orientation based on the previous triangle's
			if (sharesVertices) { result[tri_id].orientation = -result[tri_id - 1].orientation; }
		}

		return result;
	}

	float xA() { return this.v[4*0 + 0]; }
	float yA() { return this.v[4*0 + 1]; }
	float zA() { return this.v[4*0 + 2]; }
	float wA() { return this.v[4*0 + 3]; }
	float xB() { return this.v[4*1 + 0]; }
	float yB() { return this.v[4*1 + 1]; }
	float zB() { return this.v[4*1 + 2]; }
	float wB() { return this.v[4*1 + 3]; }
	float xC() { return this.v[4*2 + 0]; }
	float yC() { return this.v[4*2 + 1]; }
	float zC() { return this.v[4*2 + 2]; }
	float wC() { return this.v[4*2 + 3]; }

	float sA() { return this.t[4*0 + 0]; }
	float tA() { return this.t[4*0 + 1]; }
	float rA() { return this.t[4*0 + 2]; }
	float qA() { return this.t[4*0 + 3]; }
	float sB() { return this.t[4*1 + 0]; }
	float tB() { return this.t[4*1 + 1]; }
	float rB() { return this.t[4*1 + 2]; }
	float qB() { return this.t[4*1 + 3]; }
	float sC() { return this.t[4*2 + 0]; }
	float tC() { return this.t[4*2 + 1]; }
	float rC() { return this.t[4*2 + 2]; }
	float qC() { return this.t[4*2 + 3]; }

	Stream<Triangle> clip() 
	{	
		return Arrays.stream(new Triangle[] { this })
			.filter(Triangle::isValid)
			.flatMap(t -> 
			{	
				// Clip against each plane sequentially
				if (t.clipPlane(xp[0], xp[1]) == null)  { return Stream.empty(); }
				if (t.clipPlane(xn[0], xn[1]) == null)  { return Stream.empty(); }
				if (t.clipPlane(yp[0], yp[1]) == null)  { return Stream.empty(); }
				if (t.clipPlane(yn[0], yn[1]) == null)  { return Stream.empty(); }
				if (t.clipPlane(zp[0], zp[1]) == null)  { return Stream.empty(); }
				if (t.clipPlane(zn[0], zn[1]) == null)  { return Stream.empty(); }
	
				return Stream.of(t.project()); // If it passed all planes, it means it's at least partially visible, project it
			});
	}

	static void transform(Triangle[] triangles, Transform trVert, Transform trTex)
	{
		for (int i = 0; i < triangles.length; i++)
		{
			trVert.transform(triangles[i].v);
			if (triangles[i].t != null && trTex != null) { trTex.transform(triangles[i].t); }
		}
	}

	private boolean isValid()
	{
		return
			this.wA() >= M3GMath.EPSILON ||
			this.wB() >= M3GMath.EPSILON ||
			this.wC() >= M3GMath.EPSILON;
	}

	private Triangle project()
	{
		this.v[4*0 + 0] = this.v[4*0 + 0] / this.v[4*0 + 3];
		this.v[4*0 + 1] = this.v[4*0 + 1] / this.v[4*0 + 3];
		this.v[4*0 + 2] = this.v[4*0 + 2] / this.v[4*0 + 3];
		this.v[4*0 + 3] = 1f;
		this.v[4*1 + 0] = this.v[4*1 + 0] / this.v[4*1 + 3];
		this.v[4*1 + 1] = this.v[4*1 + 1] / this.v[4*1 + 3];
		this.v[4*1 + 2] = this.v[4*1 + 2] / this.v[4*1 + 3];
		this.v[4*1 + 3] = 1f;
		this.v[4*2 + 0] = this.v[4*2 + 0] / this.v[4*2 + 3];
		this.v[4*2 + 1] = this.v[4*2 + 1] / this.v[4*2 + 3];
		this.v[4*2 + 2] = this.v[4*2 + 2] / this.v[4*2 + 3];
		this.v[4*2 + 3] = 1f;

		this.t[4*0 + 0] = this.t[4*0 + 0] / this.t[4*0 + 3];
		this.t[4*0 + 1] = this.t[4*0 + 1] / this.t[4*0 + 3];
		this.t[4*0 + 2] = this.t[4*0 + 2] / this.t[4*0 + 3];
		this.t[4*0 + 3] = 1f;
		this.t[4*1 + 0] = this.t[4*1 + 0] / this.t[4*1 + 3];
		this.t[4*1 + 1] = this.t[4*1 + 1] / this.t[4*1 + 3];
		this.t[4*1 + 2] = this.t[4*1 + 2] / this.t[4*1 + 3];
		this.t[4*1 + 3] = 1f;
		this.t[4*2 + 0] = this.t[4*2 + 0] / this.t[4*2 + 3];
		this.t[4*2 + 1] = this.t[4*2 + 1] / this.t[4*2 + 3];
		this.t[4*2 + 2] = this.t[4*2 + 2] / this.t[4*2 + 3];
		this.t[4*2 + 3] = 1f;

		return this;
	}

	private Triangle clipPlane(float[] p, float[] pn)
	{
		pn = M3GMath.div(pn, (float) Math.sqrt(M3GMath.dotProduct(pn, pn)));

		vert[0] = Arrays.copyOfRange(this.v, 0, 4);
		vert[1] = Arrays.copyOfRange(this.v, 4, 8);
		vert[2] = Arrays.copyOfRange(this.v, 8, 12);

		tex[0] = Arrays.copyOfRange(this.t, 0, 4);
		tex[1] = Arrays.copyOfRange(this.t, 4, 8);
		tex[2] = Arrays.copyOfRange(this.t, 8, 12);

		for (int i = 0; i < 3; i++) 
		{
			if (M3GMath.dotProduct(pn, vert[i]) - M3GMath.dotProduct(pn, p) >= 0) { return this; } // Partially visible in this plane, move to next
			else { } // Test next vertex
		}
			
		return null; // If no vertex is inside, return a null object since the triangle isn't visible
	}

	// For perspective-correction
	void setTexCoords(float[] texCoordA, float[] texCoordB, float[] texCoordC) 
	{
        if (texCoordA.length != 4 || texCoordB.length != 4 || texCoordC.length != 4) 
		{
            throw new IllegalArgumentException("Each texture coordinate must have 4 elements (s, t, r, q).");
        }
        
        // Set the new texture coordinates
        System.arraycopy(texCoordA, 0, this.t, 0, 4); // sA, tA, rA, qA
        System.arraycopy(texCoordB, 0, this.t, 4, 4); // sB, tB, rB, qB
        System.arraycopy(texCoordC, 0, this.t, 8, 4); // sC, tC, rC, qC
    }

	public boolean isCounterClockwise() 
	{
		return ((xB() - xA()) * (yC() - yA()) - (xC() - xA()) * (yB() - yA())) * orientation < 0; // Clockwise if normal points towards the viewer (with corrected orientation)
	}
}

