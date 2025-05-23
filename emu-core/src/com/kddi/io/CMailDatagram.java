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
package com.kddi.io;

/* 
 * Should implement javax.microedition.io.Datagram, java.io.DataInput and java.io.DataOutput.
 * But this require too many stubbed methods in here, while also risking portability to newer/older
 * JVMs if the Java specification makes any changes to either DataInput or DataOutput. Not worth
 * the cost, especially considering how little use this class will probably have
 */
public class CMailDatagram 
{
    public CMailDatagram() { }

	public String getAddress() { return ""; }

	public byte[] getData() { return null; }

	public int getLength() { return 0; }

	public int getOffset() { return 0; }

	public void reset() { }

	public void setAddress(javax.microedition.io.Datagram reference) { }

	public void setAddress(String addr) { }

	public void setData(byte[] buffer, int offset, int len) { }

	public void setLength(int len) { }

	public void write(int b) { }
	
}