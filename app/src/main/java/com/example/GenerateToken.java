package com.example;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.lang3.StringUtils;


public class GenerateToken {

    public static final String PROVISION_TOKEN = "provision";
    private static final long EPOCH_SECONDS = 62167219200l;
    private static final String DELIM = "\0";


    public static String generateProvisionToken(String key, String jid, String expires, String vcard) throws NumberFormatException {
        String payload = StringUtils.joinWith(DELIM, PROVISION_TOKEN, jid, calculateExpiry(expires), vcard);
        /*return new String(Base64.encodeBase64(
                (StringUtils.joinWith(DELIM, payload, new HmacUtils(HmacAlgorithms.HMAC_SHA_384, key).hmacHex(payload))).getBytes()
        ));*/
        return new String(Base64.encodeBase64(
                (StringUtils.joinWith(DELIM, payload, HmacUtils.hmacSha384Hex(key, payload))).getBytes()
        ));
    }

    public static String calculateExpiry(String expires) throws NumberFormatException {
        long expiresLong = 0l;
        long currentUnixTimestamp = System.currentTimeMillis() / 1000;
        expiresLong = Long.parseLong(expires);
        return "" + (EPOCH_SECONDS + currentUnixTimestamp + expiresLong);
    }
}
