package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.*;

final class DocProps
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Doc doc;

    DocProps(Luwrain luwrain, Strings strings, Doc doc)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(doc, "doc");
	this.luwrain = luwrain;
	this.strings = strings;
	this.doc = doc;
    }

    boolean fillProperties(MutableLines lines)
    {
	NullCheck.notNull(lines, "lines");
	lines.update((text)->{
		text.add("");
		text.add(strings.propertiesAreaUrl(doc.getProperty(Doc.PROP_URL)));
		text.add(strings.propertiesAreaContentType(doc.getProperty("contenttype")));
		text.add("");
	    });
	return true;
    }
}
