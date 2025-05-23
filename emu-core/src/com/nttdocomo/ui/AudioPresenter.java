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

public class AudioPresenter implements MediaPresenter 
{

    public static final int AUDIO_PLAYING = 1;
    public static final int AUDIO_STOPPED = 2;
    public static final int AUDIO_COMPLETE = 3;

    private MediaListener listener;
    private MediaData mediaData;
    private MediaSound mediaSound;

    protected AudioPresenter() {}

    public static AudioPresenter getAudioPresenter() { return new AudioPresenter();  }

    public static AudioPresenter getAudioPresenter(int param) 
    {
        AudioPresenter presenter = new AudioPresenter();
        
        return presenter;
    }

    public MediaResource getMediaResource() 
    {
        return null;
    }

    public void play() 
    {
        if (listener != null) 
        {
            listener.mediaAction(this, AUDIO_PLAYING, 0);
        }
    }

    public void stop() 
    {
        if (listener != null) 
        {
            listener.mediaAction(this, AUDIO_STOPPED, 0);
        }
    }

    public void setData(MediaData data) { }

    public void setSound(MediaSound sound) 
    {
        this.mediaSound = sound; 
    }

    public void setAttribute(int attr, int value) { }

    public void setMediaListener(MediaListener listener) 
    {
        this.listener = listener;
    }

    public void unuse() { }

    public void dispose() { }
}