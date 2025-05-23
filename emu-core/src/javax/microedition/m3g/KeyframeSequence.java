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

public class KeyframeSequence extends Object3D
{

	public static final int CONSTANT = 192;
	public static final int LINEAR = 176;
	public static final int LOOP = 193;
	public static final int SLERP = 177;
	public static final int SPLINE = 178;
	public static final int SQUAD = 179;
	public static final int STEP = 180;


	private int duration;
	private int intType;
	private int keyframe;
	private int keyframes;
	private int repeat;
	private int rangeFirst;
	private int rangeLast;
	public int componentCount;
	private int nextKeyframe;
	private boolean dirty;

	private float[][] keyFrames;
	private float[][] inTangents;
	private float[][] outTangents;
	private int[] keyFrameTimes;
	private float[][] a;
	private float[][] b;


	public KeyframeSequence(int numKeyframes, int numComponents, int interpolation) 
	{  
		if ((numKeyframes < 1) || (numComponents < 1)) 
		{
			throw new IllegalArgumentException("Number of keyframes/components must be >= 1");
		}

		// Check given interpolation mode
		switch (interpolation) 
		{
			case SLERP:
				if (numComponents != 4)
					throw new IllegalArgumentException("SLERP and SQUAD mode requires 4 components in each keyframe");
				break;
			case SQUAD:
				if (numComponents != 4)
					throw new IllegalArgumentException("SLERP and SQUAD mode requires 4 components in each keyframe");
				a = new float[numKeyframes][4];
				b = new float[numKeyframes][4];
				break;
			case STEP:
				break;
			case LINEAR:
				break;
			case SPLINE:
				inTangents = new float[numKeyframes][numComponents];
				outTangents = new float[numKeyframes][numComponents];
				break;
			default:
				throw new IllegalArgumentException("Unknown interpolation mode");
		}

		this.keyframes = numKeyframes;
		this.componentCount = numComponents;
		this.intType = interpolation;

		// Initialize the sequence with default values
		keyFrames = new float[numKeyframes][numComponents];
		keyFrameTimes = new int[numKeyframes];
		rangeFirst = 0;
		rangeLast = numKeyframes - 1;
		dirty = true;
	}

	Object3D duplicateImpl() 
	{
		KeyframeSequence copy = new KeyframeSequence(keyframes, componentCount, intType);
		copy.repeat = repeat;
		copy.duration = duration;
		copy.rangeFirst = rangeFirst;
		copy.rangeLast = rangeLast;
		//copy.interpolationType = interpolationType;
		//copy.keyframeCount = keyframeCount;
		//copy.componentCount = componentCount;
		//copy.probablyNext = probablyNext;

		copy.keyFrames = new float[keyFrames.length][keyFrames[0].length];
		for (int i = 0; i < keyFrames.length; i++) { System.arraycopy(keyFrames[i], 0, copy.keyFrames[i], 0, keyFrames[i].length); }

		copy.keyFrameTimes = new int[keyFrameTimes.length];
		System.arraycopy(keyFrameTimes, 0, copy.keyFrameTimes, 0, keyFrameTimes.length);

		if (!dirty) 
		{
			copy.dirty = false;
			if (inTangents != null) 
			{
				copy.inTangents = new float[inTangents.length][inTangents[0].length];
				for (int i = 0; i < inTangents.length; i++) { System.arraycopy(inTangents[i], 0, copy.inTangents[i], 0, inTangents[i].length); }
				copy.outTangents = new float[outTangents.length][outTangents[0].length];
				for (int i = 0; i < outTangents.length; i++) { System.arraycopy(outTangents[i], 0, copy.outTangents[i], 0, inTangents[i].length); }
			}
			if (a != null) 
			{
				copy.a = new float[a.length][4];
				for (int i = 0; i < a.length; i++) { copy.a[i] = a[i]; }
				copy.b = new float[b.length][4];
				for (int i = 0; i < b.length; i++) { copy.b[i] = b[i]; }
			}
		} else { copy.dirty = true; }
		return copy;
	}


	public int getComponentCount() { return this.componentCount; }

	public float[] keyframeAt(int idx) { return this.keyFrames[idx]; }

	public int previousKeyframeIndex(int ind) 
	{
		if (ind == rangeFirst) { return rangeLast; }
		else if (ind == 0) { return keyframes - 1; }
		else { return ind - 1; }
	}

	public int nextKeyframeIndex(int ind) 
	{
		if (ind == rangeLast) { return rangeFirst; }
		else if (ind == (keyframes - 1)) { return 0; }
		else { return (ind + 1); }
	}

	public float[] keyframeBefore(int idx) { return keyframeAt(previousKeyframeIndex(idx)); }

	public float[] keyframeAfter(int idx) { return keyframeAt(nextKeyframeIndex(idx)); }

	public int timeDelta(int ind) 
	{
		if (ind == rangeLast) { return (duration - keyFrameTimes[rangeLast]) + keyFrameTimes[rangeFirst]; }

		return keyFrameTimes[nextKeyframeIndex(ind)] - keyFrameTimes[ind];
	}

	public float incomingTangentScale(int ind) 
	{
		if (repeat != LOOP && (ind == rangeFirst || ind == rangeLast)) { return 0; }
		else 
		{
			int prevind = previousKeyframeIndex(ind);
			return (((float) timeDelta(prevind) * 2.0f) / ((float) (timeDelta(ind) + timeDelta(prevind))));
		}
	}

	public float outgoingTangentScale(int ind) 
	{
		if (repeat != LOOP && (ind == rangeFirst || ind == rangeLast)) { return 0; }
		else 
		{
			int prevind = previousKeyframeIndex(ind);
			return (((float) timeDelta(ind) * 2.0f) / ((float) (timeDelta(ind) + timeDelta(prevind))));
		}
	}

	public float[] tangentTo(int idx) 
	{
		if (inTangents == null) { throw new NullPointerException("Cannot find tangent to keyframe if tangents are null"); }
		return inTangents[idx];
	}

	public float[] tangentFrom(int idx) 
	{
		if (outTangents == null) { throw new NullPointerException("Cannot find tangent from keyframe if tangents are null"); }
		return outTangents[idx];
	}

	public int getSample(int time, float[] sample) 
	{
		if (dirty) 
		{
			if (intType == SPLINE) 
			{
				int kf = rangeFirst;
				do 
				{
					float[] prev = keyframeBefore(kf);
					float[] next = keyframeAfter(kf);
					float sIn = incomingTangentScale(kf);
					float sOut = outgoingTangentScale(kf);
					float[] in = tangentTo(kf);
					float[] out = tangentFrom(kf);

					for (int i = 0; i < componentCount; i++) 
					{
						in[i] = ((0.5f * ((next[i] - prev[i]))) * sIn);
						out[i] = ((0.5f * ((next[i] - prev[i]))) * sOut);
					}

					kf = nextKeyframeIndex(kf);
				} while (kf != rangeFirst);
			} 
			else if (intType == SQUAD) 
			{
				int kf = rangeFirst;
				float[] start = new float[4];
				float[] end = new float[4];
				float[] prev = new float[4];
				float[] next = new float[4];
				float[] tempq = new float[4];
				float[] tempv = new float[3];
				float[] cfd = new float[3];
				float[] tangent = new float[3];
				do 
				{
					prev = keyframeBefore(kf);
					start = keyframeAt(kf);
					end = keyframeAfter(kf);
					next = keyframeAfter(nextKeyframeIndex(kf));

					M3GMath.logDiffQuat(cfd, start, end);
					M3GMath.logDiffQuat(tempv, prev, start);
					M3GMath.addVec(cfd, tempv);
					M3GMath.scaleVec(cfd, 0.5f);

					tangent = cfd;
					M3GMath.scaleVec(tangent, outgoingTangentScale(kf));

					M3GMath.logDiffQuat(tempv, start, end);
					M3GMath.subVec(tangent, tempv);
					M3GMath.scaleVec(tangent, 0.5f);
					tempq[0] = tempv[0];
					tempq[1] = tempv[1];
					tempq[2] = tempv[2];
					M3GMath.expQuat(tempq, tangent);
					a[kf] = start;
					a[kf] = M3GMath.mulQuat(tempq);

					tangent = cfd;
					M3GMath.scaleVec(tangent, incomingTangentScale(kf));

					tempv[0] = tempq[0];
					tempv[1] = tempq[1];
					tempv[2] = tempq[2];
					M3GMath.logDiffQuat(tempv, prev, start);
					M3GMath.subVec(tempv, tangent);
					M3GMath.scaleVec(tempv, 0.5f);
					tempq[0] = tempv[0];
					tempq[1] = tempv[1];
					tempq[2] = tempv[2];
					M3GMath.expQuat(tempq, tempv);
					b[kf] = start;
					b[kf] = M3GMath.mulQuat(tempq);

					kf = nextKeyframeIndex(kf);
				} while (kf != rangeFirst);
			}
			dirty = false;
			nextKeyframe = rangeFirst;
		}

		if (repeat == LOOP) 
		{
			if (time < 0) { time = (time % duration) + duration; }
			else { time = time % duration; }

			if (time < keyFrameTimes[rangeFirst]) { time += duration; }
		} 
		else 
		{
			if (time < keyFrameTimes[rangeFirst]) 
			{
				float[] value = keyframeAt(rangeFirst);
				for (int i = 0; i < componentCount; i++) { sample[i] = value[i]; }
				return (keyFrameTimes[rangeFirst] - time);
			}
			else if (time >= keyFrameTimes[rangeLast]) 
			{
				float[] value = keyframeAt(rangeLast);
				for (int i = 0; i < componentCount; i++) { sample[i] = value[i]; }
				return 0x7FFFFFFF;
			}
		}

		int start = nextKeyframe;
		if (keyFrameTimes[start] > time) { start = rangeFirst; }
		while (start != rangeLast && keyFrameTimes[nextKeyframeIndex(start)] <= time) { start = nextKeyframeIndex(start); }
		nextKeyframe = start;

		if (time == keyFrameTimes[start] || intType == STEP) 
		{
			float[] value = keyframeAt(start);
			for (int i = 0; i < componentCount; i++) { sample[i] = value[i]; }
			return (intType == STEP) ? (timeDelta(start) - (time - keyFrameTimes[start])) : 1;
		}

		float s = ((time - keyFrameTimes[start]) / (float) timeDelta(start));

		int end = nextKeyframeIndex(start);
		float[] Start;
		float[] End;
		float[] temp;
		float[] tStart;
		float[] tEnd;
		float s2;
		float s3;
		float[] q0;
		float[] q1;
		float[] sampl;
		float[] temp0;
		float[] temp1;
		float[] A;
		float[] B;
		switch (intType) 
		{
			case LINEAR:
				Start = keyframeAt(start);
				End = keyframeAt(end);
				M3GMath.lerpVec3(componentCount, sample, s, Start, End);
				break;
			case SLERP:
				if (componentCount != 4) { throw new IllegalStateException(); }
				q0 = new float[4];
				q1 = new float[4];
				sampl = new float[4];

				q0 = keyframeAt(start);
				q1 = keyframeAt(end);
				sampl = sample;

				M3GMath.slerpQuat(sampl, s, q0, q1);
				sample[0] = sampl[0];
				sample[1] = sampl[1];
				sample[2] = sampl[2];
				sample[3] = sampl[3];
				// may be not necessary
				temp = keyframeAt(start);
				temp[0] = q0[0];
				temp[1] = q0[1];
				temp[2] = q0[2];
				temp[3] = q0[3];
				temp = keyframeAt(end);
				temp[0] = q1[0];
				temp[1] = q1[1];
				temp[2] = q1[2];
				temp[3] = q1[3];
				break;
			case SPLINE:
				Start = keyframeAt(start);
				End = keyframeAt(end);
				tStart = tangentFrom(start);
				tEnd = tangentTo(end);

				s2 = s * s;
				s3 = s2 * s;

				for (int i = 0; i < componentCount; i++) { sample[i] = (Start[i] * (((s3 * 2) - (3.f * s2)) + 1.f) + (End[i] * ((3.f * s2) - (s3 * 2)) + (tStart[i] * ((s3 - (s2 * 2)) + s) + (tEnd[i] * (s3 - s2))))); }
				break;
			case SQUAD:
				if (componentCount != 4)
					throw new IllegalStateException();
				temp0 = new float[4];
				temp1 = new float[4];
				q0 = new float[4];
				q1 = new float[4];
				//A = new float[4];
				//B = new float[4];
				sampl = new float[4];

				q0 = keyframeAt(start);
				q1 = keyframeAt(end);
				//A.setQuat(a[start]);
				//B.setQuat(b[end]);
				sampl = sample;
				M3GMath.slerpQuat(temp0, s, q0, q1);
				M3GMath.slerpQuat(temp1, s, a[start], b[end]);
				M3GMath.slerpQuat(sampl, ((s * (1.0f - s)) * 2), temp0, temp1);
				sample[0] = sampl[0];
				sample[1] = sampl[1];
				sample[2] = sampl[2];
				sample[3] = sampl[3];
				// may be not necessary
				temp = keyframeAt(start);
				temp[0] = q0[0];
				temp[1] = q0[1];
				temp[2] = q0[2];
				temp[3] = q0[3];
				temp = keyframeAt(end);
				temp[0] = q1[0];
				temp[1] = q1[1];
				temp[2] = q1[2];
				temp[3] = q1[3];
				/*temp = a[start];
                temp[0] = A[0];
				temp[1] = A[1];
				temp[2] = A[2];
				temp[3] = A[3];
				temp = b[end];
				temp[0] = B[0];
				temp[1] = B[1];
				temp[2] = B[2];
				temp[3] = B[3];*/
				break;
			default:
				throw new IllegalStateException();
		}
		return 1;
	}

	public int getDuration() { return duration; }

	public int getInterpolationType() { return intType; }

	public int getKeyframe(int index, float[] value) 
	{ 
		if ((index < 0) || (index >= keyframes)) { throw new IndexOutOfBoundsException(); }

		if ((value != null) && (value.length < componentCount)) { throw new IllegalArgumentException(); }

		if (value != null) { System.arraycopy(keyFrames[index], 0, value, 0, componentCount); }

		return keyFrameTimes[index];
	}

	public int getKeyframeCount() { return keyframes; }

	public int getRepeatMode() { return repeat; }

	public int getValidRangeFirst() { return rangeFirst; }

	public int getValidRangeLast() { return rangeLast; }

	public void setDuration(int value) 
	{ 
		duration = value;
		dirty = true;
	}

	public void setKeyframe(int index, int time, float[] value) 
	{ 
		if (value == null) { throw new NullPointerException("Keyframe value vector must not be null"); }
		if ((index < 0) || (index >= keyframes))  { throw new IndexOutOfBoundsException(); }
		if ((value.length < componentCount) || (time < 0)) { throw new IllegalArgumentException(); }

		System.arraycopy(value, 0, keyFrames[index], 0, componentCount);
		keyFrameTimes[index] = time;
		if (intType == SLERP || intType == SQUAD) 
		{
			float[] q = new float[4];
			float[] kf = keyframeAt(index);
			q = kf;
			q = M3GMath.normalizeQuat(q);
			kf[0] = q[0];
			kf[1] = q[1];
			kf[2] = q[2];
			kf[3] = q[3];
		}
		dirty = true;
	}

	public void setRepeatMode(int mode) { repeat=mode; }

	public void setValidRange(int first, int last) 
	{
		if ((first < 0) || (first >= keyframes) || (last < 0) || (last >= keyframes)) 
		{
			throw new IndexOutOfBoundsException("Invalid range");
		}
		rangeFirst=first; 
		rangeLast=last;
		dirty=true;
	}

}
