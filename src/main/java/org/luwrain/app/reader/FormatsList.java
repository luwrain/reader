
package org.luwrain.app.reader;

public class FormatsList
{
    static public String[] getSupportedFormatsList()
    {
	return new String[]{
	    "text-para-indent:Текстовый файл с отступом в начале абзаца",
	    "text-para-empty-line:Текстовый файл с отделениями абзацев пустыми строками",
	    "text-para-each-line:Текстовый файл с абзацами на отдельных строках",
	    "html:Гипертекстовый документ HTML",
	    "doc:Документ Microsoft Word DOC",
	    "docx:Документ Microsoft Word DOCX",
 	};
    }
}

