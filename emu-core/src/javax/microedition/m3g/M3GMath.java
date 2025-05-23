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

public class M3GMath 
{
	static final float EPSILON = Float.MIN_VALUE * 16f;

	public static float[] calculateNormal(float[] vector) 
	{
		float[] v1 = {vector[4 * 1 + 0] - vector[4 * 0 + 0], vector[4 * 1 + 1] - vector[4 * 0 + 1], vector[4 * 1 + 2] - vector[4 * 0 + 2]};
		float[] v2 = {vector[4 * 2 + 0] - vector[4 * 0 + 0], vector[4 * 2 + 1] - vector[4 * 0 + 1], vector[4 * 2 + 2] - vector[4 * 0 + 2]};
		return normalize(crossProduct(v1, v2));
	}

	public static void transformNormal(float[] normal, Transform transform) 
	{
		float[] transformMatrix = new float[16];
		float[] normalMatrix = new float[9]; // 3x3 normal matrix
		float[] transformedNormal = new float[4];

		transform.get(transformMatrix);
		
		// Extract the upper-left 3x3 part of the 4x4 transformation matrix
		for (int i = 0; i < 3; i++) 
		{
			for (int j = 0; j < 3; j++) 
			{
				normalMatrix[i * 3 + j] = transformMatrix[i * 4 + j];
			}
		}

		// Transform the normal
		transformedNormal[0] = normalMatrix[0] * normal[0] + normalMatrix[1] * normal[1] + normalMatrix[2] * normal[2];
		transformedNormal[1] = normalMatrix[3] * normal[0] + normalMatrix[4] * normal[1] + normalMatrix[5] * normal[2];
		transformedNormal[2] = normalMatrix[6] * normal[0] + normalMatrix[7] * normal[1] + normalMatrix[8] * normal[2];
		transformedNormal[3] = 0; // Homogeneous coordinate for normal is always 0

		float length = (float) Math.sqrt(transformedNormal[0] * transformedNormal[0] +
                                      transformedNormal[1] * transformedNormal[1] +
                                      transformedNormal[2] * transformedNormal[2]);

		if (length > 0) 
		{
			normal[0] = transformedNormal[0] / length;
			normal[1] = transformedNormal[1] / length;
			normal[2] = transformedNormal[2] / length;
		}
	}
	
	// Cross product
	public static float[] crossProduct(float[] a, float[] b) 
	{
		return new float[] 
		{
			a[1] * b[2] - a[2] * b[1],
			a[2] * b[0] - a[0] * b[2],
			a[0] * b[1] - a[1] * b[0]
		};
	}
	
	// Normalize a vector
	public static float[] normalize(float[] vector) 
	{
		float length = (float) Math.sqrt(dotProduct(vector, vector));
		if (length < EPSILON) { return new float[] {0, 0, 0}; } // Handle zero-length case
		return div(vector, length);
	}

	public static float[][] intersectTriangle(
		float[] p,
		float[] pn,
		float[] a,
		float[] b,
		float[] ta,
		float[] tb
	) {
		float pd, ad, bd, ratio;
		pd = dotProduct(p, pn);
		ad = dotProduct(a, pn);
		bd = dotProduct(b, pn);
		ratio = (pd - ad) / (bd - ad);
		return new float[][] 
		{
			add(a, mul(sub(b, a), ratio)),
			add(ta, mul(sub(tb, ta), ratio))
		};
	}

	public static float[] add(float[] a, float[] b)
	{
		if (a.length != b.length) { throw new java.lang.IllegalArgumentException(); }
		float[] out = new float[a.length];
		for (int i = 0; i < a.length; i++) { out[i] = a[i] + b[i]; }
		return out;
	}

	public static float[] sub(float[] a, float[] b)
	{
		return add(a, neg(b));
	}

	public static float[] mul(float[] a, float b)
	{
		float[] out = new float[a.length];
		for (int i = 0; i < a.length; i++) { out[i] = a[i] * b; }
		return out;
	}

	public static float[] div(float[] a, float b)
	{
		return mul(a, 1f / b);
	}

	public static float[] neg(float[] a)
	{
		float[] out = new float[a.length];
		for (int i = 0; i < a.length; i++) { out[i] = -1f * a[i]; }
		return out;
	}

	public static float dotProduct(float[] a, float[] b)
	{
		if (a.length != b.length) { throw new java.lang.IllegalArgumentException(); }
		float sum = 0;
		for (int i = 0; i < a.length; i++) { sum += a[i] * b[i]; }
		return sum;
	}

	public static void scaleVec(float[] vec, float s) 
	{
		for (int i = 0; i < vec.length; i++) { vec[i] *= s; }
	}

	public static void subVec(float[] vec, float[] other) 
	{
		if (vec.length != other.length) { throw new java.lang.IllegalArgumentException(); }
		for (int i = 0; i < vec.length; i++) { vec[i] -= other[i]; }
	}

	public static void addVec(float[] vec, float[] other) 
	{
		if (vec.length != other.length) { throw new java.lang.IllegalArgumentException(); }
		for (int i = 0; i < vec.length; i++) { vec[i] += other[i]; }
	}

	// Vector3 / float[3] helpers
	// For Vector3, the following disposition is used:
	// [0] = x
	// [1] = y
	// [2] = z
	public static void lerpVec3(int size, float[] vec, float s, float[] start, float[] end) 
	{
		float sCompl = 1.f - s;
		for (int i = 0; i < size; i++) { vec[i] = (sCompl * start[i]) + (s * end[i]); }
	}


	// QVec4 / float[4] helpers
	// For QVec4, the following disposition is used:
	// [0] = x
	// [1] = y
	// [2] = z
	// [3] = w
	public static void logDiffQuat(float[] orig, float[] from, float[] to) 
	{
		float[] temp = new float[4];
		temp[0] = -from[0];
		temp[1] = -from[1];
		temp[2] = -from[2];
		temp[3] = from[3];
		temp = mulQuat(to);
		orig = logQuat(temp);
	}

	public static float[] mulQuat(float[] other) 
	{
		float[] q = new float[4];
		q = other;
		float w = q[3] * other[3] - q[0] * other[0] - q[1] * other[1] - q[2] * other[2];
		float x = q[3] * other[0] + q[0] * other[3] + q[1] * other[2] - q[2] * other[1];
		float y = q[3] * other[1] - q[0] * other[2] + q[1] * other[3] + q[2] * other[0];
		float z = q[3] * other[2] + q[0] * other[1] - q[1] * other[0] + q[2] * other[3];
		return new float[] {x,y,z,w};
	}
	
	public static float[] logQuat(float[] quat) 
	{
		float sinTheta = (float) Math.sqrt(norm3(quat));
		float s, x, y, z;

		if (sinTheta > EPSILON) 
		{
			s = (float) (Math.atan2(sinTheta, quat[3]) / sinTheta);
			x = s * quat[0];
			y = s * quat[1];
			z = s * quat[2];
		} 
		else { x = y = z = 0.0f; }

		return new float[] {x,y,z,quat[3]};
	}

	public static float norm3(float[] quat) 
	{
		return (quat[0] * quat[0] + quat[1] * quat[1] + quat[2] * quat[2]);
	}

	public static float[] normalizeQuat(float[] vec4) 
	{
		float norm = (vec4[0] * vec4[0] + vec4[1] * vec4[1] + vec4[2] * vec4[2] + vec4[3] * vec4[3]);

		if (norm > EPSILON) 
		{ 
			norm = (float) (1.0d / Math.sqrt(norm));
			scaleVec(vec4, norm);
		} 
		else { return identityQuat(); }

		return vec4;
	}

	public static void expQuat(float[] vec4, float[] vec3Exp) 
	{
		float theta = (float) (Math.sqrt(vec3Exp[0] * vec3Exp[0] + vec3Exp[1] * vec3Exp[1] + vec3Exp[2] * vec3Exp[2]));

		if (theta > EPSILON) 
		{
			float s = (float) (Math.sin(theta) * (1.0d / (double) theta));
			vec4[0] = vec3Exp[0] * s;
			vec4[1] = vec3Exp[1] * s;
			vec4[2] = vec3Exp[2] * s;
			vec4[3] = (float) Math.cos(theta);
		} 
		else 
		{
			vec4[0] = vec4[1] = vec4[2] = 0.0f;
			vec4[3] = 1.0f;
		}
	}

	public static void slerpQuat(float[] orig, float s, float[] q0, float[] q1) {
		float s0, s1;
		float cosTheta = dotProduct(q0, q1);
		float oneMinusS = 1.0f - s;

		if (cosTheta > (EPSILON - 1.0f)) 
		{
			if (cosTheta < (1.0f - EPSILON)) 
			{
				float theta = (float) Math.acos((double) cosTheta);
				float sinTheta = (float) Math.sin(theta);
				s0 = (float) (Math.sin(oneMinusS * theta) / sinTheta);
				s1 = (float) (Math.sin(s * theta) / sinTheta);
			} 
			else 
			{
				s0 = oneMinusS;
				s1 = s;
			}
			orig[0] = s0 * q0[0] + s1 * q1[0];
			orig[1] = s0 * q0[1] + s1 * q1[1];
			orig[2] = s0 * q0[2] + s1 * q1[2];
			orig[3] = s0 * q0[3] + s1 * q1[3];
		} 
		else 
		{
			orig[0] = -q0[1];
			orig[1] = q0[0];
			orig[2] = -q0[3];
			orig[3] = q0[2];

			s0 = (float) Math.sin(oneMinusS * (Math.PI / 2));
			s1 = (float) Math.sin(s * (Math.PI / 2));

			orig[0] = s0 * q0[0] + s1 * orig[0];
			orig[1] = s0 * q0[1] + s1 * orig[1];
			orig[2] = s0 * q0[2] + s1 * orig[2];
		}
	}

	public static float[] identityQuat() { return new float[] { 0.0f, 0.0f, 0.0f, 1.0f }; }

	public static float[] setQuatRotation(float[] srcAxis, float[] targetAxis) 
	{
		float[] rot = new float[4];
		float[] cross = new float[3];
		float dot = srcAxis[0] * targetAxis[0] + srcAxis[1] * targetAxis[1] + srcAxis[2] * targetAxis[2];
	
		cross[0] = srcAxis[1] * targetAxis[2] - srcAxis[2] * targetAxis[1];
		cross[1] = srcAxis[2] * targetAxis[0] - srcAxis[0] * targetAxis[2];
		cross[2] = srcAxis[0] * targetAxis[1] - srcAxis[1] * targetAxis[0];
	
		float angle = (float) Math.acos(dot);
		float sinHalfAngle = (float) Math.sin(angle / 2);
	
		rot[0] = cross[0] * sinHalfAngle; // x
		rot[1] = cross[1] * sinHalfAngle; // y
		rot[2] = cross[2] * sinHalfAngle; // z
		rot[3] = (float) Math.cos(angle / 2); // w
	
		return rot;
	}

	public static void mulQuat(float[] q1, float[] q2, float[] result) 
	{
		result[0] = q1[3] * q2[0] + q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1]; // x
		result[1] = q1[3] * q2[1] + q1[1] * q2[3] + q1[2] * q2[0] - q1[0] * q2[2]; // y
		result[2] = q1[3] * q2[2] + q1[2] * q2[3] + q1[0] * q2[1] - q1[1] * q2[0]; // z
		result[3] = q1[3] * q2[3] - q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2]; // w
	}
}