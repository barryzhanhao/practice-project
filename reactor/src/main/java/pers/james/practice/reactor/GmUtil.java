package com.hkb.mid.gxpbridge.pasfcg;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;

import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.sql.Date;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;

/**
 * need jars: bcpkix-jdk15on-160.jar bcprov-jdk15on-160.jar
 *
 * ref: https://tools.ietf.org/html/draft-shen-sm2-ecdsa-02
 * http://gmssl.org/docs/oid.html http://www.jonllen.com/jonllen/work/164.aspx
 *
 * 用BC的注意点：
 * 这个版本的BC对SM3withSM2的结果为asn1格式的r和s，如果需要直接拼接的r||s需要自己转换。下面rsAsn1ToPlainByteArray、rsPlainByteArrayToAsn1就在干这事。
 * 这个版本的BC对SM2的结果为C1||C2||C3，据说为旧标准，新标准为C1||C3||C2，用新标准的需要自己转换。下面changeC1C2C3ToC1C3C2、changeC1C3C2ToC1C2C3就在干这事。
 */
public class GmUtil {

    private static X9ECParameters x9ECParameters = GMNamedCurves.getByName("sm2p256v1");
    private static ECDomainParameters ecDomainParameters = new ECDomainParameters(x9ECParameters.getCurve(),
        x9ECParameters.getG(), x9ECParameters.getN());
    private static ECParameterSpec ecParameterSpec = new ECParameterSpec(x9ECParameters.getCurve(),
        x9ECParameters.getG(), x9ECParameters.getN());

    private PublicKey publicKeyVerify;
    private BCECPrivateKey privateKeySign;
    private byte[] userIdSign;

    // 算法提供者
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    public void init(String certFilePath, String privateKeyHex, String userId) {

        try {
            // 读取证书首行
            FileReader frCert = new FileReader(certFilePath);
            BufferedReader brCertFile = new BufferedReader(frCert);
            String lineCertFile = brCertFile.readLine();
            brCertFile.close();
            frCert.close();

            File certFile = new File(certFilePath);

            // 从证书中获取公钥
            if(lineCertFile.indexOf("-----BEGIN") >= 0) {
                publicKeyVerify = getPublickeyFromX509CaFile(certFile);
            }else {
                publicKeyVerify = getPublickeyFromX509File(certFile);
            }

            privateKeySign = getPrivatekeyFromD(new BigInteger(privateKeyHex, 16));

            // 签名客户标识
            userIdSign = userId.getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    // 签名
    public byte[] sign(byte[] msg) {
        byte[] sign = signSm3WithSm2(msg, userIdSign, privateKeySign);
        // 转为Base64编码
        return Base64.getEncoder().encode(sign);
    }

    // 验签
    public boolean verify(byte[] msg, byte[] signBase64) {
        byte[] sign = Base64.getDecoder().decode(signBase64);
        boolean result = verifySm3WithSm2(msg, userIdSign, sign, publicKeyVerify);
        return result;
    }

    /**
     *
     * @param msg
     * @param userId
     * @param privateKey
     * @return r||s，直接拼接byte数组的rs
     */
    public static byte[] signSm3WithSm2(byte[] msg, byte[] userId, PrivateKey privateKey) {
        return rsAsn1ToPlainByteArray(signSm3WithSm2Asn1Rs(msg, userId, privateKey));
    }

    /**
     *
     * @param msg
     * @param userId
     * @param privateKey
     * @return rs in <b>asn1 format</b>
     */
    public static byte[] signSm3WithSm2Asn1Rs(byte[] msg, byte[] userId, PrivateKey privateKey) {
        try {
            SM2ParameterSpec parameterSpec = new SM2ParameterSpec(userId);
            Signature signer = Signature.getInstance("SM3withSM2", "BC");
            signer.setParameter(parameterSpec);
            signer.initSign(privateKey, new SecureRandom());
            signer.update(msg, 0, msg.length);
            byte[] sig = signer.sign();
            return sig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param msg
     * @param userId
     * @param rs        r||s，直接拼接byte数组的rs
     * @param publicKey
     * @return
     */
    public static boolean verifySm3WithSm2(byte[] msg, byte[] userId, byte[] rs, PublicKey publicKey) {
        return verifySm3WithSm2Asn1Rs(msg, userId, rsPlainByteArrayToAsn1(rs), publicKey);
    }

    /**
     *
     * @param msg
     * @param userId
     * @param rs        in <b>asn1 format</b>
     * @param publicKey
     * @return
     */
    public static boolean verifySm3WithSm2Asn1Rs(byte[] msg, byte[] userId, byte[] rs, PublicKey publicKey) {
        try {
            SM2ParameterSpec parameterSpec = new SM2ParameterSpec(userId);
            Signature verifier = Signature.getInstance("SM3withSM2", "BC");
            verifier.setParameter(parameterSpec);
            verifier.initVerify(publicKey);
            verifier.update(msg, 0, msg.length);
            return verifier.verify(rs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * BC加解密使用旧标c1||c2||c3，此方法在加密后调用，将结果转化为c1||c3||c2
     *
     * @param c1c2c3
     * @return
     */
    private static byte[] changeC1C2C3ToC1C3C2(byte[] c1c2c3) {
        final int c1Len = (x9ECParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1; // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c2c3.length];
        System.arraycopy(c1c2c3, 0, result, 0, c1Len); // c1
        System.arraycopy(c1c2c3, c1c2c3.length - c3Len, result, c1Len, c3Len); // c3
        System.arraycopy(c1c2c3, c1Len, result, c1Len + c3Len, c1c2c3.length - c1Len - c3Len); // c2
        return result;
    }

    /**
     * BC加解密使用旧标c1||c3||c2，此方法在解密前调用，将密文转化为c1||c2||c3再去解密
     *
     * @param c1c3c2
     * @return
     */
    private static byte[] changeC1C3C2ToC1C2C3(byte[] c1c3c2) {
        final int c1Len = (x9ECParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1; // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c3c2.length];
        System.arraycopy(c1c3c2, 0, result, 0, c1Len); // c1: 0->65
        System.arraycopy(c1c3c2, c1Len + c3Len, result, c1Len, c1c3c2.length - c1Len - c3Len); // c2
        System.arraycopy(c1c3c2, c1Len, result, c1c3c2.length - c3Len, c3Len); // c3
        return result;
    }

    /**
     * c1||c3||c2
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] sm2Decrypt(byte[] data, PrivateKey key) {
        return sm2DecryptOld(changeC1C3C2ToC1C2C3(data), key);
    }

    /**
     * c1||c3||c2
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] sm2Encrypt(byte[] data, PublicKey key) {
        return changeC1C2C3ToC1C3C2(sm2EncryptOld(data, key));
    }

    /**
     * c1||c2||c3
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] sm2EncryptOld(byte[] data, PublicKey key) {
        BCECPublicKey localECPublicKey = (BCECPublicKey) key;
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(localECPublicKey.getQ(),
            ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * c1||c2||c3
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] sm2DecryptOld(byte[] data, PrivateKey key) {
        BCECPrivateKey localECPrivateKey = (BCECPrivateKey) key;
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(localECPrivateKey.getD(),
            ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, ecPrivateKeyParameters);
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sm4Encrypt(byte[] keyBytes, byte[] plain) {
        if (keyBytes.length != 16)
            throw new RuntimeException("err key length");
        if (plain.length % 16 != 0)
            throw new RuntimeException("err data length");

        try {
            Key key = new SecretKeySpec(keyBytes, "SM4");
            Cipher out = Cipher.getInstance("SM4/ECB/NoPadding", "BC");
            out.init(Cipher.ENCRYPT_MODE, key);
            return out.doFinal(plain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sm4Decrypt(byte[] keyBytes, byte[] cipher) {
        if (keyBytes.length != 16)
            throw new RuntimeException("err key length");
        if (cipher.length % 16 != 0)
            throw new RuntimeException("err data length");

        try {
            Key key = new SecretKeySpec(keyBytes, "SM4");
            Cipher in = Cipher.getInstance("SM4/ECB/NoPadding", "BC");
            in.init(Cipher.DECRYPT_MODE, key);
            return in.doFinal(cipher);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param bytes
     * @return
     */
    public static byte[] sm3(byte[] bytes) {
        SM3Digest sm3 = new SM3Digest();
        sm3.update(bytes, 0, bytes.length);
        byte[] result = new byte[sm3.getDigestSize()];
        sm3.doFinal(result, 0);
        return result;
    }

    private final static int RS_LEN = 32;

    private static byte[] bigIntToFixexLengthBytes(BigInteger rOrS) {
        byte[] rs = rOrS.toByteArray();
        if (rs.length == RS_LEN)
            return rs;
        else if (rs.length == RS_LEN + 1 && rs[0] == 0)
            return Arrays.copyOfRange(rs, 1, RS_LEN + 1);
        else if (rs.length < RS_LEN) {
            byte[] result = new byte[RS_LEN];
            Arrays.fill(result, (byte) 0);
            System.arraycopy(rs, 0, result, RS_LEN - rs.length, rs.length);
            return result;
        } else {
            throw new RuntimeException("err rs: " + Hex.toHexString(rs));
        }
    }

    /**
     * BC的SM3withSM2签名得到的结果的rs是asn1格式的，这个方法转化成直接拼接r||s
     *
     * @param rsDer rs in asn1 format
     * @return sign result in plain byte array
     */
    private static byte[] rsAsn1ToPlainByteArray(byte[] rsDer) {
        ASN1Sequence seq = ASN1Sequence.getInstance(rsDer);
        byte[] r = bigIntToFixexLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(0)).getValue());
        byte[] s = bigIntToFixexLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(1)).getValue());
        byte[] result = new byte[RS_LEN * 2];
        System.arraycopy(r, 0, result, 0, r.length);
        System.arraycopy(s, 0, result, RS_LEN, s.length);
        return result;
    }

    /**
     * BC的SM3withSM2验签需要的rs是asn1格式的，这个方法将直接拼接r||s的字节数组转化成asn1格式
     *
     * @param sign in plain byte array
     * @return rs result in asn1 format
     */
    private static byte[] rsPlainByteArrayToAsn1(byte[] sign) {
        if (sign.length != RS_LEN * 2)
            throw new RuntimeException("err rs. ");
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, RS_LEN));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, RS_LEN, RS_LEN * 2));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        try {
            return new DERSequence(v).getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取公私钥对象
     *
     * @return
     */
    public static KeyPair generateKeyPair() {
        try {
            ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance("EC", "BC");
            kpGen.initialize(sm2Spec, new SecureRandom());
            KeyPair kp = kpGen.generateKeyPair();
            return kp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成私钥对象
     *
     * @param d
     * @return
     */
    public static BCECPrivateKey getPrivatekeyFromD(BigInteger d) {
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecParameterSpec);
        return new BCECPrivateKey("EC", ecPrivateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 生成公钥对象
     *
     * @param x
     * @param y
     * @return
     */
    public static BCECPublicKey getPublickeyFromXY(BigInteger x, BigInteger y) {
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(x9ECParameters.getCurve().createPoint(x, y),
            ecParameterSpec);
        return new BCECPublicKey("EC", ecPublicKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 从手动生成数字证书获取公钥:需要额外对流Base64转码 如若证书没有"-----BEGIN CERTIFICATE-----“和”-----END
     * CERTIFICATE-----"则用该方法获取公钥
     *
     * @param file
     * @return
     */
    public static PublicKey getPublickeyFromX509File(File file) {
        try {
            PublicKey pKey = null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            FileInputStream in = new FileInputStream(file);
            int len = in.available();
            byte[] buf = new byte[len];
            in.read(buf, 0, len);
            in.close();
            // 手动生成证书
            ByteArrayInputStream bIn = new ByteArrayInputStream(Base64.getDecoder().decode(buf));
            X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(bIn);
            pKey = x509Certificate.getPublicKey();
            System.out.println("pKey:" + pKey);

            return pKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从CA认证数字证书中获取公钥 如若证书有"-----BEGIN CERTIFICATE-----“和”-----END
     * CERTIFICATE-----"则用该方法获取公钥
     *
     * @param file
     * @return
     */
    public static PublicKey getPublickeyFromX509CaFile(File file) {
        try {
            PublicKey pKey = null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            FileInputStream in = new FileInputStream(file);
            X509Certificate x509 = (X509Certificate) cf.generateCertificate(in);
            pKey = x509.getPublicKey();
            System.out.println("pKey:" + pKey);

            return pKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SM2构建证书DN：确认“实体”身份/信息
     *
     * @return
     */
    private static X500NameBuilder createStdBuilder() {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);

        // 1.国家代码
        builder.addRDN(BCStyle.C, "CN");
        // 2.组织
        builder.addRDN(BCStyle.O, "HKB");
        // 3.省份
        builder.addRDN(BCStyle.ST, "Hubei");
        // 4.地区
        builder.addRDN(BCStyle.L, "Wuhan");

        return builder;
    }

    /**
     * SM2拓展密钥用途：使用DERSequence对象构造一个可拓展的密钥用途序列
     *
     * @return
     */
    public static DERSequence extendedKeyUsage() {
        // 1.构造容器对象
        ASN1EncodableVector vector = new ASN1EncodableVector();
        // 2.客户端身份认证
        vector.add(KeyPurposeId.id_kp_clientAuth);
        // 3.安全电子邮件
        vector.add(KeyPurposeId.id_kp_emailProtection);

        return new DERSequence(vector);
    }

    /**
     * SM2保存证书
     *
     * @param x509Certificate
     * @param savePath
     * @throws CertificateEncodingException
     * @throws IOException
     */
    public static void saveCertFile(X509Certificate x509Certificate, Path savePath)
        throws CertificateEncodingException, IOException {
        // 1. 删除已有文件
        if (Files.exists(savePath)) {
            Files.deleteIfExists(savePath);
        }
        // 2.创建新的文件
        Files.createFile(savePath);
        // 3.获取ASN.1编码的证书字节码
        byte[] asn1BinCert = x509Certificate.getEncoded();
        // 4.进行BASE64编码
        byte[] base64EncodedCert = Base64.getEncoder().encode(asn1BinCert);
        // 5.写入文件
        Files.write(savePath, base64EncodedCert);
    }

    /**
     * SM2构造证书信息：使用X509Certificate来设置证书的基本参数
     *
     * @param savePath
     */
    public static void mackeCertFile(KeyPair KeyPair, Path savePath) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.add(Calendar.YEAR, 20);

        try {
            // 1.数字证书使用的签名算法是：SM3withSM2
            ContentSigner sigGen = new JcaContentSignerBuilder("SM3withSM2").setProvider("BC")
                .build(KeyPair.getPrivate());
            // 2.构造证书构建者X.509
            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                // 2.1.颁发者信息
                createStdBuilder().build()
                // 2.2.证书序列号
                , BigInteger.valueOf(1)
                // 2.3.证书生效日期
                , new Date(System.currentTimeMillis() - 50 * 1000)
                // 2.4.证书失效日期
                // , new Date(System.currentTimeMillis() + 3600 * 1000000000)
                , cal.getTime()
                // 2.5.使用者信息（PS：由于是自签证书，所以颁发者和使用者DN都相同）
                , createStdBuilder().build()
                // 2.6.证书公钥
                , KeyPair.getPublic())
                // 2.7.设置密钥用法
                .addExtension(Extension.keyUsage, false,
                    new X509KeyUsage(X509KeyUsage.digitalSignature | X509KeyUsage.nonRepudiation))
                // //2.8.设置扩展密钥用法：客户端身份认证、安全电子邮件
                // .addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage())
                // 2.9.基础约束,标识是否是CA证书，这里false标识为实体证书
                .addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
            // //2.10.Netscape Cert Type SSL客户端身份认证
            // .addExtension(MiscObjectIdentifiers.netscapeCertType, false, new
            // NetscapeCertType(NetscapeCertType.sslClient));
            // 3.生成X.509格式正式对象
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(certGen.build(sigGen));
            // 4.保存证书
            saveCertFile(certificate, savePath);
            System.out.println("证书生成成功!");
        } catch (OperatorCreationException e) {
            System.out.println("证书生成失败：" + e.getMessage());
        } catch (CertificateEncodingException e) {
            System.out.println("证书生成失败：" + e.getMessage());
        } catch (IOException e) {
            System.out.println("证书生成失败：" + e.getMessage());
        } catch (CertificateException e) {
            System.out.println("证书生成失败：" + e.getMessage());
        }
    }

    public static void test1()
        throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
        CertPathBuilderException, InvalidKeyException, SignatureException, CertificateException {
        // 1.手动生成的公私钥
        KeyPair kp = generateKeyPair();
        // 1.1.方式一：生成随机公私钥对象后面用来生成证书与加密（生成证书的公私钥要保存）
        BCECPrivateKey privateKey = (BCECPrivateKey) kp.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey) kp.getPublic();
        // 1.2.方式二：可以选根据自己的公私钥生成公私钥对象用来生成证书
		/*
		BCECPrivateKey privateKey1 = getPrivatekeyFromD(
				new BigInteger("64CFB708FCC261AC97A14C5E5345FC2BBF4AEB0E3F6D8669DB01BBF139CE3DF2", 16));
		BCECPublicKey publicKey1 = getPublickeyFromXY(
				new BigInteger("8368C262EE3689E126058EF6C6C6DB38C0D26BD7B07B7D09D457FE46DF85253A", 16),
				new BigInteger("1F971E87BBA09FA71C8C3BBE0310F6FA37C0EF9213B133575A6AC8FA2094F437", 16));
		 */

        // 1.3.公钥为04 + 64X + 64Y
        String pubKey = new String(Hex.encode(publicKey.getQ().getEncoded(false))).toUpperCase();
        // 1.4. 私钥长度位64
        String prvKey = privateKey.getD().toString(16).toUpperCase();
        System.out.println("随机生成的裸公钥串X+Y：" + pubKey.substring(2));
        System.out.println("随机生成的私钥串：" + prvKey);

        // 2.参数数据
        // 2.1.待处理数据
        String text = "0728900007                                                                                                          20220427091454  22042500561408      202204277624                                E   000108026231521100000686868                        10000RMBC10620000301320327848                                                                                                                                                                                                                                                                                                                                                                            ";
        byte[] msg = text.getBytes();
        // 2.2.默认客户标识
        byte[] userId = "1234567812345678".getBytes();

        // 3.签名
        byte[] sign = signSm3WithSm2(msg, userId, privateKey);
        System.out.println("生成R+S签名：" + new String(Hex.encode(sign)));

        // 3.3.转为Base64编码
        String signStr = Base64.getEncoder().encodeToString(sign);
        System.out.println("生成实际使用签名：" + signStr);

        // 4.核签
        // 4.1.转为Byte[]
        sign = Base64.getDecoder().decode(signStr);
        // 4.2.检验签名
        boolean result = verifySm3WithSm2(msg, userId, sign, publicKey);
        System.out.println("验签结果：" + result);

        // 5.生成证书
        mackeCertFile(kp, Paths.get("D:\\test-cert.cer"));

        // 6.从证书中获取公钥
        File file = new File("D:/test-cert.cer");
        if (file.exists()) {
            PublicKey pKey = getPublickeyFromX509File(file);// 从软算法生成证书中获取公钥
            // pKey = getPublickeyFromX509CaFile(file);// 从CA证书中获取公钥
            String pKeyStr = new String(Hex.encode(((BCECPublicKey) pKey).getQ().getEncoded(false))).toUpperCase();
            System.out.println("读取到公钥：" + pKeyStr.substring(2));
        } else {
            System.out.println("文件不存在!");
        }

    }

    public static void test2() throws Exception {
        GmUtil gmUtil = new GmUtil();

        // 外部提供的证书文件，用于获取公钥验签
        String PAPubCertFile = "pasfcg-pingan-cert.cer";

        // 我方私钥，用于签名
        String HkbPriKeyHex = "EED83FB1EA1B256138ADA97199D0E18E1AD7BF74597C09D5B62991D980CA026F";

        // 签名客户标识
        String signUserId = "1234567812345678";

        // 初始化：获取参数
        gmUtil.init(PAPubCertFile, HkbPriKeyHex, signUserId);

        // 签名
        byte[] signBase64 = gmUtil.sign("abc123".getBytes("gbk"));

        // 验签
        boolean result = gmUtil.verify("abc123".getBytes("gbk"), signBase64);
    }

    public static void main(String[] args) throws Exception {
        test1();
    }
}
