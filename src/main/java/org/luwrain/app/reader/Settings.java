
package org.luwrain.app.reader;

interface Settings
{
interface Bookmark
{
    String getUrl(String defValue);
    int getPosition(int defValue);
    void setUrl(String value);
    void setPosition(int value);
}

interface Note
{
    String getUrl(String defValue);
    int getPosition(int defValue);
    String getComment(String defValue);
    String getUniRef(String defValue);

void setUrl(String value);
void setPosition(int value);
void setComment(String value);
void setUniRef(String value);


}
}
