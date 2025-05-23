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
package com.jblend.media.smaf;

import com.jblend.media.MediaData;
import org.recompile.mobile.Mobile;

import java.io.IOException;

public class SmafData extends MediaData 
{

	public static final String type = "SMAF";

	public SmafData() { }

	public SmafData(String name) throws IOException { this(Mobile.getMIDletResourceAsByteArray(name)); }

	public SmafData(byte[] data) { }

	public String getMediaType() { return null; }

	public void setData(byte[] data) { }

	public int getContentType() { return 0; }

	public int getTagStart(int tag) { return 0; }

	public int getTagEnd(int tag) { return 0; }

	public int getWidth() { return 0; }

	public int getHeight() { return 0; }
}