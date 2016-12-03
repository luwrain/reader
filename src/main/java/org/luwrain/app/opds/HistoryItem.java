
package org.luwrain.app.opds;

import java.net.*;

import org.luwrain.core.*;

class HistoryItem
{
    final URL url;

    HistoryItem(URL url)  
    {
	NullCheck.notNull(url, "url");
	this.url = url;
    }
}
