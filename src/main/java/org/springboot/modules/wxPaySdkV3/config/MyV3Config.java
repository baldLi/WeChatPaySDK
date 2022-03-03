package org.springboot.modules.wxPaySdkV3.config;

import lombok.Data;
import org.springboot.modules.wxPaySdkV3.auth.*;
import org.springboot.modules.wxPaySdkV3.util.PemUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * @author 86151
 */
@Data
@Service
public class MyV3Config extends WXPayV3Config {

	@Override
	public String getAppID() {
		return "";
	}

	@Override
	public String getMchID() {
		return "";
	}

	@Override
	public String getKey() {
		return "";
	}

	/**
	 * ApiV3密钥
	 * @return
	 */
	@Override
	public String getApiV3Key() {
		return "";
	}

	@Override
	public String getSecret() {
		return "";
	}

	/**
	 * 微信回调地址
	 * @return
	 */
	@Override
	public String getNotifyUrl() {
		return "";
	}

	/**
	 * 加载证书
	 * @return
	 * @throws IOException
	 */
	@Override
	public Validator getValidator() throws IOException {
		ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
			new WechatPay2Credentials(getMchID(), new PrivateKeySigner(getMerchantSerialNumber(), getMerchantPrivateKey())),
			getApiV3Key().getBytes(StandardCharsets.UTF_8));
		return new WechatPay2Validator(verifier);
	}

	/**
	 * 获取签名所需参数
	 * @return
	 * @throws IOException
	 */
	@Override
	public X509Certificate getCertificate() throws IOException {
		ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
			new WechatPay2Credentials(getMchID(), new PrivateKeySigner(getMerchantSerialNumber(), getMerchantPrivateKey())),
			getApiV3Key().getBytes(StandardCharsets.UTF_8));
		return verifier.getLatestCertificate();
	}

	/**
	 * 获取 证书序列号
	 *
	 * @return 证书序列号
	 * @throws IOException
	 */
	@Override
	public String getMerchantSerialNumber() throws IOException {
		// 绝对路径
		InputStream inputStream = new FileInputStream("C:\\apiclient_cert.pem");

		// Resources路径
//		File file = ResourceUtils.getFile("classpath:certificate/apiclient_cert.pem");
//		InputStream inputStream = new FileInputStream(file);
		X509Certificate cert = PemUtil.loadCertificate(inputStream);
		return cert.getSerialNumber().toString(16);
	}

	/**
	 * API私钥
	 *
	 * @return API私钥
	 * @throws IOException
	 */
	@Override
	public PrivateKey getMerchantPrivateKey() throws IOException {
		// 绝对路径
		InputStream inputStream = new FileInputStream("C:\\apiclient_key.pem");

		// Resources路径
//		File file = ResourceUtils.getFile("classpath:certificate/apiclient_key.pem");
//		InputStream inputStream = new FileInputStream(file);
		return PemUtil.loadPrivateKey(inputStream);
	}

	/**
	 * 微信登录Code
	 */
	private String code;

	/**
	 * 支付详细内容
	 */
	private String body;

	/**
	 * 支付金额（单位：分， 整数）
	 */
	private int totalFee;

	/**
	 * 退款金额
	 */
	private int refundFee;

	/**
	 * 微信小程序用户登录openId
	 */
	private String openId;

	/**
	 * 商户订单号
	 */
	private String outTradeNo;

	/**
	 * 微信支付订单号
	 */
	private String transactionId;

	/**
	 * 退款订单号
	 */
	private String outRefundNo;

	/**
	 * 商品描述
	 */
	private String description;

	/**
	 * 退款币种
	 */
	private String currency;

	/**
	 * 账单日期
	 */
	private String billDate;

	/**
	 * 账单类型
	 */
	private String billType;

}
