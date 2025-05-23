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
package com.vodafone.extension;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;

import org.recompile.mobile.Mobile;

import javax.microedition.lcdui.Graphics;

// This one appears to just be an extension of Nokia's DirectGraphics
public class EnhancedGraphics 
{
	Graphics graphics;
	DirectGraphics directGraphics;

	public EnhancedGraphics(Graphics paramGraphics) 
    {
		directGraphics = DirectUtils.getDirectGraphics(graphics = paramGraphics);
	}

	public synchronized void drawPolyline(int[] xPoints, int[] yPoints, int count) 
    {
		directGraphics.drawPolygon(xPoints, 0, yPoints, 0, count, graphics.getColor() | 0xFF000000);
	}

	public synchronized void drawPolyline(int[] xPoints, int[] yPoints, int offset, int count) 
    {
		directGraphics.drawPolygon(xPoints, offset, yPoints, offset, count, graphics.getColor() | 0xFF000000);
	}

	public synchronized void fillPolygon(int[] xPoints, int[] yPoints, int count) 
    {
		directGraphics.fillPolygon(xPoints, 0, yPoints, 0, count, graphics.getColor() | 0xFF000000);
	}

	public synchronized void fillPolygon(int[] xPoints, int[] yPoints, int offset, int count) 
    {
		directGraphics.fillPolygon(xPoints, offset, yPoints, offset, count, graphics.getColor() | 0xFF000000);
	}

	public boolean getAntiAliasMode() { return Mobile.isAAEnabled; }

	public void setAntiAliasMode(boolean paramBoolean) { Mobile.isAAEnabled = paramBoolean; }
}