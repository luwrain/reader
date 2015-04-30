/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.reader.doctree;

class RowPart
{
    public Run run;
    public int posFrom = 0;
    public int posTo = 0;
    public int rowNum = 0;
    public int relRowNum = 0;

    public String text()
    {
	if (run == null)
	    throw new NullPointerException("run may not be null");
	return run.text.substring(posFrom, posTo);
    }

    public TextAttr textAttr()
    {
	if (run == null)
	    throw new NullPointerException("node may not be null");
	return run.textAttr;
    }
}
