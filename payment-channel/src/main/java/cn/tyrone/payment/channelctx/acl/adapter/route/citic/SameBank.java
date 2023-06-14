package cn.tyrone.payment.channelctx.acl.adapter.route.citic;

/**
 * 是否本行
 */
public enum SameBank {

    ZERO("0", "本行"),
    ONE("1", "他行"),
    ;

    /**
     * 操作
     */
    private String sameBank;

    /**
     * 说明
     */
    private String describe;

    private SameBank(String sameBank, String describe){
        this.sameBank = sameBank;
        this.describe = describe;
    }

    public String getSameBank() {
        return sameBank;
    }

    public void setSameBank(String sameBank) {
        this.sameBank = sameBank;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
