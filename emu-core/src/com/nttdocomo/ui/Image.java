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
package com.nttdocomo.ui;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformImage;

public abstract class Image extends PlatformImage
{ 

	protected boolean disposed = false;
	protected int alpha = 255;
	protected int transparentColor = -1;
	protected boolean transparentEnabled = false;

	protected Image() { }

	protected Image(Image source) { super(source); }

	protected Image(int width, int height) { super(width, height, null); }

	protected Image(int width, int height, int[] data, int off) { super(width, height, data, off); }

	public static Image createImage(Image source) 
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create DoJa Image from Image ");
		if (source == null) { throw new NullPointerException(); }

		return new DoJaLCDUIImage(source);
	}

	public static Image createImage(int width, int height) 
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create DoJa Image w,h " + width + ", " + height);
		if (width <= 0 || height <= 0) {throw new IllegalArgumentException();}
		
		return new DoJaLCDUIImage(width, height);
	}

	public static Image createImage(int width, int height, int[] data, int off) 
	{
		if (data == null) { throw new NullPointerException("data cannot be null"); }
		if (width <= 0 || height <= 0) { throw new IllegalArgumentException("width and height must be greater than zero"); }
		if (off < 0 || off + width * height > data.length) { throw new ArrayIndexOutOfBoundsException("Invalid offset or data length"); }

		return new DoJaLCDUIImage(width, height, data, off);
	}

	public Graphics getGraphics() { return super.getDoJaGraphics(); }

	public void dispose() // TODO: Implement this properly
	{
		disposed = true;
	}

	public boolean isDisposed() { return disposed; }

	public int getAlpha() { return alpha; }

	public void setAlpha(int alpha) 
	{
		if (alpha < 0 || alpha > 255) { throw new IllegalArgumentException("Alpha must be between 0 and 255."); }
		this.alpha = alpha;
	}

	public int getTransparentColor() { return transparentColor; }

	public void setTransparentColor(int color) { this.transparentColor = color; }

	public void setTransparentEnabled(boolean enabled) { this.transparentEnabled = enabled; }

	public boolean isTransparentEnabled() { return transparentEnabled; }
}