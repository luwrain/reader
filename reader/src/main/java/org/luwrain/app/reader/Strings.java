// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

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
