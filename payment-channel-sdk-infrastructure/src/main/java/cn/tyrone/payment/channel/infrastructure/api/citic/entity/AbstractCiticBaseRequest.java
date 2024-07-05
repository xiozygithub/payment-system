package cn.tyrone.payment.channel.infrastructure.api.citic.entity;

import cn.tyrone.payment.channel.infrastructure.api.common.entity.AbstractBaseRequest;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 中信银行请求基类
 */
@Data
@SuperBuilder
public abstract class AbstractCiticBaseRequest extends AbstractBaseRequest {

    /**
     * 登录名
     */
    protected String userName;

    /**
     * 账户
     */
    protected String accountNo;

    /**
     * 主体账号
     */
    protected String mainAccNo;

    /**
     * 对应请求代码
     */
    protected CiticAction citicAction;

    protected String actionProcessing() throws RuntimeException {
        String action = "<action>" + this.citicAction.getAction() + "</action>";
        //如果citicAction.getAction()返回值为"queryBalance"，则执行此方法将得到<action>queryBalance</action>这样的字符串。
        return action;
    }

    @Override
    protected String statementProcessing() throws RuntimeException {

        String statement = "<?xml version=\"1.0\" encoding=\"GBK\"?>";
        //<?xml version="1.0" encoding="GBK"?>

        return statement;
    }
}
