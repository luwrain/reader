/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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
import org.luwrain.reader.*;

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
	lines.update((text)->{
	text.addLine("");
	text.addLine(strings.propertiesAreaUrl(doc.getProperty("url")));
	text.addLine(strings.propertiesAreaContentType(doc.getProperty("contenttype")));
	//	lines.addLine(strings.propertiesAreaCharset(item.charset));
	text.addLine("");
	    });
	return true;
    }
}
