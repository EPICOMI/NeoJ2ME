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

public class VisualPresenter extends Component implements MediaPresenter 
{
    public static final int IMAGE_XPOS = 1;
    public static final int IMAGE_YPOS = 2;
    public static final int VISUAL_COMPLETE = 3;
    public static final int VISUAL_PLAYING = 1;
    public static final int VISUAL_STOPPED = 2;

    private MediaData mediaData;
    private MediaImage mediaImage;
    private MediaListener mediaListener;

    public VisualPresenter() { }

    public void setData(MediaData data) 
    {
        if (data == null) { throw new NullPointerException("Media data cannot be null"); }
        this.mediaData = data;
    }

    public void setImage(MediaImage mediaImage) 
    {
        if (mediaImage == null) 
        {
            throw new NullPointerException("Media image cannot be null");
        }
        this.mediaImage = mediaImage;
    }

    public MediaResource getMediaResource() 
    {
        return mediaData;
    }

    public void play() {
        if (mediaData == null) { throw new UIException(1, "Media data not set"); }
    }

    public void stop() { }

    public void setAttribute(int attr, int val) {
        if (attr != IMAGE_XPOS && attr != IMAGE_YPOS) { throw new IllegalArgumentException("Invalid attribute"); }
    }

    public void setMediaListener(MediaListener listener) { this.mediaListener = listener; }
}