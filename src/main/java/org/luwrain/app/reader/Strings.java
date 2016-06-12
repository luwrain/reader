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

public interface Strings
{
    static final String NAME = "luwrain.reader";

    String appName();
    String tableIntroduction(int rows, int cols, String text);
    String tableIntroductionWithLevel(int level, int rows, int cols, String text);
    String tableCellIntroduction(int row, int col, String text);
    String orderedListItemIntroduction(int index, String text);
    String unorderedListItemIntroduction(int index, String text);
    String paragraphIntroduction();
    String sectionIntroduction();
    String linkPrefix();
    String noContent();
    String noContentFetching();
    String fetching(String url);
    String badUrl();
    String actionOpenFile();
    String actionOpenUrl();
    String actionOpenInNarrator();
    String actionChangeFormat();
    String actionChangeCharset();
    String actionBookMode();
    String actionDocMode();
    String actionInfo(); 
    String actionPlayAudio();
    String actionAddNote();
    String infoAreaName();
    String treeAreaName();
    String notesAreaName();
    String bookTreeRoot();
    String infoPageField(String name);
    String openUrlPopupName();
    String openUrlPopupPrefix();

    String openPathPopupName();
    String openPathPopupPrefix();
    String pathToOpenMayNotBeDirectory();
    String addNotePopupName();
    String addNotePopupPrefix();
    String infoAreaAddress();
    String infoAreaFormat();
    String infoAreaCharset();
    String errorAreaIntro();
    String errorAnnouncement();
}
