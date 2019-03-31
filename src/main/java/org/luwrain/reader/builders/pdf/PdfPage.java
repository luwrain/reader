
package org.luwrain.reader.builders.pdf;

import org.luwrain.core.*;

final class PdfPage
{
    final int num;
    final PdfChar[] chars;

    PdfPage(int num, PdfChar[] chars)
    {
	if (num < 1)
	    throw new IllegalArgumentException("num (" + num + " may not be less than 1");
	NullCheck.notNullItems(chars, "chars");
	this.num = num;
	this.chars = chars;
    }
}
