/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.opds;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.opds.Opds.Entry;
import org.luwrain.app.base.*;

final class LibraryPropsLayout extends LayoutBase
{
    private final App app;
    final FormArea formArea;

    LibraryPropsLayout(App app, RemoteLibrary lib)
    {
	super(app);
	NullCheck.notNull(lib, "lib");
	this.app = app;
	this.formArea = new FormArea(getControlContext(), "Параметры библиотеки");
	formArea.addEdit("title", "Название:", lib.title != null?lib.title.trim():"");
		formArea.addEdit("url", "Адрес:", lib.url != null?lib.url.trim():"");
	setAreaLayout(formArea, actions());
    }
}
