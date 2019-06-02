/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>
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

public interface Strings
{
    static final String NAME = "luwrain.reader";

    String actionAddNote();
    String actionChangeCharset();
    String actionChangeTextParaStyle();
    String actionDeleteNote();
    String actionHideSectionsTree();
    String actionOpenFile();
    String actionOpenUrl();
    String actionRestoreBookmark();
    String actionSaveBookmark();
    String actionShowNotes();
    String actionShowSectionsTree();
    String addNotePopupName();
    String addNotePopupPrefix();
    String appName();
    String badUrl();
    String bookmarkSaved();
    String bookTreeRoot();
    String changeCharsetPopupName();
    String errorAreaName();
    String fetching();
    String noBookmark();
    String noContent();
    String noContentFetching();
    String notesAreaName();
    String openAutodetectPopupName();
    String openAutodetectPopupPrefix();
    String openPathPopupName();
    String openPathPopupPrefix();
    String openUrlPopupName();
    String openUrlPopupPrefix();
    String pathToOpenMayNotBeDirectory();
    String propertiesAreaCharset(String value);
    String propertiesAreaContentType(String value);
    String propertiesAreaFormat(String value);
    String propertiesAreaName();
    String propertiesAreaUrl(String value);
    String searchGooglePopupName();
    String searchGooglePopupPrefix();
    String treeAreaName();
}
