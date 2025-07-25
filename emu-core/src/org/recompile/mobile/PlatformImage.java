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
package org.recompile.mobile;

import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.GameCanvas;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

public class PlatformImage
{
	protected BufferedImage canvas;
	protected Graphics gc;
	protected com.nttdocomo.ui.Graphics djgc;

	protected int width;
	protected int height;

	private boolean isMutable = false;

	private boolean is2bpp = false; // SIEMENS: False = 1bpp, True = 2bpp

	public BufferedImage getCanvas() { return canvas; }

	public void setCanvas(BufferedImage newCanvas) { canvas = newCanvas; }

	public Graphics getMIDPGraphics() 
	{ 
		if(!isMutable()) { throw new IllegalStateException("Image is immutable, cannot access Graphics object"); }
		return gc;
	}

	public com.nttdocomo.ui.Graphics getDoJaGraphics() 
	{ 
		if(!isMutable()) { throw new IllegalStateException("Image is immutable, cannot access Graphics object"); }
		return djgc; 
	}


	protected void createGraphics()
	{
		gc = new Graphics(this);
		gc.setColor(0x000000);
	}

	protected void createDoJaGraphics()
	{
		djgc = new com.nttdocomo.ui.Graphics(this);
		djgc.setColor(0x000000);
	}

	public PlatformImage() { }

	public PlatformImage(int Width, int Height)
	{
		// Create blank Image
		width = Width;
		height = Height;

		if(Mobile.noAlphaOnBlankImages) { canvas = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB); }
		else { canvas = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_ARGB); }
		
		createGraphics();

		gc.setColor(0xFFFFFF);
		gc.fillRect(0, 0, width, height);
		gc.setColor(0x000000);

		isMutable = true;
	}

	public PlatformImage(int Width, int Height, int ARGBcolor)
	{
		canvas = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_ARGB);
		// Create blank Image
		width = Width;
		height = Height;
		
		createGraphics();

		gc.setARGBColor(ARGBcolor);
		gc.fillRect(0, 0, width, height);
		gc.setColor(0x000000);

		isMutable = true;
	}

	public PlatformImage(String name)
	{
		// Create Image from resource name
		Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image From Resource Name");
		BufferedImage temp;

		InputStream stream = Mobile.getPlatform().loader.getMIDletResourceAsStream(name);

		if(stream == null) 
		{
			// We should really throw an exception here, but House M.D is one game that explicitly tries to load null images without proper exception handling 
			if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't load image from resource, as the returned image is null."); }
			else 
			{
				Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image from Resource Name is NULL, ignoring due to Non Fatal Null Images being enabled.");
			}
		}
		else
		{
			try { temp = ImageIO.read(stream); } 
			catch (IOException e) { throw new IllegalArgumentException("Failed to read image from resource:" + e.getMessage()); }
			
			if(temp == null) 
			{ 
				if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't load image from resource, as the returned image is null."); }
				else 
				{
					Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image from Resource Name is NULL, ignoring due to Non Fatal Null Images being enabled.");
				}
			}
			
			width = (int)temp.getWidth();
			height = (int)temp.getHeight();

			canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			canvas.getGraphics().drawImage(temp, 0, 0, null);
		}
	}

	public PlatformImage(InputStream stream)
	{
		// Create Image from InputStream
		Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image From Stream");
		BufferedImage temp;
		try { temp = ImageIO.read(stream); } 
		catch (IOException e) { throw new IllegalArgumentException("Failed to read image from InputStream:" + e.getMessage()); }
		
		if(temp == null) 
		{ 
			if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't load image from stream, as the stream is null."); }
			else 
			{
				Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image from stream is NULL, ignoring due to Non Fatal Null Images being enabled.");
			}
		}

		width = (int)temp.getWidth();
		height = (int)temp.getHeight();

		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		canvas.getGraphics().drawImage(temp, 0, 0, null);
	}

	public PlatformImage(Image source)
	{
		// Create Image from Image
		if(source == null) 
		{ 
			if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't load image, it is null."); }
			else 
			{
				Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image is NULL, ignoring due to Non Fatal Null Images being enabled.");
			}
		}

		width = source.getWidth();
		height = source.getHeight();

		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		canvas.getGraphics().drawImage(source.getCanvas(), 0, 0, null);
	}

	public PlatformImage(byte[] imageData, int imageOffset, int imageLength, boolean mutable)
	{
		// Create Image from Byte Array Range (Data is PNG, JPG, etc.)
		InputStream stream = new ByteArrayInputStream(imageData, imageOffset, imageLength);

		BufferedImage temp;
		
		try { temp = ImageIO.read(stream); } 
		catch (IOException e) { throw new IllegalArgumentException("Failed to read image from Byte Array." + e.getMessage()); }
		
		if(temp == null) 
		{ 
			if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't load image from byte array, as the returned image is null."); }
			else 
			{
				Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image from byte array is NULL, ignoring due to Non Fatal Null Images being enabled.");
			}
		}
		
		width = temp.getWidth();
		height = temp.getHeight();

		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		if(mutable) { createGraphics(); }

		canvas.getGraphics().drawImage(temp, 0, 0, null);

		isMutable = mutable;
	}

	public PlatformImage(int[] rgb, int Width, int Height, boolean processAlpha)
	{
		// createRGBImage (Data is ARGB pixel data)
		width = Width;
		height = Height;

		if(width < 1) { width = 1; }
		if(height < 1) { height = 1; }

		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] canvasPixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();

		// Process alpha if necessary
		if (!processAlpha) 
		{
			for (int i = 0; i < rgb.length; i++) { rgb[i] |= 0xFF000000; } // Set alpha to opaque
		}

		System.arraycopy(rgb, 0, canvasPixels, 0, Math.min(rgb.length, canvasPixels.length));
	}

	public PlatformImage(Image image, int x, int y, int Width, int Height, int transform)
	{
		BufferedImage sub = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_ARGB);
	
		// Get the raw pixel data from the source image, and the new sub image
		final int[] sourceData = ((DataBufferInt) image.canvas.getRaster().getDataBuffer()).getData();
		final int[] subData = ((DataBufferInt) sub.getRaster().getDataBuffer()).getData();
	
		// Copy pixel data directly to the subimage's databuffer.
		for (int j = 0; j < Height; j++) 
		{
			int sourceRow = (y + j) * image.canvas.getWidth() + x;
			int subRow = j * Width;
	
			// Copy pixel rows from the source image to the new sub-image
			System.arraycopy(sourceData, sourceRow, subData, subRow, Math.min(Width, image.canvas.getWidth() - x));
		}
	
		canvas = transformImage(sub, transform);
		
		width = (int) canvas.getWidth();
		height = (int) canvas.getHeight();
	}

	// DoJa's image class uses these
	public PlatformImage(com.nttdocomo.ui.Image source) 
	{
		// Create DoJa Image from DoJa Image
		if(source == null) 
		{ 
			if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't load image, it is null."); }
			else 
			{
				Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Image is NULL, ignoring due to Non Fatal Null Images being enabled.");
			}
		}

		width = source.getWidth();
		height = source.getWidth();

		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		canvas.getGraphics().drawImage(source.getCanvas(), 0, 0, null);
	}

	public PlatformImage(int Width, int Height, com.nttdocomo.ui.Image source) // Just to differentiate from the other constructor above
	{
		// Create blank DoJa Image
		width = Width;
		height = Height;

		if(Mobile.noAlphaOnBlankImages) { canvas = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB); }
		else { canvas = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_ARGB); }
		
		createDoJaGraphics();

		djgc.setColor(0xFFFFFF);
		djgc.fillRect(0, 0, width, height);
		djgc.setColor(0x000000);

		isMutable = true;
	}

	public PlatformImage(int Width, int Height, int[] data, int off) 
	{
		// Create DoJa image from int array
		width = Width;
		height = Height;

		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		createDoJaGraphics();

		int[] canvasPixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
		System.arraycopy(data, off, canvasPixels, 0, width * height);
		
		isMutable = true;
	}


	// Siemens methods
	public void set2Bpp(boolean bpp) { this.is2bpp = bpp; }

    public boolean is2Bpp() { return is2bpp; }

	// Common methods
	public int getWidth() { return width; }

	public int getHeight() { return height; }

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) 
	{
		if (rgbData == null) 
		{ 
			if(!Mobile.compatNonFatalNullImages) { throw new NullPointerException("Can't use getRGB, as the returned image is null."); }
			else 
			{
				Mobile.log(Mobile.LOG_DEBUG, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "tried to use getRGB on NULL image, ignoring due to Non Fatal Null Images being enabled.");
			}
		}
		if (width <= 0 || height <= 0) { return; } // No pixels to copy
		if (x < 0 || y < 0 || x + width > canvas.getWidth() || y + height > canvas.getHeight()) 
		{
			throw new IllegalArgumentException("getRGB Requested area exceeds bounds of the image");
		}
		if (Math.abs(scanlength) < width) 
		{
			throw new IllegalArgumentException("scanlength must be >= width");
		}

		// Temporary array to hold the raw pixel data
		int[] tempData = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
		// Copy the data into rgbData, taking scanlength into account
		for (int row = 0; row < height; row++) 
		{
			int sourceIndex = (y + row) * canvas.getWidth() + x;
			int destIndex = offset + row * scanlength;
	
			System.arraycopy(tempData, sourceIndex, rgbData, destIndex, width);
		}
	}

	public int getARGB(int x, int y) 
	{ 
		if (x < 0 || y < 0 || x >= canvas.getWidth() || y >= canvas.getHeight()) 
		{
			throw new IllegalArgumentException("Requested area exceeds bounds of the image");
		}
	
		// Get the raw pixel data array directly from the canvas
		int[] pixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
		return pixels[y * canvas.getWidth() + x];
	}

	public int getPixel(int x, int y) { return getARGB(x, y); }

	public void setPixel(int x, int y, int color)
	{
		if (x < 0 || y < 0 || x >= canvas.getWidth() || y >= canvas.getHeight()) 
		{
			throw new IllegalArgumentException("Requested area exceeds bounds of the image");
		}
	
		// Get the raw pixel data array directly from the canvas
		int[] pixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
		pixels[y * canvas.getWidth() + x] = color;
	}

	public boolean isMutable() { return isMutable; }

	public static final BufferedImage transformImage(final BufferedImage image, final int transform)
	{
		// Return early if no transform is specified.
		if(transform == Sprite.TRANS_NONE) { return image; }

		final int width = (int)image.getWidth();
		final int height = (int)image.getHeight();

		BufferedImage transimage = null;
		if(transform == Sprite.TRANS_ROT90 || transform == Sprite.TRANS_ROT270 || transform == Sprite.TRANS_MIRROR_ROT90 || transform == Sprite.TRANS_MIRROR_ROT270) 
		{
			transimage = new BufferedImage(height, width, image.getType()); // Non-Math.PI rotations require width and height to be swapped
		}
		else { transimage = new BufferedImage(width, height, image.getType()); }

		// We know the data is of TYPE_INT_ARGB, so just get it directly instead of checking for its type
		final int[] sourceData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		final int[] targetData = ((DataBufferInt)transimage.getRaster().getDataBuffer()).getData();
		
		switch (transform) 
		{
			case Sprite.TRANS_ROT90:
				for (int y = 0; y < height; y++) 
				{
					int targetPos = (height - 1 - y);
					for (int x = 0; x < width; x++) 
					{
						targetData[targetPos + x * height] = sourceData[y * width + x];
					}
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_rot90");
				break;

			case Sprite.TRANS_ROT180:
				/* 
				 * Since this one also has the effect of mirroring the image horizontally like TRANS_MIRROR alongside a
				 * vertical transformation, we can optimize it by only going up to half of the image's width, making two 
				 * pixel assignments on each inner loop iteration from the image's edges to the center, then checking if 
				 * the width is odd, to just copy the pixel in the middle as it won't change on the transformed image.
				 */
				for (int y = 0; y < height; y++) 
				{
					int targetPos = (height - 1 - y) * width;
					for (int x = 0; x < width / 2; x++) 
					{
						targetData[targetPos + (width - 1 - x)] = sourceData[y * width + x];
						targetData[targetPos + x] = sourceData[y * width + (width - 1 - x)];
					}
					// If image width is odd, copy the middle pixel directly as there's no need to swap anything.
					if (width % 2 != 0) { targetData[targetPos + (width / 2)] = sourceData[y * width + (width / 2)]; }
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_rot180");
				break;
			
			case Sprite.TRANS_ROT270:
				for (int y = 0; y < height; y++) 
				{
					for (int x = 0; x < width; x++) 
					{
						targetData[y + (width - 1 - x) * height] = sourceData[y * width + x];
					}
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_rot270");
				break;

			case Sprite.TRANS_MIRROR: 
				/*
				* Even though sorting an entire column would be faster from a pure algorithmic perspective (like processing
				* a whole row at once is on TRANS_MIRROR_ROT180), image data tends to be stored so that each row is contiguous 
				* in memory, which makes its access oftentimes MUCH faster than for columns, which could negate the performance 
				* benefits of column access entirely and then some.
				* 
				* This transform is such a case. Making operations on columns, and eliminating that inner row loop actually 
				* results in far worse performance since columns would be accessed way more often. So the next best thing is
				* only working on half of the image's width, just like TRANS_ROT180.
				*/
				for (int y = 0; y < height; y++) 
				{
					int targetRow = y * width;
					for (int x = 0; x < width / 2; x++) 
					{
						targetData[targetRow + (width - 1 - x)] = sourceData[targetRow + x];
						targetData[targetRow + x] = sourceData[targetRow + (width - 1 - x)];
					}
					
					// If image width is odd, copy the middle pixel directly as there's no need to swap anything.
					if (width % 2 != 0) { targetData[targetRow + (width/2)] = sourceData[targetRow + (width/2)]; }
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_mirror");
				break;

			case Sprite.TRANS_MIRROR_ROT90:
				for (int y = 0; y < height; y++) 
				{
					int targetRow = height - 1 - y;
					for (int x = 0; x < width; x++) 
					{
						targetData[x * height + targetRow] = sourceData[y * width + (width - 1 - x)];
					}
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_mirror90");
				break;

			case Sprite.TRANS_MIRROR_ROT180: // Basically mirror vertically (an arrow pointing up will then point down).
				for (int y = 0; y < height; y++) // Due to this, we copy entire rows at once instead of going pixel by pixel
				{
					System.arraycopy(sourceData, y * width, targetData, (height - 1 - y) * width, width);
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_mirror180");
				break;
				
			case Sprite.TRANS_MIRROR_ROT270:
				for (int y = 0; y < height; y++) 
				{
					for (int x = 0; x < width; x++) 
					{
						targetData[(width - 1 - x) * height + y] = sourceData[y * width + (width - 1 - x)];
					}
				}
				//dumpImage(image, "");
				//dumpImage(transimage, "_mirror270");
				break;
		}

		return transimage;
	}

	// TODO: Turn this into a setting. Being able to dump image data would be nice.
	public static void dumpImage(BufferedImage image, String append) 
	{
        try 
		{
			String imageMD5 = generateMD5Hash(image);
			String dumpPath = "." + File.separatorChar + "FreeJ2MEDumps" + File.separatorChar + "Image" + File.separatorChar + Mobile.getPlatform().loader.suitename + File.separatorChar;
			File dumpFile = new File(dumpPath);
			
			if (!dumpFile.isDirectory()) { dumpFile.mkdirs(); }
			
			dumpPath = dumpPath + "Image_" + imageMD5 + append + ".png";
			
			dumpFile = new File(dumpPath);
			if(dumpFile.exists()) { return; } // Don't overwrite an image that already exists
            ImageIO.write(image, "png", dumpFile);
            System.out.println("Image saved successfully: " + dumpPath);
        } 
		catch (IOException e) { Mobile.log(Mobile.LOG_ERROR, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Failed to save image file: " + e.getMessage()); }
    }

	private static String generateMD5Hash(BufferedImage image) 
	{
        try 
		{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(imageBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) { sb.append(String.format("%02x", b)); }

            return sb.toString();
        } 
		catch (Exception e) 
		{
			Mobile.log(Mobile.LOG_ERROR, PlatformImage.class.getPackage().getName() + "." + PlatformImage.class.getSimpleName() + ": " + "Could not generate MD5 Hash for data: " + e.getMessage());
            return null;
        }
    }
}
