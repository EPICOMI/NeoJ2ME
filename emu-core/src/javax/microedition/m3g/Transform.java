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

import java.lang.Math;
import java.util.Arrays;

public class Transform
{

	// This is a 4x4 matrix represented as a 16 item long array.
	// The items are in row major order:
	//   [  0,  1,  2,  3 ]
	//   [  4,  5,  6,  7 ]   Addressing in 2D vs in 1D:
	//   [  8,  9, 10, 11 ]     mat_2D[row][col] == mat_1D[4*row + col]
	//   [ 12, 13, 14, 15 ]
	private float[] matrix;

	/* ------------------------- public methods ------------------------- */
	public Transform()
	{
		this.setIdentity();
	}

	public Transform(Transform transform)
	{
		/* As per JSR-184, throw NullPointerException if the given transform is null. */
		if(transform == null) { throw new NullPointerException("Cannot initialize with a null transform."); }

		this.matrix = transform.matrix.clone();
	}

	public void get(float[] matrix)
	{
		/* As per JSR-184, throw NullPointerException if the given matrix is null.*/
		if(matrix == null) { throw new NullPointerException("Cannot copy the matrix contents to a null object."); }

		/* Also per JSR-184, throw IllegalArgumentException if the matrix length is less than 16.*/
		if(matrix.length < 16) { throw new IllegalArgumentException("The received matrix is not a valid 4x4 transform matrix."); }

		System.arraycopy(this.matrix, 0, matrix, 0, 16);
	}

	public void invert()
	{
		/* The inverse matrix is calculated by using an adapted version of the Laplace Expansion Theorem. */

		/*
		 * Since the matrix is a linear array, the logic is akin to C's pointer arithmethic 
		 * on matrices, where accesses to mat[row][col] becomes mat[4*row + col].
		 */
		float s0 = this.matrix[4*0 + 0] * this.matrix[4*1 + 1] - this.matrix[4*1 + 0] * this.matrix[4*0 + 1];
    	float s1 = this.matrix[4*0 + 0] * this.matrix[4*1 + 2] - this.matrix[4*1 + 0] * this.matrix[4*0 + 2];
    	float s2 = this.matrix[4*0 + 0] * this.matrix[4*1 + 3] - this.matrix[4*1 + 0] * this.matrix[4*0 + 3];
		float s3 = this.matrix[4*0 + 1] * this.matrix[4*1 + 2] - this.matrix[4*1 + 1] * this.matrix[4*0 + 2];
		float s4 = this.matrix[4*0 + 1] * this.matrix[4*1 + 3] - this.matrix[4*1 + 1] * this.matrix[4*0 + 3];
		float s5 = this.matrix[4*0 + 2] * this.matrix[4*1 + 3] - this.matrix[4*1 + 2] * this.matrix[4*0 + 3];

		float c0 = this.matrix[4*2 + 0] * this.matrix[4*3 + 1] - this.matrix[4*3 + 0] * this.matrix[4*2 + 1];
		float c1 = this.matrix[4*2 + 0] * this.matrix[4*3 + 2] - this.matrix[4*3 + 0] * this.matrix[4*2 + 2];
		float c2 = this.matrix[4*2 + 0] * this.matrix[4*3 + 3] - this.matrix[4*3 + 0] * this.matrix[4*2 + 3];
		float c3 = this.matrix[4*2 + 1] * this.matrix[4*3 + 2] - this.matrix[4*3 + 1] * this.matrix[4*2 + 2];
		float c4 = this.matrix[4*2 + 1] * this.matrix[4*3 + 3] - this.matrix[4*3 + 1] * this.matrix[4*2 + 3];
		float c5 = this.matrix[4*2 + 2] * this.matrix[4*3 + 3] - this.matrix[4*3 + 2] * this.matrix[4*2 + 3];
		
		/* 
		 * Check if the transform matrix can be inverted by calculating its determinant.
		 */
		float determinant = (float) 1.0 / (s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0);

		/* If it can't, throw ArithmeticException as per JSR-184. */
		if(determinant == 0) { throw new ArithmeticException("This transform matrix cannot be inverted."); }

		/* Calculate the inverse. */
		float[] inverseMatrix = new float[]
		{
			( this.matrix[4*1 + 1] * c5 - this.matrix[4*1 + 2] * c4 + this.matrix[4*1 + 3] * c3) * determinant,
			(-this.matrix[4*0 + 1] * c5 + this.matrix[4*0 + 2] * c4 - this.matrix[4*0 + 3] * c3) * determinant,
			( this.matrix[4*3 + 1] * s5 - this.matrix[4*3 + 2] * s4 + this.matrix[4*3 + 3] * s3) * determinant,
			(-this.matrix[4*2 + 1] * s5 + this.matrix[4*2 + 2] * s4 - this.matrix[4*2 + 3] * s3) * determinant,

			(-this.matrix[4*1 + 0] * c5 + this.matrix[4*1 + 2] * c2 - this.matrix[4*1 + 3] * c1) * determinant,
			( this.matrix[4*0 + 0] * c5 - this.matrix[4*0 + 2] * c2 + this.matrix[4*0 + 3] * c1) * determinant,
			(-this.matrix[4*3 + 0] * s5 + this.matrix[4*3 + 2] * s2 - this.matrix[4*3 + 3] * s1) * determinant,
			( this.matrix[4*2 + 0] * s5 - this.matrix[4*2 + 2] * s2 + this.matrix[4*2 + 3] * s1) * determinant,

			( this.matrix[4*1 + 0] * c4 - this.matrix[4*1 + 1] * c2 + this.matrix[4*1 + 3] * c0) * determinant,
			(-this.matrix[4*0 + 0] * c4 + this.matrix[4*0 + 1] * c2 - this.matrix[4*0 + 3] * c0) * determinant,
			( this.matrix[4*3 + 0] * s4 - this.matrix[4*3 + 1] * s2 + this.matrix[4*3 + 3] * s0) * determinant,
			(-this.matrix[4*2 + 0] * s4 + this.matrix[4*2 + 1] * s2 - this.matrix[4*2 + 3] * s0) * determinant,

			(-this.matrix[4*1 + 0] * c3 + this.matrix[4*1 + 1] * c1 - this.matrix[4*1 + 2] * c0) * determinant,
			( this.matrix[4*0 + 0] * c3 - this.matrix[4*0 + 1] * c1 + this.matrix[4*0 + 2] * c0) * determinant,
			(-this.matrix[4*3 + 0] * s3 + this.matrix[4*3 + 1] * s1 - this.matrix[4*3 + 2] * s0) * determinant,
			( this.matrix[4*2 + 0] * s3 - this.matrix[4*2 + 1] * s1 + this.matrix[4*2 + 2] * s0) * determinant
		};

		/* Make the inverse matrix be the transform's matrix. */
		this.matrix = inverseMatrix;
	}

	public void postMultiply(Transform transform)
	{
		/* As per JSR-184, throw NullPointerException if the given transform is null. */
		if(transform == null) { throw new NullPointerException("Cannot multiply by receiving a null transform."); }
	
		this.matrix = Transform.multiply(
			this.matrix,
			transform.matrix
		);
	}

	public void postRotate(float angle, float ax, float ay, float az)
	{
		this.postMultiplyTry(Transform.rotate(angle, ax, ay, az));
	}

	public void postRotateQuat(float qx, float qy, float qz, float qw)
	{
		this.postMultiplyTry(Transform.rotateQuat(qx, qy, qz, qw));
	}

	public void postScale(float sx, float sy, float sz)
	{
		this.postMultiplyTry(Transform.scale(sx, sy, sz));
	}

	public void postTranslate(float tx, float ty, float tz)
	{
		this.postMultiplyTry(Transform.translate(tx, ty, tz));
	}

	public void set(float[] matrix)
	{
		/* As per JSR-184, throw NullPointerException if the given matrix is null. */
		if(matrix == null) { throw new NullPointerException("Tried setting the transform with a null matrix."); }

		/* Also per JSR-184, IllegalArgumentException if matrix.length < 16 (not a 4x4 matrix). */
		if(matrix.length < 16) { throw new IllegalArgumentException("Cannot copy data from a matrix with less than 16 elements."); }

		System.arraycopy(matrix, 0, this.matrix, 0, 16);
	}

	public void set(Transform transform)
	{
		/* As per JSR-184, throw NullPointerException if the given transform is null. */
		if(transform == null) { throw new NullPointerException("Tried to set a null transform."); }

		this.matrix = transform.matrix.clone();
	}

	public void setIdentity()
	{
		this.matrix = new float[] {
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
		};
	}

	public void transform(float[] vectors)
	{
		/* As per JSR-184, throw NullPointerException if the given vector is null. */
		if(vectors == null) { throw new NullPointerException("Cannot transform a null vector."); }
		
		/* Also per JSR-184, throw IllegalArgumentException if the given vector is not a flat array of quadruplets. */
		if(vectors.length % 4 != 0) { throw new IllegalArgumentException("Cannot transform a vector array that's not multiple of 4."); }

		/* Multiply each 4D vector with this transform's matrix by quadruplets, hence the vector offset of 4. */
		for (int offset = 0; offset < vectors.length; offset += 4)
		{
			float[] result = new float[4];
			for (int row = 0; row < 4; row++)
			{
				result[row] =
					+ this.matrix[4*row + 0] * vectors[offset + 0]
					+ this.matrix[4*row + 1] * vectors[offset + 1]
					+ this.matrix[4*row + 2] * vectors[offset + 2]
					+ this.matrix[4*row + 3] * vectors[offset + 3];
			}
			System.arraycopy(result, 0, vectors, offset, 4);
		}
	}

	public void transform(VertexArray in, float[] out, boolean W)
	{
		/* As per JSR-184, throw NullPointerException if either 'in' or 'out' are null. */
		if(in == null || out == null) { System.exit(0); throw new NullPointerException("Cannot transform since input vertex array or output array are null."); }

		int vertexCount = in.getVertexCount();
		int vertexDims = in.getComponentCount();
		float[] components = new float[4]; /* Temp variables that hold the vector quadruplets (x,y,z,w). */

		/* Also per JSR-184, throw IllegalArgumentException if numComponents == 4 or out.length < (4 * vertexCount). */
		if (vertexDims == 4 || out.length < 4 * vertexCount) { System.exit(0); System.out.println("lal"); throw new IllegalArgumentException("Tried to transform an invalid vertex array."); }

		// Fill the `out` array with raw data
		if (in.getComponentType() == 1) 
		{
			byte[] vertices = new byte[vertexCount * vertexDims];
			in.get(0, vertexCount, vertices);

			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++)
			{
				int  in_offset = vertexIndex * vertexDims;
				int out_offset = vertexIndex * 4;

				components[0] =                  vertices[in_offset + 0]     ;
				components[1] = vertexDims > 1 ? vertices[in_offset + 1] : 0.0f;
				components[2] = vertexDims > 2 ? vertices[in_offset + 2] : 0.0f;
				components[3] = vertexDims > 3 ? vertices[in_offset + 3] : (W ? 1f : 0f);

				System.arraycopy(components, 0, out, out_offset, 4);
			}
		}
		else 
		{
			short[] vertices = new short[vertexCount * vertexDims];
			in.get(0, vertexCount, vertices);

			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++)
			{
				int  in_offset = vertexIndex * vertexDims;
				int out_offset = vertexIndex * 4;

				components[0] =                  vertices[in_offset + 0]     ;
				components[1] = vertexDims > 1 ? vertices[in_offset + 1] : 0.0f;
				components[2] = vertexDims > 2 ? vertices[in_offset + 2] : 0.0f;
				components[3] = vertexDims > 3 ? vertices[in_offset + 3] : (W ? 1f : 0f);

				System.arraycopy(components, 0, out, out_offset, 4);
			}
		}

		// Do the transformation on the raw data that is currently in `out`
		this.transform(out);
	}

	public void transpose()
	{
		float[] old = this.matrix.clone();

		/* Transposes the matrix column by column. */
		for (int i = 0; i < 16; i++) { this.matrix[4*(i/4) + (i%4)] = old[4*(i%4) + (i/4)]; }
	}

	/* ------------------------- package methods ------------------------- */

	// The pre* methods exist to facilitate chaining transformations.
	// They are mainly used in rendering.

	// package-private
	void preMultiply(Transform transform)
	{
		if (transform == null) { throw new java.lang.NullPointerException("preMultiply() called with null transform."); }
		this.matrix = Transform.multiply(
			transform.matrix,
			this.matrix
		);
	}

	// The two *MultiplyTry methods will silently ignore a null transform.
	// This is used to concisely ignore nulls, mostly in rendering.

	// package-private
	void preMultiplyTry(Transform transform)
	{
		if (transform != null) this.preMultiply(transform);
	}

	// package-private
	void postMultiplyTry(Transform transform)
	{
		if (transform != null) this.postMultiply(transform);
	}

	// package-private
	void preRotate(float angle, float ax, float ay, float az)
	{
		this.preMultiplyTry(Transform.rotate(angle, ax, ay, az));
	}

	// package-private
	void preRotateQuat(float qx, float qy, float qz, float qw)
	{
		this.preMultiplyTry(Transform.rotateQuat(qx, qy, qz, qw));
	}

	// package-private
	void preScale(float sx, float sy, float sz)
	{
		this.preMultiplyTry(Transform.scale(sx, sy, sz));
	}

	// package-private
	void preTranslate(float tx, float ty, float tz)
	{
		this.preMultiplyTry(Transform.translate(tx, ty, tz));
	}

	// package-private
	static Transform rotate(float angle, float ax, float ay, float az)
	{
		/* Only calculate rotation if the angle is not zero. */
		if (angle == 0) { return null; }
		/* As per JSR-184, throw IllegalArgumentException if the rotation axis is zero but the angle is not. */
			if(ax == 0 && ay == 0 && az == 0) { throw new IllegalArgumentException("The rotation axis is zero."); }

		// Compute sine and cosine of the angle
		double rad = Math.toRadians(angle);
		double s = Math.sin(rad);
		double c = Math.cos(rad);
		double d = 1 - c;

		// Normalize the axis
		double l = Math.sqrt(Math.pow(ax,2) + Math.pow(ay,2) + Math.pow(az,2));
		double x = ax / l;
		double y = ay / l;
		double z = az / l;

		double[] rotationMatrix = new double[] {
			x*x*d +  c ,  y*x*d - z*s,  z*x*d + y*s,  0,
			x*y*d + z*s,  y*y*d +  c ,  z*y*d - x*s,  0,
			x*z*d - y*s,  y*z*d + x*s,  z*z*d +  c ,  0,
			     0     ,       0     ,       0     ,  1
		};

		return new Transform(rotationMatrix);
	}

	// package-private
	static Transform rotateQuat(float qx, float qy, float qz, float qw)
	{
		/* As per JSR-184, throw IllegalArgumentException if all quaternion components are zero. */
		if(qx == 0 && qy == 0 && qz == 0 && qw == 0) { throw new IllegalArgumentException("Cannot rotate when all quaternion components are zero."); }

		// Normalize the quaternion
		double l = Math.sqrt(Math.pow(qx,2) + Math.pow(qy,2) + Math.pow(qz,2) + Math.pow(qw,2));
		double x = qx / l;
		double y = qy / l;
		double z = qz / l;
		double w = qw / l;

		double[] rotationMatrix = new double[] {
			1-2*y*y-2*z*z,    2*x*y-2*z*w,    2*x*z+2*y*w,  0,
			  2*x*y+2*z*w,  1-2*x*x-2*z*z,    2*y*z-2*x*w,  0,
			  2*x*z-2*y*w,    2*y*z+2*x*w,  1-2*x*x-2*y*y,  0,
			      0      ,        0      ,        0      ,  1
		};

		return new Transform(rotationMatrix);
	}

	// package-private
	static Transform scale(float sx, float sy, float sz)
	{
		/* Only scale if there's a change in scale on any of the axis. */
		if (sx == 1 && sy == 1 && sz == 1) { return null; }

		float[] scaleMatrix = new float[] {
			sx,  0,  0, 0,
			 0, sy,  0, 0,
			 0,  0, sz, 0,
			 0,  0,  0, 1
		};

		return new Transform(scaleMatrix);
	}

	// package-private
	static Transform translate(float tx, float ty, float tz)
	{
		/* Only translate if there's actual translation in any axis. */
		if (tx == 0 && ty == 0 && tz == 0) { return null; }

		float[] translationMatrix = new float[] {
			1, 0, 0, tx,
			0, 1, 0, ty,
			0, 0, 1, tz,
			0, 0, 0,  1
		};

		return new Transform(translationMatrix);
	}

	/* ------------------------- private methods ------------------------- */

	private Transform(float[] matrix)
	{
		this.matrix = matrix;
	}

	private Transform(double[] matrix)
	{
		this.matrix = new float[16];
		/* System.arraycopy cannot be used here due to type casting. Also there's no java function to implicitly do that like there is for Doubles. */
		for (int i = 0; i < 16; i++) { this.matrix[i] = (float) matrix[i];}
	}

	private static float[] multiply(float[] left, float[] right)
	{
		float[] result = new float[16];

		for (int row = 0; row < 4; row++)
		{
			for (int col = 0; col < 4; col++)
			{
				result[4*row + col] =
					left[4*row + 0] * right[4*0 + col] +
					left[4*row + 1] * right[4*1 + col] +
					left[4*row + 2] * right[4*2 + col] +
					left[4*row + 3] * right[4*3 + col];
			}
		}
		return result;
	}

}
