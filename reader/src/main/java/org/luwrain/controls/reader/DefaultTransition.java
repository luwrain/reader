// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

public class DefaultTransition implements ReaderArea.Transition
{
    @Override public boolean transition(Type type, Iterator it)
    {
	requireNonNull(type, "type can't be null");
	requireNonNull(it, "it can't be null");
	switch(type)
	{
	case NEXT:
	    return onNext(it);
	case PREV:
	    return onPrev(it);
	case STRICT_NEXT:
	    return it.moveNext();
	case STRICT_PREV:
	    return it.movePrev();
	case NEXT_SECTION:
	case NEXT_SECTION_SAME_LEVEL:
	    return onNextSection(it, type == Type.NEXT_SECTION_SAME_LEVEL);
	case PREV_SECTION:
	case PREV_SECTION_SAME_LEVEL:
	    return onPrevSection(it, type == Type.PREV_SECTION_SAME_LEVEL);
	case NEXT_PARAGRAPH:
	    return onNextParagraph(it);
	case PREV_PARAGRAPH:
	    return onPrevParagraph(it);
	default:
	    return false;
	}
    }

    boolean onNext(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	return it.searchForward((node,para,row)->{
		return para != null;
	    }, it.getIndex() + 1);
    }

    boolean onPrev(Iterator it)
    {
	if (it.getIndex() == 0)
	    return false;
	requireNonNull(it, "it can't be null");
	return it.searchBackward((node,para,row)->{
		return para != null;
	    }, it.getIndex() - 1);
    }

    protected boolean onNextSection(Iterator it, boolean sameLevel)
    {
	final Node currentNode = it.getNode();
	if (currentNode == null)
	    return false;
	final int currentSectLevel;
	if (currentNode instanceof Heading)
	{
	    final Heading h = (Heading)currentNode;
	    currentSectLevel = h.getLevel();
	} else
	    currentSectLevel = -1;
	if (!sameLevel || currentSectLevel < 0)
	{
	    if (!it.searchForward((node,para,row)->{
			if (node == currentNode)
			    return false;
			return node instanceof Heading;
		    }, it.getIndex()))
		return false;
	} else
	{
	    if (!it.searchForward((node,para,row)->{
			if (node == currentNode)
			    return false;
			if (!(node instanceof Heading))
			    return false;
			final Heading h = (Heading)node;
			return h.getLevel() <= currentSectLevel;
		    }, it.getIndex()))
		return false;
	}
	return true;
    }

    protected boolean onPrevSection(Iterator it, boolean sameLevel)
    {
	requireNonNull(it, "it can't be null");
	final Node currentNode = it.getNode();
	if (currentNode == null)
	    return false;
	final int currentSectLevel;
	if (currentNode instanceof Heading)
	{
	    final Heading h = (Heading)currentNode;
	    currentSectLevel = h.getLevel();
	} else
	    currentSectLevel = -1;
	if (!sameLevel || currentSectLevel < 0)
	{
	    if (!it.searchBackward((node,para,row)->{
			if (node == currentNode || row.getRelNum() > 0)
			    return false;
			return node instanceof Heading;
		    }, it.getIndex()))
		return false;
	} else
	{
	    if (!it.searchBackward((node,para,row)->{
			if (node == currentNode || row.getRelNum() > 0)
			    return false;
			if (!(node instanceof Heading))
			    return false;
			final Heading h = (Heading)node;
			return h.getLevel() <= currentSectLevel;
		    }, it.getIndex()))
		return false;
	}
	return true;
    }

    protected boolean onNextParagraph(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	return it.searchForward((node,para,row)->{
		return row.getRelNum() == 0;
	    }, it.getIndex() + 1);
    }

    protected boolean onPrevParagraph(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	if (it.getIndex() == 0)
	    return false;
	return it.searchBackward((node,para,row)->{
		return row.getRelNum() == 0;
	    }, it.getIndex() - 1);
    }
}
