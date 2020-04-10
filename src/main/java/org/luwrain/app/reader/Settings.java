/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>
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

interface Settings
{
    static final String LOG_COMPONENT = "reader";
    static final String PROPERTIES_PATH = "/org/luwrain/app/reader/properties";
    static final String BOOKMARKS_PATH = "/org/luwrain/app/reader/bookmarks";

    interface Props
    {
	String getProps(String defValue);
	String setProps(String value);
    }

    interface Note
    {
	String getUrl(String defValue);
	int getPosition(int defValue);
	String getComment(String defValue);
	String getUniRef(String defValue);
	void setUrl(String value);
	void setPosition(int value);
	void setComment(String value);
	void setUniRef(String value);
    }

    static Note createNote(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Note.class);
    }


    static org.luwrain.app.reader.Note[] getNotes(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	registry.addDirectory(BOOKMARKS_PATH);
	//FIXME:
	return new org.luwrain.app.reader.Note[0];
    }

    static void addNote(Registry registry, String url,
			String noteUrl, int notePos, String noteComment, String noteUniRef)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	NullCheck.notNull(noteUrl, "noteUrl");
	NullCheck.notNull(noteComment, "noteComment");
	NullCheck.notNull(noteUniRef, "noteUniRef");
	registry.addDirectory(BOOKMARKS_PATH);
	//FIXME:
	    }

    static void deleteNote(Registry registry, String url, int num)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	registry.addDirectory(BOOKMARKS_PATH);
	//FIXME:
    }

    static void addNoteImpl(Registry registry, String path, String noteUrl, int notePos,
				    String noteComment, String noteUniRef)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	NullCheck.notNull(noteUrl, "noteurl");
	NullCheck.notNull(noteComment, "noteComment");
	NullCheck.notNull(noteUniRef, "noteUniRef");
	    final String notesPath = Registry.join(path, "notes");
	    registry.addDirectory(notesPath);
	    final int num = Registry.nextFreeNum(registry, notesPath);
	    final String notePath = Registry.join(notesPath, "" + num);
	    registry.addDirectory(notePath);
	    final Note note = createNote(registry, notePath);
	    note.setUrl(noteUrl);
	    note.setPosition(notePos);
	    note.setComment(noteComment);
	    note.setUniRef(noteUniRef);
    }

    static org.luwrain.app.reader.Note[] getNotesImpl(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	registry.addDirectory(path);
	final LinkedList<org.luwrain.app.reader.Note> res = new LinkedList<org.luwrain.app.reader.Note>();
	for(String p: registry.getDirectories(path))
	{
	    if (p.isEmpty())
		continue;
	    final Note note = createNote(registry, Registry.join(path, p));
	    final int num;
	    try {
		num = Integer.parseInt(p);
	    }
	    catch(NumberFormatException e)
	    {
		Log.warning("reader", "registry directory " + path + " contains entries with illegal names");
		continue;
	    }
	    res.add(new org.luwrain.app.reader.Note(num, note));
	}
	return res.toArray(new org.luwrain.app.reader.Note[res.size()]);
    }

    static Props createProperties(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Props.class);
    }

    
    static Properties decodeProperties(String value)
    {
	NullCheck.notNull(value, "value");
	final StringReader r = new StringReader(value);
	final Properties props = new Properties();
	try {
	    props.load(r);
	    return props;
	}
	catch(IOException e)
	{
	    Log.warning(LOG_COMPONENT, "unable to decode reader properties:" + e.getClass().getName() + ":" + e.getMessage());
	    return new Properties();
	}
    }

    static String encodeProperties(Properties props)
    {
	NullCheck.notNull(props, "props");
	final StringWriter w = new StringWriter();
	try {
	    try {
		props.store(w, "");
	    }
	    finally {
		w.flush();
	    }
	    return w.toString();
	}
	catch(IOException e)
	{
	    Log.warning(LOG_COMPONENT, "unable to save the properties:" + e.getClass().getName() + ":" + e.getMessage());
	    return "";
	}
    }

}
