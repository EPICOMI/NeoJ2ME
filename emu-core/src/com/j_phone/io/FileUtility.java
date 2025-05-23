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
package com.j_phone.io;

import com.jblend.media.MediaData;
import com.jblend.media.MediaFactory;
import com.jblend.media.MediaPlayer;
import com.jblend.media.smaf.SmafData;
import org.recompile.mobile.Mobile;

import java.io.IOException;

public final class FileUtility 
{
	public static final int WRITABLE = 0;
	public static final int EXISTS = 1;
	public static final int INSUFFICIENT = 2;
	public static final int COUNT_LIMIT = 3;
	public static final int FILETYPE_DIFFERENT = 4;
	public static final int WRITE_PROTECT = 5;
	public static final int OTHER_ERROR = -1;

	public static FileUtility getInstance() { return new FileUtility(); }

	public void play(String paramString) throws IOException 
    {
		Mobile.log(Mobile.LOG_WARNING, FileUtility.class.getPackage().getName() + "." + FileUtility.class.getSimpleName() + ": " +"(Not implemented) FileUtility.play " + paramString);
		play(Mobile.getMIDletResourceAsByteArray(paramString), 1);
	}

	public void play(byte[] paramArrayOfByte, int paramInt) throws IOException 
    {
		Mobile.log(Mobile.LOG_WARNING, FileUtility.class.getPackage().getName() + "." + FileUtility.class.getSimpleName() + ": " +"(Not implemented) FileUtility.play bytes " + paramInt);
	}

    public MediaPlayer getMediaPlayer(String paramString) throws IOException 
    {
        return MediaFactory.getMediaPlayer(paramString);
    }

    public MediaPlayer getMediaPlayer(String paramString, int paramInt) throws IOException 
    {
		return MediaFactory.getMediaPlayer(paramString, paramInt);
    }

    public MediaData getMediaData(String paramString) throws IOException 
    {
        return new SmafData(paramString);
    }

    public MediaData getMediaData(String paramString, int paramInt) throws IOException { return new SmafData(paramString); }

	public int getFreeSpace(String paramString) throws IOException 
    {
		Mobile.log(Mobile.LOG_WARNING, FileUtility.class.getPackage().getName() + "." + FileUtility.class.getSimpleName() + ": " +"(Not implemented) FileUtility.getFreeSpace " + paramString);
		return 0;
	}

	public int precheckStorable(String paramString, int paramInt) 
    {
		Mobile.log(Mobile.LOG_WARNING, FileUtility.class.getPackage().getName() + "." + FileUtility.class.getSimpleName() + ": " +"(Not implemented) FileUtility.precheckStorable " + paramString + " " + paramInt);
		return 0;
	}
}