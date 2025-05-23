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
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.wireless.messaging.MessageConnectionImpl;

import org.recompile.mobile.Mobile;

public class Connector
{

	public static final int READ = 1;
	public static final int READ_WRITE = 3;
	public static final int WRITE = 2;

	private static OutputStream output = null;
	
	public static InputStream openInputStream(String name)
	{
		if (name.startsWith("scratchpad:"))
		{
			return new com.nttdocomo.util.ScratchPadConnection(name).openInputStream();
		}
		return new InputConnectionImpl(name).openInputStream();
	}

	public static DataInputStream openDataInputStream(String name)
	{
		if (name.startsWith("scratchpad:"))
		{
			return new com.nttdocomo.util.ScratchPadConnection(name).openDataInputStream();
		}
		return new InputConnectionImpl(name).openDataInputStream();
	}

	public static Connection open(String name) { return open(name, 3, false); }

	public static Connection open(String name, int mode) { return open(name, mode, false); }

	public static Connection open(String name, int mode, boolean timeouts) 
	{
		
		if (name.startsWith("scratchpad:"))
		{
			return new com.nttdocomo.util.ScratchPadConnection(name);
		}

		if (name.startsWith("resource:")) // older Siemens phones?
		{
			return new InputConnectionImpl(name);
		}

		if (name.startsWith("http://") || name.startsWith("https://") || name.startsWith("socket://")) { return new HttpConnectionImpl(name); }

		if(Mobile.usingMessagingAPI) 
		{
			return new MessageConnectionImpl(name);
		}
		
		return null; 
	}

	public static DataOutputStream openDataOutputStream(String name) 
	{
		if (name.startsWith("scratchpad:"))
		{
			return new com.nttdocomo.util.ScratchPadConnection(name).openDataOutputStream();
		}
		return new DataOutputStream(output); 
	}

	public static OutputStream openOutputStream(String name) 
	{
		if (name.startsWith("scratchpad:"))
		{
			return new com.nttdocomo.util.ScratchPadConnection(name).openOutputStream();
		}
		return output; 
	}

}
