/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import org.luwrain.doctree.*;

public class ReaderApp implements Application, Actions
{
    static private final String STRINGS_NAME = "luwrain.reader";

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private ReaderArea area;
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
	    area = new ReaderArea(luwrain, strings, this);
	    if (docInfo != null)
		base.fetch(area, docInfo);
	    docInfo = new DocInfo();
	return true;
    }

    @Override public boolean jumpByHref(String href)
    {
	return base.jumpByHref(area, href);
    }

    @Override public void onNewDocument(Document doc)
    {
	base.acceptNewCurrentDoc(doc);
    }

    @Override public void openInNarrator()
    {
	base.openInNarrator();
    }

    @Override public boolean fetchingInProgress()
    {
	return base.fetchingInProgress();
    }

    @Override public String getAppName()
    {
	return area != null?area.getAreaName():strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}
