/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader.view;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

final class RowPart
{
    /** The run this part is associated with*/
    final Run run;
    /** Starting position in the text of the corresponding run*/
    final int posFrom;
    /** Ending position in the text of the corresponding run*/
    final int posTo;
    /** Index in the corresponding paragraph*/
    final int relRowNum;
    /** Absolute row index in the document*/
    int absRowNum = 0;

    //For empty runs
        RowPart(Run run)
    {
	NullCheck.notNull(run, "run");
	this.run = run;
	this.posFrom = -1;
	this.posTo = -1;
	this.relRowNum = 0;
    }

    RowPart(Run run, int posFrom, int posTo, int relRowNum)
    {
	NullCheck.notNull(run, "run");
	if (posFrom < 0)
	    throw new IllegalArgumentException("posFrom (" + posFrom + ") may not be negative");
	if (posTo < 0)
	    throw new IllegalArgumentException("posTo (" + posTo + ") may not be negative");
if (posFrom >= posTo)
  throw new IllegalArgumentException("posFrom (" + posFrom + ") must be less than posTo (" + posTo + ")");
	if (relRowNum < 0)
	    throw new IllegalArgumentException("relRowNum (" + relRowNum + ") may not be negative");
	this.run = run;
	this.posFrom = posFrom;
	this.posTo = posTo;
	this.relRowNum = relRowNum;
    }

boolean isEmpty()
    {
	return posFrom == posTo;
    }

    String getText()
    {
	if (isEmpty())
	    return "";
	return run.text().substring(posFrom, posTo);
    }

    //Checks relRowNum and parents of runs
    boolean onTheSameRow(RowPart rowPart)
    {
	NullCheck.notNull(rowPart, "rowPart");
	if (isEmpty() || rowPart.isEmpty())
	    return false;
	return run.getParentNode() == rowPart.run.getParentNode() && relRowNum == rowPart.relRowNum;
    }
}
