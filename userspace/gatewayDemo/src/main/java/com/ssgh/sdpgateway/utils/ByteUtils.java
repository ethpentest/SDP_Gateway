package com.ssgh.sdpgateway.utils;

import com.ssgh.sdpgateway.spa.message.SPAMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

public class ByteUtils {
    /**
     * 将int转为高字节在前，低字节在后的byte数组（大端）
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteBig(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }
    /**
     * 将int转为低字节在前，高字节在后的byte数组（小端）
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteLittle(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }
    /**
     * byte数组到int的转换(小端)
     * @param bytes
     * @return
     */
    public static int bytes2IntLittle(byte[] bytes )
    {
        int int1=bytes[0]&0xff;
        int int2=(bytes[1]&0xff)<<8;
        int int3=(bytes[2]&0xff)<<16;
        int int4=(bytes[3]&0xff)<<24;

        return int1|int2|int3|int4;
    }
    /**
     * byte数组到int的转换(大端)
     * @param bytes
     * @return
     */
    public static int bytes2IntBig(byte[] bytes )
    {
        int int1=bytes[3]&0xff;
        int int2=(bytes[2]&0xff)<<8;
        int int3=(bytes[1]&0xff)<<16;
        int int4=(bytes[0]&0xff)<<24;

        return int1|int2|int3|int4;
    }
    /**
     * 将short转为高字节在前，低字节在后的byte数组（大端）
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteBig(short n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) (n >> 8 & 0xff);
        return b;
    }

    /**
     * 将short转为低字节在前，高字节在后的byte数组(小端)
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteLittle(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }
    /**
     *  读取小端byte数组为short
     * @param b
     * @return
     */
    public static short byteToShortLittle(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }
    /**
     *  读取大端byte数组为short
     * @param b
     * @return
     */
    public static short byteToShortBig(byte[] b) {
        return (short) (((b[0] << 8) | b[1] & 0xff));
    }
    /**
     * long类型转byte[] (大端)
     * @param n
     * @return
     */
    public static byte[] longToBytesBig(long n) {
        byte[] b = new byte[8];
        b[7] = (byte) (n & 0xff);
        b[6] = (byte) (n >> 8  & 0xff);
        b[5] = (byte) (n >> 16 & 0xff);
        b[4] = (byte) (n >> 24 & 0xff);
        b[3] = (byte) (n >> 32 & 0xff);
        b[2] = (byte) (n >> 40 & 0xff);
        b[1] = (byte) (n >> 48 & 0xff);
        b[0] = (byte) (n >> 56 & 0xff);
        return b;
    }
    /**
     * long类型转byte[] (小端)
     * @param n
     * @return
     */
    public static byte[] longToBytesLittle(long n) {
        byte[] b = new byte[8];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8  & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        b[4] = (byte) (n >> 32 & 0xff);
        b[5] = (byte) (n >> 40 & 0xff);
        b[6] = (byte) (n >> 48 & 0xff);
        b[7] = (byte) (n >> 56 & 0xff);
        return b;
    }
    /**
     * byte[]转long类型(小端)
     * @param array
     * @return
     */
    public static long bytesToLongLittle( byte[] array ) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] < 0) {
                array[i] = (byte) ((int)array[i] + 256);
            }
        }
        return ((((long) array[ 0] & 0xff) << 0)
                | (((long) array[ 1] & 0xff) << 8)
                | (((long) array[ 2] & 0xff) << 16)
                | (((long) array[ 3] & 0xff) << 24)
                | (((long) array[ 4] & 0xff) << 32)
                | (((long) array[ 5] & 0xff) << 40)
                | (((long) array[ 6] & 0xff) << 48)
                | (((long) array[ 7] & 0xff) << 56));
    }

    /**
     * byte[]转long类型(大端)
     * @param array
     * @return
     */
    public static long bytesToLongBig( byte[] array ) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] < 0) {
                array[i] += 256;
            }
        }
        return ((((long) array[ 0] & 0xff) << 56)
                | (((long) array[ 1] & 0xff) << 48)
                | (((long) array[ 2] & 0xff) << 40)
                | (((long) array[ 3] & 0xff) << 32)
                | (((long) array[ 4] & 0xff) << 24)
                | (((long) array[ 5] & 0xff) << 16)
                | (((long) array[ 6] & 0xff) << 8)
                | (((long) array[ 7] & 0xff) << 0));
    }
    public static long bytesToLong(byte[] array) {
        // 将byte[] 封装为 ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getLong();
    }

    /**
     * hmac字节数组转化为存入数据库中的hmac值
     * @param bytes hmac字节数组
     * @return 存入数据库中的hmac
     */
    public static String[] bytesToHmac(byte[] bytes) {
        byte[] bytes1 = Arrays.copyOfRange(bytes, 0, 8);
        byte[] bytes2 = Arrays.copyOfRange(bytes, 8, 16);
        byte[] bytes3 = Arrays.copyOfRange(bytes, 16, 24);
        byte[] bytes4 = Arrays.copyOfRange(bytes, 24, 32);
        long l1 = ByteUtils.bytesToLongLittle(bytes1);
        String str1 = Long.toUnsignedString(l1);
        long l2 = ByteUtils.bytesToLongLittle(bytes2);
        String str2 = Long.toUnsignedString(l2);
        long l3 = ByteUtils.bytesToLongLittle(bytes3);
        String str3 = Long.toUnsignedString(l3);
        long l4 = ByteUtils.bytesToLongLittle(bytes4);
        String str4 = Long.toUnsignedString(l4);
        return new String[] {str1, str2, str3, str4};
    }

    /**
     * spa数据包转化为字节数组，spa数据包的自定义序列化
     * @param spaMessage 数据包
     * @return 序列化数组
     */
    public static byte[] spaMessageToBytes(SPAMessage spaMessage) {
        byte[] buf = new byte[160];
        byte[] clientIp = ByteUtils.intToByteLittle(spaMessage.getClientIp());
        byte[] timeStamp = ByteUtils.longToBytesLittle(spaMessage.getTimeStamp());
        byte[] randomNum = ByteUtils.intToByteLittle(spaMessage.getRandomNum());
        byte[] messageType = ByteUtils.intToByteLittle(spaMessage.getMessageType());
        byte[] clientId = ByteUtils.intToByteLittle(spaMessage.getClientId());
        byte[] defaultValue = ByteUtils.longToBytesLittle(spaMessage.getDefaultValue());
        byte[] userId = spaMessage.getUserId().getBytes();
        byte[] deviceId = spaMessage.getDeviceId().getBytes();
        byte[] hotp = spaMessage.getHotp().getBytes();
        byte[] hmac = spaMessage.getHmac().getBytes();

        for (int i = 0; i < 4; i++) {
            buf[i] = clientIp[i];
        }
        for (int i = 4; i < 12; i++) {
            buf[i] = timeStamp[i - 4];
        }
        for (int i = 12; i < 16; i++) {
            buf[i] = randomNum[i - 12];
        }
        for (int i = 16; i < 20; i++) {
            buf[i] = messageType[i - 16];
        }
        for (int i = 20; i < 28; i++) {
            buf[i] = defaultValue[i - 20];
        }
        for (int i = 28; i < 32; i++) {
            buf[i] = clientId[i - 28];
        }
        for (int i = 32; i < 64; i++) {
            buf[i] = userId[i - 32];
        }
        for (int i = 64; i < 96; i++) {
            buf[i] = deviceId[i - 64];
        }
        for (int i = 96; i < 128; i++) {
            buf[i] = hotp[i - 96];
        }
        for (int i = 128; i < 160; i++) {
            buf[i] = hmac[i - 128];
        }
        return buf;
    }

    public static SPAMessage bytesToSPAMessage(byte[] bytes) {
        SPAMessage spaMessage = new SPAMessage();

        // Base64解码
        final Base64.Decoder decoder = Base64.getDecoder();
        final Base64.Encoder encoder = Base64.getEncoder();
        
        byte[] clientIp = Arrays.copyOfRange(bytes, 0, 4);
        byte[] timeStamp = Arrays.copyOfRange(bytes, 4, 12);
        byte[] randomNum = Arrays.copyOfRange(bytes, 12, 16);
        byte[] messageType = Arrays.copyOfRange(bytes, 16, 20);
        byte[] clientId = Arrays.copyOfRange(bytes, 20, 24);
        byte[] defaultValue = Arrays.copyOfRange(bytes, 24, 32);
        byte[] userId = Arrays.copyOfRange(bytes, 32, 64);
        byte[] deviceId = Arrays.copyOfRange(bytes, 64, 96);
        byte[] hotpBase64 = Arrays.copyOfRange(bytes, 96, 128);
        byte[] hmacBase64 = Arrays.copyOfRange(bytes, 128, 160);

        final String hotp = encoder.encodeToString(hotpBase64);
        final String hmac = encoder.encodeToString(hmacBase64);

        spaMessage.setClientIp(bytes2IntLittle(clientIp));
        spaMessage.setTimeStamp(bytesToLongLittle(timeStamp));
        spaMessage.setRandomNum(bytes2IntLittle(randomNum));
        spaMessage.setMessageType(bytes2IntLittle(messageType));
        spaMessage.setClientId(bytes2IntLittle(clientId));
        spaMessage.setDefaultValue(bytesToLongLittle(defaultValue));
        spaMessage.setUserId(new String(userId));
        spaMessage.setDeviceId(new String(deviceId));
        spaMessage.setHotp(hotp);
        spaMessage.setHmac(hmac);
        return spaMessage;
    }

    public static int ip2Int(String ipString) {
        // 取 ip 的各段
        String[] ipSlices = ipString.split("\\.");
        int rs = 0;
        for (int i = 0; i < ipSlices.length; i++) {
            // 将 ip 的每一段解析为 int，并根据位置左移 8 位
            int intSlice = Integer.parseInt(ipSlices[i]) << 8 * i;
            // 或运算
            rs = rs | intSlice;
        }
        return rs;
    }

    public static String int2Ip(int ipInt) {
        String[] ipString = new String[4];
        for (int i = 0; i < 4; i++) {
            // 每 8 位为一段，这里取当前要处理的最高位的位置
            int pos = i * 8;
            // 取当前处理的 ip 段的值
            int and = ipInt & (255 << pos);
            // 将当前 ip 段转换为 0 ~ 255 的数字，注意这里必须使用无符号右移
            ipString[i] = String.valueOf(and >>> pos);
        }
        return String.join(".", ipString);
    }

    public static String intToIp(int intIp) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(intIp>> 24)+".");
        builder.append(String.valueOf((intIp& 0x00FFFFFF) >> 16)+".");
        builder.append(String.valueOf((intIp& 0x0000FFFF) >> 8)+".");
        builder.append(String.valueOf((intIp& 0x000000FF)));
        return builder.toString();
    }

    public static boolean isValidIPAddress(String ipAddress) {
        if ((ipAddress != null) && (!ipAddress.isEmpty())) {
            return Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ipAddress);
        }
        return false;
    }

}
