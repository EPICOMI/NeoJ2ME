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
package com.jblend.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;

public abstract class ProtocolBase implements Connection, StreamConnection 
{
	public abstract void open(String paramString, int paramInt, boolean paramBoolean);

	public int read() { return 0; }

	public void write(int paramInt) { }

	public void close() { }

	public InputStream openInputStream() { return null; }

	public OutputStream openOutputStream() { return null; }

	public DataInputStream openDataInputStream()  { return null; }

	public DataOutputStream openDataOutputStream() { return null; }
}