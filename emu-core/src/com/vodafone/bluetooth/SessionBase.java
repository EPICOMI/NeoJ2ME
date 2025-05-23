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

public abstract class SessionBase 
{
	public static final int REPORT_NONE = 0;
	public static final int REPORT_ERROR = 1;
	public static final int REPORT_RESULT = 2;

	SessionBase(SessionListener paramSessionListener) { }

	public boolean close(int paramInt) { return false; }

	public int send(int[] paramArrayOfInt, String paramString, int paramInt) { return 0; }

	public int send(int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt) { return 0; }

	public int sendSignal(int[] paramArrayOfInt, int paramInt1, int paramInt2) { return 0; }

	public void cleanAllMessage() { }
}