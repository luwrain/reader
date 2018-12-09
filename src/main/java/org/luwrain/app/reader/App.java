/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.reader;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;

class App implements Application
{
    static private final String LOG_COMPONENT = "reader";

    private Luwrain luwrain = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;
    private Strings strings = null;

    private ReaderArea readerArea = null;
    private TreeArea treeArea = null;
    private ListArea notesArea = null;
    private AreaLayoutHelper layout = null;

    private boolean showSections = false;
    private final boolean showNotes = false;

    private final String startingUrl;
    private final String startingContentType;

    App()
    {
	startingUrl = "";
	startingContentType = "";
    }

    App(String url, String contentType)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	this.startingUrl = url;
	this.startingContentType = contentType;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings,
			     ()->onNewDocument(),
			     ()->onNewBook(),
			     ()->showErrorPage());
	this.actions = new Actions(luwrain, base, strings);
	this.actionLists = new ActionLists(luwrain, base, strings);
	createAreas();

		this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		//luwrain.announceActiveArea();
	    }, readerArea);
	openStartFrom();
	return new InitResult();
    }

    int getCurrentRowIndex()
    {
	return 10;//FIXME:
    }

    private void createAreas()
    {
	final org.luwrain.controls.reader.Strings announcementStrings = (org.luwrain.controls.reader.Strings)luwrain.i18n().getStrings(org.luwrain.controls.reader.Strings.NAME);
	final Announcement announcement = new Announcement(new DefaultControlEnvironment(luwrain), announcementStrings);
	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getTreeModel();
	treeParams.name = strings.treeAreaName();
	treeParams.clickHandler = (area, obj)->onTreeClick( obj);
	treeArea = new TreeArea(treeParams){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if(event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(readerArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			if (ActionEvent.isAction(event, "hide-sections-tree"))
			{
			    showSections = false;
			    luwrain.setActiveArea(readerArea);
			    updateMode();
			    return true;
			}
			return false;
		    case HELP:
			luwrain.openHelp("luwrain.reader");
			return true;
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[]{
			new Action("hide-sections-tree", strings.actionHideSectionsTree(), new KeyboardEvent(KeyboardEvent.Special.ESCAPE)),
		    };
		}
	    };

	readerArea = new ReaderArea(new DefaultControlEnvironment(luwrain), announcement){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    if (showNotes)
			    {
				luwrain.setActiveArea(notesArea);
				return true;
			    }
			    if (base.isInBookMode() && showSections)
			    {
				luwrain.setActiveArea(treeArea);
				return true;
			    }
			    return false;
			case ESCAPE:
			    if (base.stopAudio())
				return true;
			    closeApp();
			    return true;
			case ENTER:
			    if (Base.hasHref(this))
				return jumpByHref(Base.getHref(this), luwrain.getAreaVisibleWidth(readerArea));
			    return actions.onPlayAudio(readerArea);
			case BACKSPACE:
			    return base.onPrevDoc();
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case SAVE:
			return actions.onSaveBookmark(readerArea);
					    case HELP:
			luwrain.openHelp("luwrain.reader");
			return true;
					    case ACTION:
			return onAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    case PROPERTIES:
			return showProps();
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.UNIREF_AREA:
			if (isEmpty() || !base.hasDocument())
			    return false;
			{
			    final String title = getAreaName().replaceAll(":", "\\:");
			    final String url = base.getDocument().getUrl().toString();
			    ((UniRefAreaQuery)query).answer("link:" + title + ":reader:" + url);
			}
			return true;
		    case AreaQuery.URL_AREA:
			if (isEmpty() || !base.hasDocument())
			    return false;
			((UrlAreaQuery)query).answer(base.getDocument().getUrl().toString());
			return true;
		    case AreaQuery.BACKGROUND_SOUND:
			if (base.isBusy())
			{
			    ((BackgroundSoundQuery)query).answer(new BackgroundSoundQuery.Answer(BkgSounds.FETCHING));
			    return true;
			}
			return false;
		    default:
			return super.onAreaQuery(query);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getReaderActions();
		}
		@Override public String getAreaName()
		{
		    if (!base.hasDocument())
			return strings.appName();
		    return base.getDocument().getTitle();
		}
		@Override protected void announceRow(org.luwrain.reader.view.Iterator it, boolean briefAnnouncement)
		{
		    NullCheck.notNull(it, "it");
		    announcement.announce(it, briefAnnouncement);
		}
		@Override protected String noContentStr()
		{
		    return base.isBusy()?strings.noContentFetching():strings.noContent();
		}
	    };

	final ListArea.Params listParams = new ListArea.Params();
	listParams.context = treeParams.environment;
	listParams.model = base.getNotesModel();
	listParams.appearance = new ListUtils.DefaultAppearance(listParams.context, Suggestions.LIST_ITEM);
	listParams.clickHandler = (area, index, obj)->onNotesClick(obj);
	listParams.name = strings.notesAreaName();
	notesArea = new ListArea(listParams){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    if (base.isInBookMode() && showSections)
				luwrain.setActiveArea(treeArea); else
				luwrain.setActiveArea(readerArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			return onAction(event);
					    case HELP:
			luwrain.openHelp("luwrain.reader");
			return true;
					    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getNotesAreaActions(base.hasDocument());
		}
	    };
    }

    private boolean onAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "open-url"))
	    return actions.onOpenUrl(Base.hasHref(readerArea)?Base.getHref(readerArea):"");
	if (ActionEvent.isAction(event, "open-file"))
	    return actions.onOpenFile();
	if (ActionEvent.isAction(event, "change-text-para-style"))
	    return actions.onChangeTextParaStyle();
	if (ActionEvent.isAction(event, "open-in-narrator"))
	    return base.openInNarrator();
	if (ActionEvent.isAction(event, "show-sections-tree"))
	    return false;//FIXME:
	if (ActionEvent.isAction(event, "hide-sections-tree"))
	    return false;//FIXME:
	if (ActionEvent.isAction(event, "show-notes"))
	    return false;//FIXME:
	if (ActionEvent.isAction(event, "hide-notes"))
	    return false;//FIXME:
	if (ActionEvent.isAction(event, "save-bookmark"))
	    return actions.onSaveBookmark(readerArea);
	if (ActionEvent.isAction(event, "restore-bookmark"))
	    return actions.onRestoreBookmark(readerArea);
	/*
	if (ActionEvent.isAction(event, "change-format"))
	    return Actions.onChangeFormat(this, luwrain, strings, base);
	*/
	/*
	if (ActionEvent.isAction(event, "change-charset"))
	    return Actions.onChangeCharset(this, luwrain, strings, base);
	*/
	if (ActionEvent.isAction(event, "add-note"))
	    return addNote();
	if (ActionEvent.isAction(event, "delete-note"))
	    return onDeleteNote();
	return false;
    }

    private boolean onTreeClick(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof Book.Section))
	    return false;
	final Book.Section sect = (Book.Section)obj;
	if (!jumpByHref(sect.href, luwrain.getAreaVisibleWidth(readerArea)))
	    return false;
	//	goToReaderArea();
	return true;
    }

    private boolean onNotesClick(Object item)
    {
	NullCheck.notNull(item, "item");
	if (!(item instanceof Note))
	    return false;
	final Note note = (Note)item;
	if (!base.isInBookMode())
	{
	    readerArea.setCurrentRowIndex(note.position);
	    goToReaderArea();
	    return true;
	}
	final Document doc = base.jumpByHrefInBook(note.url, readerArea.getCurrentRowIndex(), note.position);
	if (doc == null)
	    return false;
	readerArea.setDocument(doc, luwrain.getAreaVisibleWidth(readerArea));
	goToReaderArea();
	return true;
    }

    //Handle the new book notification
    private void onNewBook()
    {
	if (!base.isInBookMode())
	    throw new RuntimeException("Must be in book mode");
	treeArea.refresh();
	showSections = true;
    }

    //Handles the success notification
    private void onNewDocument()
    {
	if (!base.hasDocument())
	    throw new RuntimeException("base does not have any document");
	base.updateNotesModel();
	notesArea.refresh();
	updateMode();
	final Document doc = base.getDocument();
	readerArea.setDocument(doc, luwrain.getAreaVisibleWidth(readerArea));
	luwrain.setActiveArea(readerArea);
	announceNewDoc(doc);
    }

    private boolean jumpByHref(String href, int width)
    {
	if (base.isInBookMode())
	{
	    final Document doc = base.jumpByHrefInBook(href, readerArea.getCurrentRowIndex(), -1);
	    if (doc == null)
	    {
		luwrain.launchApp("reader", new String[]{href});
		return true;
	    }
	    readerArea.setDocument(doc, luwrain.getAreaVisibleWidth(readerArea));
	    updateMode();
	    goToReaderArea();
	    announceNewDoc(doc);
	    return true;
	}
	if (base.jumpByHrefInNonBook(href, getCurrentRowIndex()))
	{
	    luwrain.onAreaNewBackgroundSound(readerArea);
	    updateMode();
	    return true;
	}
	return false;
    }

    //Handles the error notification
    private void showErrorPage()
    {
	//FIXME:
    }

    private void updateMode()
    {
	final boolean sects = this.showSections && base.isInBookMode();
	final boolean notes = this.showNotes;
	if (sects && notes)
	{
	    layout.setBasicLayout(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, treeArea, readerArea, notesArea));
	    readerArea.rebuildView(luwrain.getAreaVisibleWidth(readerArea));
	    return;
	}
	if (sects)
	{
	    layout.setBasicLayout(new AreaLayout(AreaLayout.LEFT_RIGHT, treeArea, readerArea));
	    readerArea.rebuildView(luwrain.getAreaVisibleWidth(readerArea));
	    return;
	}
	if (notes)
	{
	    layout.setBasicLayout(new AreaLayout(AreaLayout.TOP_BOTTOM, readerArea, notesArea));
	    readerArea.rebuildView(luwrain.getAreaVisibleWidth(readerArea));
	    return;
	}
	layout.setBasicLayout(new AreaLayout(readerArea));
	readerArea.rebuildView(luwrain.getAreaVisibleWidth(readerArea));
    }

    private boolean showProps()
    {
	if (!base.hasDocument())
	    return false;
	final SimpleArea propsArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.propertiesAreaName()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
					    case HELP:
			luwrain.openHelp("luwrain.reader");
			return true;
					    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	final DocProps props = new DocProps(luwrain, strings, base.getDocument());
	props.fillProperties(propsArea);
	layout.openTempArea(propsArea);
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
	if (!base.hasDocument())
	    return false;
	if (!base.addNote(readerArea.getCurrentRowIndex()))
	    return true;
	notesArea.refresh();
	return true;
    }

    private boolean onDeleteNote()
    {
	if (notesArea.selected() == null || !(notesArea.selected() instanceof Note))
	    return false;
	final Note note = (Note)notesArea.selected();
	if (!Popups.confirmDefaultNo(luwrain, "Удаление закладки", "Вы действительно хотите удалить закладку \"" + note.comment + "\"?"))
	    return true;
	base.deleteNote(note);
	notesArea.refresh();
	return true;
    }

    @Override public void closeApp()
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
		base.openInitial(new  URL(startingUrl), startingContentType);
	}
	catch(MalformedURLException e)
    {
	luwrain.crash(e);//FIXME:
    }
    }

    private void goToTreeArea()
    {
	luwrain.setActiveArea(treeArea);
    }

    private void goToReaderArea()
    {
	luwrain.setActiveArea(readerArea);
    }

    private void goToNotesArea()
    {
	luwrain.setActiveArea(notesArea);
    }

    @Override public String getAppName()
    {
	return readerArea != null?readerArea.getAreaName():strings.appName();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
    }
}
