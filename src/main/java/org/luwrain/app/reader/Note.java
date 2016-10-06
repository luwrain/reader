
package org.luwrain.app.reader;

import org.luwrain.core.*;

class Note
{
    final int num;
    final Settings.Note sett;
    final String url;
    final int position;
    final String comment;
    final String uniRef;

    Note(int num, Settings.Note sett)
    {
	NullCheck.notNull(sett, "sett");
	this.num = num;
	this.sett = sett;
	this.url = sett.getUrl("");
	this.position = sett.getPosition(0);
	this.comment = sett.getComment("");
	this.uniRef = sett.getUniRef("");
    }

    @Override public String toString()
    {
	return comment;
    }
}
