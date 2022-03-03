package org.springboot.modules.wxPaySdkV3.auth;


import org.springboot.modules.wxPaySdkV3.cert.CertManagerSingleton;
import org.springboot.modules.wxPaySdkV3.constant.WXPayV3Constants;

import java.security.cert.X509Certificate;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 在原有CertificatesVerifier基础上，增加定时更新证书功能（默认1小时）
 *
 * @author lianup
 * @since 0.3.0
 */
public class ScheduledUpdateCertificatesVerifier implements Verifier {


    private final ReentrantLock lock;
    private final CertManagerSingleton certManagerSingleton;
    private final CertificatesVerifier verifier;

    public ScheduledUpdateCertificatesVerifier(Credentials credentials, byte[] apiv3Key) {
        lock = new ReentrantLock();
        certManagerSingleton = CertManagerSingleton.getInstance();
        initCertManager(credentials, apiv3Key);
        verifier = new CertificatesVerifier(certManagerSingleton.getCertificates());
    }

    public void initCertManager(Credentials credentials, byte[] apiv3Key) {
        if (credentials == null || apiv3Key.length == 0) {
            throw new IllegalArgumentException("credentials或apiv3Key为空");
        }
        certManagerSingleton.init(credentials, apiv3Key, WXPayV3Constants.UPDATE_INTERVAL_MINUTE);
    }

    @Override
    public X509Certificate getLatestCertificate() {
        return certManagerSingleton.getLatestCertificate();
    }

    @Override
    public boolean verify(String serialNumber, byte[] message, String signature) {
        if (serialNumber.isEmpty() || message.length == 0 || signature.isEmpty()) {
            throw new IllegalArgumentException("serialNumber或message或signature为空");
        }
        if (lock.tryLock()) {
            try {
                verifier.updateCertificates(certManagerSingleton.getCertificates());
            } finally {
                lock.unlock();
            }
        }
        return verifier.verify(serialNumber, message, signature);
    }

    /**
     * 该方法已废弃，请勿使用
     *
     * @return null
     */
    @Deprecated
    @Override
    public X509Certificate getValidCertificate() {
        return null;
    }


    /**
     * 停止定时更新，停止后无法再重新启动
     */
    public void stopScheduledUpdate() {
        certManagerSingleton.close();
    }

}
