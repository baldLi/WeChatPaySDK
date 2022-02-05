package org.springboot.modules.wxPaySdkV3.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springboot.modules.returnUtil.R;
import org.springboot.modules.wxPaySdkV3.config.MyV3Config;
import org.springboot.modules.wxPaySdkV3.constant.WXPayV3Constants;
import org.springboot.modules.wxPaySdkV3.server.WXPayV3Server;
import org.springboot.modules.wxPaySdkV3.util.AesUtil;
import org.springboot.modules.wxPaySdkV3.util.WXPayV3Util;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;


/**
 * @author LiMY
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wxPayV3")
public class WxPayV3Controller {

	/**
	 * 本SDK修改于微信官方提供的java小程序支付SDK
	 * 商户号信息添加在 MyV3Config类中
	 * 读取p12证书路径在MyV3Config类中
	 * 接口方法在WXPayV3Server类中
	 * MyV3Config类中使用了Lombok 中的@Data方法，如果不用请自行添加get set 方法
	 * 官方文档地址 https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_1.shtml
	 */

	/**
	 * 统一下单测试接口
	 * @param myConfig
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/unifiedOrder")
	public JSONObject unifiedOrder(@RequestBody MyV3Config myConfig) throws Exception {
		WXPayV3Server wxPayV3Server = new WXPayV3Server(myConfig,"http://wei.vaiwan.com/wxPayV3/orderNotify");
		String uuid = WXPayV3Util.generateNonceStr();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode rootNode = objectMapper.createObjectNode();
		rootNode.put("description", myConfig.getDescription())
				.put("out_trade_no", uuid);
		rootNode.putObject("amount")
				.put("total", myConfig.getTotalFee());
		rootNode.putObject("payer")
				.put("openid", myConfig.getOpenId());
		System.out.println("商户订单号：" + uuid);
		JSONObject jsonObject = wxPayV3Server.unifiedOrder(wxPayV3Server.fillRequestData(rootNode));
		return R.data(wxPayV3Server.payMap(jsonObject));
	}

	/**
	 * 通过 商户订单号 查询订单
	 * @param myConfig
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/orderQuery")
	public JSONObject orderQuery(@RequestBody MyV3Config myConfig) throws Exception {
		WXPayV3Server wxPayV3Server = new WXPayV3Server(myConfig);
		return R.data(wxPayV3Server.orderQueryNO(myConfig));
	}

	/**
	 * 统一下单回调地址
	 * @param notifyData
	 * @return
	 */
	@PostMapping("/orderNotify")
	public String orderNotify(@RequestBody JSONObject notifyData) throws GeneralSecurityException {
		System.out.println("统一下单回调地址数据" + notifyData);
		AesUtil aesUtil = new AesUtil(new MyV3Config().getApiV3Key().getBytes());
		JSONObject data = aesUtil.decryptJsonToString(notifyData);
		System.out.println(data);
		return WXPayV3Constants.NOTIFY_SUCCESS;

	}

	/**
	 * 申请退款
	 * @param myConfig
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/refunds")
	public JSONObject refunds(@RequestBody MyV3Config myConfig) throws Exception {
		WXPayV3Server wxPayV3Server = new WXPayV3Server(myConfig,"http://wei.vaiwan.com/wxPayV3/refundNotify");
		String uuid = WXPayV3Util.generateNonceStr();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode rootNode = objectMapper.createObjectNode();
		rootNode.put("out_trade_no", myConfig.getOutTradeNo())
				.put("out_refund_no", uuid);
		rootNode.putObject("amount")
				.put("refund", myConfig.getRefundFee())
				.put("total", myConfig.getTotalFee())
				.put("currency", myConfig.getCurrency());
		System.out.println("商户退款订单号" + uuid);
		return R.data(wxPayV3Server.refunds(rootNode));
	}

	/**
	 * 查询退款订单
	 * @param myConfig
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/refundQuery")
	public JSONObject refundQuery(@RequestBody MyV3Config myConfig) throws Exception {
		WXPayV3Server wxPayV3Server = new WXPayV3Server(myConfig);
		return R.data(wxPayV3Server.refundQueryNO(myConfig));
	}

	/**
	 * 申请退款回调地址
	 * @param notifyData
	 * @return
	 */
	@PostMapping("/refundNotify")
	public String refundNotify(@RequestBody JSONObject notifyData) throws GeneralSecurityException {
		System.out.println("申请退款回调地址数据" + notifyData);
		AesUtil aesUtil = new AesUtil(new MyV3Config().getApiV3Key().getBytes());
		JSONObject data = aesUtil.decryptJsonToString(notifyData);
		System.out.println(data);
		return WXPayV3Constants.NOTIFY_SUCCESS;

	}

	/**
	 * 申请交易账单
	 * @param myV3Config
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@PostMapping("/tradeBill")
	public JSONObject refundNotify(@RequestBody MyV3Config myV3Config) throws IOException, URISyntaxException{
		WXPayV3Server wxPayV3Server = new WXPayV3Server(myV3Config);
		return wxPayV3Server.tradeBill(myV3Config);

	}

}
