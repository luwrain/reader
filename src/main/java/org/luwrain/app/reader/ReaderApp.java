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
    static private final String STRINGS_NAME = "luwrain.reader";

    static private final int DOC_ONLY_LAYOUT_INDEX = 0;
    static private final int INFO_LAYOUT_INDEX = 1;

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private ReaderArea readerArea;
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
	layouts.add(new AreaLayout(infoArea));
	    if (docInfo != null)
		base.fetch(readerArea, docInfo); else
	    docInfo = new DocInfo();
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	    readerArea = new ReaderArea(luwrain, strings, this);

	    infoArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){
		    @Override public boolean onKeyboardEvent(KeyboardEvent event)
		    {
			NullCheck.notNull(event, "event");
			if (event.isCommand() && !event.isModified())
			    switch(event.getCommand())
			    {
			    case KeyboardEvent.ESCAPE:
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

    @Override public boolean jumpByHref(String href)
    {
	return base.jumpByHref(readerArea, href);
    }

    @Override public void onNewDocument(Result res)
    {
	base.acceptNewCurrentDoc(res);
    }

    @Override public void openInNarrator()
    {
	base.openInNarrator();
    }

    @Override public boolean fetchingInProgress()
    {
	return base.fetchingInProgress();
    }

    @Override public boolean showDocInfo()
    {
	final Result currentDoc = base.currentDoc();
	if (currentDoc == null)
	    return false;
	infoArea.clear();
	base.prepareInfoText(currentDoc, infoArea);
	layouts.show(INFO_LAYOUT_INDEX);
	return true;
    }

    @Override public void showErrorPage(Result res)
    {
	NullCheck.notNull(res, "res");
	infoArea.clear();
	base.prepareInfoText(res, infoArea);
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	layouts.show(INFO_LAYOUT_INDEX);
    }

    @Override public boolean returnFromInfoArea()
    {
	final Result currentDoc = base.currentDoc();
	if (currentDoc == null)
	    return false;
	layouts.show(DOC_ONLY_LAYOUT_INDEX);
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
