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
package javax.microedition.media.decoders;

import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.nio.ByteOrder;
import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.recompile.mobile.Mobile;

public final class SMAFDecoder
{

    private static final byte CRCSize = 2; // SMAF has 2 bytes for CRC at the end of the file chunk, which is the main data chunk

    private static Map<Byte, Integer> huffmanFreqMap = new HashMap<>();
    private static HuffmanTree huffmanTree;
    private static HuffmanNode root;
    private static Map<Byte, String> huffmanCodes = new HashMap<>();

    private static ChannelData[] channelData;

    private static int decodePos = 0;
    private static byte formatType = 0;
    private static byte handyChannelIdx = 0;

    private static byte defaultVelocity = 64;

    private static byte TimeBase_D;
    private static byte TimeBase_G;

    // Used for handy phone format, as it can simulate more instruments with many Sequence Chunks
    private static boolean wrapInstruments = false; 
    private static int preWrapLongestDuration = 0;

    // Create a new sequence and track for the converted SMAF file
    private static Sequence sequence;
    private static Track[] channels;

    private static byte[] input;

    private static final byte[] shortModValues = new byte[] 
    {
        0x00, // Short type 0x1
        0x08, // Short type 0x2
        0x10, // Short type 0x3
        0x18, // Short type 0x4
        0x20, // Short type 0x5
        0x28, // Short type 0x6
        0x30, // Short type 0x7
        0x38, // Short type 0x8
        0x40, // Short type 0x9
        0x48, // Short type 0xA
        0x50, // Short type 0xB
        0x60, // Short type 0xC
        0x70, // Short type 0xD
        0x7F  // Short type 0xE
    };

    private static final byte[] shortPitchBendValues = new byte[] 
    {
        0x08, // Short type 0x1
        0x10, // Short type 0x2
        0x18, // Short type 0x3
        0x20, // Short type 0x4
        0x28, // Short type 0x5
        0x30, // Short type 0x6
        0x38, // Short type 0x7
        0x40, // Short type 0x8
        0x48, // Short type 0x9
        0x50, // Short type 0xA
        0x58, // Short type 0xB
        0x60, // Short type 0xC
        0x68, // Short type 0xD
        0x70  // Short type 0xE
    };

    private static final byte[] shortExpressionValues = new byte[] 
    {
        0x00, // Short type 0x1
        0x1F, // Short type 0x2
        0x27, // Short type 0x3
        0x2F, // Short type 0x4
        0x37, // Short type 0x5
        0x3F, // Short type 0x6
        0x47, // Short type 0x7
        0x4F, // Short type 0x8
        0x57, // Short type 0x9
        0x5F, // Short type 0xA
        0x67, // Short type 0xB
        0x6F, // Short type 0xC
        0x77, // Short type 0xD
        0x7F  // Short type 0xE
    };

    // These are used only for debugging
    private static String[] formatTypes = {"Handy Phone Standard (MA-1/2)", "Mobile Standard (MA-3/5) - Compressed (Untested)", "Mobile Standard (MA-3/5) - Not Compressed", "Yamaha MA-7 (Unsupported)"};
    private static String[] sequenceTypes = {"Stream Sequence", "Sub-Sequence (UNTESTED)"};
    private static String[] channelTypes = {"No Care", "Melody", "No Melody", "Rhythm"};
    private static String[] noteTypes = {"Invalid", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "Invalid", "Invalid", "Invalid"};
    private static final List<SimpleEntry<Byte, String>> timebases = Arrays.asList(
        new SimpleEntry<>((byte) 0, "1msec"),
        new SimpleEntry<>((byte) 1, "2msec"),
        new SimpleEntry<>((byte) 2, "4msec"),
        new SimpleEntry<>((byte) 3, "5msec"),
        new SimpleEntry<>((byte) 16, "10msec"),
        new SimpleEntry<>((byte) 17, "20msec"),
        new SimpleEntry<>((byte) 18, "40msec"),
        new SimpleEntry<>((byte) 19, "50msec")
    );

    // PCM-Specific variables
    public static boolean isPCM = false; // Used by PlatformPlayer to decide whether it'll load the decoded data into a WAVPlayer or MIDIPlayer
    private static String[] pcmDataFormats = {"2's complement PCM", "Offset Binary PCM", "YAMAHA ADPCM"};
    private static String[] pcmBaseBits = {"4 bits", "8 bits", "12 bits", "16 bits", "Reserved", "Reserved", "Reserved", "Reserved"};

    // Structures that hold decoded SMAF data

    public static List<InputStream> pcmData = null;
    public static InputStream SequenceData = null;
    public static Map<Integer, Integer> pcmDataPositions = new HashMap<>();

    public static synchronized void decodeSMAF(byte[] data)
	{
        // Reset any and all static variables to their defaults
        isPCM = false;
        wrapInstruments = false;
        preWrapLongestDuration = 0;
        handyChannelIdx = 0;
        decodePos = 0;
        huffmanFreqMap.clear();
        huffmanCodes.clear();
        channelData = new ChannelData[16];
        for (int j = 0; j < channelData.length; j++) { channelData[j] = new ChannelData(); }
        huffmanTree = new HuffmanTree();
        sequence = null; // Clear the previous MIDI sequence

        // Clear previous decoded data objects
        if(pcmData != null) { pcmData.clear(); }
        pcmData = null;
        SequenceData = null;
        pcmData = new ArrayList<>();
        pcmDataPositions.clear();

        input = data;
    
        // Start parsing the file.
        decodeHeader(); // MMMD (file chunk header)

        // After viewing some SMAF files with an hex editor, it seems that those three chunks can repeat multiple times (and that's for audio only, we still need to handle for graphics and PCM chunks)
        while(((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 'T' && (char) data[decodePos+2] == 'R') ||
            ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 's' && (char) data[decodePos+2] == 'p' && (char) data[decodePos+3] == 'I') || 
            ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 't' && (char) data[decodePos+2] == 's' && (char) data[decodePos+3] == 'u') || 
            ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 't' && (char) data[decodePos+2] == 's' && (char) data[decodePos+3] == 'q') ||
            ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 't' && (char) data[decodePos+2] == 's' && (char) data[decodePos+3] == 'p') ||
            ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 'w' && (char) data[decodePos+2] == 'a') ||
            
            // The ones below are specific to Sharp SMAf files, only seen those on them
            ((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 'M' && (char) input[decodePos+2] == 'M' && (char) input[decodePos+3] == 'G') ||
            ((char) input[decodePos] == 'V' && (char) input[decodePos+1] == 'O' && (char) input[decodePos+2] == 'I' && (char) input[decodePos+3] == 'C')
            )
        {
            if((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 'T' && (char) data[decodePos+2] == 'R') 
            {
                decodeScoreTrackChunk(); // MTR
            }
            if((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 's' && (char) data[decodePos+2] == 'p' && (char) data[decodePos+3] == 'I') 
            {
                decodeSeekAndPhraseChunk(); // Mspi
            }
            if((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 't' && (char) data[decodePos+2] == 's' && (char) data[decodePos+3] == 'u') 
            {
                decodeSetupDataChunk(); // Mtsu
            }
            else if ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 't' && (char) data[decodePos+2] == 's' && (char) data[decodePos+3] == 'q') 
            {
                scoreTrackSequenceData(); // Mtsq
                // Handy Phone format states that more than 4 channels can be simulated by having multiple Score Track Sequences
                if(formatType == 0x00) { handyChannelIdx += 4; }

                /* 
                    * If there are more than 4 Score Track Sequences, we have no choice but to wrap back to the first 4 channels
                    * But something we do here is to begin adding any new wrapped events (which often start with bank changes, etc) AFTER 
                    * the last longest total duration, which means that these events will only start happening AFTER all the previously active 
                    * MIDI channels become silent and have no new notes to play. This should also prevent unintended instrument changes and 
                    * notes to come into play at an instant that they shouldn't, overriding previously slotted channel data.
                    */
                if(handyChannelIdx > 12) { handyChannelIdx = 0; wrapInstruments = true;  }
            }

            // PCM data
            else if ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 't' && (char) data[decodePos+2] == 's' && (char) data[decodePos+3] == 'p') 
            {
                scoreTrackPCMData(); // Mtsp
            }
            else if ((char) data[decodePos] == 'M' && (char) data[decodePos+1] == 'w' && (char) data[decodePos+2] == 'a') 
            {
                Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " SMAF PCM Chunk found. PCM support not complete yet. ");
                pcmData.add(new ByteArrayInputStream(scoreTrackWaveData())); // Mwa
            }


            // Sharp stuff
            else if(((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 'M' && (char) input[decodePos+2] == 'M' && (char) input[decodePos+3] == 'G')) 
            {
                Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " This is a non-standard SMAF file usually found in Sharp phones! Trying to parse... ");
                decodeSharpHeader();
            }
            else if(((char) input[decodePos] == 'V' && (char) input[decodePos+1] == 'O' && (char) input[decodePos+2] == 'I' && (char) input[decodePos+3] == 'C')) 
            {
                decodeSharpVOICChunk();
            }
        }

        // Warn if the parser is somehow exiting earlier than the file's expected byte data size.
        if(decodePos < data.length - CRCSize) 
        { 
            Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " Early exit: Parsed " + decodePos + " of " + (data.length - CRCSize) + " bytes. Decoded file might be missing data..."); 
        }

        // If there was no early exit, check the file's CRC for good measure. If that's fine, any and all inaccuracies with the decoding lie in the decoder itself.
        if(SmafCRC.verifySmafCRC(input)) 
        {
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " SMAF file passed CRC check! ");
        }
        else 
        {
            Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " SMAF file did not pass CRC check. There might be corruption or incorrect data on the decoded file as a result. ");
        }
        

        // TODO: Decode more of SMAF

        // Convert the resulting sequence to byte array and send to the player.
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MidiSystem.write(sequence, 1, output);
            SequenceData = new ByteArrayInputStream(output.toByteArray());

            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " SMAF parsing and conversion finished, Sequence data size:" + output.size() + " | number of PCM streams:" + pcmData.size());
        }
        catch (IOException e) 
        { 
            Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " couldn't write converted SMAF Data:" + e.getMessage()); 
            e.printStackTrace();
            SequenceData = null;
            pcmData = null;
        }
	}

    public static void decodeHeader() 
    {
        String fileChunkID = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++]; // "MMMD"
        int fileChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        String cnti = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++];
        int cntichunksize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        byte contentClass = (byte) (input[decodePos++] & 0xFF);
        byte contentType = (byte) (input[decodePos++] & 0xFF);
        byte contentCodeType = (byte) (input[decodePos++] & 0xFF);
        byte copyStatus = (byte) (input[decodePos++] & 0xFF);
        byte copyCounts = (byte) (input[decodePos++] & 0xFF);
        String[] data = {""};
        StringBuilder sb = new StringBuilder();

        // Used just to make the content type print below a bit easier to understand
        String typeString = "", codeString = "", copyStatString = "";

        if(Mobile.minLogLevel <= Mobile.LOG_DEBUG) 
        {
            if((0x00 <= contentType && contentType <= 0x0F) || (0x30 <= contentType && contentType <= 0x33)) 
            {
                typeString = "Ringtone";
            }
            else if((0x10 <= contentType && contentType <= 0x1F) || (0x40 <= contentType && contentType <= 0x42)) 
            {
                typeString = "Karaoke";
            }
            else if((0x20 <= contentType && contentType <= 0x2F) || (0x50 <= contentType && contentType <= 0x53)) 
            {
                typeString = "Commercial";
            }
            else { typeString = "Reserved"; }

            switch(contentCodeType)
            {
                case 0x00: codeString = "Shift-JIS"; break;
                case 0x01: codeString = "Latin-1"; break;
                case 0x02: codeString = "EUC-KR"; break;
                case 0x03: codeString = "GB-2312"; break;
                case 0x04: codeString = "Big5"; break;
                case 0x05: codeString = "KOI8-R"; break;
                case 0x06: codeString = "TCVN-5773:1993"; break;
                case 0x20: codeString = "UCS-2"; break;
                case 0x21: codeString = "UCS-4"; break;
                case 0x22: codeString = "UTF-7"; break;
                case 0x23: codeString = "UTF-8"; break;
                case 0x24: codeString = "UTF-16"; break;
                case 0x25: codeString = "UTF-32"; break;
                default: codeString = "RESERVED " + contentCodeType; break;
            }

            if((copyStatus & 0x04) == 1) { copyStatString = copyStatString + " NO Edit"; }
            else { copyStatString = copyStatString + " Edit OK"; }

            if((copyStatus & 0x02) == 1) { copyStatString = copyStatString + " NO Save"; } 
            else { copyStatString = copyStatString + " Save OK"; }

            if((copyStatus & 0x01) == 1) { copyStatString = copyStatString + " NO Trans"; }
            else { copyStatString = copyStatString + " Trans OK"; }
        }
        


        // -------------------------- Back to parsing
        
        // NOTE: "," or 0x2C is used as a delimiter for data
        
                
        if(contentClass >= 0x00 && contentClass <= 0xFF)
        {
            while(!((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 'T' && (char) input[decodePos+2] == 'R') &&
                !((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 'M' && (char) input[decodePos+2] == 'M' && (char) input[decodePos+3] == 'G')) 
            {
                sb.append((char) (input[decodePos++] & 0xFF));
            }

            data = sb.toString().split(",");
        }

        while(!((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 'T' && (char) input[decodePos+2] == 'R') && 
            !((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 'M' && (char) input[decodePos+2] == 'M' && (char) input[decodePos+3] == 'G')) // assume garbage data is present in files that don't use that "options" field
        {
            decodePos++;
        }

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- SMAF CONTENT HEADER --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"fileChunkID: " + fileChunkID);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"fileChunkSize: " + fileChunkSize);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"contentsInfoChunk: " + cnti);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"contentsInfoChunkSize: " + cntichunksize); // This seems to always match with the real CNTI chunk size minus this data, so it's likely always present
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"contentClass: " + (contentClass == 0x00 ? "YAMAHA" : "OTHER " + contentClass));
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"contentType: " + (typeString + " " + (contentType & 0xFF)));
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"contentCodeType: " + codeString);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"copyStatus: " + copyStatString);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"copyCounts: " + copyCounts);
        if(contentClass >= 0x00 && contentClass <= 0xFF)
        {
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"Format has 'Option' field!");
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\t additional data: ");
            for (int i = 0; i < data.length; i++) { Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\t " + data[i]); }
        }
        // OK, we're at the start of the "MTR" (Score) Track, which means the content info chunk (content header) has been left behind
    }

    public static void decodeSharpHeader() 
    {
        String MMMG = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++]; // "MMMG"
        int MMMGChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        String data = "" + (char) input[decodePos++] + (char) input[decodePos++];

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- " + MMMG + " HEADER --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"MMMGChunkSize: " + MMMGChunkSize);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"MMMGChunkData: " + data);

        // According to some Sharp SMAFs, this should be followed by the VOIC chunk
    }

    public static void decodeSharpVOICChunk() 
    {
        String voic = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++]; // "VOIC"
        int voicChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
    
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- " + voic + " HEADER --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"VOICChunkSize: " + voicChunkSize);
    }

    public static void decodeScoreTrackChunk() // TODO: Parsing issues here
    {
        // We're at the Score Track Chunk, so let's decode the info about the audio data
        String mtr = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++]; // "MTR" actually uses 4 chars, the last one appears to always be 0x01
        int mtrChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        formatType = (byte) (input[decodePos++]); // 0x00 = Handy Phone, 0x01 = Standard Mobile (compress), 0x02 = Standard Mobile (no comp.)
        byte sequenceType = input[decodePos++]; // for formatType 0x00, 0x00 and 0x01 are allowed, 0x02 is reserved
        TimeBase_D = input[decodePos++]; // 0x00 = 1msec, 0x01 = 2msec, 0x02 = 4msec, 0x03 = 5msec. 0x10 = 10msec, 0x11 = 20msec, 0x12 = 40msec, 0x13 = 50msec
        TimeBase_G = input[decodePos++]; // same value range as above

        // These will depend on formatType;
        byte channelStatus1 = 0x00, channelStatus2 = 0x00; // used for formatType 0x00
        
        int formatDataChunk; // sequenceDataChunk, or also StreamPCMDataChunk when formatType = "Mobile Standard (0x01 or 0x02)"

        if(formatType == (byte) 0x00) // Handy Phone
        {
            channelStatus1 = (byte) (input[decodePos++] & 0xFF);
            channelStatus2 = (byte) (input[decodePos++] & 0xFF);

            for(int i = 0; i < 4; i++)
            {
                byte dataToInsert = (i < 2) ? channelStatus1 : channelStatus2;

                if (i % 2 == 0) // Channels 1 and 2
                {
                    channelData[i].keyControlBasic = (dataToInsert & 0x80) != 0; // KCS
                    channelData[i].vibStatus = (dataToInsert & 0x40) != 0; // VS
                    channelData[i].channelType = (byte) ((dataToInsert >> 4) & 0x03); // Channel Type
                }
                else // Channels 2 and 3
                {
                    channelData[i].keyControlBasic = (dataToInsert & 0x08) != 0; // KCS
                    channelData[i].vibStatus = (dataToInsert & 0x04) != 0; // VS
                    channelData[i].channelType = (byte) (dataToInsert & 0x03); // Channel Type
                }
            }
        }
        else // The only difference between the Standard Mobile formats is compression, so they're identical here
        {
            int i = 0;
            while(!((char) input[decodePos] == 'M' && ((char) input[decodePos + 1] == 's' || (char) input[decodePos + 1] == 't'))) // Before we reach the "MspI" (Seek and Phrase) or "Mtsu" (Setup Data Chunk) sections, keep adding channels
            {
                if(i < 16) 
                {
                    channelData[i].keyControl = (byte) (input[decodePos] & 0b11000000);
                    channelData[i].vibStatus = (input[decodePos] & 0b00100000) != 0;
                    channelData[i].led = (input[decodePos] & 0b00010000) != 0;
                    channelData[i].channelType = (byte) ((input[decodePos] & 0b00001100) >> 2);
                    i++;
                }
                else { Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"channel limit reached, ignoring further channel data..."); }
                decodePos++;
            }
        }

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- '" + mtr +"' SECTION --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"mtrChunkSize: " + mtrChunkSize);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"formatType: " + formatTypes[formatType]);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"sequenceType: " + sequenceTypes[sequenceType]);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"TimeBase_Duration: " + getTimeBaseDescription(TimeBase_D));
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"TimeBase_GateTime: " + getTimeBaseDescription(TimeBase_G));
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"channelStatus: ");
        for(int i = 0; i < (formatType == (byte) 0x00 ? 4 : channelData.length); i++) // Format 0x00 has 4 channels, 0x01 and 0x02 have 16
        {
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\tChannel" + (i+1) + ": ");
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\t\tKeyControlStatus:" + channelData[i].keyControl);
            if(formatType != (byte) 0x00) { Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\t\tLED:" + (channelData[i].led ? "Enabled" : "Disabled")); }
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\t\tVibration:" + (channelData[i].vibStatus ? "Enabled" : "Disabled"));
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"\t\tChannelType:" + channelTypes[channelData[i].channelType]);
        }
        
        // We now have sufficient data to create a proper MIDI sequence.
        if(sequence == null) 
        { 
            try 
            {
                sequence = new Sequence(Sequence.PPQ, 500, 16); // TODO: Maybe revise this? We shouldn't rely on a fixed PPQ value, i think. (though there are separate timebases for duration and gateTime, so who knows, maybe it's correct)
                channels = sequence.getTracks();
            } catch(InvalidMidiDataException ie) { Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + " couldn't create MIDI Sequence to convert:" + ie.getMessage()); }
        }
    }

    public static void decodeSeekAndPhraseChunk() 
    {
        String seekAndPhrase = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++];
        StringBuilder sb = new StringBuilder();

        while(!((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 't' && (char) input[decodePos+2] == 's' && (char) input[decodePos+3] == 'u') && 
                !((char) input[decodePos] == 'M' && (char) input[decodePos+1] == 't' && (char) input[decodePos+2] == 's' && (char) input[decodePos+3] == 'q')) 
        {
            sb.append((char) (input[decodePos++] & 0xFF));
        }
        String startAndStopPoints = sb.toString();

        if(seekAndPhrase != "")
        {
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- '" + seekAndPhrase +"' SECTION --------------------------");
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"startAndStopPoints parsed ");
        }
    }

    public static void decodeSetupDataChunk() 
    {
        // We're at the Score Track Chunk, so let's decode the info about the audio data
        String mtsu = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++];
        int mtsuChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        List<Byte> exclusiveMessage = new ArrayList<>(); // Seems to relate to SysEx messages, maybe we don't need these?
        int mtsuPos = 0;

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- '" + mtsu +"' SECTION --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"mtsuChunkSize: " + mtsuChunkSize);

        // FormatType doesn't seem to really matter for us here
        while(mtsuPos < mtsuChunkSize) 
        {
            exclusiveMessage.add(input[decodePos++]);
            mtsuPos++;
        }
        
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"Exclusive Message(SysEx) length: " + exclusiveMessage.size());
        // Now we should be entering the Score Track Sequence Data (Mtsq) section, which contains actual notes
    }

    public static void scoreTrackSequenceData() 
    {
        // We're at the Score Track Chunk, so let's decode the info about the audio data
        String mtsq = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++];
        int mtsqChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        int curPos = 0;

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- '" + mtsq +"' SECTION --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"mtsqChunkSize: " + mtsqChunkSize);

        byte[] seqBytes = new byte[mtsqChunkSize];
        // TODO: Decode these properly
        if(formatType == (byte) 0x00 || formatType == (byte) 0x02) // Handy Phone and Uncompressed Mobile Standard are about the same here
        {
            // Collect sequence data
            while (curPos < mtsqChunkSize)
            {
                seqBytes[curPos] = input[decodePos++];
                curPos++;
            }

            try { convertSequenceEvents(seqBytes); }
            catch(Exception e) { Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Couldn't decode sequence events:" + e.getMessage()); e.printStackTrace(); }
        } 
        else if(formatType == (byte) 0x01) // Standard Mobile, Compressed (Huffman table must be applied here, COMPLETELY UNTESTED)
        {
            Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Huffman decoding is untested. Expect issues. ");
            
            while (curPos < mtsqChunkSize)
            {
                seqBytes[curPos] = input[decodePos++];
                curPos ++;
            }

            // Populate the Huffman Frequency Map
            for (byte b : seqBytes) { huffmanFreqMap.put(b, huffmanFreqMap.getOrDefault(b, 0) + 1); }

            root = huffmanTree.buildTree(huffmanFreqMap);

            // Generate Huffman Codes
            huffmanCodes.clear();
            huffmanTree.generateCodes(root, "", huffmanCodes);

            // Decode using Huffman
            byte[] decodedData = decodeHuffman(seqBytes);

            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"orig len:" + seqBytes.length);
            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"dec len:" + decodedData.length);

            /*
            System.out.print("orig:[");
            for(int i = 0; i < seqBytes.length; i++) 
            {
                System.out.printf(" " + String.format("%02X", (seqBytes[i] & 0xFF)));
            }
            System.out.println("] origLen:" + seqBytes.length);
            
            System.out.print("Dec:[");
            for(int i = 0; i < decodedData.length; i++) 
            {
                System.out.printf(" " + String.format("%02X", (decodedData[i] & 0xFF)));
            }
            System.out.println("]");
            */

            try { convertSequenceEvents(decodedData); }
            catch(Exception e) { Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Couldn't decode sequence events:" + e.getMessage()); e.printStackTrace(); }
        }
        else 
        {
            Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Unknown format type: " + formatType);
        }
    }

    public static void scoreTrackPCMData() 
    {
        String mtsp = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++];
        int mtspChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"-------------------------- '" + mtsp +"' SECTION --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"mtspChunkSize: " + mtspChunkSize);

        isPCM = true;
    }

    public static byte[] scoreTrackWaveData() 
    {
        String mwa = "" + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++] + (char) input[decodePos++];
        int mwaChunkSize = (input[decodePos++] & 0xFF) << 24 | (input[decodePos++] & 0xFF) << 16 | (input[decodePos++] & 0xFF) << 8 | (input[decodePos++] & 0xFF);
        
        byte waveType = (byte) (input[decodePos++] & 0xFF);
        byte channelType = (byte) ((waveType >> 4) & 0x01);
        byte dataFormat = (byte) ((waveType >> 5) & 0x03);
        byte baseBit = (byte) ((waveType >> 2) & 0x03);
        byte samplingFreqMSB = (byte) (input[decodePos++] & 0xFF);
        byte samplingFreqLSB = (byte) (input[decodePos++] & 0xFF);
        short samplingFrequency = (short) (((samplingFreqMSB & 0xFF) << 8) | (samplingFreqLSB & 0xFF)); // I really doubt this will ever go over 48000Hz (hell, even 22050Hz since it's J2ME), so a short should suffice

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "-------------------------- '" + mwa +"' SECTION --------------------------");
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "mwaChunkSize: " + mwaChunkSize);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "waveNumber: " + (waveType > (byte) 0x00 && waveType < (byte) 0x3F ? "Wave ID" : "PROHIBITED"));
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "channelType: " + (channelType == (byte) 0x00 ? "Mono" : "Stereo"));
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "dataFormat: " + pcmDataFormats[dataFormat]);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "baseBit: " + pcmBaseBits[baseBit]);
        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "samplingFrequency: " + samplingFrequency);

        
        
        if(!((char) input[decodePos] == 'A' && (char) input[decodePos+1] == 'T' && (char) input[decodePos+2] == 'R')) 
        {
            // We have no PCM Audio Track Chunk, prepare to parse and, if needed, decode the PCM data directly
            byte[] waveData = new byte[mwaChunkSize];
            // "mwaChunkSize-3" because we have alread read 3 bytes after mwaChunkSize
            for(int i = 0; i < mwaChunkSize-3; i++) { waveData[i] = input[decodePos++]; }

            if(dataFormat == (byte) 0x00) // 2's complement PCM WAV
            {
                if(baseBit == (byte) 0x00) { return WavImaAdpcmDecoder.convert4BitWav(waveData, channelType+1, samplingFrequency, true);}
                if(baseBit == (byte) 0x01) { return WavImaAdpcmDecoder.convert8BitWav(waveData, channelType+1, samplingFrequency, true);}
                if(baseBit == (byte) 0x02) { return WavImaAdpcmDecoder.convert12BitWav(waveData, channelType+1, samplingFrequency, true);}
                if(baseBit == (byte) 0x03) { return WavImaAdpcmDecoder.convert16BitWav(waveData, channelType+1, samplingFrequency, true);}
            }
            else if(dataFormat == (byte) 0x01) // Binary Offset PCM WAV
            {
                if(baseBit == (byte) 0x00) { return WavImaAdpcmDecoder.convert4BitWav(waveData, channelType+1, samplingFrequency, false);}
                if(baseBit == (byte) 0x01) { return WavImaAdpcmDecoder.convert8BitWav(waveData, channelType+1, samplingFrequency, false);}
                if(baseBit == (byte) 0x02) { return WavImaAdpcmDecoder.convert12BitWav(waveData, channelType+1, samplingFrequency, false);}
                if(baseBit == (byte) 0x03) { return WavImaAdpcmDecoder.convert16BitWav(waveData, channelType+1, samplingFrequency, false);}
            } 
            else if(dataFormat == (byte) 0x02) // YAMAHA ADPCM
            {
                Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"PCM data uses YAMAHA ADPCM. Decoding for this is not implemented!");
                return null;
            }
            else { Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"Invalid PCM data format!"); return null;}
        }
        else 
        {
            Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"ATR Chunk detected. Parsing not implemented!");
        }

        return null; // We should never hit this
    }

    /* -------------------------------------------------------------------------------------- */
    /*         Lower level decoding functions (huffman, sequence event, duration, etc)        */
    /* -------------------------------------------------------------------------------------- */

    public static void convertSequenceEvents(byte[] data) throws InvalidMidiDataException 
    {
        int offset = 0;
        int totalDuration = (wrapInstruments) ? preWrapLongestDuration : 0;
    
        while (offset < data.length) 
        {
            byte firstDurByte = 0;
            int duration = 0;
            byte channel = 0;
            int gateTime = 0;
            byte firstGateByte = 0;
            byte noteNumber = 0;

            MidiEvent midiEvent = null;

            if(formatType == (byte) 0x00) // Handy Phone format
            {
                firstDurByte = (byte) (data[offset++] & 0xFF);
                
                // Check firt byte's MSB for duration size in bytes
                if ((firstDurByte & 0x80) == 0) // Single-byte duration
                {
                    duration = firstDurByte;
                } 
                else // Dual-byte duration
                {
                    byte secondDurByte = (byte) (data[offset++] & 0xFF);
                    duration = ((firstDurByte & 0x3F) << 7) | (secondDurByte & 0x7F);
                    duration += 128; // Add 128 to Duration as per the SMAF documentation
                }

                totalDuration += (duration * timeBasetoMs(TimeBase_D)); // Update total duration

                // Now read the following event bytes.
                byte eventType = (byte) (data[offset++] & 0xFF);

                if (eventType == 0x00) // Event Type 0x00 indicates a message (bank change, program change, pitch bend, etc)
                {
                    byte eventData = (byte) (data[offset++] & 0xFF); // Read the next byte to determine the event type

                    channel = (byte) ((eventData >> 6) & 0x03); // Extract channel value from bits 6-7
                    channel += handyChannelIdx;
                    // bits 4 and 5 are additional event classification bits (used to discern between long type and short type events)
                    int b5 = (eventData >> 5) & 0x01;
                    int b4 = (eventData >> 4) & 0x01;

                    byte shortEventValue = (byte) (eventData & 0x0F);

                    if(eventData == (byte) 0x00) // Might signal the start of an End-Of-Sequence byte arrangement
                    {
                        byte endOfSequenceEventCheck = (byte) (data[offset++] & 0xFF);

                        if (endOfSequenceEventCheck == 0x00) { Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Reached end of Handy Phone Sequence Chunk."); } 
                        else 
                        {
                            Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Invalid Handy Phone End Of Sequence value: " + endOfSequenceEventCheck);
                            return;
                        }
                    }
                    else if (b5 == 1 && b4 == 1) // Long type event
                    {
                        byte eventCategory = (byte) (eventData & 0x0F); // Get bits 4-7 for event category
                        byte valueField = (byte) (data[offset++] & 0xFF);

                        switch (eventCategory) 
                        {
                            case 0x0: // Program Change
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding program change value 0x" + String.format("%02X", handyPhoneBankToMidi(valueField, channel)) + "(" + handyPhoneBankToMidi(valueField, channel) + ") to channel " + channel);
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, handyPhoneBankToMidi(valueField, channel), 0), totalDuration);
                                channels[channel].add(midiEvent);
                                break;

                            case 0x1: // Bank Select
                                int bankType = (valueField & 0x80) >> 7; // Check if it's normal or drum bank
                                int bankNumber = valueField & 0x7F; // Use the lower 7 bits for the bank number
                    
                                // Log the bank type and number
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding bank change value 0x" + String.format("%02X", bankNumber) + "(" + bankNumber + ") of Type: (" + (bankType == 0 ? "Normal" : "Drum") + ") to channel " + channel);
                    
                                // Send the bank select message
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 0, bankNumber), totalDuration);
                                channels[channel].add(midiEvent);
                    
                                // If it's a drum bank, we'll need to change to a drum instrument by altering the midi instrument mapping
                                if (bankType == 1) 
                                {
                                    Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Channel Drum Bank requested. Altering MIDI mapping for channel " + channel + " until a non-drum bank is requested");
                                    channelData[channel].usingDrumBank = true;
                                }
                                else 
                                {
                                    channelData[channel].usingDrumBank = false;
                                }
                                break;

                            case 0x2: // Octave Shift (NOTE: MIDI doesn't have anything analogous to this, we have to implement it manually by shifting all notes in the channel after this event)                                
                                byte octaveShift = 0;

                                if (valueField >= 0x00 && valueField <= 0x04) { octaveShift = valueField; } /* Octave shifts 0 to +4 */
                                else if (valueField >= 0x81 && valueField <= 0x84) 
                                {
                                    octaveShift = (byte) (valueField - 0x80); // Map to -1 to -4
                                    octaveShift = (byte) -octaveShift; // Convert to negative value
                                } 
                                else 
                                {
                                    Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Reserved octave shift value: 0x" + String.format("%02X", valueField));
                                    return;
                                }
                                
                                channelData[channel].octaveShift = octaveShift;
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding Octave Shift value 0x" + String.format("%02X", valueField) + "(" + channelData[channel].octaveShift + " octave) to channel " + channel);
                                break;

                            case 0x3: // Modulation (Long Type)
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding modulation value 0x" + String.format("%02X", valueField) + "(" + valueField + ") to channel " + channel);
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 1, valueField), totalDuration);
                                channels[channel].add(midiEvent);
                                break;

                            case 0x4: // Pitch Bend (Long Type)
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding pitch bend value 0x" + String.format("%02X", valueField) + "(" + valueField + ") to channel " + channel);
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.PITCH_BEND, channel, valueField), totalDuration);
                                channels[channel].add(midiEvent);
                                break;

                            case 0x7: // Volume Change
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding volume value 0x" + String.format("%02X", valueField) + "(" + valueField + ") to channel " + channel);
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 7, valueField), totalDuration);
                                channels[channel].add(midiEvent);
                                break;

                            case 0xA: // Panning Change
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding panning value 0x" + String.format("%02X", valueField) + "(" + valueField + ") to channel " + channel);
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 10, valueField), totalDuration);
                                channels[channel].add(midiEvent);
                                break;

                            case 0xB: // Expression Change (Long Type)
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding expression value 0x" + String.format("%02X", valueField) + "(" + valueField + ") to channel " + channel);
                                midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 11, valueField), totalDuration);
                                channels[channel].add(midiEvent);
                                break;

                            default:
                                Mobile.log(Mobile.LOG_ERROR, "Unknown long event category: " + eventCategory);
                                break;
                        }
                    } 
                    else // Short type event (bits 4 and/or 5 are not zero, any order)
                    {
                        // NOTE: Short values for mod, pitch, expr always go from 0x1 to 0xE, so this is why we access value-1 in the constant arrays
                        if (b5 == 1 && b4 == 0) // Modulation (Short Type)
                        {
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "(short) Adding modulation value 0x" + String.format("%02X", shortModValues[shortEventValue-1]) + "(" + shortModValues[shortEventValue-1] + ") to channel " + channel);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 1, shortModValues[shortEventValue-1]), totalDuration);
                            channels[channel].add(midiEvent);
                        }

                        if (b5 == 0 && b4 == 1) // Pitch Bend (Short Type)
                        {
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "(short) Adding pitch bend value 0x" + String.format("%02X", shortPitchBendValues[shortEventValue-1]) + "(" + shortPitchBendValues[shortEventValue-1] + ") to channel " + channel);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.PITCH_BEND, channel, shortPitchBendValues[shortEventValue-1]), totalDuration);
                            channels[channel].add(midiEvent);
                        }

                        if (b5 == 0 && b4 == 0) // Expression (Short Type)
                        {
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "(short) Adding expression value 0x" + String.format("%02X", shortExpressionValues[shortEventValue-1]) + "(" + shortExpressionValues[shortEventValue-1] + ") to channel " + channel);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 11, shortExpressionValues[shortEventValue-1]), totalDuration);
                            channels[channel].add(midiEvent);
                        }
                    }
                }
                else if (eventType == (byte) 0xFF) // 0xFF denotes this event is a sysEx message or NOP (No Operation)
                {
                    if(data[offset+1] == 0x00) 
                    {
                        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "NOP event received");
                        offset++;
                    }
                    else 
                    {
                        List<Byte> exclusiveMessage = new ArrayList<>(); // Seems to relate to SysEx messages, maybe we don't need these?
                        while(data[offset] != (byte) 0xF7) // 0xF7 as the value marks the end of the SysEx message
                        {
                            exclusiveMessage.add(data[offset++]);   
                        }
                        offset++; // Move out of offset containing 0xF7, as the next one is the next status byte
                        
                        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "SysEx event received");
                        // TODO: Maybe use this SysEx for something?
                    }
                }
                else // Any other value for event type is a note message (which contains channel, octave and note in the eventType byte itself)
                {
                    // Extract channel, octave, and note number from the eventType
                    channel = (byte) ((eventType >> 6) & 0x03); // Bits 6-7 for channel
                    byte octave = (byte) ((eventType >> 4) & 0x03); // Bits 4-5 for octave
                    noteNumber = (byte) (eventType & 0x0F); // Bits 0-3 for note number
                
                    // Read gate time
                    firstGateByte = (byte) (data[offset++] & 0xFF); // First byte for gate time
            
                    // gateTime works pretty much like duration in how its read
                    if ((firstGateByte & 0x80) == 0) // Single-byte gate time
                    { 
                        gateTime = firstGateByte;
                    } 
                    else // Dual-byte gate time
                    { 
                        byte secondGateByte = (byte) (data[offset++] & 0xFF);
                        gateTime = ((firstGateByte & 0x3F) << 7) | (secondGateByte & 0x7F);
                        gateTime += 128; // Add 128 to gate time as per the SMAF documentation
                    }
            
                    // As per the documentation, gateTime cannot be zero, this indicates either a corrupted file or a parse error
                    if (gateTime <= 0) 
                    {
                        Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "note gateTime value cannot be zero. ");
                        return;
                    }

                    int midiNoteNumber = 0;
                    switch (octave) 
                    {
                        case 0x00: midiNoteNumber = (byte) (48 + noteNumber - 1); break; // Low
                        case 0x01: midiNoteNumber = (byte) (60 + noteNumber - 1); break; // Mid Low
                        case 0x02: midiNoteNumber = (byte) (72 + noteNumber - 1); break; // Mid High
                        case 0x03: midiNoteNumber = (byte) (84 + noteNumber - 1); break; // High
                    }
                    midiNoteNumber += 12 * channelData[channel].octaveShift;

                    // Create MIDI note event
                    Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding note event " + noteTypes[noteNumber] + (4+octave+channelData[channel].octaveShift) + " to channel " + channel);
                    midiEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel, midiNoteNumber, defaultVelocity), totalDuration);
                    channels[channel].add(midiEvent); // Add the note event to the corresponding channel
                    midiEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, channel, midiNoteNumber, 0), totalDuration+(gateTime * timeBasetoMs(TimeBase_G)));
                    channels[channel].add(midiEvent);
                }
            }
            else // Mobile Standard formats (with or without compression, as this method will receive uncompressed data either way)
            {
                //duration = getVariableLengthValue(data, new int[]{offset});
                int numBytes = 0;
                // In Mobile Standard, both duration and gateTime use the variable length notation, up to 4 bytes
                // TODO: We might need to do more stuff here (handy phone format has an increment if more than one byte is used for example)...
                for (int i = 0; i < 4; i++) 
                {
                    
                    byte currentByte = data[offset++]; // Read the current byte
                    duration = (i > 0 ? ((duration & 0x3F) | (currentByte & 0x7F)) : currentByte);
                    duration += (numBytes << 7);

                    numBytes++;
                    // Check if the MSB is 0 to determine if this is the last byte
                    if ((currentByte & 0x80) == 0) { break; }
                    
                }

                totalDuration += (duration * timeBasetoMs(TimeBase_D)); // Update total duration
                // Read status byte
                int status = data[offset++] & 0xFF; // Read status byte
           
                if(status == 0xF0) // SysEx messages, maybe we don't need these?
                {
                    List<Byte> exclusiveMessage = new ArrayList<>();

                    while(offset < data.length && data[offset] != (byte) 0xF7) // Like with formatType 0x00, 0xF7 as the value marks the end of the SysEx message
                    {
                        exclusiveMessage.add(data[offset++]);   
                    }
                    offset++; // Move out of offset containing 0xF7, as the next one is the next status byte
                    
                    Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "SysEx Message parsed. Size:" + exclusiveMessage.size());
                    // TODO: Maybe use this SysEx for something?
                }
                else if(status >= 0xF1 && status <= 0xFE) // Reserved Sequence status bytes
                {
                    Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Found a reserved sequence status byte: " + status);
                }
                else if(status == 0xFF) // End of Sequence (EOS) or NOP
                {
                    if (data[offset] == (byte) 0x2F) // EOS
                    {
                        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "End of Sequence reached. " + status);
                        return;
                    }
                    if (data[offset] == (byte) 0x00) // NOP
                    {
                        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "NOP parsed");
                        offset += 1; // Skip 0xFF and 0x00
                    }
                }
                else // Audio events
                {
                    switch (status & 0xF0) 
                    {
                        case 0x80: // Note without velocity
                            channel = (byte) (status & 0x0F);
                            noteNumber = (byte) (data[offset++] & 0x7F); // Note Number
                            numBytes = 0;
                            // Read gate time
                            for (int i = 0; i < 4; i++) 
                            {            
                                byte currentByte = data[offset++]; // Read the current byte
                                gateTime = (i > 0 ? ((gateTime & 0x3F) | (currentByte & 0x7F)) : currentByte);
                                gateTime += (numBytes << 7);
                                numBytes++;
                                // Check if the MSB is 0 to determine if this is the last byte
                                if ((currentByte & 0x80) == 0) { break; }
                            }
                    
                            // As per the documentation, gateTime cannot be zero, this indicates either a corrupted file or a parse error
                            if (gateTime <= 0) 
                            {
                                Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "note gateTime value cannot be zero. ");
                                return;
                            }

                            
                            
                            // TODO: This might be incorrect as the SMAF documentation doesn't detail how the chip differentiates between PCM data and Sequence Data to play
                            // All it notes is that a piano has 88 notes and in midi it goes all the way up to 108 (which is where this "20" value comes from, as anything lower supposedly is a PCM file)
                            if(noteNumber < 20)
                            {
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding PCM Request value " + noteNumber + " to channel " + channel + " at duration " + totalDuration);
                                pcmDataPositions.put(totalDuration, (int) noteNumber);
                            }
                            else { Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding note value " + noteNumber + " to channel " + channel); }
                            
                            // We still add the notes no matter, just so that the sequencer can actually reach the PCM request time
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel, noteNumber, channelData[channel].velocity), totalDuration);
                            channels[channel].add(midiEvent);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, channel, noteNumber, 0), totalDuration+(gateTime * timeBasetoMs(TimeBase_G)));
                            channels[channel].add(midiEvent);
                            break;
            
                        case 0x90: // Note with velocity
                            channel = (byte) (status & 0x0F);
                            noteNumber = (byte) (data[offset++] & 0x7F); // Note Number
                            channelData[channel].velocity = (byte) (data[offset++] & 0x7F); // Key Velocity
                            numBytes = 0;
                            // Read gate time
                            for (int i = 0; i < 4; i++) 
                            {
                                byte currentByte = data[offset++]; // Read the current byte
                                gateTime = (i > 0 ? ((gateTime & 0x3F) | (currentByte & 0x7F)) : currentByte);
                                gateTime += (numBytes << 7);
                                numBytes++;
                                // Check if the MSB is 0 to determine if this is the last byte
                                if ((currentByte & 0x80) == 0) { break; }
                            }
                    
                            // As per the documentation, gateTime cannot be zero, this indicates either a corrupted file or a parse error
                            if (gateTime <= 0) 
                            {
                                Mobile.log(Mobile.LOG_ERROR, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "note gateTime value cannot be zero. ");
                                return;
                            }

                            
                            if(noteNumber < 20)
                            {
                                Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding PCM Request value " + noteNumber + " with new velocity to channel " + channel + " at duration " + totalDuration);
                                pcmDataPositions.put(totalDuration, (int) noteNumber);
                            }
                            else { Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding note value " + noteNumber + " with new velocity to channel " + channel); }
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel, noteNumber, channelData[channel].velocity), totalDuration);
                            channels[channel].add(midiEvent);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, channel, noteNumber, 0), totalDuration+(gateTime * timeBasetoMs(TimeBase_G)));
                            channels[channel].add(midiEvent);
                            break;
            
                        case 0xA0: // Reserved (3 bytes)
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Skipping 3 reserved bytes");
                            offset += 3; // Skip 3 bytes
                            break;
            
                        case 0xB0: // Control Change (0xB0 to 0xBF)
                            channel = (byte) (status & 0x0F);
                            int controlNumber = data[offset++] & 0x7F; // Control Number
                            int controlValue = data[offset++] & 0x7F; // Control Value
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding control change number " + controlNumber + " with value " + controlValue + " to channel " + channel);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, controlNumber, controlValue), totalDuration);
                            channels[channel].add(midiEvent);
                            break;
            
                        case 0xC0: // Program Change (0xC0 to 0xCF)
                            channel = (byte) (status & 0x0F);
                            int programNumber = data[offset++] & 0x7F; // Program Number
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding program change number " + programNumber + " to channel " + channel);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, programNumber, 0), totalDuration);
                            channels[channel].add(midiEvent);
                            break;
            
                        case 0xD0: // Reserved (2 bytes)
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Skipping 2 reserved bytes");
                            offset += 2; // Skip 2 bytes
                            break;
            
                        case 0xE0: // Pitch Bend (0xE0 to 0xEF)
                            channel = (byte) (status & 0x0F);
                            int pitchBendLSB = data[offset++] & 0x7F; // Pitch Bend Change LSB
                            int pitchBendMSB = data[offset++] & 0x7F; // Pitch Bend Change MSB
                            Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Adding pitch bend MSB " + pitchBendMSB + " LSB " + pitchBendLSB + " to channel " + channel);
                            midiEvent = new MidiEvent(new ShortMessage(ShortMessage.PITCH_BEND, channel, pitchBendLSB, pitchBendMSB), totalDuration);
                            channels[channel].add(midiEvent);
                            break;

                        default:
                            // Unknown status
                            Mobile.log(Mobile.LOG_WARNING, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " + "Unknown status byte: " + status);
                            break;
                    }
                }
            }
        }

        Mobile.log(Mobile.LOG_DEBUG, SMAFDecoder.class.getPackage().getName() + "." + SMAFDecoder.class.getSimpleName() + ": " +"Sequence finished. Returning.");

        // Only update preWrap duration if we are not at the wrapped channels (hopefully no Handy Phone SMAF uses more than 8 sequence chunks or "32" simulated channels)
        if(formatType == 0x00 && !wrapInstruments && preWrapLongestDuration < totalDuration) { preWrapLongestDuration = totalDuration; }
    }

    private static int timeBasetoMs(byte timeBase) 
    {
        switch (timeBase) 
        {
            case 0x00: return 1;   // 1 ms
            case 0x01: return 2;   // 2 ms
            case 0x02: return 4;   // 4 ms
            case 0x03: return 5;   // 5 ms
            case 0x10: return 10;  // 10 ms
            case 0x11: return 20;  // 20 ms
            case 0x12: return 40;  // 40 ms
            case 0x13: return 50;  // 50 ms
            default: return 4;     // Default to 4 ms
        }
    }

    private static byte[] decodeHuffman(byte[] seqBytes) 
    {
        StringBuilder bitString = new StringBuilder();
    
        // Convert bytes to bits
        for (byte b : seqBytes) 
        {
            for (int i = 7; i >= 0; i--) { bitString.append((b >> i) & 1); }
        }
    
        List<Byte> decodedBytes = new ArrayList<>();
        StringBuilder currentCode = new StringBuilder();
    
        // Decode using Huffman codes
        for (char bit : bitString.toString().toCharArray()) 
        {
            currentCode.append(bit);
            boolean found = false;
    
            for (Map.Entry<Byte, String> entry : huffmanCodes.entrySet()) 
            {
                if (entry.getValue().equals(currentCode.toString())) // We found a matching code
                {
                    decodedBytes.add(entry.getKey());
                    currentCode.setLength(0);
                    found = true;
                    break;
                }
            }
        }
    
        // Convert the decoded List<Byte> back to a byte array
        byte[] result = new byte[decodedBytes.size()];
        for (int i = 0; i < decodedBytes.size(); i++) { result[i] = decodedBytes.get(i); }
    
    
        return result;
    }

    // Handy Phone format has a separate set of instruments for the drum bank (any time the bank change goes over 127), we cannot convert instruments to MIDI 1:1 in those cases
    private static byte handyPhoneBankToMidi(byte handyInst, byte channel)
    {
        if(!channelData[channel].usingDrumBank) { return handyInst; }
        
        switch(handyInst) 
        {
            case 24: return 108; // SeqClick H
            case 25: return 106; // Brush Tap
            case 26: return 107; // Brush Swirl L
            case 27: return 109; // Brush Slap
            case 28: return 110; // Brush Swirl H
            case 29: return 60;  // Snare Roll
            case 30: return 65;  // Castanet
            case 31: return 61;  // Snare L
            case 32: return 36;  // SeqClick H (Acoustic Bass Drum)
            case 33: return 34;  // Brush Tap (Fingered Bass)
            case 34: return 35;  // Bass Drum L
            case 35: return 36;  // Bass Drum M
            case 36: return 37;  // Closed Rim Shot
            case 37: return 38;  // Snare M
            case 38: return 39;  // Hand Clap
            case 39: return 41;  // Floor Tom L
            case 40: return 40;  // Snare H
            case 41: return 43;  // Floor Tom H
            case 42: return 42;  // Hi-Hat Closed
            case 43: return 45;  // Floor Tom M
            case 44: return 46;  // Hi-Hat Pedal
            case 45: return 47;  // Low Tom
            case 46: return 48;  // Hi-Hat Open
            case 47: return 49;  // Mid Tom L
            case 48: return 50;  // Mid Tom H
            case 49: return 49;  // Crash Cymbal 1
            case 50: return 50;  // High Tom
            case 51: return 51;  // Ride Cymbal 1
            case 52: return 52;  // Chinese Cymbal
            case 53: return 53;  // Ride Cymbal Cup
            case 54: return 54;  // Tambourine
            case 55: return 57;  // Splash Cymbal
            case 56: return 58;  // Cowbell
            case 57: return 59;  // Crash Cymbal 2
            case 58: return 60;  // Vibraslap
            case 59: return 61;  // Bongo H
            case 60: return 62;  // Bongo L
            case 61: return 63;  // Conga H
            case 62: return 64;  // Conga H Open
            case 63: return 65;  // Conga L
            case 64: return 66;  // Timbale H
            case 65: return 67;  // Timbale L
            case 66: return 68;  // Agogo H
            case 67: return 69;  // Agogo L
            case 68: return 70;  // Cabasa
            case 69: return 71;  // Maracas
            case 70: return 72;  // Tambourine
            case 71: return 73;  // Triangle
            case 72: return 74;  // Shaker
            case 73: return 75;  // Jingle Bell
            case 74: return 76;  // Bottle Wood Block H
            case 75: return 77;  // Wood Block L
            case 76: return 78;  // Cuica Mute
            case 77: return 79;  // Cuica Open
            case 78: return 80;  // Square Lead
            case 79: return 81;  // Triangle Mute
            case 80: return 82;  // Saw Lead
            case 81: return 83;  // Triangle Open
            case 82: return 84;  // Shaker
            case 83: return 85;  // Jingle Bell
            case 84: return 86;  // Belltree
            default: return 0;   // Default case (we should never hit this)
        }
    }

    // Helper method to get the timebase readable string based on its byte value
    private static String getTimeBaseDescription(byte timeBaseValue) 
    {
        for (SimpleEntry<Byte, String> entry : timebases) 
        {
            if (entry.getKey().equals(timeBaseValue)) 
            {
                return entry.getValue();
            }
        }
        return "Unknown TimeBase";
    }

    private static int getVariableLengthValue(byte[] data, int[] offset) 
    {
        int value = 0;
        int byteRead;
    
        do 
        {
            byteRead = data[offset[0]] & 0xFF; // Read the byte
            value = ((value & 0x3F) << 7) | (byteRead & 0x7F);
            offset[0]++; // Move to the next byte
        } while ((byteRead & 0x80) != 0);
    
        return value; // Return the constructed value
    }
}

class ChannelData 
{
    public byte keyControl, channelType;
    public boolean keyControlBasic, led, vibStatus, usingDrumBank;
    public byte octaveShift, velocity;

    ChannelData() 
    {
        keyControl = 0x1;
        channelType = 0x0;
        led = false;
        vibStatus = false;
        octaveShift = 0;
        usingDrumBank = false;
        velocity = 0;
    }
}

class HuffmanNode implements Comparable<HuffmanNode> 
{
    byte data;      // The byte value (only for leaf nodes)
    int frequency;  // Frequency of the byte
    HuffmanNode left;   // Left child
    HuffmanNode right;  // Right child

    // Constructor for leaf nodes
    public HuffmanNode(byte data, int frequency) 
    {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    // Constructor for internal nodes
    public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) 
    {
        this.data = 0; // Not used for internal nodes
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode other) 
    {
        return Integer.compare(this.frequency, other.frequency);
    }
}

class HuffmanTree 
{
    // Build the Huffman tree from a frequency map
    public HuffmanNode buildTree(Map<Byte, Integer> frequencyMap) 
    {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        // Create leaf nodes for each byte and add to the priority queue
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) 
        {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // Build the tree
        while (priorityQueue.size() > 1) 
        {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency, left, right);
            priorityQueue.add(parent);
        }

        return priorityQueue.poll(); // The root of the tree
    }

    // Generate Huffman codes from the Huffman tree
    public void generateCodes(HuffmanNode root, String code, Map<Byte, String> huffmanCodes) 
    {
        if (root == null) return;

        // Leaf node
        if (root.left == null && root.right == null) 
        {
            huffmanCodes.put(root.data, code);
            return;
        }

        // Traverse left and right
        generateCodes(root.left, code + "0", huffmanCodes);
        generateCodes(root.right, code + "1", huffmanCodes);
    }
}

class SmafCRC 
{
    private static final int CHAR_BIT = 8;
    private static final int UCHAR_MAX = 0xFF;
    private static final int CRCPOLY1 = 0x1021;
    private static final int[] crctable = new int[UCHAR_MAX + 1];

    static { makeCRCTable(); }

    private static void makeCRCTable() 
    {
        for (int i = 0; i <= UCHAR_MAX; i++) 
        {
            int r = i << (16 - CHAR_BIT);
            for (int j = 0; j < CHAR_BIT; j++) 
            {
                if ((r & 0x8000) != 0) { r = (r << 1) ^ CRCPOLY1; } 
                else { r <<= 1; }
            }
            crctable[i] = r & 0xFFFF;
        }
    }

    public static int makeCRC(int n, byte[] c) 
    {
        int r = 0xFFFF;
        int i = 0;
        while(--n >= 0) 
        {
            r = (r << CHAR_BIT) ^ crctable[(byte) (((r >> (16 - CHAR_BIT)) ^ c[i++])) & 0xFF];
        }
        return ~r & 0xFFFF; // Return the inverted CRC
    }

    public static boolean verifySmafCRC(byte[] fileChunk) 
    {
        if (fileChunk.length < 2) { return false; }

        int providedCRC = ((fileChunk[fileChunk.length - 2] & 0xFF) << 8) | (fileChunk[fileChunk.length - 1] & 0xFF);

        int calculatedCRC = makeCRC(fileChunk.length - 2, fileChunk);

        return calculatedCRC == providedCRC;
    }
}