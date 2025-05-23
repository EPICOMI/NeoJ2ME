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
package mmpp.media;

import javax.microedition.media.Manager;

public final class Beep 
{

    public static final int SND_4OCT_A = 0;
    public static final int SND_4OCT_AS = 1;
    public static final int SND_4OCT_B = 2;
    public static final int SND_5OCT_A = 3;
    public static final int SND_5OCT_AS = 4;
    public static final int SND_5OCT_B = 5;
    public static final int SND_5OCT_C = 6;
    public static final int SND_5OCT_CS = 7;
    public static final int SND_5OCT_D = 8;
    public static final int SND_5OCT_DS = 9;
    public static final int SND_5OCT_E = 10;
    public static final int SND_5OCT_F = 11;
    public static final int SND_5OCT_FS = 12;
    public static final int SND_5OCT_G = 13;
    public static final int SND_5OCT_GS = 14;
    public static final int SND_6OCT_A = 15;
    public static final int SND_6OCT_AS = 16;
    public static final int SND_6OCT_B = 17;
    public static final int SND_6OCT_C = 18;
    public static final int SND_6OCT_CS = 19;
    public static final int SND_6OCT_D = 20;
    public static final int SND_6OCT_DS = 21;
    public static final int SND_6OCT_E = 22;
    public static final int SND_6OCT_F = 23;
    public static final int SND_6OCT_FS = 24;
    public static final int SND_6OCT_G = 25;
    public static final int SND_6OCT_GS = 26;
    public static final int SND_7OCT_A = 27;
    public static final int SND_7OCT_AS = 28;
    public static final int SND_7OCT_B = 29;
    public static final int SND_7OCT_C = 30;
    public static final int SND_7OCT_CS = 31;
    public static final int SND_7OCT_D = 32;
    public static final int SND_7OCT_DS = 33;
    public static final int SND_7OCT_E = 34;
    public static final int SND_7OCT_F = 35;
    public static final int SND_7OCT_FS = 36;
    public static final int SND_7OCT_G = 37;
    public static final int SND_7OCT_GS = 38;

    public Beep() { }

    public static void playBeep(int tone, int duration) 
    { 
        try { Manager.playTone(toMidiNote(tone), duration, 127); }
        catch (Exception e) { }
    }

    public static int toMidiNote(int beepConstant) 
    {
        switch (beepConstant) {
            case SND_4OCT_A: return 69; // A4
            case SND_4OCT_AS: return 70; // A#4/Bb4
            case SND_4OCT_B: return 71; // B4
            case SND_5OCT_A: return 81; // A5
            case SND_5OCT_AS: return 82; // A#5/Bb5
            case SND_5OCT_B: return 83; // B5
            case SND_5OCT_C: return 84; // C6
            case SND_5OCT_CS: return 85; // C#6/Db6
            case SND_5OCT_D: return 86; // D6
            case SND_5OCT_DS: return 87; // D#6/Eb6
            case SND_5OCT_E: return 88; // E6
            case SND_5OCT_F: return 89; // F6
            case SND_5OCT_FS: return 90; // F#6/Gb6
            case SND_5OCT_G: return 91; // G6
            case SND_5OCT_GS: return 92; // G#6/Ab6
            case SND_6OCT_A: return 93; // A6
            case SND_6OCT_AS: return 94; // A#6/Bb6
            case SND_6OCT_B: return 95; // B6
            case SND_6OCT_C: return 96; // C7
            case SND_6OCT_CS: return 97; // C#7/Db7
            case SND_6OCT_D: return 98; // D7
            case SND_6OCT_DS: return 99; // D#7/Eb7
            case SND_6OCT_E: return 100; // E7
            case SND_6OCT_F: return 101; // F7
            case SND_6OCT_FS: return 102; // F#7/Gb7
            case SND_6OCT_G: return 103; // G7
            case SND_6OCT_GS: return 104; // G#7/Ab7
            case SND_7OCT_A: return 105; // A7
            case SND_7OCT_AS: return 106; // A#7/Bb7
            case SND_7OCT_B: return 107; // B7
            case SND_7OCT_C: return 108; // C8
            case SND_7OCT_CS: return 109; // C#8/Db8
            case SND_7OCT_D: return 110; // D8
            case SND_7OCT_DS: return 111; // D#8/Eb8
            case SND_7OCT_E: return 112; // E8
            case SND_7OCT_F: return 113; // F8
            case SND_7OCT_FS: return 114; // F#8/Gb8
            case SND_7OCT_G: return 115; // G8
            case SND_7OCT_GS: return 116; // G#8/Ab8
            default: return -1; // Invalid tone
        }
    }
}