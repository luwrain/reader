
package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;

interface Settings
{
    static final String BOOKMARKS_PATH = "/org/luwrain/app/reader/bookmarks";

    interface Bookmark
    {
	String getUrl(String defValue);
	int getPosition(int defValue);
	void setUrl(String value);
	void setPosition(int value);
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

    static Bookmark createBookmark(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Bookmark.class);
    }

    static Note createNote(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Note.class);
    }

    static int getBookmark(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	registry.addDirectory(BOOKMARKS_PATH);
	for(String p: registry.getDirectories(BOOKMARKS_PATH))
	{
	    if (p.isEmpty())
		continue;
	    final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, p));
	    if (bookmark.getUrl("").equals(url))
		return bookmark.getPosition(-1);
	}
	return -1;
    }

    static void setBookmark(Registry registry, String url, int pos)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	registry.addDirectory(BOOKMARKS_PATH);
	for(String p: registry.getDirectories(BOOKMARKS_PATH))
	{
	    if (p.isEmpty())
		continue;
	    final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, p));
	    if (bookmark.getUrl("").equals(url))
	    {
		bookmark.setPosition(pos);
		return;
	    }
	}
	final int next = Registry.nextFreeNum(registry, BOOKMARKS_PATH);
	final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, ""+next));
	bookmark.setUrl(url);
	bookmark.setPosition(pos);
    }

    static org.luwrain.app.reader.Note[] getNotes(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	registry.addDirectory(BOOKMARKS_PATH);
	for(String p: registry.getDirectories(BOOKMARKS_PATH))
	{
	    if (p.isEmpty())
		continue;
	    final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, p));
	    if (bookmark.getUrl("").equals(url))
		return getNotesImpl(registry, Registry.join(Registry.join(BOOKMARKS_PATH, p), "notes"));
	}
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
	for(String p: registry.getDirectories(BOOKMARKS_PATH))
	{
	    if (p.isEmpty())
		continue;
	    final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, p));
	    if (bookmark.getUrl("").equals(url))
	    {
		addNoteImpl(registry, Registry.join(BOOKMARKS_PATH, p), noteUrl, notePos, noteComment, noteUniRef);
	    return;
	    }
	}
	final int num = Registry.nextFreeNum(registry, BOOKMARKS_PATH);
	final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, "" + num));
	bookmark.setUrl(url);
	bookmark.setPosition(0);
	addNoteImpl(registry, Registry.join(BOOKMARKS_PATH, "" + num), noteUrl, notePos, noteComment, noteUniRef);
    }

    static void deleteNote(Registry registry, String url, int num)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	registry.addDirectory(BOOKMARKS_PATH);
	for(String p: registry.getDirectories(BOOKMARKS_PATH))
	{
	    if (p.isEmpty())
		continue;
	    final Bookmark bookmark = createBookmark(registry, Registry.join(BOOKMARKS_PATH, p));
	    if (!bookmark.getUrl("").equals(url))
		continue;
	    final String pp = Registry.join(Registry.join(Registry.join(BOOKMARKS_PATH, p), "notes"), "" + num);
	    registry.deleteDirectory(pp);
	    return;
	}
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
}