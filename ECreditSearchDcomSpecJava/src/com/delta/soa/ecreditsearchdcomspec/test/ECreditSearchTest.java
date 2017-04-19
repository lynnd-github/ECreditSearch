package com.delta.soa.ecreditsearchdcomspec.test;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.delta.schemas.ecredit.ecreditsearchrequest.ECreditSearchRequestDocument;
import com.delta.schemas.ecredit.ecreditsearchresponse.ECreditSearchResponseDocument;
import com.delta.soa.ecreditsearchdcomspec.v1.BusinessFaultException;
import com.delta.soa.ecreditsearchdcomspec.ECreditSearchDcomSpecImpl;


/**
 *
 * This is a test class used to test MiscellaneousDocsV2 service opearations
 * using standalone javacode.
 * o
 */
public class ECreditSearchTest {

	private static String testId = "CM";

	private static String testLab = "TPB";

	public static void main(String[] args) {
		ECreditSearchDcomSpecImpl impl = new ECreditSearchDcomSpecImpl();
		try {
			System.setProperty("AMX_LOG_DIR", "C:/amx_logs/ecreditsearch");
			System.setProperty("AMX_NODE_NAME", "");
			System.setProperty("AMX_LOGFILE_EXTN", "txt");

			impl.setInitialLogLevel("DEBUG");
			impl.setTpfRegion(testLab);
			impl.setShowTimings("false");
			impl.setEcreditTimeOut("50");
			impl.setUnHookETickets("true");
			impl.setPrimaryCarrierCode("DL");
			impl.setPrimaryCarrierFullName("Delta Air Lines, Inc");
			impl.setPrimaryCarrierICAOCode("DAL");
			impl.setPrimaryCarrierName("Delta");
            //impl.setAddYToDocNumber("Y");
			impl.start();
			impl.setRunAsMain(true);
			impl.m_log.setLevel(Level.DEBUG);

			testECredits(impl);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		//	impl.stop();
		}
	}
	private static void testECredits(ECreditSearchDcomSpecImpl impl)
	{
		ECreditSearchRequestDocument ecSrchRQ = ECreditSearchRequestDocument.Factory.newInstance();
		File xmlFile = new File("c:\\LocalFiles\\ecreditsrchrqs\\ecreditsrchrq.txt");

		try {
			ecSrchRQ = ECreditSearchRequestDocument.Factory.parse(xmlFile);
			System.out.println("### EcreditSearchRQ = "+ecSrchRQ);

			long startTime = System.currentTimeMillis();
			try {
				ECreditSearchResponseDocument ecreditRS = impl.retrieveECredits(ecSrchRQ);
				if (ecreditRS != null)
					System.out.println("### EcreditSearchRS = "+ecreditRS);
			}
			catch (BusinessFaultException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("EndTime = "+ String.valueOf(endTime));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
