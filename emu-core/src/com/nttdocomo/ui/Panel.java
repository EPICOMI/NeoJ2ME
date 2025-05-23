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

import java.util.ArrayList;
import java.util.List;

public class Panel extends Frame 
{

    private String title;
    private FocusManager focusManager;
    private LayoutManager layoutManager;
    private List<Component> components;

    public Panel() 
    {
        this.title = null; 
        this.components = new ArrayList<>();
    }

    public void setTitle(String title) 
    {
        this.title = title; 
    }

    public void setBackground(int color) 
    {
        super.setBackground(color);
    }

    public void add(Component comp) 
    {
        if (comp == null) { throw new NullPointerException("Component cannot be null."); }

        if (components.contains(comp)) { throw new UIException(1, "Component is already added to another panel."); }
        components.add(comp);
    }

    public void setComponentListener(ComponentListener listener) { }

    public void setSoftKeyListener(SoftKeyListener listener) { }

    public void setKeyListener(KeyListener listener) { }

    public void setFocusManager(FocusManager fm) 
    {
        if (fm == null)  { throw new NullPointerException("FocusManager cannot be null."); }
        this.focusManager = fm; 
    }

    public FocusManager getFocusManager() { return focusManager; }

    public void setLayoutManager(LayoutManager lm) { this.layoutManager = lm; }
}