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
package javax.microedition.lcdui;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.game.Sprite;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformImage;


public class Image extends PlatformImage
{

	protected Image() { super(); }

	protected Image(int Width, int Height) { super(Width, Height); }

	protected Image(String name) { super(name); }

	protected Image(InputStream stream) { super(stream); }
	
	protected Image(int Width, int Height, int ARGBcolor) { super(Width, Height, ARGBcolor); }

	protected Image(Image source) { super(source); }

	protected Image(byte[] imageData, int imageOffset, int imageLength, boolean mutable) { super(imageData, imageOffset, imageLength, mutable); }

	protected Image(int[] rgb, int Width, int Height, boolean processAlpha) { super(rgb, Width, Height, processAlpha); }

	protected Image(Image image, int x, int y, int Width, int Height, int transform) { super(image, x, y, Width, Height, transform); } 

	// TODO: This will create mutable images for both MIDP (shouldn't) and Nokia DirectGraphics (should)
	public static Image createImage(byte[] imageData, int imageOffset, int imageLength) throws IllegalArgumentException
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image from image data ");
		if (imageData == null) {throw new NullPointerException();}
		if (imageOffset + imageLength > imageData.length) {throw new ArrayIndexOutOfBoundsException();}

		return new Image(imageData, imageOffset, imageLength, false);
	}

	public static Image createImage(byte[] imageData, int imageOffset, int imageLength, boolean mutable) throws IllegalArgumentException
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image from image data ");
		if (imageData == null) {throw new NullPointerException();}
		if (imageOffset + imageLength > imageData.length) {throw new ArrayIndexOutOfBoundsException();}

		return new Image(imageData, imageOffset, imageLength, mutable);
	}

	public static Image createImage(Image source)
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image from Image ");
		if (source == null) {throw new NullPointerException();}
		// If the source is immutable, just return it, despite the docs not mentioning it directly
		if (!source.isMutable()) { return source; }

		// Else, create an immutable copy of the received image.
		return new Image(source);
	}

	public static Image createImage(Image img, int x, int y, int width, int height, int transform)
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image from sub-image " + " img_w:" + Integer.toString(img.getWidth()) + " img_h:" + Integer.toString(img.getHeight()) + " x:" + Integer.toString(x) + " y:" + Integer.toString(y) + " width:" + Integer.toString(width) + " height:" + Integer.toString(height));
		if (img == null) { throw new NullPointerException(); }
		if (x+width > img.getWidth() || y+height > img.getHeight()) {throw new IllegalArgumentException();}
		if (width <= 0 || height <= 0) {throw new IllegalArgumentException();}

		if(!img.isMutable() && (x+width == img.getWidth() && y+height == img.getHeight()) && transform == Sprite.TRANS_NONE) { return img; }

		return new Image(img, x, y, width, height, transform);
	}

	public static Image createImage(InputStream stream) throws IOException, IllegalArgumentException
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image stream");
		if (stream == null) {throw new NullPointerException();}
		return new Image(stream);
	}

	public static Image createImage(int width, int height)
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image w,h " + width + ", " + height);
		if (width <= 0 || height <= 0) {throw new IllegalArgumentException();}

		return new Image(width, height);
	}

	public static Image createImage(int width, int height, int ARGBcolor)
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image w,h,color " + width + ", " + height  + ", " + ARGBcolor);
		if (width <= 0 || height <= 0) {throw new IllegalArgumentException();}

		return new Image(width, height, ARGBcolor);
	}

	public static Image createImage(String name) throws IOException
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image " + name);
		if (name == null) {throw new NullPointerException();}
		return new Image(name);
	}

	public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha)
	{
		Mobile.log(Mobile.LOG_DEBUG, Image.class.getPackage().getName() + "." + Image.class.getSimpleName() + ": " + "Create Image RGB " + width + ", " + height);
		if (rgb == null) {throw new NullPointerException();}
		if (width <= 0 || height <= 0) {throw new IllegalArgumentException();}
		if (rgb.length < width * height) {throw new ArrayIndexOutOfBoundsException();}
		return new Image(rgb, width, height, processAlpha);
	}

	public Graphics getGraphics() { return super.getMIDPGraphics(); }
}
