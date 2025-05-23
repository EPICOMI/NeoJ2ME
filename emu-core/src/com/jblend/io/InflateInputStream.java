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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

public class InflateInputStream extends InputStream 
{

	private final InflaterInputStream in;

	public InflateInputStream(InputStream in) { this.in = new InflaterInputStream(in); }

	public int read() throws IOException { return in.read(); }

	public int read(byte[] b) throws IOException {return in.read(b); }

	public int read(byte[] b, int off, int len) throws IOException { return in.read(b, off, len); }

	public long skip(long n) throws IOException { return in.skip(n); }

	public void close() throws IOException { in.close(); }
}