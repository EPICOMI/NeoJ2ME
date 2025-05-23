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
package com.nttdocomo.ui;

public final class ShortTimer implements com.nttdocomo.util.TimeKeeper 
{
    private boolean isRunning;
    private boolean isDisposed;
    private Canvas canvas;
    private int id;
    private int time;
    private boolean repeat;

    protected ShortTimer() 
    {
        isRunning = false;
        isDisposed = false;
    }

    public static ShortTimer getShortTimer(Canvas canvas, int id, int time, boolean repeat) {
        if (canvas == null) { throw new NullPointerException("Canvas cannot be null"); }
        if (time < 0) { throw new IllegalArgumentException("Time must be non-negative"); }
                
        ShortTimer timer = new ShortTimer();
        timer.canvas = canvas;
        timer.id = id;
        timer.time = time;
        timer.repeat = repeat;

        return timer;
    }

    public void start() 
    {
        if (isDisposed) { throw new UIException(1, "Timer has been disposed"); }
        if (isRunning) { throw new UIException(1, "Timer is already running"); }

        isRunning = true;
    }

    public void stop() {
        if (!isRunning) { return; }

        isRunning = false;
    }

    public void dispose() 
    {
        if (isDisposed) { return; }

        isDisposed = true;
    }

    public int getResolution() { return 1; } // 1ms resolution
}