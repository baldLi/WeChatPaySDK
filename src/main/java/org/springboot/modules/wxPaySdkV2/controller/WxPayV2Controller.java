package org.springboot.modules.wxPaySdkV2.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import org.springboot.modules.returnUtil.R;
import org.springboot.modules.wxPaySdkV2.config.MyV2Config;
import org.springboot.modules.wxPaySdkV2.server.WXPayV2Server;
import org.springboot.modules.wxPaySdkV2.util.WXPayV2Util;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LiMY
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wxPay")
public class WxPayV2Controller{

	/**
	 * 本SDK修改于微信官方提供的java小程序支付SDK
	 * 商户号信息添加在 MyV2Config类中
	 * 读取p12证书路径在MyV2Config类中
	 * 接口方法在WXPayV2Server类中
	 * MyV2Config类中使用了Lombok 中的@Data方法，如果不用请自行添加get set 方法
	 * 官方文档地址 https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_3&index=1
	 */

	/**
	 * 统一下单测试接口
	 * @param myConfig
	 * @param httpServletRequest
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/payTest")
	public JSONObject payTest(@RequestBody MyV2Config myConfig, HttpServletRequest httpServletRequest) throws Exception {
		WXPayV2Server wxPayV2Server = new WXPayV2Server(myConfig, " http://wei.vaiwan.com/wxPay/notify", false, false);
		System.out.println(myConfig.getTotalFee());
		HashMap<String, String> data = new HashMap<>();
		data.put("total_fee", myConfig.getTotalFee());
		data.put("body", myConfig.getBody());
		data.put("openid", myConfig.getOpenId());
		data.put("out_trade_no", WXPayV2Util.generateUUID());
		data.put("spbill_create_ip", WXPayV2Util.getIpAddr(httpServletRequest));
		data.put("trade_type", "JSAPI");
		System.out.println("支付订单号" + data.get("out_trade_no"));

		Map<String, String> resp = wxPayV2Server.unifiedOrder(wxPayV2Server.fillRequestData(data), 1500, 1500);
		if ("SUCCESS".equals(resp.get("return_code"))){
			Map<String, String> payMap = wxPayV2Server.payMap(resp);
			return R.data(payMap);
		}else {
			return R.fail("统一下单失败！");
		}
	}

	/**
	 * 退款测试接口
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/payRefundTest")
	public JSONObject payRefundTest(@RequestBody MyV2Config myConfig) throws Exception {
		WXPayV2Server payRefundPay = new WXPayV2Server(myConfig, "http://weixin.vaiwan.com/wechat/pay/notify");
		HashMap<String, String> data = new HashMap<>();
		data.put("total_fee", myConfig.getTotalFee());
		data.put("refund_fee", myConfig.getRefundFee());
		data.put("out_trade_no", myConfig.getOutTradeNo());
		data.put("out_refund_no", WXPayV2Util.generateUUID());
		Map<String, String> resp = payRefundPay.refund(data, 1500, 1500);
		if ("SUCCESS".equals(resp.get("return_code"))){
			return R.data("退款成功！");
		}else {
			return R.fail("退款失败！");
		}
	}

	/**
	 * 微信回调接口
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/notify")
	public String wxNotify(@RequestBody String data) throws Exception {
		Map<String, String> notifyData = WXPayV2Util.xmlToMap(data);
		String status = "<xml><return_code><![CDATA[%s]]></return_code></xml>";
		if ("SUCCESS".equals(notifyData.get("result_code"))){
			return String.format(status, "SUCCESS");
		}else {
			System.out.println(String.format(status, "FAIL"));
			return String.format(status, "FAIL");
		}
	}

	/**
	 * 查询订单
	 * @param myConfig
	 * @return
	 */
	@PostMapping("/orderQuery")
	public JSONObject orderQuery(@RequestBody MyV2Config myConfig) throws Exception {
		WXPayV2Server wxPayV2Server = new WXPayV2Server(myConfig, "http://weixin.vaiwan.com/wechat/pay/notify");
		HashMap<String, String> data = new HashMap<>();
		data.put("out_trade_no", myConfig.getOutTradeNo());
		return R.data(wxPayV2Server.orderQuery(wxPayV2Server.fillRequestData(data)));
	}

	/**
	 * 查询退款订单
	 * @param myConfig
	 * @return
	 */
	@PostMapping("/refundQuery")
	public JSONObject refundQuery(@RequestBody MyV2Config myConfig) throws Exception {
		WXPayV2Server wxPayV2Server = new WXPayV2Server(myConfig, "http://weixin.vaiwan.com/wechat/pay/notify");
		HashMap<String, String> data = new HashMap<>();
		data.put("out_trade_no", myConfig.getOutTradeNo());
		return R.data(wxPayV2Server.refundQuery(wxPayV2Server.fillRequestData(data)));
	}

	/**
	 * 查询退款订单
	 * @param myConfig
	 * @return
	 */
	@PostMapping("/downloadBill")
	public JSONObject downloadBill(@RequestBody MyV2Config myConfig) throws Exception {
		WXPayV2Server wxPayV2Server = new WXPayV2Server(myConfig, "http://weixin.vaiwan.com/wechat/pay/notify", false, false);
		HashMap<String, String> data = new HashMap<>();
		data.put("bill_date", myConfig.getBillDate());
		data.put("bill_type", "ALL");
		return R.data(wxPayV2Server.downloadBill(wxPayV2Server.fillRequestData(data)));
	}

}
