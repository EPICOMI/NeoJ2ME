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
package com.vodafone.io;

public class RemoteControlData 
{
    public static final int OUTPUT_MANCHESTER = 0;
    public static final int OUTPUT_PPM_HIGH_LOW = 1;
    public static final int OUTPUT_PPM_LOW_HIGH = 2;

    private int leaderOn;
    private int leaderOff;
    private int trailerOn;
    private int carrierOn;
    private int carrierOff;
    private byte[] data;
    private int repeatTime;
    private int repeatCount;

    public RemoteControlData() { }

    public void setCarrier(int on, int off) 
    {
        if (on <= 0 || off <= 0) { throw new IllegalArgumentException("Carrier frequency values must be greater than zero."); }
        this.carrierOn = on;
        this.carrierOff = off;
    }

    public void setData(int length, byte[] data) 
    {
        if (data == null) { throw new NullPointerException("Data cannot be null."); }
        if (length < 1) { throw new IllegalArgumentException("Length must be at least 1."); }
        if (length > data.length * 8) { throw new ArrayIndexOutOfBoundsException("Length exceeds data array size."); }
        this.data = data;
    }

    public void setLogicalPulse(int output, int data0_on, int data0_off, int data1_on, int data1_off) 
    {
        if (output < 0 || output > 2) { throw new IllegalArgumentException("Invalid modulation type."); }
        if (data0_on <= 0 || data0_off <= 0 || data1_on <= 0 || data1_off <= 0) { throw new IllegalArgumentException("Pulse timing values must be greater than zero."); }
        if (output == OUTPUT_MANCHESTER && (data0_on != data0_off || data1_on != data1_off)) { throw new IllegalArgumentException("For Manchester encoding, all timing values must match."); }
        
    }

    public void setPulse(int leader_on, int leader_off, int trailer_on) 
    {
        if (leader_on < 0 || leader_off < 0 || trailer_on < 0) { throw new IllegalArgumentException("Pulse values must be positive."); }
        this.leaderOn = leader_on;
        this.leaderOff = leader_off;
        this.trailerOn = trailer_on;
    }

    public void setRepeat(int time, int count) 
    {
        if (time <= 0 || count <= 0) { throw new IllegalArgumentException("Time and count must be greater than zero."); }
        this.repeatTime = time;
        this.repeatCount = count;
    }
}