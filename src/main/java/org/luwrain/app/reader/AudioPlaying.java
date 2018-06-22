/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import org.luwrain.doctree.*;
import org.luwrain.controls.doctree.*;

class AudioPlaying  implements Listener
{
    private Luwrain luwrain;
    private Player player;

    private DocumentArea area;
    private Run prevRun;
    private Book book;
    private Document doc;
    private Playlist currentPlaylist = null;

    boolean init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.player = luwrain.getPlayer();
	if (player == null)
	    return false;
	    player.addListener(this);
					  return true;
    }

    boolean playAudio(Book book, Document doc,
		      DocumentArea area, String[] ids)
    {
	NullCheck.notNull(book, "book");
	NullCheck.notNull(doc, "doc");
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	if (doc.getUrl() == null)
	    return false;
    final URL url = doc.getUrl();
	    for(String id: ids)
	    {
		final AudioInfo audioInfo = book.findAudioForId(url.toString() + "#" + id);
		if (audioInfo != null)
		{
		    Log.debug("reader", "audio info found:" + audioInfo.src() + " from " + audioInfo.beginPosMsec());
		    URL audioFileUrl = null;
		    try {
			audioFileUrl = new URL(url, audioInfo.src());
		    }
		    catch(MalformedURLException e)
		    {
			Log.error("reader", "unable to prepare the URL for player:" + audioInfo.src() + ":" + e.getMessage());
			continue;
		    }
		    this.book = book;
		    this.doc = doc;
		    this.area = area;
		    this.currentPlaylist = new Playlist(audioFileUrl.toString());
		    player.play(currentPlaylist, 0, audioInfo.beginPosMsec(), Player.DEFAULT_FLAGS);
		    return true;
		}
	    }
	    return false;
    }

    void stop()
    {
	player.stop();
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
	if (playlist.getPlaylistUrls() == null || trackNum >= playlist.getPlaylistUrls().length)
	    return;
	final String track = playlist.getPlaylistUrls()[trackNum];
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
	    Log.debug("reader", "unable to parse the URL to find the text for new playing time:" + link + ":" + e.getMessage());
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
    
    static private class  AudioFollowingVisitor implements Visitor
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
	    if (para.runs != null)
		for(Run r: para.runs)
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
