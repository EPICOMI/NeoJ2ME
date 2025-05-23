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
package org.recompile.mobile;

import java.util.concurrent.atomic.AtomicLong;

public class MIDletEnhancements 
{
    private static final AtomicLong curNanoTime = new AtomicLong(0);
    private static long lastNanoTime = System.nanoTime();

    public static void drawSleep(long millis) throws InterruptedException
    {
        if (Mobile.unlockFramerateHack == 0 && !MobilePlatform.pressedKeys[19]) { Thread.sleep(millis); } 
        else { Thread.sleep(1); }
    }

    public static void sleep(long millis) throws InterruptedException
    {
        if (Mobile.unlockFramerateHack == 0 && !MobilePlatform.pressedKeys[19]) { Thread.sleep(millis); } 
        else { Thread.sleep(1); }
    }

    // Uses nanoTime due to its higher precision, but converted to milliseconds
    public static long currentTimeMillis() { return nanoTime() / 1_000_000; }

    public static long nanoTime() 
    {
        long now = System.nanoTime();
        long elapsedNanos = now - lastNanoTime;

        if (MobilePlatform.pressedKeys[19]) { curNanoTime.addAndGet(elapsedNanos * 20); } 
        else if (Mobile.unlockFramerateHack > 2) 
        {
            curNanoTime.addAndGet((long) (elapsedNanos * (Mobile.limitFPS == 0 ? 20 : (float) Mobile.limitFPS / 10f)));
        } 
        else { curNanoTime.addAndGet(elapsedNanos); }

        lastNanoTime = now;
        return curNanoTime.get();
    }

    /* Helps with jars that spam GC calls, causing cpu usage spikes */
    public static void noGC() 
    { 
        if(Mobile.compatIgnoreGCCalls) { }
        else { System.gc(); }
    }

    /* Can reduce cpu usage in some games, and even helps fix others like Super Action Hero (pulled from J2ME-Loader) */
    public static void yieldOverride() throws InterruptedException
    { 
        Thread.sleep(1);
    }
}