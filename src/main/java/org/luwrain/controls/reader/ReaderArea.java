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
public class ReaderArea implements Area, ClipboardTranslator.Provider
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
	public enum Type{NEXT, PREV};

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
    private String areaName = null;//FIXME:No corresponding constructor;
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
	    switch(event.getChar())
	    {
	    case ' ':
		return onFindNextHref();
	    case '[':
		return onLeftSquareBracket(event);
	    case ']':
		return onNextParagraph(event);
	    case '.':
		return onNextSentence(event);
	    }
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case ENTER:
		return onClick();
	    case ARROW_DOWN:
		return onMoveDown(event, false);
	    case ARROW_UP:
		return onMoveUp(event, false);
	    case ALTERNATIVE_ARROW_DOWN:
		return onMoveDown(event, true);
	    case ALTERNATIVE_ARROW_UP:
		return onMoveUp(event, true);
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
		return onFindPrevSection( false);
	    case PAGE_DOWN:
		return onFindNextSection(false);
	    case ALTERNATIVE_PAGE_UP:
		return onFindPrevSection(true);
	    case ALTERNATIVE_PAGE_DOWN:
		return onFindNextSection(true);
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
	case LISTENING_FINISHED:
	    return onListeningFinishedEvent((ListeningFinishedEvent)event);
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
	case AreaQuery.BEGIN_LISTENING:
	    return onBeginListeningQuery((BeginListeningQuery)query);
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
	if (areaName != null)
	    return areaName;
	if (document != null)
	{
	    final String title = document.getTitle();
	    return title != null?title:"";
	}
	return "";
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

protected boolean onBeginListeningQuery(BeginListeningQuery query)
    {
	NullCheck.notNull(query, "query");
	final Jump jump = Jump.nextSentence(iterator, hotPointX);
	if (jump.isEmpty())
	    return false;
	query.answer(new BeginListeningQuery.Answer(textUntil(jump.it, jump.pos), new ListeningInfo(jump.it, jump.pos)));
return true;
    }

    protected boolean onListeningFinishedEvent(ListeningFinishedEvent event)
    {
	NullCheck.notNull(event, "event");
	if (!(event.getExtraInfo() instanceof ListeningInfo))
	    return false;
	final ListeningInfo info = (ListeningInfo)event.getExtraInfo();
	iterator = info.it;
	hotPointX = info.pos;
	context.onAreaNewHotPoint(this);
	return true;
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

    protected boolean onMoveDown(KeyboardEvent event, boolean quickNav)
    {
	if (noContentCheck())
	    return true;
	if (transition.transition(Transition.Type.NEXT, iterator))
	    onNewHotPointY( quickNav); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_LINES_BELOW));
	return true;
    }

    protected boolean onMoveUp(KeyboardEvent event, boolean quickNav)
    {
	if (noContentCheck())
	    return true;
	if (transition.transition(Transition.Type.PREV, iterator))
	    onNewHotPointY( quickNav); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_LINES_ABOVE));
	return true;
    }

    protected boolean onAltEnd(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveEnd();
	onNewHotPointY( false);
	return true;
    }

    protected boolean onAltHome(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveBeginning();
	onNewHotPointY( false);
	return true;
    }

    protected boolean onFindNextSection(boolean sameLevel)
    {
	if (noContentCheck())
	    return true;
	final Node currentNode = iterator.getNode();
	if (currentNode == null)//Actually very strange, should never happen
	    return false;
	final int currentSectLevel; 
	if (currentNode instanceof Section)
	{
	    final Section sect = (Section)currentNode;
	    currentSectLevel = sect.getSectionLevel();
	} else
	    currentSectLevel = -1;
	if (!sameLevel || currentSectLevel < 0)
	{
	    if (!iterator.searchForward((node,para,row)->{
			if (node == currentNode)
			    return false;
			return node.getType() == Node.Type.SECTION;
		    }, iterator.getIndex()))
		return false;
	} else
	{
	    if (!iterator.searchForward((node,para,row)->{
			if (node == currentNode)
			    return false;
			if (node.getType() != Node.Type.SECTION)
			    return false;
			final Section sect = (Section)node;
			return sect.getSectionLevel() <= currentSectLevel;
		    }, iterator.getIndex()))
		return false;
	}
	onNewHotPointY(false);
	return true;
    }

    protected boolean onFindPrevSection(boolean sameLevel)
    {
		if (noContentCheck())
	    return true;
	final Node currentNode = iterator.getNode();
	if (currentNode == null)//Actually very strange, should never happen
	    return false;
	final int currentSectLevel; 
	if (currentNode instanceof Section)
	{
	    final Section sect = (Section)currentNode;
	    currentSectLevel = sect.getSectionLevel();
	} else
	    currentSectLevel = -1;
	if (!sameLevel || currentSectLevel < 0)
	{
	    if (!iterator.searchBackward((node,para,row)->{
			if (node == currentNode || row.getRelNum() > 0)
			    return false;
			return node.getType() == Node.Type.SECTION;
		    }, iterator.getIndex()))
		return false;
	} else
	{
	    if (!iterator.searchBackward((node,para,row)->{
			if (node == currentNode || row.getRelNum() > 0)
			    return false;
			if (node.getType() != Node.Type.SECTION)
			    return false;
			final Section sect = (Section)node;
			return sect.getSectionLevel() <= currentSectLevel;
		    }, iterator.getIndex()))
		return false;
	}
	onNewHotPointY(false);
	return true;
    }

protected boolean onNextParagraph(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final Jump jump = Jump.nextParagraph(iterator, hotPointX);
	NullCheck.notNull(jump, "jump");
	jump.announce(context);
	if (!jump.isEmpty())
	{
	    iterator = jump.it;
	    hotPointX = jump.pos;
	    context.onAreaNewHotPoint(this);
	}
	return true;
    }

protected boolean onNextSentence(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final Jump jump = Jump.nextSentence(iterator, hotPointX);
	NullCheck.notNull(jump, "jump");
	jump.announce(context);
	if (!jump.isEmpty())
	{
	    iterator = jump.it;
	    hotPointX = jump.pos;
	    context.onAreaNewHotPoint(this);
	}
	return true;
    }

    protected boolean onLeftSquareBracket(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (!iterator.movePrev())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_LINES_ABOVE));
	    return true;
	}
	while(!iterator.isParagraphBeginning() && iterator.movePrev());
	onNewHotPointY( false);
	return true;
    }

    protected boolean onMoveLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	//	if (!iterator.isEmptyRow())
	{
	    final String text = iterator.getText();
	    hotPointX = Math.min(hotPointX, text.length());
	if (hotPointX > 0)
	{
	    --hotPointX;
	    context.sayLetter(text.charAt(hotPointX));
	    context.onAreaNewHotPoint(this);
	    return true;
	}
	}
	if (!iterator.canMovePrev())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.BEGIN_OF_TEXT));
	    return true;
	}
	iterator.movePrev();
	final String prevRowText = iterator.getText();
	hotPointX = prevRowText.length();
	context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
	return true;
    }

    protected boolean onMoveRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	//	if (!iterator.isEmptyRow())
	{
	final String text = iterator.getText();
	if (hotPointX < text.length())
	{
	    ++hotPointX;
	    if (hotPointX < text.length())
		context.sayLetter(text.charAt(hotPointX)); else
		context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
	    context.onAreaNewHotPoint(this);
	    return true;
	}
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
	    context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE)); else
	    context.sayLetter(nextRowText.charAt(0));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onAltLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	/*
	if (iterator.isEmptyRow())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	    return true;
	}
	*/
	final String text = iterator.getText();
	final WordIterator it = new WordIterator(text, hotPointX);
	if (!it.stepBackward())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.BEGIN_OF_LINE));
	    return true;
	}
	hotPointX = it.pos();
	context.say(it.announce());
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onAltRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	/*
	if (iterator.isEmptyRow())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE);
	    return true;
	}
	*/
	final String text = iterator.getText();
	final WordIterator it = new WordIterator(text, hotPointX);
	if (!it.stepForward())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
	    return true;
	}
	hotPointX = it.pos();
	if (it.announce().length() > 0)
	    context.say(it.announce()); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onHome(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	/*
	if (iterator.isEmptyRow())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE);
	    return true;
	}
	*/
	final String text = iterator.getText();
	hotPointX = 0;
	if (!text.isEmpty())
	    context.sayLetter(text.charAt(0)); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onEnd(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	/*
	if (iterator.isEmptyRow())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE);
	    return true;
	}
	*/
	final String text = iterator.getText();
	hotPointX = text.length();
	context.setEventResponse(DefaultEventResponse.hint(Hint.END_OF_LINE));
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

    protected void onNewHotPointY(boolean briefAnnouncement, boolean alwaysSpeakTitleText)
    {
	onNewHotPointY(briefAnnouncement);
    }

    protected void onNewHotPointY(boolean briefAnnouncement)
    {
	hotPointX = 0;
	/*
	if (iterator.isEmptyRow())
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE)); else
	*/
	    announceRow(iterator, briefAnnouncement);
	context.onAreaNewHotPoint(this);
    }

    protected void announceRow(Iterator it, boolean briefAnnouncement)
    {
	NullCheck.notNull(it, "it");
	announcement.announce(it, briefAnnouncement);
    }

    protected 	void announceFragment(Iterator itFrom, Iterator itTo)
    {
	final StringBuilder b = new StringBuilder();
	Iterator it = itFrom;
	while(!it.equals(itTo))
	{
	    //	    if (!it.isEmptyRow())
		b.append(it.getText());
	    if (!it.moveNext())
		break;
	}
	context.say(b.toString());
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

    static protected class ListeningInfo
    {
	final Iterator it;
	final int pos;

	ListeningInfo(Iterator it, int pos)
	{
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
