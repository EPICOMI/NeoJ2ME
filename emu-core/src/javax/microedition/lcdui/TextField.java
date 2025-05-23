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
package javax.microedition.lcdui;

import org.recompile.mobile.Mobile;

public class TextField extends Item
{
	public static final int ANY = 0;
	public static final int CONSTRAINT_MASK = 0xFFFF;
	public static final int DECIMAL = 5;
	public static final int EMAILADDR = 1;
	public static final int INITIAL_CAPS_SENTENCE = 0x200000;
	public static final int INITIAL_CAPS_WORD = 0x100000;
	public static final int NON_PREDICTIVE = 0x80000;
	public static final int NUMERIC = 2;
	public static final int PASSWORD = 0x10000;
	public static final int PHONENUMBER = 3;
	public static final int SENSITIVE = 0x40000;
	public static final int UNEDITABLE = 0x20000;
	public static final int URL = 4;


	private String text;
	private int max;
	private int constraints;
	private int caretPosition = 0;
	private String mode;
	private int padding;
	private int margin;
	private boolean highlighted;

	private char[][] charSet = // Not all of these charsets are complete.
	{
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%~^<.,;?/[]{}>&*()-_+'\"|`\n".toCharArray(), // Default subset, BASIC_LATIN, IS_LATIN
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%~^<.,;?/[]{}>&*()-_+'\"|`\n".toCharArray(),                           // MIDP_UPPERCASE_LATIN
		"abcdefghijklmnopqrstuvwxyz!@#$%~^<.,;?/[]{}>&*()-_+'\"|`0123456789\n".toCharArray(),                           // MIDP_LOWERCASE_LATIN
		"0123456789".toCharArray(),                                                                                     // NUMERIC, IS_LATIN_DIGITS
		"αβγδεζηθικλμνξοπρστυφχψω\n".toCharArray(),                                                                      // UCB_GREEK
		"абвгдежзийклмнопрстуфхцчшщъыьэюя\n".toCharArray(),                                                              // UCB_CYRILLIC
		"աբգդեիզլւխճմյնոպջռտուքֆք\n".toCharArray(),                                                                    // UCB_ARMENIAN
		"אבגדהווזחטיך\n".toCharArray(),                                                                                     // UCB_HEBREW
		"ابجدهوزحطی\n".toCharArray(),                                                                                     // UCB_ARABIC
		"अआइईउऊऋएऐओऔकखगघचछजझटठडढणतथदधनपरयलवशषसह\n".toCharArray(),                                                     // UCB_DEVANAGARI
		"অআইঈউঊএঐও\n".toCharArray(),                                                                                  // UCB_BENGALI
		"กขฃคฅฆงจฉชซฌญฎฏฐฑฒณดตถทธนบปผฝพฟห\n".toCharArray(),                                                            // UCB_THAI
		"あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん\n".toCharArray(),                 // UCB_HIRAGANA
		"アイウエオカキクケコサシスセソタチツテトナニヌネノ\n".toCharArray(),                                                    // UCB_KATAKANA
		"가각갂갃간갅갆갇\n".toCharArray(),                                                                                 // USB_HANGUL_SYLLABLES
		"０１２３４５６７８９\n".toCharArray(),                                                                             // IS_FULLWIDTH_DIGITS
		"ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ\n".toCharArray(),       // IS_FULLWIDTH_LATIN
		"ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝﾞﾟ\n".toCharArray(),                                                      // IS_HALFWIDTH_KATAKANA
		"日月火水木金土山川田口目耳手足心大小多少新古白黒青赤\n".toCharArray(),                                                  // IS_HANJA, IS_KANJI
		"我你他她它是不在有这那了人们说去来好学吃喝玩笑爱天日月年时\n".toCharArray(),                                             // IS_SIMPLIFIED_HANZI
		"我你他她它是不好在有這那了人們說去來好學吃喝玩笑愛天日月年時\n".toCharArray()                                            // IS_TRADITIONAL_HANZI
	};

	private char[][] charSetHint = 
	{
		"Lat".toCharArray(),
		"LAT".toCharArray(),
		"lat".toCharArray(),
		"NUM".toCharArray(),
		"GRK".toCharArray(),
		"CYR".toCharArray(),
		"ARME".toCharArray(),
		"HEBR".toCharArray(),
		"ARAB".toCharArray(),
		"DEVA".toCharArray(),
		"BENG".toCharArray(),
		"THAI".toCharArray(),
		"JP_あ".toCharArray(),
		"JP_ア".toCharArray(),
		"HANG".toCharArray(),
		"F_NUM".toCharArray(),
		"F_LAT".toCharArray(),
		"JP_ｱ".toCharArray(),
		"TH_日".toCharArray(),
		"JP_日".toCharArray(),
		"CN_S".toCharArray(),
		"CN_T".toCharArray()
	};

	private byte charSetIdx = 0; // Maps to the charsets above

	private int selectedCharIndex = 0;  // Index for the currently selected character


	public TextField(String label, String value, int maxSize, int Constraints)
	{
		setLabel(label);
		text = value == null ? "" : value;
		max = maxSize;
		constraints = Constraints;

		caretPosition = text.length();

		// these can't be static because of Font.getDefaultFont().getHeight()
		padding = Font.getDefaultFont().getHeight() / 5; 
		margin = Font.getDefaultFont().getHeight() / 5;
	}

	void delete(int offset, int length)
	{
		text = text.substring(0, offset) + text.substring(offset+length);
		if (caretPosition > text.length()) { caretPosition = text.length(); }
		_invalidateContents();
	}

	public int getCaretPosition() { return caretPosition; }

	public int getChars(char[] data)
	{
		for(int i=0; i<text.length(); i++)
		{
			data[i] = text.charAt(i);
		}
		return text.length();
	}

	public int getConstraints() { return constraints; }

	public int getMaxSize() { return max; }

	public String getString() { return text; }

	public void insert(char[] data, int offset, int length, int position)
	{
		StringBuilder out = new StringBuilder();
		out.append(text, 0, position);
		out.append(data, offset, length);
		out.append(text.substring(position));
		text = out.toString();

		caretPosition = text.length();

		_invalidateContents();
	}

	public void insert(String src, int position)
	{
		StringBuilder out = new StringBuilder();
		out.append(text, 0, position);
		out.append(src);
		out.append(text.substring(position));
		text = out.toString();

		caretPosition = text.length();

		_invalidateContents();
	}

	public void setChars(char[] data, int offset, int length)
	{
		if (data == null) {
			setString("");
			return;
		}

		StringBuilder out = new StringBuilder();
		out.append(data, offset, length);
		text = out.toString();
		caretPosition = text.length();
		_invalidateContents();
	}

	public void setConstraints(int Constraints) { constraints = Constraints; }

	public void setInputMode(int mode) { charSetIdx = (byte) mode; }

	public void setInitialInputMode(String characterSubset) 
	{ 
		mode = characterSubset;

		switch (mode) 
		{
            case "MIDP_UPPERCASE_LATIN":
                charSetIdx = 1;
                break;
            case "MIDP_LOWERCASE_LATIN":
                charSetIdx = 2;
                break;
            case "NUMERIC":
			case "IS_LATIN_DIGITS":
                charSetIdx = 3;
                break;
			case "UCB_GREEK":
                charSetIdx = 4;
                break;
            case "UCB_CYRILLIC":
                charSetIdx = 5;
                break;
            case "UCB_ARMENIAN":
                charSetIdx = 6;
                break;
			case "UCB_HEBREW":
                charSetIdx = 7;
                break;
			case "UCB_ARABIC":
                charSetIdx = 8;
                break;
            case "UCB_DEVANAGARI":
                charSetIdx = 9;
                break;
            case "UCB_BENGALI":
                charSetIdx = 10;
                break;
			case "UCB_THAI":
                charSetIdx = 11;
                break;
			case "UCB_HIRAGANA":
                charSetIdx = 12;
                break;
            case "UCB_KATAKANA":
                charSetIdx = 13;
                break;
            case "USB_HANGUL_SYLLABLES":
                charSetIdx = 14;
                break;
			case "IS_FULLWIDTH_DIGITS":
                charSetIdx = 15;
                break;
            case "IS_FULLWIDTH_LATIN":
                charSetIdx = 16;
                break;
			case "IS_HALFWIDTH_KATAKANA":
                charSetIdx = 17;
                break;
            case "IS_HANJA":
                charSetIdx = 18;
                break;
			case "IS_SIMPLIFIED_HANZI":
                charSetIdx = 19;
                break;
			case "IS_TRADITIONAL_HANZI":
                charSetIdx = 20;
                break;
            default:
                charSetIdx = 0; // Default subset (BASIC_LATIN, IS_LATIN)
                break;
        }
	}

	public int setMaxSize(int maxSize) { max = maxSize; return max; }

	public void setString(String value) 
	{
		if (value == null) { value = ""; }
		
		text = value;
		caretPosition = text.length();
		_invalidateContents();
	}

	public int size() { return text.length(); }

	protected int getContentHeight(int width) { return Font.getDefaultFont().getHeight() + padding*2 + 2*margin; /* padding */ }

	protected boolean keyPressed(int key) 
	{
		boolean handled = true, changed = true;

		if(constraints == UNEDITABLE) { return false; } // If this field is uneditable, the user shall not be able to make changes through input
		else 
		{
			if (key == Canvas.DOWN) { selectedCharIndex = (selectedCharIndex - 1 + charSet[charSetIdx].length) % charSet[charSetIdx].length; } // Cycle down through the character set
			else if (key == Canvas.UP) { selectedCharIndex = (selectedCharIndex + 1) % charSet[charSetIdx].length; } // Cycle up through the character set
			else if (key == Canvas.LEFT && caretPosition > 0) // Move back one char
			{ 
				caretPosition--;
				// Check the character under the caret
				char currentChar = text.charAt(caretPosition);
				// Find the index of the current character in charSet
				for (int i = 0; i < charSet[charSetIdx].length; i++) 
				{
					if (charSet[charSetIdx][i] == currentChar) 
					{
						selectedCharIndex = i;
						break;
					}
				}
				changed = true;
			} 
			else if (key == Canvas.RIGHT && caretPosition < text.length()) // Move forward one char
			{
				if(caretPosition+1 < text.length())
				{
					char currentChar = text.charAt(caretPosition+1);
					for (int i = 0; i < charSet[charSetIdx].length; i++) 
					{
						if (charSet[charSetIdx][i] == currentChar) 
						{
							selectedCharIndex = i;
							break;
						}
					}
					changed = true;
				}
				caretPosition++;
			} 
			else if (key == Canvas.FIRE || key == Canvas.KEY_NUM5) // Insert the selected character into the current caret position
			{ 
				if (caretPosition < text.length()) // Replace the character at the caret position
				{
					text = text.substring(0, caretPosition) + charSet[charSetIdx][selectedCharIndex] + text.substring(caretPosition + 1);
					caretPosition++;
				} 
				else // Append if at the end if the caret is already at the end
				{
					if(text.length() < max) 
					{
						text += charSet[charSetIdx][selectedCharIndex];
						caretPosition++;
					}
				}
				changed = true;
			}
			else if (key == Canvas.KEY_STAR) // Remove the char at the current caret position
			{ 
				if (caretPosition < text.length()) 
				{
					// Remove the character at the caret position
					text = text.substring(0, caretPosition) + text.substring(caretPosition + 1);
					// Optionally, move caret left after deletion
					caretPosition = Math.max(caretPosition - 1, 0);
					changed = true;
				}
			}
			else if (key == Canvas.KEY_POUND && constraints != (NUMERIC | EMAILADDR | PHONENUMBER | DECIMAL)) // Insert a space into the current caret position (in constrants that allow it)
			{ 
				if (caretPosition < text.length() && text.length() < max) // Replace the character at the caret position
				{
					text = text.substring(0, caretPosition) + ' ' + text.substring(caretPosition);
					caretPosition++;
				} 
				else // Append if at the end if the caret is already at the end
				{
					if(text.length() < max) 
					{
						text += ' ';
						caretPosition++;
					}
				}
				changed = true;
			}
			else { handled = false; changed = false; }

			if (changed) { notifyStateChanged(); }

			if (handled) { _invalidateContents(); }

			return handled;
		}
	}

	protected void renderItem(Graphics graphics, int x, int y, int width, int height) 
	{
		graphics.getGraphics2D().translate(x, y);

		// Draw the border of the field
		graphics.setColor(Mobile.lcduiTextColor);
		graphics.drawRect( margin, margin, width - margin, Font.getDefaultFont().getHeight() + margin);

		// Replace line breaks, they aren't visible by default.
		String formattedText = text.replace('\n', '↳');
		
		// Draw the existing text before the caret (we'll make a space to highlight the char position the user is currently editing)
		graphics.setColor(Mobile.lcduiTextColor);

		if (caretPosition > 0) {
			graphics.drawChars(formattedText.substring(0, caretPosition).toCharArray(), 0, formattedText.substring(0, caretPosition).length(), margin + padding, margin + padding, 0);
		}

		int caretWidth = Font.getDefaultFont().stringWidth(formattedText.substring(0, caretPosition));

		// Fill the background for the character to be inserted (always at the caret position)
		// Check if the character to be drawn at the caret is a line break
		String caretChar = (charSet[charSetIdx][selectedCharIndex] == '\n') ? "↳" : String.valueOf(charSet[charSetIdx][selectedCharIndex]);
		int caretCharWidth = Font.getDefaultFont().stringWidth(caretChar);

		graphics.setColor(Mobile.lcduiTextColor); // Fill with the same color as the text (effectively giving a strong background color to the caret position
		graphics.fillRect(margin + padding + caretWidth, margin + padding, caretCharWidth, Font.getDefaultFont().getHeight());

		graphics.setColor(Mobile.lcduiBGColor); // Set to background color for the character
		graphics.drawString(caretChar, margin + padding + caretWidth, margin + padding, 0);


		// Draw the remaining text after the caret
		int remainWidth = 0;
		graphics.setColor(Mobile.lcduiTextColor); // Restore color to the text's default after the caret position
		if(formattedText.length() - (caretPosition+1) > 0) 
		{
			graphics.drawChars(formattedText.substring(caretPosition + 1).toCharArray(), 0, formattedText.length() - (caretPosition + 1), margin + padding + caretWidth + caretCharWidth, margin + padding, 0);
			remainWidth = Font.getDefaultFont().stringWidth(formattedText.substring(caretPosition + 1));
		}
		
		// Draw indicators to show whether more text is allowed or not
		String indicator = (formattedText.length() < max) ? "⨁" : "⨂";
		graphics.setColor(formattedText.length() < max ? 0x00BB00 : 0x770000); // Color based on state
		graphics.drawString(indicator, margin + padding + caretWidth + caretCharWidth + remainWidth, margin + padding, 0);

		// Draw arrows using "^" and "v" characters to hint the user that the current field can be altered
		graphics.setColor(Mobile.lcduiTextColor); // Set arrow color
		graphics.drawString("^", margin + padding + caretWidth + caretCharWidth / 2 - 2, margin + padding - Font.getDefaultFont().getHeight() + padding, 0); // Arrow up
		graphics.drawString("v", margin + padding + caretWidth + caretCharWidth / 2 - 2, margin + padding + Font.getDefaultFont().getHeight(), 0); // Arrow down

		// Render the characterSet hint
		String hintText = new String(charSetHint[charSetIdx]);
		int hintWidth = Font.getDefaultFont().stringWidth(hintText);
	
		// Draw background for hint text (it follows the same logic as the highlighted caret char)
		graphics.setColor(Mobile.lcduiTextColor);
		graphics.fillRect(width - margin - hintWidth - padding, margin + padding + Font.getDefaultFont().getHeight(), hintWidth+1, Font.getDefaultFont().getHeight() - 4);
	
		graphics.setColor(Mobile.lcduiBGColor);
		graphics.drawString(hintText, width - margin - hintWidth - padding, margin + padding + Font.getDefaultFont().getHeight(), 0);

		graphics.getGraphics2D().translate(-x, -y);
	}

	protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) 
	{
		if (!highlighted) 
		{
			highlighted = true;
			_invalidateContents();
		}
		
		return false;
	}

	protected void traverseOut() 
	{ 
		if (highlighted) 
		{
			highlighted = false;
			_invalidateContents();
		}
	}
}
