// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.util.WordIterator;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

import org.luwrain.io.bookdoc.view.Iterator;

import static java.util.Objects.*;

/**
 * The reader area.
 *
 * <p>The area presents a {@link Doc} as a sequence of accessible rows using
 * the view iterator model. It supports reading navigation, hyperlink lookup,
 * text extraction and listening to sentence-sized fragments.</p>
 */
public class ReaderArea implements Area, ListenableArea, ClipboardTranslator.Provider
{
    /** The state of the reader area. */
    public enum State {LOADING, READY};

    /** Handler for clicks on runs that have a link. */
    public interface ClickHandler
    {
	boolean onReaderClick(ReaderArea area, Run run);
    }

    /** Provides the announcement of the current row. */
    public interface Announcement
    {
	void announce(Iterator it, boolean brief);
    }

    /** Provides navigation between rows and document elements. */
    public interface Transition
    {
	/** The supported navigation types. */
	enum Type{
	    NEXT, PREV,
	    STRICT_NEXT, STRICT_PREV,
	    NEXT_SECTION, PREV_SECTION,
	    NEXT_SECTION_SAME_LEVEL, PREV_SECTION_SAME_LEVEL,
	    NEXT_PARAGRAPH, PREV_PARAGRAPH,
	};

	boolean transition(Type type, Iterator it);
    }

    /** Construction parameters for the reader area. */
    static public final class Params
    {
	public ControlContext context = null;
	public String name = "";
	public ClickHandler clickHandler = null;
	public Announcement announcement = null;
	public Transition transition = new DefaultTransition();
	public Doc doc = null;
	public int width = 100;
    }

    protected final ControlContext context;
    protected final RegionPoint regionPoint = new RegionPoint();
    protected final ClipboardTranslator clipboardTranslator = new ClipboardTranslator(this, regionPoint, EnumSet.noneOf(ClipboardTranslator.Flags.class));
    protected final String name;
    protected final Announcement announcement;
    protected final Transition transition;
    protected ClickHandler clickHandler = null;

    protected Doc document = null;
    protected View view = null;
    protected Layout layout = null;
    protected Iterator iterator = null;
    protected int hotPointX = 0;
    private State state = State.READY;

    /**
     * Creates a reader area from the supplied parameters.
     *
     * @param params the construction parameters; must not be {@code null}
     * @throws NullPointerException if {@code params}, its context, transition or name is {@code null}
     * @throws IllegalArgumentException if the supplied width is negative
     */
    public ReaderArea(Params params)
    {
	requireNonNull(params, "params can't be null");
	NullCheck.notNull(params.context, "params.context");
	NullCheck.notNull(params.transition, "params.transition");
	NullCheck.notNull(params.name, "params.name");
	this.context = params.context;
	if (params.announcement != null)
	    this.announcement = params.announcement; else
	    this.announcement = new DefaultAnnouncement(params.context, (Strings)params.context.getI18n().getStrings(Strings.class));
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

    /**
     * Creates a reader area with an explicit announcement provider.
     *
     * @param context the control context; must not be {@code null}
     * @param announcement the announcement provider; must not be {@code null}
     * @param document the document to display; may be {@code null}
     * @param width the display width; must not be negative if a document is supplied
     * @throws NullPointerException if {@code context} or {@code announcement} is {@code null}
     * @throws IllegalArgumentException if {@code document} is not {@code null} and {@code width} is negative
     */
    public ReaderArea(ControlContext context, Announcement announcement, Doc document, int width)
    {
	requireNonNull(context, "context can't be null");
	requireNonNull(announcement, "announcement can't be null");
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

    /**
     * Creates a reader area with an explicit announcement provider and no document.
     *
     * @param context the control context; must not be {@code null}
     * @param announcement the announcement provider; must not be {@code null}
     * @throws NullPointerException if {@code context} or {@code announcement} is {@code null}
     */
    public ReaderArea(ControlContext context, Announcement announcement)
    {
	this(context, announcement, null, 0);
    }

    /**
     * Sets the document displayed by this area.
     *
     * @param document the document to display; must not be {@code null}
     * @param width the display width; must not be negative
     * @throws NullPointerException if {@code document} is {@code null}
     * @throws IllegalArgumentException if {@code width} is negative
     */
    public void setDocument(Doc document, int width)
    {
	requireNonNull(document, "document can't be null");
	if (width < 0)
	    throw new IllegalArgumentException("width (" + width + ") may not be negative");
	state = State.LOADING;
	this.document = document;
	this.view = new View(document, width);
	this.layout = view.createLayout();
	int defaultIndex = -1;
	final String defaultIndexStr = document.getProperty(View.DEFAULT_ITERATOR_INDEX_PROPERTY);
	if (defaultIndexStr != null && !defaultIndexStr.isEmpty())
	{
	    try {
		defaultIndex = Integer.parseInt(defaultIndexStr);
	    }
	    catch (NumberFormatException e)
	    {
	    }
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
	state = State.READY;
	context.onAreaNewContent(this);
	context.onAreaNewHotPoint(this);
    }

    /**
     * Returns the current state of this area.
     *
     * @return the current state
     */
    public State getState()
    {
	return state;
    }

    public boolean hasDocument()
    {
	return document != null && iterator != null;
    }

    public boolean isEmpty()
    {
	return !hasDocument() || iterator.noContent();
    }

    public Doc getDocument()
    {
	return document;
    }

    /**
     * Returns the title of the current document.
     *
     * @return the document title, or an empty string if there is no attribute or document
     */
    public String getDocTitle()
    {
	if (!hasDocument())
	    return "";
	final String res = document.getProperty(Doc.PROP_TITLE);
	return res != null?res:"";
    }

    /**
     * Returns the URL of the current document.
     *
     * @return the document URL, or an empty string if there is no attribute or document
     */
    public String getDocUrl()
    {
	if (!hasDocument())
	    return "";
	final String urlStr = document.getProperty(Doc.PROP_URL);
	return urlStr != null?urlStr:"";
    }

    /**
     * Returns the universal reference of the current document.
     *
     * @return the universal reference, or an empty string if no URL is available
     */
    public String getDocUniRef()
    {
	final String addr = getDocUrl();
	if (addr.isEmpty())
	    return "";
	return UniRefInfo.makeValue(UniRefProcs.TYPE_URL, addr);
    }

    public Run getCurrentRun()
    {
	if (isEmpty())
	    return null;
	return iterator.getRunUnderPos(hotPointX);
    }

    /**
     * Moves the hot point to the supplied run.
     *
     * @param run the run to find; must not be {@code null}
     * @return {@code true} if the run was found, {@code false} otherwise
     * @throws NullPointerException if {@code run} is {@code null}
     */
    public boolean findRun(Run run)
    {
	requireNonNull(run, "run can't be null");
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

    /**
     * Moves the reader to the specified row index.
     *
     * @param index the row index
     * @return {@code true} if the row was found and selected, {@code false} otherwise
     */
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
	if (isEmpty())
	    return new String[0];
	final LinkedList<String> res = new LinkedList<String>();
	final Run run = iterator.getRunUnderPos(hotPointX);
	if (run == null)
	    return new String[0];
	Attributes attrs = run.getAttrs();
	while (attrs != null)
	{
	    final Object value = attrs.attrMap.get(Attributes.ID);
	    if (value != null)
	    {
		final String valueStr = value.toString();
		if (!valueStr.isEmpty())
		    res.add(valueStr);
	    }
	    attrs = (attrs.parentAttr != null && !attrs.parentAttr.isEmpty()) ? attrs.parentAttr.get(0) : null;
	}
	return res.toArray(new String[res.size()]);
    }

    /**
     * Rebuilds the view with a new display width while trying to preserve the
     * current run selection.
     *
     * @param width the new width; must not be negative
     * @return {@code true} if the view was rebuilt, {@code false} if there is no document
     */
    public boolean rebuildView(int width)
    {
	if (isEmpty())
	    return false;
	final Run currentRun = getCurrentRun();
	view = new View(document, width);
	layout = view.createLayout();
	boolean found = false;
	if (currentRun != null)
	    found = findRun(currentRun);
	if (!found)
	{
	    iterator = view.getIterator();
	    hotPointX = 0;
	}
	if (iterator == null)
	    return false;
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

    @Override public boolean onInputEvent(InputEvent event)
    {
	requireNonNull(event, "event can't be null");
	if (!event.isSpecial() && !event.isModified())
	    switch(InputEvent.getKeyboardLayout().getAsciiOfButton(event.getChar()))
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

    @Override public boolean onSystemEvent(SystemEvent event)
    {
	requireNonNull(event, "event can't be null");
	if (event.getType() != SystemEvent.Type.REGULAR)
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
	requireNonNull(query, "query can't be null");
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
		final String res = run.getHref();
		if (res == null || res.isEmpty())
		    return false;
		((UniRefHotPointQuery)query).answer(UniRefProcs.TYPE_URL + ":" + res);
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
	if (!regionPoint.set(fromX, fromY, toX, toY))
	    return true;
	final ReaderArea.TextSelection selection = new TextSelection(view, fromX, fromY, toX, toY);
	final List<String> lines = selection.collect();
	if (lines == null || lines.isEmpty())
	    return false;
	context.getClipboard().set(lines.toArray(new String[lines.size()]));
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
	requireNonNull(listeningInfo, "listeningInfo can't be null");
	if (!(listeningInfo instanceof ReaderArea.ListeningInfo))
	    return;
	final ReaderArea.ListeningInfo info = (ReaderArea.ListeningInfo)listeningInfo;
	this.iterator = info.it;
	this.hotPointX = info.pos;
	context.onAreaNewHotPoint(this);
    }

    protected boolean onMoveHotPoint(MoveHotPointEvent event)
    {
	requireNonNull(event, "event can't be null");
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
		nearest = it2.clone();
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
	    context.onAreaNewHotPoint(this);
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

    protected boolean onTransition(InputEvent event, Transition.Type type, boolean briefAnnouncement, Hint hintFailed)
    {
	requireNonNull(event, "event can't be null");
	requireNonNull(type, "type can't be null");
	requireNonNull(hintFailed, "hintFailed can't be null");
	if (noContentCheck())
	    return true;
	if (transition.transition(type, iterator))
	    onNewRow(briefAnnouncement); else
	    context.setEventResponse(DefaultEventResponse.hint(hintFailed));
	return true;
    }

    protected boolean onAltEnd(InputEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveEnd();
	onNewRow(false);
	return true;
    }

    protected boolean onAltHome(InputEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveBeginning();
	onNewRow(false);
	return true;
    }

    protected boolean onNextSentence(InputEvent event)
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
	if (sentIt.atRowEnd() && !sentIt.forward(b, " "))
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.NO_LINES_BELOW));
	    return true;
	}

	this.iterator = sentIt.getIterator();
	this.hotPointX = sentIt.getPos();
	b = new StringBuilder();
	sentIt.forward(b, " ");
	final String text = new String(b).trim();
	context.onAreaNewHotPoint(this);
	if (!text.isEmpty())
	    context.setEventResponse(DefaultEventResponse.text(text)); else
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	return true;
    }

    protected boolean onMoveLeft(InputEvent event)
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
	context.onAreaNewHotPoint(this);
	return true;
    }

    protected boolean onMoveRight(InputEvent event)
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

    protected boolean onAltLeft(InputEvent event)
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

    protected boolean onAltRight(InputEvent event)
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

    protected boolean onHome(InputEvent event)
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

    protected boolean onEnd(InputEvent event)
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
		if (r.getHref() != null && !r.getHref().trim().isEmpty())
		{
		    final int pos = iterator.runBeginsAt(r);
		    if (pos >= 0)
		    {
			hotPointX = pos;
			context.say(r.getText());
			context.onAreaNewHotPoint(this);
			return true;
		    }
		}
	    }
	}
	if (iterator.getIndex() + 1 >= iterator.getCount())
	    return false;
	if (!iterator.searchForward((node,para,row)-> {
		    final Run[] runs = row.getRuns();
		    for(Run r: runs)
			if (r.getHref() != null && !r.getHref().trim().isEmpty())
			    return true;
		    return false;
		}, iterator.getIndex() + 1))
	    return false;
	final Run[] runs = iterator.getRuns();
	int k = 0;
	while (k < runs.length && (runs[k].getHref() == null || runs[k].getHref().trim().isEmpty()))
	    ++k;
	if (k >= runs.length)
	    return false;
	final int pos = iterator.runBeginsAt(runs[k]);
	if (pos < 0)
	    return false;
	hotPointX = pos;
	context.say(runs[k].getText());
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
	requireNonNull(text, "text can't be null");
	for(int i = startFrom;i < text.length();++i)
	    if (SentenceUtils.isSentenceEndChar(text.charAt(i)))
		return i;
	return -1;
    }

    static protected boolean charOfSentenceEnd(char ch)
    {
	return SentenceUtils.isSentenceEndChar(ch);
    }

    static protected class ListeningInfo extends ListenableArea.ListeningInfo
    {
	final Iterator it;
	final int pos;
	ListeningInfo(String text, Iterator it, int pos)
	{
	    super(text);
	    requireNonNull(it, "it can't be null");
	    this.it = it;
	    this.pos = pos;
	}
    }

    protected String textUntil(Iterator itTo, int posTo)
    {
	requireNonNull(itTo, "itTo can't be null");
	final Iterator tmpIt = iterator.clone();
	if (tmpIt.equals(itTo))
	    return tmpIt.getText().substring(Math.min(hotPointX, tmpIt.getText().length()), Math.min(posTo, tmpIt.getText().length()));
	final StringBuilder b = new StringBuilder();
	b.append(tmpIt.getText().substring(Math.min(hotPointX, tmpIt.getText().length())));
	while(tmpIt.moveNext() && !tmpIt.equals(itTo))
	    b.append(" " + tmpIt.getText());
	if (tmpIt.equals(itTo))
	    b.append(" " + tmpIt.getText().substring(0, Math.min(posTo, tmpIt.getText().length())));
	return new String(b);
    }

    /**
     * Collects the text covering the supplied selection coordinates.
     */
    static private final class TextSelection
    {
	private final View view;
	private final int fromX;
	private final int fromY;
	private final int toX;
	private final int toY;

	TextSelection(View view, int fromX, int fromY, int toX, int toY)
	{
	    requireNonNull(view, "view can't be null");
	    final int tmp;
	    this.view = view;
	    if (fromY == toY)
	    {
		if (fromX <= toX)
		{
		    this.fromX = fromX;
		    this.toX = toX;
		} else
		{
		    this.fromX = toX;
		    this.toX = fromX;
		}
		this.fromY = fromY;
		this.toY = toY;
	    } else if (fromY < toY)
	    {
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
	    } else
	    {
		this.fromX = toX;
		this.fromY = toY;
		this.toX = fromX;
		this.toY = fromY;
	    }
	}

	List<String> collect()
	{
	    final List<String> res = new ArrayList<>();
	    final Iterator it = view.getIterator();
	    boolean found = false;
	    while (it.canMoveNext())
	    {
		if (it.coversPos(fromX, fromY))
		    found = true;
		if (found)
		{
		    res.add(rowTextForSelection(it));
		}
		if (it.coversPos(toX, toY))
		    break;
		it.moveNext();
	    }
	    return found?res:null;
	}

	private String rowTextForSelection(Iterator it)
	{
	    final String text = it.getText();
	    final int start = it.coversPos(fromX, fromY) ? Math.max(0, fromX - it.getX()) : 0;
	    final int end = it.coversPos(toX, toY) ? Math.max(start, Math.min(text.length(), toX - it.getX())) : text.length();
	    if (start >= text.length() || end <= start)
		return "";
	    return text.substring(start, end);
	}
    }
}
