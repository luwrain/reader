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
import org.luwrain.doctree.*;
import org.luwrain.controls.doc.*;
import org.luwrain.app.reader.books.*;

class App implements Application
{
    static private final String LOG_COMPONENT = "reader";

    private Luwrain luwrain = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;
    private Strings strings = null;

    private DocumentArea readerArea = null;
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
			     ()->showErrorPage());
	this.actions = new Actions(luwrain, base, strings);
	this.actionLists = new ActionLists(luwrain, base, strings);
	createAreas();

		this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
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
	final org.luwrain.controls.doc.Strings announcementStrings = (org.luwrain.controls.doc.Strings)luwrain.i18n().getStrings("luwrain.doctree");
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
			    goToReaderArea();
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
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getTreeAreaActions(base.hasDocument());
		}
	    };

	readerArea = new DocumentArea(new DefaultControlEnvironment(luwrain), announcement){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    /*
			    switch(mode)
			    {
			    case DOC:
			    case BOOK:
				return false;
			    case DOC_NOTES:
			    case BOOK_NOTES_ONLY:
			    case BOOK_TREE_NOTES:
				goToNotesArea();
			    return true;
			    case BOOK_TREE_ONLY:
goToTreeArea();
return true;
			    default:
			    return false;
			}
			    */
			    //FIXME:
			    return false;
			case ESCAPE:
			    return base.stopAudio();
			case ENTER:
			    if (Base.hasHref(this))
				return jumpByHref(Base.getHref(this), luwrain.getAreaVisibleWidth(readerArea));
			    return Actions.onPlayAudio(base, readerArea);
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


		    case ACTION:
			return onAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    case PROPERTIES:
			return showDocProperties();
		    default:
			return super.onSystemEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actionLists.getReaderActions();
		}
		@Override public String getAreaName()
		{
		    final Document doc = getDocument();
		    return doc != null?doc.getTitle():strings.appName();
		}
		@Override protected void announceRow(org.luwrain.doctree.view.Iterator it, boolean briefAnnouncement)
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
			    //FIXME:
			    /*
			    switch(mode)
			    {
			    case DOC:
			    case BOOK:
			    case DOC_NOTES:
			    case BOOK_NOTES_ONLY:
				goToReaderArea();
				return true;
			    case BOOK_TREE_ONLY:
			    case BOOK_TREE_NOTES:
				goToTreeArea();
			    return true;
			    default:
			    return false;
			    }
			    */
			    return false;
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
	if (ActionEvent.isAction(event, "play-audio"))
	    return Actions.onPlayAudio(base, readerArea);
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
	Log.debug("reader", "click in sections tree to open:" + sect.href);
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

    //Handles the success notification
    private void onNewDocument()
    {
	final Document doc = base.getDocument();
	if (base.isInBookMode())
	    treeArea.refresh();
	base.updateNotesModel();
	notesArea.refresh();
	updateMode();
	//doc.commit();
	Log.debug(LOG_COMPONENT, "Preparing reader area for new document");
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

    private boolean showDocProperties()
    {
	//FIXME:
	return false;
    }

    //Handles the error notification
    private void showErrorPage()
    {
	//FIXME:
    }

    private void updateMode()
    {
	//FIXME:
    }

    //Doesn't check if the base in book mode
void switchMode()
    {
	/*
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
	readerArea.rebuildView(luwrain.getAreaVisibleWidth(readerArea));
	    */
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
