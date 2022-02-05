package org.springboot.modules.wxPaySdkV3.config;


import org.springboot.modules.wxPaySdkV3.auth.Validator;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public abstract class WXPayV3Config {



    /**
     * 获取 App ID
     *
     * @return App ID
     */
	public abstract String getAppID();


    /**
     * 获取 Mch ID
     *
     * @return Mch ID
     */
    public abstract String getMchID();


    /**
     * 获取 API 密钥
     *
     * @return API密钥
     */
    public abstract String getKey();

    /**
     * 获取 ApiV3Key 密钥
     *
     * @return ApiV3Key
     */
    public abstract String getApiV3Key();

	/**
	 * 回调接口地址
	 *
	 * @return 回调接口地址
	 */
	public abstract String getNotifyUrl();

	/**
	 * 加载证书
	 * @return
	 * @throws IOException
	 */
	public abstract Validator getValidator() throws IOException;

	/**
	 * 获取签名所需参数
	 * @return
	 * @throws IOException
	 */
	public abstract X509Certificate getCertificate() throws IOException;

	/**
	 * 获取 证书序列号
	 *
	 * @return 证书序列号
	 * @throws IOException
	 */
	public abstract String getMerchantSerialNumber() throws IOException;


	/**
	 * API私钥
	 *
	 * @return API私钥
	 * @throws IOException
	 */
	public abstract PrivateKey getMerchantPrivateKey() throws IOException;

}
