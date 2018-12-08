/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

//LWR_API 1.0

package org.luwrain.controls.doc;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.view.Iterator;

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
	NullCheck.notNull(it, "it");
	NullCheck.notNull(text, "text");
	this.it = it;
	this.pos = pos;
	this.text = text;
	this.sound = sound;
    }

    boolean isEmpty()
    {
	return it == null;
    }

    void announce(ControlEnvironment environment)
    {
	NullCheck.notNull(environment, "environment");
	if (isEmpty())
	{
	    environment.playSound(Sounds.BLOCKED);
	    return;
	}
	if (sound != null)
	    environment.say(text, sound); else
	    environment.say(text);
    }

    static Jump nextParagraph(Iterator fromIt, int fromPos)
    {
	NullCheck.notNull(fromIt, "fromIt");
	//	if (fromIt.isEmptyRow())
	//	    return new Jump();
	//Looking for the beginning of the next paragraph
	final Iterator it = (Iterator)fromIt.clone();
	if (!it.moveNext())
	    return new Jump();
		do {
		    if (it.isParagraphBeginning() && /*!it.isEmptyRow() &&*/ !it.isTitleRow())
			return new Jump(it, 0, getParagraphText(it), chooseSound(it, 0));
		} while (it.moveNext());
	return new Jump();
    }

    static private String getParagraphText(Iterator fromIt)
    {
	NullCheck.notNull(fromIt, "fromIt");
	//	if (fromIt.isEmptyRow())
	//	    return "";
	final Iterator it = (Iterator)fromIt.clone();
	final StringBuilder b = new StringBuilder();
	do {
	    b.append(it.getText() + " ");
	} while(it.moveNext() && !it.isParagraphBeginning());
	return new String(b).trim();
    }

    static Jump nextSentence(Iterator fromIt, int fromPos)
    {
	NullCheck.notNull(fromIt, "fromIt");
	Iterator it = (Iterator)fromIt.clone();
	if (/*!it.isEmptyRow() &&*/ !it.isTitleRow())
	{
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
	}
	//It is necessary to check next iterator positions
	if (!it.moveNext())
	    return new Jump();
	do {
	    if (/*it.isEmptyRow() ||*/ it.isTitleRow())
		continue;
	    Log.debug("reader", "checking " + it.getText());
	    if (it.isParagraphBeginning())
		return new Jump(it, 0, getSentenceText(it, 0), chooseSound(it, 0));
		    final int pos = findNextSentenceBeginning(it.getText(), 0);
	if (pos >= 0)
	{
	    if (pos < it.getText().length())
		return new Jump(it, pos, getSentenceText(it, pos), chooseSound(it, pos));
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
	NullCheck.notNull(fromIt, "fromIt");
	final Iterator it = (Iterator)fromIt;
	if (!it.moveNext())
	    return fromIt;
	do {
	    if (/*it.isEmptyRow() ||*/ it.isTitleRow())
		continue;
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

    static private String getSentenceText(Iterator fromIt, int fromPos)
    {
	NullCheck.notNull(fromIt, "fromIt");
	if (/*!fromIt.isEmptyRow() &&*/ !fromIt.isTitleRow())
	{
	    final int pos = findNextSentenceBeginning(fromIt.getText(), fromPos);
	    if (pos >= 0)
		return fromIt.getText().substring(fromPos, pos);
	}
	    final StringBuilder b = new StringBuilder();
	b.append(fromIt.getText().substring(fromPos));
	final Iterator it = (Iterator)fromIt.clone();
	if (!it.moveNext())
	    return new String(b);
	do {
	    if (/*it.isEmptyRow() ||*/ it.isTitleRow())
		continue;
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
	NullCheck.notNull(it, "it");
	if (/*it.isEmptyRow() ||*/ !it.isParagraphBeginning() || pos > 0)
	    return null;
	switch(it.getNode().getType())
	{
	case LIST_ITEM:
	    return Sounds.LIST_ITEM;
	case SECTION:
	    return Sounds.DOC_SECTION;
	default:
	    return Sounds.PARAGRAPH;
	}
    }
}
