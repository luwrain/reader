/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;
import org.luwrain.doctree.loading.*;

public class ReaderApp implements Application
{
    enum Modes {DOC, DOC_NOTES, BOOK, BOOK_TREE_ONLY, BOOK_NOTES_ONLY, BOOK_TREE_NOTES};

    static private final int READER_ONLY_LAYOUT_INDEX = 0;
    static private final int READER_TREE_LAYOUT_INDEX = 1;
    static private final int READER_NOTES_LAYOUT_INDEX = 2;
    static private final int READER_TREE_NOTES_LAYOUT_INDEX = 3;
    static private final int PROPERTIES_LAYOUT_INDEX = 4;

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private DoctreeArea readerArea;
    private TreeArea treeArea;
    private ListArea notesArea;
    private SimpleArea propertiesArea;
    private AreaLayoutSwitch layouts;
    private Announcement announcement;
    private String startingUrl;
    private String startingContentType;
    private Modes mode = Modes.DOC;

    public ReaderApp()
    {
	startingUrl = "";
    startingContentType = "";
    }

    public ReaderApp(String url, String contentType)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	this.startingUrl = url;
	this.startingContentType = contentType;
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(readerArea));
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT, treeArea, readerArea));
	layouts.add(new AreaLayout(AreaLayout.TOP_BOTTOM, readerArea, notesArea));
	layouts.add(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, treeArea, readerArea, notesArea));
	layouts.add(new AreaLayout(propertiesArea));
	openStartFrom();
	return true;
    }

    void onNewResult(UrlLoader.Result res)
    {
	NullCheck.notNull(res, "res");
	Log.debug("reader", "new result, type is " + res.type().toString());
	luwrain.onAreaNewBackgroundSound(readerArea);
	if (res.type() != UrlLoader.Result.Type.OK)
	    showErrorPage(res); else
	    onNewDocument(res.book(), res.doc());
    }

    int getCurrentRowIndex()
    {
	return 10;//FIXME:
    }

    private void createAreas()
    {
	announcement = new Announcement(new DefaultControlEnvironment(luwrain), strings);


	//	final Actions actions = this;

	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getTreeModel();
	treeParams.name = strings.treeAreaName();
	treeParams.clickHandler = (area, obj)->onTreeClick( obj);

	treeArea = new TreeArea(treeParams){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if(event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    goToReaderArea();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			return onAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return Actions.getTreeAreaActions(strings, base.hasDocument(), mode, base.isInBookMode());
		}
	    };

	readerArea = new DoctreeArea(new DefaultControlEnvironment(luwrain), null){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    switch(mode)
			    {
			    case DOC:
			    case BOOK:
				return false;
			    case DOC_NOTES:
			    case BOOK_NOTES_ONLY:
			    case BOOK_TREE_NOTES:
				return goToNotesArea();
			    case BOOK_TREE_ONLY:
return goToTreeArea();
			    default:
			    return false;
			    }
			case ENTER:
			    if (Base.hasHref(this))
				return jumpByHref(Base.getHref(this), luwrain.getAreaVisibleWidth(readerArea));
			    return Actions.onPlayAudio(base, readerArea);
			case BACKSPACE:
			    return onBackspace(event);
			}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			return onAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    case PROPERTIES:
			return showDocProperties();
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return Actions.getReaderAreaActions(strings, base.hasDocument(), mode, base.isInBookMode());
		}
		@Override public String getAreaName()
		{
		    final Document doc = getDocument();
		    return doc != null?doc.getTitle():strings.appName();
		}
		@Override protected void announceRow(org.luwrain.doctree.Iterator it, boolean briefAnnouncement)
		{
		    NullCheck.notNull(it, "it");
		    announcement.announce(it, briefAnnouncement);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.BACKGROUND_SOUND:
if (base.fetchingInProgress())
			{
			    ((BackgroundSoundQuery)query).answer(new BackgroundSoundQuery.Answer(BkgSounds.FETCHING));
			return true;
			}
			return false;
		    default:
			return super.onAreaQuery(query);
		    }
		}
		@Override protected String noContentStr()
		{
		    return base.fetchingInProgress()?strings.noContentFetching():strings.noContent();
		}
	    };

	final ListArea.Params listParams = new ListArea.Params();
	listParams.environment = treeParams.environment;
	listParams.model = base.getNotesModel();
	listParams.appearance = new DefaultListItemAppearance(listParams.environment);
	listParams.clickHandler = (area, index, obj)->onNotesClick(obj);
	listParams.name = strings.notesAreaName();

	notesArea = new ListArea(listParams){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    goToTreeArea();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			return onAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return Actions.getNotesAreaActions(strings, base.hasDocument(), mode, base.isInBookMode());
		}
	    };

	propertiesArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.propertiesAreaName()){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    return closePropertiesArea();
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "evet");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
    }

    private boolean onAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "open-url"))
	    return base.openNew(this, true, Base.hasHref(readerArea)?Base.getHref(readerArea):"");
	if (ActionEvent.isAction(event, "open-file"))
	    return base.openNew(this, false, Base.hasHref(readerArea)?Base.getHref(readerArea):"");
	if (ActionEvent.isAction(event, "open-in-narrator"))
	    return base.openInNarrator();
	/*
	if (ActionEvent.isAction(event, "doc-mode"))
	    return docMode();
	if (ActionEvent.isAction(event, "book-mode"))
	    return bookMode();
	*/
	if (ActionEvent.isAction(event, "change-format"))
	    return Actions.onChangeFormat(this, luwrain, strings, base);
	if (ActionEvent.isAction(event, "change-charset"))
	    return Actions.onChangeCharset(this, luwrain, strings, base);
	if (ActionEvent.isAction(event, "play-audio"))
	    return Actions.onPlayAudio(base, readerArea);
	if (ActionEvent.isAction(event, "add-note"))
	    return addNote();
	return false;
    }

    private boolean onTreeClick(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof Book.Section))
	    return false;
	final Book.Section sect = (Book.Section)obj;
	Log.debug("reader", "click in sections tree to open:" + sect.href());
	if (!jumpByHref(sect.href(), luwrain.getAreaVisibleWidth(readerArea)))
	    return false;
	//	goToReaderArea();
	return true;
    }

    private boolean onNotesClick(Object item)
    {
	NullCheck.notNull(item, "item");
	if (!base.isInBookMode() || !(item instanceof Book.Note))
	    return false;
	final Book.Note note = (Book.Note)item;
	base.jumpByNote(this, note);
	return true;
    }

    private void onNewDocument(Book book, Document doc)
    {
	if (book == null && doc == null)
	    return;
	final Document newDoc = base.acceptNewSuccessfulResult(book, doc, getSuitableWidth());
	if (base.isInBookMode())
	    treeArea.refresh();
	updateMode();
	readerArea.setDocument(newDoc);
	goToReaderArea();
	announceNewDoc(newDoc);
    }

    private boolean onBackspace(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (base.isInBookMode())
	{
	    final Document doc = base.onPrevDocInBook();
	    if (doc == null)
		return false;
	    updateMode();
	    readerArea.setDocument(doc);
	    goToReaderArea();
	    announceNewDoc(doc);
	    return true;
	}
	if (base.onPrevDocInNonBook(this))
	{
	    luwrain.onAreaNewBackgroundSound(readerArea);
	    updateMode();
	    return true;
	}
	return false;
    }

    private boolean jumpByHref(String href, int width)
    {
	if (base.isInBookMode())
	{
	    final Document doc = base.jumpByHrefInBook(href, width);
	    if (doc == null)
	    {
		luwrain.launchApp("reader", new String[]{href});
		return true;
	    }
	    readerArea.setDocument(doc);
	    updateMode();
	    goToReaderArea();
	    announceNewDoc(doc);
	    return true;
	}
	if (base.jumpByHrefInNonBook(this, href))
	{
	    luwrain.onAreaNewBackgroundSound(readerArea);
	    updateMode();
	    return true;
	}
	return false;
    }

    private boolean showDocProperties()
    {
	propertiesArea.clear();
	if (!base.fillDocProperties(propertiesArea))
	    return false;
	layouts.show(PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    private void showErrorPage(UrlLoader.Result res)
    {
	NullCheck.notNull(res, "res");
	propertiesArea.clear();
	base.prepareErrorText(res, propertiesArea);
	//	luwrain.silence();
	//	luwrain.playSound(Sounds.INTRO_REGULAR);
	layouts.show(PROPERTIES_LAYOUT_INDEX);
	luwrain.message(strings.errorAnnouncement(), Luwrain.MESSAGE_ERROR);
    }

    private void updateMode()
    {
	switch(mode)
	{
	case DOC:
	    if (base.isInBookMode())
		switchMode(Modes.BOOK_TREE_ONLY);
	    return;
	case DOC_NOTES:
	    if (base.isInBookMode())
		switchMode(Modes.BOOK_TREE_NOTES);
	    return;
	case BOOK:
	case BOOK_TREE_ONLY:
	case BOOK_NOTES_ONLY:
	case BOOK_TREE_NOTES:
	    if (!base.isInBookMode())
		switchMode(Modes.DOC);
	    return;
	}
    }

    //Doesn't check if the base in book mode
    private void switchMode(Modes newMode)
    {
	NullCheck.notNull(newMode, "newMode");
	switch(newMode)
	{
	case DOC:
	    layouts.show(READER_ONLY_LAYOUT_INDEX);
	    break;
	case DOC_NOTES:
	    layouts.show(READER_NOTES_LAYOUT_INDEX);
	    break;
	case BOOK:
	    layouts.show(READER_ONLY_LAYOUT_INDEX);
	    break;
	case BOOK_TREE_ONLY:
	    layouts.show(READER_TREE_LAYOUT_INDEX);
	    break;
	case BOOK_NOTES_ONLY:
	    layouts.show(READER_NOTES_LAYOUT_INDEX);
	    break;
	case BOOK_TREE_NOTES:
	    layouts.show(READER_TREE_NOTES_LAYOUT_INDEX);
	    break;
	default:
	    return;
	}
	mode = newMode;
    }

    /*
    private boolean docMode()
    {
	layouts.show(DOC_MODE_LAYOUT_INDEX);
	return true;
    }

    private boolean bookMode()
    {
	layouts.show(BOOK_MODE_LAYOUT_INDEX);
	return true;
    }
    */

    private boolean closePropertiesArea()
    {
	if (!base.hasDocument())
	    return false;
	switchMode(mode);
	  luwrain.announceActiveArea();
	  return true;
    }

    private int getSuitableWidth()
    {
	final int areaWidth = luwrain.getAreaVisibleWidth(readerArea);
	final int screenWidth = luwrain.getScreenWidth();
	int width = areaWidth;
	if (width < 80)
	    width = screenWidth;
	if (width < 80)
	    width = 80;
	return width;
    }

    private boolean addNote()
    {
	if (!base.isInBookMode())
	    return false;
	if (!base.addNote())
	    return true;
	notesArea.refresh();
	return true;
    }

    private void closeApp()
    {
	luwrain.closeApp();
    }

    private void announceNewDoc(Document doc)
    {
	NullCheck.notNull(doc, "doc");
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	luwrain.say(doc.getTitle());
    }

private void openStartFrom()
{
    try {
	if (!startingUrl.isEmpty())
	    base.open(this, new  URL(startingUrl), startingContentType);
    }
    catch(MalformedURLException e)
    {
	luwrain.crash(e);//FIXME:
    }
}

    private boolean goToTreeArea()
    {
	/*
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return false;
	*/
	luwrain.setActiveArea(treeArea);
	return true;
    }

    private boolean goToReaderArea()
    {
	/*
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return false;
	*/
	luwrain.setActiveArea(readerArea);
	return true;
    }

    private boolean goToNotesArea()
    {
	/*
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return false;
	*/
	luwrain.setActiveArea(notesArea);
	return true;
    }

    @Override public String getAppName()
    {
	return readerArea != null?readerArea.getAreaName():strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }
}
