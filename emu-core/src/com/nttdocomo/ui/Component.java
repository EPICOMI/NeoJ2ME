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

public abstract class Component
{
    private int width;
    private int height;
    private int x;
    private int y;
    private int backgroundColor;
    private int foregroundColor;
    private boolean visible;

    // Constructor
    public Component() 
    {
        this.width = 0;
        this.height = 0;
        this.x = 0;
        this.y = 0;
        this.visible = true; 
    }

    public int getHeight() { return height; }

    public int getWidth() { return width; }

    public int getX() { return x; }

    public int getY() { return y; }

    public void setBackground(int c) 
    {
        if (c < 0) { throw new IllegalArgumentException("Invalid background color value."); }
        this.backgroundColor = c; 
    }

    public void setForeground(int c) 
    {
        if (c < 0) { throw new IllegalArgumentException("Invalid foreground color value."); }
        this.foregroundColor = c; 
    }

    public void setLocation(int x, int y) 
    {
        this.x = x;
        this.y = y; 
    }

    public void setSize(int width, int height) 
    {
        if (width < 0 || height < 0) { throw new IllegalArgumentException("Width and height must be non-negative."); }
        this.width = width;
        this.height = height; 
    }

    public void setVisible(boolean b) { this.visible = b; }
    
    public boolean isVisible() { return visible; }
}