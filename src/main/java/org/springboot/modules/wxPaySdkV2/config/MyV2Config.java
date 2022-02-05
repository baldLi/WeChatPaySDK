package org.springboot.modules.wxPaySdkV2.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springboot.modules.wxPaySdkV2.constant.WXPayV2Constants;
import org.springboot.modules.wxPaySdkV2.server.IWXPayDomain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author 86151
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MyV2Config extends WXPayV2Config {

	/**
	 * 应用ID
	 * @return
	 */
	@Override
	public String getAppID() {
		return "";
	}

	/**
	 * 商户号
	 * @return
	 */
	@Override
	public String getMchID() {
		return "";
	}

	/**
	 * 私钥
	 * @return
	 */
	@Override
	public String getKey() {
		return "";
	}

	/**
	 * 微信证书内容
	 */
	private byte [] certData;

	@Override
	public InputStream getCertStream() {
		try {
			// 读取绝对路径
			InputStream certStream = new FileInputStream("C:\\apiclient_cert.p12");
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int n = 0;
			while (-1 != (n = certStream.read(buffer))) {
				output.write(buffer, 0, n);
			}
			certStream.close();
			this.certData = output.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ByteArrayInputStream(this.certData);
	}


	@Override
	public IWXPayDomain getWXPayDomain() {
		return new IWXPayDomain() {
			@Override
			public void report(String domain, long elapsedTimeMillis, Exception ex) {

			}

			@Override
			public DomainInfo getDomain(WXPayV2Config config) {
				return new DomainInfo(WXPayV2Constants.DOMAIN_API, true);
			}
		};
	}

	/**
	 * 支付详细内容
	 */
	private String body;

	/**
	 * 支付金额（单位：分， 整数）
	 */
	private String totalFee;

	/**
	 * 退款金额
	 */
	private String refundFee;

	/**
	 * 微信小程序用户登录openId
	 */
	private String openId;

	/**
	 * 微信订单号
	 */
	private String outTradeNo;

	/**
	 * 退款订单号
	 */
	private String outRefundNo;

	/**
	 * 对账单日期
	 */
	private String billDate;

}
