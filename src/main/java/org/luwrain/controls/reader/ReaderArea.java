/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.controls.reader;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.util.WordIterator;
import org.luwrain.reader.*;
import org.luwrain.reader.view.*;
import org.luwrain.reader.view.Iterator;


// Transition tries to iteratate over inner objects
// while announcing tries to announce greater objects
public class ReaderArea implements Area, ListenableArea, ClipboardTranslator.Provider
{
    public enum State {LOADING, READY};

    public interface ClickHandler
    {
	boolean onReaderClick(ReaderArea area, Run run);
    }

    public interface Announcement
    {
	void announce(Iterator it, boolean brief);
    }

    public interface Transition
    {
	public enum Type{
	    NEXT, PREV,
	    STRICT_NEXT, STRICT_PREV,
	    NEXT_SECTION, PREV_SECTION,
	    NEXT_SECTION_SAME_LEVEL, PREV_SECTION_SAME_LEVEL,
	    NEXT_PARAGRAPH, PREV_PARAGRAPH,
	};

	boolean transition(Type type, Iterator it);
    }

    static public final class Params
    {
	public ControlContext context = null;
	public String name = "";
	public ClickHandler clickHandler = null;
	public Announcement announcement = null;
	public Transition transition = new DefaultTransition();
	public Document doc = null;
	public int width = 100;
    }

    protected final ControlContext context;
    protected final RegionPoint regionPoint = new RegionPoint();
    protected final ClipboardTranslator clipboardTranslator = new ClipboardTranslator(this, regionPoint, EnumSet.noneOf(ClipboardTranslator.Flags.class));
    protected final String name;
    protected final Announcement announcement;
    protected final Transition transition;
    protected ClickHandler clickHandler = null;

    protected Document document = null;
    protected View view = null;
    protected Layout layout = null;
    protected org.luwrain.reader.view.Iterator iterator = null;
    protected int hotPointX = 0;

    public ReaderArea(Params params)
    {
	NullCheck.notNull(params, "params");
	NullCheck.notNull(params.context, "params.context");
	NullCheck.notNull(params.transition, "params.transition");
	NullCheck.notNull(params.name, "params.name");
	this.context = params.context;
	if (params.announcement != null)
	    this.announcement = params.announcement; else
	    this.announcement = new DefaultAnnouncement(params.context, (Strings)params.context.getI18n().getStrings(Strings.NAME));
	this.transition = params.transition;
	this.clickHandler = params.clickHandler;
	if (params.doc != null)
	{
	    if (params.width < 0)
		throw new IllegalArgumentException("width (" + params.width + ") may not be negative");
	    setDocument(params.doc, params.width);
	}
	this.name = params.name;
    }

    public ReaderArea(ControlContext context, Announcement announcement, Document document, int width)
    {
	NullCheck.notNull(context, "context");
	NullCheck.notNull(announcement, "announcement");
	this.context = context;
	this.announcement = announcement;
	if (document != null)
	{
	    if (width < 0)
		throw new IllegalArgumentException("width (" + width + ") may not be negative");
	    setDocument(document, width);
	}
	this.transition = new DefaultTransition();
	this.name = "";
    }

    public ReaderArea(ControlContext context, Announcement announcement)
    {
	this(context, announcement, null, 0);
    }

    public void setDocument(Document document, int width)
    {
	NullCheck.notNull(document, "document");
	if (width < 0)
	    throw new IllegalArgumentException("width (" + width + ") may not be negative");
	this.document = document;
	this.view = new View(document, width);
	this.layout = view.createLayout();
	int defaultIndex = -1;
	if (!document.getProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY).isEmpty())
	    try {
		defaultIndex = Integer.parseInt(document.getProperty("defaultiteratorindex"));
	    }
	    catch (NumberFormatException e)
	    {
	    }
	if (defaultIndex >= 0)
	{
	    try {
		iterator = view.getIterator(defaultIndex);
	    }
	    catch(IllegalArgumentException e)
	    {
		iterator = view.getIterator();
	    }
	} else
	    iterator = view.getIterator();
	hotPointX = 0;
	context.onAreaNewContent(this);
	context.onAreaNewHotPoint(this);
    }

    public boolean hasDocument()
    {
	return document != null && iterator != null;
    }

    public boolean isEmpty()
    {
	return !hasDocument() || iterator.noContent();
    }

    public Document getDocument()
    {
	return document;
    }

    public String getDocTitle()
    {
	if (!hasDocument())
	    return "";
	final String res = getDocument().getTitle();
	return res != null?res:"";
    }

    public String getDocUrl()
    {
	if (!hasDocument())
	    return "";
	final URL url = document.getUrl();
	return url != null?url.toString():"";
    }

    public String getDocUniRef()
    {
	final String addr = getDocUrl();
	if (addr.isEmpty())
	    return "";
	return UniRefUtils.makeUniRef(UniRefUtils.URL, addr);
    }

    public Run getCurrentRun()
    {
	if (isEmpty())
	    return null;
	return iterator.getRunUnderPos(hotPointX);
    }

    public boolean findRun(Run run)
    {
	NullCheck.notNull(run, "run");
	if (isEmpty())
	    return false;
	final Iterator newIt = view.getIterator();
	while(newIt.canMoveNext() && !newIt.hasRunOnRow(run))
	    newIt.moveNext();
	if (!newIt.hasRunOnRow(run))
	    return false;
	final int pos = newIt.runBeginsAt(run);
	if (pos < 0)
	    return false;
	iterator = newIt;
	hotPointX = pos;
	context.onAreaNewHotPoint(this);
	return true;
    }

    public int getCurrentRowIndex()
    {
	return !isEmpty()?iterator.getIndex():-1;
    }

    public boolean setCurrentRowIndex(int index)
    {
	if (isEmpty())
	    return false;
	final Iterator newIt;
	try {
	    newIt = view.getIterator(index);
	}
	catch(IllegalArgumentException e)
	{
	    return false;
	}
	this.iterator = newIt;
	hotPointX = 0;
	context.onAreaNewHotPoint(this);
	return true;
    }

    public String[] getHtmlIds()
    {
	if (isEmpty()/* || iterator.isEmptyRow()*/)
	    return new String[0];
	final LinkedList<String> res = new LinkedList<String>();
	final Run run = iterator.getRunUnderPos(hotPointX);
	if (run == null)
	    return new String[0];
	ExtraInfo info = run.extraInfo();
	while (info != null)
	{
	    if (info.attrs.containsKey("id"))
	    {
		final String value = info.attrs.get("id");
		if (!value.isEmpty())
		    res.add(value);
	    }
	    info = info.parent;
	}
	return res.toArray(new String[res.size()]);
    }

    public boolean rebuildView(int width)
    {
	if (isEmpty())
	    return false;
	final Run currentRun = getCurrentRun();
	view = new View(document, width);
	layout = view.createLayout();
	if (currentRun != null)
	    findRun(currentRun);
	hotPointX = Math.min(hotPointX, iterator.getText().length());
	context.onAreaNewContent(this);
	context.onAreaNewHotPoint(this);
	return true;
    }

    @Override public int getLineCount()
    {
	return !isEmpty()?layout.getLineCount() + 1:1;
    }

    @Override public String getLine(int index)
    {
	if (index < 0)
	    throw new IllegalArgumentException("index (" + index + ") may not be negative");
	if (isEmpty())
	    return index == 0?noContentStr():"";
	return index < layout.getLineCount()?layout.getLine(index):"";
    }

    @Override public boolean onInputEvent(KeyboardEvent event) 
    {
	NullCheck.notNull(event, "event");
	if (!event.isSpecial() && !event.isModified())
	    switch(KeyboardEvent.getKeyboardLayout().getAsciiOfButton(event.getChar()))
	    {
	    case ' ':
		return onFindNextHref();
	    case '[':
		return onTransition(event, Transition.Type.PREV_PARAGRAPH, false, Hint.NO_LINES_ABOVE);
	    case ']':
		return onTransition(event, Transition.Type.NEXT_PARAGRAPH, false, Hint.NO_LINES_BELOW);
	    case '.':
		return onNextSentence(event);
	    }
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case ENTER:
		return onClick();
	    case ARROW_DOWN:
		return onTransition(event, Transition.Type.NEXT, false, Hint.NO_LINES_BELOW);
	    case ARROW_UP:
		return onTransition(event, Transition.Type.PREV, false, Hint.NO_LINES_ABOVE);
	    case ALTERNATIVE_ARROW_DOWN:
		return onTransition(event, Transition.Type.STRICT_NEXT, false, Hint.NO_LINES_BELOW);
	    case ALTERNATIVE_ARROW_UP:
		return onTransition(event, Transition.Type.STRICT_PREV, false, Hint.NO_LINES_BELOW);
	    case ARROW_LEFT:
		return onMoveLeft(event);
	    case ARROW_RIGHT:
		return onMoveRight(event);
	    case ALTERNATIVE_ARROW_LEFT:
		return onAltLeft(event);
	    case ALTERNATIVE_ARROW_RIGHT:
		return onAltRight(event);
	    case HOME:
		return onHome(event);
	    case END:
		return onEnd(event);
	    case ALTERNATIVE_HOME:
		return onAltHome(event);
	    case ALTERNATIVE_END:
		return onAltEnd(event);
	    case PAGE_UP:
		return onTransition(event, Transition.Type.PREV_SECTION, false, Hint.NO_LINES_ABOVE);
	    case PAGE_DOWN:
		return onTransition(event, Transition.Type.NEXT_SECTION, false, Hint.NO_LINES_BELOW);
	    case ALTERNATIVE_PAGE_UP:
		return onTransition(event, Transition.Type.PREV_SECTION_SAME_LEVEL, false, Hint.NO_LINES_ABOVE);
	    case ALTERNATIVE_PAGE_DOWN:
		return onTransition(event, Transition.Type.NEXT_SECTION_SAME_LEVEL, false, Hint.NO_LINES_BELOW);
	    }
	return false;
    }

    @Override public boolean onSystemEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.getType() != EnvironmentEvent.Type.REGULAR)
	    return false;
	switch(event.getCode())
	{
	case MOVE_HOT_POINT:
	    if (event instanceof MoveHotPointEvent)
		return onMoveHotPoint((MoveHotPointEvent)event);
	    return false;
	default:
	    return clipboardTranslator.onSystemEvent(event, getHotPointX(), getHotPointY());
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[0];
    }

    @Override public boolean onAreaQuery(AreaQuery query)
    {
	NullCheck.notNull(query, "query");
	switch(query.getQueryCode())
	{
	case AreaQuery.UNIREF_AREA:
	    {
		final String title = getDocTitle();
		final String uniRef = getDocUniRef();
		if (uniRef.isEmpty())
		    return false;
		if (title.isEmpty())
		    ((UniRefAreaQuery)query).answer(uniRef); else
		    ((UniRefAreaQuery)query).answer(UniRefUtils.makeAlias(title, uniRef));
		return true;
	    }
	case AreaQuery.URL_AREA:
	    {
		final String url = getDocUrl();
		if (url.isEmpty())
		    return false;
		((UrlAreaQuery)query).answer(url);
		return true;
	    }
	case AreaQuery.UNIREF_HOT_POINT:
	    {
		final Run run = getCurrentRun();
		if (isEmpty() || run == null)
		    return false;
		final String res = getCurrentRun().href();
		if (res == null || res.isEmpty())
		    return false;
		((UniRefHotPointQuery)query).answer("url:" + res);
		return true;
	    }
	default:
	    return false;
	}
    }

    @Override public int getHotPointX()
    {
	if (isEmpty())
	    return 0;
	return iterator.getX() + hotPointX;
    }

    @Override public int getHotPointY()
    {
	if (isEmpty())
	    return 0;
	return iterator.getY();
    }

    @Override public String getAreaName()
    {
	if (name.isEmpty())
	    return getDocTitle();
	return name;
    }

    @Override public boolean onClipboardCopyAll()
    {
	if (isEmpty())
	    return false;
	final TextExtractorWhole extractor = new TextExtractorWhole(context.getScreenWidth());
	extractor.onNode(document.getRoot());
	context.getClipboard().set(extractor.getLines());
	return true;
    }

    @Override public boolean onClipboardCopy(int fromX, int fromY, int toX, int toY, boolean withDeleting)
    {
	if (isEmpty() || withDeleting)
	    return false;
	Run run1 = null;
	Run run2 = null;
	final Iterator it = new org.luwrain.reader.view.Iterator(view);
	if (it.noContent())
	    return false;
	do {
	    if (it.coversPos(fromX, fromY))
	    {
		run1 = it.getRunUnderPos(fromX - it.getX());
		if (run1 == null)
		    throw new RuntimeException("The iterator is unable to provide a run under the covered point");
	    }
	    if (it.coversPos(toX, toY))
	    {
		run2 = it.getRunUnderPos(toX - it.getX());
		if (run1 == null)
		    throw new RuntimeException("The iterator is unable to provide a run under the covered point");
	    }
    	} while(it.moveNext());
	if (run1 == null || run2 == null)
	    return false;
	context.getClipboard().set(new String[]{
		run1.text(),
		run2.text(),
	    });
	return true;
    }

    @Override public boolean onDeleteRegion(int fromX, int fromY, int toX, int toY)
    {
	return false;
    }

    @Override public ListenableArea.ListeningInfo onListeningStart()
    {
	final Jump jump = Jump.nextSentence(iterator, hotPointX);
	if (jump.isEmpty())
	    return new ListenableArea.ListeningInfo();
	return new ListeningInfo(textUntil(jump.it, jump.pos), jump.it, jump.pos);
    }

    @Override public void onListeningFinish(ListenableArea.ListeningInfo listeningInfo)
    {
	NullCheck.notNull(listeningInfo, "listeningInfo");
	if (!(listeningInfo instanceof ReaderArea.ListeningInfo))
	    return;
	final ReaderArea.ListeningInfo info = (ReaderArea.ListeningInfo)listeningInfo;
	this.iterator = info.it;
	this.hotPointX = info.pos;
	context.onAreaNewHotPoint(this);
    }

    protected boolean onMoveHotPoint(MoveHotPointEvent event)
    {
	NullCheck.notNull(event, "event");
	if (isEmpty())
	    return false;
	final Iterator it2 = view.getIterator();
	final int x = event.getNewHotPointX();
	final int y = event.getNewHotPointY();
	if (x < 0 || y < 0)
	    return false;
	Iterator nearest = null;
	while (it2.canMoveNext() && !it2.coversPos(x, y))
	{
	    if (it2.getY() == y)
		nearest = (Iterator)it2.clone();
	    it2.moveNext();
	}
	if (it2.coversPos(x, y) &&
	    x >= it2.getX())
	{
	    iterator = it2;
	    hotPointX = x - iterator.getX();
	    context.onAreaNewHotPoint(this);
	    return true;
	}
	if (event.precisely())
	    return false;
	if (nearest != null)
	{
	    iterator = nearest;
	    hotPointX = 0;
	    return true;
	}
	return false;
    }

    protected boolean onClick()
    {
	if (clickHandler == null)
	    return false;
	final Run run = getCurrentRun();
	if (run == null)
	    return false;
	return clickHandler.onReaderClick(this, run);
    }

    protected boolean onTransition(KeyboardEvent event, Transition.Type type, boolean briefAnnouncement, Hint hintFailed)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(type, "type");
	NullCheck.notNull(hintFailed, "hintFailed");
	if (noContentCheck())
	    return true;
	if (transition.transition(type, iterator))
	    onNewRow( briefAnnouncement); else
	    context.setEventResponse(DefaultEventResponse.hint(hintFailed));
	return true;
    }

    protected boolean onAltEnd(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveEnd();
	onNewRow( false);
	return true;
    }

    protected boolean onAltHome(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveBeginning();
	onNewRow( false);
	return true;
    }

    protected boolean onNextSentence(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final SentenceIterator sentIt = new SentenceIterator(this.iterator, hotPointX);
	StringBuilder b = new StringBuilder();
	if (!sentIt.forward(b, " "))
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_LINES_BELOW));
	    return true;
	}
	//If we are at the row end
	if (sentIt.atRowEnd() && !sentIt.forward(b, " "))
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_LINES_BELOW));
	    return true;
	}

		
	this.iterator = sentIt.getIterator();
	this.hotPointX = sentIt.getPos();
	//Making one more iteration to get the next of the next sentence
	b = new StringBuilder();
	sentIt.forward(b, " ");
	final String text = new String(b).trim();
	context.onAreaNewHotPoint(this);
	if (!text.isEmpty())
	    context.setEventResponse(DefaultEventResponse.text(text)); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	return true;
    }

    protected boolean onMoveLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final String text = iterator.getText();
	hotPointX = Math.min(hotPointX, text.length());
	if (hotPointX > 0)
	{
	    --hotPointX;
	    context.setEventResponse(DefaultEventResponse.letter(text.charAt(hotPointX)));
	    context.onAreaNewHotPoint(this);
	    return true;
	}
	if (!iterator.canMovePrev())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.BEGIN_OF_TEXT));
	    return true;
	}
	iterator.movePrev();
	final String prevRowText = iterator.getText();
	hotPointX = prevRowText.length();
	context.setEventResponse(DefaultEventResponse.hint(Hint.LINE_BOUND));
	return true;
    }

    protected boolean onMoveRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final String text = iterator.getText();
	if (hotPointX < text.length())
	{
	    ++hotPointX;
	    if (hotPointX < text.length())
		context.setEventResponse(DefaultEventResponse.letter(text.charAt(hotPointX))); else
		context.setEventResponse(DefaultEventResponse.hint(Hint.LINE_BOUND));
	    context.onAreaNewHotPoint(this);
	    return true;
	}
	if (!iterator.canMoveNext())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_TEXT));
	    return true;
	}
	iterator.moveNext();
	final String nextRowText = iterator.getText();
	hotPointX = 0;
	if (nextRowText.isEmpty())
	    context.setEventResponse(DefaultEventResponse.hint(Hint.LINE_BOUND)); else
	    context.setEventResponse(DefaultEventResponse.letter(nextRowText.charAt(0)));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onAltLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final String text = iterator.getText();
	final WordIterator it = new WordIterator(text, hotPointX);
	if (!it.stepBackward())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.BEGIN_OF_LINE));
	    return true;
	}
	hotPointX = it.pos();
	context.setEventResponse(DefaultEventResponse.text(it.announce()));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onAltRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final String text = iterator.getText();
	final WordIterator it = new WordIterator(text, hotPointX);
	if (!it.stepForward())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
	    return true;
	}
	hotPointX = it.pos();
	if (it.announce().length() > 0)
	    context.setEventResponse(DefaultEventResponse.text(it.announce())); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onHome(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final String text = iterator.getText();
	hotPointX = 0;
	if (!text.isEmpty())
	    context.setEventResponse(DefaultEventResponse.letter(text.charAt(0))); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onEnd(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final String text = iterator.getText();
	hotPointX = text.length();
	context.setEventResponse(DefaultEventResponse.hint(Hint.LINE_BOUND));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onFindNextHref()
    {
	if (noContentCheck())
	    return true;
	final Run currentRun = iterator.getRunUnderPos(hotPointX);
	if (currentRun != null)
	{
	    //Trying to find the run with href on the current row
	    final Run[] runs = iterator.getRuns();
	    boolean skipping = true;
	    for(Run r: runs)
	    {
		if (r == currentRun)
		{
		    skipping = false;
		    continue;
		}
		if (skipping)
		    continue;
		if (r.href() != null && !r.href().trim().isEmpty())
		{
		    hotPointX = iterator.runBeginsAt(r);
		    context.say(r.text());
		    context.onAreaNewHotPoint(this);
		    return true;    
		}
	    }
	}
	if (iterator.getIndex() + 1 >= iterator.getCount())
	    return false;
	if (!iterator.searchForward((node,para,row)->{
		    final Run[] runs = row.getRuns();
		    for(Run r: runs)
			if (r.href() != null && !r.href().trim().isEmpty())
			    return true;
		    return false;
		}, iterator.getIndex() + 1))
	    return false;
	final Run[] runs = iterator.getRuns();
	int k = 0;
	while (k < runs.length && (runs[k].href() == null || runs[k].href().trim().isEmpty()))
	    ++k;
	if (k >= runs.length)//Should never happen
	    return false;
	hotPointX = iterator.runBeginsAt(runs[k]);
	context.say(runs[k].text());
	context.onAreaNewHotPoint(this);
	return true;    
    }

    protected void onNewRow(boolean briefAnnouncement)
    {
	hotPointX = 0;
	context.onAreaNewHotPoint(this);
	announcement.announce(iterator, briefAnnouncement);
    }

    protected String noContentStr()
    {
	return context.getStaticStr("DocumentNoContent");
    }

    private boolean noContentCheck()
    {
	if (isEmpty())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_CONTENT, noContentStr()));
	    return true;
	}
	return false;
    }

    static protected int findEndOfSentence(String text, int startFrom)
    {
	NullCheck.notNull(text, "text");
	for(int i = startFrom;i < text.length();++i)
	    if (charOfSentenceEnd(text.charAt(i)))
		return i;
	return -1;
    }

    static protected boolean charOfSentenceEnd(char ch)
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

    static protected class ListeningInfo extends ListenableArea.ListeningInfo
    {
	final Iterator it;
	final int pos;
	ListeningInfo(String text, Iterator it, int pos)
	{
	    super(text);
	    NullCheck.notNull(it, "it");
	    this.it = it;
	    this.pos = pos;
	}
    }

    //Method does not check if current position is prior to the required position
    protected String textUntil(Iterator itTo, int posTo)
    {
	NullCheck.notNull(itTo, "itTo");
	final Iterator tmpIt = (Iterator)iterator.clone();
	if (tmpIt.equals(itTo))
	    return tmpIt.getText().substring(hotPointX, posTo);
	final StringBuilder b = new StringBuilder();
	b.append(tmpIt.getText().substring(hotPointX));
	while(tmpIt.moveNext() && !tmpIt.equals(itTo))
	    b.append(" " + tmpIt.getText());
	if (tmpIt.equals(itTo))
	    b.append(" " + tmpIt.getText().substring(0, posTo));
	return new String(b);
    }
}
