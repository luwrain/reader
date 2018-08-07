
package org.luwrain.app.reader.loading;

import java.net.*;

public interface UrlLoaderFactory
{
    UrlLoader newUrlLoader(URL url) throws MalformedURLException;
}
