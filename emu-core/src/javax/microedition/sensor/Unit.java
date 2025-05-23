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
package javax.microedition.sensor;

import org.recompile.mobile.Mobile;

public class Unit
{
	private String symbol;

	public Unit(String symbol) 
	{ 
		Mobile.log(Mobile.LOG_WARNING, Unit.class.getPackage().getName() + "." + Unit.class.getSimpleName() + ": " + "Created new Unit with symbol " + symbol + ".");
		this.symbol = symbol; 
	}

	public static Unit getUnit(String symbol) { return new Unit(symbol); }

	public String toString() { return symbol; }
}
