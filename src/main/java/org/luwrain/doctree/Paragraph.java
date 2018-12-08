/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.doctree;

import java.util.*;

public class Paragraph extends Node
{
    public Run[] runs = new Run[0];


    Paragraph()
    {
	super(Node.Type.PARAGRAPH);
    }

    @Override void preprocess()
    {
	subnodes = null;
	if (runs == null)
	    runs = new Run[0];
	if (titleRun != null)
	    titleRun.setParentNode(this);
	for(Run r: runs)
	{
	    r.setParentNode(this);
	    r.prepareText();
	}
    }

    @Override void setEmptyMark()
    {
	empty = true;
	if (importance < 0)
	    return;
	if (runs == null || runs.length < 1)
	    return;
	for(Run r: runs)
	    if (!r.toString().trim().isEmpty())
		empty = false;
    }

    @Override int prune()
    {
	if (runs == null)
	    return 0;
	int k = 0;
	for(int i = 0;i < runs.length;++i)
	    if (runs[i].isEmpty() )
		++k; else
		runs[i - k] = runs[i];
	if (k > 0)
	    runs = Arrays.copyOf(runs, runs.length - k);
	return k;
    }

    int getParaIndex()
    {
	return getIndexInParentSubnodes();
    }

    @Override public String toString()
    {
	if (runs == null)
	    return "";
	final StringBuilder sb = new StringBuilder();
	for(Run r: runs)
	    sb.append(r.toString());
	return sb.toString();
    }

    public Run[] runs()
    {
	return runs != null?runs:new Run[0];
    }

    @Override public String getCompleteText()
    {
	if (runs == null)
	    return "";
	final StringBuilder b = new StringBuilder();
	boolean first = true;
	for(Run r: runs)
	{
	    final String value = r.text();
	    if (value.isEmpty())
		continue;
	    if (!first)
		b.append(" ");
	    first = false;
	    b.append(value);
	}
	return new String(b);
    }

    public boolean withEmptyLine()
    {
	return parentNode.getType() == Type.ROOT;
    }
}
