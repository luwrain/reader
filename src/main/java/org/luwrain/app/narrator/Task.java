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
    private Strings strings;
    private Path path;
    private String text;
    private Channel channel;
    private String compressorCmd = "";

    private Path currentFile;
    private OutputStream stream;
    private int fragmentNum = 1;
    private AudioFormat chosenFormat = null;
    private int lastPercents = 0;

    Task(Strings strings, String text, Path path,
	 String compressorCmd, Channel channel)
    {
	this.strings = strings;
	this.text = text;
	this.path = path;
	this.compressorCmd = compressorCmd;
	this.channel = channel;
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(text, "text");
	NullCheck.notNull(path, "path");
	NullCheck.notNull(compressorCmd, "compressorCmd");
	NullCheck.notNull(channel, "channel");
    }

    abstract protected void progressLine(String text, boolean doneMessage);

    @Override public void run()
    {
	try {
	    AudioFormat[] formats = channel.getSynthSupportedFormats();
	    if (formats == null || formats.length < 0)
	    {
		progressLine(strings.noSupportedAudioFormats(), false);
		return;
	    }
	    chosenFormat = formats[0];
	    openStream();
	    splitText();
	    closeStream();
	    progressLine(strings.done(), true);
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
	    final int percents = (i * 100) / text.length();
	    if (percents > lastPercents)
	    {
		progressLine("" + percents + "%", false);
		lastPercents = percents;
	    }
	    final char c = text.charAt(i);
	    final char cc = (i + 1 < text.length())?text.charAt(i + 1):'\0';
	    if (c == '\n' && cc == '#')
	    {
		int k = i + 1;
		while(k < text.length() && text.charAt(k) != '\n')
		    ++k;
		final String s = new String(b);
		if (k >= text.length())//If the line with hash command is the last one, skipping it
		    break;
		if (k > i + 1 && onHashCmd(s, text.substring(i + 1, k)))
		{
		    b = new StringBuilder();
		    i = k;
		    continue;
		}
	    }
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
		    onNewPortion(s, true);
		continue;
	    }
	    b.append(c);
	}
	final String s = new String(b);
	if (!s.isEmpty())
	    onNewPortion(s, true);
    }

    private void onNewPortion(String s, boolean commit) throws IOException
    {
	channel.synth(s, 0, 0, chosenFormat, stream);
	if (commit)
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
	progressLine(strings.compressing(compressedFile.toString()), false);
	callCompressor(currentFile, compressedFile);
	Log.debug("narrator", "deleting temporary file " + currentFile.toString());
	Files.delete(currentFile);
	currentFile = null;
    }

    private void checkSize() throws IOException
    {
	stream.flush();
	if (Files.size(currentFile) > timeToBytes(300000))//5 min
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
	    progressLine(e.getMessage(), false);
	}
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	}
    }

    private void silence(int delay) throws IOException
    {
	final int numBytes = timeToBytes(delay);
	Log.debug("narrator", "writing a silence of " + numBytes + " bytes");
	final byte[] buf = new byte[numBytes];
	for(int i = 0;i < buf.length;++i)
	    buf[i] = 0;
	stream.write(buf);
    }

    private boolean onHashCmd(String uncommittedText, String cmd) throws IOException
    {
	if (cmd.length() < 2)
	    return false;
	final String body = cmd.substring(1);
try {
	    final int delay = Integer.parseInt(body);
	    if (delay > 100 && delay < 100000)
	    {
		onNewPortion(uncommittedText, false);
		silence(delay);
	    return true;
	    } else
		return false;
}
	    catch (NumberFormatException e)
	    { return false; }
    }

    private int timeToBytes(int msec)
    {
	float value = chosenFormat.getSampleRate() * chosenFormat.getSampleSizeInBits() * chosenFormat.getChannels();//bits in a second
	value /= 8;//bytes in a second
	value /= 1000;//bytes in millisecond
	return (int)(value * msec);
    }
}
