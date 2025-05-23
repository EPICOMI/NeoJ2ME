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

public final class ImageLabel extends Component 
{
    private Image image;

    public ImageLabel()  { this.image = null; }

    public ImageLabel(Image image) 
    {
        if (image == null) { throw new NullPointerException("Image cannot be null"); }
        this.image = image;
    }

    public void setImage(Image image) 
    {
        if (image == null) { throw new NullPointerException("Image cannot be null"); }
        if (image.isDisposed()) { throw new UIException(1, "Cannot set a disposed image"); }
        this.image = image;
    }

}