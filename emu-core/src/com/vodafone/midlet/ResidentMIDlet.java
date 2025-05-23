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
package com.vodafone.midlet;

import javax.microedition.midlet.MIDlet;

public abstract class ResidentMIDlet extends MIDlet 
{
	public static final int CBS = 1;
	public static final int MMS = 2;
	public static final int SMS = 3;
	public static final int WAP_PUSH = 4;
	public static final int DELIVERY_CONF = 5;

	public abstract void dropped();

	public abstract void notice(String paramString);

	public abstract void received(String paramString1, String paramString2, int paramInt);

	public abstract void ring(String paramString1, String paramString2);
}