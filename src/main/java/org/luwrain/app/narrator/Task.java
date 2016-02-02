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

package org.luwrain.app.narrator;

import java.io.*;
import java.nio.file.*;
import javax.sound.sampled.AudioFormat;

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
    private AudioFormat chosenFormat = null;
    private String compressorCmd = "";

    Task(String text, Path path,
	 String compressorCmd, Channel channel)
    {
	this.text = text;
	this.path = path;
	this.compressorCmd = compressorCmd;
	this.channel = channel;
	NullCheck.notNull(text, "text");
	NullCheck.notNull(path, "path");
	NullCheck.notNull(compressorCmd, "compressorCmd");
	NullCheck.notNull(channel, "channel");
    }

    abstract protected void progressLine(String text);

    @Override public void run()
    {
	try {
	    AudioFormat[] formats = channel.getSynthSupportedFormats();
	    if (formats == null || formats.length < 0)
	    {
		progressLine("Отсутствуют поддерживаемые форматы");//FIXME:
		return;
	    }
	    chosenFormat = formats[0];
	    openStream();
	    splitText();
	    closeStream();
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
	channel.synth(s, 0, 0, chosenFormat, stream);
checkSize();
    }

    private void openStream() throws IOException
    {
	currentFile = Files.createTempFile("lwrnarrator", "");
	Log.debug("narrator", "opening a temporary stream on " + currentFile.toString());
    stream = Files.newOutputStream(currentFile);
    }

    private void closeStream() throws IOException
    {
	stream.flush();
	stream.close();
	stream = null;
	String fileName = "" + fragmentNum;
	++fragmentNum;
	while(fileName.length() < 3)
	    fileName = "0" + fileName;
	fileName += ".mp3";
	Path compressedFile = path.resolve(fileName);
	progressLine("Compressing " + compressedFile.toString());
	callCompressor(currentFile, compressedFile);
	Log.debug("narrator", "deleting temporary file " + currentFile.toString());
	Files.delete(currentFile);
	currentFile = null;


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

    private void callCompressor(Path inputFile, Path outputFile)
    {
	Log.debug("narrator", "calling a compressor (" + compressorCmd + ") " + inputFile.toString() + "->" + outputFile.toString());
	try {
	    final Process p = new ProcessBuilder(compressorCmd, inputFile.toString(), outputFile.toString()).start();
	    p.waitFor();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	    progressLine(e.getMessage());
	}
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	}
    }
}
