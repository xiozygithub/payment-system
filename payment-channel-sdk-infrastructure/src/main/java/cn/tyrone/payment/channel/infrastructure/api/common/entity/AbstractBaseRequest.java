package cn.tyrone.payment.channel.infrastructure.api.common.entity;

import cn.tyrone.payment.channel.common.enums.PaymentChannelConfig;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Data
@SuperBuilder
public abstract class AbstractBaseRequest {

    /**
     * 请求明文
     */
    protected String requestPlaintext;

    /**
     * 请求密文
     */
    protected String requestCiphertext;

    /**
     * 请求签名
     */
    protected String requestSign;

    /**
     * 支付渠道配置
     */
    protected PaymentChannelConfig paymentChannelConfig;

    /**
     * 报文声明处理过程
     * @return
     * @throws RuntimeException
     */
    protected abstract String statementProcessing() throws RuntimeException;

    /**
     * 报文体处理过程
     * @return
     * @throws RuntimeException
     */
    public abstract String processing() throws RuntimeException;

    /**
     * 节点处理过程
     * @param elementName 节点名称
     * @param elementText 节点的值
     * @param ifMust 是否必填
     * @return
     */
    protected String elementProcessing(String elementName, String elementText, boolean ifMust) {

        String element = "";

        Optional<String> elementTextOptional = Optional.ofNullable(elementText);
        if (ifMust) {

            //isPresent(),存在非空值是返回true
            if (!elementTextOptional.isPresent() || "".equals(elementText)) {
                throw new RuntimeException("节点" + elementName + "不允许为空");
            }
            element = "<" + elementName + ">" + elementText + "</" + elementName + ">";
            //// 输出可能是：<Amount>100.00</Amount>
        }

        if (!ifMust) {

            if (!elementTextOptional.isPresent()) {
                elementText = "";
            }
            element = "<" + elementName + ">" + elementText + "</" + elementName + ">";
            // 输出可能是：<Remark></Remark>
        }

        return element;
    }


}
