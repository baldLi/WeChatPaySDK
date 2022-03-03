package org.springboot.modules.wxPaySdkV3.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
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
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 86151
 */
@Service
public class WXPayV3Server implements WXPayServer{

	private final WXPayV3Config config;

	public WXPayV3Server(WXPayV3Config config) {
		this.config = config;
	}

	/**
	 * 向 ObjectNode 中添加 appid、mch_id、notify_url
	 *
	 * @param rootNode
	 * @return
	 */
	@Override
	public ObjectNode fillRequestData(ObjectNode rootNode){
		rootNode.put("mchid",config.getMchID())
			.put("appid", config.getAppID());
		return rootNode;
	}

	/**
	 * 微信鉴权
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Override
	public JSONObject getAccessToken() throws IOException, URISyntaxException {
		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.ACCESS_TOKEN);
		newBuilder.setParameter("appid", config.getAppID());
		newBuilder.setParameter("secret", config.getSecret());
		newBuilder.setParameter("grant_type", "client_credential");

		HttpGet httpGet = new HttpGet(newBuilder.build());
		httpGet.addHeader("Accept", "application/json");
		httpGet.addHeader("Content-type","application/json; charset=utf-8");

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	/**
	 * 获取手机号（新版）
	 * @param jsonObject
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Override
	public JSONObject getPhoneNumber(JSONObject jsonObject) throws IOException, URISyntaxException {
		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		HttpPost httpPost = new HttpPost(WXPayV3Constants.PHONE_NUMBER + getAccessToken().getString("access_token"));
		httpPost.addHeader("Accept", "application/json");
		httpPost.addHeader("Content-type","application/json; charset=utf-8");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(bos, jsonObject);

		httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
		CloseableHttpResponse response = httpClient.execute(httpPost);

		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	}

	@Override
	public JSONObject wxLogin(MyV3Config myV3Config) throws IOException, URISyntaxException {
		System.out.println("config" + config);
		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.WECHAT_LOGIN);
		newBuilder.setParameter("appid", config.getAppID());
		newBuilder.setParameter("secret", config.getSecret());
		newBuilder.setParameter("js_code", myV3Config.getCode());
		newBuilder.setParameter("grant_type", "authorization_code");

		HttpGet httpGet = new HttpGet(newBuilder.build());
		httpGet.addHeader("Accept", "application/json");
		httpGet.addHeader("Content-type","application/json; charset=utf-8");

		CloseableHttpResponse response = httpClient.execute(httpGet);
		return JSON.parseObject(EntityUtils.toString(response.getEntity()));
	};

	/**
	 * 统一下单接口
	 * @param rootNode
	 * @return
	 * @throws IOException
	 */
	@Override
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
	@Override
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
	@Override
	public JSONObject orderQueryNO(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.QUERY_ORDER_OUT_TRADE_NO + myV3Config.getOutTradeNo());
		return getJsonObject(httpClient, newBuilder);
	}

	/**
	 * 通过 微信支付订单号 查询订单
	 * @param myV3Config
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Override
	public JSONObject orderQueryId(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.QUERY_ORDER_TRANSACTION_ID + myV3Config.getTransactionId());
		return getJsonObject(httpClient, newBuilder);
	}

	@Override
	public JSONObject getJsonObject(CloseableHttpClient httpClient, URIBuilder newBuilder) throws URISyntaxException, IOException {
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
	@Override
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
	@Override
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
	@Override
	public JSONObject tradeBill(MyV3Config myV3Config) throws IOException, URISyntaxException {

		WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
			.withMerchant(config.getMchID(), config.getMerchantSerialNumber(), config.getMerchantPrivateKey())
			.withValidator(config.getValidator());
		// 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
		//通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
		CloseableHttpClient httpClient = builder.build();

		URIBuilder newBuilder = new URIBuilder(WXPayV3Constants.BASIC_URL + WXPayV3Constants.TRADE_BILL);
		newBuilder.setParameter("bill_date", myV3Config.getBillDate());
		newBuilder = myV3Config.getBillType() == null ? newBuilder.setParameter("bill_type", myV3Config.getBillType()) : newBuilder;
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
	@Override
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
