// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

/**
 * The default implementation of {@link ReaderArea.Transition}.
 *
 * <p>This implementation provides conventional reader navigation: line-by-line
 * movement, strict row movement, movement between paragraphs and movement
 * between section headings.</p>
 */
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
	return it.searchForward((node,para,row)-> para != null, it.getIndex() + 1);
    }

    boolean onPrev(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	if (it.getIndex() == 0)
	    return false;
	return it.searchBackward((node,para,row)-> para != null, it.getIndex() - 1);
    }

    protected boolean onNextSection(Iterator it, boolean sameLevel)
    {
	requireNonNull(it, "it can't be null");
	final Container currentNode = it.getContainer();
	if (currentNode == null)
	    return false;
	final int currentSectLevel = sectionLevel(currentNode);
	if (!sameLevel || currentSectLevel < 0)
	{
	    return it.searchForward((node,para,row)-> isAnotherHeading(node, currentNode), it.getIndex());
	}
	return it.searchForward((node,para,row)-> isHeadingOnLevelOrAbove(node, currentNode, currentSectLevel), it.getIndex());
    }

    protected boolean onPrevSection(Iterator it, boolean sameLevel)
    {
	requireNonNull(it, "it can't be null");
	final Container currentNode = it.getContainer();
	if (currentNode == null)
	    return false;
	final int currentSectLevel = sectionLevel(currentNode);
	if (!sameLevel || currentSectLevel < 0)
	{
	    return it.searchBackward((node,para,row)-> isAnotherHeadingOnRowStart(node, currentNode, row), it.getIndex());
	}
	return it.searchBackward((node,para,row)-> isHeadingOnLevelOrAboveOnRowStart(node, currentNode, row, currentSectLevel), it.getIndex());
    }

    protected boolean onNextParagraph(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	return it.searchForward((node,para,row)-> para != null && row.getRelNum() == 0, it.getIndex() + 1);
    }

    protected boolean onPrevParagraph(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	if (it.getIndex() == 0)
	    return false;
	return it.searchBackward((node,para,row)-> para != null && row.getRelNum() == 0, it.getIndex() - 1);
    }

    static private int sectionLevel(Container node)
    {
	requireNonNull(node, "node can't be null");
	if (node instanceof Heading)
	{
	    final Heading h = (Heading)node;
	    return h.getLevel();
	}
	return -1;
    }

    static private boolean isAnotherHeading(Node node, Container currentNode)
    {
	requireNonNull(node, "node can't be null");
	requireNonNull(currentNode, "currentNode can't be null");
	return node != currentNode && node instanceof Heading;
    }

    static private boolean isAnotherHeadingOnRowStart(Node node, Container currentNode, org.luwrain.io.bookdoc.view.Row row)
    {
	requireNonNull(node, "node can't be null");
	requireNonNull(currentNode, "currentNode can't be null");
	requireNonNull(row, "row can't be null");
	return isAnotherHeading(node, currentNode) && row.getRelNum() == 0;
    }

    static private boolean isHeadingOnLevelOrAbove(Node node, Container currentNode, int currentSectLevel)
    {
	requireNonNull(node, "node can't be null");
	requireNonNull(currentNode, "currentNode can't be null");
	if (!(node instanceof Heading))
	    return false;
	if (node == currentNode)
	    return false;
	final Heading h = (Heading)node;
	return h.getLevel() <= currentSectLevel;
    }

    static private boolean isHeadingOnLevelOrAboveOnRowStart(Node node, Container currentNode, org.luwrain.io.bookdoc.view.Row row, int currentSectLevel)
    {
	requireNonNull(node, "node can't be null");
	requireNonNull(currentNode, "currentNode can't be null");
	requireNonNull(row, "row can't be null");
	return isHeadingOnLevelOrAbove(node, currentNode, currentSectLevel) && row.getRelNum() == 0;
    }
}
