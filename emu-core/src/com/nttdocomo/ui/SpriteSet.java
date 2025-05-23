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

public class SpriteSet 
{

    private Sprite[] sprites;
    private int[] collisionFlags;

    public SpriteSet(Sprite[] sprites) 
    {
        if (sprites == null) 
        {
            throw new NullPointerException("Sprites array cannot be null");
        }
        if (sprites.length == 0 || sprites.length > 32) 
        {
            throw new IllegalArgumentException("Number of sprites must be between 1 and 32");
        }
        this.sprites = sprites;
        this.collisionFlags = new int[sprites.length];
    }

    public Sprite[] getSprites() { return sprites; }

    public int getCount() { return sprites.length; }

    public Sprite getSprite(int index) 
    {
        if (index < 0 || index >= sprites.length)  { throw new ArrayIndexOutOfBoundsException("Index out of bounds"); }
        return sprites[index];
    }

    public void setCollisionAll() 
    {
        for (int i = 0; i < sprites.length; i++) { setCollisionOf(i); }
    }

    public void setCollisionOf(int index) 
    {
        if (index < 0 || index >= sprites.length) { throw new ArrayIndexOutOfBoundsException("Index out of bounds"); }

        Sprite sprite1 = sprites[index];
        if (sprite1 == null || !sprite1.isVisible()) 
        {
            collisionFlags[index] = 0;
            return;
        }

        int collisionFlag = 0;
        for (int i = 0; i < sprites.length; i++) 
        {
            if (i != index) 
            {
                Sprite sprite2 = sprites[i];
                if (sprite2 != null && sprite2.isVisible()) 
                {
                    if (isOverlapping(sprite1, sprite2)) { collisionFlag |= (1 << i); }
                }
            }
        }
        collisionFlags[index] = collisionFlag;
    }

    public boolean isCollision(int index1, int index2) 
    {
        return (getCollisionFlag(index1) & (1 << index2)) != 0;
    }

    public int getCollisionFlag(int index) 
    {
        if (index < 0 || index >= sprites.length)  { throw new ArrayIndexOutOfBoundsException("Index out of bounds"); }
        return collisionFlags[index];
    }

    private boolean isOverlapping(Sprite sprite1, Sprite sprite2) 
    {
        int x1 = sprite1.getX();
        int y1 = sprite1.getY();
        int width1 = sprite1.getWidth();
        int height1 = sprite1.getHeight();

        int x2 = sprite2.getX();
        int y2 = sprite2.getY();
        int width2 = sprite2.getWidth();
        int height2 = sprite2.getHeight();

        return (x1 < x2 + width2 && x1 + width1 > x2 && y1 < y2 + height2 && y1 + height1 > y2);
    }
}