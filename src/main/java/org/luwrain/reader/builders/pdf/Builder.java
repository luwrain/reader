/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader.builders.pdf;

import java.io.*;
import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

final class Builder implements DocumentBuilder
{
    @Override public org.luwrain.reader.Document buildDoc(File file, Properties props) throws IOException
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(props, "props");
	final InputStream is = new FileInputStream(file);
	try {
	    return buildDoc(is, props);
	}
	finally {
	    is.close();
	}
    }

    @Override public org.luwrain.reader.Document buildDoc(String text, Properties props)
{
    NullCheck.notNull(text, "text");
    NullCheck.notNull(props, "props");
    return null;
    }

    @Override public org.luwrain.reader.Document buildDoc(InputStream is, Properties props) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(props, "props");
	return null;
    }
}
