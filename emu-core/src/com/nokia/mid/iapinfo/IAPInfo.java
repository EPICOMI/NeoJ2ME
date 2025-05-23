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
package com.nokia.mid.iapinfo;

import org.recompile.mobile.Mobile;

public abstract class IAPInfo 
{

    public IAPInfo() { Mobile.log(Mobile.LOG_WARNING, IAPInfo.class.getPackage().getName() + "." + IAPInfo.class.getSimpleName() + ": " + "IAPInfo constructor called."); }

    public abstract AccessPoint getAccessPoint(int id);

    public abstract AccessPoint getAccessPoint(String name);

    public abstract AccessPoint[] getaAccessPoints();

    public abstract AccessPoint[] getConnectionPreferences();

    public abstract DestinationNetwork getDestinationNetwork(int id);

    public abstract DestinationNetwork getDestinationNetwork(String name);

    public abstract DestinationNetwork[] getDestinationNetworks();

    public abstract AccessPoint getLastUsedAccessPoint();

    public static IAPInfo getIAPInfo() { return null; }
}