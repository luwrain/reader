
package org.luwrain.app.opds;

import org.luwrain.core.*;

class RemoteLibrary implements Comparable
{
    final String title;
    final String url;

    RemoteLibrary(Registry registry, String regPath)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(regPath, "regPath");
	final Settings sett = RegistryProxy.create(registry, regPath, Settings.class);
	title = sett.getTitle("");
	url = sett.getUrl("");
    }

    @Override public String toString()
    {
	return !title.trim().isEmpty()?title:url;
    }

    @Override public int compareTo(Object o)
    {
	if (o == null || !(o instanceof RemoteLibrary))
	    return 0;
	return title.compareTo(((RemoteLibrary)o).title);
    }

    interface Settings
    {
	String getTitle(String defValue);
	void setTitle(String value);
	String getUrl(String defValue);
	void setUrl(String value);
    }
    }
