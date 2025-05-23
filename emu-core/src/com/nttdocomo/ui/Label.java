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

public final class Label extends Component 
{

    public static final int LEFT = 0; 
    public static final int CENTER = 1; 
    public static final int RIGHT = 2; 

    private String text;
    private int alignment;

    public Label() { this("", CENTER); }

    public Label(String text) { this(text, CENTER);  }

    public Label(String text, int alignment) 
    {
        setText(text); 
        setAlignment(alignment);
        setVisible(true);
    }

    public void setText(String text) { this.text = (text == null) ? "" : text; }

    public void setAlignment(int alignment) 
    {
        if (alignment < LEFT || alignment > RIGHT) { throw new IllegalArgumentException("Invalid alignment value: " + alignment); }
        this.alignment = alignment; 
    }
}