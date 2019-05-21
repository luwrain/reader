
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

    public String forward(String delim)
    {
	final String thisRow = checkThisRowForward();
	if (thisRow != null)
	    return thisRow;
		//It is necessary to check next iterator positions
	final StringBuilder b = new StringBuilder();
	b.append(it.getText().substring(this.pos));
	if (!canMoveNext())
	{
	    //Saving the current row, but moving the pos to the end of the row
	    final String text = it.getText();
	    final String res = text.substring(this.pos);
	    this.pos = text.length();
	    return res;
	}
	do {
	    it.moveNext();
	    b.append(delim);
	    final String text = it.getText();
		    final int nextPos = findNextSentenceBeginning(text, 0);
		    if (nextPos < 0)
		    {
			b.append(text);
			continue;
		    }
	    if (nextPos < text.length())
	    {
		this.pos = nextPos;
		b.append(text.substring(0, nextPos));
		return new String(b);
	}
	} while (canMoveNext());
	this.pos = it.getText().length();
	return new String(b);
    }

    protected String checkThisRowForward()
    {
	final String text = it.getText();
	final int nextPos = findNextSentenceBeginning(text, pos);
	if (nextPos < 0)
	    return null;
	    //We have new sentence at the current iterator position
		if (pos < text.length())
		{
		    final String res = text.substring(this.pos, nextPos);
		    this.pos = nextPos;
		    return res;
		}
		//Ops, we have only sentence end here,the  beginning of the next sentence is somewhere on next iterator positions
		return null;//FIXME:
    }

    protected boolean canMoveNext()
    {
	final Paragraph p = it.getParagraph();
	final Iterator i = it.clone();
	if (!i.canMoveNext())
	    return false;
	i.moveNext();
	return p == i.getParagraph();
    }


    // Returns:
    // -1 if nothing found
    // any number less than the length of the text, if there is beginning of the next sentence on the current line
    // the length of the text, if there is end of the current sentence, but there is no beginning of the next sentence
    static private int findNextSentenceBeginning(String text, int posFrom)
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
