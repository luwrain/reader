/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.doctree;

import javax.activation.*;
import java.nio.file.*;

import org.luwrain.core.*;

class Formats
{
    static final String MIME_TYPE_TEXT = "text/plain";
    static final String MIME_TYPE_DOC = "application/doc";
    static final String MIME_TYPE_DOCX = "application/docx";
    static final String MIME_TYPE_HTML = "text/html";
    static final String MIME_TYPE_ZIP = "application/zip";
    static final String MIME_TYPE_FB2 = "application/fb2";
    static final String MIME_TYPE_EPUB = "application/epub";

    static public MimeType suggest(Path path) throws MimeTypeParseException
    {
	NullCheck.notNull(path, "path");
	if (path.toString().isEmpty())
	    return new MimeType();
	final String ext = FileTypes.getExtension(path.toString());
	if (ext == null)
	    return new MimeType();
	switch(ext.toLowerCase())
	{
	case "epub":
	    return new MimeType(MIME_TYPE_EPUB);
	case "txt":
	    return new MimeType(MIME_TYPE_TEXT);
	case "doc":
	    return new MimeType(MIME_TYPE_DOC);
	case "docx":
	    return new MimeType(MIME_TYPE_DOCX);
	case "html":
	case "htm":
	    return new MimeType(MIME_TYPE_HTML);
	case "zip":
	    return new MimeType(MIME_TYPE_ZIP);
	case "fb2":
	    return new MimeType(MIME_TYPE_FB2);
	default:
	    return new MimeType();
	}
    }
}
