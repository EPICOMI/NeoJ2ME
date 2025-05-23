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
package com.vodafone.lcdui;

public class SVGImage 
{
    private int viewportWidth;
    private int viewportHeight;

    public static SVGImage createImage(java.io.InputStream is) { return new SVGImage(); }

    public static SVGImage createImage(String svgURI) { return new SVGImage(); }

    public void drawImage(javax.microedition.lcdui.Graphics g, int x, int y) { }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() { return viewportHeight; }

    public void setViewportSize(int width, int height) 
    {
        if (width < 0 || height < 0) { throw new IllegalArgumentException("Width and height must be non-negative."); }
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void pan(float panX, float panY) { }

    public void resetUserTransform() { }

    public void rotate(float theta, float x, float y) { }

    public void zoom(float zoom) 
    {
        if (zoom <= 0) { throw new IllegalArgumentException("Zoom factor must be greater than 0."); }
    }

    public void toUserSpace(int[] viewportCoordinate, float[] userSpaceCoordinate) {
        if (viewportCoordinate == null || userSpaceCoordinate == null || viewportCoordinate.length != 2 || userSpaceCoordinate.length != 2) 
        {
            throw new IllegalArgumentException("Invalid input coordinates.");
        }
    }

    public void toViewportSpace(float[] userSpaceCoordinate, int[] viewportCoordinate) 
    {
        if (userSpaceCoordinate == null || viewportCoordinate == null || userSpaceCoordinate.length != 2 || viewportCoordinate.length != 2) 
        {
            throw new IllegalArgumentException("Invalid input coordinates.");
        }
    }
}