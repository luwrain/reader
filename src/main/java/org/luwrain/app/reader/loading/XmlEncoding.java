
package org.luwrain.app.reader.loading;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

import org.luwrain.core.NullCheck;

class XmlEncoding
{
    static private final Pattern pattern1 = Pattern.compile("<?xml.*encoding\\s*=\\s*\"([^\"]*)\".*?>", Pattern.CASE_INSENSITIVE);
    static private final Pattern pattern2 = Pattern.compile("<?xml.*encoding\\s*=\\s*\'([^\']*)\'.*?>", Pattern.CASE_INSENSITIVE);

    static String getEncoding(InputStream s) throws IOException
    {
	NullCheck.notNull(s, "s");
	final BufferedReader r = new BufferedReader(new InputStreamReader(s));
	String line;
	while ( (line = r.readLine()) != null)
	{
	Matcher matcher = pattern1.matcher(line);
	if (matcher.find())
	    return matcher.group(1);
	matcher = pattern2.matcher(line);
	if (matcher.find())
	    return matcher.group(1);
	}
	return null;
    }

    static String getEncoding(Path path) throws IOException
    {
	NullCheck.notNull(path, "path");
	InputStream is = null;
	try {
	    is = Files.newInputStream(path);
	    return getEncoding(is);
	}
	finally
	{
	    if (is != null)
		is.close();
	}
    }
}
