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
package javax.microedition.location;

public class Landmark 
{
    
	private String name;
	private QualifiedCoordinates coordinates;
	private AddressInfo address;
	private String description;
	LandmarkStore landmarkStore;

	public Landmark(String name, String description, QualifiedCoordinates coordinates, AddressInfo addressInfo) 
    {
		if (name == null) { throw new NullPointerException("The landmark name cannot be null"); }
		this.name = name;
		this.address = addressInfo;
		this.coordinates = coordinates;
		this.description = description;
	}

	public String getName() { return this.name; }

	public String getDescription() { return this.description; }

	public QualifiedCoordinates getQualifiedCoordinates() { return this.coordinates; }

	public AddressInfo getAddressInfo() { return this.address; }

	public void setName(String name) 
    {
		if (name == null) { throw new NullPointerException("The landmark name cannot be null"); }
		this.name = name;
	}

	public void setDescription(String description) { this.description = description; }

	public void setQualifiedCoordinates(QualifiedCoordinates coordinates) { this.coordinates = coordinates; }

	public void setAddressInfo(AddressInfo addressInfo) { this.address = addressInfo; }
}