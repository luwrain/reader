// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.net.*;

import org.luwrain.core.NullCheck;

import static java.util.Objects.*;

public final class AudioFragment
{
    public final String src;
    public final long beginPos;
    public final long endPos;

    public AudioFragment(String src, long beginPos)
    {
	requireNonNull(src, "src can't be null");
	this.src = src;
	this.beginPos = beginPos;
	this.endPos = -1;
    }

    public AudioFragment(String src)
    {
	requireNonNull(src, "src can't be null");
	this.src = src;
	this.beginPos = -1;
	this.endPos = -1;
    }

    public AudioFragment(String src, long beginPos, long endPos)
    {
	requireNonNull(src, "src can't be null");
	this.src = src;
	this.beginPos = beginPos;
	this.endPos = endPos;
    }

    public long beginPosMsec() {return beginPos;}
    public long endPosMsec() {return endPos;}

    public boolean covers(String audioFileUrl, long msec)
    {
	if (!src.equals(audioFileUrl))
	    return false;
	if (endPos < 0)
	    return msec >= beginPos;
	return msec >= beginPos && msec <= endPos;
    }

    public boolean covers(String audioFileUrl, long msec, URL baseUrl)
    {
	requireNonNull(baseUrl, "baseUrl can't be null");
	try {
	    if (!(new URL(baseUrl, src).toString()).equals(new URL(baseUrl,audioFileUrl).toString()))
		return false;
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	    return false;
	}
	if (endPos < 0)
	    return msec >= beginPos;
	return msec >= beginPos && msec <= endPos;
    }

    @Override public String toString()
    {
	return "Audio: " + src + " (from " + beginPos + ", to " + endPos + ")";
    }
}
