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


public class CompositingMode extends Object3D
{

	public static final int ALPHA = 64;
	public static final int ALPHA_ADD = 65;
	public static final int MODULATE = 66;
	public static final int MODULATE_X2 = 67;
	public static final int REPLACE = 68;


	private int blending;
	private float alphaThreshold;
	private boolean alphaWrite;
	private boolean depthWrite;
	private boolean depthTest;
	private float depthOffsetUnits;
	private float depthOffsetFactor;
	private boolean colorWrite;


	public CompositingMode()
	{
			this.blending = REPLACE;
			this.alphaThreshold = 0f;
			this.depthOffsetUnits = 0f;
			this.depthOffsetFactor = 0f;
			this.depthTest = true;
			this.depthWrite = true;
			this.colorWrite = true;
			this.alphaWrite = true;
	}

	Object3D duplicateImpl() 
	{
		CompositingMode copy = new CompositingMode();
		copy.depthTest = depthTest;
		copy.depthWrite = depthWrite;
		copy.colorWrite = colorWrite;
		copy.alphaWrite = alphaWrite;
		copy.blending = blending;
		copy.alphaThreshold = alphaThreshold;
		copy.depthOffsetFactor = depthOffsetFactor;
		copy.depthOffsetUnits = depthOffsetUnits;
		return copy;
	}


	public float getAlphaThreshold()
	{
		return this.alphaThreshold;
	}

	public int getBlending()
	{
		return this.blending;
	}

	public float getDepthOffsetFactor()
	{
		return this.depthOffsetFactor;
	}

	public float getDepthOffsetUnits()
	{
		return this.depthOffsetUnits;
	}

	public boolean isAlphaWriteEnabled()
	{
		return this.alphaWrite;
	}

	public boolean isColorWriteEnabled()
	{
		return this.colorWrite;
	}

	public boolean isDepthTestEnabled()
	{
		return this.depthTest;
	}

	public boolean isDepthWriteEnabled()
	{
		return this.depthWrite;
	}

	public void setAlphaThreshold(float threshold)
	{
		if (threshold < 0 || 1 < threshold)
			throw new java.lang.IllegalArgumentException();

		this.alphaThreshold = threshold;
	}

	public void setAlphaWriteEnable(boolean enable)
	{
		this.alphaWrite = enable;
	}

	public void setBlending(int mode)
	{
		if (mode != ALPHA &&
			mode != ALPHA_ADD &&
			mode != MODULATE &&
			mode != MODULATE_X2 &&
			mode != REPLACE)
			throw new java.lang.IllegalArgumentException();

		this.blending = mode;
	}

	public void setColorWriteEnable(boolean enable)
	{
		this.colorWrite = enable;
	}

	public void setDepthOffset(float factor, float units)
	{
		this.depthOffsetFactor = factor;
		this.depthOffsetUnits = units;
	}

	public void setDepthTestEnable(boolean enable)
	{
		this.depthTest = enable;
	}

	public void setDepthWriteEnable(boolean enable)
	{
		this.depthWrite = enable;
	}

}
