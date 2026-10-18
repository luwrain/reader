// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import static java.util.Objects.*;

/**
 * Shared helpers for working with sentence boundaries in reader rows.
 *
 * <p>This class is package-private because it is an implementation detail of
 * the reader package. It keeps sentence-detection logic in one place so that
 * {@link Jump} and {@link SentenceIterator} do not drift apart.</p>
 */
final class SentenceUtils
{
    /**
     * Finds the position where the next sentence starts.
     *
     * @param text the row text; must not be {@code null}
     * @param posFrom the position to start searching from
     * @return the position where the next sentence starts, {@code -1} if there
     *         is no sentence end on the current row, or {@code text.length()}
     *         if the current sentence ends on this row but the next sentence
     *         begins later
     * @throws NullPointerException if {@code text} is {@code null}
     */
    static int findNextSentenceBeginning(String text, int posFrom)
    {
	requireNonNull(text, "text can't be null");
	int pos = posFrom;
	while (pos < text.length() && !isSentenceEndChar(text.charAt(pos)))
	    ++pos;
	if (pos >= text.length())
	    return -1;
	while (pos < text.length() && isSentenceEndChar(text.charAt(pos)))
	    ++pos;
	if (pos >= text.length())
	    return text.length();
	while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
	    ++pos;
	return pos;
    }

    /**
     * Checks whether the specified character may end a sentence.
     *
     * @param ch the character to check
     * @return {@code true} if the character is a sentence-ending punctuation mark,
     *         {@code false} otherwise
     */
    static boolean isSentenceEndChar(char ch)
    {
	switch(ch)
	{
	case '.':
	case '!':
	case '?':
	    return true;
	default:
	    return false;
	}
    }
}
