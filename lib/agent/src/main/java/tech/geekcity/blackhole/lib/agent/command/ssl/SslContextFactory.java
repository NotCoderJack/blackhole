package tech.geekcity.blackhole.lib.agent.command.ssl;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.X509Certificate;

public class SslContextFactory {
    public static void main(String[] args) {
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(2048);

            //Generate self signed certificate
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 3600);

            System.out.println("Certificate : " + chain[0].toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static SslContext sslContextForServer() throws SSLException {
        String keyCertChainFilePath = keyCertChainFilePath();
        String keyFilePath = keyFilePath();
        String trustCertCollectionFilePath = trustCertCollectionFilePath();
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(
                new File(keyCertChainFilePath),
                new File(keyFilePath));
        if (null != trustCertCollectionFilePath) {
            sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL)
                .build();
    }

    public static SslContext sslContextForClient() throws SSLException {
        String keyCertChainFilePath = keyCertChainFilePath();
        String keyFilePath = keyFilePath();
        String trustCertCollectionFilePath = trustCertCollectionFilePath();
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient()
                .keyManager(new File(keyCertChainFilePath), new File(keyFilePath));
        if (null != trustCertCollectionFilePath) {
            sslContextBuilder.trustManager(new File(trustCertCollectionFilePath));
        }
        return sslContextBuilder.build();
    }

    private static String trustCertCollectionFilePath() {
        return "";
    }

    private static String keyFilePath() {
        return "";
    }

    private static String keyCertChainFilePath() {
        return "";
    }
}
