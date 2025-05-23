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
package javax.microedition.m3g;

public class Appearance extends Object3D
{

	private int layer = 0;
	private CompositingMode compositingMode = null;
	private Fog fog = null;
	private PolygonMode polygonMode = null;
	private Material material = null;
	private Texture2D[] textures;

	public Appearance()
	{
		this.layer = 0;
		this.polygonMode = null;
		this.compositingMode = null;
		this.textures = new Texture2D[Graphics3D.NUM_TEXTURE_UNITS];
		this.material = null;
		this.fog = null;
	}

	Object3D duplicateImpl() 
	{
		Appearance copy = new Appearance();
		copy.layer = layer;
		copy.compositingMode = compositingMode;
		copy.fog = fog;
		copy.polygonMode = polygonMode;
		copy.material = material;
		copy.textures = new Texture2D[textures.length];
		System.arraycopy(this.textures, 0, copy.textures, 0, textures.length);
		return copy;
	}

	@Override
	public int doGetReferences(Object3D[] references) 
	{
		int num = super.doGetReferences(references);
		if (compositingMode != null) 
		{
			if (references != null) { references[num] = compositingMode; }
			num++;
		}
		if (polygonMode != null) 
		{
			if (references != null) { references[num] = polygonMode; }
			num++;
		}
		if (fog != null) 
		{
			if (references != null) { references[num] = fog; }
			num++;
		}
		if (material != null) 
		{
			if (references != null) { references[num] = material; }
			num++;
		}
		for (int i = 0; i < textures.length; i++) 
		{
			if (textures[i] != null) 
			{
				if (references != null) { references[num] = textures[i]; }
				num++;
			}
		}
		return num;
	}

	@Override
	public Object3D findID(int userID) 
	{
		Object3D found = super.findID(userID);

		if ((found == null) && (compositingMode != null)) { found = compositingMode.findID(userID); }
		if ((found == null) && (polygonMode != null)) { found = polygonMode.findID(userID); }
		if ((found == null) && (fog != null)) { found = fog.findID(userID); }
		if ((found == null) && (material != null)) { found = material.findID(userID); }
		
		for (int i = 0; (found == null) && (i < textures.length); i++)
		{
			if (textures[i] != null) { found = textures[i].find(userID); }
		}
		return found;
	}

	@Override
	public int applyAnimation(int time) 
	{
		int minValidity = 0x7FFFFFFF;
		int validity;

		if (compositingMode != null) 
		{
			validity = compositingMode.applyAnimation(time);
			minValidity = Math.min(validity, minValidity);
		}
		if (fog != null) 
		{
			validity = fog.applyAnimation(time);
			minValidity = Math.min(validity, minValidity);
		}
		if (material != null) 
		{
			validity = material.applyAnimation(time);
			minValidity = Math.min(validity, minValidity);
		}
		for (int i = 0; i < textures.length; i++) 
		{
			if (textures[i] != null) 
			{
				validity = textures[i].applyAnimation(time);
				minValidity = Math.min(validity, minValidity);
			}
		}
			
		return minValidity;
	}

	public void setLayer(int layer) { this.layer = layer; }

	public int getLayer() { return layer; }

	public void setFog(Fog fog) { this.fog = fog; }

	public Fog getFog() { return fog; }

	public void setPolygonMode(PolygonMode polygonMode) { this.polygonMode = polygonMode; }

	public PolygonMode getPolygonMode() { return polygonMode; }

	public void setMaterial(Material material) { this.material = material; }

	public Material getMaterial() { return material; }

	public void setCompositingMode(CompositingMode comp) { this.compositingMode = comp; }

	public CompositingMode getCompositingMode() { return this.compositingMode; }

	public void setTexture(int index, Texture2D texture) 
	{
		if (index < 0 || index >= textures.length) 
		{
			throw new IndexOutOfBoundsException("index must be in [0," + textures.length + "]");
		}
		textures[index] = texture;
	}

	public Texture2D getTexture(int index) 
	{
		if (index < 0 || index >= textures.length) 
		{
			throw new IndexOutOfBoundsException("index must be in [0," + textures.length + "]");
		}
			
		return textures[index];
	}

}
