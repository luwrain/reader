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

    static private final String STRINGS_NAME = "luwrain.reader";

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private ReaderArea readerArea;
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
	this.docInfo = docInfo;
	NullCheck.notNull(docInfo, "docInfo");
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
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
		base.fetch(readerArea, docInfo); else
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

	treeArea = new TreeArea(treeParams){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if(event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    actions.goToReaderArea();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	    readerArea = new ReaderArea(luwrain, strings, this);

	    final ListParams listParams = new ListParams();
	    listParams.environment = treeParams.environment;
	    listParams.model = base.getNotesModel();
	    listParams.appearance = new DefaultListItemAppearance(listParams.environment);
	    listParams.clickHandler = (area, index, obj)->{return actions.onNotesClick(obj);};
	    listParams.name = strings.notesAreaName();

	    notesArea = new ListArea(listParams){
		    @Override public boolean onKeyboardEvent(KeyboardEvent event)
		    {
			NullCheck.notNull(event, "event");
			if (event.isSpecial() && !event.isModified())
			    switch(event.getSpecial())
			    {
			    case TAB:
				actions.goToTreeArea();
				return true;
			    }
			return super.onKeyboardEvent(event);
		    }
		    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		    {
			NullCheck.notNull(event, "event");
			switch(event.getCode())
			{
			case CLOSE:
			    actions.closeApp();
			    return true;
			default:
			    return super.onEnvironmentEvent(event);
			}
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
				return actions.returnFromInfoArea();
			    }
				return super.onKeyboardEvent(event);
		    }
		    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		    {
			NullCheck.notNull(event, "evet");
			switch(event.getCode())
			{
			case CLOSE:
			    actions.closeApp();
			    return true;
			default:
			    return super.onEnvironmentEvent(event);
			}
		    }
		};
    }

@Override public boolean onNotesClick(Object item)
    {
	return false;
    }

    @Override public boolean jumpByHref(String href)
    {
	return base.jumpByHref(readerArea, href);
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
	readerArea.setDocument(doc);
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	luwrain.say(doc.getTitle());
    }

    @Override public boolean openInNarrator()
    {
	base.openInNarrator();
	return true;
    }

    @Override public boolean openNew(boolean url)
    {
	base.openNew(url);
	return true;
    }

    @Override public boolean anotherFormat()
    {
	base.anotherFormat();
	return true;
    }

    @Override public boolean anotherCharset()
    {
	base.anotherCharset();
	return true;
    }


    @Override public boolean fetchingInProgress()
    {
	return base.fetchingInProgress();
    }

    @Override public boolean showDocInfo()
    {
	/*
	final Result currentDoc = base.currentDoc();
	if (currentDoc == null)
	    return false;
	infoArea.clear();
	base.prepareInfoText(currentDoc, infoArea);
	layouts.show(INFO_MODE_LAYOUT_INDEX);
	return true;
	*/
	return false;
    }

    @Override public void showErrorPage(Result res)
    {
	NullCheck.notNull(res, "res");
	infoArea.clear();
	base.prepareInfoText(res, infoArea);
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	layouts.show(INFO_MODE_LAYOUT_INDEX);
    }

    @Override public boolean docMode()
    {
	layouts.show(DOC_MODE_LAYOUT_INDEX);
	return true;
    }

    @Override public boolean bookMode()
    {
	layouts.show(BOOK_MODE_LAYOUT_INDEX);
	return true;
    }


    @Override public boolean returnFromInfoArea()
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

    @Override public void goToTreeArea()
    {
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return;
	luwrain.setActiveArea(treeArea);
    }

    @Override public void goToReaderArea()
    {
	luwrain.setActiveArea(readerArea);
    }

    @Override public void goToNotesArea()
    {
	if (layouts.getCurrentIndex() != BOOK_MODE_LAYOUT_INDEX)
	    return;
	luwrain.setActiveArea(notesArea);
    }
}
