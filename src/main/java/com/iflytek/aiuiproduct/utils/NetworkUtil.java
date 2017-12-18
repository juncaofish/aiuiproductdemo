package com.iflytek.aiuiproduct.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;


/**
 * 网络信息
 * 
 * @author yjzhao
 * 
 */
public class NetworkUtil {

	public static final String NET_UNKNOWN = "none";

	// wifi, cmwap, ctwap, uniwap, cmnet, uninet, ctnet,3gnet,3gwap
	// 其中3gwap映射为uniwap
	public static final String NET_WIFI   = "wifi";
	public static final String NET_CMWAP  = "cmwap";
	public static final String NET_UNIWAP = "uniwap";
	public static final String NET_CTWAP  = "ctwap";
	public static final String NET_CTNET = "ctnet";

	/**
	 * 判断当前网络是否为wifi网络
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnect(Context context) {
		String netType = "";
		if(context != null)
		{
			ConnectivityManager  nmgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo active = nmgr.getActiveNetworkInfo();
			if(active == null) {
				
			} else {
				netType = NetworkUtil.getNetType(active);
			}
		}
		return NET_WIFI.equals(netType);
	}


	/**
	 * 获取网络类型参数，包括cmwap,uniwap,ctwap,wifi,cmnet,ctnet,uninet,3gnet
	 * 由于底层msc无法处理3gwap，3gwap映射为uniwap
	 * @param info
	 * @return
	 */
	public static String getNetType(NetworkInfo info)
	{
		if(info == null)
			return NET_UNKNOWN;

		try {
			if(info.getType() == ConnectivityManager.TYPE_WIFI)
				return NET_WIFI;
			else
			{
				String extra = info.getExtraInfo().toLowerCase();
				if(TextUtils.isEmpty(extra))
					return NET_UNKNOWN;
				// 3gwap由于底层msc兼容不了，转换为uniwap
				if(extra.startsWith("3gwap") || extra.startsWith(NET_UNIWAP))
				{
					return NET_UNIWAP;
				}else if(extra.startsWith(NET_CMWAP))
				{
					return NET_CMWAP;
				}else if(extra.startsWith(NET_CTWAP))
				{
					return NET_CTWAP;
				}else if(extra.startsWith( NET_CTNET )){
					return NET_CTNET;
				}else
					return extra;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NET_UNKNOWN;
	}

	/**
	 * 获取网络类型详细信息，包括EDGE、CDMA-EvDo rev.A、HSDPA等
	 * @param info
	 * @return 以字符串加int类型进行组合，如EDGE;2
	 */
	public static String getNetSubType(NetworkInfo info)
	{
		if(info == null)
			return NET_UNKNOWN;
		try {
			if(info.getType() == ConnectivityManager.TYPE_WIFI)
				return NET_UNKNOWN;
			else
			{
				String subtype = "";
				subtype += info.getSubtypeName();
				subtype += ";" + info.getSubtype();
				return subtype;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NET_UNKNOWN;
	}


	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (checkPermission(context, "android.permission.INTERNET")) {
			ConnectivityManager cManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 检查APP权限是否开通
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	private static boolean checkPermission(Context context, String str) {
		return context.checkCallingOrSelfPermission(str) == PackageManager.PERMISSION_GRANTED;
	}
	
	//        /**
	//         * 判断网络是否可用 <br>
	//         * code from: http://www.androidsnippets.com/have-internet
	//         * 
	//         * @param context
	//         * @return
	//         */
	//        public static boolean haveInternet(Context context) {
	//                NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
	//                                .getSystemService(Context.CONNECTIVITY_SERVICE))
	//                                .getActiveNetworkInfo();
	//                if (info == null || !info.isConnected()) {
	//                    return false;
	//                }
	//                if (info.isRoaming()) {
	//                    // here is the roaming option you can change it if you want to
	//                    // disable internet while roaming, just return false
	//                    // 是否在漫游，可根据程序需求更改返回值
	//                    return false;
	//                }
	//                return true;
	//        }
	// 
	//        /**
	//         * 判断网络是否可用
	//         * @param context
	//         * @return
	//         */
	//        public static boolean isnetWorkAvilable(Context context) {
	//                ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	//                if(connectivityManager == null) {
	//                	DebugLog.LogD("couldn't get connectivity manager");
	//                } else {
	//                    NetworkInfo [] networkInfos = connectivityManager.getAllNetworkInfo();
	//                    if(networkInfos != null){ 
	//                        for (int i = 0, count = networkInfos.length; i < count; i++) {
	//                            if(networkInfos[i].getState() == NetworkInfo.State.CONNECTED){
	//                                return true;
	//                            }
	//                        }
	//                    }
	//                }
	//                return false;
	//        }
	//        /**
	//         * IP地址<br>
	//         * code from:
	//         * http://www.droidnova.com/get-the-ip-address-of-your-device,304.html <br>
	//         * 
	//         * @return 如果返回null，证明没有网络链接。 如果返回String，就是设备当前使用的IP地址，不管是WiFi还是3G
	//         */
	//        public static String getLocalIpAddress() {
	//                try {
	//                    for (Enumeration<NetworkInterface> en = NetworkInterface
	//                                    .getNetworkInterfaces(); en.hasMoreElements();) 
	//                    {
	//                        NetworkInterface intf = en.nextElement();
	//                        for (Enumeration<InetAddress> enumIpAddr = intf
	//                                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
	//                            InetAddress inetAddress = enumIpAddr.nextElement();
	//                            if (!inetAddress.isLoopbackAddress()) {
	//                                    return inetAddress.getHostAddress().toString();
	//                            }
	//                        }
	//                    }
	//                } catch (SocketException ex) {
	//                	DebugLog.LogD("getLocalIpAddress : " + ex.toString());
	//                }
	//                return null;
	//        }
	//        /**
	//         * 获取MAC地址 <br>
	//         * code from: http://orgcent.com/android-wifi-mac-ip-address/
	//         * 
	//         * @param context
	//         * @return
	//         */
	//        public static String getLocalMacAddress(Context context) {
	//            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	//            WifiInfo info = wifi.getConnectionInfo();
	//            return info.getMacAddress();
	//        }
	//        /**
	//         * WIFI 是否可用
	//         * @param context
	//         * @return
	//         */
	//        public static boolean isWiFiActive(Context context) {
	//                ConnectivityManager connectivity = (ConnectivityManager) context  
	//                .getSystemService(Context.CONNECTIVITY_SERVICE);  
	//	        if (connectivity != null) {  
	//	            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
	//	            if (info != null) {  
	//	                for (int i = 0; i < info.length; i++) {  
	//	                    if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {  
	//	                        return true;  
	//	                    }  
	//	                }  
	//	            }  
	//	        }  
	//	        return false;
	//        }
	//        /**
	//         * 存在多个连接点
	//         * @param context
	//         * @return
	//         */
	//        public static boolean hasMoreThanOneConnection(Context context){
	//                ConnectivityManager manager = (ConnectivityManager)context
	//                        .getSystemService(Context.CONNECTIVITY_SERVICE);
	//                if(manager==null){
	//                    return false;
	//                }else{
	//                    NetworkInfo [] info = manager.getAllNetworkInfo();
	//                    int counter = 0;
	//                    for(int i = 0 ;i<info.length;i++){
	//                        if(info[i].isConnected()){
	//                                counter++;
	//                        }
	//                    }
	//                    if(counter>1){
	//                        return true;
	//                    }
	//                }
	//                return false;
	//        }
	//     /*
	//     * HACKISH: These constants aren't yet available in my API level (7), but I need to handle these cases if they come up, on newer versions
	//     */
	//    public static final int NETWORK_TYPE_EHRPD=14; // Level 11
	//    public static final int NETWORK_TYPE_EVDO_B=12; // Level 9
	//    public static final int NETWORK_TYPE_HSPAP=15; // Level 13
	//    public static final int NETWORK_TYPE_IDEN=11; // Level 8
	//    public static final int NETWORK_TYPE_LTE=13; // Level 11
	//    /**
	//     * Check if there is any connectivity
	//     * @param context
	//     * @return
	//     */
	//    public static boolean isConnected(Context context){
	//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	//        NetworkInfo info = cm.getActiveNetworkInfo();
	//        return (info != null && info.isConnected());
	//    }
	//    /**
	//     * Check if there is fast connectivity
	//     * @param context
	//     * @return
	//     */
	//    public static boolean isConnectedFast(Context context){
	//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	//        NetworkInfo info = cm.getActiveNetworkInfo();
	//        return (info != null && info.isConnected() && isConnectionFast(info.getType(),info.getSubtype()));
	//    }
	//    /**
	//     * Check if the connection is fast
	//     * @param type
	//     * @param subType
	//     * @return
	//     */
	//    public static boolean isConnectionFast(int type, int subType){
	//        if(type==ConnectivityManager.TYPE_WIFI){
	//            System.out.println("CONNECTED VIA WIFI");
	//            return true;
	//        }else if(type==ConnectivityManager.TYPE_MOBILE){
	//            switch(subType){
	//            case TelephonyManager.NETWORK_TYPE_1xRTT:
	//                return false; // ~ 50-100 kbps
	//            case TelephonyManager.NETWORK_TYPE_CDMA:
	//                return false; // ~ 14-64 kbps
	//            case TelephonyManager.NETWORK_TYPE_EDGE:
	//                return false; // ~ 50-100 kbps
	//            case TelephonyManager.NETWORK_TYPE_EVDO_0:
	//                return true; // ~ 400-1000 kbps
	//            case TelephonyManager.NETWORK_TYPE_EVDO_A:
	//                return true; // ~ 600-1400 kbps
	//            case TelephonyManager.NETWORK_TYPE_GPRS:
	//                return false; // ~ 100 kbps
	//            case TelephonyManager.NETWORK_TYPE_HSDPA:
	//                return true; // ~ 2-14 Mbps
	//            case TelephonyManager.NETWORK_TYPE_HSPA:
	//                return true; // ~ 700-1700 kbps
	//            case TelephonyManager.NETWORK_TYPE_HSUPA:
	//                return true; // ~ 1-23 Mbps
	//            case TelephonyManager.NETWORK_TYPE_UMTS:
	//                return true; // ~ 400-7000 kbps
	//            // NOT AVAILABLE YET IN API LEVEL 7
	//            case NETWORK_TYPE_EHRPD:
	//                return true; // ~ 1-2 Mbps
	//            case NETWORK_TYPE_EVDO_B:
	//                return true; // ~ 5 Mbps
	//            case NETWORK_TYPE_HSPAP:
	//                return true; // ~ 10-20 Mbps
	//            case NETWORK_TYPE_IDEN:
	//                return false; // ~25 kbps 
	//            case NETWORK_TYPE_LTE:
	//                return true; // ~ 10+ Mbps
	//            // Unknown
	//            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
	//                return false; 
	//            default:
	//                return false;
	//            }
	//        }else{
	//            return false;
	//        }
	//    }
	/**

	 * IP转整型
	 * @param ip
	 * @return
	 */
	public static long ip2int(String ip) {
		String[] items = ip.split("\\.");
		return Long.valueOf(items[0]) << 24
				| Long.valueOf(items[1]) << 16
				| Long.valueOf(items[2]) << 8 | Long.valueOf(items[3]);
	}
	/**
	 * 整型转IP
	 * @param ipInt
	 * @return
	 */
	public static String int2ip(long ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}
	
	/**
	 * 判断wifi ap是否启用
	 * @param context
	 * @return
	 */
	public static boolean isWifiApEnabled(Context context) {
		try {
			WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			Method method = manager.getClass().getMethod("isWifiApEnabled");
			return (Boolean) method.invoke(manager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
