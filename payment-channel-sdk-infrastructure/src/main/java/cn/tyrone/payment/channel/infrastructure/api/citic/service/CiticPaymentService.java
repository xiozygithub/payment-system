package cn.tyrone.payment.channel.infrastructure.api.citic.service;


import cn.tyrone.payment.channel.common.enums.PaymentChannelConfig;
import cn.tyrone.payment.channel.infrastructure.api.citic.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * 参数String requestMessage大致内容：
 * xml格式
 * <Request>
 *     <Head>
 *         <Version>1.0</Version>
 *         <TransCode>DLBREGSN</TransCode>
 *         <MerId>YOUR_MERCHANT_ID</MerId>
 *         <Date>TRANSACTION_DATE</Date>
 *         <Time>TRANSACTION_TIME</Time>
 *         <RequestId>SOME_UNIQUE_REQUEST_ID</RequestId>
 *         <Signature>SIGNATURE_DATA</Signature>
 *     </Head>
 *     <Body>
 *         <MemberInfo>
 *             <CustName>MEMBER_NAME</CustName>
 *             <CustType>CUSTOMER_TYPE</CustType>
 *             <Cust证件类型>ID_TYPE</Cust证件类型>
 *             <Cust证件号码>ID_NUMBER</Cust证件号码>
 *             <CustMobile>MOBILE_PHONE</CustMobile>
 *             <CustEmail>EMAIL_ADDRESS</CustEmail>
 *             <!-- 其他必要的会员信息 -->
 *         </MemberInfo>
 *     </Body>
 * </Request>
 *
 * sjon格式：
 * {
 *   "head": {
 *     "version": "1.0",
 *     "transCode": "DLBREGSN",
 *     "merId": "YOUR_MERCHANT_ID",
 *     "date": "TRANSACTION_DATE",
 *     "time": "TRANSACTION_TIME",
 *     "requestId": "SOME_UNIQUE_REQUEST_ID",
 *     "signature": "SIGNATURE_DATA"
 *   },
 *   "body": {
 *     "memberInfo": {
 *       "custName": "MEMBER_NAME",
 *       "custType": "CUSTOMER_TYPE",
 *       "custIdType": "ID_TYPE",
 *       "custIdNumber": "ID_NUMBER",
 *       "custMobile": "MOBILE_PHONE",
 *       "custEmail": "EMAIL_ADDRESS"
 *       // 其他必要的会员信息字段
 *     }
 *   }
 * }
 *
 * 实际上就是xml格式，由DlbregsnRequest类生产
 *
 * 中信银行发送报文过程
 *
 * @param
 * @param
 * @return
 * @throws IOException
 * @throws DocumentException
 */
@Slf4j
@Service
public class CiticPaymentService {

    /**
     * execute方法接收请求消息（XML格式的报文字符串）和支付通道配置，根据配置的前置应用地址建立网络连接，
     * 并将请求报文通过这个连接发送到银行服务器。它处理了网络连接的建立、超时设置、以及请求报文的发送过程。
     * @param requestMessage
     * @param paymentChannelConfig
     * @return
     * @throws Exception
     */
    private String execute(String requestMessage, PaymentChannelConfig paymentChannelConfig) throws Exception {


        Map<String, Object> channelConfig = paymentChannelConfig.getChannelConfig();

        // 前置应用地址//从支付通道配置中提取前置应用的URL地址，这是请求的目标地址。
        String preApplicaionUrl = String.valueOf(channelConfig.get("pre_applicaion_url"));

        //使用提取的URL创建URL对象，进而建立到银行服务器的网络连接。
        URL sendUrl = new URL(preApplicaionUrl.trim());
        URLConnection connection = sendUrl.openConnection();


        //设置连接参数:
        connection.setConnectTimeout(30000); // 连接超时时间设置
        connection.setReadTimeout(30000); // 读取超时时间设置
        connection.setDoOutput(true); // 设置为输出模式，允许写入请求体


        //发送请求报文:
        //通过输出流，使用指定的字符集（GBK）将请求报文写入连接，并确保数据已发送。
        //connection.getOutputStream() 得到一个字节输出流，它代表了到服务器的输出通道。
        //new OutputStreamWriter 是将字节输出流转换为了字符输出流，以便于以指定的字符编码写入字符串。
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "GBK");
        out.write(requestMessage);
        out.flush();
        out.close();

        //接收响应并解析
        // 一旦发送成功，用以下方法就可以得到服务器的回应：
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(inputStreamReader);
        String responseMessage = document.asXML();

        return responseMessage;
    }

    private String execute(AbstractCiticBaseRequest request) throws Exception {

        PaymentChannelConfig paymentChannelConfig = request.getPaymentChannelConfig();
        Map<String, Object> channelConfig = paymentChannelConfig.getChannelConfig();

        String requestMessage = request.processing();

        // 前置应用地址
        String preApplicaionUrl = String.valueOf(channelConfig.get("pre_applicaion_url"));

        URL sendUrl = new URL(preApplicaionUrl.trim());
        URLConnection connection = sendUrl.openConnection();

        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setDoOutput(true);

        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "GBK");
        out.write(requestMessage);
        out.flush();
        out.close();

        // 一旦发送成功，用以下方法就可以得到服务器的回应：
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(inputStreamReader);
        String responseMessage = document.asXML();

        return responseMessage;
    }

    /**
     * 3.2.5 会员注册
     *
     * @param request
     * @return
     */
    public DlbregsnResponse dlbregsn(DlbregsnRequest request) {

        DlbregsnResponse response = null;

        PaymentChannelConfig paymentChannelConfig = request.getPaymentChannelConfig();
        String requestMessage = request.processing();
        try {

            String responseMessage = this.execute(requestMessage, paymentChannelConfig);

            response = new DlbregsnResponse(responseMessage);

        } catch (Exception e) {
            log.error("中信银行会员注册系统异常:", e);
        }

        return response;
    }


    public DlsbalqrResponse dlsbalqr(DlsbalqrRequest request) {

        DlsbalqrResponse response = null;

        PaymentChannelConfig paymentChannelConfig = request.getPaymentChannelConfig();
        String requestMessage = request.processing();
        try {

            String responseMessage = this.execute(requestMessage, paymentChannelConfig);

            response = new DlsbalqrResponse(responseMessage);

        } catch (Exception e) {
            log.error("中信银行商户资金分簿余额查询:", e);
        }

        return response;

    }

    public DlmdetrnResponse dlmdetrn(DlmdetrnRequest request) {
        DlmdetrnResponse response = null;

        PaymentChannelConfig paymentChannelConfig = request.getPaymentChannelConfig();
        String requestMessage = request.processing();
        try {

            String responseMessage = this.execute(requestMessage, paymentChannelConfig);

            response = new DlmdetrnResponse(responseMessage);

        } catch (Exception e) {
            log.error("中信银行商户资金分簿余额查询系统异常:", e);
        }

        return response;
    }

    /**
     * 3.43 平台出金
     * 商户可使用此接口完成会员交易资金资金分簿出金功能。
     * @param request
     * @return
     */
    public DlintfcsResponse dlintfcs(DlintfcsRequest request) {

        DlintfcsResponse response = null;

        PaymentChannelConfig paymentChannelConfig = request.getPaymentChannelConfig();
        String requestMessage = request.processing();
        try {

            String responseMessage = this.execute(requestMessage, paymentChannelConfig);

            response = new DlintfcsResponse(responseMessage);

        } catch (Exception e) {
            log.error("中信银行平台出金系统异常:", e);
        }

        return response;
    }

    /**
     * 现金管理系统余额查询
     * @param request
     * @return
     */
    public DlbalqryResponse dlbalqry(DlbalqryRequest request){
        DlbalqryResponse response = null;

        try {

            String responseMessage = this.execute(request);

            response = new DlbalqryResponse(responseMessage);

        } catch (Exception e) {
            log.error("现金管理系统余额查询系统异常:", e);
        }

        return response;
    }

    /**
     * 现金管理系统账户明细信息查询
     * @param request
     * @return
     */
    public DltrnallResponse dltrnall(DltrnallRequest request){
        DltrnallResponse response = null;

        try {

            String responseMessage = this.execute(request);

            response = new DltrnallResponse(responseMessage);

        } catch (Exception e) {
            log.error("现金管理系统账户明细信息查询系统异常:", e);
        }

        return response;
    }

    /**
     * 现金管理系统支付转账
     * @param request
     * @return
     */
    public DlinttrnResponse dlinttrn(DlinttrnRequest request){
        DlinttrnResponse response = null;

        try {

            String responseMessage = this.execute(request);

            response = new DlinttrnResponse(responseMessage);

        } catch (Exception e) {
            log.error("现金管理系统支付转账系统异常:", e);
        }

        return response;
    }

    /**
     * 现金管理系统交易状态查询
     * @param request
     * @return
     */
    public DlcidsttResponse dlcidstt(DlcidsttRequest request){
        DlcidsttResponse response = null;

        try {

            String responseMessage = this.execute(request);

            response = new DlcidsttResponse(responseMessage);

        } catch (Exception e) {
            log.error("现金管理系统交易状态查询系统异常:", e);
        }

        return response;
    }

    /**
     * 现金管理系统回单信息查询
     * @param request
     * @return
     */
    public DleddrsqResponse dleddrsq(DleddrsqRequest request){
        DleddrsqResponse response = null;

        try {

            String responseMessage = this.execute(request);

            response = new DleddrsqResponse(responseMessage);

        } catch (Exception e) {
            log.error("现金管理系统交易状态查询系统异常:", e);
        }

        return response;
    }

    /**
     * 现金管理系统回单下载
     * @param request
     * @return
     */
    public DledcdtdResponse dledcdtd(DledcdtdRequest request){
        DledcdtdResponse response = null;

        try {

            String responseMessage = this.execute(request);

            response = new DledcdtdResponse(responseMessage);

        } catch (Exception e) {
            log.error("现金管理系统交易状态查询系统异常:", e);
        }

        return response;
    }

}
