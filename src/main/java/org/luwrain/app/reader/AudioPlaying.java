
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

    private DoctreeArea area;
    private Run prevRun;
    private Book book;
    private Document doc;
    private Playlist currentPlaylist = null;

    boolean init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
	if (player == null)
	    return false;
	    player.addListener(this);
					  return true;
    }

    boolean playAudio(Book book, Document doc,
		      DoctreeArea area, String[] ids)
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
		    this.currentPlaylist = new SingleLocalFilePlaylist(audioFileUrl.toString());
		    player.play(currentPlaylist, 0, audioInfo.beginPosMsec());
		    return true;
		}
	    }
	    return false;
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
	if (playlist.getPlaylistItems() == null || trackNum >= playlist.getPlaylistItems().length)
	    return;
	final String track = playlist.getPlaylistItems()[trackNum];
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
	luwrain.runInMainThread(()->area.findRun(resultingRun));
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

    @Override public void onPlayerStop()
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
