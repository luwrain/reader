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
import org.luwrain.controls.*;
import org.luwrain.doctree.*;

public class ReaderApp implements Application, Actions
{
    static private final int DOC_MODE_LAYOUT_INDEX = 0;
    static private final int BOOK_MODE_LAYOUT_INDEX = 1;
    static private final int INFO_MODE_LAYOUT_INDEX = 2;

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private DocTreeArea readerArea;
    private TreeArea treeArea;
    private ListArea notesArea;
    private SimpleArea infoArea;
    private AreaLayoutSwitch layouts;
    private Document doc = null;
    private DocInfo docInfo = null;

    public ReaderApp()
    {
	docInfo = null;
    }

    public ReaderApp(DocInfo docInfo)
    {
	NullCheck.notNull(docInfo, "docInfo");
	this.docInfo = docInfo;
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
	layouts.add(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, treeArea, readerArea, notesArea));
	layouts.add(new AreaLayout(infoArea));
	if (docInfo != null)
	    base.fetch(this, docInfo); else
	    docInfo = new DocInfo();
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getTreeModel();
	treeParams.name = strings.treeAreaName();
	treeParams.clickHandler = (area, obj)->onTreeClick(area, obj);

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
			return onTreeAreaAction(event);
		    case CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return getTreeAreaActions();
		}
	    };

	readerArea = new DocTreeArea(new DefaultControlEnvironment(luwrain), new Introduction(new DefaultControlEnvironment(luwrain), strings), null){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && event.withShiftOnly())
			switch(event.getSpecial())
			{
			case ENTER:
			    return showDocInfo();
			}
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    goToNotesArea();
			    return true;
			case ENTER:
			    if (hasHref())
				return jumpByHref(getHref());
			    return false;
			    /*
			      case BACKSPACE:
			      return onBackspace(event);
			    */
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			return onReaderAreaAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return getReaderAreaActions();
		}
		@Override public String getAreaName()
		{
		    final Document doc = getDocument();
		    return doc != null?doc.getTitle():strings.appName();
		}
		@Override protected String noContentStr()
		{
		    return fetchingInProgress()?strings.noContentFetching():strings.noContent();
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
			return onNotesAreaAction(event);
		    case CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return getNotesAreaActions();
		}
	    };

	infoArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    return returnFromInfoArea();
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "evet");
		    switch(event.getCode())
		    {
		    case ACTION:
			return onInfoAreaAction(event);
		    case CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return getInfoAreaActions();
		}
	    };
    }

    private Action[] getReaderAreaActions()
    {
	final LinkedList<Action> res = new LinkedList<Action>();
	res.add(new Action("open-file", strings.actionOpenFile(), new KeyboardEvent(KeyboardEvent.Special.F5)));
	res.add(new Action("open-url", strings.actionOpenUrl(), new KeyboardEvent(KeyboardEvent.Special.F6)));
	if (base.hasDocument())
	{
	    res.add(new Action("open-in-narrator", strings.actionOpenInNarrator(), new KeyboardEvent(KeyboardEvent.Special.F8)));
	    res.add(new Action("change-format", strings.actionChangeFormat(), new KeyboardEvent(KeyboardEvent.Special.F9)));
	    res.add(new Action("change-charset", strings.actionChangeCharset(), new KeyboardEvent(KeyboardEvent.Special.F10)));
	    res.add(new Action("book-mode", strings.actionBookMode()));
	    res.add(new Action("doc-mode", strings.actionDocMode()));
	    res.add(new Action("play-audio", strings.actionPlayAudio(), new KeyboardEvent(KeyboardEvent.Special.F7)));
	    res.add(new Action("info", strings.actionInfo(), new KeyboardEvent(KeyboardEvent.Special.F8)));
	}
	return res.toArray(new Action[res.size()]);
    }

    private boolean onReaderAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "open-url"))
	    return openNew(true);
	if (ActionEvent.isAction(event, "open-file"))
	    return openNew(false);
	if (ActionEvent.isAction(event, "open-in-narrator"))
	    return openInNarrator();
	if (ActionEvent.isAction(event, "doc-mode"))
	    return docMode();
	if (ActionEvent.isAction(event, "book-mode"))
	    return bookMode();
	if (ActionEvent.isAction(event, "change-format"))
	    return anotherFormat();
	if (ActionEvent.isAction(event, "another-charset"))
	    return anotherCharset();
	if (ActionEvent.isAction(event, "play-audio"))
	    return playAudio();
	return false;
    }

    private Action[] getTreeAreaActions()
    {
	return getTreeAreaActions();
    }

    private boolean onTreeAreaAction(EnvironmentEvent event)
    {
	return false;
    }

    private boolean onTreeClick(TreeArea area, Object obj)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof Book.Section))
	    return false;
	final Book.Section sect = (Book.Section)obj;
	if (!jumpByHref(sect.href()))
	    return false;
	goToReaderArea();
	return true;
    }

    private Action[] getNotesAreaActions()
    {
	return getTreeAreaActions();
    }

    private boolean onNotesAreaAction(EnvironmentEvent event)
    {
	return false;
    }

    private boolean onNotesClick(Object item)
    {
	return false;
    }

    private Action[] getInfoAreaActions()
    {
	return new Action[]{
	};
    }

    private boolean onInfoAreaAction(EnvironmentEvent event)
    {
	return false;
    }

    @Override public void setDocument(Document doc)
    {
    }

    @Override public int getReaderAreaVisibleWidth()
    {
	return luwrain.getAreaVisibleWidth(readerArea); 
    }

    private boolean playAudio()
    {
	if (!base.isInBookMode())
	    return false;
	final String[] ids = readerArea.getHtmlIds();
	if (ids == null || ids.length < 1)
	    return false;
	return base.playAudio(readerArea, ids);
    }

    private boolean jumpByHref(String href)
    {
	return base.jumpByHref(this, href);
    }

    @Override public void onNewResult(Result res)
    {
	NullCheck.notNull(res, "res");
	if (res.type() != Result.Type.OK)
	{
	    showErrorPage(res);
	    return;
	}
	final Document doc = base.acceptNewSuccessfulResult(res);
	if (doc == null)
	    return;
	base.setDocument(this, doc);
	if (base.isInBookMode())
	{
	    bookMode(); 
	    treeArea.refresh();
	} else
	    docMode();
    }

    private boolean openInNarrator()
    {
	base.openInNarrator();
	return true;
    }

    private boolean openNew(boolean url)
    {
	base.openNew(url, readerArea.hasHref()?readerArea.getHref():"");
	return true;
    }

    private boolean anotherFormat()
    {
	base.anotherFormat();
	return true;
    }

    private boolean anotherCharset()
    {
	base.anotherCharset();
	return true;
    }

    private boolean fetchingInProgress()
    {
	return base.fetchingInProgress();
    }

    private boolean showDocInfo()
    {
	infoArea.clear();
	base.prepareDocInfoText(infoArea);
	layouts.show(INFO_MODE_LAYOUT_INDEX);
	luwrain.say("Информация о документе");
	return true;
    }

    private void showErrorPage(Result res)
    {
	NullCheck.notNull(res, "res");
	infoArea.clear();
	base.prepareErrorText(res, infoArea);
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	layouts.show(INFO_MODE_LAYOUT_INDEX);
    }

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

    private boolean returnFromInfoArea()
    {
	/*
	  final Result currentDoc = base.currentDoc();
	  if (currentDoc == null)
	  return false;
	  layouts.show(DOC_MODE_LAYOUT_INDEX);
	  return true;
	*/
	return false;
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

    private boolean goToTreeArea()
    {
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return false;
	luwrain.setActiveArea(treeArea);
	return true;
    }

    private boolean goToReaderArea()
    {
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return false;
	luwrain.setActiveArea(readerArea);
	return true;
    }

    private boolean goToNotesArea()
    {
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return false;
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

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}
