
package org.luwrain.app.opds;

import org.luwrain.core.*;

class PropertiesItem
{
    final String url;
    final String contentType;

    PropertiesItem(String url, String contentType)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	this.url = url;
	this.contentType = contentType;
    }

    @Override public String toString()
    {
	if (contentType.isEmpty())
	    return url;
	return url + " (" + contentType + ")";
    }
}
