package org.luwrain.app.reader;

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.player.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;

class AudioPlaying  implements Listener
{
    private final Luwrain luwrain;
    private final Player player;

    private ReaderArea area = null;
    private Run prevRun = null;
    private Book book = null;
    private Doc doc = null;
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

    boolean playAudio(Book book, Doc doc, ReaderArea area, String[] ids)
    {
	NullCheck.notNull(book, "book");
	NullCheck.notNull(doc, "doc");
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	final String urlStr = doc.getProperty(Doc.PROP_URL);
	if (urlStr == null || urlStr.isEmpty())
	    return false;
	URL url = null;
	try {
	    url = new URL(urlStr);
	}
	catch(MalformedURLException e)
	{
	    return false;
	}
	for(String id: ids)
	{
	    final AudioFragment audioInfo = book.findAudioForId(url.toString() + "#" + id);
	    if (audioInfo != null)
	    {
		final URL audioFileUrl;
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
		this.currentPlaylist = new FixedPlaylist(audioFileUrl.toString());
		//luwrain.playSound(Sounds.PLAYING);
		player.play(currentPlaylist, 0, audioInfo.beginPosMsec(), Player.DEFAULT_FLAGS);
		luwrain.silence();
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
	return true;
    }

    @Override public void onTrackTime(Playlist playlist, int trackNum,  long msec)
    {
	NullCheck.notNull(playlist, "playlist");
	if (doc == null || book == null || area == null)
	    return;
	final String urlStr = doc.getProperty(Doc.PROP_URL);
	if (urlStr == null || urlStr.isEmpty())
	    return;
	if (playlist != currentPlaylist)
	    return;
	if (trackNum >= playlist.getTrackCount())
	    return;
	final String track = playlist.getTrackUrl(trackNum);
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
	    return;
	}
	try {
	    if (!docUrl.equals(new URL(urlStr)))
		return;
	}
	catch(MalformedURLException e)
	{
	    return;
	}
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

    static private final class AudioFollowingVisitor extends Visitor
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
	@Override public void visit(Heading h)
	{
	}
	private void checkRun(Run run)
	{
	    if (resultingRun != null)
		return;
	    final Attributes attrs = run.getAttrs();
	    if (attrs != null && attrs.hasIdWithParents(desiredId))
		this.resultingRun = run;
	}
	Run result()
	{
	    return this.resultingRun;
	}
    }
}
