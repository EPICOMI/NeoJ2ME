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

import java.io.InputStream;

import org.recompile.mobile.PlatformImage;

public abstract class PalettedImage extends Image 
{
    protected Image image;
    protected Palette palette;
    protected byte[] imageData;
    protected int transparentIndex = -1;

    protected PalettedImage() 
    {
        super();
        this.palette = new Palette(256); // Palette has 256 colors
    }

    public static PalettedImage createPalettedImage(byte[] data) 
    {
        return new PalettedImageImpl(data);
    }

    public static PalettedImage createPalettedImage(InputStream in) 
    {
        try 
        {
            byte[] tmpData = new byte[in.available()];
            in.read(tmpData, 0, in.available());
            return new PalettedImageImpl(tmpData);
        }
        catch (Exception e) { return null; }
    }

    public static PalettedImage createPalettedImage(int width, int height) 
    {
        return new PalettedImageImpl(width, height);
    }

    public void changeData(byte[] data) 
    {
        this.imageData = data;
    }

    public void changeData(InputStream in) 
    {
        try 
        {
            in.read(imageData, 0, in.available());
        }
        catch (Exception e) { }
    }

    public static Palette extractPalette(byte[] imageData) {
        if (imageData.length < 4) {
            throw new IllegalArgumentException("Image data is too short.");
        }
    
        // Check for GIF signature
        if (imageData[0] == 'G' && imageData[1] == 'I' && imageData[2] == 'F') {
            return extractPaletteFromGIF(imageData);
        }
        // Check for BMP signature
        else if (imageData[0] == 'B' && imageData[1] == 'M') {
            return extractPaletteFromBMP(imageData);
        } else {
            throw new UnsupportedOperationException("Unsupported image format.");
        }
    }
    
    private static Palette extractPaletteFromGIF(byte[] gifData) 
    {
        final int headerOffset = 13;
        int colorCount = 256;
        int[] colors = new int[colorCount];
    
        for (int i = 0; i < colorCount; i++) 
        {
            int r = gifData[headerOffset + (i * 3)];
            int g = gifData[headerOffset + (i * 3) + 1];
            int b = gifData[headerOffset + (i * 3) + 2];
            colors[i] = (0xFF << 24) | (r << 16) | (g << 8) | b; 
        }
    
        return new Palette(colors);
    }
    
    private static Palette extractPaletteFromBMP(byte[] bmpData) 
    {
        int bitDepth = bmpData[28] & 0xFF;
        final int colorTableOffset = 54;
        int colorCount = (bitDepth == 1) ? 2 : (bitDepth == 4) ? 16 : 256;
        int[] colors = new int[colorCount];
    
        for (int i = 0; i < colorCount; i++) 
        {
            int b = bmpData[colorTableOffset + (i * 4)];
            int g = bmpData[colorTableOffset + (i * 4) + 1];
            int r = bmpData[colorTableOffset + (i * 4) + 2];
            colors[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    
        return new Palette(colors);
    }

    public Palette getPalette() { return palette; }

    public void setPalette(Palette palette) { this.palette = palette; }

    public int getTransparentIndex() { return transparentIndex; }

    public void setTransparentIndex(int index) { this.transparentIndex = index; }

    public void setTransparentEnabled(boolean enabled) { }

    public Graphics getGraphics() {
        throw new UnsupportedOperationException("getGraphics() is not supported for PalettedImage.");
    }

    public int getWidth() { return super.getWidth(); }

    public int getHeight() { return super.getWidth(); }
}