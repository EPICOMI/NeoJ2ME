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

package com.siemens.mp.game;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformImage;
import org.recompile.mobile.PlatformGraphics;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.siemens.mp.misc.NativeMem;

public class ExtendedImage extends com.siemens.mp.misc.NativeMem
{
	private int[] palette1Bpp = { 0xFFFFFFFF, 0xFF000000 };
	private int[] palette2Bpp = { 0x00FFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF000000 };

	private Image image;

	private PlatformGraphics gc;

	private int width;

	private int height; 
	
	public ExtendedImage(Image img)
	{
		image = Image.createImage(img);
		width = image.getWidth();
		height = image.getHeight();
		gc = image.getGraphics();
	}

	public Image getImage() { return image; }

	public int getPixel(int x, int y) 
	{
		int pixelValue = image.getPixel(x, y);
		
		if (image.is2Bpp()) 
		{
			switch (pixelValue) 
			{
				case 0x00FFFFFF:
					return 0; // Transparent
				case 0xFFFFFFFF:
					return 1; // White
				case 0xFF000000:
				default:
					return 2; // Black
			}
		} 
		else 
		{
			return (pixelValue == 0xFFFFFFFF) ? 0 : 1; // 0 = white, 1 = black
		}
	}

	public void setPixel(int x, int y, byte color)
	{
		if(image.is2Bpp()) { image.setPixel(x, y, palette2Bpp[color & 0x3]); }
		else { image.setPixel(x, y, palette1Bpp[color & 0x1]); }
	}

	public void getPixelBytes(byte[] pixels, int x, int y, int width, int height) 
	{
		if (x % (image.is2Bpp() ? 4 : 8) != 0 || width % (image.is2Bpp() ? 4 : 8) != 0) 
		{
			throw new IllegalArgumentException("x and width must be multiples of " + (image.is2Bpp() ? 4 : 8));
		}
	
		for (int j = 0; j < height; j++) 
		{
			for (int i = 0; i < width; i++) 
			{
				int pixelColor = getPixel(x + i, y + j);
				int pixelIndex = (j * width + i) / (image.is2Bpp() ? 4 : 8);
				int bitIndex = (j * width + i) % (image.is2Bpp() ? 4 : 8);
	
				if (image.is2Bpp()) 
				{
					if (pixelColor == 0) { }  // Transparent
					else if (pixelColor == 1) { pixels[pixelIndex] |= (1 << (6 - bitIndex * 2)); } // White 
					else if (pixelColor == 2 || pixelColor == 3) { pixels[pixelIndex] |= (2 << (6 - bitIndex * 2)); } // Black
				} 
				else 
				{
					if (pixelColor == 0) { pixels[pixelIndex] &= ~(1 << (7 - bitIndex)); } // White 
					else if (pixelColor == 1) { pixels[pixelIndex] |= (1 << (7 - bitIndex)); } // Black
				}
			}
		}
	}
	
	public void setPixels(byte[] pixels, int x, int y, int width, int height) 
	{
		if (x % (image.is2Bpp() ? 4 : 8) != 0 || width % (image.is2Bpp() ? 4 : 8) != 0) 
		{
			throw new IllegalArgumentException("x and width must be multiples of " + (image.is2Bpp() ? 4 : 8));
		}

		if(x < 0) { x = 0; }
		if(y < 0) { y = 0; }

		for (int j = 0; j < Math.min(height, image.getHeight()); j++) 
		{
			for (int i = 0; i < Math.min(width, image.getWidth()); i++) 
			{
				int pixelIndex = (j * width + i) / (image.is2Bpp() ? 4 : 8);
				int bitIndex = (j * width + i) % (image.is2Bpp() ? 4 : 8);
	
				if(pixelIndex >= pixels.length) { continue; }
				if (image.is2Bpp()) 
				{
					int value = (pixels[pixelIndex] >> (6 - bitIndex * 2)) & 0x03;
					switch (value) 
					{
						case 0: // Transparent
							setPixel(x + i, y + j, (byte) 0); // Transparent
							break;
						case 1: // White
							setPixel(x + i, y + j, (byte) 1); // White
							break;
						case 2:
						case 3:
							setPixel(x + i, y + j, (byte) 2); // Black
							break;
					}
				} 
				else 
				{
					int bitValue = (pixels[pixelIndex] >> (7 - bitIndex)) & 0x01;
					setPixel(x + i, y + j, (byte) (bitValue == 1 ? 1 : 0)); // 1 = black, 0 = white
				}
			}
		}
	}

	public void clear(byte color)
	{
		if(image.is2Bpp()) { gc.setColor(palette2Bpp[color & 0x3]); }
		else { gc.setColor(palette1Bpp[color & 0x1]); }
		gc.fillRect(0, 0, width, height);
		gc.setColor(0xFFFFFFFF);
	}

	public void blitToScreen(int x, int y) // from Micro Java Game Development By David Fox, Roman Verhovsek
	{
		Mobile.getPlatform().flushGraphics(image, x, y, width, height);
	} 
}