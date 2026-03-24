package com.example.doanck.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayUtil {

    public static String createQueryString(Map<String,String> params){

        StringBuilder query = new StringBuilder();

        for(Map.Entry<String,String> entry : new TreeMap<>(params).entrySet()){

            if(query.length() > 0){
                query.append("&");
            }

            query.append(
                    URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
            );

            query.append("=");

            query.append(
                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
            );
        }

        return query.toString();
    }

    // TẠO CHỮ KÝ VNPay
    public static String hmacSHA512(String key, String data){

        try{

            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            key.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA512");

            mac.init(secretKey);

            byte[] rawHmac =
                    mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();

            for(byte b : rawHmac){

                String hex = Integer.toHexString(0xff & b);

                if(hex.length()==1){
                    hash.append('0');
                }

                hash.append(hex);
            }

            return hash.toString();

        }catch(Exception e){

            throw new RuntimeException("Error while hashing");

        }

    }
}