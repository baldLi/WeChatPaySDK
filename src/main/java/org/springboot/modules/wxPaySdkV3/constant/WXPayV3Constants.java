package org.springboot.modules.wxPaySdkV3.constant;


/**
 * 常量
 * @author 86151
 */
public class WXPayV3Constants {

	public static final int UPDATE_INTERVAL_MINUTE = 60;

	public static final String SIGN_TYPE = "RSA";

	public static final String NOTIFY_SUCCESS = "{\"code\": \"SUCCESS\",\"message\": \"成功\"}";


	public static final String BASIC_URL = "https://api.mch.weixin.qq.com/v3";

	public static final String UNIFIED_ORDER = "/pay/transactions/jsapi";

	public static final String QUERY_ORDER_OUT_TRADE_NO = "/pay/transactions/out-trade-no/";

	public static final String QUERY_ORDER_TRANSACTION_ID = "/pay/transactions/id/";

	public static final String REFUND = "/refund/domestic/refunds";

	public static final String QUERY_REFUND = "/refund/domestic/refunds/";

	public static final String TRADE_BILL = "/bill/tradebill";

}

