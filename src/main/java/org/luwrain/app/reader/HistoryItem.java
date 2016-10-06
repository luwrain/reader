
package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class HistoryItem
{
    final String url;
    final String contentType;
    final String format;
    final String charset;
    int startingRowIndex;
    int lastRowIndex;

    HistoryItem(Document doc)
    {
	NullCheck.notNull(doc, "doc");
	url = doc.getProperty("url");
	contentType = doc.getProperty("contenttype");
	charset = doc.getProperty("charset");
	format = doc.getProperty("format");
    }
}
