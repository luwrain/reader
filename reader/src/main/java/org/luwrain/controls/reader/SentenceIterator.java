// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

/**
 * Iterates over sentences inside a {@link Paragraph}.
 *
 * <p>The iterator is constructed at a particular row position and walks
 * forward while staying inside the current paragraph. It may also pass over
 * empty rows and row boundaries.</p>
 */
public class SentenceIterator
{
    private final Iterator it;
    private int pos;

    /**
     * Creates a sentence iterator before the supplied reader row position.
     *
     * @param it the row iterator to start from; must not be {@code null}
     * @param pos the position inside the current row
     * @throws NullPointerException if {@code it} is {@code null}
     */
    public SentenceIterator(Iterator it, int pos)
    {
	requireNonNull(it, "it can't be null");
	this.it = it.clone();
	this.pos = pos;
    }

    /**
     * Advances the iterator to the beginning of the next sentence and appends
     * the skipped text to the supplied string builder.
     *
     * @param b the string builder to append text to; must not be {@code null}
     * @param delim the delimiter inserted between rows; must not be {@code null}
     * @return {@code true} if the iterator successfully advanced, {@code false} otherwise
     * @throws NullPointerException if {@code b} or {@code delim} is {@code null}
     */
    public boolean forward(StringBuilder b, String delim)
    {
	requireNonNull(b, "b can't be null");
	requireNonNull(delim, "delim can't be null");
	boolean afterSentenceEnd;
	if (atRowEnd())
	{
	    if (!skipInitialForward(b, delim))
		return false;
	    afterSentenceEnd = true;
	} else
	    afterSentenceEnd = false;
	if (this.pos > this.it.getText().length())
	    throw new IllegalStateException("pos (" + pos + ") > row.length (" + it.getText().length() + ")");
	do {
	    final String text = it.getText();
	    if (!afterSentenceEnd)
	    {
		final int nextPos = SentenceUtils.findNextSentenceBeginning(text, this.pos);
		if (nextPos >= 0 && nextPos < text.length())
		{
		    b.append(text.substring(this.pos, nextPos));
		    this.pos = nextPos;
		    return true;
		}
		if (nextPos >= 0)//There is the sentence end, but no next sentence beginning on this row
		    afterSentenceEnd = true;
		b.append(text.substring(pos));
		this.pos = text.length();
		continue;
	    }
	    if (this.pos != 0)
		throw new IllegalStateException("pos (" + pos + ") is not zero");
	    //Looking for any non-space character
	    while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
		pos++;
	    b.append(text.substring(0, pos));
	    if (pos < text.length())
		return true;
	} while (moveNextInParagraph(b, delim));
	return true;
    }

    protected boolean skipInitialForward(StringBuilder b, String delim)
    {
	requireNonNull(b, "b can't be null");
	requireNonNull(delim, "delim can't be null");
	while (it.canMoveNext())
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
	requireNonNull(b, "b can't be null");
	requireNonNull(delim, "delim can't be null");
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

    /**
     * Finds the position where the next sentence starts on the supplied row.
     *
     * @param text the row text; must not be {@code null}
     * @param posFrom the position to start searching from
     * @return the position of the beginning of the next sentence, {@code -1}
     *         if there is no sentence end on the row, or {@code text.length()}
     *         if the sentence ends on this row but its next beginning is later
     */
    protected int findNextSentenceInString(String text, int posFrom)
    {
	return SentenceUtils.findNextSentenceBeginning(text, posFrom);
    }

    /**
     * Returns a snapshot of the current row iterator.
     *
     * @return the current row iterator
     */
    public Iterator getIterator()
    {
	return it.clone();
    }

    /**
     * Returns the current offset inside the current row.
     *
     * @return the current offset
     */
    public int getPos()
    {
	return pos;
    }

    /**
     * Checks whether the current position is at the end of the current row.
     *
     * @return {@code true} if the position is at or past the end of the row,
     *         {@code false} otherwise
     */
    public boolean atRowEnd()
    {
	return pos >= it.getText().length();
    }
}
