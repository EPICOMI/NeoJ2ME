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
package mmpp.microedition.lcdui;

import java.awt.image.BufferedImage;

import org.recompile.mobile.PlatformImage;

public class GraphicsX extends org.recompile.mobile.PlatformGraphics
{

    public static int DEFAULT_ALPHA = 256;

    javax.microedition.lcdui.Image img;

    public GraphicsX(PlatformImage image) { super(image); }

    public javax.microedition.lcdui.Image capture(int x, int y, int width, int height) 
    {
        img = javax.microedition.lcdui.Image.createRGBImage(toIntArray(getCanvas().getSubimage(x, y, width, height)), width, height, true);
        return img;
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints, int argbColor) 
    {
        int tempColor = color;
        setAlphaRGB(argbColor);
        getGraphics2D().drawPolygon(xPoints, yPoints, nPoints);
        setColor(tempColor);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) 
    {
        for (int i = 0; i < nPoints - 1; i++) 
        {
            drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints, int argbColor) 
    {
        int tempColor = color;
        setAlphaRGB(argbColor);
        getGraphics2D().fillPolygon(xPoints, yPoints, nPoints);
        setColor(tempColor);
    }

    public int getPixel(int x, int y) { return img.getPixel(x, y); }

    public void setAlpha(int alpha) { setAlphaRGB(alpha); }

    public void setPaintMode() { }

    public void setPixel(int x, int y, int RGB) 
    {
        img.setPixel(x, y, RGB);
    }

    public void setXORMode(int RGB) { }

    public int[] toIntArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        
        // Get RGB values from the BufferedImage
        image.getRGB(0, 0, width, height, pixels, 0, width);
        
        return pixels;
    }
}