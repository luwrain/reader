/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;

public interface Actions
{
    void closeApp();
    boolean fetchingInProgress();
    boolean jumpByHref(String href);
    void onNewResult(Result res);
    boolean onNotesClick(Object item);
    void showErrorPage(Result res);
    boolean showDocInfo();
    boolean returnFromInfoArea();
    void goToTreeArea();
    void goToReaderArea();
    void goToNotesArea();
    boolean docMode();
    boolean bookMode();
    Action[] areaActions();
    boolean onAreaAction(EnvironmentEvent event);
    boolean onTreeClick(TreeArea area, Object obj);
}
