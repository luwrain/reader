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

import java.util.*;

import org.luwrain.core.*;

final class Cleaning
{
    int prune(Node node)
    {
	NullCheck.notNull(node, "node");
	if (node instanceof Paragraph)
	    return pruneParagraph((Paragraph)node);
	Node[] subnodes = node.getSubnodes();
	int k = 0;
	for(int i = 0;i < subnodes.length;++i)
	    if (subnodes[i].empty)
		++k; else
		subnodes[i - k] = subnodes[i];
	if (k > 0)
	    subnodes = Arrays.copyOf(subnodes, subnodes.length - k);
	for(Node n: subnodes)
	    k += prune(n);
	node.setSubnodes(subnodes);
	return k;
    }

    private int pruneParagraph(Paragraph para)
    {
	NullCheck.notNull(para, "para");
	final Run[] runs = para.getRuns();
	int k = 0;
	for(int i = 0;i < runs.length;++i)
	    if (runs[i].isEmpty() )
		++k; else
		runs[i - k] = runs[i];
	if (k > 0)
	    para.setRuns(Arrays.copyOf(runs, runs.length - k));
	return k;
    }
}
