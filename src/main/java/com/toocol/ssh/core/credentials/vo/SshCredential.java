package com.toocol.ssh.core.credentials.vo;

import io.vertx.core.json.JsonObject;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:57
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SshCredential implements Serializable {

    private static final long serialVersionUID = 1184930928749870706L;

    /**
     * the ip of target server.
     */
    private String host;
    /**
     * the user of target server.
     */
    private String user;
    /**
     * the password of target server.
     */
    private String password;
    /**
     * the port of target server.
     */
    private int port;

    public static SshCredential transFromJson(JsonObject jsonObject) {
        return new SshCredential(
                jsonObject.getString("host"),
                jsonObject.getString("user"),
                jsonObject.getString("password"),
                jsonObject.getInteger("port")
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(4);
        map.put("host", host);
        map.put("user", user);
        map.put("password", password);
        map.put("port", port);
        return map;
    }

    @Override
    public String toString() {
        return "SshCredential{" +
                "host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SshCredential that = (SshCredential) o;

        return new EqualsBuilder().append(port, that.port).append(host, that.host).append(user, that.user).append(password, that.password).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(host).append(user).append(password).append(port).toHashCode();
    }
}
