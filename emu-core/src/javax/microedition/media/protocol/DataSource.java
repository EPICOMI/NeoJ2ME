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
package javax.microedition.media.protocol;

import javax.microedition.media.Control;
import javax.microedition.media.Controllable;

import org.recompile.mobile.Mobile;

public abstract class DataSource implements Controllable
{
	String locator;

	public DataSource(java.lang.String locator)
	{
		Mobile.log(Mobile.LOG_DEBUG, DataSource.class.getPackage().getName() + "." + DataSource.class.getSimpleName() + ": " + "Media DataSource Locator:" + locator);
		this.locator = locator;
	}

	public abstract void connect();

	public abstract void disconnect();

	public abstract java.lang.String getContentType();

	public abstract Control getControl(java.lang.String controlType);

	public abstract Control[] getControls();

	public java.lang.String getLocator() { return locator; }

	public abstract SourceStream[] 	getStreams();

	public abstract void start();

	public abstract void stop();
}
