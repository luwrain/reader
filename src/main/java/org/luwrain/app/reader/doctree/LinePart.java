/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.preview.doctree;

public class LinePart
{
public static final int SECTION = 0;

    public Node node;
    public int posFrom = 0;
    public int posTo = 0;
    public int lineNum = 0;
    //    public int partNum = 0;
    public int type;
    public boolean isSection;
    public boolean isHRef;
    public TextAttr textAttr;
}
