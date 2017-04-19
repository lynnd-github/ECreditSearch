/*
 * Created on November 25, 2011
 *   Lynn Deason
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.delta.soa.ecreditsearchdcomspec.tpf;

import java.util.Properties;
import org.apache.log4j.Logger;

import com.delta.schemas.common.requestinfo.v4.RequestInfoType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ListResponseType;
import com.delta.soa.ecreditsearchdcomspec.ECreditSearchDcomSpecImpl;
import com.delta.soa.ecreditsearchdcomspec.util.Timer;
import com.delta.soa.miscellaneousdocument.v2.ECertificateRequestType;

public class ECreditSearchDataSource
{
    private Logger m_log=null;
    private ECreditSearchDcomSpecImpl m_ecreditSearchImpl=null;
	private static String DOCNUMBER_SEARCH = "DOCNUMBER";
	public String processEtickets="true";
	public ECreditSearchDataSource(Logger p_log, ECreditSearchDcomSpecImpl p_ecreditSearchImpl)
    {
        m_log = p_log;
        m_ecreditSearchImpl = p_ecreditSearchImpl;
        processEtickets=m_ecreditSearchImpl.getUnHookETickets();
    }

    public void sendRequest(ECreditSearchDataContainer[] p_ecreditsContainer, Properties p_props, ECertificateRequestType p_ecert,
    		                RequestInfoType requestInfo)
    throws Exception, com.delta.soa.miscellaneousdocument.v2.SystemFaultException, com.delta.soa.ticket.v3.SystemFaultException
    {
       String strTrxTimeout = p_props.getProperty("ecreditTimeOut", "70");
       Timer timer1 = new Timer("ECreditSearch Data Source");

       if (strTrxTimeout == null)
    	   strTrxTimeout = "70";
   	   //There can be up to 50 threads.. one for miscellaneousdocument.retrieveECertificate
       // and numberous threads for calling TIcketintgProcessV3.retrieve
       ECreditSearchThread[] threadContainer = new ECreditSearchThread[p_ecreditsContainer.length];
       Boolean showTimings = Boolean.valueOf(p_props.getProperty("showTimings", "false"));

        try
        {
            if (showTimings)
                timer1.start();

            // Launch the threads.  The number of threads is passed in through the ecreditSearchDataContainer
            for (int i=0; i < p_ecreditsContainer.length; i++)
            {

               if (p_ecreditsContainer[i] != null)
               {
                     ECreditSearchThread ecThread = new ECreditSearchThread(m_log, p_ecreditsContainer[i], null, m_ecreditSearchImpl, p_ecert);
                     ecThread.start();
                     threadContainer[i] = ecThread;
                     m_log.debug("started thread: " + Long.toString(threadContainer[i].getId()));
               }
            }

            // As a caution: get timeout in millis, to interrupt the thread.
      	  //THis must be set higher than the source timeouts in the
            Long ecertCompTimeout = Long.parseLong(strTrxTimeout);
            ecertCompTimeout = (ecertCompTimeout + 5) * 1000;
            for (int j=0; j < threadContainer.length; j++)
            {
          	  if (threadContainer[j] != null)
          	  {
                threadContainer[j].join(ecertCompTimeout);
        		m_log.debug("Joined Thread:  " +  Long.toString(threadContainer[j].getId()));
          	  }
            }

            // Check if threads died, if not interrupt them.
            // This should not happen; this is a precautionary measure.
            for (int k = 0; k < threadContainer.length; k++) {
              if (threadContainer[k] != null) {
                 if (threadContainer[k].isAlive()) {
                    //m_log.error("ECreditSearchDataSource: Interrupting Thread.." + Long.toString(threadContainer[k].getId()));
                    threadContainer[k].interrupt();
                    threadContainer[k].join();
                 }
              }
            }

            // Get the response from the  above threads
            for (int j=0; j<threadContainer.length; j++)
            {
          	  if (threadContainer[j] != null)
          	  {
          		  if ( threadContainer[j].getECreditResponse() != null)
          		  {
                   	if (threadContainer[j].getECreditResponse().getECreditRespDoc()!=null)
                        m_log.debug("ECreditSearchThread:\n" + threadContainer[j].getECreditResponse().getECreditRespDoc());
                    //  COME BACK HERE
                    //p_ecreditsContainter[j]. .setECreditRespDoc(threadContainer[j].getECreditResponse().getECreditRespDoc());
                   m_log.debug("captured response... thread ID: " + threadContainer[j].getId());
         		  }
          		  else
          			  //Error err = threadContainer[j].getECreditResponse().getECreditRespDoc().getECreditSearchResponse().getError();
          			  m_log.error(" Thread Timed Out after: " + String.valueOf(ecertCompTimeout) + "ms");
          	  }
            }

            // Now we need to check to see if this was a search by IDnumber or FF#
            // if so, then we need to get the ListResponse results and
            // set up threads for each of those document numbers.
             ECreditSearchThread[] ecThreads = null;
            if (p_ecreditsContainer.length > 0)
            {
              for (int x=0; x < p_ecreditsContainer.length && p_ecreditsContainer[x] != null; x++)
              {
            	  ListResponseType[] listArray = null;
            	  if (p_ecreditsContainer[x].getListResponses() == null)
            		  continue;
            	  else
            	  {
            		  listArray = p_ecreditsContainer[x].getListResponses();
                      ecThreads = new ECreditSearchThread[listArray.length];
            	  }
                  String newSrchType = DOCNUMBER_SEARCH;

                  for (int i=0; i < listArray.length; i++)
                  {
                	// Do not add the Voided ones to the thread list.
                	if (listArray[i].getDocumentVoided() != null &&
                	listArray[i].getDocumentVoided().equalsIgnoreCase("Y"))
                	{
                	  if (listArray[i].getDocumentNumber()!=null)
                        m_log.debug("Doc is VOID " + listArray[i].getDocumentNumber());
                	  continue;
                	}
                	/***
                	// If this flag is NOT true, then do NOT process ETickets.
                	if (!this.processEtickets.equalsIgnoreCase("true") &&
             		listArray[i].getDocumentTypeCode().equalsIgnoreCase("ETK"))
                	{
                  	  if (listArray[i].getDocumentNumber()!=null)
                          m_log.debug("Doc is ETK - ETKs are turned off - this doc not added. " + listArray[i].getDocumentNumber());
                  	  continue;

                	}
                	***/
                	ECreditSearchDataContainer newECDataContainer = new ECreditSearchDataContainer();
                	newECDataContainer.setSearchType(p_ecreditsContainer[x].getSearchType());
                	newECDataContainer.setOriginalSearchType(p_ecreditsContainer[x].getOriginalSearchType());
                	newECDataContainer.setECreditRequestType(p_ecreditsContainer[x].getECreditReqType());
            		if (listArray[i].getDocumentNumber()!= null)
            	    {
                	  // Now create a new thread for each one of the document numbers found
              	      // in the ListResponse array.
                      newECDataContainer.setDocumentNumber(listArray[i].getDocumentNumber());
                      newECDataContainer.setECreditRespType(p_ecreditsContainer[x].getECreditRespType());
                      if (p_ecreditsContainer[x].getIDNumberSearchRQ() != null)
                          newECDataContainer.setIDNumberSearchRQ(p_ecreditsContainer[x].getIDNumberSearchRQ());
                      else
                          newECDataContainer.setSkyMilesSrchRQ(p_ecreditsContainer[x].getECSkyMilesRQ());
                       m_log.debug("Adding TicketNumber " + listArray[i].getDocumentNumber());
                      ECreditSearchThread ecDocThread = new ECreditSearchThread(m_log, newECDataContainer,
                    		  newSrchType, m_ecreditSearchImpl, null);
                      ecDocThread.start();
                      ecThreads[i] = ecDocThread;
                      m_log.debug("started thread: " + Long.toString(ecThreads[i].getId()));
                    }

                  }
              }
            }
            // As a caution: get timeout in millis, to interrupt the thread.
        	  //THis must be set higher than the source timeouts in the
               for (int j=0; ecThreads != null && j < ecThreads.length; j++)
              {
            	  if (ecThreads[j] != null)
            	  {
                    ecThreads[j].join(ecertCompTimeout);
                    if (m_log.isDebugEnabled())
          		       m_log.debug("Joined Thread:  " +  Long.toString(ecThreads[j].getId()));
            	  }
              }

              // Check if threads died, if not interrupt them.
              // This should not happen; this is a precautionary measure.
              for (int k = 0; ecThreads != null && k < ecThreads.length; k++) {
                if (ecThreads[k] != null) {
                   if (ecThreads[k].isAlive()) {
                      m_log.error("ECreditSearchDataSource: Interrupting Thread.." + Long.toString(ecThreads[k].getId()));
                      ecThreads[k].interrupt();
                      ecThreads[k].join();
                   }
                }
              }

              // Get the response from the threads
              for (int j=0; ecThreads != null && j < ecThreads.length; j++)
              {
              	if (ecThreads[j] != null && ecThreads[j].getECreditResponse() != null)
            	{
              		m_log.debug("captured response... thread ID: " + ecThreads[j].getId());
           		}
           		else
           		  m_log.error("LOGGING ERROR NOT FINISHED... ADD LOGIC HERE");
              }
        }
        catch (InterruptedException iex) {
            m_log.error("ECreditSearchDataSource: sendRequest(): InterruptedException:" + iex.getMessage() + "\nStack Trace:\n" + iex.getStackTrace());
            throw iex;
        }
        catch (Exception ex) {
            m_log.error("ECreditSearchDataSource: sendRequest(): Exception:" + ex.getMessage() + "\nStack Trace:\n" + ex.getStackTrace());
            throw ex;
        }
        finally
        {
            if (showTimings)
            {
                timer1.stop();
                m_log.info("Elapsed time (ms): " + timer1.toString());
            }
        }
    }
 }
