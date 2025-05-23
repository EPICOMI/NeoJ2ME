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

public class ImageMap 
{
    private int cellWidth;
    private int cellHeight;
    private int mapWidth;
    private int mapHeight;
    private int[] data;
    private Image[] images;
    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;

    protected ImageMap() { }

    public ImageMap(int cellWidth, int cellHeight) {
        if (cellWidth <= 0 || cellHeight <= 0) { throw new IllegalArgumentException("Cell dimensions must be greater than zero"); }

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.windowWidth = 0;
        this.windowHeight = 0;
    }

    public ImageMap(int cellWidth, int cellHeight, int mapWidth, int mapHeight, int[] data, Image[] images) 
    {
        this(cellWidth, cellHeight, mapWidth, mapHeight, data, images, false);
    }

    public ImageMap(int cellWidth, int cellHeight, int mapWidth, int mapHeight, int[] data, Image[] images, boolean concat) 
    {
        if (cellWidth <= 0 || cellHeight <= 0) { throw new IllegalArgumentException("Cell dimensions must be greater than zero"); }

        if (mapWidth <= 0 || mapHeight <= 0) { throw new IllegalArgumentException("Map dimensions must be positive"); }

        if (data.length < mapWidth * mapHeight) { throw new IllegalArgumentException("Data array length is insufficient"); }

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.data = data;
        this.images = images;
        this.windowX = 0;
        this.windowY = 0;
        this.windowWidth = mapWidth * cellWidth;
        this.windowHeight = mapHeight * cellHeight;
    }

    public void setImageMap(int mapWidth, int mapHeight, int[] data, Image[] images) 
    {
        setImageMap(mapWidth, mapHeight, data, images, false);
    }

    public void setImageMap(int mapWidth, int mapHeight, int[] data, Image[] images, boolean concat) 
    {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.data = data;
        this.images = images;
    }

    public void setWindow(int x, int y, int width, int height) 
    {
        this.windowX = x;
        this.windowY = y;
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void moveWindowLocation(int dx, int dy) 
    {
        this.windowX += dx;
        this.windowY += dy;
    }

    public void setWindowLocation(int x, int y) 
    {
        this.windowX = x;
        this.windowY = y;
    }

    public void draw(Graphics g) 
    {
        for (int y = 0; y < mapHeight; y++) 
        {
            for (int x = 0; x < mapWidth; x++) 
            {
                int index = mapWidth * y + x;
                int imgIndex = data[index];
                if (imgIndex >= 0 && imgIndex < images.length && images[imgIndex] != null) 
                {
                    int drawX = windowX + x * cellWidth;
                    int drawY = windowY + y * cellHeight;
                    g.getGraphics2D().drawImage(images[imgIndex].getCanvas(), drawX, drawY, cellWidth, cellHeight, null);
                }
            }
        }
    }
}