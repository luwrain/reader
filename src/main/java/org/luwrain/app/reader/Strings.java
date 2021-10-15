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

public interface Strings
{
    static final String NAME = "luwrain.reader";

    String appName();

    String localRepoAreaName();
    String treeAreaName();
    String notesAreaName();
    String errorAreaName();

    String actionAddNote();
    String actionHideSectionsTree();
    String actionHideNotes();
    String actionOpenFile();
    String actionOpenUrl();
    String actionShowNotes();
    String actionShowSectionsTree();
    String addNotePopupName();
    String addNotePopupPrefix();
    String bookTreeRoot();
    String noContent();
    String noContentFetching();
    String openPathPopupName();
    String openPathPopupPrefix();
    String openUrlPopupName();
    String openUrlPopupPrefix();

    String propertiesAreaCharset(String value);
    String propertiesAreaContentType(String value);
    String propertiesAreaFormat(String value);
    String propertiesAreaName();
    String propertiesAreaUrl(String value);

    String localRepoActDelete();
    String localRepoDeletePopupName();
    String localRepoDeletePopupText(String title);
    String localRepoBookCorrupted();

    String wizardGreetingIntro();
    String wizardGreetingRemote();
    String wizardGreetingLocal();

    String wizardLoginIntro();
    String wizardLoginMail();
    String wizardLoginPasswd();
    String wizardLoginConnect();

    String wizardConfirmationIntro();
    String wizardConfirmationCode();
    String wizardConfirmationConfirm();
}
