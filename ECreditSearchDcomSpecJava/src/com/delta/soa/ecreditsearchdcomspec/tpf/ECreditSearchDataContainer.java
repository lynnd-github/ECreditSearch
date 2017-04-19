/*
 * Created on June 25, 2--8
 *
 * This class is used by both the Fares MileageGrid and
 * AwardCalendar threads.
 */
package com.delta.soa.ecreditsearchdcomspec.tpf;

import java.util.Vector;

import com.delta.schemas.misc.ecertificateresponse.RetrieveECertificateResponseDocument;
import com.delta.schemas.ticket.retrievetickettypes.v2.ListResponseType;
import com.delta.schemas.ticket.retrievetickettypes.v2.RetrieveRequestType;
import com.delta.schemas.ticket.retrievetickettypes.v2.RetrieveResponseType;
import com.delta.schemas.ecredit.ecreditidnumbersearch.ECreditIDNumberSearchType;
import com.delta.schemas.ecredit.ecreditnumbersearch.ECreditNumberSearchType;
import com.delta.schemas.ecredit.ecreditsearchcriteria.ECreditSearchCriteriaType;
import com.delta.schemas.ecredit.ecreditskymilessearch.ECreditSkyMilesSearchType;
import com.delta.schemas.ecredit.ecreditsearchrequest.ECreditSearchRequestType;
import com.delta.schemas.ecredit.ecreditsearchresponse.ECreditSearchResponseType;
import com.delta.soa.miscellaneousdocument.v2.ECreditSearchResponseDocument;
import com.delta.soa.miscellaneousdocument.v2.ECreditSearchRequestDocument;
import com.delta.soa.miscellaneousdocument.v2.RetrieveECertificateRequestDocument;
import com.delta.soa.miscellaneousdocument.v2.RetrieveECertificateRequestType;
import com.delta.soa.miscellaneousdocument.v2.SystemFaultException;
import com.delta.schemas.misc.ecertificateresponse.RetrieveECertificateResponseType;

public class ECreditSearchDataContainer
{
	private static String SKYMILES_SEARCH = "SKYMILES";
	private static String IDNUMBER_SEARCH = "IDNUMBER";
	private static String DOCNUMBER_SEARCH = "DOCNUMBER";

    long m_threadId = 0;
    String m_searchType = null;
    String m_originalSearchType = null;
    String m_documentNumber = null;
    int m_expiredECreditCount = 0;
    int m_openECreditCount = 0;
    String m_openECertCount = null;
    String m_expiredECertCount = null;
    RetrieveRequestType m_tktRetrieveRQ = null;
    RetrieveResponseType m_tktRetrieveRS = null;
    RetrieveECertificateRequestType m_ecertRQ = null;
    RetrieveECertificateResponseType m_ecertRS = null;
    RetrieveECertificateResponseDocument m_ecertRSDoc = null;
    RetrieveECertificateRequestDocument m_ecertRQDoc = null;
    ECreditSkyMilesSearchType m_ecreditSkyMilesRQ = null;
	ECreditIDNumberSearchType m_ecreditIDSearchRQ = null;
	Vector<ECreditNumberSearchType> m_ecreditNumberRQs = null;
	ECreditNumberSearchType m_ecreditNumberRQ = null;
	ECreditSearchResponseDocument m_ecreditRespDoc = null;
	ECreditSearchResponseType m_ecreditResp = null;
	ECreditSearchRequestDocument m_ecreditReqDoc = null;
	ECreditSearchCriteriaType m_ecreditSrchCrit = null;
	ECreditSearchRequestType m_ecreditReqType = null;
	ListResponseType[] m_listResponses=null;
    Throwable m_exception = null;
    com.delta.soa.ticket.v3.SystemFaultException m_tktSysFault = null;
    SystemFaultException m_ecertSysFault = null;
    
    public void setSearchType(String p_srchType)
    {
    	if (p_srchType.equalsIgnoreCase(SKYMILES_SEARCH) ||
    		p_srchType.equalsIgnoreCase(IDNUMBER_SEARCH) ||
    		p_srchType.equalsIgnoreCase(DOCNUMBER_SEARCH))
    	  m_searchType = p_srchType;
    	else
    	{
   			throw new RuntimeException("Invalid value for searchType.  Valid values are: " +  
   					                   SKYMILES_SEARCH + ", " + IDNUMBER_SEARCH + ", or " + 
   					                   DOCNUMBER_SEARCH );
       	}
    }
    public void setOriginalSearchType(String p_origSearchType)
    {
    	m_originalSearchType = p_origSearchType;
    }
    public String getOriginalSearchType()
    {
    	return m_originalSearchType;
    }
    public void setDocumentNumber(String docNbr)
    {
    	m_documentNumber = docNbr;
    }
    public String getDocumentNumber()
    {
    	return m_documentNumber;
    }
   
    public String getSearchType()
    {
    	return m_searchType;
    }
    public void setThreadId( long p_Id )
    {
        m_threadId = p_Id;
    }
    public long getThreadId( )
    {
        return m_threadId;
    }
    public void setException( Throwable ex )
    {
        m_exception = ex;
    }
    public Throwable getException( )
    {
        return m_exception;
    }
    public boolean isException( )
    {
        if ( m_exception != null ) return true;
        return false;
    }
    public void setECertSystemFault(SystemFaultException ecertFault ) {
        m_ecertSysFault = ecertFault;
    }
    public void setTktSystemFault(com.delta.soa.ticket.v3.SystemFaultException p_tktSysFault ) {
        m_tktSysFault = p_tktSysFault;
    }	
	public RetrieveECertificateRequestType getECertRQ() {
		return m_ecertRQ;
	}
	public void setECertificateRQ(RetrieveECertificateRequestType p_certReqType) {
		m_ecertRQ = p_certReqType;
	}
	public RetrieveECertificateRequestDocument getECertRQDoc() {
		return this.m_ecertRQDoc;
	}
	public void setECertificateRQDoc(RetrieveECertificateRequestDocument p_ecertRQDoc) {
		this.m_ecertRQDoc = p_ecertRQDoc;
	}
	
	public void setRetrieveECertificateResponseDocument(RetrieveECertificateResponseDocument p_ecertRSDoc)
	{
		this.m_ecertRSDoc = p_ecertRSDoc;
	}
	public RetrieveECertificateResponseDocument getRetrieveECertificateResponseDocument()
	{
		return this.m_ecertRSDoc;
	}
	public ListResponseType[] getListResponses()
	{
		return (m_listResponses);
	}
	public void setListResponses(ListResponseType[] p_listResponses)
	{
		m_listResponses = p_listResponses;
	}
	public ECreditSearchCriteriaType getEcSrchCrit()
	{
		return m_ecreditSrchCrit;
	}
	public void setECreditSearchCriteria(ECreditSearchCriteriaType p_ecSrchCrit)
	{
		m_ecreditSrchCrit = p_ecSrchCrit;
	}
	public void setECreditRequestType(ECreditSearchRequestType p_ecRS)
	{
		m_ecreditReqType = p_ecRS;
	}
	public ECreditSearchRequestType getECreditReqType()
	{
		return m_ecreditReqType;
	}
	public ECreditSearchResponseDocument getECreditRespDoc()
	{
		return m_ecreditRespDoc;
	}

	public void setECreditRespType(ECreditSearchResponseType p_ecRSType)
	{
		m_ecreditResp = p_ecRSType;
	}
	public ECreditSearchResponseType getECreditRespType()
	{
		return m_ecreditResp;
	}

	public void setECreditRespDoc(ECreditSearchResponseDocument p_ecRSDoc)
	{
		m_ecreditRespDoc = p_ecRSDoc;
	}

	public ECreditSearchRequestDocument getECreditReqDoc()
	{
		return m_ecreditReqDoc;
	}
	public void setECreditReqDoc(ECreditSearchRequestDocument p_newEcreditRQDoc)
	{
		m_ecreditReqDoc = p_newEcreditRQDoc;
	}
	public ECreditSkyMilesSearchType getECSkyMilesRQ()
	{
			return m_ecreditSkyMilesRQ;
	}

	public void setSkyMilesSrchRQ(ECreditSkyMilesSearchType p_ecSMRQ)
	{
		   m_ecreditSkyMilesRQ = p_ecSMRQ;
	}

	public void setIDNumberSearchRQ(ECreditIDNumberSearchType p_ecIDRQ)
	{
		m_ecreditIDSearchRQ = p_ecIDRQ;
	}
	public ECreditIDNumberSearchType getIDNumberSearchRQ()
	{
		return m_ecreditIDSearchRQ;
	}
	
    public void setEcreditNumberSearch(ECreditNumberSearchType p_ecNbrSrch)
    {
       m_ecreditNumberRQ = p_ecNbrSrch; 	   
    }

    public ECreditNumberSearchType getEcreditNumberSearch()
    {
       return m_ecreditNumberRQ; 	   
    }

    public RetrieveECertificateResponseType getECertRS() {
		return m_ecertRS;
	}
	public void setECertificateRS(RetrieveECertificateResponseType p_type) {
		m_ecertRS = p_type;
	}    
    public int getOpenECreditCount()
    {
    	return m_openECreditCount;
    }
    public void setOpenECreditCount(int p_openCount)
    {
    	m_openECreditCount=p_openCount;
    }
    public int getExpiredECreditCount()
    {
    	return m_expiredECreditCount;
    }
    public void setExpiredECreditCount(int p_expiredCount)
    {
    	m_expiredECreditCount=p_expiredCount;
    }
    public String getOpenECerttCount()
    {
    	return m_openECertCount;
    }
    public void setOpenECertCount(String p_openCount)
    {
    	m_openECertCount=p_openCount;
    }
    public String getExpiredECertCount()
    {
    	return m_expiredECertCount;
    }
    public void setExpiredECertCount(String p_expiredCount)
    {
    	m_expiredECertCount=p_expiredCount;
    }
}
