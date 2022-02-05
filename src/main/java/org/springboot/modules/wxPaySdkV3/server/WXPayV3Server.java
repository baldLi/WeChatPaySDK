package org.springboot.modules.wxPaySdkV3.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springboot.modules.wxPaySdkV3.auth.WechatPayHttpClientBuilder;
import org.springboot.modules.wxPaySdkV3.config.MyV3Config;
import org.springboot.modules.wxPaySdkV3.config.WXPayV3Config;
import org.springboot.modules.wxPaySdkV3.constant.WXPayV3Constants;
import org.springboot.modules.wxPaySdkV3.util.RsaCryptoUtil;
import org.springboot.modules.wxPaySdkV3.util.WXPayV3Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 86151
 */
public class WXPayV3Server {
	private final String notifyUrl;
	private final WXPayV3Config config;

	public WXPayV3Server(final WXPayV3Config config, final String notifyUrl) {
		this.notifyUrl = notifyUrl;
		this.config = config;
	}

	public WXPayV3Server(final WXPayV3Config config) {
		this.notifyUrl = config.getNotifyUrl();
		this.config = config;
	}

	/**
	 * 向 ObjectNode 中添加 appid、mch_id、notify_url
	 *
	 * @param rootNode
	 * @return
	 */
	public ObjectNode fillRequestData(ObjectNode rootNode){
		rootNode.put("mchid",config.getMchID())
			.put("appid", config.getAppID())
			.put("notify_url", notifyUrl);
		return rootNode;
	}

	/**
	 * 统一下单接口
	 * @param rootNode
	 * @return
	 * @throws IOException
	 */
	public JSONObject unifiedOrder(ObjectNode rootNode) throws IOException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		HttpPost httpPost = new HttpPost(WXPayV3Constants.BASIC_URL + WXPayV3Constants.UNIFIED_ORDER);
		httpPost.addHeader("Accept", "application/json");
		httpPost.addHeader("Content-type","application/json; charset=utf-8");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(bos, rootNode);

		httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
		CloseableHttpResponse response = httpClient.execute(httpPost);

		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 统一下单二次签名数据处理(个人编写，可自行修改)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> payMap(JSONObject jsonObject) throws Exception {
		HashMap<String, String> payMap = new HashMap<>();
		payMap.put("appId", config.getAppID());
		payMap.put("timeStamp", WXPayV3Util.getCurrentTimestamp() + "");
		payMap.put("nonceStr", WXPayV3Util.generateNonceStr());
		payMap.put("signType", WXPayV3Constants.SIGN_TYPE);
		payMap.put("package", "prepay_id=" + jsonObject.get("prepay_id"));
		String paySign = RsaCryptoUtil.structurePaySign(payMap, config.getMerchantPrivateKey());
		payMap.put("paySign", paySign);
		return payMap;
	}

	/**
	 * 通过 商户订单号 查询订单
	 * @param myV3Config
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public JSONObject orderQueryNO(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.QUERY_ORDER_OUT_TRADE_NO + myV3Config.getOutTradeNo());
		newBuilder.setParameter("mchid", config.getMchID());

		HttpGet httpGet = new HttpGet(newBuilder.build());
		httpGet.addHeader("Accept", "application/json");
		httpGet.addHeader("Content-type","application/json; charset=utf-8");

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 通过 微信支付订单号 查询订单
	 * @param myV3Config
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public JSONObject orderQueryId(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.QUERY_ORDER_TRANSACTION_ID + myV3Config.getTransactionId());
		newBuilder.setParameter("mchid", config.getMchID());

		HttpGet httpGet = new HttpGet(newBuilder.build());
		httpGet.addHeader("Accept", "application/json");
		httpGet.addHeader("Content-type","application/json; charset=utf-8");

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 通过 商户订单号 申请退款
	 * @param rootNode
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public JSONObject refunds(ObjectNode rootNode) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		HttpPost httpPost = new HttpPost(WXPayV3Constants.BASIC_URL + WXPayV3Constants.REFUND);
		httpPost.addHeader("Accept", "application/json");
		httpPost.addHeader("Content-type","application/json; charset=utf-8");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		rootNode.put("notify_url", notifyUrl);
		objectMapper.writeValue(bos, rootNode);

		httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
		CloseableHttpResponse response = httpClient.execute(httpPost);

		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 通过 商户订单号 查询退款订单
	 * @param myV3Config
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public JSONObject refundQueryNO(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.QUERY_REFUND + myV3Config.getOutTradeNo());

		HttpGet httpGet = new HttpGet(newBuilder.build());
		httpGet.addHeader("Accept", "application/json");
		httpGet.addHeader("Content-type","application/json; charset=utf-8");

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 申请交易账单
	 * @param myV3Config
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public JSONObject tradeBill(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.TRADE_BILL);
		newBuilder.setParameter("bill_date", myV3Config.getBillDate());
		newBuilder = myV3Config.getBillType() != null ? newBuilder.setParameter("bill_type", myV3Config.getBillType()) : newBuilder;
		System.out.println("URL" + newBuilder.build());

		HttpGet httpGet = new HttpGet(newBuilder.build());
		httpGet.addHeader("Accept", "application/json");
		httpGet.addHeader("Content-type","application/json; charset=utf-8");

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 下载交易账单（官方文档不全，接口不好使）
	 * @param jsonObject
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public JSONObject billDownload(JSONObject jsonObject) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		HttpGet httpGet = new HttpGet(jsonObject.getString("download_url"));
		httpGet.addHeader("Authorization", "WECHATPAY2-SHA256-RSA2048mchid=\""+config.getMchID()+"\"");
		httpGet.addHeader("nonce_str",WXPayV3Util.generateNonceStr());
		httpGet.addHeader("signature", "12");
		httpGet.addHeader("timestamp", "\"" + WXPayV3Util.getCurrentTimestamp() + "\"");
		httpGet.addHeader("serial_no", config.getMerchantSerialNumber());

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}


}
