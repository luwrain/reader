

package org.luwrain.app.reader.filters;

import java.net.*;
import java.io.*;

import org.luwrain.util.*;

public class UrlEncodingPrint
{
    public static void main(String[] args) throws Exception
    {
	if (args == null || args.length != 1)
	{
	    System.out.println("Give me a URL to take encoding from");
	    System.exit(1);
	}

	URL url = new URL(args[0]);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	StringBuilder builder = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
	    builder.append(inputLine);
        in.close();

	HtmlEncoding encoding = new HtmlEncoding();
	new MlReader(encoding, encoding, builder.toString()).read();
	System.out.println(encoding.getEncoding());

    }
}
