
package org.luwrain.app.wiki;

import org.luwrain.core.*;

class Page
{
    final String lang;
    final String title;
    final String comment;

    Page(String lang, String title, String comment)
    {
	NullCheck.notNull(lang, "lang");
	NullCheck.notNull(title, "title");
	NullCheck.notNull(comment, "comment");
	this.lang = lang;
	this.title = title;
	this.comment = comment;
    }

    @Override public String toString()
    {
	if (comment.trim().isEmpty())
	    return title;
	return title + ", " + comment;
    }
}
