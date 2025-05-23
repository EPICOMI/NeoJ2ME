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

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

public abstract class Canvas extends Frame 
{

    public Canvas() 
    { 
        super(); 
        Mobile.log(Mobile.LOG_INFO, Canvas.class.getPackage().getName() + "." + Canvas.class.getSimpleName() + ": " + "Create I-Appli Canvas:" + width+", "+height);
    }

    public Graphics getGraphics() { return graphics; }

    public int getKeypadState() { return MobilePlatform.DoJaKeyState; }

    public abstract void paint(Graphics g);

    public void processEvent(int type, int param) 
	{ 
		Mobile.log(Mobile.LOG_WARNING, Canvas.class.getPackage().getName() + "." + Canvas.class.getSimpleName() + ": " + "DoJa Canvas Process event type:" + type + " , param:" + param);
	}

    public void repaint() { repaint(0, 0, Mobile.lcdWidth, Mobile.lcdHeight); }

    public void repaint(int x, int y, int width, int height) 
    {
        if (width < 0 || height < 0) { throw new IllegalArgumentException("Width and height must be non-negative."); }
        
        try 
		{

			if(!isShown()) { return; }

			graphics.reset();
			paint(graphics);
			
			// Draw command bar whenever the canvas is not fullscreen and there are commands in the bar
			//if (!fullscreen && !commands.isEmpty()) { paintCommandsBar(); }

			Mobile.getPlatform().flushGraphics(platformImage, x, y, width, height);
		}
		catch (Exception e) 
		{
			Mobile.log(Mobile.LOG_ERROR, Canvas.class.getPackage().getName() + "." + Canvas.class.getSimpleName() + ": " + "Serious Exception hit in repaint(): " + e.getMessage());
			e.printStackTrace();
		}
    }

}