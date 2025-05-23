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
package com.vodafone.v10.util;

public class FixedPoint 
{

    private int value;

    public FixedPoint() { this.value = 0; }

    public FixedPoint(int value) { this.value = value; }

    public FixedPoint acos(FixedPoint v) { return new FixedPoint((int) (Math.acos(v.toDouble()) * 65536)); }

    public FixedPoint add(FixedPoint n) { return new FixedPoint(this.value + n.value); }

    public FixedPoint add(int n) { return new FixedPoint(this.value + (n << 16)); }

    public FixedPoint asin(FixedPoint v) { return new FixedPoint((int) (Math.asin(v.toDouble()) * 65536)); }

    public FixedPoint atan(FixedPoint v) { return new FixedPoint((int) (Math.atan(v.toDouble()) * 65536)); }

    public FixedPoint clone() { return new FixedPoint(this.value); }

    public FixedPoint cos(FixedPoint r) { return new FixedPoint((int) (Math.cos(r.toDouble()) * 65536)); }

    public FixedPoint divide(FixedPoint n) { return new FixedPoint((this.value << 16) / n.value); }

    public FixedPoint divide(int n) { return new FixedPoint((this.value << 16) / n); }

    public int getDecimal() { return value & 0xFFFF; }

    public int getInteger() { return value >> 16; }

    public static FixedPoint getMaximum() { return new FixedPoint(Integer.MAX_VALUE); }

    public static FixedPoint getMinimum() { return new FixedPoint(Integer.MIN_VALUE); }

    public static FixedPoint getPI() { return new FixedPoint((int) (Math.PI * 65536)); }

    public FixedPoint inverse() { return new FixedPoint((1 << 16) / this.value); }

    public boolean isInfinite() { return this.value == Integer.MAX_VALUE; }

    public FixedPoint multiply(FixedPoint n) { return new FixedPoint((this.value * n.value) >> 16); }

    public FixedPoint multiply(int n) { return new FixedPoint(this.value * n); }

    public FixedPoint pow() { return multiply(this); }

    public void setValue(int value) { this.value = value; }

    public FixedPoint sin(FixedPoint r) { return new FixedPoint((int) (Math.sin(r.toDouble()) * 65536)); }

    public FixedPoint sqrt() { return new FixedPoint((int) (Math.sqrt(toDouble()) * 65536)); }

    public FixedPoint subtract(FixedPoint n) { return new FixedPoint(this.value - n.value); }

    public FixedPoint subtract(int n) { return new FixedPoint(this.value - (n << 16)); }

    public FixedPoint tan(FixedPoint r) { return new FixedPoint((int) (Math.tan(r.toDouble()) * 65536)); }

    public double toDouble() { return value / 65536.0; }
}