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

package org.luwrain.app.reader;

import java.net.*;
import java.io.*;

import org.luwrain.core.*;

class FetchThread implements Runnable
{
    private Luwrain luwrain;
    private Area area;
    private URL url;

    public FetchThread(Luwrain luwrain,
		       Area area,
		       URL url)
    {
	this.luwrain = luwrain;
	this.area = area;
	this.url = url;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (area == null)
	    throw new NullPointerException("area may not be null");
	if (url == null)
	    throw new NullPointerException("url may not be null");
    }

    @Override public void run()
    {
	try {
	    impl();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.enqueueEvent(new FetchEvent(area));
	}
    }

    private void impl() throws Exception
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	StringBuilder builder = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
	    builder.append(inputLine);
        in.close();

	luwrain.enqueueEvent(new FetchEvent(area, builder.toString()));

	System.out.println("done");
    }
}
