/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
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

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import static org.luwrain.util.FileUtils.*;

final class StandaloneSettings  implements Settings
{
    static private final String
	LOG_COMPONENT = App.LOG_COMPONENT,
	REPO_FILE = "repo.json",
	ATTRS_FILE = "attrs.json";

    private final File dir;

    StandaloneSettings(File dir)
    {
	NullCheck.notNull(dir, "dir");
	this.dir = dir;
    }

    @Override public String getLocalRepoMetadata(String defValue)
    {
	final File repoFile = new File(dir, REPO_FILE);
	try {
	    createSubdirs(dir);
	    return readTextFileSingleString(repoFile, "UTF-8");
	}
	catch(IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to read the local repo file " + repoFile.getAbsolutePath() + ": " + e.getClass().getName() + ": " + e.getMessage());
	    e.printStackTrace();
	    return defValue;
	}
    }

    @Override public void setLocalRepoMetadata(String value)
    {
	NullCheck.notNull(value, "value");
	final File repoFile = new File(dir, REPO_FILE);
	try {
	    createSubdirs(dir);
	    writeTextFileSingleString(repoFile, value, "UTF-8");
	}
	catch(IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to write the local repo file  " + repoFile.getAbsolutePath() + ": " + e.getClass().getName() + ": " + e.getMessage());
	    e.printStackTrace();
	}
    }

    @Override public String getAttributes(String defValue)
    {
	final File attrsFile = new File(dir, ATTRS_FILE);
	try {
	    createSubdirs(dir);
	    return readTextFileSingleString(attrsFile, "UTF-8");
	}
	catch(IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to read the local attributes file " + attrsFile.getAbsolutePath() + ": " + e.getClass().getName() + ": " + e.getMessage());
	    e.printStackTrace();
	    return defValue;
	}
    }

    @Override public void setAttributes(String value)
    {
	NullCheck.notNull(value, "value");
	final File attrsFile = new File(dir, ATTRS_FILE);
	try {
	    createSubdirs(dir);
	    writeTextFileSingleString(attrsFile, value, "UTF-8");
	}
	catch(IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to write the local attributes file  " + attrsFile.getAbsolutePath() + ": " + e.getClass().getName() + ": " + e.getMessage());
	    e.printStackTrace();
	}
    }
}
