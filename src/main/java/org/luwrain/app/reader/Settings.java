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
import java.io.*;

import org.luwrain.core.*;

interface Settings
{
    static final String
	PATH = "/org/luwrain/app/reader",
	ATTRIBUTES_PATH = "/org/luwrain/app/reader/attributes";

    String getLocalRepoMetadata(String defValue);
    void setLocalRepoMetadata(String value);
        String getNotes(String defValue);
    void setNotes(String value);

    static Settings create(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return RegistryProxy.create(luwrain.getRegistry(), PATH, Settings.class);
    }
}
