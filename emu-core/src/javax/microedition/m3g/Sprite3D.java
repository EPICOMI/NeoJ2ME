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

import java.util.Hashtable;

import org.recompile.mobile.Mobile;

public class Sprite3D extends Node
{

	private static final int FLIPX = 1;
	private static final int FLIPY = 2;
	private int flip;

	private static Hashtable<Image2D, Texture2D> textures = new Hashtable<Image2D, Texture2D>();

	private Image2D image;
	private Appearance appearance;
	private boolean scaled;
	private Texture2D texture;

	private int cropw;
	private int croph;
	private int cropx;
	private int cropy;


	public Sprite3D(boolean isScaled, Image2D img, Appearance a)
	{
		scaled = isScaled;
		image = img;
		appearance = a;
	}

	Object3D duplicateImpl() 
	{
		Sprite3D copy = new Sprite3D(scaled, image, appearance);
		super.duplicate((Node) copy);
		copy.cropx = cropx;
		copy.cropy = cropy;
		copy.cropw = cropw;
		copy.croph = croph;
		copy.flip = flip;
		return copy;
	}


	public Appearance getAppearance() { return appearance; }

	public int getCropHeight() { return croph; }

	public int getCropWidth() { return cropw; }

	public int getCropX() { return cropx; }

	public int getCropY() { return cropy; }

	public Image2D getImage() { return image; }

	public boolean isScaled() { return scaled; }

	public void setAppearance(Appearance a) { appearance = a; }

	public void setCrop(int cropX, int cropY, int width, int height)
	{
		cropx=cropX;
		cropy=cropY;
		cropw=width;
		croph=height;
	}

	public void setImage(Image2D img) 
	{ 
		this.image = image;
		texture = (Texture2D) textures.get(image);

		if (texture == null) {
			texture = new Texture2D(image);
			texture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
			texture.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);
			texture.setBlending(Texture2D.FUNC_REPLACE);

			// cache texture
			textures.put(image, texture);
		}
	}

	@Override
	void updateProperty(int property, float[] value) 
	{
		Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "AnimTrack updating Sprite3D property");
		switch (property) 
		{
			case AnimationTrack.CROP:
				if (value.length > 2) 
				{
					setCrop((int)value[0], (int)value[1], (int)Math.max(-Graphics3D.MAX_TEXTURE_DIMENSION, Math.min(Graphics3D.MAX_TEXTURE_DIMENSION, value[2])),
							(int)Math.max(-Graphics3D.MAX_TEXTURE_DIMENSION, Math.min(Graphics3D.MAX_TEXTURE_DIMENSION, value[3])));
				} 
				else 
				{
					setCrop((int)value[0], (int)value[1], getCropWidth(), getCropHeight());
				}
			default:
				super.updateProperty(property, value);
		}
	}

	boolean animTrackCompatible(AnimationTrack track) 
	{
		switch (track.getTargetProperty()) 
		{
			case AnimationTrack.CROP:
				return true;
			default:
				return super.animTrackCompatible(track);
		}
	}
}
