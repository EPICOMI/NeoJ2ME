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
package com.nokia.mid.payment;

import java.io.IOException;
import java.io.InputStream;

public class DrmResourceInputStream extends InputStream 
{

	public int read() throws IOException { return 0; }

	public void close() throws IOException { }

	public int available() throws IOException { return 0; }

	public int read(byte[] b, int off, int len) throws IOException { return 0; }

	public void mark(int readlimit) { }

	public boolean markSupported() { return false; }

	public void reset() throws IOException { }

	public long skip(long n) throws IOException { return 0; }
}