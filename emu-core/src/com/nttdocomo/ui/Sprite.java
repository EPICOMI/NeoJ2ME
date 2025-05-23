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

public class Sprite 
{

    private Image image;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible;
    private int flipMode;
    private int[] rotationMatrix;

    protected Sprite() { this.visible = true; }

    public Sprite(Image image) 
    {
        setImage(image);
        this.x = 0;
        this.y = 0;
        this.visible = true;
    }

    public Sprite(Image image, int x, int y, int width, int height) 
    {
        setImage(image, x, y, width, height);
        this.visible = true;
    }

    public int getWidth() 
    {
        return (width > 0) ? width : (image != null ? image.getWidth() : 0);
    }

    public int getHeight() 
    {
        return (height > 0) ? height : (image != null ? image.getHeight() : 0);
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public void setImage(Image image) 
    {
        if (image == null) { throw new NullPointerException("Image cannot be null"); }

        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public void setImage(Image image, int x, int y, int width, int height) 
    {
        if (image == null) 
        {
            throw new NullPointerException("Image cannot be null");
        }
        if (width < 0 || height < 0) 
        {
            throw new IllegalArgumentException("Width and height must be non-negative");
        }
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setLocation(int x, int y) 
    {
        this.x = x;
        this.y = y;
    }

    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isVisible() { return visible; }

    public void setFlipMode(int flipMode) { this.flipMode = flipMode; }

    public void setRotation(int[] lt) 
    {
        if (lt == null) { this.rotationMatrix = null; }
        else if (lt.length < 4) 
        {
            throw new ArrayIndexOutOfBoundsException("Rotation matrix must have at least 4 elements");
        } 
        else 
        {
            this.rotationMatrix = new int[4];
            System.arraycopy(lt, 0, this.rotationMatrix, 0, 4);
        }
    }

    public Image getImage() { return image; }
}