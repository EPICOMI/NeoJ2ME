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

import org.recompile.mobile.MobilePlatform;

public final class Button extends Component implements Interactable 
{
    
    private String label;

    public Button() 
    {
        this.label = " "; 
        setVisible(true);
        setEnabled(true);
    }

    public Button(String label) 
    {
        this.label = (label != null) ? label : " ";
        setVisible(true);
        setEnabled(true);
    }

    public void setLabel(String label) { this.label = (label != null) ? label : " "; }

    public void requestFocus() { }

    public void setEnabled(boolean flag) { }

    public String getLabel() { return label; }
    
    public int getWidth() 
    {
        return 0;
    }

    public int getHeight() { return 0; }
    
    private int getScreenWidth() { return MobilePlatform.lcdWidth;  }
}