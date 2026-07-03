// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;

import static java.nio.file.Files.*;
import static java.util.Objects.*;
import static org.luwrain.util.LineIterator.*;
import static org.luwrain.util.FileUtils.*;

final class StandaloneSettings  implements Settings
{
    static private final String
	LOG_COMPONENT = App.LOG_COMPONENT,
	REPO_FILE = "repo.json",
	ATTRS_FILE = "attrs.json",
	UTF_8 = "UTF-8";

    private final File dir;

    StandaloneSettings(File dir)
    {
	requireNonNull(dir, "dir can't be null");
	this.dir = dir;
    }

    @Override public String getLocalRepoMetadata(String defValue)
    {
	final File repoFile = new File(dir, REPO_FILE);
	try {
	    createDirectories(dir.toPath());
	    return join(repoFile, UTF_8, System.lineSeparator());
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
	final File repoFile = new File(dir, REPO_FILE);
	try {
	    createDirectories(dir.toPath());
	    writeTextFile(repoFile, value, "UTF-8");
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
	    createDirectories(dir.toPath());
	    return join(attrsFile, UTF_8, System.lineSeparator());
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
	requireNonNull(value, "value can't be null");
	final File attrsFile = new File(dir, ATTRS_FILE);
	try {
	    createDirectories(dir.toPath());
	    writeTextFile(attrsFile, value, UTF_8);
	}
	catch(IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to write the local attributes file  " + attrsFile.getAbsolutePath() + ": " + e.getClass().getName() + ": " + e.getMessage());
	    e.printStackTrace();
	}
    }
}
