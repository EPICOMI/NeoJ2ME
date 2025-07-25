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

package com.nokia.mid.m3d;

import javax.microedition.lcdui.Image;

import org.recompile.mobile.Mobile;

public class Texture
{
	public Image texture;

	public int[] imagedata;

	public int width = 0;

	public int height = 0;

	private double[] uvm = new double[9]; // UV matrix

	private double[] sourcem = new double[9]; // source triangle

	private double[] transm = new double[9]; // transformation matrix

	private double[] tempm = new double[9]; // scratch

	private int[] colors = {0xFFFFFFFF, 0xFF000000};

	public Texture() { }
	
	public Texture(int a, int b, Image c)
	{
		texture = c;
		width = texture.getWidth();
		height = texture.getHeight();
		imagedata = new int[width*height];
		texture.getRGB(imagedata, 0, width, 0, 0, width, height);
		// Fix Alpha //
		for (int i=0; i<imagedata.length; i++) { imagedata[i] |= 0x00000000; imagedata[i] &= 0xFFFFFFFF; }
	}

	// Texture mapping stuff //

	public int map(int x, int y) // return texture color
	{
		int px = (int)(transm[0]*x + transm[1]*y + transm[2]);
		int py = (int)(transm[3]*x + transm[4]*y + transm[5]);
		int idx = px+(py*width);
		if(idx>0 && idx<imagedata.length)
		{
			return colors[imagedata[idx]&1];
		}
		return 0;
	}

	public void mapto(int x1, int y1, int x2, int y2, int x3, int y3)
	{
		// find transformation matrix for this triangle to UVs
		// Tm = UVm x Sm^-1
		sourcem[0] = x1; sourcem[1] = x2; sourcem[2] = x3;
		sourcem[3] = y1; sourcem[4] = y2; sourcem[5] = y3;
		sourcem[6] =  1; sourcem[7] =  1; sourcem[8] =  1;

		inverse(sourcem);
		clone(transm, uvm); 
		matmul(transm, sourcem);
		transm[6] = 0; transm[7] = 0; transm[8] = 1;
	}

	public void setUVs(int u1, int v1, int u2, int v2, int u3, int v3)
	{
		// UVs range from -1 to 1
		// change to 0 to 1, multiply by width or height
		
		uvm[0] = ((u1+1)/2) * width;
		uvm[1] = ((u2+1)/2) * width;
		uvm[2] = ((u3+1)/2) * width;

		uvm[3] = ((v1+1)/2) * height;
		uvm[4] = ((v2+1)/2) * height;
		uvm[5] = ((v3+1)/2) * height;

		uvm[6] = 1;
		uvm[7] = 1;
		uvm[8] = 1;
	}

	private void clone(double[] m1, double[] m2)
	{
		for(int i=0; i<9; i++)
		{
			m1[i] = m2[i];
		}
	}
	private void inverse(double[] m) // invert 3x3 matrix
	{
		double det = m[0] * (m[4] * m[8] - m[7] * m[5]) -
			 m[1] * (m[3] * m[8] - m[5] * m[6]) +
			 m[2] * (m[3] * m[7] - m[4] * m[6]);

		if(det==0) { return; }

		det = 1 / det;

		tempm[0] = (m[4] * m[8] - m[7] * m[5]) * det;
		tempm[1] = (m[2] * m[7] - m[1] * m[8]) * det;
		tempm[2] = (m[1] * m[5] - m[2] * m[4]) * det;
		tempm[3] = (m[5] * m[6] - m[3] * m[8]) * det;
		tempm[4] = (m[0] * m[8] - m[2] * m[6]) * det;
		tempm[5] = (m[3] * m[2] - m[0] * m[5]) * det;
		tempm[6] = (m[3] * m[7] - m[6] * m[4]) * det;
		tempm[7] = (m[6] * m[1] - m[0] * m[7]) * det;
		tempm[8] = (m[0] * m[4] - m[3] * m[1]) * det;
		
		clone(m, tempm);
	}

	private void matmul(double[] m1, double[] m2)
	{
		tempm[0] = m1[0]*m2[0] + m1[1]*m2[3] + m1[2]*m2[6];
		tempm[1] = m1[0]*m2[1] + m1[1]*m2[4] + m1[2]*m2[7];
		tempm[2] = m1[0]*m2[2] + m1[1]*m2[5] + m1[2]*m2[8];

		tempm[3] = m1[3]*m2[0] + m1[4]*m2[3] + m1[5]*m2[6];
		tempm[4] = m1[3]*m2[1] + m1[4]*m2[4] + m1[5]*m2[7];
		tempm[5] = m1[3]*m2[2] + m1[4]*m2[5] + m1[5]*m2[8];
		
		tempm[6] = m1[6]*m2[0] + m1[7]*m2[3] + m1[8]*m2[6];
		tempm[7] = m1[6]*m2[1] + m1[7]*m2[4] + m1[8]*m2[7];
		tempm[8] = m1[6]*m2[2] + m1[7]*m2[5] + m1[8]*m2[8];

		clone(m1, tempm);
	}
}
