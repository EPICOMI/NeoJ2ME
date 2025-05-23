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

import java.io.IOException;
import java.util.Enumeration;

public class LandmarkStore 
{
    
	public static LandmarkStore getInstance(String paramString) { return null; }

	public static void createLandmarkStore(String paramString) throws IOException, LandmarkException { }

	public static void deleteLandmarkStore(String paramString) throws IOException, LandmarkException { }

	public static String[] listLandmarkStores() throws IOException { return null; }

	public void addLandmark(Landmark paramLandmark, String paramString) throws IOException { }

	public Enumeration getLandmarks(String paramString1, String paramString2) throws IOException { return null; }

	public Enumeration getLandmarks() throws IOException { return null; }

	public Enumeration getLandmarks(String paramString, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) throws IOException { return null; }

	public void removeLandmarkFromCategory(Landmark paramLandmark, String paramString) throws IOException { }

	public void updateLandmark(Landmark paramLandmark) throws LandmarkException, IOException { }

	public void deleteLandmark(Landmark paramLandmark) throws LandmarkException, IOException { }

	public Enumeration getCategories() { return null; }

	public void addCategory(String paramString) throws IOException, LandmarkException { }

	public void deleteCategory(String paramString) throws IOException, LandmarkException { }
}