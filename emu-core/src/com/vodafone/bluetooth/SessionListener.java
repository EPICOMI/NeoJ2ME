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
package com.vodafone.bluetooth;

public abstract interface SessionListener 
{
	public static final int SIGNAL_START = 0;
	public static final int SIGNAL_END = 1;
	public static final int SIGNAL_PAUSE = 2;
	public static final int SIGNAL_WAIT = 3;
	public static final int SIGNAL_REJECT = 4;
	public static final int CONN_OPENED = 5;
	public static final int CONN_CLOSED = 6;
	public static final int CONN_FAILED = 7;
	public static final int SUCCESS = 0;
	public static final int ERROR_NO_CONNECTION = 1;
	public static final int ERROR_GOT_NACK = 2;
	public static final int ERROR_ACK_TIMEOUT = 3;

	public abstract void gotConnectionStatus(int paramInt1, int paramInt2);

	public abstract void gotMemberList(int[] paramArrayOfInt);

	public abstract void gotMessage(int paramInt, String paramString);

	public abstract void gotMessage(int paramInt, byte[] paramArrayOfByte);

	public abstract void gotSignal(int paramInt1, int paramInt2);

	public abstract void gotResult(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2);
}