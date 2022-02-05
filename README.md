# WeChatPaySDK
基于springboot框架对接的微信小程序支付接口，包含了官网提供的V2与V3两个版本的接口。

	注意事项：
		  1. MyV2Config.java与MyV3Config.java类中使用了Lombok方法，不使用请自行更改。
		  2. 以下主要配置中所提到的证书地址均为绝对路径。

## V2版本接口说明
V2版本接口代码位于wxPaySdkV2包中，个人使用只需要wxPaySdkV2/config/MyV2Config.java类中配置对应的需求参数。

	主要配置有：	
	            appId -> 应用ID
                mchId -> 商户号
                key   -> 私钥
                CertStream -> 证书地址（需要apiclient_cert.p12证书的绝对地址）

	使用说明：
                我在wxPaySdkV2/controller包中写了基本使用的测试方法，可以自行查看。
                调用的方法位于wxPaySdkV2/server/WXPayV2Server.java类中。
                涉及的的参数建议配合官方文档使用，官方文档地址如下：
                https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1

## V3版本接口说明
V3版本接口代码位于wxPaySdkV3包中，个人使用只需要wxPaySdkV3/config/MyV3Config.java类中配置对应的需求参数。

	主要配置有：	
	            appId -> 应用ID
                mchId -> 商户号
                key   -> 私钥
                ApiV3Key -> ApiV3密钥
                NotifyUrl -> 回调地址（这里为默认回调地址，在构建方法中没有设置回调地址时会使用这里的回调地址）
                CertStream -> 证书地址（本代码中使用的是绝对路径，证书名称为apiclient_cert.p12）
                MerchantSerialNumber -> 证书序列号（需要apiclient_cert.pem证书的绝对地址）
                MerchantPrivateKey -> 商户API私钥（需要apiclient_key.pem证书的绝对地址）

	使用说明：
                我在wxPaySdkV3/controller包中写了基本使用的测试方法，可以自行查看。
                调用的方法位于wxPaySdkV3/server/WXPayV3Server.java类中。
                涉及的的参数建议配合官方文档使用，官方文档地址如下：
                https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_1.shtml