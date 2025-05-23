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

import java.util.Hashtable;
import java.util.List;

import javax.microedition.lcdui.Graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.image.DataBufferInt;

import org.recompile.mobile.Mobile;

public class Graphics3D
{

	public static final int ANTIALIAS = 2;
	public static final int DITHER = 4;
	public static final int OVERWRITE = 16; // This might be unused here, as SW rasterization gives us direct control over pixels
	public static final int TRUE_COLOR = 8;


	public static final boolean SUPPORT_ANTIALIASING = false;
	public static final boolean SUPPORT_TRUE_COLOR = false;
	public static final boolean SUPPORT_DITHERING = false;
	public static final boolean SUPPORT_MIPMAPPING = false;
	public static final boolean SUPPORT_PERSPECTIVE_CORRECTION = true;
	public static final boolean SUPPORT_LOCAL_CAMERA_LIGHTING = false;
	public static final int MAX_LIGHTS = 8;
	public static final int MAX_VIEWPORT_WIDTH = 1024;
	public static final int MAX_VIEWPORT_HEIGHT = 1024;
	public static final int MAX_VIEWPORT_DIMENSION = 1024;
	public static final int MAX_TEXTURE_DIMENSION = 512;
	public static final int MAX_SPRITE_CROP_DIMENSION = 256;
	public static final int MAX_TRANSFORMS_PER_VERTEX = 4;
	public static final int NUM_TEXTURE_UNITS = 1;
	private static Hashtable properties;

	// Render target
	private Object target;

	private static Graphics3D instance = null;

	// Viewport
	private int viewx;
	private int viewy;
	private int vieww;
	private int viewh;

	private boolean depthEnabled;
	private float[] depthBuffer;
	private float near;
	private float far;

	private int hints;

	private Camera currCam;
	private Transform currCamTrans;
	private Transform currCamTransInv;
	private ArrayList<Light> currLights;
	private ArrayList<Transform> currLightTrans;

	// Reusable rendering variables
	int[] rasterData;
	int canvasWidth, canvasHeight;
	
	// Untextured polygon variables
	Color colorOrig;
	GradientPaint gradient;

	final int[] coXr = new int[3];
	final int[] coYr = new int[3]; 

	byte[][] color_vertex = new byte[3][4]; 
	
	Color[] colors = new Color[3];

	// Textured polygon variables
	final float[] coX = new float[3];
	final float[] coY = new float[3];
	final float[] coZ = new float[3];
	final float[] coS = new float[3];
	final float[] coT = new float[3];

	final float[] xOrdered = new float[3];
	final float[] yOrdered = new float[3];
	final float[] zOrdered = new float[3];
	final float[] sOrdered = new float[3];
	final float[] tOrdered = new float[3];

	float xTop, yTop, zTop, sTop, tTop;
	float xMidL, yMid, zMidL, sMidL, tMidL;
	float xBot, yBot, zBot, sBot, tBot;

	float rHorizon, xMidR, zMidR, sMidR, tMidR;


	public Graphics3D()
	{
		/* 
		 * The default depth range used is that of window coordinates, so 0 to near, and 1 to far
		 * JSR-184 specifies that Normalized Device Coordinates (NDC) can also be used, which ranges from -1 to 1.
		 */
		this.near = 0f;
		this.far = 1f;
		this.currCam = null;
		this.currCamTrans = null;
		this.currCamTransInv = null;
		this.currLights = new ArrayList<Light>();
		this.currLightTrans = new ArrayList<Transform>();
	}


	public int addLight(Light light, Transform transform)
	{
		/* As per JSR-184, addLight() must throw a NullPointerException if no light is given */
		if (light == null) { throw new NullPointerException("addLight() was called but no light object was provided."); }

		if (transform == null) { transform = new Transform(); }

		this.currLights.add(light);
		this.currLightTrans.add(transform);
		return this.currLights.size() - 1;
	}

	public void bindTarget(Object target)
	{
		/* Calls the method below specifying the depth buffer as enabled, and no render hints, as per JSR-184. */
		this.bindTarget(target, true, 0);
	}

	public void bindTarget(Object target, boolean depthBuffer, int hints)
	{
		/* 
		 * As per JSR-184, this function returns: 
		 * NullPointerException: If no render target is received as argument
		 * IllegalStateException: If the current Graphics3D Object already has a render target
		 */
		if (target == null) { throw new NullPointerException("bindTarget() was called but no render target was provided."); }
		if (this.target != null) { throw new IllegalStateException("This Graphics3D object already has a render target."); }

		/* The target can be an Image2D Object, or a Graphics Object. */
		if (target instanceof Image2D)
		{
			Image2D i2d = (Image2D) target;

			/* JSR-184 specifies that Image2D render targets can only have RGB or RGBA format. */
			if (i2d.getFormat() != Image2D.RGB && i2d.getFormat() != Image2D.RGBA)
			{ throw new IllegalArgumentException("Received a 2D render target with invalid internal format"); }

			/* It's a 2D image, so paint the canvas with it starting from the top-left corner */
			this.viewx = 0;
			this.viewy = 0;
			this.vieww = i2d.getWidth();
			this.viewh = i2d.getHeight();
		}
		else if (target instanceof Graphics)
		{
			Graphics pgrp = (Graphics) target;
			this.viewx = pgrp.getClipX();
			this.viewy = pgrp.getClipY();
			this.vieww = pgrp.getClipWidth();
			this.viewh = pgrp.getClipHeight();
			rasterData = ((DataBufferInt) pgrp.getCanvas().getRaster().getDataBuffer()).getData();
			canvasWidth = pgrp.getCanvas().getWidth();
			canvasHeight = pgrp.getCanvas().getHeight();
		} else 
		{
			/* If it is neither of those, throw an IllegalArgumentException as per JSR-184. */ 
			throw new IllegalArgumentException("Received render target is neither an instance of Image2D nor Graphics");
		}

		/* 
		 * The final check performed before binding throws IllegalArgumentException if:
		 * 1 - The render target's width is larger than the max supported.
		 * 2 - The render target's height is taller than the max supported.
		 * 3 - The render hint is an OR bitmask that matches with one or more of [ANTIALIAS, DITHER, TRUE_COLOR, OVERWRITE], or not zero.
		 */
		if (this.vieww > MAX_VIEWPORT_WIDTH || this.viewh > MAX_VIEWPORT_HEIGHT || (hints & ~(ANTIALIAS | DITHER | TRUE_COLOR | OVERWRITE)) != 0)
			{ throw new IllegalArgumentException("Render target either has larger dimensions than supported, or the render hint is invalid"); }

		this.target = target;
		this.depthBuffer = new float[this.vieww * this.viewh];
		Arrays.fill(this.depthBuffer, this.far);
		this.depthEnabled = depthBuffer;
		this.hints = hints;
	}

	public void clear(Background background)
	{
		/* As per JSR-184, throw IllegalStateException if this Graphics3D object does not have a render target. */
		if (this.target == null) { throw new IllegalStateException("Cannot clear Background on a Graphics3D without a render target."); }

		int color = 0;
		int x = viewx;
		int y = viewy;
		int w = vieww;
		int h = viewh;
		boolean clearColor = true;
		boolean clearDepth = true;

		if (background != null)
		{
			color = background.getColor();
			x = background.getCropX();
			y = background.getCropY();
			w = background.getCropWidth();
			h = background.getCropHeight();
			clearColor = background.isColorClearEnabled();
			clearDepth = background.isDepthClearEnabled();
		}
		else { color = 0x00000000; }

		/* 
		 * If the background object is null: 
		 * Color buffer is cleared to transparent black 
		 * Depth buffer is cleared to the max depth value, 1.0.
		 */

		if (clearColor)
		{
			if (this.target instanceof Image2D)
			{
				Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "Clear to Image2D not Implemented");
				Image2D i2d = (Image2D) this.target;

				// CHECK is the bg image used only if clearColor is true?

				if (background.getImage() == null || background.getImage().getFormat() != i2d.getFormat())
				{ throw new IllegalArgumentException("The background image to be cleared does not have the same format as the render target."); }

				// TODO support clearing Image2D
			}
			else if (this.target instanceof Graphics)
			{
				Graphics grp = (Graphics) this.target;

				// Fill the background with the background color
				grp.getGraphics2D().setColor(new Color(color));
				grp.getGraphics2D().fillRect(x, y, w, h);

				// Draw the background's image if any (and there's a background)
				if(background != null && background.getImage() != null) 
				{
					Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "Clear with Background Image Untested");
					for(; y < h; y++) 
					{
						for(; x < w; x++) 
						{
							rasterData[y * canvasWidth + x] = background.getImage().getConvertedPixel(x, y);
						}
					}
				}
			}
		}

		if (clearDepth) { Arrays.fill(this.depthBuffer, this.far); }
	}

	public Camera getCamera(Transform transform)
	{
		if (transform != null) { transform.set(this.currCamTrans); }
		return this.currCam;
	}

	public float getDepthRangeFar() { return far; }

	public float getDepthRangeNear() { return near;}

	public int getHints() { return hints; }

	public static Graphics3D getInstance() 
	{ 
		if( instance == null) { instance = new Graphics3D(); } 
		return instance; 
	}

	public Light getLight(int index, Transform transform)
	{
		/* As per JSR-184, throw IndexOutOfBoundsException if the requested light index is out of bounds. */
		if (index < 0 || index > this.currLights.size()) { throw new IndexOutOfBoundsException("The received light index is out of bounds."); }

		/* If a transform variable is received, use it to store the requested light's transform. */
		if (transform != null) { transform.set(this.currLightTrans.get(index)); }

		return this.currLights.get(index);
	}

	/* This is supposed to include nulls, so just return the size */
	public int getLightCount() { return this.currLights.size(); }

	public static Hashtable getProperties()
	{
		if (Graphics3D.properties != null)
			return Graphics3D.properties;

		Hashtable<String, Object> p = new Hashtable<String, Object>();
		p.put("supportAntialiasing", SUPPORT_ANTIALIASING);
		p.put("supportTrueColor", SUPPORT_TRUE_COLOR);
		p.put("supportDithering", SUPPORT_DITHERING);
		p.put("supportMipmapping", SUPPORT_MIPMAPPING);
		p.put("supportPerspectiveCorrection", SUPPORT_PERSPECTIVE_CORRECTION);
		p.put("supportLocalCameraLighting", SUPPORT_LOCAL_CAMERA_LIGHTING);
		p.put("maxLights", MAX_LIGHTS);
		p.put("maxViewportWidth", MAX_VIEWPORT_WIDTH);
		p.put("maxViewportHeight", MAX_VIEWPORT_HEIGHT);
		p.put("maxViewportDimension", MAX_VIEWPORT_DIMENSION);
		p.put("maxTextureDimension", MAX_TEXTURE_DIMENSION);
		p.put("maxSpriteCropDimension", MAX_SPRITE_CROP_DIMENSION);
		p.put("maxTransformsPerVertex", MAX_TRANSFORMS_PER_VERTEX);
		p.put("numTextureUnits", NUM_TEXTURE_UNITS);
		Graphics3D.properties = p;

		return Graphics3D.properties;
	}

	public static int getTextureUnitCount() { return NUM_TEXTURE_UNITS; }

	public Object getTarget() { return this.target; }

	public int getViewportHeight() { return viewh; }

	public int getViewportWidth() { return vieww; }

	public int getViewportX() { return viewx; }

	public int getViewportY() { return viewy; }

	public boolean isDepthBufferEnabled() { return this.depthEnabled; }

	public void releaseTarget()
	{
		/* Ignore the call if no render target is bound. */
		if(this.target != null) 
		{
			/* 
			 * TODO: Flush the rendered 3D image to this target before releasing it 
			 * in order to ensure that the 3D image becomes visible.
			 */
			
			/* If there is a render target, release it */ 
			this.target = null;
		}
	}

	public void render(World world)
	{
		/* Clear the background first */
		clear(world.getBackground());

		/* As per JSR-184, throw NullPointerException if the received world is null. */
		if (world == null) { throw new NullPointerException("render(world) was called but no world was provided."); }
		
		/* Also per JSR-184, throw IllegalStateException this object has no render target yet. */
		if (this.target == null) { throw new IllegalStateException("render(world) was called but there is no render target."); }

		Transform tr = new Transform();

		Camera worldCamera = world.getActiveCamera();

		if(worldCamera == null) { throw new IllegalStateException("Cannot render a world that has no active camera."); }

		if(!worldCamera.getTransformTo(world, tr)) { throw new IllegalStateException("Active camera is not in world."); }
		
		/* 
		 * if the bg-img of `world` is not the same format as `this.target`:
		 * throw new IllegalStateException();
		 */

		setCamera(worldCamera, tr);
		resetLights();
		positionLights(world, world);

		render((Group) world, new Transform());
	}

	public void render(Node node, Transform transform)
	{
		/* As per JSR-184, throw NullPointerException if no node is received. */
		if(node == null) { throw new NullPointerException("render() was called but no node was provided."); }
	
		/* Also per JSR-184, throw IllegalStateException if this method is called but there's no camera or render target available. */ 
		if (this.target == null || this.currCam == null) { throw new IllegalStateException("render() was called but there is no camera or render target."); }

		/* Also per JSR-184, throw IllegalStateException if if node is not a Sprite3D, Mesh, or Group Object. */
		if (!(node instanceof Mesh || node instanceof Sprite3D || node instanceof Group)) { throw new IllegalArgumentException("Node is not an instance of any of the following: Sprite3D, Mesh, Group"); }

		// if any Mesh that is rendered violates the constraints defined in
		//    Mesh, MorphingMesh, SkinnedMesh, VertexBuffer, or IndexBuffer
		//    throw new java.lang.IllegalStateException();

		if (node instanceof Mesh) 
		{
			Mesh mesh = (Mesh) node;
			int subMeshes = mesh.getSubmeshCount();
			VertexBuffer vertices = mesh.getVertexBuffer();
			for (int i = 0; i < subMeshes; i++) 
			{
				if (mesh.getAppearance(i) != null) { render(vertices, mesh.getIndexBuffer(i), mesh.getAppearance(i), transform, node.getScope()); }
			}
		}
		else if (node instanceof Sprite3D) 
		{
			Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "Graphics3D.render Node: Sprite3D Not Implemented!");
		}
		else if (node instanceof Group) 
		{
			Node child = ((Group) node).firstChild;
			if (child != null) 
			{
				do 
				{
					if (child != (Object3D) node) 
					{
						if(child instanceof Sprite3D || child instanceof Mesh || child instanceof Group) 
						{
							Transform t = new Transform();
							child.getCompositeTransform(t);
							t.preMultiply(transform);
							render(child, t); 
						}
					}
					child = child.right;
				} while (child != ((Group) node).firstChild);
			}
		}
	}

	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform) 
	{ this.render(vertices, triangles, appearance, transform, -1); }

	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform, int scope) 
	{
		/* TODO: Check the scope used by the submesh to find out which lights need to be applied, if it needs to be rendered, etc. */

		/* As per JSR-184, if vertices, triangles or appearence are null, throw a NullPointerException. */
		if (vertices == null || triangles == null || appearance == null) { throw new NullPointerException("Tried to render a submesh with incomplete info."); }
		
		/* Also per JSR-184, throw IllegalStateException if the application tries to render without having set up a render target or camera beforehand. */
		if (this.target == null || this.currCam == null) { throw new IllegalStateException("Tried to render a submesh without having a render target or camera first."); }
		
		// if `vertices` or `triangles` violates the constraints
		//    defined in VertexBuffer or IndexBuffer
		//    throw new java.lang.IllegalStateException();

		/* Receiving a null transform indicates that the identity matrix must be used. */
		if (transform == null) { transform = new Transform(); }

		CompositingMode compositingMode = appearance.getCompositingMode() != null ? appearance.getCompositingMode() : new CompositingMode();
		
		// TODO: Shading mode is not implemented
		int shadingMode = appearance.getPolygonMode() != null ? appearance.getPolygonMode().getShading() : PolygonMode.SHADE_SMOOTH;
		
		int cullingMode = appearance.getPolygonMode() != null ? appearance.getPolygonMode().getCulling() : PolygonMode.CULL_BACK;
		int windingOrder = appearance.getPolygonMode() != null ? appearance.getPolygonMode().getWinding() : PolygonMode.WINDING_CCW;
		boolean perspectiveCorrectionEnabled = appearance.getPolygonMode() != null ? appearance.getPolygonMode().isPerspectiveCorrectionEnabled() : false;

		// Handle winding order
		Integer[] ord = {0, 1, 2};

		if (windingOrder == PolygonMode.WINDING_CW) 
		{
			Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "Polygon Winding is Clockwise! Untested, might render incorrectly");
			ord = new Integer[]{0, 2, 1}; // Adjust order for Clockwise Winding 
		}

		// Set up fog properties
		Fog fog = appearance.getFog();
		float fogFactor[] = { 0.0f, 0.0f, 0.0f };

		float[] scaleBias = new float[4];

		Transform tr = new Transform();
		Transform textr = new Transform();
		Transform texcomptr = new Transform();

		VertexArray vertColors = vertices.getColors();
		VertexArray vertPos = vertices.getPositions(scaleBias);
		int vertCount = vertPos.getVertexCount();

		int[] triIndices = new int[triangles.getIndexCount()];
		triangles.getIndices(triIndices);

		// Scale and translate mesh
		tr.preScale(scaleBias[0], scaleBias[0], scaleBias[0]);
		tr.preTranslate(scaleBias[1], scaleBias[2], scaleBias[3]);

		Texture2D tex = appearance.getTexture(0);
		Image2D teximg = tex == null ? null : tex.getImage();
		VertexArray texCoords = vertices.getTexCoords(0, scaleBias); // get Texture coordinates

		if (tex != null) { tex.getCompositeTransform(texcomptr); }

		// Scale and translate texture coordinates (same scaleBias)
		textr.preScale(scaleBias[0], scaleBias[0], scaleBias[0]);
		textr.preTranslate(scaleBias[1], scaleBias[2], scaleBias[3]);
		textr.preMultiply(texcomptr);
		
		// -> Local space

		Transform projection = new Transform();
		this.currCam.getProjection(projection);

		// Transform mesh from local coords to world coords
		tr.preMultiplyTry(transform);
		// -> World space

		// Apply the inverse of the camera's transform to the mesh
		tr.preMultiplyTry(this.currCamTransInv);
		// -> View space

		// Apply projection matrix
		tr.preMultiply(projection);
		// -> Clip space

		// Do the transformation
		float[] vertClip = new float[4 * vertCount];
		tr.transform(vertPos, vertClip, true);

		float[] texVert = new float[4 * vertCount];
		if (texCoords != null) { textr.transform(texCoords, texVert, true); }

		// Create Triangle objects for clipping
		Triangle[] trisClip = Triangle.fromVertAndTris(vertClip, texVert, triIndices);
		int renderableTriangles = 0; // Counter for non-culled triangles

		for (Triangle tri : trisClip) 
		{
			for (int i = 0; i < 3; i++) // Go through vertices A, B and C
			{ 
				int index = i * 4;
				float w = tri.v[index + 3];
				if (w >= near) // W cannot be smaller than the near plane, otherwise we'll erroneously cull triangles close to the camera
				{ 
					tri.v[index + 0] /= w; // x / w
					tri.v[index + 1] /= w; // y / w
					tri.v[index + 2] /= w; // z / w
				}
			}

			boolean cullTriangle = (cullingMode == PolygonMode.CULL_BACK && tri.isCounterClockwise()) ||
								(cullingMode == PolygonMode.CULL_FRONT && !tri.isCounterClockwise());
			if (!cullTriangle) 
			{
				trisClip[renderableTriangles++] = tri; // Move non-culled triangles to the front of the array (culled stuff will be dropped in "trisScreen")

				// We now have to restore the renderable geometry back to its original coordinates, otherwise rendering will be broken
				for (int i = 0; i < 3; i++) 
				{
					int index = i * 4;
					float w = tri.v[index + 3];
					tri.v[index + 0] *= w; // x * w
					tri.v[index + 1] *= w; // y * w
					tri.v[index + 2] *= w; // z * w
				}
			}
		}
		
		// Clip the remaining triangles (less work than clipping everything, THEN culling)
		Triangle[] trisScreen = Arrays.stream(Arrays.copyOf(trisClip, renderableTriangles))
						.flatMap(t -> t.clip())
						.toArray(Triangle[]::new);
		// At this point the triangles in `trisScreen` are actually
		// in Normalized Device Coordinates, but they will be tranformed
		// to Screen space in-place, hence the name.

		// Reset transform
		tr.setIdentity();
		textr.setIdentity();

		// Fit to viewport
		tr.preScale(1, -1, 1);
		tr.preTranslate(1, 1, 0);
		tr.preScale((float) vieww / 2f, (float) viewh / 2f, 1f);
		if (teximg != null) { textr.preScale(teximg.getWidth(), teximg.getHeight(), 1); }

		// -> Screen space

		// Perform viewport transform
		Triangle.transform(trisScreen, tr, textr);

		if (this.target instanceof Image2D)
		{
			Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "Render Target is instance of Image2D!");
			Image2D i2d = (Image2D) this.target;
			// TODO support rendering to Image2D
		}
		else if (this.target instanceof Graphics)
		{
			Graphics pgrp = (Graphics) this.target;
			Graphics2D grp = pgrp.getGraphics2D();

			colorOrig = grp.getColor();

			for (int tri_id = 0; tri_id < trisScreen.length; tri_id++)
			{

				// If perspective correction is enabled, do it for texture coordinates
				if (perspectiveCorrectionEnabled) 
				{
					// Get the w components for each triangle vertex
					float wA = trisScreen[tri_id].wA();
					float wB = trisScreen[tri_id].wB();
					float wC = trisScreen[tri_id].wC();
				
					// W again cannot be smaller than the near plane, otherwise it'll result in incorrect calculations
					if (wA > near && wB > near && wC > near) 
					{
						// Calculate perspective correction through Inverse-Z.
						float invW_A = 1.0f / wA;
						float invW_B = 1.0f / wB;
						float invW_C = 1.0f / wC;
				
						float[] texCoordA = 
						{
							trisScreen[tri_id].sA() * invW_A,
							trisScreen[tri_id].tA() * invW_A,
							0, // rA
							1  // qA
						};
						float[] texCoordB = 
						{
							trisScreen[tri_id].sB() * invW_B,
							trisScreen[tri_id].tB() * invW_B,
							0, // rB
							1  // qB
						};
						float[] texCoordC = 
						{
							trisScreen[tri_id].sC() * invW_C,
							trisScreen[tri_id].tC() * invW_C,
							0, // rC
							1  // qC
						};
				
						// Set the corrected texture coordinates back into the triangle
						trisScreen[tri_id].setTexCoords(texCoordA, texCoordB, texCoordC);
					}
				}
				

				// If there's no texture coords or a texture image, we should try rendering with vertex colors. (also used for debug render modes)
				if (tex == null || texCoords == null || Mobile.M3GRenderUntexturedPolygons || Mobile.M3GRenderWireframe) 
				{
					coXr[0] = Math.round(trisScreen[tri_id].xA());
					coXr[1] = Math.round(trisScreen[tri_id].xB());
					coXr[2] = Math.round(trisScreen[tri_id].xC());

					coYr[0] = Math.round(trisScreen[tri_id].yA());
					coYr[1] = Math.round(trisScreen[tri_id].yB());
					coYr[2] = Math.round(trisScreen[tri_id].yC());
					
					grp.translate(viewx, viewy);
					if(vertices.getColors() == null) // If there's no vertex colors, we have to render with the VertexBuffer's default color.
					{ 
						grp.setColor(new Color(vertices.getDefaultColor()));

						if(Mobile.M3GRenderWireframe) { grp.drawPolygon(coXr, coYr, 3); }
						else { grp.fillPolygon(coXr, coYr, 3); }
					} 
					else // If we have vertex colors, good. Read them to color up the triangles properly.
					{
						for (int i = 0; i < 3; i++) 
        				{
							vertColors.get(trisScreen[tri_id].bufIndex[i], 1, color_vertex[i]);

							colors[i] = new Color(
								Byte.toUnsignedInt(color_vertex[i][0]), 
								Byte.toUnsignedInt(color_vertex[i][1]), 
								Byte.toUnsignedInt(color_vertex[i][2]), 
								vertColors.getComponentCount() == 4 ? Byte.toUnsignedInt(color_vertex[i][3]) : 255
							);
						}

						// Blend fog value with the vertex color, if applicable
						if(fog != null) 
						{
							if (fog.getMode() == Fog.LINEAR) 
							{
								float nearDistance = fog.getNearDistance();
								float farDistance = fog.getFarDistance();
								fogFactor[0] = Math.max(0, Math.min(1, (farDistance - trisScreen[tri_id].zA()) / (farDistance - nearDistance)));
								fogFactor[1] = Math.max(0, Math.min(1, (farDistance - trisScreen[tri_id].zB()) / (farDistance - nearDistance)));
								fogFactor[2] = Math.max(0, Math.min(1, (farDistance - trisScreen[tri_id].zC()) / (farDistance - nearDistance)));
							} 
							else 
							{
								fogFactor[0] = (float) Math.exp(-fog.getDensity() * trisScreen[tri_id].zA());
								fogFactor[0] = Math.max(0, Math.min(1, fogFactor[0])); // Clamp to the [0, 1] interval
								fogFactor[1] = (float) Math.exp(-fog.getDensity() * trisScreen[tri_id].zB());
								fogFactor[1] = Math.max(0, Math.min(1, fogFactor[1]));
								fogFactor[2] = (float) Math.exp(-fog.getDensity() * trisScreen[tri_id].zC());
								fogFactor[2] = Math.max(0, Math.min(1, fogFactor[2]));
							}

							for(int i = 0; i < colors.length; i++) 
							{
								colors[i] = new Color(blendFog(colors[i].getRGB(), fog.getColor(), fogFactor[i]));
							}
						}

						/* 
						 * TODO: Not accurate, as all 3 vertices of a triangle can have different colors that have to be interpolated,
						 * this method is not doing it the correct way.
						 */
						Paint originalPaint = grp.getPaint();

						// Draw first gradient from color1 to color2
						gradient = new GradientPaint(
							coXr[0], coYr[0], colors[0],
							coXr[1], coYr[1], colors[1]
						);
						grp.setPaint(gradient);
						
						if(Mobile.M3GRenderWireframe) { grp.drawPolygon(coXr, coYr, 3); }
						else { grp.fillPolygon(coXr, coYr, 3); }
						

						// Draw second gradient from color2 to color3
						gradient = new GradientPaint(
							coXr[1], coYr[1], colors[1],
							coXr[2], coYr[2], colors[2]
						);
						grp.setPaint(gradient);

						if(Mobile.M3GRenderWireframe) { grp.drawPolygon( new int[]{coXr[1], coXr[2], coXr[0]}, new int[]{coYr[1], coYr[2], coYr[0]}, 3 ); }
						else { grp.fillPolygon( new int[]{coXr[1], coXr[2], coXr[0]}, new int[]{coYr[1], coYr[2], coYr[0]}, 3 ); }

						grp.setPaint(originalPaint);
					}

					grp.translate(-viewx, -viewy);
					continue;
				}

				// Collect vertex attributes
				coX[0] = trisScreen[tri_id].xA(); coX[1] = trisScreen[tri_id].xB(); coX[2] = trisScreen[tri_id].xC();
				coY[0] = trisScreen[tri_id].yA(); coY[1] = trisScreen[tri_id].yB(); coY[2] = trisScreen[tri_id].yC();
				coZ[0] = trisScreen[tri_id].zA(); coZ[1] = trisScreen[tri_id].zB(); coZ[2] = trisScreen[tri_id].zC();
				coS[0] = trisScreen[tri_id].sA(); coS[1] = trisScreen[tri_id].sB(); coS[2] = trisScreen[tri_id].sC();
				coT[0] = trisScreen[tri_id].tA(); coT[1] = trisScreen[tri_id].tB(); coT[2] = trisScreen[tri_id].tC();

				// Instead of using the previous Arrays.sort() call. Let's handle position and winding sorting by hand, i think it's more readable.
				int topIdx = ord[0], midIdx = ord[1], botIdx = ord[2];

				if (coY[midIdx] < coY[topIdx]) 
				{
					int temp = topIdx;
					topIdx = midIdx;
					midIdx = temp;
				}
				if (coY[botIdx] < coY[topIdx]) 
				{
					int temp = topIdx;
					topIdx = botIdx;
					botIdx = temp;
				}
				if (coY[botIdx] < coY[midIdx]) 
				{
					int temp = midIdx;
					midIdx = botIdx;
					botIdx = temp;
				}

				// Assign ordered vertex attributes based on their determined order
				xTop = coX[topIdx]; xMidL = coX[midIdx]; xBot = coX[botIdx];
				yTop = coY[topIdx]; yMid = coY[midIdx]; yBot = coY[botIdx];
				zTop = coZ[topIdx]; zMidL = coZ[midIdx]; zBot = coZ[botIdx];
				sTop = coS[topIdx]; sMidL = coS[midIdx]; sBot = coS[botIdx];
				tTop = coT[topIdx]; tMidL = coT[midIdx]; tBot = coT[botIdx];

				// Calculate the right horizon
				rHorizon = (yMid - yTop) / (yBot - yTop);
				xMidR = xTop + rHorizon * (xBot - xTop);
				zMidR = zTop + rHorizon * (zBot - zTop);
				sMidR = sTop + rHorizon * (sBot - sTop);
				tMidR = tTop + rHorizon * (tBot - tTop);

				// Swap midpoints if necessary
				if (xMidL > xMidR) 
				{
					float temp;

					// Swap values between left and right midpoints
					temp = xMidL; xMidL = xMidR; xMidR = temp;
					temp = zMidL; zMidL = zMidR; zMidR = temp;
					temp = sMidL; sMidL = sMidR; sMidR = temp;
					temp = tMidL; tMidL = tMidR; tMidR = temp;
				}

				// Draw both halves of the triangle
				for (int half = 0; half < 2; half++) 
				{
					// Determine the range for the y-coordinate
					int yStart = half == 0 ? Math.max(Math.round(yTop), 0) : Math.max(Math.round(yMid), 0);
					int yEnd = half == 0 ? Math.min(Math.round(yMid), viewh) : Math.min(Math.round(yBot), viewh);
					
					// Adjust drawY calculation based on half
					for (int y = yStart; y < yEnd; y++) 
					{
						float drawY = half == 0
							? (y - yTop) / (yMid - yTop)  // Upper half
							: 1f - (y - yMid) / (yBot - yMid); // Lower half
						drawY = Math.max(0f, Math.min(drawY, 1f));

						// Calculate interpolated values
						float xL = half == 0
							? xTop + drawY * (xMidL - xTop)
							: xBot + drawY * (xMidL - xBot);
						float xR = half == 0
							? xTop + drawY * (xMidR - xTop)
							: xBot + drawY * (xMidR - xBot);
						float zL = half == 0
							? zTop + drawY * (zMidL - zTop)
							: zBot + drawY * (zMidL - zBot);
						float zR = half == 0
							? zTop + drawY * (zMidR - zTop)
							: zBot + drawY * (zMidR - zBot);
						float sL = half == 0
							? sTop + drawY * (sMidL - sTop)
							: sBot + drawY * (sMidL - sBot);
						float sR = half == 0
							? sTop + drawY * (sMidR - sTop)
							: sBot + drawY * (sMidR - sBot);
						float tL = half == 0
							? tTop + drawY * (tMidL - tTop)
							: tBot + drawY * (tMidL - tBot);
						float tR = half == 0
							? tTop + drawY * (tMidR - tTop)
							: tBot + drawY * (tMidR - tBot);


						final int ixL = Math.max(Math.round(xL), 0), ixR = Math.min(Math.round(xR), vieww);

						// Draw the pixels for the current y-coordinate
						for (int x = ixL; x < ixR; x++) 
						{
							try 
							{
								float drawX = (x - xL) / (xR - xL);
								drawX = Math.max(0f, Math.min(drawX, 1f));
								float z = zL + drawX * (zR - zL);
								
								// Only depth test if the compositingMode has the feature enabled. If compositingMode is not set, check if this target has depthBuffer enabled
								if(compositingMode.isDepthTestEnabled() && isDepthBufferEnabled())
								{
									// Depth testing and depth buffer updates don't need to match against the pixel's translated viewport coordinates, if they are translated
									if (this.depthBuffer[this.vieww * y + x] < z) { continue; } // Skip if this pixel is not visible
								}
								
								float s = sL + drawX * (sR - sL);
								float t = tL + drawX * (tR - tL);
								int texPixel = teximg.getConvertedPixel(Math.round(s), Math.round(t));

								// Extract the alpha channel from the texture pixel
								int alpha = (texPixel >> 24) & 0xFF; // Assuming ARGB format

								if (alpha < (int) (compositingMode.getAlphaThreshold() * 255)) { continue; } // Skip transparent pixels below the alpha threshold

								// Blend the pixel with the background
								int backgroundPixel = rasterData[(y+viewy) * canvasWidth + (x+viewx)];
								int blendedPixel = texPixel;

								if (vertColors != null) // We have to do texture blending, as we have vertex colors and the texture goes on top of them
								{
									// Get vertex indices
									int[] indices = { trisScreen[tri_id].bufIndex[0], trisScreen[tri_id].bufIndex[1], trisScreen[tri_id].bufIndex[2] };
									int[] colors = new int[3];
									byte[] color_vertex = new byte[4];

									for (int i = 0; i < 3; i++) 
									{
										color_vertex = new byte[4];
										vertColors.get(indices[i], 1, color_vertex);
										colors[i] = (vertColors.getComponentCount() == 3)
											? (255 << 24) | (Byte.toUnsignedInt(color_vertex[0]) << 16) |
											(Byte.toUnsignedInt(color_vertex[1]) << 8) | Byte.toUnsignedInt(color_vertex[2])
											: (Byte.toUnsignedInt(color_vertex[3]) << 24) |
											(Byte.toUnsignedInt(color_vertex[0]) << 16) |
											(Byte.toUnsignedInt(color_vertex[1]) << 8) | Byte.toUnsignedInt(color_vertex[2]);
									}

									// Clipped triangles tend to have two vertices sharing the exact same x or y coordinate, which is a problem
									// for area calculations. We could use barycentric coordinates, but they don't seem to work at all in this context
									// and result in about half of the screen triangles having invalid weights and blending improperly.

									// The best approach so far is making sure that none of the areas below result in zero, which is why these small shifts are present.
									// Hacky, but it's what works best for now
									final float epsilon = 0.001f;

									// Ensure distinct x and y values
									if (Math.abs(xTop - xBot) < epsilon)  { xTop += epsilon; }
									if (Math.abs(xMidL - xBot) < epsilon) { xMidL += epsilon; }
									if (Math.abs(yBot - yTop) < epsilon)  { yTop += epsilon; }
									if (Math.abs(yMid - yTop) < epsilon)  { yMid += epsilon; }
									if (Math.abs(yMid - yBot) < epsilon)  { yMid += epsilon; }

									// Calculate weights based on pixel position
									float totalArea = Math.abs((xBot - xTop) * (yMid - yTop) - (xBot - xMidL) * (yBot - yTop));
									float areaA = Math.abs((xBot - xTop) * (y - yTop) - (x - xTop) * (yBot - yTop));
									float areaB = Math.abs((xMidL - xTop) * (y - yTop) - (x - xTop) * (yMid - yTop));
									float areaC = Math.abs((x - xBot) * (yMid - yTop) - (xBot - xMidL) * (y - yTop));

									float weightA = areaA / totalArea;
									float weightB = areaB / totalArea;
									float weightC = areaC / totalArea;

									// Normalize weights
									float totalWeight = weightA + weightB + weightC;
									weightA /= totalWeight;
									weightB /= totalWeight;
									weightC /= totalWeight;

									// Interpolate color based on weights
									int r = (int) ((weightA * ((colors[0] >> 16) & 0xFF)) + (weightB * ((colors[1] >> 16) & 0xFF)) + (weightC * ((colors[2] >> 16) & 0xFF)));
									int g = (int) ((weightA * ((colors[0] >> 8) & 0xFF)) + (weightB * ((colors[1] >> 8) & 0xFF)) + (weightC * ((colors[2] >> 8) & 0xFF)));
									int b = (int) ((weightA * (colors[0] & 0xFF)) + (weightB * (colors[1] & 0xFF)) + (weightC * (colors[2] & 0xFF)));

									int interpolatedColor = (alpha << 24) | (r << 16) | (g << 8) | b; // ARGB

									// Blend with texture pixel
									blendedPixel = blendTexPixels(interpolatedColor, texPixel, alpha, tex.getBlending());
								}

								// To blend the fog value here, we have to take the current pixel's z value into consideration
								if(fog != null) 
								{
									if (fog.getMode() == Fog.LINEAR) 
									{
										float nearDistance = fog.getNearDistance();
										float farDistance = fog.getFarDistance();
										fogFactor[0] = Math.max(0, Math.min(1, (farDistance - z) / (farDistance - nearDistance)));
									} 
									else 
									{
										fogFactor[0] = (float) Math.abs(Math.exp(-fog.getDensity() * z));
										fogFactor[0] = Math.max(0, Math.min(1, fogFactor[0]));
									}

									blendedPixel = blendFog(blendedPixel, fog.getColor(), fogFactor[0]);
								}

								// Handle compositing mode AFTER the fog calculation, otherwise alpha values won't be correct
								blendedPixel = blendPixels(backgroundPixel, blendedPixel, alpha, compositingMode.getBlending());

								rasterData[(y+viewy) * canvasWidth + (x+viewx)] = blendedPixel;

								// Update depth buffer, same as depth test, check this target's DepthBuffer if compositingMode is absent
								if(compositingMode.isDepthWriteEnabled() && isDepthBufferEnabled()) 
								{ 
									this.depthBuffer[this.vieww * y + x] = z;
								}

							} catch (Exception e) { Mobile.log(Mobile.LOG_WARNING, Graphics3D.class.getPackage().getName() + "." + Graphics3D.class.getSimpleName() + ": " + "Error drawing triangle:" + e.getMessage()); e.printStackTrace(); }
						}
					}
				}
			}
			grp.setColor(colorOrig);
		}
	}

	private void positionLights(World world, Object3D obj) 
	{
		int numReferences = obj.getReferences(null);
		if (numReferences > 0) 
		{
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for (int i = 0; i < numReferences; ++i) 
			{
				if (objArray[i] instanceof Light) 
				{
					Transform t = new Transform();
					Light light = (Light) objArray[i];
					if (light.isRenderingEnabled() && light.getTransformTo(world, t)) { addLight(light, t); }
				}
				positionLights(world, objArray[i]);
			}
		}
	}

	public void resetLights()
	{
		this.currLights.clear();
		this.currLightTrans.clear();
	}

	public void setCamera(Camera camera, Transform transform)
	{
		this.currCam = camera;

		/* If no transform is given, the identity matrix is used as per JSR-184. */
		if (transform == null) 
		{ 
			this.currCamTrans = new Transform();
			this.currCamTransInv = new Transform();
		}
		else /* Else, set the transform and its inverse accordingly. */
		{
			this.currCamTrans = new Transform(transform);
			this.currCamTransInv = new Transform(transform);
		}
		this.currCamTransInv.invert(); /* This one will execute regardless of the given transform above. */
	}

	public void setDepthRange(float near, float far)
	{
		/* As per JSR-184, throw IllegalArgumentException if the received near and/or far planes have unsupported values. */
		if (near < 0 || far < 0 || 1 < near || 1 < far) { throw new IllegalArgumentException("The requested Depth Range values are invalid."); }
		else 
		{ 
			this.near=near; this.far=far;
			Arrays.fill(this.depthBuffer, this.far);
		}	
	}

	public void setLight(int index, Light light, Transform transform)
	{
		/* As per JSR-184, throw IndexOutOfBoundsException if index < 0 or index > CurrentAmountOfLights. */
		if (index < 0 || index > this.currLights.size()) { throw new IndexOutOfBoundsException("Tried to modify a Light on an out-of-bounds index."); }

		/* If no transform is received, use the identity matrix. */
		if (transform == null) { transform = new Transform(); }

		// Indices are NOT supposed to change here,
		// so we're simply updating the arrays at the index,
		// even if any new value is null.
		this.currLights.set(index, light);
		this.currLightTrans.set(index, transform);
	}

	public void setViewport(int x, int y, int width, int height)
	{
		/* As per JSR-184, throw IllegalArgumentException if the received width and height are < 0, or beyond the max allowed. */
		if (width <= 0 || height <= 0 || width > MAX_VIEWPORT_WIDTH || height > MAX_VIEWPORT_HEIGHT)
			{ throw new IllegalArgumentException("Tried to set a viewport of unsupported size."); }

		this.viewx = x;
		this.viewy = y;
		this.vieww = width;
		this.viewh = height;
	}


	/* Helper Methods */

	// This one is used for pixel blending when rendering to the screen
	private int blendPixels(int background, int foreground, int alpha, int blendMode) 
	{
		int bgA = (background >> 24) & 0xFF;
		int bgR = (background >> 16) & 0xFF;
		int bgG = (background >> 8) & 0xFF;
		int bgB = background & 0xFF;

		int fgA = (foreground >> 24) & 0xFF;
		int fgR = (foreground >> 16) & 0xFF;
		int fgG = (foreground >> 8) & 0xFF;
		int fgB = foreground & 0xFF;

		int outR, outG, outB, outA;

		float alphaNorm = alpha / 255f;

		switch (blendMode)
		{
			case CompositingMode.REPLACE:
				outA = (int) (fgA + (bgA * (1 - (fgA / 255f))));
				outR = (int) (fgR * (fgA / 255f) + bgR * (1 - (fgA / 255f)));
				outG = (int) (fgG * (fgA / 255f) + bgG * (1 - (fgA / 255f)));
				outB = (int) (fgB * (fgA / 255f) + bgB * (1 - (fgA / 255f)));
				return (Math.max(0, Math.min(outA, 255)) << 24) | 
					(Math.max(0, Math.min(outR, 255)) << 16) | 
					(Math.max(0, Math.min(outG, 255)) << 8) | 
					Math.max(0, Math.min(outB, 255));

			case CompositingMode.ALPHA_ADD:
				outR = (int) Math.min(255, (fgR * alphaNorm) + bgR);
				outG = (int) Math.min(255, (fgG * alphaNorm) + bgG);
				outB = (int) Math.min(255, (fgB * alphaNorm) + bgB);
				outA = (int) Math.min(255, bgA + (int)(alpha * (1 - (bgA / 255f))));
				return (outA << 24) | (outR << 16) | (outG << 8) | outB;

			case CompositingMode.ALPHA:
				outR = (int) ((fgR * alphaNorm) + (bgR * (1 - alphaNorm)));
				outG = (int) ((fgG * alphaNorm) + (bgG * (1 - alphaNorm)));
				outB = (int) ((fgB * alphaNorm) + (bgB * (1 - alphaNorm)));
				outA = (int) (bgA * (1 - alphaNorm) + fgA * alphaNorm);
				return (Math.max(0, Math.min(outA, 255)) << 24) | 
					(Math.max(0, Math.min(outR, 255)) << 16) | 
					(Math.max(0, Math.min(outG, 255)) << 8) | 
					Math.max(0, Math.min(outB, 255));

			case CompositingMode.MODULATE:
				outR = (int) ((fgR * bgR) / 255);
				outG = (int) ((fgG * bgG) / 255);
				outB = (int) ((fgB * bgB) / 255);
				outA = Math.max(bgA, fgA);
				return (Math.max(0, Math.min(outA, 255)) << 24) | 
					(Math.max(0, Math.min(outR, 255)) << 16) | 
					(Math.max(0, Math.min(outG, 255)) << 8) | 
					Math.max(0, Math.min(outB, 255));

			case CompositingMode.MODULATE_X2:
				outR = (int) (((2 * fgR) * bgR) / 255);
				outG = (int) (((2 * fgG) * bgG) / 255);
				outB = (int) (((2 * fgB) * bgB) / 255);
				outA = Math.max(bgA, fgA);
				return (Math.max(0, Math.min(outA, 255)) << 24) | 
					(Math.max(0, Math.min(outR, 255)) << 16) | 
					(Math.max(0, Math.min(outG, 255)) << 8) | 
					Math.max(0, Math.min(outB, 255));

			default:
				return background; // Fallback
		}
	}

	// Similar to blendPixels, however, this blends texture colors (foreground) with the geometry's vertex color (background)
	private int blendTexPixels(int background, int foreground, int alpha, int blendMode) 
	{
		int bgA = (background >> 24) & 0xFF;
		int bgR = (background >> 16) & 0xFF;
		int bgG = (background >> 8) & 0xFF;
		int bgB = background & 0xFF;

		int fgA = (foreground >> 24) & 0xFF; // Extract alpha from the foreground
		int fgR = (foreground >> 16) & 0xFF;
		int fgG = (foreground >> 8) & 0xFF;
		int fgB = foreground & 0xFF;

		int outR, outG, outB, outA;

		float alphaNorm = alpha / 255f; // Normalize alpha for blending

		switch (blendMode)
		{
			case Texture2D.FUNC_REPLACE:
				// Blend foreground and background based on the foreground alpha
				outA = (int) (fgA + (bgA * (1 - fgA / 255f)));
				outR = (int) ((fgR * fgA / 255) + (bgR * (1 - fgA / 255)));
				outG = (int) ((fgG * fgA / 255) + (bgG * (1 - fgA / 255)));
				outB = (int) ((fgB * fgA / 255) + (bgB * (1 - fgA / 255)));
				return (Math.min(Math.max(outA, 0), 255) << 24) | 
					(Math.min(Math.max(outR, 0), 255) << 16) | 
					(Math.min(Math.max(outG, 0), 255) << 8) | 
					Math.min(Math.max(outB, 0), 255);

			case Texture2D.FUNC_MODULATE:
				outR = (fgR * bgR) / 255;
				outG = (fgG * bgG) / 255;
				outB = (fgB * bgB) / 255;
				outA = Math.max(bgA, fgA); // Take maximum alpha
				return (Math.min(Math.max(outA, 0), 255) << 24) | 
					(Math.min(Math.max(outR, 0), 255) << 16) | 
					(Math.min(Math.max(outG, 0), 255) << 8) | 
					Math.min(Math.max(outB, 0), 255);

			case Texture2D.FUNC_DECAL:
				outR = (fgR * fgA / 255) + (bgR * (255 - fgA) / 255);
				outG = (fgG * fgA / 255) + (bgG * (255 - fgA) / 255);
				outB = (fgB * fgA / 255) + (bgB * (255 - fgA) / 255);
				outA = fgA; // Use foreground's alpha
				return (Math.min(Math.max(outA, 0), 255) << 24) | 
					(Math.min(Math.max(outR, 0), 255) << 16) | 
					(Math.min(Math.max(outG, 0), 255) << 8) | 
					Math.min(Math.max(outB, 0), 255);

			case Texture2D.FUNC_BLEND:
				outR = (int) ((fgR * alphaNorm) + (bgR * (1 - alphaNorm)));
				outG = (int) ((fgG * alphaNorm) + (bgG * (1 - alphaNorm)));
				outB = (int) ((fgB * alphaNorm) + (bgB * (1 - alphaNorm)));
				outA = (int) (alpha * alphaNorm + bgA * (1 - alphaNorm));
				return (Math.min(Math.max(outA, 0), 255) << 24) | 
					(Math.min(Math.max(outR, 0), 255) << 16) | 
					(Math.min(Math.max(outG, 0), 255) << 8) | 
					Math.min(Math.max(outB, 0), 255);

			case Texture2D.FUNC_ADD:
				outR = Math.min(bgR + fgR, 255);
				outG = Math.min(bgG + fgG, 255);
				outB = Math.min(bgB + fgB, 255);
				outA = Math.max(bgA, fgA); // Use maximum alpha
				return (Math.min(Math.max(outA, 0), 255) << 24) | 
					(outR << 16) | 
					(outG << 8) | 
					outB;

			default:
				return background; // Fallback
		}
	}

	private int interpolateTexColors(int color0, int color1, int color2, float alpha, float beta, float gamma) 
	{
		int a0 = (color0 >> 24) & 0xFF;
		int r0 = (color0 >> 16) & 0xFF;
		int g0 = (color0 >> 8) & 0xFF;
		int b0 = color0 & 0xFF;
	
		int a1 = (color1 >> 24) & 0xFF;
		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = color1 & 0xFF;
	
		int a2 = (color2 >> 24) & 0xFF;
		int r2 = (color2 >> 16) & 0xFF;
		int g2 = (color2 >> 8) & 0xFF;
		int b2 = color2 & 0xFF;
	
		int r = Math.round(r0 * alpha + r1 * beta + r2 * gamma);
		int g = Math.round(g0 * alpha + g1 * beta + g2 * gamma);
		int b = Math.round(b0 * alpha + b1 * beta + b2 * gamma);
		int a = Math.round(a0 * alpha + a1 * beta + a2 * gamma);
	
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int blendFog(int pixelColor, int fogColor, float fogFactor) 
	{
		int r = ((pixelColor >> 16) & 0xFF);
		int g = ((pixelColor >> 8) & 0xFF);
		int b = (pixelColor & 0xFF);
	
		int fogR = ((fogColor >> 16) & 0xFF);
		int fogG = ((fogColor >> 8) & 0xFF);
		int fogB = (fogColor & 0xFF);
	
		/*
		 * M3G specifies that, the smaller the fogFactor value, the more we
		 * should blend the fog color into the received color... which means
		 * that the fog's contribution to the resulting color should be
		 * 1 - fogFactor;
		 */
		int blendedR = (int) (r * (1 - fogFactor) + fogR * fogFactor);
		int blendedG = (int) (g * (1 - fogFactor) + fogG * fogFactor);
		int blendedB = (int) (b * (1 - fogFactor) + fogB * fogFactor);
	
		// Fog only has RGB channels, so it's always fully opaque
		return (255 << 24) | (blendedR << 16) | (blendedG << 8) | blendedB;
	}
}
