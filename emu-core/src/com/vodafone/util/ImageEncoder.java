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
package com.vodafone.util;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import java.awt.Image; // We don't need to use an lcdui image here.
import java.util.Iterator;

public class ImageEncoder 
{
	public static int FORMAT_PNG = 0;
	public static int FORMAT_JPEG = 1;
	private int format;
    private int jpegQualitySize = 100;

	public ImageEncoder(int format) { this.format = format; }

	public static ImageEncoder createEncoder(int format) 
    {
		if (format != FORMAT_PNG && format != FORMAT_JPEG) { throw new IllegalArgumentException("Invalid image format received"); }
		return new ImageEncoder(format);
	}

	public byte[] encodeOffscreen(Image src, int x, int y, int width, int height) throws IOException
    {
        if (src == null) { throw new NullPointerException("Source image cannot be null"); }
        if (x < 0 || y < 0 || width <= 0 || height <= 0) { throw new IllegalArgumentException("Invalid parameters"); }
    
        BufferedImage bufferedImage = new BufferedImage(src.getWidth(null), src.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
    
        BufferedImage croppedImage = bufferedImage.getSubimage(x, y, width, height);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try 
        {
            if (format == FORMAT_JPEG) 
            {
                return encodeAsJpeg(croppedImage, stream);
            } 
            else
            {
                ImageIO.write(croppedImage, "png", stream);
                return stream.toByteArray();
            }
        } 
        catch (IOException e) { throw new RuntimeException("Error encoding image: " + e.getMessage(), e); } 
        finally 
        {
            try { stream.close(); } 
            catch (IOException e) { }
        }
    }

    private byte[] encodeAsJpeg(BufferedImage image, ByteArrayOutputStream stream) throws IOException
    {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) { throw new RuntimeException("No writer found for JPEG format"); }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        float quality = jpegQuality(jpegQualitySize);
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.setOutput(ImageIO.createImageOutputStream(stream));
        writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        writer.dispose();
        
        return stream.toByteArray();
    }

    public void setJpegOption(int size) 
    {
        if (format != FORMAT_JPEG) { throw new IllegalArgumentException("JPEG options can only be set if image is a JPEG"); }
        if (size != 0 && size != 6 && size != 12 && size != 30 && size != 100 && size != 200 && size != 300) 
        {
            throw new IllegalArgumentException("Invalid size value. Must be one of {0, 6, 12, 30, 100, 200, 300}");
        }
        this.jpegQualitySize = size;
    }

    private float jpegQuality(int value) 
    {
        switch (value) 
        {
            case 0:   return 0.0f; // Lowest quality
            case 6:   return 0.2f; // Low quality
            case 12:  return 0.4f; // Medium-low quality
            case 30:  return 0.6f; // Medium quality
            case 100: return 0.8f; // Medium-high quality
            case 200: return 0.9f; // High quality
            case 300: return 1.0f; // Best quality
            default:  throw new IllegalArgumentException("Invalid quality value");
        }
    }
}