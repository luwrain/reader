
package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;

class BookTreeModelSource implements CachedTreeModelSource
{
    private String root;
    private Document[] docs;

    BookTreeModelSource(String root, Document[] docs)
    {
	NullCheck.notNull(root, "root");
	NullCheck.notNullItems(docs, "docs");
	this.root = root;
	this.docs = docs;
    }

    void setDocuments(Document[] docs)
    {
	NullCheck.notNull(docs, "docs");
	this.docs = docs;
    }

    @Override public Object getRoot()
    {
	return root;
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	if (obj == root)
	    return docs;
	if (obj instanceof Document)
	{
	    final Document doc = (Document)obj;
return doc.getRoot().getSubnodes();
	}
	if (obj instanceof Node)
	{
	    final Node node = (Node)obj;
	    return node.getSubnodes();
	}
	return new Object[0];
    }
}
