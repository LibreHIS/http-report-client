/*
 * Created on Feb 10, 2005
 *
 */
package com.ims.report.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import com.ims.report.client.exceptions.HttpReportClientException;
import org.apache.log4j.Logger;

/**
 * This class is used to produce a report
 * The client app. will send down the template (base64 encoded), 
 * the datasource (base64 encoded) and the format in which the report 
 * will be exported back to the client
 * 
 * @author vpurdila
 */
public class HttpReportClient
{
	private static final int TIMEOUT = 1000 * 60 * 15;
	private static final int MAX_BUFFER_LIMIT_NO_WARNING = 1024*1024;
	
	static final Logger log = Logger.getLogger(HttpReportClient.class);
	
	private HttpClient client;
    PostMethod post;
    Base64 base64;
	
	public HttpReportClient()
	{
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
        client.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);		
        client.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
        client.getParams().setIntParameter(HttpMethodParams.BUFFER_WARN_TRIGGER_LIMIT, MAX_BUFFER_LIMIT_NO_WARNING);
        
        base64 = new Base64();
	}


	/**
	 * Connects to the report server, passes down the report template, datasource, 
	 * report type and gets the newly created report as a byte array
	 *
	 * @param serverUrl			example:	http://192.168.1.10/ImsReportServerCgi.exe
	 * @param reportTemplate	example:	a byte[] containng the template
	 * @param dataSource		example:	a byte[] containng the datasource
	 * @param exportType		example:	ExportType.FP3 or ExportType.PDF or ExportType.HTML or ExportType.RTF		
	 * @param copies			example:	2 //prints 2 copies		
	 *  
	 * @return byte[]
	 */	
	public byte[] buildReport(String serverUrl, byte[] reportTemplate, byte[] dataSource, ExportType exportType, String printTo, int nCopies) throws HttpReportClientException
	{
        byte[] result = null;
        
        long mili1 = 0;
        long mili2 = 0;
        
        if(log.isDebugEnabled())
        {
        	log.debug("Calling function HttpReportClient.buildReport()...");
        	log.debug("param @serverUrl = " + serverUrl);
        	log.debug("param @reportTemplate = " + new String(reportTemplate));
        	if(dataSource.length < 20000)
        		log.debug("param @dataSource = " + new String(dataSource));
        	else
        		log.debug("param @dataSource = ...too big to be displayed...see ds.xml file..." + String.valueOf(dataSource.length) + " bytes");
        	log.debug("param @exportType = " + exportType.toString());
        	log.debug("param @printTo = " + printTo);
        	log.debug("param @copies = " + nCopies);
        	
        	mili1 = System.currentTimeMillis();
        }
        
		PostMethod post = new PostMethod(serverUrl);
        
        NameValuePair[] data = 
        {
          new NameValuePair("template", new String(base64.encode(reportTemplate))),
          new NameValuePair("datasource", new String(base64.encode(dataSource))),
          new NameValuePair("format", exportType.toString()),
		  new NameValuePair("printto", printTo),
		  new NameValuePair("copies", String.valueOf(nCopies))
        };
        
        post.setRequestBody(data);
        
        int iGetResultCode;
		try
		{
			iGetResultCode = client.executeMethod(post);
			
	        if(log.isDebugEnabled())
	        {
	        	mili2 = System.currentTimeMillis();
	        	
	        	log.debug("The HttpReportClient.buildReport() call took " + String.valueOf(mili2 - mili1) + " miliseconds");
	        }

			if(iGetResultCode == HttpStatus.SC_OK)
	        {
	    		result = getResponseAsByteArray(post);
	    		
		        if(log.isDebugEnabled())
		        {
		        	log.debug("The HttpReportClient.buildReport() call was succesfull");
		        }
	        }
	        else
	        {
	        	log.error("The HttpReportClient.buildReport() call returned the error: " + iGetResultCode);
	        	log.error("The error message was : '" + post.getResponseBodyAsString() + "'");
	        	throw new HttpReportClientException("The report server returned the error: " + iGetResultCode + ", the error message was: '" + post.getResponseBodyAsString() + "'");
	        }
		} 
		catch (HttpException e)
		{
			log.error("The HttpReportClient.buildReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an HttpException: " + e.toString());
		} catch (IOException e)
		{
			log.error("The HttpReportClient.buildReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an IOException: " + e.toString());
		}
		finally
		{
			post.releaseConnection();
		}

        if(log.isDebugEnabled())
        {
        	log.debug("HttpReportClient.buildReport() returned " + String.valueOf(result.length) + " bytes");
        	
        	if(!exportType.equals(ExportType.PDF))
        		log.debug(new String(result));
        }
		
		return result;
	}
	
	/**
	 * Connects to the report server, passes down the report template, datasource, 
	 * report type and prints the report
	 * If the report was printed succesfully returns "true" as byte[]
	 *
	 * @param serverUrl			example:	http://192.168.1.10/ImsReportServerCgi.exe
	 * @param reportTemplate	example:	a byte[] containng the template
	 * @param dataSource		example:	a byte[] containng the datasource
	 * @param printTo			example:	this is the printer name		
	 *  
	 * @return byte[]
	 */	
	public byte[] printReport(String serverUrl, byte[] reportTemplate, byte[] dataSource, String printTo, int nCopies) throws HttpReportClientException
	{
        byte[] result = null;
        
        long mili1 = 0;
        long mili2 = 0;
        
        if(log.isDebugEnabled())
        {
        	log.debug("Calling function HttpReportClient.printReport()...");
        	log.debug("param @serverUrl = " + serverUrl);
        	log.debug("param @reportTemplate = " + new String(reportTemplate));
        	log.debug("param @dataSource = " + new String(dataSource));
        	log.debug("param @printTo = " + printTo);
        	log.debug("param @copies = " + nCopies);
        	
        	mili1 = System.currentTimeMillis();
        }
        
		PostMethod post = new PostMethod(serverUrl);
        
        NameValuePair[] data = 
        {
          new NameValuePair("template", new String(base64.encode(reportTemplate))),
          new NameValuePair("datasource", new String(base64.encode(dataSource))),
		  new NameValuePair("printto", printTo),
		  new NameValuePair("copies", String.valueOf(nCopies))
        };
        
        post.setRequestBody(data);
        
        int iGetResultCode;
		try
		{
			iGetResultCode = client.executeMethod(post);
			
	        if(log.isDebugEnabled())
	        {
	        	mili2 = System.currentTimeMillis();
	        	
	        	log.debug("The HttpReportClient.printReport() call took " + String.valueOf(mili2 - mili1) + " miliseconds");
	        }

			if(iGetResultCode == HttpStatus.SC_OK)
	        {
	    		result = getResponseAsByteArray(post);
	    		
		        if(log.isDebugEnabled())
		        {
		        	log.debug("The HttpReportClient.printReport() call was succesfull");
		        }
	        }
	        else
	        {
	        	log.error("The HttpReportClient.printReport() call returned the error: " + iGetResultCode);
	        	log.error("The error message was : '" + post.getResponseBodyAsString() + "'");
	        	throw new HttpReportClientException("The report server returned the error: " + iGetResultCode + ", the error message was: '" + post.getResponseBodyAsString() + "'");
	        }
		} 
		catch (HttpException e)
		{
			log.error("The HttpReportClient.printReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an HttpException: " + e.toString());
		} catch (IOException e)
		{
			log.error("The HttpReportClient.printReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an IOException: " + e.toString());
		}
		finally
		{
			post.releaseConnection();
		}

        if(log.isDebugEnabled())
        {
        	log.debug("HttpReportClient.printReport() returned " + new String(result));
        }
		
		return result;
	}

	/**
	 * Connects to the report server, passes down the prepared report and prints the report
	 * If the report was printed succesfully returns "true" as byte[]
	 *
	 * @param serverUrl			example:	http://192.168.1.10/ImsReportServerCgi.exe
	 * @param preparedReport	example:	a byte[] containng the prepared report
	 * @param printTo			example:	this is the printer name		
	 * @param copies			example:	2 //prints 2 copies		
	 *  
	 * @return byte[]
	 */	
	public byte[] printReport(String serverUrl, byte[] preparedReport, String printTo, int nCopies) throws HttpReportClientException
	{
        byte[] result = null;
        
        long mili1 = 0;
        long mili2 = 0;
        
        if(log.isDebugEnabled())
        {
        	log.debug("Calling function HttpReportClient.printReport()...");
        	log.debug("param @serverUrl = " + serverUrl);
        	log.debug("param @preparedReport = " + new String(preparedReport));
        	log.debug("param @printTo = " + printTo);
        	log.debug("param @copies = " + nCopies);
        	
        	mili1 = System.currentTimeMillis();
        }
        
		PostMethod post = new PostMethod(serverUrl);
        
        NameValuePair[] data = 
        {
          new NameValuePair("preparedReport", new String(base64.encode(preparedReport))),
		  new NameValuePair("printto", printTo),
		  new NameValuePair("copies", String.valueOf(nCopies))
        };
        
        post.setRequestBody(data);
        
        int iGetResultCode;
		try
		{
			iGetResultCode = client.executeMethod(post);
			
	        if(log.isDebugEnabled())
	        {
	        	mili2 = System.currentTimeMillis();
	        	
	        	log.debug("The HttpReportClient.printReport() call took " + String.valueOf(mili2 - mili1) + " miliseconds");
	        }

			if(iGetResultCode == HttpStatus.SC_OK)
	        {
	    		result = getResponseAsByteArray(post);
	    		
		        if(log.isDebugEnabled())
		        {
		        	log.debug("The HttpReportClient.printReport() call was succesfull");
		        }
	        }
	        else
	        {
	        	log.error("The HttpReportClient.printReport() call returned the error: " + iGetResultCode);
	        	log.error("The error message was : '" + post.getResponseBodyAsString() + "'");
	        	throw new HttpReportClientException("The report server returned the error: " + iGetResultCode + ", the error message was: '" + post.getResponseBodyAsString() + "'");
	        }
		} 
		catch (HttpException e)
		{
			log.error("The HttpReportClient.printReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an HttpException: " + e.toString());
		} catch (IOException e)
		{
			log.error("The HttpReportClient.printReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an IOException: " + e.toString());
		}
		finally
		{
			post.releaseConnection();
		}

        if(log.isDebugEnabled())
        {
        	log.debug("HttpReportClient.printReport() returned " + new String(result));
        }
		
		return result;
	}
	

	/**
	 * Connects to the report server, passes down the prepared report, convert type , converts the report and prints it if the printer is not empty
	 *
	 * @param serverUrl			example:	http://192.168.1.10/ImsReportServerCgi.exe
	 * @param preparedReport	example:	a byte[] containng the prepared report
	 * @param exportType		example:	ExportType.FP3 or ExportType.PDF or ExportType.HTML or ExportType.RTF		
	 * @param printTo			example:	this is the printer name		
	 * @param copies			example:	2 //prints 2 copies		
	 *  
	 * @return byte[]
	 */	
	public byte[] convertReport(String serverUrl, byte[] preparedReport, ExportType exportType, String printTo, int nCopies) throws HttpReportClientException
	{
        byte[] result = null;
        
        long mili1 = 0;
        long mili2 = 0;
        
        if(log.isDebugEnabled())
        {
        	log.debug("Calling function HttpReportClient.convertReport()...");
        	log.debug("param @serverUrl = " + serverUrl);
        	log.debug("param @preparedReport = " + new String(preparedReport));
        	log.debug("param @exportType = " + exportType.toString());
        	log.debug("param @printTo = " + printTo);
        	log.debug("param @copies = " + nCopies);
        	
        	mili1 = System.currentTimeMillis();
        }
        
		PostMethod post = new PostMethod(serverUrl);
        
        NameValuePair[] data = 
        {
          new NameValuePair("preparedReport", new String(base64.encode(preparedReport))),
          new NameValuePair("format", exportType.toString()),
		  new NameValuePair("printto", printTo),
		  new NameValuePair("copies", String.valueOf(nCopies))
        };
        
        post.setRequestBody(data);
        
        int iGetResultCode;
		try
		{
			iGetResultCode = client.executeMethod(post);
			
	        if(log.isDebugEnabled())
	        {
	        	mili2 = System.currentTimeMillis();
	        	
	        	log.debug("The HttpReportClient.convertReport() call took " + String.valueOf(mili2 - mili1) + " miliseconds");
	        }

			if(iGetResultCode == HttpStatus.SC_OK)
	        {
	    		result = getResponseAsByteArray(post);
	    		
		        if(log.isDebugEnabled())
		        {
		        	log.debug("The HttpReportClient.convertReport() call was succesfull");
		        }
	        }
	        else
	        {
	        	log.error("The HttpReportClient.convertReport() call returned the error: " + iGetResultCode);
	        	log.error("The error message was : '" + post.getResponseBodyAsString() + "'");
	        	throw new HttpReportClientException("The report server returned the error: " + iGetResultCode + ", the error message was: '" + post.getResponseBodyAsString() + "'");
	        }
		} 
		catch (HttpException e)
		{
			log.error("The HttpReportClient.convertReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an HttpException: " + e.toString());
		} catch (IOException e)
		{
			log.error("The HttpReportClient.convertReport() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an IOException: " + e.toString());
		}
		finally
		{
			post.releaseConnection();
		}

        if(log.isDebugEnabled())
        {
        	log.debug("HttpReportClient.printReport() returned " + new String(result));
        }
		
		return result;
	}
	
	/**
	 * Connects to the report server, passes down the office document, convert type , converts the report to desired format
	 *
	 * @param serverUrl			example:	http://192.168.1.10/ImsReportServerCgi.exe
	 * @param officeDocument	example:	a byte[] containng the office document
	 * @param exportType		example:	ExportType.FP3 or ExportType.PDF or ExportType.DOC or ExportType.RTF or ExportType.DOCX		
	 *  
	 * @return byte[]
	 */	
	public byte[] convertOfficeDocument(String serverUrl, byte[] officeDocument, ExportType exportType) throws HttpReportClientException
	{
        byte[] result = null;
        
        long mili1 = 0;
        long mili2 = 0;
        
        if(log.isDebugEnabled())
        {
        	log.debug("Calling function HttpReportClient.convertOfficeDocument()...");
        	log.debug("param @serverUrl = " + serverUrl);
        	log.debug("param @officeDocument = " + new String(officeDocument));
        	log.debug("param @exportType = " + exportType.toString());
        	
        	mili1 = System.currentTimeMillis();
        }
        
		PostMethod post = new PostMethod(serverUrl);
        
        NameValuePair[] data = 
        {
          new NameValuePair("officeDocument", new String(base64.encode(officeDocument))),
          new NameValuePair("format", exportType.toString()),
        };
        
        post.setRequestBody(data);
        
        int iGetResultCode;
		try
		{
			iGetResultCode = client.executeMethod(post);
			
	        if(log.isDebugEnabled())
	        {
	        	mili2 = System.currentTimeMillis();
	        	
	        	log.debug("The HttpReportClient.convertOfficeDocument() call took " + String.valueOf(mili2 - mili1) + " miliseconds");
	        }

			if(iGetResultCode == HttpStatus.SC_OK)
	        {
	    		result = getResponseAsByteArray(post);
	    		
		        if(log.isDebugEnabled())
		        {
		        	log.debug("The HttpReportClient.convertOfficeDocument() call was succesfull");
		        }
	        }
	        else
	        {
	        	log.error("The HttpReportClient.convertOfficeDocument) call returned the error: " + iGetResultCode);
	        	log.error("The error message was : '" + post.getResponseBodyAsString() + "'");
	        	throw new HttpReportClientException("The report server returned the error: " + iGetResultCode + ", the error message was: '" + post.getResponseBodyAsString() + "'");
	        }
		} 
		catch (HttpException e)
		{
			log.error("The HttpReportClient.convertOfficeDocument() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an HttpException: " + e.toString());
		} catch (IOException e)
		{
			log.error("The HttpReportClient.convertOfficeDocument() call returned the error: " + e);
			throw new HttpReportClientException("The report server has thrown an IOException: " + e.toString());
		}
		finally
		{
			post.releaseConnection();
		}

        if(log.isDebugEnabled())
        {
        	log.debug("HttpReportClient.convertOfficeDocument() returned " + new String(result));
        }
		
		return result;
	}
	
	private byte[] getResponseAsByteArray(PostMethod post) throws IOException
	{
		InputStream instream = post.getResponseBodyAsStream();
		
		if (instream != null) 
		{
			long contentLength = post.getResponseContentLength();
			
			// guard below cast from overflow
			if (contentLength > Integer.MAX_VALUE) 
			{ 
				throw new IOException("Content too large to be buffered: "+ contentLength +" bytes");
			}
			
			ByteArrayOutputStream outstream = new ByteArrayOutputStream(contentLength > 0 ? (int) contentLength : 4*1024);
			byte[] buffer = new byte[4096];
			int len;
			while ((len = instream.read(buffer)) > 0) 
			{
			    outstream.write(buffer, 0, len);
			}
			outstream.close();

			return outstream.toByteArray();
        }	
		else
			return null;
	}
	
}
