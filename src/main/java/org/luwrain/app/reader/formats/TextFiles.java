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

package org.luwrain.app.reader.formats;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.util.*;

public final class TextFiles
{
    public enum ParaStyle {
	EMPTY_LINES,
	EACH_LINE,
	INDENT};

    private final File file;
    private String title;
    private final String charset;
    private final ParaStyle paraStyle;

    public TextFiles(File file, String title, String charset, ParaStyle paraStyle)
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(title, "title");
	NullCheck.notEmpty(charset, "charset");
	NullCheck.notNull(paraStyle, "paraStyle");
	this.file = file;
	this.title = title;
	this.charset = charset;
	this.paraStyle = paraStyle;
    }

    public Document makeDoc() throws IOException
    {
	final String[] lines = FileUtils.readTextFileMultipleStrings(file, charset, null);
	switch(paraStyle)
	{
	case EMPTY_LINES:
	    return formatParaEmptyLines(lines);
	case INDENT:
	    return formatParaIndent(lines);
	case EACH_LINE:
	    return formatParaEachLine(lines);
	default:
	    return null;
	}
    }

    private Document formatParaEmptyLines(String[] lines)
    {
	NullCheck.notNullItems(lines, "lines");
	final List<String> paraLines = new LinkedList();
	final NodeBuilder builder = new NodeBuilder();
	for(String line: lines)
	{
	    if (line.trim().isEmpty())
	    {
		final Paragraph para = createPara(paraLines);
		if (para != null)
		    builder.addSubnode(para);
		continue;
	    }
	    paraLines.add(line.trim());
	}
	final Paragraph para = createPara(paraLines);
	if (para != null)
	    builder.addSubnode(para);
	Log.debug("proba", "" + builder.newRoot().getSubnodes().length + " nodes");
	return new Document(title, builder.newRoot());
    }

    private Document formatParaEachLine(String[] lines)
    {
	NullCheck.notNullItems(lines, "lines");
	final NodeBuilder builder = new NodeBuilder();
	for(String line: lines)
	{
	    if (line.trim().isEmpty())
		continue;
	    builder.addPara(line.trim());
	}
	return new Document(title, builder.newRoot());
    }

    private Document formatParaIndent(String[] lines)
    {
	NullCheck.notNullItems(lines, "lines");
	final List<String> paraLines = new LinkedList();
	final NodeBuilder builder = new NodeBuilder();
	for(String line: lines)
	{
	    if (line.trim().isEmpty())
	    {
		final Node para = createPara(paraLines);
		if (para != null)
		    builder.addSubnode(para);
		continue;
	    }
	    int indent = 0;
	    while(indent < line.length() && Character.isSpace(line.charAt(indent)))
		indent++;
	    if (indent > 0)
	    {
		final Node para = createPara(paraLines);
		if (para != null)
		    builder.addSubnode(para);
		paraLines.add(line.trim());
		continue;
	    }
	    paraLines.add(line.trim());
	}
	final Node para = createPara(paraLines);
	if (para != null)
	    builder.addSubnode(para);
	return new Document(title, builder.newRoot());
    }

    private Paragraph createPara(List<String> lines)
    {
	NullCheck.notNull(lines, "lines");
	final String[] l = lines.toArray(new String[lines.size()]);
	lines.clear();
	if (l.length == 0)
	    return null;
	Log.debug("proba", "here");
	if (l.length == 1)
	    return NodeFactory.newPara(l[0]);
	//Log.debug("proba", "here2");
	final StringBuilder b = new StringBuilder();
	b.append(l[0]);
	for(int i = 1;i < l.length;++i)
	    b.append(" " + l[i]);
	//Log.debug("proba", new String(b));
	return NodeFactory.newPara(new String(b));
    }
}
