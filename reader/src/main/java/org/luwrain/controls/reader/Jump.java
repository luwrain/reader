// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

/**
 * A computed jump to the beginning of the next sentence.
 *
 * <p>This class is package-private. It is used by {@link ReaderArea} when the
 * user asks for the next sentence while a document is being listened to.</p>
 */
class Jump
{
    final Iterator it;
    final int pos;
    final String text;
    final Sounds sound;

    Jump()
    {
	this.it = null;
	this.pos = 0;
	this.text = "";
	this.sound = null;
    }

    Jump(Iterator it, int pos,
	 String text, Sounds sound)
    {
	requireNonNull(it, "it can't be null");
	requireNonNull(text, "text can't be null");
	this.it = it;
	this.pos = pos;
	this.text = text;
	this.sound = sound;
    }

    boolean isEmpty()
    {
	return it == null;
    }

    void announce(ControlContext environment)
    {
	requireNonNull(environment, "environment can't be null");
	if (isEmpty())
	{
	    environment.playSound(Sounds.BLOCKED);
	    return;
	}
	if (sound != null)
	    environment.say(text, sound); else
	    environment.say(text);
    }

    static Jump nextSentence(Iterator fromIt, int fromPos)
    {
	requireNonNull(fromIt, "fromIt can't be null");
	Iterator it = fromIt.clone();
	final int pos = SentenceUtils.findNextSentenceBeginning(it.getText(), fromPos);
	//Do we have a new sentence at the current iterator position
	if (pos >= 0)
	{
	    if (pos < it.getText().length())
		return new Jump(it, pos, getSentenceText(it, pos), chooseSound(it, pos));
	    //We have only the sentence end here, so the next sentence begins further below
	    it = findTextBelow(it);
	    if (it == null)
		return new Jump();
	    return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
	}
	//It is necessary to check the next iterator positions
	if (!it.moveNext())
	    return new Jump();
	do {
	    Log.debug("reader", "checking " + it.getText());
	    if (it.isParagraphBeginning())
		return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
	    final int pos2 = SentenceUtils.findNextSentenceBeginning(it.getText(), 0);
	    if (pos2 >= 0)
	    {
		if (pos2 < it.getText().length())
		    return new Jump(it, pos2, getSentenceText(it, pos2), chooseSound(it, pos2));
		//We have only the sentence end here, so the next sentence begins further below
		it = findTextBelow(it);
		if (it == null)
		    return new Jump();
		return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
	    }
	} while (it.moveNext());
	return new Jump();
    }

    //Returns an iterator with any text below, or null if there is no text below
    static private Iterator findTextBelow(Iterator fromIt)
    {
	requireNonNull(fromIt, "fromIt can't be null");
	final Iterator it = fromIt.clone();
	if (!it.moveNext())
	    return null;
	do {
	    if (!it.getText().trim().isEmpty())
		return it;
	} while (it.moveNext());
	return null;
    }

    static private String getSentenceText(Iterator fromIt, int fromPos)
    {
	requireNonNull(fromIt, "fromIt can't be null");
	{
	    final int pos = SentenceUtils.findNextSentenceBeginning(fromIt.getText(), fromPos);
	    if (pos >= 0)
		return fromIt.getText().substring(fromPos, Math.min(pos, fromIt.getText().length()));
	}
	final StringBuilder b = new StringBuilder();
	b.append(fromIt.getText().substring(Math.min(fromPos, fromIt.getText().length())));
	final Iterator it = fromIt.clone();
	if (!it.moveNext())
	    return new String(b);
	do {
	    if (it.isParagraphBeginning())
		return new String(b);
	    final int pos = SentenceUtils.findNextSentenceBeginning(it.getText(), 0);
	    if (pos >= 0)
	    {
		b.append(" " + it.getText().substring(0, Math.min(pos, it.getText().length())));
		return new String(b);
	    }
	    b.append(" " + it.getText());
	} while (it.moveNext());
	return new String(b);
    }

    static private Sounds chooseSound(Iterator it, int pos)
    {
	requireNonNull(it, "it can't be null");
	if (!it.isParagraphBeginning() || pos > 0)
	    return null;
	return Sounds.PARAGRAPH;
    }
}
