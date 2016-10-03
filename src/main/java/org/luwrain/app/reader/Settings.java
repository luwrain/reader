
package org.luwrain.app.reader;

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



}
