
package org.luwrain.reader.builders.pdf;

import java.io.*;
import java.util.*;

import java.awt.geom.AffineTransform;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.util.*;
import org.apache.pdfbox.pdmodel.*;

import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.contentstream.operator.text.*;

import org.luwrain.core.*;

final class PdfCharsExtractor
{
    PdfPage[] getChars(File file) throws IOException
    {
	NullCheck.notNull(file, "file");
	PDDocument document = null;
	try {
	    document = PDDocument.load(file);
	    return processPages(document);
	}
	finally {
	    if (document != null)
		document.close();
	}
    }

    private PdfPage[] processPages(PDDocument doc) throws IOException
    {
	NullCheck.notNull(doc, "doc");
	int pageNum = 0;
	final List<PdfPage> res = new LinkedList();
	for (PDPage page : doc.getPages())
	{
	    ++pageNum;
            if (page.hasContents())
	    {
		final StreamChars chars = new StreamChars();
                chars.processPage(page);
		res.add(new PdfPage(pageNum, chars.output.toArray(new PdfChar[chars.output.size()])));
	    }
	}
	return res.toArray(new PdfPage[res.size()]);
    }

    final static private class StreamChars extends org.apache.pdfbox.contentstream.PDFStreamEngine
    {
	final List<PdfChar> output = new java.util.Vector(); 

	StreamChars()
	{
	    addOperator(new BeginText());
	    addOperator(new Concatenate());
	    addOperator(new DrawObject()); // special text version
	    addOperator(new EndText());
	    addOperator(new SetGraphicsStateParameters());
	    addOperator(new Save());
	    addOperator(new Restore());
	    addOperator(new NextLine());
	    addOperator(new SetCharSpacing());
	    addOperator(new MoveText());
	    addOperator(new MoveTextSetLeading());
	    addOperator(new SetFontAndSize());
	    addOperator(new ShowText());
	    addOperator(new ShowTextAdjusted());
	    addOperator(new SetTextLeading());
	    addOperator(new SetMatrix());
	    addOperator(new SetTextRenderingMode());
	    addOperator(new SetTextRise());
	    addOperator(new SetWordSpacing());
	    addOperator(new SetTextHorizontalScaling());
	    addOperator(new ShowTextLine());
	    addOperator(new ShowTextLineAndSpace());
	}

	@Override protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode, org.apache.pdfbox.util.Vector displacement) throws IOException
	{
	    final AffineTransform at = textRenderingMatrix.createAffineTransform();
	    at.concatenate(font.getFontMatrix().createAffineTransform());
	    final double x = getCm(at.getTranslateX());
	    final double y = getCm(at.getTranslateY());
	    final boolean bold = (font.getName().toLowerCase().indexOf("bold") >= 0);
	    output.add(new PdfChar((unicode != null && !unicode.isEmpty())?unicode.charAt(0):'\0', x, y, bold));
	}

	static private double getCm(double pt)
	{
	    return (pt / 72) * 2.54;
	}
    }
}
