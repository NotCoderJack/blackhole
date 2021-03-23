package tech.geekcity.blackhole.application.army.knife.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.core.exception.BugException;
import tech.geekcity.blackhole.lib.ssh.SimpleScp;
import tech.geekcity.blackhole.lib.ssh.SshCommander;
import tech.geekcity.blackhole.lib.ssh.wrap.RsaKeyPairWrap;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

@FreeBuilder
@JsonDeserialize(builder = SshConnector.Builder.class)
public abstract class SshConnector implements Configurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshConnector.class);
    private transient boolean configured = false;
    private transient RsaKeyPairWrap rsaKeyPairWrapToUse;
    private transient SshCommander sshCommander;
    private transient SimpleScp simpleScp;
    private transient boolean validated = false;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link SshConnector}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SshConnector} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SshConnector_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public String toJsonSilently() {
            try {
                return objectMapper.writeValueAsString(build());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public SshConnector parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SshConnector.class);
        }
    }

    @Override
    public void configure() throws IOException {
        if (configured) {
            return;
        }
        RsaKeyPairWrap rsaKeyPairWrap = rsaKeyPairWrap();
        String password = password();
        Preconditions.checkArgument(null != password || null != rsaKeyPairWrap,
                "password or rsaKeyPairWrap cannot both be null");
        if (null == rsaKeyPairWrap) {
            try {
                rsaKeyPairWrapToUse = RsaKeyPairWrap.Builder.newInstance().generate(2048);
            } catch (NoSuchAlgorithmException e) {
                throw new BugException("cannot generate rsa key pair wrap");
            }
            copyPublicKey(host(), port(), username(), password, rsaKeyPairWrapToUse.publicKeyAsString(username()));
        } else {
            rsaKeyPairWrapToUse = rsaKeyPairWrap;
        }
        configured = true;
    }


    @Override
    public void close() throws IOException {
        if (null != sshCommander) {
            sshCommander.close();
            sshCommander = null;
        }
        if (null != simpleScp) {
            simpleScp.close();
            simpleScp = null;
        }
        if (validated) {
            validated = false;
        }
        if (null != rsaKeyPairWrapToUse) {
            rsaKeyPairWrapToUse = null;
        }
        if (configured) {
            configured = false;
        }
    }

    public abstract String username();

    public abstract String host();

    public abstract int port();

    public abstract OutputStream standardOutput();

    public abstract OutputStream errorOutput();

    @Nullable
    public abstract RsaKeyPairWrap rsaKeyPairWrap();

    @Nullable
    public abstract String password();

    public SshCommander validateSshCommander() throws IOException {
        if (!validated) {
            validate();
            validated = true;
        }
        return sshCommander;
    }

    public SimpleScp validateSimpleScp() throws IOException {
        if (!validated) {
            validate();
            validated = true;
        }
        return simpleScp;
    }

    private void validate() throws IOException {
        SshClientWrap sshClientWrap = SshClientWrap.Builder.newInstance()
                .username(username())
                .host(host())
                .port(port())
                .rsaKeyPairWrap(rsaKeyPairWrapToUse)
                .build();
        sshCommander = SshCommander.Builder.newInstance()
                .sshClientWrap(sshClientWrap)
                .standardOutput(standardOutput())
                .errorOutput(errorOutput())
                .build();
        simpleScp = SimpleScp.Builder.newInstance()
                .sshClientWrap(sshClientWrap)
                .build();
        sshCommander.configure();
        simpleScp.configure();
        sshCommander.runAndCheckReturn("cat /etc/redhat-release");
        File tempFile = File.createTempFile("readhat-release.", ".tmp");
        tempFile.delete();
        simpleScp.download(Collections.singletonList("/etc/redhat-release"), tempFile.getAbsolutePath());
        tempFile.delete();
        LOGGER.info("validation for succeed: {} {}", standardOutput().toString(), errorOutput().toString());
    }

    private void copyPublicKey(
            String host,
            int port,
            String username,
            String password,
            String publicKeyAsStringToWrite)
            throws IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try (SshCommander sshCommander = SshCommander.Builder.newInstance()
                .sshClientWrap(SshClientWrap.Builder.newInstance()
                        .username(username)
                        .host(host)
                        .port(port)
                        .password(password)
                        .build())
                .standardOutput(stdout)
                .errorOutput(stderr)
                .build()) {
            sshCommander.configure();
            sshCommander.runAndCheckReturn("mkdir -p $HOME/.ssh && chmod 700 $HOME/.ssh");
            sshCommander.runAndCheckReturn(String.format(
                    "echo '%s' >> $HOME/.ssh/authorized_keys", publicKeyAsStringToWrite));
        }
    }
}
