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
package com.j_phone.midlet;

import com.j_phone.system.MailListener;
import com.j_phone.system.RingStateListener;
import com.j_phone.system.ScheduledAlarmListener;
import com.j_phone.system.TelephonyListener;

import javax.microedition.midlet.MIDlet;

public abstract class ResidentMIDlet extends MIDlet implements TelephonyListener, MailListener, ScheduledAlarmListener, RingStateListener 
{
	public abstract void ring(String paramString1, String paramString2);

	public abstract void ignored();

	public abstract void received(String paramString1, String paramString2, int paramInt);

	public abstract void notice(String paramString);

	public abstract void ringStarted();

	public abstract void ringStopped();
}