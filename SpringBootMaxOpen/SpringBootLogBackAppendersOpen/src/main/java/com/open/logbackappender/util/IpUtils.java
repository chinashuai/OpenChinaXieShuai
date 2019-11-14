package com.open.logbackappender.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashSet;

public class IpUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);

    /**
     * 静态变量缓存IP
     */
    private static String cachedip = null;

    /**
     * 静态变量缓存IP
     */
    private static String localcachedip = null;

    /*
     * 同步块使用
     */
    private static Object syncObject = new Object();

    static {
        try {
            cachedip = IpUtils.getRealIp();
        } catch (SocketException e) {
            LOGGER.error("", e);
            cachedip = "127.0.0.1";
        }
    }

    /**
     * 取得本机的IP，并把结果放到static变量中
     * 
     * @return 如果有多个IP地址返回外网的IP，多个外网IP返回第一个IP（在多网管等特殊情况下）
     * @throws SocketException
     */
    public static String getRealIpWithStaticCache() {
        if (cachedip == null) {
            synchronized (syncObject) {
                try {
                    cachedip = IpUtils.getRealIp();
                } catch (SocketException e) {
                    LOGGER.error("", e);
                    cachedip = "127.0.0.1";
                }
            }
            return cachedip;
        } else {
            return cachedip;
        }
    }

    /**
     * 取得本机的内网IP，并把结果放到static变量中
     * 
     * @return 如果有多个IP地址返回外网的IP，多个外网IP返回第一个IP（在多网管等特殊情况下）
     * @throws SocketException
     */
    public static String getLocalRealIpWithStaticCache() {
        if (localcachedip == null) {
            synchronized (syncObject) {
                try {
                    localcachedip = IpUtils.getLoaclRealIp();
                } catch (SocketException e) {
                    LOGGER.error("", e);
                    localcachedip = "127.0.0.1";
                }
            }
            return localcachedip;
        } else {
            return localcachedip;
        }
    }

    /**
     * 刷新getRealIpWithStaticCache()方法的static变量
     */
    public static void flushIpStaticCache() {
        synchronized (syncObject) {
            cachedip = null;
        }
    }

    /**
     * 取得本机的IP
     * 
     * @return 如果有多个IP地址返回外网的IP，多个外网IP返回第一个IP（在多网管等特殊情况下）
     * @throws SocketException
     */
    public static String getRealIp() throws SocketException {
        String localip = null; // 本地IP，如果没有配置外网IP则返回它
        String netip = null; // 外网IP

        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false; // 是否找到外网IP
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) { // 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                        && ip.getHostAddress().indexOf(":") == -1) { // 内网IP
                    localip = ip.getHostAddress();
                }
            }
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    /**
     * 取得本机内网的IP
     * 
     * @return 如果有多个内网IP地址返回第一个内网IP
     * @throws SocketException
     */
    public static String getLoaclRealIp() throws SocketException {
        String localip = null; // 本地IP，如果没有配置外网IP则返回它
        String netip = null; // 外网IP

        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false; // 是否找到外网IP
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) { // 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                }
            }
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    /**
     * 取得HTTP请求者的真实IP
     * 
     * @param request
     * @return 返回IP（如果使用代理服务器有可能返回多个IP）
     */
    public static String getHttpClientRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {// NOSONAR
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {// NOSONAR
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {// NOSONAR
            ip = getLocalRealIpWithStaticCache();
        }
        return ip;
    }

    public static String getHttpClientProxyIp(HttpServletRequest request) {
        String ip = request.getHeader("X-ORIGINAL-FORWARDED-FOR");
        if (ip != null && !"".equals(ip)) {
            return getHttpStringProxyIp(ip);
        }
        return "";
    }

    public static String getHttpStringProxyIp(String xOriginalForwardedor) {
        try {
            if (xOriginalForwardedor != null && !"".equals(xOriginalForwardedor)) {
                String[] sp = xOriginalForwardedor.split(",");
                LinkedHashSet<String> sSet = new LinkedHashSet<String>();
                String rString = "";
                for (String s : sp) {
                    sSet.add(s);
                }
                for (String s : sSet) {
                    if ("".equals(rString)) {
                        rString = s;
                    } else {
                        rString = rString + "," + s.trim();
                    }
                }
                return rString;
            }
        } catch (Exception ex) {
            LOGGER.warn("{}", ex.getMessage());
        }
        return "";
    }

    public static String getIpAddr(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        try {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null) {
                String[] arr = ip.split(",");
                ip = arr[0];
            }
        } catch (Exception e) {
            LOGGER.error("获取IP地址错误{}", e);
        }

        return ip;
    }

    public static void main(String[] args) {
        System.out.println(getHttpStringProxyIp("125.90.172.66,122.228.243.79, 121.32.239.2,122.228.243.79"));
    }
}
