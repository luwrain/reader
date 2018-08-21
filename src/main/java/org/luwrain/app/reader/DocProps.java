/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;

final class DocProps
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Document doc;

    DocProps(Luwrain luwrain, Strings strings, Document doc)
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
	lines.beginLinesTrans();
	lines.addLine("");
	lines.addLine(strings.propertiesAreaUrl(doc.getProperty("url")));
	lines.addLine(strings.propertiesAreaContentType(doc.getProperty("contenttype")));
	//	lines.addLine(strings.propertiesAreaCharset(item.charset));
	lines.addLine("");
	lines.endLinesTrans();
	return true;
    }
}
