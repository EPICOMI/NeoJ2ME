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

import java.util.Vector;
import java.lang.Object;

public abstract class Object3D
{

	protected int userID = 0;
	protected Object userObject = null;
	Vector<AnimationTrack> animationTracks = new Vector<AnimationTrack>();

	void updateProperty(int property, float[] value) { }

	int applyAnimation(int time) 
	{
		int validity = 0x7FFFFFFF;

		if (animationTracks.isEmpty()) { return validity; }

		int numTracks = animationTracks.size();

		for (int trackIndex = 0; trackIndex < numTracks; ) 
		{
			AnimationTrack track = (AnimationTrack) animationTracks.elementAt(trackIndex);
			KeyframeSequence sequence = track.sequence;

			int components = sequence.componentCount;
			int property = track.property;
			int nextProperty;

			float sumWeights = 0;
			float[] sumValues = new float[components];

			for (int i = 0; i < components; i++) sumValues[i] = 0;

			do 
			{
				float[] weight = new float[1];
				int[] Validity = new int[1];

				track.getContribution(time, sumValues, weight, Validity);
				if (Validity[0] <= 0)
					return 0;

				sumWeights += weight[0];
				validity = Math.min(validity, Validity[0]);

				if (++trackIndex == numTracks) { break; }
				track = (AnimationTrack) animationTracks.elementAt(trackIndex);
				nextProperty = track.property;
			} while (nextProperty == property);

			if (sumWeights > 0) { updateProperty(property, sumValues); }
		}
		return validity;
	}

	public final Object3D duplicate() 
	{
		Object3D copy = duplicateImpl();
		copy.userID = userID;
		copy.userObject = userObject;
		copy.animationTracks = new Vector<AnimationTrack>();
		for (int i = 0; i < animationTracks.size(); i++) { copy.animationTracks.add((AnimationTrack) animationTracks.elementAt(i).duplicateImpl()); }
		
		return copy;
	}

	abstract Object3D duplicateImpl();

	public int doGetReferences(Object3D[] references) 
	{
		if (!animationTracks.isEmpty()) 
		{
			if (references != null) 
			{
				for (int i = 0; i < animationTracks.size(); ++i) 
				{
					references[i] = (Object3D) animationTracks.elementAt(i);
				}
			}
			return animationTracks.size();
		}
		return 0;
	}

	public Object3D findID(int userID) 
	{
		if (this.userID == userID) { return this; }

		if (animationTracks != null) 
		{
			for (int i = 0; i < animationTracks.size(); i++) 
			{
				AnimationTrack track = (AnimationTrack) animationTracks.elementAt(i);
				Object3D found = track.findID(userID);
				if (found != null) { return found; }
			}
		}
			
		return null;
	}

	public Object3D find(int userID) 
	{
		if (this.userID == userID) { return this; }

		return findID(userID);
	}

	public int getReferences(Object3D[] references) { return doGetReferences(references); }

	public int getUserID() { return userID; }

	public void setUserID(int userID) { this.userID = userID; }

	public Object getUserObject() { return this.userObject; }

	public void setUserObject(Object userObject) { this.userObject = userObject; }

	public void addAnimationTrack(AnimationTrack animationTrack) 
	{
		if (animationTrack == null) { throw new NullPointerException(); }

		if (animationTracks.contains(animationTrack))
		{
			throw new IllegalArgumentException("AnimationTrack already exists");
		}

		int newTrackTarget = animationTrack.getTargetProperty();
		int components = animationTrack.getKeyframeSequence().getComponentCount();
		int i;
		for (i = 0; i < animationTracks.size(); i++) 
		{
			AnimationTrack track = (AnimationTrack) animationTracks.elementAt(i);

			if (track.getTargetProperty() > newTrackTarget) { break; }

			if (track.getTargetProperty() == newTrackTarget && (track.getKeyframeSequence().getComponentCount() != components)) 
			{
				throw new IllegalArgumentException();
			}
		}

		animationTracks.add(i, animationTrack);
	}

	public AnimationTrack getAnimationTrack(int index) { return (AnimationTrack) animationTracks.elementAt(index); }

	public void removeAnimationTrack(AnimationTrack animationTrack) { animationTracks.removeElement(animationTrack); }

	public int getAnimationTrackCount() { return animationTracks.size(); }

	public final int animate(int time) { return applyAnimation(time); }

	boolean animTrackCompatible(AnimationTrack animationtrack) { return false; }
}
