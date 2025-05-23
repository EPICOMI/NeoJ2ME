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
package com.jblend.media.smaf.phrase;

public class PhrasePlayer 
{
	private static final PhrasePlayer phrasePlayer = new PhrasePlayer();

	protected int trackCount;
	protected int audioTrackCount;

	public static PhrasePlayer getPlayer() { return phrasePlayer; }

	public void disposePlayer() { }

	public PhraseTrack getTrack() { return new PhraseTrack(trackCount++); }

	public AudioPhraseTrack getAudioTrack() { return new AudioPhraseTrack(audioTrackCount++); }

	public int getTrackCount() { return 16; }

	public int getAudioTrackCount() { return 16; }

	public PhraseTrack getTrack(int track) { return new PhraseTrack(track); }

	public AudioPhraseTrack getAudioTrack(int track) { return new AudioPhraseTrack(track); }

	public void disposeTrack(PhraseTrack t) { }

	public void disposeAudioTrack(AudioPhraseTrack t) { }

	public void kill() { }

	public void pause() { }

	public void resume() { }
}