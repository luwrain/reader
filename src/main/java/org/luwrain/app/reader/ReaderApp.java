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
    private Strings strings;
    private ReaderArea area;
    private Document doc = null;
    private final DocInfo docInfo = new DocInfo();

    public ReaderApp()
    {
    }

    public ReaderApp(int type, String file)
    {
	NullCheck.notNull(file, "file");
	docInfo.type = type;
docInfo.fileName = file;
    }

    public ReaderApp(int type, String file, String format)
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(format, "format");
	docInfo.type = type;
	docInfo.fileName = file;
	docInfo.format = DocInfo.formatByStr(format);
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	return processArgs();
    }

    private boolean processArgs()
    {
	if (docInfo.fileName == null || docInfo.fileName.isEmpty())
	{
	    area = new ReaderArea(luwrain, strings, this, null, docInfo);
	    return true;
	}
	if (docInfo.type == DocInfo.LOCAL)
	{
	    if (docInfo.format == Factory.UNRECOGNIZED)
		docInfo.format = Factory.suggestFormat(docInfo.fileName);
	    if (docInfo.format == Factory.UNRECOGNIZED)
		docInfo.format = DocInfo.DEFAULT_FORMAT;
	    doc = Factory.loadFromFile(docInfo.format, docInfo.fileName, luwrain.getScreenWidth() - 2, docInfo.charset);
	    if (doc == null)
	    {
		luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
		return false;
	    }
	    area = new ReaderArea(luwrain, strings, this, doc, docInfo);
	    return true;
	}
	if (docInfo.type == DocInfo.URL)
	{
	    try {
		area = new ReaderArea(luwrain, strings, this, null, docInfo);
		new Thread(new FetchThread(luwrain, area, new URL(docInfo.fileName))).start();
	    }
	    catch (MalformedURLException e)
	    {
		e.printStackTrace();
		luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
		return false;
	    }
	    return true;
	}
	return false;
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
