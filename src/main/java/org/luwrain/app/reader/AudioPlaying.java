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

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.player.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;

class AudioPlaying  implements Listener
{
    static private final String LOG_COMPONENT = Base.LOG_COMPONENT;

    private final Luwrain luwrain;
    private final Player player;

    private ReaderArea area = null;
    private Run prevRun = null;
    private Book book = null;
    private Document doc = null;
    private Playlist currentPlaylist = null;

    AudioPlaying(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.player = luwrain.getPlayer();
	if (player != null)
	    player.addListener(this);
    }

    boolean isLoaded()
    {
	return player != null;
    }

    boolean playAudio(Book book, Document doc, ReaderArea area, String[] ids)
    {
	NullCheck.notNull(book, "book");
	NullCheck.notNull(doc, "doc");
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	final URL url = doc.getUrl();
	if (url == null)
	    return false;
	for(String id: ids)
	{
	    final AudioFragment audioInfo = book.findAudioForId(url.toString() + "#" + id);
	    if (audioInfo != null)
	    {
		URL audioFileUrl = null;
		try {
		    audioFileUrl = new URL(url, audioInfo.src);
		}
		catch(MalformedURLException e)
		{
		    continue;
		}
		this.book = book;
		this.doc = doc;
		this.area = area;
		this.currentPlaylist = new Playlist(audioFileUrl.toString());
		luwrain.playSound(Sounds.PLAYING);
		player.play(currentPlaylist, 0, audioInfo.beginPosMsec(), Player.DEFAULT_FLAGS, null);
		return true;
	    }
	}
	return false;
    }

    boolean stop()
    {
	if (player.getState() != Player.State.PLAYING || this.currentPlaylist != player.getPlaylist())
	    return false;
	player.stop();
	luwrain.playSound(Sounds.PLAYING);
	return true;
    }

    @Override public void onTrackTime(Playlist playlist, int trackNum,  long msec)
    {
	NullCheck.notNull(playlist, "playlist");
	if (doc == null || book == null || area == null)
	    return;
	if (doc.getUrl() == null)
	    return;
	if (playlist != currentPlaylist)
	    return;
	if (trackNum >= playlist.getTrackCount())
	    return;
	final String track = playlist.getTrack(trackNum);
	final String link = book.findTextForAudio(track, msec);
	if (link == null)
	    return;
	URL url = null;
	URL docUrl = null;
	try {
	    url = new URL(link);
	    docUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
	}
	catch(MalformedURLException e)
	{
	    Log.debug(LOG_COMPONENT, "unable to parse the URL to find the text for new playing time:" + link + ":" + e.getMessage());
	    return;
	}
	if (!doc.getUrl().equals(docUrl))
	    return;
	if (url.getRef().isEmpty())
	    return;
	final AudioFollowingVisitor visitor = new AudioFollowingVisitor(url.getRef());
	Visitor.walk(doc.getRoot(), visitor);
	final Run resultingRun = visitor.result();
	if (resultingRun == null || prevRun == resultingRun)
	    return;
	luwrain.runUiSafely(()->area.findRun(resultingRun));
	prevRun = resultingRun;
    }

    @Override public void onNewPlaylist(Playlist playlist)
    {
	if (playlist != null && playlist != currentPlaylist)
	    onPlayerStop();
    }

    @Override public void onNewTrack(Playlist playlist, int trackNum)
    {
    }

    @Override public void onNewState(org.luwrain.player.Playlist playlist, org.luwrain.player.Player.State state)
    {
	NullCheck.notNull(state, "state");
	if (playlist != currentPlaylist)
	    return;
	if (state == org.luwrain.player.Player.State.STOPPED)
	    onPlayerStop();
    }

    @Override public void onPlayingError(org.luwrain.player.Playlist playlist, Exception error )
    {
    }

    private void onPlayerStop()
    {
	currentPlaylist = null;
	book = null;
	doc = null;
	area = null;
	prevRun = null;
    }

    static private final class  AudioFollowingVisitor implements Visitor
    {
	private String desiredId;
	private Run resultingRun = null;

	AudioFollowingVisitor(String desiredId)
	{
	    NullCheck.notEmpty(desiredId, "desiredId");
	    this.desiredId = desiredId;
	}

	@Override public void visit(Paragraph para)
	{
	    NullCheck.notNull(para, "para");
	    if (resultingRun != null)
		return;
	    for(Run r: para.getRuns())
		    checkRun(r);
	}

	@Override public void visitNode(Node node)
	{
	}

	@Override public void visit(ListItem node)
	{
	}

	@Override public void visit(Section node)
	{
	}

	@Override public void visit(TableCell node)
	{
	}

	@Override public void visit(Table node)
	{
	}

	@Override public void visit(TableRow node)
	{
	}

	private void checkRun(Run run)
	{
	    if (resultingRun != null)
		return;
	    ExtraInfo info = run.extraInfo();
	    while (info != null)
	    {
		if (info.attrs.containsKey("id") && info.attrs.get("id").equals(desiredId))
		{
		    resultingRun = run;
		    return;
		}
		info = info.parent;
	    }
	}
	Run result()
	{
	    return resultingRun;
	}
    }
}
