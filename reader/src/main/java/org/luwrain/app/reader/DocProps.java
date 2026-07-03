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

    String[] getLines()
    {
	final String url = doc.getProperty(Doc.PROP_URL);
	final String contentType = doc.getProperty("contenttype");
	final String charset = doc.getProperty("charset");
	final String format = doc.getProperty("format");
	return new String[]{
	    "",
	    strings.propertiesAreaUrl(url != null ? url : ""),
	    strings.propertiesAreaContentType(contentType != null ? contentType : ""),
	    strings.propertiesAreaCharset(charset != null ? charset : ""),
	    strings.propertiesAreaFormat(format != null ? format : ""),
	    "",
	};
    }
}
