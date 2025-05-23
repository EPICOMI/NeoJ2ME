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

import org.recompile.mobile.Mobile;

public class MorphingMesh extends Mesh
{

	private VertexBuffer[] targets;
	private float[] weights;

	public MorphingMesh(VertexBuffer base, VertexBuffer[] targets, IndexBuffer[] submeshes, Appearance[] appearances) 
	{
		super(base, submeshes, appearances);
		checkTargets(targets);
		this.targets = targets;
	}

	public MorphingMesh(VertexBuffer base, VertexBuffer[] targets, IndexBuffer submeshes, Appearance appearances) 
	{
		super(base, submeshes, appearances);
		checkTargets(targets);
		this.targets = targets;
	}

	private MorphingMesh() { }

	@Override
	public int doGetReferences(Object3D[] references) 
	{
		int num = super.doGetReferences(references);
		for (int i = 0; i < targets.length; i++) 
		{
			if (targets[i] != null) 
			{
				if (references != null) { references[num] = targets[i]; }
				num++;
			}
		}
		return num;
	}

	@Override
	public Object3D findID(int userID) 
	{
		Object3D found = super.findID(userID);

		for (int i = 0; (found == null) && (i < targets.length); i++)
		{
			if (targets[i] != null) { found = targets[i].findID(userID); }
		}
			
		return found;
	}

	public VertexBuffer getMorphTarget(int index) { return targets[index]; }

	public int getMorphTargetCount() { return targets.length; }

	public void setWeights(float[] weights) 
	{
		if (weights == null) 
		{
			throw new NullPointerException("Weights must not be null");
		}
		this.weights = weights;
	}

	public void getWeights(float[] weights) 
	{
		if (weights == null) 
		{
			throw new NullPointerException("Weights must not be null");
		}
		if (weights.length < getMorphTargetCount()) 
		{
			throw new IllegalArgumentException("Number of weights must be greater or equal to getMorphTargetCount()");
		}
		System.arraycopy(this.weights, 0, weights, 0, this.weights.length);
	}

	private void checkTargets(VertexBuffer[] targets) 
	{

		if (targets == null) 
		{
			throw new NullPointerException();
		}
		if (targets.length == 0) 
		{
			throw new IllegalArgumentException("Skeleton already has a parent");
		}

		boolean hasArrayNullElement = false;
		for (int i = 0; i < targets.length; i++) 
		{
			if (targets[i] == null) { hasArrayNullElement = true; }
		}
		if (hasArrayNullElement) 
		{
			throw new IllegalArgumentException("Target array contains null elements");
		}

	}

	@Override
	public void updateProperty(int property, float[] value) 
	{
		Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "AnimTrack updating morphingMesh property");
		switch (property) 
		{
			case AnimationTrack.MORPH_WEIGHTS:
				for (int i = 0; i < targets.length; i++) 
				{
					if (i < value.length) { weights[i] = value[i]; }
					else { weights[i] = 0; }
				}
				break;
			default:
				super.updateProperty(property, value);
		}
	}
		

	boolean animTrackCompatible(AnimationTrack track) 
	{
		switch (track.getTargetProperty()) 
		{
			case AnimationTrack.MORPH_WEIGHTS:
				return true;
			default:
				return super.animTrackCompatible(track);
		}
	}
}
