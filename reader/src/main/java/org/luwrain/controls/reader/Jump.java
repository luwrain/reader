// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

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
	final int pos = findNextSentenceBeginning(it.getText(), fromPos);
	//Do we have new sentence at the current iterator position
	if (pos >= 0)
	{
	    if (pos < it.getText().length())
		return new Jump(it, pos, getSentenceText(it, pos), chooseSound(it, pos));
	    //Ops, we have only sentence end here, beginning of the next sentence is somewhere on next iterator positions
	    it = findTextBelow(it);
	    return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
	}
	//It is necessary to check next iterator positions
	if (!it.moveNext())
	    return new Jump();
	do {
	    Log.debug("reader", "checking " + it.getText());
	    if (it.isParagraphBeginning())
		return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
	    final int pos2 = findNextSentenceBeginning(it.getText(), 0);
	    if (pos2 >= 0)
	    {
		if (pos2 < it.getText().length())
		    return new Jump(it, pos2, getSentenceText(it, pos2), chooseSound(it, pos));
		//Ops, we have only sentence end here, beginning of the next sentence is somewhere on next iterator positions
		it = findTextBelow(it);
		return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
	    }
	} while (it.moveNext());
	return new Jump();
    }

    //Returns any new position with any text or the same position as given, if there is no text below
    static private Iterator findTextBelow(Iterator fromIt)
    {
	requireNonNull(fromIt, "fromIt can't be null");
	final Iterator it = fromIt.clone();
	if (!it.moveNext())
	    return fromIt;
	do {
	    if (!it.getText().trim().isEmpty())
		return it;
	} while(it.moveNext());
	return fromIt;
    }

    // Returns:
    // -1 if nothing found
    // any number less than the length of the text, if there is beginning of the next sentence on the current line
    // the length of the text, if there is end of the current sentence, but there is no beginning of the next sentence
    static private int findNextSentenceBeginning(String text, int posFrom)
    {
	requireNonNull(text, "text can't be null");
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

    static private String getSentenceText(Iterator fromIt, int fromPos)
    {
	requireNonNull(fromIt, "fromIt can't be null");
	{
	    final int pos = findNextSentenceBeginning(fromIt.getText(), fromPos);
	    if (pos >= 0)
		return fromIt.getText().substring(fromPos, pos);
	}
	final StringBuilder b = new StringBuilder();
	b.append(fromIt.getText().substring(fromPos));
	final Iterator it = fromIt.clone();
	if (!it.moveNext())
	    return new String(b);
	do {
	    if (it.isParagraphBeginning())
		return new String(b);
	    final int pos = findNextSentenceBeginning(it.getText(), 0);
	    if (pos >= 0)
	    {
		b.append(" " + it.getText().substring(0, pos));
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
