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

public abstract class Node extends Transformable
{

	public static final int NONE  = 144;
	public static final int ORIGIN  = 145;
	public static final int X_AXIS  = 146;
	public static final int Y_AXIS  = 147;
	public static final int Z_AXIS  = 148;

	public Node parent = null;
	public Node left;
	public Node right;
	private Node yRef = null;
	private Node zRef = null;
	private int yTarget = NONE;
	private int zTarget = NONE;
	private float alphaFactor = 1f;
	private boolean picking = true;
	private boolean rendering = true;
	private int scope = -1;

	boolean hasRenderables = false;
	boolean hasBones = false;
	boolean[] dirtyBits = new boolean[2]; // {renderablesBit, BonesBit}, used mostly to track for animation changes

	protected void duplicate(Node copy) 
	{
		super.duplicate((Transformable) copy);
		
		copy.parent = null;
		copy.left = left;
		copy.right = right;
		copy.scope = scope;
		copy.zRef = zRef;
		copy.yRef = yRef;
		copy.alphaFactor = alphaFactor;
		copy.zTarget = zTarget;
		copy.yTarget = yTarget;
		copy.picking = picking;
		copy.rendering = rendering;
		copy.scope = scope;
		System.arraycopy(dirtyBits, 0, copy.dirtyBits, 0, dirtyBits.length);
		copy.hasRenderables = hasRenderables;
		copy.hasBones = hasBones;
	}

	boolean doAlign(Node ref) 
	{
		if (ref == null) { return this.computeAlignment(this); }
		else { return this.computeAlignment(ref); }
	}

	public final void align(Node reference) 
	{
		Mobile.log(Mobile.LOG_WARNING, Node.class.getPackage().getName() + "." + Node.class.getSimpleName() + ": " + "Node Alignment requested (untested)");
		if (reference != null && (this.getRootNode() != reference.getRootNode())) { throw new IllegalArgumentException(); }
		
		doAlign(reference == null ? this : reference);
	}

	static void transformAlignmentTarget(int target, float[] transform, float[] out) // transform is a 4x4 matrix, out is a float[4] array (Vec4)
	{
		out[0] = out[1] = out[2] = 0; out[3] = 0;

		switch (target) 
		{
			case ORIGIN:
				out[0] = 0;
				out[1] = 0;
				out[2] = 0;
				out[3] = 1;
				break;
			case X_AXIS:
				out[0] = 1;
				out[1] = 0;
				out[2] = 0;
				out[3] = 0;
				break;
			case Y_AXIS:
				out[0] = 0;
				out[1] = 1;
				out[2] = 0;
				out[3] = 0;
				break;
			case Z_AXIS:
				out[0] = 0;
				out[1] = 0;
				out[2] = 1;
				out[3] = 0;
				break;
		}

		// Transfrom the 4x4 matrix by the out array
		float[] result = new float[4];
		result[0] = transform[0] * out[0] + transform[1] * out[1] + transform[2] * out[2] + transform[3] * out[3];
		result[1] = transform[4] * out[0] + transform[5] * out[1] + transform[6] * out[2] + transform[7] * out[3];
		result[2] = transform[8] * out[0] + transform[9] * out[1] + transform[10] * out[2] + transform[11] * out[3];
		result[3] = transform[12] * out[0] + transform[13] * out[1] + transform[14] * out[2] + transform[15] * out[3];

		System.arraycopy(result, 0, out, 0, 4); // Now we copy the result to out
	}

	boolean computeAlignmentRotation(float[] srcAxis, Node targetNode, int targetAxisName, int constraint) 
	{
		Node parent = this.parent;
		Transform transform = new Transform();
		float[] transformMatrix = new float[16];
		float[] orientation = new float[4];
		float[] targetAxis = new float[] {0, 0, 0, 0};

		if (!targetNode.getTransformTo(parent, transform)) { return false; } 

		getOrientation(orientation);
		getMatrix(transformMatrix);

		transform.preTranslate(transformMatrix[12], transformMatrix[13], transformMatrix[14]);

		if (constraint != NONE) 
		{
			float[] rot = new float[4];
			System.arraycopy(orientation, 0, rot, 0, 4);
			rot[3] = -rot[3];
			transform.preRotate(rot[0], rot[1], rot[2], rot[3]);
		}

		
		transform.get(transformMatrix);

		transformAlignmentTarget(targetAxisName, transformMatrix, targetAxis);

		if (constraint == Z_AXIS) 
		{
			float norm = targetAxis[0] * targetAxis[0] + targetAxis[1] * targetAxis[1];
	
			if (norm < 1.0e-5f) { return true; }
	
			norm = (float) (1.0 / Math.sqrt(norm));
			targetAxis[0] *= norm;
			targetAxis[1] *= norm;
			targetAxis[2] = 0.0f;
		} 
		else 
		{
			// Normalize targetAxis
			float norm = targetAxis[0] * targetAxis[0] + targetAxis[1] * targetAxis[1] + targetAxis[2] * targetAxis[2];
			if (norm > 1.0e-5f) 
			{
				norm = (float) (1.0 / Math.sqrt(norm));
				targetAxis[0] *= norm;
				targetAxis[1] *= norm;
				targetAxis[2] *= norm;
			}
		}

		float[] rot = M3GMath.setQuatRotation(srcAxis, targetAxis);
	
		if (constraint != NONE) 
		{
			float[] newOrientation = new float[4];
			M3GMath.mulQuat(orientation, rot, newOrientation);
			System.arraycopy(newOrientation, 0, orientation, 0, 4);
		} 
		else { System.arraycopy(rot, 0, orientation, 0, 4); }
	
		invalidateTransformable();
		return false;
	}

	boolean computeAlignment(Node refNode) 
	{
		Node root = this.getRootNode();
		Node zRef = this.zRef;
		Node yRef = this.yRef;
		int zTarget = this.zTarget;
		int yTarget = this.yTarget;

		if (zTarget == NONE && yTarget == NONE) { return true; }

		if (zRef != null && (isChildOf(this, zRef) || zRef.getRootNode() != root)) { return false; }
		if (yRef != null && (isChildOf(this, yRef) || yRef.getRootNode() != root)) { return false; }

		if (this.zTarget != NONE) 
		{
			if (zRef == null && refNode == this) { return false; }
			if (!computeAlignmentRotation(new float[] { 0, 0, 1 }, (zRef != null) ? zRef : refNode, zTarget, NONE)) { return false; }
		}

		if (this.yTarget != NONE) 
		{
			if (yRef == null && refNode == this) { return false; }
			if (!computeAlignmentRotation(new float[] { 0, 1, 0 }, (yRef != null) ? yRef : refNode, yTarget, (zTarget != NONE) ? Z_AXIS : NONE)) { return false; }
		}

		return true;
	}

	static boolean isChildOf(Node parent, Node child) 
	{
		Node n;
		for (n = child; n != null; n = n.parent) 
		{
			if (n.parent == parent) { return true; }
		}
			
		return false;
	}

	public Node getAlignmentReference(int axis) 
	{ 
		if(axis == Y_AXIS) { return this.yRef; }
		else if(axis == Z_AXIS) { return this.zRef; }

		/* If it's not Y_AXIS or Z_AXIS, throw IllegalArgumentException as per JSR-184. */
		throw new IllegalArgumentException("Tried requesting alignment reference on invalid axis.");
	}

	public int getAlignmentTarget(int axis) 
	{ 
		if(axis == Y_AXIS) { return this.yTarget; }
		else if(axis == Z_AXIS) { return this.zTarget; }

		/* If it's not Y_AXIS or Z_AXIS, throw IllegalArgumentException as per JSR-184. */
		throw new IllegalArgumentException("Tried requesting alignment target on invalid axis.");
	}

	public float getAlphaFactor() { return this.alphaFactor; }

	public Node getParent() { return this.parent; }

	public int getScope() { return this.scope; }

	public boolean getTransformTo(Node target, Transform transform) 
	{
		if (target == null) { throw new NullPointerException("Target node cannot be null"); }
		if (transform == null) { throw new NullPointerException("Transform object cannot be null"); }
	
		// Initialize a temporary transformation
		Transform compositeTransform = new Transform();
		Node currentNode = this;
	
		// Traverse upwards to find the target node
		while (currentNode != null) 
		{
			if (currentNode == target) 
			{
				// We found the target node, apply the accumulated transformations
				transform.set(compositeTransform);
				return true;
			}
	
			// Accumulate the transformation
			try 
			{
				if (currentNode.getParent() != null) 
				{
					// Multiply the current transform with the parent's transform
					Transform localTransform = new Transform();
					currentNode.getTransform(localTransform);
					compositeTransform.preMultiply(localTransform);
				}
			} 
			catch (ArithmeticException e) { throw new ArithmeticException("Singular transformation encountered"); }
	
			// Move to the parent node
			currentNode = currentNode.getParent();
		}
	
		// If we exit the loop, there was no path to the target node
		return false;
	}

	public boolean isPickingEnabled() { return this.picking; }

	public boolean isRenderingEnabled() { return this.rendering; }

	public void setAlignment(Node zRef, int zTarget, Node yRef, int yTarget) 
	{
		/* 
		 * As per JSR-184, throw IllegalArgumentException if:
		 * yTarget or zTarget is not one of the symbolic constants listed above
		 * (zRef == yRef) && (zTarget == yTarget != NONE)
		 * zRef or yRef is this Node.
		 */
		if ( ((zTarget != this.NONE) && (zTarget != this.X_AXIS) && (zTarget != this.Y_AXIS) && (zTarget != this.Z_AXIS) && (zTarget != this.ORIGIN)) 
			|| ((yTarget != this.NONE) && (yTarget != this.X_AXIS) && (yTarget != this.Y_AXIS) && (yTarget != this.Z_AXIS) && (yTarget != this.ORIGIN)) )
			{ throw new IllegalArgumentException("Node target axis is invalid."); }
		if ((zRef == yRef) && (zTarget != NONE || yTarget != NONE))
			{ throw new IllegalArgumentException("Tried to align with two references having the same axis."); }
		if (zRef == this || yRef == this)
			{ throw new IllegalArgumentException("Tried to use this node as one of the reference nodes."); }
		
		this.zRef = (zTarget != NONE) ? zRef : null;
		this.yRef = (yTarget != NONE) ? yRef : null;
		this.zTarget = zTarget;
		this.yTarget = yTarget;
	}

	public void setAlphaFactor(float alphaFactor) 
	{ 
		/* As per JSR-184, throw IllegalArgumentException if factor < 0 or factor > 1.0.*/
		if (alphaFactor < 0 || alphaFactor > 1)
			{ throw new IllegalArgumentException("Tried to set AlphaFactor with out of range value."); }
		
		this.alphaFactor = alphaFactor; 
	}

	public void setPickingEnable(boolean enable) { this.picking = enable; }

	public void setRenderingEnable(boolean enable) { this.rendering = enable; }

	public void setScope(int scope) { this.scope = scope; }

	void setParent(Node parent) 
	{
		int nonCullableChange = 0, renderableChange = 0;

		if (this instanceof Group) 
		{
			nonCullableChange = ((Group) this).numNonCullables;
			renderableChange = ((Group) this).numRenderables;
		} 
		else if (this instanceof Sprite3D) 
		{
			renderableChange = 1;
			if (!((Sprite3D) this).isScaled()) { nonCullableChange = 1; }
		}
		else if (this instanceof Light) { nonCullableChange = 1; }
		else if (this instanceof SkinnedMesh) 
		{
			nonCullableChange += ((SkinnedMesh) this).skeleton.numNonCullables;
			renderableChange += ((SkinnedMesh) this).skeleton.numRenderables + 1;
		} 
		else if (this instanceof Mesh || this instanceof MorphingMesh) { renderableChange = 1; }

		if (this.parent != null) 
		{
			this.parent.updateNodeCounters(-nonCullableChange, -renderableChange);
			if (renderableChange != 0) { this.parent.invalidateNode(new boolean[]{true, true}); }
		}

		this.parent = parent;

		if (parent != null) 
		{
			boolean[] dirtyBits = new boolean[2];
			System.arraycopy(this.dirtyBits, 0, dirtyBits, 0, 2);
			if (renderableChange != 0) { dirtyBits[0] = true; }
			if (hasBones) { dirtyBits[1] = true; }
			parent.updateNodeCounters(nonCullableChange, renderableChange);
			parent.invalidateNode(dirtyBits);
		}
	}

	boolean compareFlags(boolean[] flags) 
	{
		if (dirtyBits[0] == flags[0] && dirtyBits[1] == flags[1]) { return true; }

		return false;
	}

	void invalidateNode(boolean[] flags) 
	{
		Node node = this;
		while (node != null && !compareFlags(flags)) 
		{
			System.arraycopy(flags, 0, dirtyBits, 0, 2);
			node = node.parent;
		}
	}

	boolean validate(boolean[] state, int scope) 
	{
		if (dirtyBits != null && parent != null) { parent.invalidateNode(dirtyBits); }
		dirtyBits[0] = false;
		dirtyBits[1] = false;
		return true;
	}

	void updateNodeCounters(int nonCullableChange, int renderableChange) 
	{
		boolean hasRenderables = (renderableChange > 0);
		Node node = this;
		while (node != null) 
		{
			if (node instanceof Group || node instanceof World) 
			{
				((Group) node).numNonCullables += nonCullableChange;
				((Group) node).numRenderables += renderableChange;
				hasRenderables = ((Group) node).numRenderables > 0;
			}
			node.hasRenderables = hasRenderables;
			node = node.parent;
		}
	}

	/* Mostly used so we can find whether a child node*/
	public Node getRootNode()
	{
		Node root = this;
		
		while (root.getParent() != null) { root = root.getParent(); }
		
		return root;
	}

	@Override
	void updateProperty(int property, float[] value) 
	{
		Mobile.log(Mobile.LOG_WARNING, Node.class.getPackage().getName() + "." + Node.class.getSimpleName() + ": " + "AnimTrack updating Node property");
		switch (property) 
		{
			case AnimationTrack.ALPHA:
				alphaFactor = (int) (Math.max(0.f, Math.min(1.f, value[0]) * 0xFFFF));
				break;
			case AnimationTrack.PICKABILITY:
				picking = (value[0] >= 0.5f);
				break;
			case AnimationTrack.VISIBILITY:
				rendering = (value[0] >= 0.5f);
				break;
			default:
				super.updateProperty(property, value);
		}
	}


	boolean animTrackCompatible(AnimationTrack track) 
	{
		switch (track.getTargetProperty()) 
		{
			case AnimationTrack.ALPHA:
			case AnimationTrack.VISIBILITY:
			case AnimationTrack.PICKABILITY:
				return true;
			default:
				return super.animTrackCompatible(track);
		}
	}
}
