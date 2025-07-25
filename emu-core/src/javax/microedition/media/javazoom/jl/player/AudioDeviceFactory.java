/*
 * 11/19/04		1.0 moved to LGPL.
 * 29/01/00		Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javazoom.jl.decoder.JavaLayerException;

/**
 * An <code>AudioDeviceFactory</code> class is responsible for creating
 * a specific <code>AudioDevice</code> implementation. A factory implementation
 * can be as simple or complex as desired and may support just one implementation
 * or may return several implementations depending upon the execution
 * environment. 
 * <p>
 * When implementing a factory that provides an AudioDevice that uses
 * class that may not be present, the factory should dynamically link to any
 * specific implementation classes required to instantiate or test the audio
 * implementation. This is so that the application as a whole
 * can run without these classes being present. The audio
 * device implementation, however, will usually statically link to the classes
 * required. (See the JavaSound deivce and factory for an example
 * of this.)
 * 
 * @see FactoryRegistry
 * 
 * @since	0.0.8
 * @author	Mat McGowan
 */
public abstract class AudioDeviceFactory
{
	/**
	 * Creates a new <code>AudioDevice</code>.
	 * 
	 * @return	a new instance of a specific class of <code>AudioDevice</code>.
	 * @throws	JavaLayerException if an instance of AudioDevice could not
	 *			be created. 
	 */
	public abstract AudioDevice createAudioDevice() throws JavaLayerException;
	
	/**
	 * Creates an instance of an AudioDevice implementation. 
	 * @param loader	The <code>ClassLoader</code> to use to
	 *					load the named class, or null to use the
	 *					system class loader.
	 * @param name		The name of the class to load.
	 * @return			A newly-created instance of the audio device class.
	 */
	@SuppressWarnings("unchecked") // This is assumed to be type-safe
	protected AudioDevice instantiate(ClassLoader loader, String name)
		throws ClassNotFoundException, 
			   IllegalAccessException, 
			   InstantiationException,
			   InvocationTargetException,
			   NoSuchMethodException
	{
		AudioDevice dev = null;
		
		Class cls = null;
		if (loader==null)
		{
			cls = Class.forName(name);
		}
		else
		{
			cls = loader.loadClass(name);	
		}

		Constructor constructor = cls.getDeclaredConstructor();
		Object o = constructor.newInstance();
		dev = (AudioDevice)o;
		
		return dev;
	}
}
