package cn.tyrone.payment.channel.domain.route.strategy.context;


import cn.tyrone.payment.channel.common.entity.TransferAccountRequest;
import cn.tyrone.payment.channel.common.enums.ChannelConfigCode;
import cn.tyrone.payment.channel.common.enums.PaymentGatewayType;
import cn.tyrone.payment.channel.domain.route.strategy.TransferAccountRouteStrategy;
import cn.tyrone.payment.channel.domain.service.IPaymentRouteStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 转账支付策略上下文
 */
@Component
public class TransferAccountRouteStrategyContext {

    @Autowired
    private IPaymentRouteStrategyService paymentRouteStrategyService;

    @Autowired
    private Map<String, TransferAccountRouteStrategy> transferAccountRouteStrategyMap = new ConcurrentHashMap<>();

    //将构造参数中的map传递到成员变量中去
    public TransferAccountRouteStrategyContext(Map<String, TransferAccountRouteStrategy> transferAccountRouteStrategyMap) {
        this.transferAccountRouteStrategyMap.clear();
        transferAccountRouteStrategyMap.forEach(this.transferAccountRouteStrategyMap::put);
    }

    public TransferAccountRouteStrategy geTransferAccountRouteStrategy(TransferAccountRequest request){
        PaymentGatewayType paymentGatewayType = PaymentGatewayType.TRANSFER_ACCOUNT;
        ChannelConfigCode channelConfigCode = request.getChannelConfigCode();

        //根据支付网关类型和渠道配置编码获取支付路由策略
        String strategyName = paymentRouteStrategyService.paymentRouteStrategy(paymentGatewayType, channelConfigCode);

        TransferAccountRouteStrategy transferAccountRouteStrategy = this.transferAccountRouteStrategyMap.get(strategyName);

        return transferAccountRouteStrategy;
    }




}
