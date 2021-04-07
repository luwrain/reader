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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

final class Conversations
{
    private final App app;
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    String newLibraryName()
    {
	return Popups.textNotEmpty(luwrain, "Новая библиотека", "Имя новой библиотеки:", "");
    }
}
