
//LWR_API 1.0

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.reader.view.Iterator;

public class SentenceIterator
{
    private final Iterator it;
    private int pos;

    public SentenceIterator(Iterator it, int pos)
    {
	NullCheck.notNull(it, "it");
	this.it = it;
	this.pos = pos;
    }

    public boolean forward(StringBuilder b, String delim)
    {
	NullCheck.notNull(b, "b");
	NullCheck.notNull(delim, "delim");
	if (!skipInitialForward(b, delim))
	    return false;
		if (this.pos >=  this.it.getText().length())
	    throw new RuntimeException("pos (" + pos + ") == row.length (" + it.getText().length() + ")");
		boolean afterSentenceEnd = false;
do {
	final String text = it.getText();
	if (!afterSentenceEnd)
	{
final int nextPos = findNextSentenceInString(text, this.pos);
	if (nextPos >= 0 && nextPos < text.length())
	{
	    b.append(text.substring(this.pos, nextPos));
	    this.pos = nextPos;
	    return true;
	}
	if (nextPos >= 0)//There is the sentence end, but there is no next sentence begin
	    afterSentenceEnd = true;
		    this.pos = text.length();
		    b.append(text.substring(pos));
		    continue;
	}
	if (this.pos != 0)
	    throw new RuntimeException("pos (" + pos + ") is not zero");
	//Looking for any non-space character
	while(pos < text.length() && !Character.isSpaceChar(text.charAt(pos)))
	    pos++;
	b.append(text.substring(0, pos));
	if (pos < text.length())
	    return true;
} while(moveNextInParagraph(b, delim));
	return true;
    }

    protected boolean skipInitialForward(StringBuilder b, String delim)
    {
	NullCheck.notNull(b, "b");
	NullCheck.notNull(delim, "delim");
	if (pos < it.getText().length())
	    return true;
	while(it.canMoveNext())
	{
	    it.moveNext();
	    b.append(delim);
	    if (!it.getText().isEmpty())
	    {
		this.pos = 0;
		return true;
	    }
	}
	this.pos = it.getText().length();
	return false;
    }

    protected boolean moveNextInParagraph(StringBuilder b, String delim)
    {
	NullCheck.notNull(b, "b");
	NullCheck.notNull(delim, "dleim");
	final Paragraph p = it.getParagraph();
	final Iterator i = it.clone();
	if (!i.canMoveNext())
	    return false;
	i.moveNext();
	if (p != i.getParagraph())
	    return false;
	this.it.moveNext();
	this.pos = 0;
	b.append(delim);
	return true;
	
    }

    // Returns:
    // -1 if nothing found
    // any number less than the length of the text, if there is beginning of the next sentence on the current line
    // the length of the text, if there is end of the current sentence, but there is no beginning of the next sentence
    protected int findNextSentenceInString(String text, int posFrom)
    {
	NullCheck.notNull(text, "text");
	int pos = posFrom;
	//Looking for any character of the end of the sentence 
	while (pos < text.length() && (
				       text.charAt(pos) != '.' && text.charAt(pos) != '!' && text.charAt(pos) != '?'))
	    ++pos;
	if (pos >= text.length())
	    return -1;
	//Skipping all the characters of the sentence end
	while (pos < text.length() && (
				       text.charAt(pos) == '.' || text.charAt(pos) == '!' || text.charAt(pos) == '?'))
	    ++pos;
	if (pos >= text.length())
	    return text.length();
	//Skipping all spaces before the beginning of the next sentence
	while (pos < text.length() && Character.isSpace(text.charAt(pos)))
	    ++pos;
	return pos;
    }
}
