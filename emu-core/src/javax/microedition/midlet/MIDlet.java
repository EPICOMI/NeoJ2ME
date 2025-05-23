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
package javax.microedition.midlet;

import java.util.HashMap;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Display;

import org.recompile.mobile.Mobile;

public abstract class MIDlet
{
	public static HashMap<String, String> properties;

	private Display display = new Display();

	protected MIDlet()
	{
		Mobile.log(Mobile.LOG_INFO, MIDlet.class.getPackage().getName() + "." + MIDlet.class.getSimpleName() + ": " + "Create MIDlet");
		Mobile.midlet = this;
	}


	public final int checkPermission(String permission)
	{
		// 0 - denied; 1 - allowed; -1 unknown
		Mobile.log(Mobile.LOG_INFO, MIDlet.class.getPackage().getName() + "." + MIDlet.class.getSimpleName() + ": " + "checkPermission: "+permission);
		return -1;
	}

	protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

	public String getAppProperty(String key)
	{ 
		return properties.get(key);
	}

	public static void initAppProperties(HashMap<String, String> initProperties)
	{
		properties = initProperties;
	}

	public final void notifyDestroyed()
	{ 
		Mobile.log(Mobile.LOG_INFO, MIDlet.class.getPackage().getName() + "." + MIDlet.class.getSimpleName() + ": " + "MIDlet sent Destroyed Notification");
		for (StackTraceElement element : Thread.currentThread().getStackTrace()) 
        {
            Mobile.log(Mobile.LOG_DEBUG, MIDlet.class.getPackage().getName() + "." + MIDlet.class.getSimpleName() + ": " + element);
        }
		System.exit(0);
	}

	public final void notifyPaused() { }

	protected abstract void pauseApp();

	public void callPauseApp() 
	{
		pauseApp();
	}

	public final boolean platformRequest(String URL) { return false; }

	public final void resumeRequest() { }

	protected abstract void startApp() throws MIDletStateChangeException;

	public Display getDisplay() { return display; }

}
