/*
 * Created on Feb 10, 2005
 *
 */
package com.ims.report.client;

/**
 * @author vpurdila
 *
 */

public class ExportType
{
	private int nType;
	
	public static final ExportType FP3 = new ExportType(0);
	public static final ExportType PDF = new ExportType(1);
	public static final ExportType HTML = new ExportType(2);
	public static final ExportType RTF = new ExportType(3);
	public static final ExportType CSV = new ExportType(4);
	public static final ExportType DS = new ExportType(5);
	public static final ExportType JPEG = new ExportType(6);
	public static final ExportType TXT = new ExportType(7);
	public static final ExportType XLS = new ExportType(8);
	public static final ExportType DOC = new ExportType(9);
	public static final ExportType DOCX = new ExportType(10);
	public static final ExportType MHT = new ExportType(11);
	
	public ExportType(int type)
	{
		nType = type;
	}
	public String toString()
	{
		if(nType == 0)
			return "FP3";
		else if(nType == 1)
			return "PDF";
		else if(nType == 2)
			return "HTML";
		else if(nType == 3)
			return "RTF";
		else if(nType == 4)
			return "CSV";
		else if(nType == 5)
			return "DS";
		else if(nType == 6)
			return "JPEG";
		else if(nType == 7)
			return "TXT";
		else if(nType == 8)
			return "XLS";
		else if(nType == 9)
			return "DOC";
		else if(nType == 10)
			return "DOCX";
		else if(nType == 11)
			return "MHT";
		else
			return "";
	}
	
	public int getValue()
	{
		return nType;
	}
	
	public boolean equals(Object other)
	{
		if(other == null)
			return false;
		
		if(!(other instanceof ExportType))
		{
			return false;
		}
		
		if(this.nType != ((ExportType)other).getValue())
			return false;
		
		return true;
	}
}

