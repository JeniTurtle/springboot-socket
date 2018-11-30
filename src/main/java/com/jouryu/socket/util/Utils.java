package com.jouryu.socket.util;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by tomorrow on 18/11/13.
 */

@Component
public class Utils {

    private Utils() {}

    /**src
     * byte[]转16进制字符串
     *
     * @param
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] hexStringToBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    /**
     * 通过Map的value查询key
     * @param map
     * @param value
     * @param <T>
     * @param <I>
     * @return
     */
    public static <T, I> T getKeyByValue(Map<T, I> map, I value){
        T ret = null;

        for(T key: map.keySet()){
            if (map.get(key).equals(value)){
                ret = key;
            }
        }
        return ret;
    }

    /**
     * CRC 加密
     * @param bytes
     * @return
     */
    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int high,low;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }

        }
        high=CRC%256;
        low=CRC/256;
        int res=high*256+low;
        return Integer.toHexString(res);
    }
}
