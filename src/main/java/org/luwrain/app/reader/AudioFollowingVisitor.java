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

import org.luwrain.core.NullCheck;
import org.luwrain.doctree.*;

class  AudioFollowingVisitor implements Visitor
{
    private String desiredId;
    private Run resultingRun = null;

    AudioFollowingVisitor(String desiredId)
    {
	NullCheck.notNull(desiredId, "desiredId");
	this.desiredId = desiredId;
    }

    @Override public void visit(Paragraph para)
    {
	NullCheck.notNull(para, "para");
	if (resultingRun != null)
	    return;
	if (para.runs != null)
	    for(Run r: para.runs)
		checkRun(r);
    }

    @Override public void visitNode(Node node)
    {
    }

    @Override public void visit(ListItem node)
    {
    }

    @Override public void visit(Section node)
    {
    }

    @Override public void visit(TableCell node)
    {
    }

    @Override public void visit(Table node)
    {
    }

    @Override public void visit(TableRow node)
    {
    }

    private void checkRun(Run run)
    {
	if (resultingRun != null)
	    return;
	ExtraInfo info = run.extraInfo();
	while (info != null)
	{
	    if (info.attrs.containsKey("id") && info.attrs.get("id").equals(desiredId))
	    {
		resultingRun = run;
		return;
	    }
	    info = info.parent;
	}
    }

    Run result()
    {
	return resultingRun;
    }
}
