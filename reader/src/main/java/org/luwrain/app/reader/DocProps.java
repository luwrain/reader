// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.*;

import static java.util.Objects.*;

final class DocProps
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Doc doc;

    DocProps(Luwrain luwrain, Strings strings, Doc doc)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(strings, "strings can't be null");
	requireNonNull(doc, "doc can't be null");
	this.luwrain = luwrain;
	this.strings = strings;
	this.doc = doc;
    }

    boolean fillProperties(MutableLines lines)
    {
	requireNonNull(lines, "lines can't be null");
	lines.update((text)->{
		text.add("");
		text.add(strings.propertiesAreaUrl(doc.getProperty(Doc.PROP_URL)));
		text.add(strings.propertiesAreaContentType(doc.getProperty("contenttype")));
		text.add("");
	    });
	return true;
    }
}
