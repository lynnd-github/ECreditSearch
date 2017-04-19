package com.delta.soa.ecreditsearchdcomspec.tpf;
import org.apache.log4j.Logger;

import com.delta.schemas.common.requestinfo.v4.RequestInfoType;
import com.delta.schemas.common.ticketingrequestparams.v2.TicketingRequestParamsType;
import com.delta.schemas.ecredit.ecredit.ECreditType;
import com.delta.schemas.ticket.tickettypes.v3.UnHookRequestDocument;
import com.delta.schemas.ticket.tickettypes.v3.UnHookRequestType;

public class UnhookRequestGenerator
{
	private String requestString;
	Logger m_log = null;
	public UnhookRequestGenerator(Logger parentLogger) 
	{
		m_log = parentLogger;
	}
	
	public String getBackendRequestString()
	{
		return requestString;
	}
	
	protected UnHookRequestDocument createUnhookRequest(ECreditType ecredit, RequestInfoType reqInfo) throws Exception
	{
		UnHookRequestDocument  requestDoc = UnHookRequestDocument.Factory.newInstance();
		UnHookRequestType request = requestDoc.addNewUnHookRequest();
		request.setRequestInfo(reqInfo);
		TicketingRequestParamsType tktRQ = request.addNewTicketingRequestParamsV2();
		tktRQ.setLastNameNumber("01");
		tktRQ.setFirstNameNumber("01");
		return requestDoc;
	}
}
