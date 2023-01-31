package com.ryfast;

import com.ryfast.common.utils.security.CipherUtils;
import org.apache.shiro.codec.Base64;

public class Test {
    public static void main(String[] args) {
        String aes = Base64.encodeToString(CipherUtils.generateNewKey(128, "AES").getEncoded());
        System.out.println(aes);
    }
}
