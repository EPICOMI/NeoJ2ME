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
package com.jblend.media;

import com.jblend.media.smaf.SmafPlayer;
import org.recompile.mobile.Mobile;

import java.io.IOException;

public class MediaFactory 
{

	public static final int MEDIA_TYPE_SMAF = 1;
	public static final int MEDIA_TYPE_KARAOKE = 11;

	public static MediaPlayer getMediaPlayer(String name) throws IOException 
    {
		return getMediaPlayer(Mobile.getMIDletResourceAsByteArray(name));
	}

	public static MediaPlayer getMediaPlayer(byte[] data) 
    {
		return new SmafPlayer(data);
	}

	public static MediaPlayer getMediaPlayer(String paramString, int paramInt) throws IOException 
    {
		return getMediaPlayer(Mobile.getMIDletResourceAsByteArray(paramString), paramInt);
	}

	public static MediaPlayer getMediaPlayer(byte[] paramArrayOfByte, int paramInt) 
    {
		return new SmafPlayer(paramArrayOfByte);
	}
}