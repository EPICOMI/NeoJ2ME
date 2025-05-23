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

public class Material extends Object3D
{

	public static final int AMBIENT = 1024;
	public static final int DIFFUSE = 2048;
	public static final int EMISSIVE = 4096;
	public static final int SPECULAR = 8192;

	private int ambientColor;
	private int diffuseColor;
	private int emissiveColor;
	private int specularColor;
	private float shininess;
	private boolean tracking;

	public Material() 
	{  
		this.tracking = false;
		this.ambientColor = 0x00333333;
		this.diffuseColor = 0xFFCCCCCC;
		this.emissiveColor = 0x00000000;
		this.specularColor = 0x00000000;
		this.shininess = 0f;
	}

	Object3D duplicateImpl() {
		Material copy = new Material();
		copy.ambientColor = ambientColor;
		copy.diffuseColor = diffuseColor;
		copy.emissiveColor = emissiveColor;
		copy.specularColor = specularColor;
		copy.shininess = shininess;
		copy.tracking = tracking;
		return copy;
	}


	public int getColor(int target) 
	{ 
		/* As per JSR-184, throw IllegalArgumentException if target has a value other than AMBIENT, DIFFUSSE, EMISSIVE or SPECULAR. */
		if(target != AMBIENT || target != DIFFUSE || target != EMISSIVE || target != SPECULAR) 
			{ throw new IllegalArgumentException("Tried to get invalid color component from material."); }
		
		switch(target)
		{
			case AMBIENT:
				return this.ambientColor; 
			case DIFFUSE:
				return this.diffuseColor;
			case EMISSIVE:
				return this.emissiveColor;
			case SPECULAR:
				return this.specularColor;
		}

		return this.ambientColor; 
	}

	public float getShininess() { return this.shininess; }

	public boolean isVertexColorTrackingEnabled() { return this.tracking; }

	public void setColor(int target, int ARGB) 
	{ 
		/* As per JSR-184, throw IllegalArgumentException if target has a value other than an inclusive OR of one or more of AMBIENT, DIFFUSE, EMISSIVE, SPECULAR. */
		if((target & ~(AMBIENT | DIFFUSE | EMISSIVE | SPECULAR)) != 0) 
			{throw new IllegalArgumentException("Trying to set material color on invalid material component."); }
		

		if ((target & AMBIENT)  != 0) { this.ambientColor = ARGB;  }
		if ((target & DIFFUSE)  != 0) { this.diffuseColor = ARGB;  }
		if ((target & EMISSIVE) != 0) { this.emissiveColor = ARGB; }
		if ((target & SPECULAR) != 0) { this.specularColor = ARGB; }
	}

	public void setShininess(float shininess) 
	{ 
		/* As per JSR-184, throw IllegalArgumentException if shininess > 128(1f) or < 0(0f). */
		//if(shininess < 0f || shininess > 1f) { throw new IllegalArgumentException("Material received invalid shininess value:" + shininess); }
		
		if(shininess < 0f) { shininess = 0f; }
		else if (shininess > 1f) { shininess = 1f; }
		
		this.shininess = shininess; 
	}

	public void setVertexColorTrackingEnable(boolean enable) { this.tracking = enable; }

	@Override
	void updateProperty(int property, float[] value) 
	{
		Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "AnimTrack updating material property");
		switch (property) 
		{
			case AnimationTrack.ALPHA:
				diffuseColor = (diffuseColor | 0xFF000000) & ((int) value[0] << 24);
				break;
			case AnimationTrack.AMBIENT_COLOR:
				ambientColor = (int) value[0] >> 16 & (int) value[1] >> 8 & (int) value[2];
				break;
			case AnimationTrack.DIFFUSE_COLOR:
				diffuseColor = (diffuseColor | 0x00FFFFFF) & ((int) value[0] >> 16 & (int) value[1] >> 8 & (int) value[2]);
				break;
			case AnimationTrack.EMISSIVE_COLOR:
				emissiveColor = ((int) value[0] >> 16 & (int) value[1] >> 8 & (int) value[2] & 0x00FFFFFF);
				break;
			case AnimationTrack.SHININESS:
				shininess = Math.max(0.f, Math.min(128.f, value[0]));
				break;
			case AnimationTrack.SPECULAR_COLOR:
				specularColor = (int) value[0] >> 16 & (int) value[1] >> 8 & (int) value[2];
				break;
			default:
				super.updateProperty(property, value);
		}
	}

	boolean animTrackCompatible(AnimationTrack track) 
	{
		switch (track.getTargetProperty()) {
			case AnimationTrack.ALPHA:
			case AnimationTrack.AMBIENT_COLOR:
			case AnimationTrack.DIFFUSE_COLOR:
			case AnimationTrack.EMISSIVE_COLOR:
			case AnimationTrack.SHININESS:
			case AnimationTrack.SPECULAR_COLOR:
				return true;
			default:
				return super.animTrackCompatible(track);
		}
	}
}
