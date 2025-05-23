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
package mmpp.lang;

public final class MathFP 
{

    public static final int E = (int) (Math.exp(1) * 4096); // e ~ 2.71828 * 4096
    public static final int PI = (int) (Math.PI * 4096); // π ~ 3.14159 * 4096
    public static final int MAX_VALUE = 524287; // Max fixed point value
    public static final int MIN_VALUE = -524288; // Min fixed point value
    public static final int MAX_VALUE_INT = Integer.MAX_VALUE; // Max integer value
    public static final int MIN_VALUE_INT = Integer.MIN_VALUE; // Min integer value

    public MathFP() { }

    public static int abs(int i) 
    {
        return (i < 0) ? -i : i;
    }

    public static int acos(int r) 
    {
        if (r < -4096 || r > 4096) { throw new IllegalArgumentException("Value out of range"); }
        return (int) (Math.acos(r / 4096.0) * 4096);
    }

    public static int add(int i, int j) 
    {
        if (i > 0 && j > MAX_VALUE - i) { throw new ArithmeticException("Overflow"); }
        return i + j;
    }

    public static int asin(int r) {
        if (r < -4096 || r > 4096) { throw new IllegalArgumentException("Value out of range"); }
        return (int) (Math.asin(r / 4096.0) * 4096); 
    }

    public static int atan(int r) 
    {
        return (int) (Math.atan(r / 4096.0) * 4096); 
    }

    public static int cos(int r) 
    {
        return (int) (Math.cos(r / 4096.0) * 4096);
    }

    public static int divide(int i, int j) 
    {
        if (j == 0) { throw new ArithmeticException("Division by zero"); }
        return (int) (((long) i * 4096) / j);
    }

    public static int exp(int f) 
    {
        return (int) (Math.exp(f / 4096.0) * 4096);
    }

    public static int log(int f) 
    {
        if (f <= 0) { throw new IllegalArgumentException("Value must be positive"); }
        return (int) (Math.log(f / 4096.0) * 4096);
    }

    public static int max(int a, int b) 
    {
        return (a > b) ? a : b;
    }

    public static int min(int a, int b) 
    {
        return (a < b) ? a : b;
    }

    public static int multiply(int i, int j) 
    {
        long result = (long) i * j;
        if (result > MAX_VALUE || result < MIN_VALUE) { throw new ArithmeticException("Overflow"); }
        return (int) (result >> 12);
    }

    public static int parseFP(int f) 
    {
        if (f > MAX_VALUE_INT || f < MIN_VALUE_INT) { throw new NumberFormatException("Value out of bounds"); }
        return f << 12;
    }

    public static int parseFP(String s) 
    {
        try 
        {
            double value = Double.parseDouble(s);
            return parseFP((int) (value * 4096));
        } 
        catch (NumberFormatException e) { throw new NumberFormatException("Invalid format"); }
    }

    public static int pow(int b, int e) 
    {
        if (b < 0 && (e & 1) != 0) { throw new IllegalArgumentException("Negative base with fractional exponent"); }
        return (int) (Math.pow(b / 4096.0, e / 4096.0) * 4096);
    }

    public static int round(int i) 
    {
        return (i + 2048) >> 12;
    }

    public static int sin(int r) 
    {
        return (int) (Math.sin(r / 4096.0) * 4096);
    }

    public static int sqrt(int i) 
    {
        if (i < 0) { throw new IllegalArgumentException("Negative value"); }
        return (int) (Math.sqrt(i / 4096.0) * 4096);
    }

    public static int sub(int i, int j) 
    {
        if (i < j) { throw new ArithmeticException("Underflow"); }
        return i - j;
    }

    public static int tan(int r) 
    {
        return (int) (Math.tan(r / 4096.0) * 4096); 
    }

    public static int toInt(int i) 
    {
        return (i + 2048) >> 12; 
    }

    public static String toString(int f) 
    {
        return String.valueOf(f / 4096.0);
    }
}