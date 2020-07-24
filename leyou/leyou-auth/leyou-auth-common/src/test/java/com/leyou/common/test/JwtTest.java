package com.leyou.common.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "C:\\Users\\wjw\\.leyoursa\\rsa.pub";

    private static final String priKeyPath = "C:\\Users\\wjw\\.leyoursa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");    //最后一个是盐，越复杂越好
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        //改成自己的token试试解密
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU5NTI2MDYyOX0.WNv75UHZwBmW7fsoPFeWAkPHd_zE_sokBSz5tczWbKYlII-S3rRCV-gkloFZIv-lRRq_0o8U1vU7cueCzFcORnT_oNwOu0KqOW3KjeKlwUxIblT_14NufRZgOpjwwc897TuPCXF0rkca9lCCyzVUb76P96RAmGj6BNhSDmsqcJI";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}