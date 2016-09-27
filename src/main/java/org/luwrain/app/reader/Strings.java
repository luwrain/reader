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

    String actionAddNote();
    String actionBookMode();
    String actionChangeCharset();
    String actionChangeFormat();
    String actionDocMode();
    String actionInfo(); 
    String actionOpenFile();
    String actionOpenInNarrator();
    String actionOpenUrl();
    String actionPlayAudio();
    String addNotePopupName();
    String addNotePopupPrefix();
    String appName();
    String badUrl();
    String bookTreeRoot();
    String errorAnnouncement();
    String errorAreaIntro();
    String fetching();
    String fetchingError(String descr);
    String httpError(String httpCode);
    String linkPrefix();
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
    String undeterminedContentType();
    String unknownHost(String hostName );
    String unrecognizedFormat(String contentType);
}
