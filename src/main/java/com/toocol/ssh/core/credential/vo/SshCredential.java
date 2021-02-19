package com.toocol.ssh.core.credential.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:57
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SshCredential implements Serializable {

    private static final long serialVersionUID = 1184930928749870706L;

    private String ip;

    private String user;

    private String password;
}
