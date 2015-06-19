
package org.luwrain.app.reader.filters;

import java.util.*;
/*
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
*/

import org.luwrain.util.*;

public class HtmlEncoding implements MlReaderListener, MlReaderConfig
{
    final String[] nonClosingTags = new String[]{
	"!doctype",
	"input",
	"br",
	"link",
	"img",
	"meta"
    }; 

    public static final String PREFIX1 = "text/html;charset=";

    private String encoding = "";

    @Override public void onMlTagOpen(String tagName, Map<String, String> attrs)
    {
	if (!tagName.toLowerCase().equals("meta") || attrs == null)
	    return;
	if (attrs.containsKey("charset"))
	{
	    encoding = attrs.get("charset");
	    return;
	}
	if (attrs.containsKey("content"))
	{
	String content = attrs.get("content");
	if (content == null || content.isEmpty())
	    return;
	content = content.replaceAll(" ", "").toLowerCase();
	if (content.startsWith(PREFIX1))
	    encoding = content.substring(PREFIX1.length());
	}
    }

    @Override public void onMlTagClose(String tagName)
    {
    }

    @Override public void onMlText(String text, LinkedList<String> tagsStack)
    {
    }

    @Override public boolean mlTagMustBeClosed(String tag)
    {
	final String adjusted = tag.toLowerCase().trim();
	for(String s: nonClosingTags)
	    if (s.equals(adjusted))
		return false;
	return true;
    }

    @Override public boolean mlAdmissibleTag(String tagName)
    {
	for(int i = 0;i < tagName.length();++i)
	{
	    final char c = tagName.charAt(i);
	    if (!Character.isLetter(c) && !Character.isDigit(c))
		return false;
	}
	return true;
    }

    public String getEncoding()
    {
	return encoding != null?encoding:"";
    }

    /*

    public static String decode(String charsetName, String text)
    {
	if (charsetName.toLowerCase().equals("utf-8"))
	    return text;
	try {
	    final Charset charset = Charset.forName(charsetName);
	    if (charset == null)
		return "bad charset";//FIXME:
	    final CharsetDecoder decoder = charset.newDecoder();
	    ByteBuffer buf = CharBuffer.wrap(text);
	    CharBuffer res = decoder.decode(buf);
	    return res.toString();
	}
	catch (CharacterCodingException e)
	{
	    e.printStackTrace();
	    return text;
	}
    }
    */
}
