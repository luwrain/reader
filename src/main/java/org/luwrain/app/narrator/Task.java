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

package org.luwrain.app.narrator;

import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.speech.*;

abstract class Task implements Runnable
{
    private Path path;
    private String text;
    private Channel channel;

    private Path currentFile;
    private OutputStream stream;
    private int fragmentNum = 1;

    Task(String text, Path path,
	 Channel channel)
    {
	this.text = text;
	this.path = path;
	this.channel = channel;
	NullCheck.notNull(text, "text");
	NullCheck.notNull(path, "path");
	//	NullCheck.notNull(channel, "channel");
    }

    abstract protected void progressLine(String text);

    @Override public void run()
    {
	try {
	    //	    openStream();
	    splitText();
	    //	    closeStream();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void splitText() throws IOException
    {
	StringBuilder b = new StringBuilder();
	for(int i = 0;i < text.length();++i)
	{
	    final char c = text.charAt(i);
	    if (Character.isISOControl(c))
	    {
		b.append(" ");
		continue;
	    }
	    if (c == '.' || c == '!' || c == '?')
	    {
		b.append(c);
		final String s = new String(b);
		b = new StringBuilder();
		if (s.length() > 1)
		    onNewPortion(s); 
		continue;
	    }
	    b.append(c);
	}
	final String s = new String(b);
	if (!s.isEmpty())
	    onNewPortion(s);
    }

    private void onNewPortion(String s) throws IOException
    {
	//	channel.synth(s, 0, 0, null, stream);
	progressLine(s);
	//	checkSize();
    }

    private void openStream() throws IOException
    {
    String fileName = "" + fragmentNum;
    ++fragmentNum;
    while(fileName.length() < 3)
	fileName = "0" + fileName;
    fileName = "book" + fileName;
    currentFile = path.resolve(fileName);
    stream = Files.newOutputStream(currentFile);
    }

    private void closeStream() throws IOException
    {
	stream.flush();
	stream.close();
    }

    private void checkSize() throws IOException
    {
	stream.flush();
	if (Files.size(currentFile) > 1048576 * 10)
	{
	    closeStream();
	    openStream();
	}

    }
}
