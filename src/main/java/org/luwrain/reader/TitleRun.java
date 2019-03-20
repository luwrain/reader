/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader;

import org.luwrain.core.NullCheck;

public class TitleRun implements Run
{
    protected Node parentNode;
    protected final ExtraInfo extraInfo = new ExtraInfo();
    protected String parentClassName;

    TitleRun(String parentClassName)
    {
	NullCheck.notNull(parentClassName, "parentClassName");
	this.parentClassName = parentClassName;
    }

    @Override public String text()
    {
	return "";
    }

    @Override public boolean isEmpty()
    {
	return false;
    }

    @Override public String href()
    {
	return "";
    }

    @Override public TextAttr textAttr()
    {
	return new TextAttr();
    }

    @Override public String toString()
    {
	//	return text();
	return parentClassName;
    }

    @Override public void prepareText()
    {
    }

    public ExtraInfo extraInfo()
    {
	return extraInfo;
    }

@Override public void setParentNode(Node node)
    {
	NullCheck.notNull(node, "node");
	parentNode = node;
    }

    public Node getParentNode()
    {
	return parentNode;
    }
}
