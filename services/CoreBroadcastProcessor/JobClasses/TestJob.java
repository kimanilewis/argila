import java.util.HashMap;


public class TestJob{

    private String jsonConfigs;
    
    //private TestJob(){
      //log = new Logger;
    //}
    
    /**
     * Push payment to the client e.g Airtime,Normal
     * @param invoiceNumber
     * @param beepTransactionID
     * @param narration
     * @param amount
     * @param currencyCode
     * @param payerClientCode
     * @param payerTransactionID
     * @param MSISDN
     * @param overallStatus
     * @param statusDescription
     * @param statusCode 
     */
    public void processRequest(String invoiceNumber,
             String beepTransactionID,  String narration,
             String amount,  String currencyCode,
             String payerClientCode,  String payerTransactionID,
             String MSISDN,  int overallStatus,
             String statusDescription,  int statusCode, HashMap configs) {
        
        System.out.println("HERE ==> "+configs.toString());
        
    }
    
    /**
     * Send airtime for networks { 72,71,70 }
     * @param dateAndTime
     * @param networkExtraCode
     * @param bankMSISDN
     * @param pin
     * @param bankLoginID
     * @param bankPassword
     * @param bankExtraCode
     * @param bankTransactionID
     * @param customerMSISDN
     * @param amount
     * @param bankLanguage
     * @param customerLanguage
     * @param selector
     * @return 
     */
    public String sendAirtime(String dateAndTime, String networkExtraCode,
            String bankMSISDN, String pin, String bankLoginID,
            String bankPassword, String bankExtraCode,
            String bankTransactionID, String customerMSISDN, String amount,
            String bankLanguage, String customerLanguage, String selector) {

        String response = "";
        int httpStatusCode = 0;
        
        String request = "VENDOR="
                + bankExtraCode
                + "&REQTYPE=EXRCTRFREQ&DATA=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<ns0:COMMAND xmlns:ns0=\"http://safaricom.co.ke/Pinless/keyaccounts/\">"
                + "<ns0:TYPE>EXRCTRFREQ</ns0:TYPE>" + "<ns0:DATE>"
                + dateAndTime + "</ns0:DATE>"
                + "<ns0:EXTNWCODE>SA</ns0:EXTNWCODE>"
                + "<ns0:MSISDN></ns0:MSISDN>" + "<ns0:PIN></ns0:PIN>"
                + "<ns0:LOGINID></ns0:LOGINID>"
                + "<ns0:PASSWORD></ns0:PASSWORD>" + "<ns0:EXTCODE>"
                + bankExtraCode + "</ns0:EXTCODE>" + "<ns0:EXTREFNUM>"
                + bankTransactionID + "</ns0:EXTREFNUM>" + "<ns0:MSISDN2>"
                + customerMSISDN + "</ns0:MSISDN2>" + "<ns0:AMOUNT>" + amount
                + "</ns0:AMOUNT>" + "<ns0:LANGUAGE1>" + bankLanguage
                + "</ns0:LANGUAGE1>" + "<ns0:LANGUAGE2>" + customerLanguage
                + "</ns0:LANGUAGE2>" + "<ns0:SELECTOR>" + selector
                + "</ns0:SELECTOR>" + "</ns0:COMMAND>";

        //infoLog.info("VirtualTopUpV3BrandTone | rawXmlPost --- XML POST --- XML Body: "
         //       + request);
        //nfoLog.info("VirtualTopUpV3BrandTone | rawXmlPost --- READ TIME OUT - " + READ_TIME_OUT + ":: Conncet TIME OUT - " + CONNECT_TIME_OUT);
        HttpClient httpclient = new HttpClient();
        PostMethod post = new PostMethod(RPC_SERVER_URL);
        post.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
        post.setRequestHeader("User-Agent", "The Incutio XML-RPC PHP Library");
       
        httpclient.getParams().setSoTimeout(READ_TIME_OUT);
        httpclient.getParams().setConnectionManagerTimeout(CONNECT_TIME_OUT);

        int len = request.length();
        if (len < Integer.MAX_VALUE) {
            post.setRequestContentLength((int) len);
        } else {
            post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
        }
        post.setRequestBody(request);
        try {
            httpStatusCode = httpclient.executeMethod(post);
            response = post.getResponseBodyAsString();
            post.releaseConnection();

            if (httpStatusCode == 400) {
                response = "205";

            }
            //infoLog.info("VirtualTopUpV3BrandTone | rawXmlPost --- HTTP STATUS CODE: "
            //       + httpStatusCode);

            return response;
        } catch (HttpException e) {
           //errorLog.fatal(
           //        "VirtualTopUpV3BrandTone | rawXmlPost --- HttpException thrown while sending request to server. DO NOT Re-schedule the request. ",
           //        e);
            return (response = "203");
        } catch (ConnectException ce) {
            //errorLog.fatal(
             //       "VirtualTopUpV3BrandTone | rawXmlPost --- Connection timeout exception thrown while sending request to server. Re-schedule the request for processing. ",
             //       ce);
            return (response = "200");
        } catch (SocketTimeoutException se) {
            //errorLog.fatal(
            //        "VirtualTopUpV3BrandTone | rawXmlPost --- Read timed out exception thrown while sending request to server. DO NOT Re-schedule the request. ",
             //       se);
            return (response = "201");
        } catch (UnknownHostException ue) {
            //errorLog.fatal(
            //        "VirtualTopUpV3BrandTone | rawXmlPost --- Unknown host exception thrown while sending request to server. Re-schedule the request for processing. ",
             //       ue);
            return (response = "202");
        } catch (IOException e) {
            //errorLog.fatal(
            //        "VirtualTopUpV3BrandTone | rawXmlPost --- IOException thrown while sending request to server. DO NOT Re-schedule the request. ",
            //       e);
            return (response = "204");
        }
    }
    
    protected void aknowledgeAirtimePayment() throws Exception{
    }
    
    public HashMap ParseXMLString(String xmlRecords) {
        HashMap response = new HashMap();
        //infoLog.info("VirtualTopUpV3BrandTone | ParseXMLString --- Data to be parsed: "
        //            + xmlRecords);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlRecords));

            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("ns0:COMMAND");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                NodeList TXNSTATUS = element.getElementsByTagName("ns0:TXNSTATUS");
                Element line = (Element) TXNSTATUS.item(0);
                response.put("status", getCharacterDataFromElement(line));
                NodeList EXTREFNUM = element.getElementsByTagName("ns0:EXTREFNUM");
                line = (Element) EXTREFNUM.item(0);
                response.put("requestID", getCharacterDataFromElement(line));
                NodeList TXNID = element.getElementsByTagName("ns0:TXNID");
                line = (Element) TXNID.item(0);
                response.put("preTUPSID", getCharacterDataFromElement(line));
                NodeList MESSAGE = element.getElementsByTagName("ns0:MESSAGE");
                line = (Element) MESSAGE.item(0);
                response.put("statusMessage", getCharacterDataFromElement(line));
            }
            return response;
        } catch (Exception e) {
            //errorLog.error(
            //        VirtualTopUpV3BrandTone.class.getName()
            //       + " | ParseXMLString --- Exception thrown while processing topup server response. Was parsing: " + xmlRecords,
            //        e);
            return response;
        }
    }
    
    protected String getLogPreString(){
        return "|";
    }
}
