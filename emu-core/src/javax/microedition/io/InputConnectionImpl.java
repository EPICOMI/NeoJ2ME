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
package javax.microedition.io;

import java.io.InputStream;
import java.io.DataInputStream;

import org.recompile.mobile.Mobile;

public class InputConnectionImpl implements InputConnection 
{

	private String name;
	private int mode;
	private boolean timeouts;

	public InputConnectionImpl(String name) 
	{ 
		this.name = name; 
		Mobile.log(Mobile.LOG_WARNING, InputConnectionImpl.class.getPackage().getName() + "." + InputConnectionImpl.class.getSimpleName() + ": " + "New Connector: "+ this.name);
	}

	public InputConnectionImpl(String name, int mode) 
	{ 
		this.name = name; 
		this.mode = mode;
		Mobile.log(Mobile.LOG_WARNING, InputConnectionImpl.class.getPackage().getName() + "." + InputConnectionImpl.class.getSimpleName() + ": " + "New Connector: "+ this.name + ". mode " + this.mode);
	}

	public InputConnectionImpl(String name, int mode, boolean timeouts) 
	{ 
		this.name = name; 
		this.mode = mode; 
		this.timeouts = timeouts;
		Mobile.log(Mobile.LOG_WARNING, InputConnectionImpl.class.getPackage().getName() + "." + InputConnectionImpl.class.getSimpleName() + ": " + "New Connector: "+ this.name + ". mode " + this.mode + ". timeout:" + (this.timeouts ? "true" : "false"));
	}

	public void close() 
	{
		this.name = null;
	}

	public DataInputStream openDataInputStream() 
	{
		return new DataInputStream(openInputStream());
	}

	public InputStream openInputStream() 
	{
		return Mobile.getMIDletResourceAsStream(name);
	}
}
