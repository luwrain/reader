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

package org.luwrain.doctree.view;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class BoundingInfo
{
    interface Acceptor
    {
	void accept(Run run, int posFrom, int posTo);
    }

    final Run runFrom;
    final Run runTo;
    final int posFrom;
    final int posTo;

    BoundingInfo(Run runFrom, int posFrom, Run runTo, int posTo)
    {
	if (runFrom == null && runTo == null)
	    throw new IllegalArgumentException("runFrom and runTo may not be null simultainously");
	if (runFrom != null && posFrom < 0)
	    throw new IllegalArgumentException("posFrom may not be negative");
	if (runTo != null && posTo < 0)
	    throw new IllegalArgumentException("posTo may not be negative");

	if (runFrom == runTo && posTo < posFrom)
	    throw new IllegalArgumentException("posTo (" + posTo + ") may not be less than posFrom (" + posFrom + ") with the same runFrom and runTo");
	
	this.runFrom = runFrom;
	this.posFrom = posFrom;
	this.runTo = runTo;
	this.posTo = posTo;
	    }

    void filter(Run[] runs, Acceptor acceptor)
    {
	NullCheck.notNullItems(runs, "runs");
	NullCheck.notNull(acceptor, "acceptor");
	boolean accepting = runFrom == null;
	for(Run r: runs)
	{
	    if (accepting)
	    {
		if (r == runTo)
		{
		    acceptor.accept(r, 0, Math.min(r.text().length(), posTo));
		    return;
		}
		acceptor.accept(r, 0, r.text().length());
		continue;
	    }
	    //not accepting
	    if (r == runFrom)
	    {
		if (r == runTo)
		{
		    //runFrom == runTo, nothing strange
		    acceptor.accept(r, posFrom, Math.min(r.text().length(), posTo));
		    return;
		}
		acceptor.accept(r, posFrom, r.text().length());
		accepting = true;
		continue;
	    } // encountering runFrom
	    if (r == runTo)//runTo met before we accepted anything, as you wish...
		return;
	}
    }
}
