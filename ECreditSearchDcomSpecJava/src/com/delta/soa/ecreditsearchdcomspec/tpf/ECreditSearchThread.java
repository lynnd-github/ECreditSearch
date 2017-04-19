/*
 * Created on Jun 7, 2008
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.delta.soa.ecreditsearchdcomspec.tpf;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.delta.soa.ecreditsearchdcomspec.ECreditSearchDcomSpecImpl;
import com.delta.soa.ecreditsearchdcomspec.util.ECreditsUtil;
import com.delta.soa.miscellaneousdocument.v2.ECertificateRequestType;
import com.delta.soa.miscellaneousdocument.v2.ECreditSearchResponseDocument;
import com.delta.soa.miscellaneousdocument.v2.RetrieveECertificateRequestDocument;
import com.delta.soa.miscellaneousdocument.v2.RetrieveECertificateResponseDocument;
import com.delta.soa.miscellaneousdocument.v2.RetrieveECertificateResponseType;
import com.delta.soa.partnerutils.PnrCommonV6PartnerUtil;
import com.delta.soa.partnerutils.TicketV3PartnerUtil;
import com.delta.soa.partnerutils.TicketingProcessV3PartnerUtil;
import com.delta.soa.partnerutils.MiscellaneousDocumentV2PartnerUtil;

import com.delta.schemas.common.advisorymessage.v2.AdvisoryMessageType;
import com.delta.schemas.common.error.v4.ErrorType;
import com.delta.schemas.common.requestinfo.v4.RequestInfoType;
import com.delta.schemas.ecredit.ecredit.ECreditType;
import com.delta.schemas.ecredit.ecreditidnumbersearch.ECreditIDNumberSearchType;
import com.delta.schemas.ecredit.ecreditnumbersearch.ECreditNumberSearchType;
import com.delta.schemas.ecredit.ecreditsearchcriteria.ECreditSearchCriteriaType;
import com.delta.schemas.ecredit.ecreditsearchrequest.ECreditSearchRequestType;
import com.delta.schemas.ecredit.ecreditsearchresponse.ECreditSearchResponseType;
import com.delta.schemas.ecredit.ecreditskymilessearch.ECreditSkyMilesSearchType;
import com.delta.schemas.ecredit.eticket.ETicketType;
import com.delta.schemas.ecredit.legdetails.LegDetailsType;
import com.delta.schemas.misc.ecertificateresponse.ECertificateResponseType;
import com.delta.schemas.misc.ecertificateresponse.EcertDocumentType;
import com.delta.schemas.ticket.retrievetickettypes.v2.AdditionalTicketLevelDataType;
import com.delta.schemas.ticket.retrievetickettypes.v2.AlternateCouponFltHistoryType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ConjunctiveTicketEndorsementType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ElectronicMiscellaneousDocumentType;
import com.delta.schemas.ticket.retrievetickettypes.v2.EtRemarksType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ExchangeConjunctiveTicketDataType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ExchangeDocumentType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ExtendedCouponAssociatedDataType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ListResponseType;
import com.delta.schemas.ticket.retrievetickettypes.v2.PassengerInformationType;
import com.delta.schemas.ticket.retrievetickettypes.v2.ResidualValueType;
import com.delta.schemas.ticket.retrievetickettypes.v2.RetrieveRequestDocument;
import com.delta.schemas.ticket.retrievetickettypes.v2.RetrieveRequestType;
import com.delta.schemas.ticket.retrievetickettypes.v2.RetrieveResponseDocument;
import com.delta.schemas.ticket.retrievetickettypes.v2.RetrieveResponseType;
import com.delta.schemas.ticket.retrievetickettypes.v2.SkymilesDataType;
import com.delta.schemas.ticket.retrievetickettypes.v2.TicketCouponType;
import com.delta.schemas.ticket.retrievetickettypes.v2.TicketInformationType;
import com.delta.schemas.ticket.retrievetickettypes.v2.TicketRequestResponseType;
import com.delta.schemas.ticket.retrievetickettypes.v2.TicketRequestType;
import com.delta.schemas.ticket.retrievetickettypes.v2.TicketResponseType;
import com.delta.webservices.toolkit.common.StringUtilities;

public class ECreditSearchThread extends Thread {

    Logger m_log;
    private ECreditSearchDataContainer m_ecreditData;
    private ECreditSearchRequestType m_ecreditRQ=null;
    private ECertificateRequestType m_ecertRequest=null;
    private RetrieveECertificateRequestDocument m_ecertRQDoc =null;
    
	protected ECreditSearchDcomSpecImpl m_ecreditSrchImpl=null;

    private String m_newSrchType = null;
    private boolean OPEN_ECREDITS_EXIST = false;
    private int EXPIRED_ECREDIT_COUNT = 0;
    private int OPEN_ECREDIT_COUNT=0;
    //private boolean bUnHookETickets=true;
    private String unHookETickets="true";
	/**
	 * Strings for SSC calls.
	 */
	public PnrCommonV6PartnerUtil m_pnrV6PartnerUtil = null;
	public TicketingProcessV3PartnerUtil m_tktProcV3PartnerUtil = null;
	public TicketV3PartnerUtil m_tktV3PartnerUtil = null;
	public MiscellaneousDocumentV2PartnerUtil m_miscDocV2PartnerUtil = null;

    public ECreditSearchThread(Logger p_m_log, ECreditSearchDataContainer p_ecreditData,
    		String newSrchType, ECreditSearchDcomSpecImpl p_ecreditSrchImpl, ECertificateRequestType p_ecertRequest)
    {
        m_log = p_m_log;
        m_ecreditData = p_ecreditData;
        m_ecreditRQ = m_ecreditData.getECreditReqType();
        m_ecertRQDoc = m_ecreditData.getECertRQDoc();
        m_newSrchType = null;
        if (newSrchType != null)
          m_newSrchType = newSrchType;
        m_ecreditSrchImpl = p_ecreditSrchImpl;
         unHookETickets = m_ecreditSrchImpl.getUnHookETickets();
         m_log.debug("\n*** unHookETicket = " + unHookETickets);
        m_pnrV6PartnerUtil = new PnrCommonV6PartnerUtil();
        m_pnrV6PartnerUtil.setLogger(m_log);
        m_pnrV6PartnerUtil.setReference(m_ecreditSrchImpl.getPnrCommon());
        m_pnrV6PartnerUtil.setRunAsMain(m_ecreditSrchImpl.m_runAsMain);
        
        m_tktProcV3PartnerUtil = new TicketingProcessV3PartnerUtil();
        m_tktProcV3PartnerUtil.setLogger(m_log);
        m_tktProcV3PartnerUtil.setReference(m_ecreditSrchImpl.getTicketingProcess());
        m_tktProcV3PartnerUtil.setRunAsMain(m_ecreditSrchImpl.m_runAsMain);
        
        m_tktV3PartnerUtil = new TicketV3PartnerUtil();
        m_tktV3PartnerUtil.setLogger(m_log);
        m_tktV3PartnerUtil.setReference(m_ecreditSrchImpl.getTicket());
        m_tktV3PartnerUtil.setRunAsMain(m_ecreditSrchImpl.m_runAsMain);
        
        m_miscDocV2PartnerUtil = new MiscellaneousDocumentV2PartnerUtil();
        m_miscDocV2PartnerUtil.setLogger(m_log);
        m_miscDocV2PartnerUtil.setReference(m_ecreditSrchImpl.getMiscDocument());
        m_miscDocV2PartnerUtil.setRunAsMain(m_ecreditSrchImpl.m_runAsMain);
        
        m_ecertRequest = p_ecertRequest;
        
    }
    
	public void run()
	{
		ECreditSearchResponseDocument newECRSDoc = m_ecreditData.getECreditRespDoc();
		ECreditSearchResponseType newECRS = m_ecreditData.getECreditRespType();
		//Retrieve2RequestDocument ret2TktRQDoc = Retrieve2RequestDocument.Factory.newInstance();
		//Retrieve2RequestType ret2TktRQ = ret2TktRQDoc.addNewRetrieve2Request();

		//TicketingRequestParamsType tktRQParms = ret2TktRQ.addNewTicketingRequestParams();
		//RequestInfoType rqInfo = ret2TktRQ.addNewRequestInfo();

		RetrieveRequestDocument retTktRQDoc = RetrieveRequestDocument.Factory.newInstance();
		RetrieveRequestType retTktRQ = retTktRQDoc.addNewRetrieveRequest();
		TicketRequestType tktRQParms = retTktRQ.addNewTicketRequest();
		RequestInfoType rqInfo = retTktRQ.addNewRequestInfo();
		Date today = new Date();
		long time = today.getTime();

		try
		{
			if (newECRS == null)
				newECRS = ECreditSearchResponseType.Factory.newInstance();
			// -- Retrieve the ECoupons from the Oracle database
			if (m_ecreditData.getECertRQ() != null)
			{
				retrieveEcertificates((RetrieveECertificateRequestDocument) m_ecertRQDoc, newECRS);
				m_ecreditData.setECertificateRS(newECRS.getRetrieveECertificateResponse());
				m_ecreditData.setOpenECertCount(newECRS.getECertCount());
				m_ecreditData.setExpiredECertCount(newECRS.getRecentExpiredECertCount());
				if (m_ecertRQDoc.getRetrieveECertificateRequest().getECertificateRequest().getFrequentFlyerNumber() != null)
				{
					m_ecreditData.setECreditSearchCriteria(this.m_ecreditData.getEcSrchCrit());
				}
			} else
			{
				// **** Lets get the ECREDITS now from DELTAMATIC ******
				// call Ticket.Retrieve Partner Service to retrieve the Ecredits
				// from Deltamatic
				if (m_ecreditRQ.getECreditSearchCriteria() != null)
				{
					tktRQParms.setTrustedSource("Y");
					tktRQParms.setReturnResidualValueInCurrency(m_ecreditRQ.getECreditSearchCriteria().getCurrencyCode());
					tktRQParms.setProvideResidualValue("N");

					if (m_ecreditRQ != null)
					{
						rqInfo.setApplicationId(m_ecreditRQ.getRequestInfo().getApplicationId());
						rqInfo.setAppChannelName(m_ecreditRQ.getRequestInfo().getAppChannelName());
						rqInfo.setTestLab(m_ecreditRQ.getRequestInfo().getTestLab());
						rqInfo.setTransactionId(m_ecreditRQ.getRequestInfo().getTransactionId());
					}

					// See if there is a SkyMiles # to search By in the Search
					// Reqeues
					if (m_ecreditRQ.getECreditSearchCriteria().getECreditSkyMilesSearch() != null)
					{
						// Set up the TicketV2.Retrieve2Request by copying in the
						// parameters from the ECreditRequest
						tktRQParms.setSkyMilesNumber(m_ecreditRQ.getECreditSearchCriteria().getECreditSkyMilesSearch().getSmNumber());
						tktRQParms.setOptionNumberForDisplayRequest("2");
					} else
					{
						// Client Sent in an ID number such as TravelAgency,
						// CorpID, SkyBonus#...etc
						if (m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch() != null)
						{
							// Set up the TicketV2.Retrieve2Request by copying in the
							// parameters from the ECreditRequest
							// set to 6 for request by Agency/Corp/Skybonus
							tktRQParms.setOptionNumberForDisplayRequest("6");

							// See if there is a Corp ID, Agency ID, or SkyBonus Number
							// to search by
							// if
							// (m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getAgencyNumber()!=
							// null)
							if (StringUtilities.isNotNullNotEmpty(m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getAgencyNumber()))
								tktRQParms.setCorporateId(m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getAgencyNumber());
							else if (StringUtilities.isNotNullNotEmpty(m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getCorpID()))
								tktRQParms.setCorporateId(m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getCorpID());
							else if (StringUtilities
									.isNotNullNotEmpty(m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getSkyBonusNumber()))
								tktRQParms.setCorporateId(m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch().getSkyBonusNumber());
						}
					}
					RetrieveResponseDocument retTktRSDoc = RetrieveResponseDocument.Factory.newInstance();
					RetrieveResponseType retTktRS = null;
					//Retrieve2ResponseDocument ret2TktRSDoc = Retrieve2ResponseDocument.Factory.newInstance();
					//Retrieve2ResponseType ret2TktRS = null;
					// ECreditSearchResponseDocument newECRSDoc =
					// m_ecreditData.getECreditRespDoc();
					// ECreditSearchResponseType newECRS =
					// m_ecreditData.getECreditResp();
					// THis section is for searching for individual Document numbers.
					// There should only be 1 as the threads in ECreditDataSource are
					// set up
					// to create 1 thread for each individual document search request
					if ((m_newSrchType != null && m_newSrchType.equalsIgnoreCase(ECreditsUtil.DOCNUMBER_SEARCH))
							|| m_ecreditData.getOriginalSearchType().equalsIgnoreCase(ECreditsUtil.DOCNUMBER_SEARCH))
					{
						// Set up the Request to call Partner Service
						// TicketingprocessV3
						// --only set this if it's an ETicket
						// -- Not all doc#s are accounted for. But most should be here
						// -- Updated for Air4. Removed 006
						String docNumbType = m_ecreditData.getDocumentNumber().substring(3, 6);
						if (docNumbType != "054" && docNumbType != "058" && docNumbType != "064" && docNumbType != "065" && docNumbType != "066"
								&& docNumbType != "067" && docNumbType != "068" && docNumbType != "069" && docNumbType != "270")
							tktRQParms.setProvideResidualValue("Y");
						/*************************************************************
						 * if (!m_ecreditData.getDocumentNumber().startsWith("006054") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006058") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006064") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006065") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006066") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006067") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006068") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006069") &&
						 * !m_ecreditData.getDocumentNumber().startsWith("006270"))
						 * tktRQParms.setProvideResidualValue("Y");
						 ************************************************************/
						// set to 1 for request by Document Number
						tktRQParms.setOptionNumberForDisplayRequest("1");
						tktRQParms.setElectronicDocumentNumber(m_ecreditData.getDocumentNumber());
						ECreditType newEC = null;
						ECreditNumberSearchType ecNumSrch = null;
						if (m_ecreditData.getOriginalSearchType().equalsIgnoreCase(ECreditsUtil.IDNUMBER_SEARCH))
							newEC = m_ecreditData.getIDNumberSearchRQ().addNewECredit();
						else if (m_ecreditData.getOriginalSearchType().equalsIgnoreCase(ECreditsUtil.SKYMILES_SEARCH))
							newEC = m_ecreditData.getECSkyMilesRQ().addNewECredit();
						else
						{
							ecNumSrch = m_ecreditData.getEcreditNumberSearch();
							newEC = ecNumSrch.addNewECredit();
						}
						if (tktRQParms.getElectronicDocumentNumber().length() > 13)
							newEC.setUIDocumentNumber(tktRQParms.getElectronicDocumentNumber().substring(0, 13));
						else
							newEC.setUIDocumentNumber(tktRQParms.getElectronicDocumentNumber());

						// newEC.setUIDocumentNumber(tktRQParms.getElectronicDocumentNumber());
						m_log.debug("--- Calling TicketingProcV3 by DocNumber: " + tktRQParms.getElectronicDocumentNumber());
						try
						{
							//retTktRSDoc = m_tktV3PartnerUtil.retrieve(retTktRQDoc);
							retTktRSDoc = m_tktProcV3PartnerUtil.retrieve(retTktRQDoc);
							//ret2TktRSDoc = m_tktProcV3PartnerUtil.retrieve2(ret2TktRQDoc);
						} catch (Exception ex)
						{
							ErrorType tktErr = newEC.addNewError();
							tktErr.setErrorCode(ECreditsUtil.ERR_DOC_TICKETV3_PARTNER_CALL);
							tktErr.setErrorText(ECreditsUtil.ERR_DOC_TICKETV3_PARTNER_CALL_TXT);
							m_log.error("exception is " + ex.toString());
							m_log.error("Calling TicketingProcV3.retrieve Exception " + ex.getMessage() + "; Exception Class Name: "
									+ ex.getClass().getName() + "\n Request to TicketProcV3.retrieve is: \n" + tktRQParms.toString()
									+ "\n Exception Stack Trace: \n" + this.getStackTrace(ex));
							m_pnrV6PartnerUtil = null;
							m_tktProcV3PartnerUtil = null;
							m_tktV3PartnerUtil = null;
							return;
						}
						//ret2TktRS = ret2TktRSDoc.getRetrieve2Response();
						retTktRS = retTktRSDoc.getRetrieveResponse();
						if (retTktRS.getTicketRequestResponseArray(0) == null || retTktRS.getTicketRequestResponseArray(0).getTicketResponseArray() == null)
						{
							m_log.error("No Ticketing Response Returned for this document");
							ErrorType tktErr = newEC.addNewError();
							tktErr.setErrorCode(ECreditsUtil.ERR_DOC_TICKETV3_PARTNER_RESPONSE_NULL);
							tktErr.setErrorText(ECreditsUtil.ERR_DOC_TICKETV3_PARTNER_RESPONSE_NULL_TXT + tktRQParms.getElectronicDocumentNumber());
							m_pnrV6PartnerUtil = null;
							m_tktProcV3PartnerUtil = null;
							m_tktV3PartnerUtil = null;
							return;
						}
						// Copy the results from the retrieve2 call into the
						// ECreditSearch object
						// For IDNumberSearch and SkyMilesNumber Search, we will be
						// adding ECredits.
						try
						{
							populateTicketResponse(retTktRS, newEC, m_ecreditRQ.getRequestInfo());
							if (m_ecreditData.getECreditRespType() != null && m_ecreditData.getECreditRespType().getECreditCount() != null)
								OPEN_ECREDIT_COUNT = Integer.valueOf(m_ecreditData.getECreditRespType().getECreditCount()) + OPEN_ECREDIT_COUNT;

							if (m_ecreditData.getECreditRespType() != null && m_ecreditData.getECreditRespType().getRecentExpiredECreditCount() != null)
								EXPIRED_ECREDIT_COUNT = Integer.valueOf(m_ecreditData.getECreditRespType().getRecentExpiredECreditCount())
										+ EXPIRED_ECREDIT_COUNT;

							m_ecreditData.getECreditRespType().setECreditCount(String.valueOf(OPEN_ECREDIT_COUNT));
							m_ecreditData.getECreditRespType().setRecentExpiredECreditCount(String.valueOf(EXPIRED_ECREDIT_COUNT));
						} catch (Exception ex)
						{
							ErrorType tktErr = newEC.addNewError();
							tktErr.setErrorCode(ECreditsUtil.ERR_DOC_POPULATING_ECREDIT);
							tktErr.setErrorText(ECreditsUtil.ERR_DOC_POPULATING_ECREDIT_TXT);
							m_log.error("PopulateTicketResponse Exception" + ex.getMessage() + "; Exception Class Name: " + ex.getClass().getName()
									+ "\n Request for ECredit\n" + newEC + "\n Exception Stack Trace: \n" + this.getStackTrace(ex));
						}
						m_log.debug("***** Iteration for Tkt # ********" + m_ecreditData.getDocumentNumber());
						// m_ecreditData.setECreditRespDoc(newECRSDoc);
						// m_ecreditData.setECreditRespType(newECRS);
						m_pnrV6PartnerUtil = null;
						m_tktProcV3PartnerUtil = null;
						m_tktV3PartnerUtil = null;
						return;
					}

					// Call partner service Ticket.retrieve2 to search by SkyMiles or
					// IDNUmber
					// A list of ECredit numbers is returned.
					// We are going to return the list immediately back to the
					// CompositeDataSource
					// class.
					if (m_ecreditRQ.getECreditSearchCriteria().getECreditSkyMilesSearch() != null
							|| m_ecreditRQ.getECreditSearchCriteria().getECreditIDNumberSearch() != null)
					{
						m_log.debug("Calling TicketProcV3 by skymiles or idnumber");
						String searchData = null;
						if (m_ecreditRQ.getECreditSearchCriteria().getECreditSkyMilesSearch() != null)
							searchData = "SkyMilesNumber " + tktRQParms.getSkyMilesNumber();
						else
							searchData = "IDNumber " + tktRQParms.getCorporateId();
						try
						{
							// retTktRSDoc =
							// m_miscDocImpl.tktV3PartnerUtil.retrieve(retTktRQDoc);
							retTktRSDoc = m_tktProcV3PartnerUtil.retrieve(retTktRQDoc);
						} catch (Exception ex)
						{
							ErrorType tktErr = newECRS.addNewError();
							tktErr.setErrorCode(ECreditsUtil.ERR_DOC_TICKETV3_PARTNER_CALL_LIST);
							tktErr.setErrorText(ECreditsUtil.ERR_DOC_TICKETV3_PARTNER_CALL_LIST_TXT + searchData);
							m_log.error("exception is " + ex.toString());
							m_log.error("Calling TicketProcV3.retrieve by SkyMiles or IDNumber FAILED " + ex.getMessage() + "; Exception Class Name: "
									+ ex.getClass().getName() + "\n Request to TicketProcV3.retrieve is: \n" + tktRQParms.toString()
									+ "\n Exception Stack Trace: \n" + this.getStackTrace(ex));
							m_tktProcV3PartnerUtil = null;
							m_pnrV6PartnerUtil = null;
							m_tktV3PartnerUtil = null;
							return;
						}

						// Retrieve the individual Doc Numbers and send additional
						// request for individual document numbers
						if (retTktRSDoc != null)
						{
							// Check for Errors
							retTktRS = retTktRSDoc.getRetrieveResponse();
							if (retTktRS.getError() != null)
							{
								ErrorType error = ErrorType.Factory.newInstance();
								error = retTktRS.getError();
								newECRS.setError(error);
								m_ecreditData.setECreditRespDoc(newECRSDoc);
								m_log.error("No response returned from TicketProcV3.retrieve for document#: " + tktRQParms.getElectronicDocumentNumber());
								// newECRS.setECreditCount("0");
								m_tktProcV3PartnerUtil = null;
								m_pnrV6PartnerUtil = null;
								m_tktV3PartnerUtil = null;
								return;
							}
							// save this List of EDoc Numbers.
							// We will come back through and search by EDoc number.
							if (retTktRS != null && retTktRS.getTicketRequestResponseArray() != null)
							{

								TicketRequestResponseType tktRQRS = retTktRS.getTicketRequestResponseArray(0);
								if (tktRQRS.getError() != null || tktRQRS.getTicketResponseArray() == null || tktRQRS.getTicketResponseArray().length <= 0)
								{
									ErrorType tktErr = newECRS.addNewError();
									newECRS.setError(tktRQRS.getError());
									newECRS.getError().setErrorCode(ECreditsUtil.ERR_DOC_POPULATING_ECREDIT);
									m_ecreditData.setECreditRespDoc(newECRSDoc);
									m_log.error("No Documents returned from TicketProcV3.retrieve");
								}
								// TicketResponseType tktRS = tktRQRS.getTicketResponseArray(0);
								// If more than 1 is returned, then a ListResponse will be returned.
								int listRSSize = ECreditsUtil.determineListResponseSize(tktRQRS.getTicketResponseArray());
								ListResponseType[] listRS = new ListResponseType[listRSSize];
								int t = 0;
								for (int x = 0; x < tktRQRS.getTicketResponseArray().length; x++)
								{
									TicketResponseType tktRS = tktRQRS.getTicketResponseArray(x);
									if (tktRS != null && tktRS.getListResponseArray().length > 0)
									{
										// -- There can be more than 1 ListResponse.
										
										for (int j=0; j < tktRS.getListResponseArray().length; t++,j++)
										{
											listRS[t]=tktRS.getListResponseArray(j);
										}
										if (t==listRSSize)
										  m_ecreditData.setListResponses(listRS);
									}
									// If only 1 is returned, then a single Ticket
									// Response is returned.
									else
									{
										// if this is an ETIcket, We need to retrieve it
										// again with the
										// Get Residual value flag set. Searching by
										// SkyMiles# or ID# does not
										// Return the Residual Value.
										if (tktRS.getTicketInformation() != null && tktRS.getTicketInformation().getDocumentType().equalsIgnoreCase("T"))
										{
											ListResponseType[] listRSs = new ListResponseType[1];
											listRSs[0] = tktRS.addNewListResponse();
											// listRSs[0].setDocumentNumber(tktRQRS.getTicketRequest().getElectronicDocumentNumber());
											listRSs[0].setDocumentNumber(tktRS.getTicketInformation().getFirstTicketNumber());
											m_ecreditData.setListResponses(listRSs);
										}
										// If not an Eticket, then just add this one
										// document to the Response and return it.
										else
										{
											// Copy the results from the retrieve2 call
											// into the
											// ECreditSearch object
											if (m_ecreditData.getECreditRespDoc() == null)
											{
												newECRS.setECreditSearchCriteria(m_ecreditData.getEcSrchCrit());
												m_ecreditData.setSearchType(m_ecreditData.getOriginalSearchType());
												m_ecreditData.setECreditRespDoc(newECRSDoc);
											}
											ECreditType newEC = null;
											ECreditSearchCriteriaType srchCrit = m_ecreditData.getEcSrchCrit();

											if (m_ecreditData.getOriginalSearchType().equalsIgnoreCase(ECreditsUtil.IDNUMBER_SEARCH))
											{
												ECreditIDNumberSearchType ecIDNum = m_ecreditData.getIDNumberSearchRQ();
												newEC = ecIDNum.addNewECredit();
												srchCrit.setECreditIDNumberSearch(ecIDNum);
											} else
											{
												ECreditSkyMilesSearchType ecSkyMiles = m_ecreditData.getECSkyMilesRQ();
												newEC = ecSkyMiles.addNewECredit();
												srchCrit.setECreditSkyMilesSearch(ecSkyMiles);
											}
											// Copy the results from the retrieve2 call
											// into the ECreditSearch object
											// For IDNumberSearch and SkyMilesNumber
											// Search, we will be adding ECredits.
											if (tktRQRS.getError() != null || tktRQRS.getTicketResponseArray() == null
													|| tktRQRS.getTicketResponseArray().length > 0)
											{
												try
												{
													populateTicketResponse(retTktRS, newEC, m_ecreditRQ.getRequestInfo());
													if (m_ecreditData.getECreditRespType() != null && m_ecreditData.getECreditRespType().getECreditCount() != null)
														OPEN_ECREDIT_COUNT = Integer.valueOf(m_ecreditData.getECreditRespType().getECreditCount())
																+ OPEN_ECREDIT_COUNT;

													if (m_ecreditData.getECreditRespType() != null
															&& m_ecreditData.getECreditRespType().getRecentExpiredECreditCount() != null)
														EXPIRED_ECREDIT_COUNT = Integer.valueOf(m_ecreditData.getECreditRespType().getRecentExpiredECreditCount())
																+ EXPIRED_ECREDIT_COUNT;

													m_ecreditData.getECreditRespType().setECreditCount(String.valueOf(OPEN_ECREDIT_COUNT));
													m_ecreditData.getECreditRespType().setRecentExpiredECreditCount(String.valueOf(EXPIRED_ECREDIT_COUNT));
												} catch (Exception ex)
												{
													ErrorType tktErr = newECRS.addNewError();
													tktErr.setErrorCode(ECreditsUtil.ERR_DOC_POPULATING_ECREDIT);
													tktErr.setErrorText(ECreditsUtil.ERR_DOC_POPULATING_ECREDIT_TXT);
													m_log.error("PopulateTicketResponse Exception" + ex.getMessage() + "; Exception Class Name: "
															+ ex.getClass().getName() + "\n Request For ECredit:\n" + newEC + "\n Exception Stack Trace: \n"
															+ this.getStackTrace(ex));
												}
											}
										}
									}
								}
							} // End if TktResponse
							
						}
					}
				}
			} // else
		} catch (Exception ex)
		{
			ErrorType error = ErrorType.Factory.newInstance();
			error.setErrorText("Error calling TicketProcV3 Service...");
			error.setReasonCode(ex.toString());
			newECRS.setError(error);

			ex.getLocalizedMessage().toString();
			m_ecreditData.setECreditRespDoc(newECRSDoc);
			m_ecreditData.setThreadId(this.getId());
			m_log.error("Composite ECreditThread error: Exception: Message:" + ex.getMessage() + "; Exception Class Name: " + ex.getClass().getName()
					+ "\n Request to ECreditRetrieve:\n" + m_ecreditData.getECreditReqDoc() + "\n Exception Stack Trace: \n" + this.getStackTrace(ex));
			// throw ex;
		}

		finally
		{
		}
		m_tktProcV3PartnerUtil = null;
		m_pnrV6PartnerUtil = null;
		m_tktV3PartnerUtil = null;
	}

	private void retrieveEcertificates(RetrieveECertificateRequestDocument eCertReqDoc, ECreditSearchResponseType ecreditResponse)
	{
		RetrieveECertificateResponseDocument ecertRespDoc = RetrieveECertificateResponseDocument.Factory.newInstance();
				
		try
		{
			if (m_log.isDebugEnabled())
				m_log.debug("ECertThread: ecertRQ = " + eCertReqDoc.toString());
			ecertRespDoc = m_miscDocV2PartnerUtil.retrieveECertificate(eCertReqDoc);       
			ErrorType error = ErrorType.Factory.newInstance();
			// Copy the ECertificate Response into the ECreditSearch Response;
			if (ecertRespDoc != null)
			{
				int count = 0;
				ECreditsUtil ecreditsUtil = new ECreditsUtil();
				if (m_log.isInfoEnabled())
					m_log.info("** RetrieveEcerts Response ***" + ecertRespDoc.toString());

				if (ecertRespDoc.getRetrieveECertificateResponse().getECertificateResponseArray() != null)
				{
					RetrieveECertificateResponseType retrvECertRS =ecertRespDoc.getRetrieveECertificateResponse(); 
					if (retrvECertRS.getECertificateResponseArray(0).getFreeformText() != null)
					{
					  if (retrvECertRS.getECertificateResponseArray(0).getEcertDocumentArray() == null ||
					  (retrvECertRS.getECertificateResponseArray(0).getEcertDocumentArray() != null &&
					  retrvECertRS.getECertificateResponseArray(0).getEcertDocumentArray().length <=0))
					  {
						  
						error = retrvECertRS.addNewError();
						error.setErrorText(retrvECertRS.getECertificateResponseArray(0).getFreeformText());
						error.setErrorCode(ECreditsUtil.ERR_ECERT_NONE_FOUND);
					  }
					}
				    else
				    {
						m_log.debug("ecertRESPDoc = "+ecertRespDoc);
					    ecreditsUtil.addECertificatesToResponse(retrvECertRS.getECertificateResponseArray(), ecreditResponse);
				    } 
				   if (retrvECertRS.getECertificateResponseArray(0).getEcertDocumentArray().length > 0 )
				   {
					   ecreditResponse.setECertCount(String.valueOf(ecreditsUtil.getEcertCount()));
					   ecreditResponse.setOpenCertsOrECreditsExist("TRUE");
					   ecreditResponse.setRecentExpiredECertCount(String.valueOf(ecreditsUtil.getRecentExpiredCount()));
				   }
				}
				else
				{
					error = ecertRespDoc.getRetrieveECertificateResponse().addNewError();
					error.setErrorText(ECreditsUtil.ERR_ECERT_NO_RESPONSE_TXT);
					error.setErrorCode(ECreditsUtil.ERR_ECERT_NO_RESPONSE);
				}
			}
		}
		catch(Exception ex)
		{
			m_ecreditData.setException(ex);
			m_ecreditData.setThreadId(this.getId());
			long lECertCallErrorTime = System.currentTimeMillis();
			String sTime = String.valueOf(lECertCallErrorTime);
			m_log.error("-----" + sTime + ": Call to RetrieveECertificates Failed ----" +'\n');
			m_log.error("retrieveEcertificates error: Exception: " + ex.getMessage() + "\nStack Trace: " + 
					this.getStackTrace(ex) + "\n Request to ECert Partner Service:\n" + eCertReqDoc);
		}
		finally
		{
			
			// this.setECertResponse(m_ecertData);
			// m_ecertData.setECreditRespDoc(ecreditRespDoc);
			// Thread.currentThread().setContextClassLoader(rootCLSLoader);
		}
		// return ecreditResponse;
		
	}
    public void cleanTktRQParms(RetrieveRequestType retTktRQ)
    {
    	int j=0;
    	while ((j=retTktRQ.getTicketRequestArray().length) > 0)
    	{
   		    retTktRQ.removeTicketRequest(j-1);
    	}

    }


    /*
		 * <TicketRequestResponse> <TicketRequest BuildTle="Y"
		 * ElectronicDocumentNumber="0062320384152" NumberOfItemsToReturn="250"
		 * OptionNumberForDisplayRequest="1" ProvideResidualValue="Y"
		 * ReturnHistoryData="N" TrustedSource="Y"
		 * xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
		 * xmlns:v1="http://schemas.delta.com/ticket/retrievetickettypes/v1"
		 * xmlns:v2="http://schemas.delta.com/common/userauthdata/v2"
		 * xmlns:v21="http://schemas.delta.com/common/ptrauthdata/v2"
		 * xmlns:v4="http://schemas.delta.com/common/requestinfo/v4"
		 * xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"/>
		 * <TicketResponse> <DocumentResponse DisplayType="D"/> <ResidualValue
		 * CurrencyCode="USD" CurrencyDecimalPlaces="2" FlownBaseFare="0.00"
		 * FlownTaxes="0.00" FlownTotalFare="0.00" OriginalBaseFare="453.95"
		 * OriginalFareCalc="ATL DL LAX226.98KE07A0UJ DL ATL226.97KE07A0UJ
		 * USD453.95END ZP ATLLAX XF ATL4.5LAX4.5 TAX AY 5.00 XF 9.00 ZP 7.80 US
		 * 34.05" OriginalTaxes="55.85" OriginalTotalFare="509.80"
		 * Penalty="150.00" ResidualBaseFare="453.95" ResidualTaxes="55.85"
		 * ResidualTotal="359.80" ResidualTotalFare="509.80"/>
		 * <PassengerInformation LengthOfGivenName="02" LengthOfSureName="08"
		 * PassengerName="TESTACCTED" PassengerType="A"/> <PhoneNumber
		 * LengthOfPhoneNumber="10" PhoneNumber="3333333333"/> <TicketInformation
		 * AgentType="A" AllOpenItinerary="N" BartsTestLocation="Y"
		 * BaseAmountIsNonDecimal="N" BaseFare="453.95"
		 * BaseFareDigitsToRightOfDecimal="2" BookingAgent="G" CompanyCode="DL"
		 * CurrencyCodeOfBaseFare="USD" CurrencyCodeOfTotalFare="USD"
		 * DataRequiredForRepriceIsNotPresent="N" DestinationAirportCode="ATL"
		 * DirectConnectIssued="N" DocumentType="T" ETLockIndForOATickets="N"
		 * ElectronicExcessBaggage="N" ElectronicSST="N"
		 * FareCalcReplacementNotSuccessful="N" FareLOGIXTicket="N"
		 * FarecalcParserError="N" FarecalcReplacementSuccessful="N"
		 * FirstTicketNumber="0062320384152" ForwardReferenceEligibleDocument="N"
		 * GMTPNRCreateDate="03JAN13" InfantETKT="N" InvolReissueTicket="N"
		 * LastTicketNumber="0062320384152" LengthOfCityStateAgencyName="8"
		 * MayContainOneOfThreeInformation="LAX WEB" MustShowCreditCard="N"
		 * NonDecimalInd="N" NonEndorsable="N" NonRefundable="N"
		 * NotValidForReprice="N" NumberOfFlightSegments="2"
		 * OriginAirportCode="ATL" OriginalSalesInd="N"
		 * PNRFileAddressToWhichTicketIsHooked="6E0EB86A" PenaltyApplies="N"
		 * PricingInd="4" PrintedByETPRFunction="N"
		 * PseudoCityOfAgentInitiatingRequest="LAX" PseudoTicket="N"
		 * ReceiptFaxed="N" ReceiptMailed="Y" ReservationSystemIdCode="WEB"
		 * ReservationSystemOfPNRAddress="DL" SDCModifiedFarePNR="N"
		 * SameDayConfirmedPNR="N" SettlementAuthcodeForVoidedTicket="N"
		 * StatisticalCode="D" SymbolicPNRAddress="GVY6OG"
		 * SymbolicPNRAddressInd="N" SystemProvider="0066"
		 * TicketCreateDate="03JAN13" TicketIsDecontented="N"
		 * TicketedISOCountryCode="US" TicketingAgentId="DL/WWWEB"
		 * TicketingDate="03JAN13" TotalFare="509.80"
		 * TotalFareDigitsToRightOfDecimal="2" TotalNumberOfBooklets="1"
		 * UpgradedRepriceSegmentPresentInPNR="N" UpsellRequestInd="N"
		 * XTExists="N"/> <ConjunctiveTicketEndorsement DescriptionInTLO3="0101"
		 * Endorsement="NONREF/PENALTY/APPLIES" EndorsementLineLength="22"/>
		 * <ConjunctiveTicketEndorsement DescriptionInTLO3="0102"
		 * Endorsement="NONREF/PENALTY/APPLIES" EndorsementLineLength="22"/>
		 * <AdditionalTicketLevelData BrandIndication="D" Category25Pricing="N"
		 * Category35Pricing="N" CustomerId="0002339754760"
		 * FareGuaranteeDate="03JAN13" PFCBreakdown="ATL4.5LAX4.5"
		 * PFCFieldLength="12" PassengerTypeCode="ADT" PlusUpInd="N"
		 * PricingInternationalItineraryInd="D" TicketExpirationDate="03JAN14"/>
		 * <LadderData FareCalculationLadderData="FP/ AX37101Q0JJJJ0000/1234"
		 * LengthOfFareCalculation="26"
		 * ReformattedFareCalculationFromValidateAgencyTransactionEngine="N"/>
		 * <LadderData FareCalculationLadderData="FC/ ATL DL LAX226.98KE07A0UJ DL
		 * ATL226.97KE07A0UJ USD453.95END ZP ATLLAX XF ATL4.5LAX4.5"
		 * LengthOfFareCalculation="88"
		 * ReformattedFareCalculationFromValidateAgencyTransactionEngine="N"/>
		 * <DTITaxItem DigitsToTheRightOfTheDecimal="2" TaxAmount="5.00"
		 * TaxAmountCurrencyCode="USD" TaxCode="AY"
		 * TaxIsIncludedInAnXTBreakdown="N" TaxIsNotPaid="N" TaxIsPaid="N"/>
		 * <DTITaxItem DigitsToTheRightOfTheDecimal="2" TaxAmount="9.00"
		 * TaxAmountCurrencyCode="USD" TaxCode="XF"
		 * TaxIsIncludedInAnXTBreakdown="N" TaxIsNotPaid="N" TaxIsPaid="N"/>
		 * <DTITaxItem DigitsToTheRightOfTheDecimal="2" TaxAmount="7.80"
		 * TaxAmountCurrencyCode="USD" TaxCode="ZP"
		 * TaxIsIncludedInAnXTBreakdown="N" TaxIsNotPaid="N" TaxIsPaid="N"/>
		 * <DTITaxItem DigitsToTheRightOfTheDecimal="2" TaxAmount="34.05"
		 * TaxAmountCurrencyCode="USD" TaxCode="US"
		 * TaxIsIncludedInAnXTBreakdown="N" TaxIsNotPaid="N" TaxIsPaid="N"/>
		 * <TotalTaxAmount DigitsToTheRightOfTheDecimal="2" NumberOfTaxItems="4"
		 * TotalTaxAmount="55.85" TotalTaxCurrencyCode="USD"/> <TicketData
		 * DocumentType="T" TicketNumber="0100"/> <ExtendedCouponAssociatedData
		 * DocumentNumber="N" MovieFlag="N" RefundCurrencyisNonDecimal="N"
		 * SeatId="24D" TicketCouponSequenceNumber="0101"/>
		 * <ExtendedCouponAssociatedData DocumentNumber="N" MovieFlag="N"
		 * RefundCurrencyisNonDecimal="N" SeatId="14B"
		 * TicketCouponSequenceNumber="0102"/> <EtRemarks
		 * FreeFormText="IPAP-10.200.234.112 / 1842Z03JAN13"
		 * ResponseCommentsLength="34" TextCode="IP"/> <AlternateCouponFltHistory
		 * ChangeOfGauge="N" CityCode="ATL" ClassOfServiceOperatedAs="K"
		 * CodeToQualifyQuantity="P" CouponStatus="O" DLSetTheInvolIndication="N"
		 * DcsOrPnlIndication="N" EtLockIndForOATickets="N"
		 * FarebasisCode="KE07A0UJ" FlightDepartureDate="15FEB13"
		 * FlightDepartureTime="940A" FlightDestinationAirport="LAX"
		 * MarketedByFlightNumber="1555" MarketedClassOfService="K"
		 * MarketingCarrierCode="DL" MustShowCreditCard="N" NonEndorsable="N"
		 * NonRefunable="N" OperatedByCarrierCode="DL"
		 * OperatedByFlightNumber="1555" PenaltyApplies="N" SegmentIfNeeded="N"
		 * StatusChangedBySystem="N" TicketNumberAndCouponNbr="0101"
		 * TkneOSIExists="N"/> <AlternateCouponFltHistory ChangeOfGauge="N"
		 * CityCode="LAX" ClassOfServiceOperatedAs="K" CodeToQualifyQuantity="P"
		 * CouponStatus="O" DLSetTheInvolIndication="N" DcsOrPnlIndication="N"
		 * EtLockIndForOATickets="N" FarebasisCode="KE07A0UJ"
		 * FlightDepartureDate="22FEB13" FlightDepartureTime="630A"
		 * FlightDestinationAirport="ATL" MarketedByFlightNumber="2154"
		 * MarketedClassOfService="K" MarketingCarrierCode="DL"
		 * MustShowCreditCard="N" NonEndorsable="N" NonRefunable="N"
		 * OperatedByCarrierCode="DL" OperatedByFlightNumber="2154"
		 * PenaltyApplies="N" SegmentIfNeeded="N" StatusChangedBySystem="N"
		 * StopoverCode="O" TicketNumberAndCouponNbr="0102" TkneOSIExists="N"/>
		 * <TimeDayLogicalRecord Date="03JAN13" PlatingCarrier="DL"
		 * RecordIdentifier="C" ResidualAmtIsNonDecimal="N" Time="134223"/>
		 * <AdvisoryMessage FreeformText="RESIDUAL VALUE CANNOT BE DETERMINED" MessageNumber="1"  
		 * Process="EK8A" SscRequest="ETS8" xmlns="http://schemas.delta.com/common/advisorymessage/v2"/>
		 * </TicketResponse>
		 */

    private void populateTicketResponse(RetrieveResponseType p_retTktRS, ECreditType eCredit, RequestInfoType ecreditRQInfo)
	{
		ECreditsUtil ecreditUtil = new ECreditsUtil(m_log);
		TicketRequestResponseType tktRSType = p_retTktRS.getTicketRequestResponseArray(0);

		for (int i = 0; i < p_retTktRS.getTicketRequestResponseArray().length; i++)
		{
			String ETKTStatus = null;
			ErrorType eCreditErr = null;
			String originalDocNumber = null;
			String residualDocNumber = null;
			TicketResponseType tktRS = tktRSType.getTicketResponseArray(i);
			// Check for Errors
			ErrorType err = p_retTktRS.getTicketRequestResponseArray(i).getError();
			if (err != null)
			{
				// test
				// err.setReasonText("1234567890123456789012345678901234");
				eCreditErr = eCredit.addNewError();
				// eCredit.setErrorArray(0, err);
				// eCreditErr = err;

				if (err.getErrorCode() != null)
					eCreditErr.setErrorCode(err.getErrorCode());
				else if (err.getReasonCode() != null)
					eCreditErr.setErrorCode(err.getReasonCode());
				if (err.getReasonText() != null)
				{
					eCreditErr.setErrorText(err.getReasonText());
					if (eCreditErr.getErrorText().length() > 50)
					{
						eCreditErr.setErrorText(eCreditErr.getErrorText().substring(0, 50));
					}
				} else if (err.getErrorText() != null)
					eCreditErr.setErrorText(err.getErrorText());
				continue;
			}
			AdvisoryMessageType advMsg = null;
			if (tktRS.getAdvisoryMessageArray() != null && tktRS.getAdvisoryMessageArray().length > 0)
			{
				advMsg = tktRS.getAdvisoryMessageArray(0);
				if (advMsg.getFreeformText() != null && 
				(advMsg.getFreeformText().equalsIgnoreCase("RESIDUAL VALUE CANNOT BE DETERMINED")) ||
				 advMsg.getFreeformText().equalsIgnoreCase("NO VALUE LEFT IN TICKET"))
				{
					eCreditErr = eCredit.addNewError();
					eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_NO_RESIDUAL_VALUE);
					eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_NO_RESIDUAL_VALUE_TXT);
				}
			}
			if (p_retTktRS.getTicketRequestResponseArray(0).getTicketRequest() != null
					&& p_retTktRS.getTicketRequestResponseArray(0).getTicketRequest().getElectronicDocumentNumber() != null)
			{
				originalDocNumber = p_retTktRS.getTicketRequestResponseArray(0).getTicketRequest().getElectronicDocumentNumber();
				// Do not copy over the Check Digit.
				if (originalDocNumber.length() > 13)
					eCredit.setUIDocumentNumber(originalDocNumber.substring(0, 13));
				else
					eCredit.setUIDocumentNumber(originalDocNumber);
			}
			// Set up the Ticket Response Objects for each iteration
			TicketInformationType tktInfo = tktRS.getTicketInformation();
			if (tktInfo == null)
			{
				eCreditErr = eCredit.addNewError();
				eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_NO_TICKETINFO);
				eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_NO_TICKETINFO_TXT);
				continue;
			}
			if ("VOID".equalsIgnoreCase(tktRS.getTicketCouponArray(0).getStatusCode()))
			{
				eCreditErr = eCredit.addNewError();
				eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_IS_VOID);
				eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_IS_VOID_TXT);
				continue;
			}
			if (StringUtilities.isNotNullNotEmpty(tktInfo.getFirstTicketNumber()))
			{
				// The RAW data from deltamatic has a "check Digit" at the end.
				// do NOT return that to the client.
				if (tktInfo.getFirstTicketNumber().length() > 13)
					eCredit.setETRDocumentNumber(tktInfo.getFirstTicketNumber().substring(0, 13));
				else
					eCredit.setETRDocumentNumber(tktInfo.getFirstTicketNumber());
				residualDocNumber = eCredit.getETRDocumentNumber();
			}
			if (StringUtilities.isNotNullNotEmpty(tktInfo.getAgencyNumber()))
				eCredit.setAgencyID(tktInfo.getAgencyNumber());

			// The ETS8 SSC will always return a Record Locator. Even if it's no
			// longer hooked
			// So check this field, for the
			// PNRFileAddress to determine if it's still hooked to this Record
			// Locator.
			if (tktInfo.getCompanyCode().equalsIgnoreCase("DL"))
			{
				if (StringUtilities.isNullOrEmpty(tktInfo.getPNRFileAddressToWhichTicketIsHooked()))
				{
					eCredit.setDLRecordLocator(null);
					eCredit.setOtherRecordLocator(null);
				}
				else
				{
					// Copy the record locator accordingly.
					if (StringUtilities.isNotNullNotEmpty(tktInfo.getSymbolicPNRAddress()))
						eCredit.setDLRecordLocator(tktInfo.getSymbolicPNRAddress());

					if (StringUtilities.isNotNullNotEmpty(tktInfo.getSymbolicPNRAddressGDS()))
						eCredit.setOtherRecordLocator(tktInfo.getSymbolicPNRAddressGDS());
				}
			} 
			else 
				if (StringUtilities.isNullOrEmpty(tktInfo.getSymbolicPNRAddressGDS()))
				{
					eCredit.setDLRecordLocator(null);
					eCredit.setOtherRecordLocator(null);
				} 
				else
				{
					// Copy the record locator accordingly.
					if (StringUtilities.isNotNullNotEmpty(tktInfo.getSymbolicPNRAddress()))
						eCredit.setDLRecordLocator(tktInfo.getSymbolicPNRAddress());

					if (StringUtilities.isNotNullNotEmpty(tktInfo.getSymbolicPNRAddressGDS()))
						eCredit.setOtherRecordLocator(tktInfo.getSymbolicPNRAddressGDS());
				}
			
			// Set Passenger Name
			if (tktRS.getPassengerInformation() != null)
			{
				PassengerInformationType paxInfo = tktRS.getPassengerInformation();
				String tmpPaxName = paxInfo.getPassengerName();
				int lenLastName = Integer.parseInt(paxInfo.getLengthOfSureName());
				String lName = tmpPaxName.substring(0, lenLastName);
				String fName = tmpPaxName.substring(lenLastName);

				fName = fName.trim();
				eCredit.setLastName(lName);
				eCredit.setFirstName(fName);
			}

			// ****** Get Fare Info *****
			String remainingValue = "0.00";
			String originalValue = "0.00";
			if (StringUtilities.isNotNullNotEmpty(tktInfo.getBaseFare()))
			{
				// LYNN Look at this... Believe this to be wrong!!!
				// Copied from Tux..
				String tktFareType = tktInfo.getNetReportingInd();

				if (tktFareType != null && !tktFareType.equalsIgnoreCase("BT") && !tktFareType.equalsIgnoreCase("IT")
						&& !tktFareType.equalsIgnoreCase("NT"))
				{
					eCreditErr = eCredit.addNewError();
					eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_NOT_BT_IT_NT);
					eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_NOT_BT_IT_NT_TXT);
					// continue;
				}
				// if
				// (StringUtilities.isNotNullNotEmpty(tktInfo.getCurrencyCodeOfBaseFare()))
				// eCredit.setCurrencyCode(tktInfo.getCurrencyCodeOfBaseFare());
				// -- Per Deb, we should be getting currency Code of Total Fare, not
				// Base
				// -- They could be different if ticketed by an agency for Intl
				// Tkts.
				if (StringUtilities.isNotNullNotEmpty(tktInfo.getCurrencyCodeOfTotalFare()))
					eCredit.setCurrencyCode(tktInfo.getCurrencyCodeOfTotalFare());
				if (StringUtilities.isNotNullNotEmpty(tktInfo.getTotalFare()))
					eCredit.setValue(tktInfo.getTotalFare().trim());
				remainingValue = eCredit.getValue();
			}
			// Set Issue Date
			if (StringUtilities.isNotNullNotEmpty(tktInfo.getTicketCreateDate()))
			{
				// issueDate = tktInfo.getTicketCreateDate();
				eCredit.setIssueDay(tktInfo.getTicketCreateDate().substring(0, 2));
				eCredit.setIssueMonth(tktInfo.getTicketCreateDate().substring(2, 5));
				eCredit.setIssueYear("20" + tktInfo.getTicketCreateDate().substring(5, 7));
			}
			if (StringUtilities.isNotNullNotEmpty(tktInfo.getDocumentType()))
			{
				eCredit.setHighLevelDocType(tktInfo.getDocumentType());
				TicketCouponType[] tktCoupon = tktRS.getTicketCouponArray();
				ETicketType eTkt = ETicketType.Factory.newInstance();

				if (tktCoupon != null && tktCoupon.length > 0)
				{
					eCredit.setSkyMilesNumber(tktCoupon[0].getFQTVNumber());
				}
				// If the docType is "T", then this is an ETKT.
				if (eCredit.getHighLevelDocType().equalsIgnoreCase("T"))
				{
					eCredit.setHighLevelDocType("E");
					eCredit.setSubDocType("ETK");
					eCredit.setDocType("ETK");
					// Set up the Etickets attached to this ECredit
					// ETicketType eTkt = eCredit.addNewETicket();
					eTkt.setCurrencyCode(eCredit.getCurrencyCode());

					if (StringUtilities.isNotNullNotEmpty(tktInfo.getLastTicketNumber())
							&& !tktInfo.getFirstTicketNumber().equalsIgnoreCase(tktInfo.getLastTicketNumber()))
					{
						if (tktInfo.getLastTicketNumber().length() > 13)
							eTkt.setLastConjDocNumber(tktInfo.getLastTicketNumber().substring(0, 12));
						else
							eTkt.setLastConjDocNumber(tktInfo.getLastTicketNumber());
						Long fDocNumber = new Long(tktInfo.getFirstTicketNumber());
						Long lDocNumber = new Long(tktInfo.getLastTicketNumber());
						Long numConjDocs = lDocNumber - fDocNumber;
						// eCredit.getETicket().setNumConjuctive(numConjDocs.toString());
						eTkt.setNumConjuctive(numConjDocs.toString());
					}
				}
				for (int j = 0; j < tktCoupon.length; j++)
				{
					if ("OPEN".equalsIgnoreCase(tktCoupon[j].getStatusCode()) || ("O-XX").equalsIgnoreCase(tktCoupon[j].getStatusCode())
							|| ("O").equalsIgnoreCase(tktCoupon[j].getStatusCode()))
						ETKTStatus = "OPEN";
					else
						ETKTStatus = tktCoupon[j].getStatusCode();
					// -- For EGIFS, we have residuals chained together.
					// -- So check the ExtendedCouponAssociatedData for a residual

					if (eCredit.getHighLevelDocType().equalsIgnoreCase("E"))
					{
						if (!tktCoupon[j].getMarketingFlightNumber().equalsIgnoreCase("VOID"))
						{
							LegDetailsType leg = eTkt.addNewLegDetails();
							leg.setDept(tktCoupon[j].getTickeCouponOriginatingAirport());
							leg.setCpnNumber(tktCoupon[j].getTicketCouponNumber());
							leg.setArr(tktCoupon[j].getTicketCouponDestinationAirport());
							// format of date is DDMMMYY
							// We want to
							leg.setDeptDay(tktCoupon[j].getTicketLegDepartureDate().substring(0, 2));
							leg.setDeptMonth(tktCoupon[j].getTicketLegDepartureDate().substring(2, 5));
							leg.setDeptYear("20" + tktCoupon[j].getTicketLegDepartureDate().substring(5, 7));
							leg.setStatus(tktCoupon[j].getStatusCode());
							ETKTStatus = leg.getStatus();

						}
					}
				}

				if (tktRS.getTicketStatusRemark() != null)
				{
					// If this field is blank, then this is OPEN.
					if (StringUtilities.isNotNullNotEmpty(tktRS.getTicketStatusRemark().getTicketStatusCode()))
					{
						if (tktRS.getTicketStatusRemark().getTicketStatusCode().equalsIgnoreCase("S"))
						{
							ETKTStatus = "SUSP";
						}
						if (tktRS.getTicketStatusRemark().getTicketStatusCode().equalsIgnoreCase("V"))
						{
							ETKTStatus = "VOID";
						}
						if (tktRS.getTicketStatusRemark().getTicketStatusCode().equalsIgnoreCase("P"))
						{
							ETKTStatus = "PAPER";
						}
						this.OPEN_ECREDITS_EXIST = false;
					}
					// else
					// {
					// ETKTStatus = "OPEN";
					// ETKTStatus = ETKTStatus;
					// this.OPEN_ECREDITS_EXIST=true;
					// }
				}
				// -- Enhancement Defect #35893
				// -- Set STatus to CLOSED for these types
				if (ETKTStatus.equalsIgnoreCase("VOID"))
				{
					eCredit.setStatus("VOID");
				}
				eCredit.setStatus(ETKTStatus);
				// Set the Residual Value.
				if (eCredit.getHighLevelDocType().equalsIgnoreCase("E"))
				{
					eCredit.setETicket(eTkt);
					if (tktRS.getResidualValue() != null)
					{
						ResidualValueType resValue = tktRS.getResidualValue();
						eCredit.getETicket().setUsableValue(resValue.getResidualTotal());
						eCredit.getETicket().setPenaltyValue(resValue.getPenalty());
						if (!ecreditUtil.auditAmount(eCredit.getETicket().getUsableValue()))
						{
							eCreditErr = eCredit.addNewError();
							eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_NO_RESIDUAL_VALUE);
							eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_NO_RESIDUAL_VALUE_TXT);
						}
					}
				}
				// }
			}
			if (eCredit.getDocType() == null && eCredit.getHighLevelDocType().equalsIgnoreCase("D"))
			{
				eCredit.setDocType("DTV");
				eCredit.setSubDocType("DTV");
				eCredit.setStatus(ETKTStatus);
			}
			if (eCredit.getDocType() == null && eCredit.getHighLevelDocType().equalsIgnoreCase("M"))
			{
				eCredit.setDocType("MCO");
				eCredit.setSubDocType("MCO");
			}
			if (eCredit.getDocType() != null && eCredit.getSubDocType() == null)
				eCredit.setSubDocType(eCredit.getDocType());

			// SkyMiles Data
			if (tktRS.getSkymilesData() != null)
			{
				SkymilesDataType skyMilesData = tktRS.getSkymilesData();
				if (StringUtilities.isNotNullNotEmpty(skyMilesData.getSmAccountNumber()))
					eCredit.setSkyMilesNumber(skyMilesData.getSmAccountNumber());
			}

			EtRemarksType[] etRemarks = tktRS.getEtRemarksArray();
			if (etRemarks != null)
			{
				for (EtRemarksType etRemark : etRemarks)
				{
					if (etRemark.getFreeFormText() != null && etRemark.getTextCode().equals("SP"))
					{
						eCredit.setSPID(etRemark.getFreeFormText());
						break;
					}
				}
			}
			// Get Expiration Date
			if (tktRS.getAdditionalTicketLevelDataArray() != null && tktRS.getAdditionalTicketLevelDataArray().length > 0)
			{
				AdditionalTicketLevelDataType[] tktData = tktRS.getAdditionalTicketLevelDataArray();
				for (int j = 0; j < tktData.length; j++)
				{
					if (StringUtilities.isNotNullNotEmpty(tktData[j].getTicketExpirationDate())
							&& !tktData[j].getTicketExpirationDate().equalsIgnoreCase("NONE"))
					{
						String expDate = tktData[j].getTicketExpirationDate();
						eCredit.setExpiryDay(expDate.substring(0, 2));
						eCredit.setExpiryMonth(expDate.substring(2, 5));
						eCredit.setExpiryYear("20" + expDate.substring(5, 7));
					}
				}
			}
			// Set REfundable status based on Endorsements
			ConjunctiveTicketEndorsementType[] tktEndorsements = tktRS.getConjunctiveTicketEndorsementArray();
			if (tktEndorsements == null || tktEndorsements.length <= 0)
				eCredit.setRefundable("Y");
			else
			{
				for (int j = 0; j < tktEndorsements.length; j++)
				{
					if (StringUtilities.isNullOrEmpty(tktEndorsements[j].getEndorsement()))
						eCredit.setRefundable("Y");
					else
					{
						if (tktEndorsements[j].getEndorsement().indexOf("NON") >= 0 || tktEndorsements[j].getEndorsement().indexOf("PENALTY") >= 0)
							eCredit.setRefundable("N");
						else
							eCredit.setRefundable("Y");
					}
				}
			}
			// TODO There's is no Exch Doc Type that I can find..
			// Revisit this.
			ExchangeDocumentType[] exchDoc = tktRS.getExchangeDocumentArray();
			if (exchDoc != null && exchDoc.length > 0)
			{
				eCredit.setExchangedToDocNbr(exchDoc[0].getExchangeDocumentOrCouponNumbers());
				originalValue = exchDoc[0].getTotalFareAmount();
			}

			// -- This section has the original issue date.
			// -- Need to populate that for Forward Chained Residual type
			// Documents.
			ExchangeConjunctiveTicketDataType[] exchConjTkt = tktRS.getExchangeConjunctiveTicketDataArray();
			if (exchConjTkt != null && exchConjTkt.length > 0)
			{
				eCredit.setExchangedToDocNbr(exchConjTkt[0].getConunctiveTicketNbr());
				if (exchConjTkt[0].getOrigIssueDate() != null)
				{
					eCredit.setIssueDay(exchConjTkt[0].getOrigIssueDate().substring(0, 2));
					eCredit.setIssueMonth(exchConjTkt[0].getOrigIssueDate().substring(2, 5));
					eCredit.setIssueYear("20" + exchConjTkt[0].getOrigIssueDate().substring(5, 7));
				}
			}

			// ETKT38
			ElectronicMiscellaneousDocumentType[] eMiscDocs = tktRS.getElectronicMiscellaneousDocumentArray();
			if (eMiscDocs != null && eMiscDocs.length > 0)
			{
				for (int j = 0; j < eMiscDocs.length; j++)
				{
					ElectronicMiscellaneousDocumentType eMiscDoc = eMiscDocs[j];
					int iEmdID = 0;
					String emdID = eMiscDoc.getMiscellaneousDocumentDataIdCode();
					// This is a bandaid. Travelport changed the format of the LREC33
					// we are expecting an integer in the emdIDCode field. They are
					// sending a string
					// sometimes.
					if (!isEmdIDOK(emdID))
						continue;
					else
						iEmdID = Integer.parseInt(emdID);

					switch (iEmdID)
					{
					// General Document Description "001"
					case ECreditsUtil.DataIDSubType:
						eCredit.setDocType(eMiscDoc.getMiscellaneousDocumentTypeCode().trim());
						if (eCredit.getDocType().equalsIgnoreCase("GIF") && eCredit.getExpiryDay() != null)
						{
							eCredit.setExpiryDay(null);
							eCredit.setExpiryMonth(null);
							eCredit.setExpiryYear(null);
						}
						eCredit.setSubDocType(eMiscDoc.getMiscellaneousDocumentSubTypeCode().trim());
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getNonDecimalCurrency()))
							eCredit.setDecimalCurrencyInd(eMiscDoc.getNonDecimalCurrency());
						else
							eCredit.setDecimalCurrencyInd("N");
						if (eMiscDoc.isSetMiscellaneousDocumentCurrencyCode())
							eCredit.setCurrencyCode(eMiscDoc.getMiscellaneousDocumentCurrencyCode().trim());
						// -- If EGIF and residual exists, then the UIDoc# and etrDoc#
						// will not
						// -- Match and the ECredit.value was set earler. Do NOT
						// overwrite value.
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentAmount())
								&& (eCredit.getUIDocumentNumber() == eCredit.getETRDocumentNumber()))
							eCredit.setValue(eMiscDoc.getMiscellaneousDocumentAmount());
						eCredit.setDocDescription(eMiscDoc.getMiscellaneousDocumentData().trim());
						break;
					// Not used by dcom "002"
					case ECreditsUtil.DataIDPurchName:
						break;
					// currently not used by dcom "003"
					case ECreditsUtil.DataIDTktInfo:
						break;
					// not used by dcom "004"
					case ECreditsUtil.DataIDFltInfo:
						break;
					// Reason for creating this document "005
					case ECreditsUtil.DataIDReason:
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentDataIdCode()))
							eCredit.setDataIDCode(eMiscDoc.getMiscellaneousDocumentDataIdCode().trim());
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentData()))
						{
							eCredit.setDataText(eMiscDoc.getMiscellaneousDocumentData().trim());
							if (eCredit.getDataText() != null)
								eCredit.setDataText(eCredit.getDataText().trim());
						}
						if (eCredit.getDataText() != null)
							eCredit.setEMDText(eCredit.getDataText().trim());
						break;
					// "006" Choice Mileage. DotCom currently not using this.
					case ECreditsUtil.DataIDBonusSkyMiles:
						break;

					// "007"
					case ECreditsUtil.DataIDSKyMilesRedeemed:
						break;

					case ECreditsUtil.DataIDPPRFee:
						break;
					// "009" Choice Text
					case ECreditsUtil.DataIDChoiceValue:
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentData()))
							eCredit.setEMDText(eMiscDoc.getMiscellaneousDocumentData());
						break;

					// "010"
					case ECreditsUtil.DataIDOrgCertValue:
						if (eMiscDoc.getNonDecimalCurrency() != null)
							eCredit.setDecimalCurrencyInd(eMiscDoc.getNonDecimalCurrency());
						else
							eCredit.setDecimalCurrencyInd("N");
						break;

					// "011" Get the TicketDesignator
					case ECreditsUtil.DataIDTktDesignator:
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentData()))
							eCredit.setTicketDesignator(eMiscDoc.getMiscellaneousDocumentData());
						break;

					// "012"
					case ECreditsUtil.DataIDAgyCorpIDSkyBonus:
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentData()))
						{
							if (m_ecreditData.getOriginalSearchType().equalsIgnoreCase(ECreditsUtil.IDNUMBER_SEARCH))
							{
								if (StringUtilities.isNotNullNotEmpty(m_ecreditData.getIDNumberSearchRQ().getAgencyNumber()))
									eCredit.setAgencyID(eMiscDoc.getMiscellaneousDocumentData());
								if (StringUtilities.isNotNullNotEmpty(m_ecreditData.getIDNumberSearchRQ().getCorpID()))
									eCredit.setCorpID(eMiscDoc.getMiscellaneousDocumentData());
								if (StringUtilities.isNotNullNotEmpty(m_ecreditData.getIDNumberSearchRQ().getSkyBonusNumber()))
									eCredit.setSkyBonusID(eMiscDoc.getMiscellaneousDocumentData());
								break;
							}
							// if not IDNUMBER SEARCH, then we don't really know where
							// this eMiscDoc.
							// MiscellaneousData should go. So just put it in all three
							// places for now.
							eCredit.setAgencyID(eMiscDoc.getMiscellaneousDocumentData());
							eCredit.setCorpID(eMiscDoc.getMiscellaneousDocumentData());
							eCredit.setSkyBonusID(eMiscDoc.getMiscellaneousDocumentData());
						}

						break;

					// "013"
					case ECreditsUtil.DataIDRedemptionCode:
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentData()))
							eCredit.setRedemptionCode(eMiscDoc.getMiscellaneousDocumentData());
						break;

					// "014" Get Original Document Number.
					case ECreditsUtil.DataIDOrgDocNumber:
						if (StringUtilities.isNotNullNotEmpty(eMiscDoc.getMiscellaneousDocumentData()))
						{
							if (eMiscDoc.getMiscellaneousDocumentData().length() > 13)
								eCredit.setOriginalDocNumber(eMiscDoc.getMiscellaneousDocumentData().substring(0, 13));
							else
								eCredit.setOriginalDocNumber(eMiscDoc.getMiscellaneousDocumentData());
						}
						break;

					// "015"
					case ECreditsUtil.DataIDEMDTranxID:
						break;

					default:
						break;
					}
				}
			}
			// for GIFS generated by ATLENCI, there will be NO hold.
			// so Only check 72 hours for all the others
			// DEFECT 49336
			// And make sure its NOT a residual GIF. ATLENCI docs generate
			// a LAXWEB residual GIF when used through delta.com. We need to weed
			// those
			// out so they are not marked with < 72 hours.
			if (eCredit.getDocType().equalsIgnoreCase("GIF"))
			{
				m_log.debug("GIF DOC: check for 72 hour hold" + '\n');
				// tktRS.getExchangeConjunctiveTicketDataArray(arg0)
				// tktRS.gete
				if (!"ATLENCI".equalsIgnoreCase(tktInfo.getMayContainOneOfThreeInformation())
						&& (("0.00".equalsIgnoreCase(originalValue) && ecreditUtil.isDocWithin72HourHold(eCredit.getIssueYear(), eCredit.getIssueMonth(),
								eCredit.getIssueDay(), tktRS.getTimeDayLogicalRecord())) || (!"0.00".equalsIgnoreCase(originalValue) && originalValue
								.equalsIgnoreCase(eCredit.getValue()))))
				{
					m_log.debug("Document# " + eCredit.getETRDocumentNumber() + " within 72 Hour Hold" + '\n');
					eCreditErr = eCredit.addNewError();
					eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_GIF_72HOUR_HOLD);
					eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_GIF_72HOUR_HOLD_TXT);
				} else
					m_log.debug("Document# " + eCredit.getETRDocumentNumber() + " NOT with 72 Hour Hold" + '\n');
			}

			if (eCredit.getValue() != null)
				remainingValue = eCredit.getValue();

			if (tktRS.getExtendedCouponAssociatedDataArray() != null)
			{
				ExtendedCouponAssociatedDataType[] extCoupons = tktRS.getExtendedCouponAssociatedDataArray();
				for (ExtendedCouponAssociatedDataType extCoupon : extCoupons)
				{
					if (extCoupon.getResultingReissueTicketNumber() != null)
					{
						if (!eCredit.getStatus().equalsIgnoreCase("OPEN"))
							eCredit.setExchangedToDocNbr(extCoupon.getResultingReissueTicketNumber());
					}
				}
			}
			// If searching by SkyMiles# or ID#, we don't want to show this doc
			// if forward referenced because the residual will display.
			if ("Y".equalsIgnoreCase(tktInfo.getForwardReferenceEligibleDocument()))
			{
				if (eCredit.getUIDocumentNumber() != null && !eCredit.getUIDocumentNumber().equalsIgnoreCase(eCredit.getETRDocumentNumber()))
				{

					if (!m_ecreditData.getOriginalSearchType().equalsIgnoreCase(ECreditsUtil.DOCNUMBER_SEARCH))
					{
						eCreditErr = eCredit.addNewError();
						eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_FORWARD_REFERENCED);
						eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_FORWARD_REFERENCED_TXT + eCredit.getETRDocumentNumber());
						// -- Set the ETRDocnumber to be same as UIDocNumber.
						// -- THis way it will display as CLosed with correct #
						eCredit.setETRDocumentNumber(eCredit.getUIDocumentNumber());
						eCredit.setStatus("EXCH");
						Double dRemainingValue = Double.valueOf(remainingValue);
						Double dOriginalValue = Double.valueOf(originalValue);
						BigDecimal bdRemainingValue = BigDecimal.valueOf(dOriginalValue - dRemainingValue);
						eCredit.setValue(bdRemainingValue.toString());
					}

				} else
					eCredit.setUIDocumentNumber(eCredit.getETRDocumentNumber());
			}
			// TPF made update for Expired DTVs and MCOS.
			// Get the STatus from AltnerateCouponFltHistoryType.
			if (eCredit.getDocType().equalsIgnoreCase("DTV") || eCredit.getDocType().equalsIgnoreCase("MCO"))
			{
				AlternateCouponFltHistoryType[] fltLegs = tktRS.getAlternateCouponFltHistoryArray();
				if (fltLegs != null && fltLegs[0].getStatusCode() != null && fltLegs[0].getStatusCode().equalsIgnoreCase("EXPD"))
					eCredit.setStatus(fltLegs[0].getStatusCode());
			}

			// Check to see if this document is expired
			// we do display expired documents that are < 30 days expired.
			// If more than that, then do not return the document to the Client.
			// The client should do this... but for ETKTS, we send additional
			// demand
			// codes, so it cuts down on processing time if we can weed them
			// out here. For large accounts, they may time out if we have to send.
			// all those extra demand codes.
			String isThisExpired = ecreditUtil.isExpired(eCredit.getExpiryYear(), eCredit.getExpiryDay(), eCredit.getExpiryMonth());
			// if (!isThisExpired.equalsIgnoreCase(ECreditsUtil.NOT_EXPIRED))
			if (eCredit.getStatus().equalsIgnoreCase("EXPIRED") || eCredit.getStatus().equalsIgnoreCase("EXPD"))
			{
				eCredit.setStatus("EXPIRED");
				if (isThisExpired.equalsIgnoreCase(ECreditsUtil.ERR_DOC_EXPIRED_GT_30))
				{

					m_log.debug("DOC #: " + eCredit.getETRDocumentNumber() + " is expired More than 30 days");
					eCreditErr = eCredit.addNewError();
					eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_EXPIRED_GT_30);
					eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_EXPIRED_GT_30_TXT);
					// EXPIRED_ECREDIT_COUNT++;
					// continue;
				} else
				{
					m_log.debug("DOC #: " + eCredit.getETRDocumentNumber() + " is expired within 30 days");
					eCreditErr = eCredit.addNewError();
					eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_EXPIRED_WITHIN_30);
					eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_EXPIRED_WITHIN_30_TXT);
				}
				EXPIRED_ECREDIT_COUNT++;
			}

			// TODO: add more here for checking if TOTAL is < $0
			// Check to see if all the data needed has been populated.
			// For TIcket Designator Docs, the value will be empty.
			if (StringUtilities.isNullOrEmpty(eCredit.getTicketDesignator()) && !validateAmount(eCredit.getValue()))
			{
				eCredit.setValue("0.00");
				eCreditErr = eCredit.addNewError();
				eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_VALUE_NOT_VALID);
				eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_VALUE_NOT_VALID_TXT);
				// continue;
			}

			// if (eCredit.getHighLevelDocType().equalsIgnoreCase("E") &&
			// StringUtilities.isNotNullNotEmpty(tktInfo.getPNRFileAddressToWhichTicketIsHooked())&&
			// eCredit.getStatus().equalsIgnoreCase("OPEN"))
			// {
			// eCreditErr = eCredit.addNewError();
			// eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_HAS_ACTIVE_SEGS);
			// eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_HAS_ACTIVE_SEGS_TXT);
			// }

			// / NEW CODE COMMENT OUT
			//if (eCredit.getHighLevelDocType().equalsIgnoreCase("E") &&
			//(StringUtilities.isNotNullNotEmpty(tktInfo.getPNRFileAddressToWhichTicketIsHooked()) ||
			//StringUtilities.isNotNullNotEmpty(tktInfo.getSymbolicPNRAddressGDS())) &&
			//eCredit.getStatus().equalsIgnoreCase("OPEN"))
			if (eCredit.getHighLevelDocType().equalsIgnoreCase("E") &&
			eCredit.getStatus().equalsIgnoreCase("OPEN") &&
			(StringUtilities.isNotNullNotEmpty(eCredit.getDLRecordLocator()) ||
			StringUtilities.isNotNullNotEmpty(eCredit.getOtherRecordLocator())))
			{
				// THere is a properties file that can be turned On and Off for
				// this.
				// It gets sent through CheckHealth.
				if (this.unHookETickets.equalsIgnoreCase("true"))
				{
					String recordLocator = null;
					if (eCredit.getDLRecordLocator() != null)
						recordLocator = eCredit.getDLRecordLocator();
					else if (eCredit.getOtherRecordLocator() != null)
						recordLocator = eCredit.getOtherRecordLocator();
					if (eCredit.getDLRecordLocator()!= eCredit.getOtherRecordLocator() &&
					!tktInfo.getCompanyCode().equalsIgnoreCase("DL"))
					{
						m_log.debug("ECreditSearch: NON DL ETicket with Record Locator: " + recordLocator);
						eCreditErr = eCredit.addNewError();
						eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_UNABLE_UNHOOK);
						eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_UNABLE_UNHOOK_TXT);
						eCredit.setStatus("");
					}
					else
					{
						Unhook unhook = new Unhook(m_log);
						m_log.debug("ECreditSearch: Attempting to unhook Inactive PNR: " + recordLocator);
						if (unhook.unhookInactivePNR(m_tktV3PartnerUtil, m_pnrV6PartnerUtil, eCredit, ecreditRQInfo, recordLocator, m_ecreditSrchImpl
								.getPrimaryCarrierCode()))
						{
							m_log.debug("ECreditSearch: Unhook was Successful for ETicket: " + eCredit.getUIDocumentNumber() + " for record Locator: "
									+ recordLocator);
							eCredit.setDLRecordLocator(null);
							eCredit.setOtherRecordLocator(null);
						} else
						{
							if (eCredit.getErrorArray() == null)
							{
								eCreditErr = eCredit.addNewError();
								eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_UNABLE_UNHOOK);
								eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_UNABLE_UNHOOK_TXT);
								eCredit.setStatus("");
							}
						}
					}
				} 
				else
				{
					eCreditErr = eCredit.addNewError();
					eCreditErr.setErrorCode(ECreditsUtil.ERR_DOC_UNABLE_UNHOOK);
					eCreditErr.setErrorText(ECreditsUtil.ERR_DOC_UNABLE_UNHOOK_TXT);
				}
			}
		}
		// The EDOCViewer Database needs these values set as this
		// in order for the lookups to work
		if (eCredit.getDocType() == null)
			eCredit.setDocType("NONE");
		if (eCredit.getTicketDesignator() == null)
			eCredit.setTicketDesignator("NONE");

		if (eCredit.getErrorArray().length == 0 || (eCredit.getErrorArray() != null && eCredit.getErrorArray(0).getErrorCode() == null))
		{
			if (eCredit.getStatus().equalsIgnoreCase("OPEN"))
			{
				OPEN_ECREDITS_EXIST = true;
				OPEN_ECREDIT_COUNT++;
			}
			m_log.debug("OPEN_ECREDITS_COUNT = : " + String.valueOf(OPEN_ECREDIT_COUNT));
		} else
			m_log.debug("*** CANNOT INCREMENT OPEN_ECREDIT_COUNT for doc# " + eCredit.getETRDocumentNumber());

	}

 	public void addECertificatesToResponse(ECertificateResponseType p_ECertResp, ECreditSearchResponseType p_ecResp)
	{
		com.delta.schemas.misc.ecertificateresponse.EcertDocumentType eCertDoc = null;
	    com.delta.schemas.misc.ecertificateresponse.CustomerType customer = null;
	    com.delta.schemas.misc.ecertificateresponse.PromotionsType promotions = null;

  	  com.delta.schemas.misc.ecertificateresponse.RetrieveECertificateResponseType retECertRS = p_ecResp.addNewRetrieveECertificateResponse();
	  com.delta.schemas.misc.ecertificateresponse.ECertificateResponseType ecECert = retECertRS.addNewECertificateResponse();

		for (int i = 0; i < p_ECertResp.getEcertDocumentArray().length; i++)
		{
		  EcertDocumentType p_eCertDoc = p_ECertResp.getEcertDocumentArray(i);
		  eCertDoc = ecECert.addNewEcertDocument();
		  // Set Ecertificate.Customer
		  customer = eCertDoc.addNewCustomer();
          customer.setCreationLocalTimestamp(p_eCertDoc.getCustomer().getCreationLocalTimestamp());
          customer.setCustomerExternalReferenceNumber(p_eCertDoc.getCustomer().getCustomerExternalReferenceNumber());
          customer.setCustomerLastName(p_eCertDoc.getCustomer().getCustomerLastName());
          customer.setECertificateCustomerAffiliateIdentifier(p_eCertDoc.getCustomer().getECertificateCustomerAffiliateIdentifier());
          customer.setECertificateCustomerAffiliateTypeCode(p_eCertDoc.getCustomer().getECertificateCustomerAffiliateTypeCode());
          customer.setECertificateGroupIdentifier(p_eCertDoc.getCustomer().getECertificateGroupIdentifier());
          customer.setFirstName(p_eCertDoc.getCustomer().getFirstName());
          customer.setGlobalCustomerIdentifier(p_eCertDoc.getCustomer().getGlobalCustomerIdentifier());
          customer.setMiddleName(p_eCertDoc.getCustomer().getMiddleName());
          customer.setPostalAreaCode(p_eCertDoc.getCustomer().getPostalAreaCode());
          customer.setPrefixNameCode(p_eCertDoc.getCustomer().getPrefixNameCode());
          customer.setSkyMilesNumber(p_eCertDoc.getCustomer().getSkyMilesNumber());
          customer.setSuffixNameCode(p_eCertDoc.getCustomer().getSuffixNameCode());
          customer.setWorldPerksNumber(p_eCertDoc.getCustomer().getWorldPerksNumber());

          // Set Ecertificate.CertifcateUsages
		  for (int j=0 ; j < p_ECertResp.getEcertDocumentArray().length; j++)
		  {
			  com.delta.schemas.misc.ecertificateresponse.CertificateUsagesType certUsages = eCertDoc.addNewCertificateUsages();
			  certUsages.setEBusinessFunctionStartLocalTimestamp(p_eCertDoc.getCertificateUsagesArray(i).getEBusinessFunctionStartLocalTimestamp());
			  certUsages.setTicketDocumentNumber(p_eCertDoc.getCertificateUsagesArray(i).getTicketDocumentNumber());
			  certUsages.setRecordLocatorIdentifier(p_eCertDoc.getCertificateUsagesArray(i).getRecordLocatorIdentifier());
			  certUsages.setWorldPerksNumber(p_eCertDoc.getCertificateUsagesArray(i).getWorldPerksNumber());
		  }
		  // Promotions
  	      promotions = eCertDoc.addNewPromotions();
		   promotions.setAdvancePurchaseRequiredDayCount(p_eCertDoc.getPromotions().getAdvancePurchaseRequiredDayCount());
		   promotions.setCreationAgentNumber(p_eCertDoc.getPromotions().getCreationAgentNumber());
		   promotions.setCreationAgentNumber(p_eCertDoc.getPromotions().getCreationAgentNumber());
		   promotions.setDistributionAgentNumber(p_eCertDoc.getPromotions().getDistributionAgentNumber());
		   promotions.setECertificateDocumentActualDistributionLocalDate(p_eCertDoc.getPromotions().getECertificateDocumentActualDistributionLocalDate());
		   promotions.setECertificateDocumentAgentDistributionLocalDate(p_eCertDoc.getPromotions().getECertificateDocumentAgentDistributionLocalDate());
		   promotions.setECertificateDocumentMaximumUsageCount(p_eCertDoc.getPromotions().getECertificateDocumentMaximumUsageCount());
		   promotions.setECertificateDocumentScheduledDistributionLocalDate(p_eCertDoc.getPromotions().getECertificateDocumentScheduledDistributionLocalDate());
		   promotions.setECertificateDocumentTransferIndicator(p_eCertDoc.getPromotions().getECertificateDocumentTransferIndicator());
		   promotions.setECertificatePromotionDescription(p_eCertDoc.getPromotions().getECertificatePromotionDescription());
		   promotions.setECertificatePromotionDistributionMethodCode(p_eCertDoc.getPromotions().getECertificatePromotionDistributionMethodCode());
		   promotions.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getECertificatePromotionIdentifier());
		   promotions.setECertificatePromotionName(p_eCertDoc.getPromotions().getECertificatePromotionName());
		   promotions.setECertificatePromotionStatusCode(p_eCertDoc.getPromotions().getECertificatePromotionStatusCode());
		   promotions.setExcludeMedallionIndicator(p_eCertDoc.getPromotions().getExcludeMedallionIndicator());
		   promotions.setFareBasisCode(p_eCertDoc.getPromotions().getFareBasisCode());
		   promotions.setGRSLocatorIdentifier(p_eCertDoc.getPromotions().getGRSLocatorIdentifier());
		   promotions.setGuestSkymilesMembershipRequiredIndicator(p_eCertDoc.getPromotions().getGuestSkymilesMembershipRequiredIndicator());
		   promotions.setLastUpdateAgentNumber(p_eCertDoc.getPromotions().getLastUpdateAgentNumber());
		   promotions.setMaximumStayDayCount(p_eCertDoc.getPromotions().getMaximumStayDayCount());
		   promotions.setMinimumStayDayCount(p_eCertDoc.getPromotions().getMinimumStayDayCount());
		   promotions.setMultipleFlightLegRequiredIndicator(p_eCertDoc.getPromotions().getMultipleFlightLegRequiredIndicator());
		   promotions.setNonStopFlightRequiredIndicator(p_eCertDoc.getPromotions().getNonStopFlightRequiredIndicator());
		   promotions.setRecipientSkymilesMembershipRequiredIndicator(p_eCertDoc.getPromotions().getRecipientSkymilesMembershipRequiredIndicator());
		   promotions.setRoundTripRequiredIndicator(p_eCertDoc.getPromotions().getRoundTripRequiredIndicator());
		   promotions.setSaturdayNightRequiredIndicator(p_eCertDoc.getPromotions().getSaturdayNightRequiredIndicator());
		   promotions.setSecureRateAgreementCode(p_eCertDoc.getPromotions().getSecureRateAgreementCode());
		   promotions.setSkymilesMemberOnlyIndicator(p_eCertDoc.getPromotions().getSkymilesMemberOnlyIndicator());
		   promotions.setTicketDesignatorCode(p_eCertDoc.getPromotions().getTicketDesignatorCode());
		   promotions.setTicketRefundableIndicator(p_eCertDoc.getPromotions().getTicketRefundableIndicator());
		   promotions.setPricingAgentNumber(p_eCertDoc.getPromotions().getPricingAgentNumber());
		   promotions.setPromotionAwardTypeCode(p_eCertDoc.getPromotions().getPromotionAwardTypeCode());
		   promotions.setPromotionCertificateTypeCode(p_eCertDoc.getPromotions().getPromotionCertificateTypeCode());
		   promotions.setPromotionCreationLocalTimestamp(p_eCertDoc.getPromotions().getPromotionCreationLocalTimestamp());
		   promotions.setPromotionDeltaExpressIndicator(p_eCertDoc.getPromotions().getPromotionDeltaExpressIndicator());
		   promotions.setPromotionDeltaShuttleIndicator(p_eCertDoc.getPromotions().getPromotionDeltaShuttleIndicator());
		   promotions.setPromotionDisableIndicator(p_eCertDoc.getPromotions().getPromotionDisableIndicator());
		   promotions.setPromotionDiscountQuantity(p_eCertDoc.getPromotions().getPromotionDiscountQuantity());
		   promotions.setPromotionEffectiveLocalDate(p_eCertDoc.getPromotions().getPromotionEffectiveLocalDate());
		   promotions.setPromotionExpirationLocalDate(p_eCertDoc.getPromotions().getPromotionExpirationLocalDate());
		   promotions.setPromotionGuestAllowedCount(p_eCertDoc.getPromotions().getPromotionGuestAllowedCount());
		   promotions.setPromotionImplementationLocalTimestamp(p_eCertDoc.getPromotions().getPromotionImplementationLocalTimestamp());
		   promotions.setPromotionInternetOnlyIndicator(p_eCertDoc.getPromotions().getPromotionInternetOnlyIndicator());
		   promotions.setPromotionPartnerCarrierIndicator(p_eCertDoc.getPromotions().getPromotionPartnerCarrierIndicator());
		   promotions.setPromotionPricingApprovedLocalTimestamp(p_eCertDoc.getPromotions().getPromotionPricingApprovedLocalTimestamp());
		   promotions.setPromotionTravelEndLocalDate(p_eCertDoc.getPromotions().getPromotionUpdateLocalTimestamp());
		   promotions.setPromotionUpdateLocalTimestamp(p_eCertDoc.getPromotions().getPromotionUpdateLocalTimestamp());
		   promotions.setPromotionUsageCount(p_eCertDoc.getPromotions().getPromotionUsageCount());

		   //Promotions.CompartmentCOdes
		   for (int j=0; j < p_eCertDoc.getPromotions().getCompartmentCodesArray().length; j++)
		   {
			 com.delta.schemas.misc.ecertificateresponse.CompartmentCodesType compartment =  promotions.addNewCompartmentCodes();
			 compartment.setCarrierCode(p_eCertDoc.getPromotions().getCompartmentCodesArray(j).getCarrierCode());
			 compartment.setCompartmentCode(p_eCertDoc.getPromotions().getCompartmentCodesArray(j).getCompartmentCode());
			 compartment.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getCompartmentCodesArray(j).getECertificatePromotionIdentifier());
		   }

		   //Promotions.DayOfWeeks
		   for (int j=0; j< p_eCertDoc.getPromotions().getDaysOfWeekArray().length; j++)
		   {
			  com.delta.schemas.misc.ecertificateresponse.DaysOfWeekType daysOfWeek = promotions.addNewDaysOfWeek();
			  daysOfWeek.setDayOfWeekName(p_eCertDoc.getPromotions().getDaysOfWeekArray(j).getDayOfWeekName());
			  daysOfWeek.setDayOfWeekNumber(p_eCertDoc.getPromotions().getDaysOfWeekArray(j).getDayOfWeekNumber());
			  daysOfWeek.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getDaysOfWeekArray(j).getECertificatePromotionIdentifier());
		   }

		   //Promotions.DebitCreditCards
		   for (int j=0; j<p_eCertDoc.getPromotions().getDebitCreditCardsArray().length; j++)
		   {
			  com.delta.schemas.misc.ecertificateresponse.DebitCreditCardsType dbCredCards = promotions.addNewDebitCreditCards();
			  dbCredCards.setECertificateProgramDebitCreditCardTypeCode(p_eCertDoc.getPromotions().getDebitCreditCardsArray(j).getECertificateProgramDebitCreditCardTypeCode());
			  dbCredCards.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getDebitCreditCardsArray(j).getECertificatePromotionIdentifier());
		   }

		   //Promotions.DebitCreditCardTravels
		   for (int j=0; j<p_eCertDoc.getPromotions().getDebitCreditCardTravelArray().length; j++)
		   {
			  com.delta.schemas.misc.ecertificateresponse.DebitCreditCardTravelType dbCredCardTravel = promotions.addNewDebitCreditCardTravel();
			  dbCredCardTravel.setDebitCreditCardLevelCode(p_eCertDoc.getPromotions().getDebitCreditCardTravelArray(j).getDebitCreditCardLevelCode());
			  dbCredCardTravel.setDebitCreditCardTravelTypeCode(p_eCertDoc.getPromotions().getDebitCreditCardTravelArray(j).getDebitCreditCardTravelTypeCode());
			  dbCredCardTravel.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getDebitCreditCardTravelArray(j).getECertificatePromotionIdentifier());
		   }
		   //Promotions.TermsAndConditions
		   for (int j=0; j < p_eCertDoc.getPromotions().getTermsAndConditionsArray().length; j++)
		   {
			  com.delta.schemas.misc.ecertificateresponse.TermsAndConditionsType tc = promotions.addNewTermsAndConditions();
			  tc.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getTermsAndConditionsArray(j).getECertificatePromotionIdentifier());
			  tc.setECertificateTermConditionText(p_eCertDoc.getPromotions().getTermsAndConditionsArray(j).getECertificateTermConditionText());
		   }

		   //promotions.transportations
		   for (int j=0; j < p_eCertDoc.getPromotions().getTransportationStationsArray().length; j++)
		   {
		      com.delta.schemas.misc.ecertificateresponse.TransportationStationsType stations= promotions.addNewTransportationStations();
	          stations.setECertificatePromotionIdentifier(p_eCertDoc.getPromotions().getTransportationStationsArray(j).getECertificatePromotionIdentifier());
	          stations.setECertificatePromotionMarketTypeCode(p_eCertDoc.getPromotions().getTransportationStationsArray(j).getECertificatePromotionMarketTypeCode());
	          stations.setECertificatePromotionStationCode(p_eCertDoc.getPromotions().getTransportationStationsArray(j).getECertificatePromotionStationCode());
	          stations.setOriginDestinationCode(p_eCertDoc.getPromotions().getTransportationStationsArray(j).getOriginDestinationCode());
		   }

		   eCertDoc.setCustomerSupportAgentNumber(p_eCertDoc.getCustomerSupportAgentNumber());
		   eCertDoc.setCustomerSupportAgentNumber(p_eCertDoc.getCustomerSupportAgentNumber());
		   eCertDoc.setECertificateDocumentCreationLocalTimestamp(p_eCertDoc.getECertificateDocumentCreationLocalTimestamp());
		   eCertDoc.setECertificateDocumentRedemptionLocalTimestamp(p_eCertDoc.getECertificateDocumentRedemptionLocalTimestamp());
		   eCertDoc.setECertificateDocumentUpdateLocalTimestamp(p_eCertDoc.getECertificateDocumentUpdateLocalTimestamp());
		   eCertDoc.setECertificateDocumentUsageCount(p_eCertDoc.getECertificateDocumentUsageCount());
		   eCertDoc.setECertificateGroupIdentifier(p_eCertDoc.getECertificateGroupIdentifier());
		   eCertDoc.setECertificatePromotionIdentifier(p_eCertDoc.getECertificatePromotionIdentifier());
		   eCertDoc.setECertificateRedemptionCode(p_eCertDoc.getECertificateRedemptionCode());
 	       eCertDoc.setGlobalCustomerIdentifier(p_eCertDoc.getGlobalCustomerIdentifier());
		   eCertDoc.setLastUpdateLocalTimestamp(p_eCertDoc.getLastUpdateLocalTimestamp());
		   eCertDoc.setWithholdECertificateDocumentIndicator(p_eCertDoc.getWithholdECertificateDocumentIndicator());
		   eCertDoc.setWorldPerksECertificateDocumentIdentifier(p_eCertDoc.getWorldPerksECertificateDocumentIdentifier());
		}// For ( ECertDocArray() )
	}

 	public boolean validateAmount(String value) {
		if (value == null || value.equals(""))
			//ecredit.setValue("0.00");
			return false;

		double dBaseFare = 0.00;
		dBaseFare = Double.parseDouble(value);
		if (dBaseFare > .01)
		  return true;
		else
          return false;

	}
	public boolean isEmdIDOK(String emdID)
	{
		for (int n=0; n < emdID.length(); n++)
		{
		  if (!Character.isDigit(emdID.charAt(n)))
		  {
			return false;
		  }
		}
		return true;
	}
	public String formatErrMessage(String sErrorMsg) {
		StringBuffer sbTmp = new StringBuffer();
		//TODO:  Add HostContext Stuff Here
		sbTmp.append("Host: ").append(getHostName()).append(" ");
		//if (cm != null)
		//	sbTmp.append("WSine: ").append(cm.getWsine()).append(" ");
		sbTmp.append("Error: ");
		sbTmp.append(sErrorMsg);
		return sbTmp.toString();
	}
	public String getHostName() {
		InetAddress iNetAddress = null;
		String strHostName = "Unknown host";
		try {
			iNetAddress = InetAddress.getLocalHost();
			strHostName = iNetAddress.getHostName();
		} catch (UnknownHostException uhe) {
			strHostName = "Unknown host";
		}
		return strHostName;
	}

    public void uncaughtExceptionHandler( Thread t, Throwable e )
    {
        //Instrumentation.m_logException( m_container, e, "getECreditsThread.uncaughtExceptionHandler() " + t.getId() );
        m_ecreditData.setException(e);
        m_ecreditData.setThreadId(t.getId());
        this.setECreditDataResponse(m_ecreditData);
    }

    public ECreditSearchDataContainer getECreditResponse() {
        return m_ecreditData;
    }

    private void setECreditDataResponse(ECreditSearchDataContainer p_ecreditData) {
        m_ecreditData = p_ecreditData;
    }

    private String getStackTrace(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
