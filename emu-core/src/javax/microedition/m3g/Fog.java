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

import org.recompile.mobile.Mobile;

public class Fog extends Object3D
{

	public static final int	EXPONENTIAL = 80;
	public static final int LINEAR = 81;


	private float near = 0.0f;
	private float far = 1.0f;
	private int mode = LINEAR;
	private int color = 0x00000000;
	private float density = 1.0f;


	public Fog() {  }

	Object3D duplicateImpl() 
	{
		Fog copy = new Fog();
		copy.color = color;
		copy.mode = mode;
		copy.density = density;
		copy.near = near;
		copy.far = far;
		return copy;
	}

	public int getColor() { return color; }

	public float getDensity() { return density; }

	public float getFarDistance() { return far; }

	public int getMode() { return mode; }

	public float getNearDistance() { return near; }

	public void setColor(int RGB) { color = RGB; }

	public void setDensity(float value) 
	{
		if(density < 0) { throw new IllegalArgumentException("Invalid density value"); }
		density = value; 
	}

	public void setLinear(float Near, float Far)
	{
		// Convert values into the [0, 1] interval (if both are equal, the result is expected to be undefined)
		if(Far > Near)
		{
			near = Near/Far;
			far = Far/Far; 
		}
		else 
		{
			near = Near/Near;
			far = Far/Near; 
		}
	}

	public void setMode(int value) 
	{ 
		if(mode != LINEAR && mode != EXPONENTIAL) { throw new IllegalArgumentException("Fog only supports LINEAR and EXPONENTIAL types"); }
		mode = value; 
	}

	@Override
	void updateProperty(int property, float[] value) 
	{
		Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "AnimTrack updating fog property");
		switch (property) 
		{
			case AnimationTrack.COLOR:
				color = (int) value[0] >> 16 & (int) value[1] >> 8 & (int) value[2] & 0x00FFFFFF;
				break;
			case AnimationTrack.DENSITY:
				density = (value[0] < 0.f) ? 0.f : value[0];
				break;
			case AnimationTrack.FAR_DISTANCE:
				far = value[0];
				break;
			case AnimationTrack.NEAR_DISTANCE:
				near = value[0];
				break;
			default:
				super.updateProperty(property, value);
		}
	}

	boolean animTrackCompatible(AnimationTrack track) 
	{
		switch (track.getTargetProperty()) 
		{
			case AnimationTrack.COLOR:
			case AnimationTrack.DENSITY:
			case AnimationTrack.FAR_DISTANCE:
			case AnimationTrack.NEAR_DISTANCE:
				return true;
			default:
				return super.animTrackCompatible(track);
		}
	}
}
