
package org.luwrain.reader.builders.pdf;

final class PdfChar
{
    final char ch;
    final double x;
    final double y;
    final boolean bold;

    PdfChar(char ch, double x, double y, boolean bold)
    {
	 this.ch = ch;
	 this.x = x;
	 this.y = y;
	 this.bold = bold;
	 }
}
