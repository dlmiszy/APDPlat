package com.apdplat.module.security.service.filter;

import com.apdplat.platform.util.FileUtils;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *IP地址访问限制
 * @author ysc
 */
public class IPAccessControler {
    protected static final Logger log = LoggerFactory.getLogger(IPAccessControler.class);
    private Collection<String> allow;
    private Collection<String> deny;

    public IPAccessControler() {
        reInit();
    }
    public final void reInit(){
        allow=FileUtils.getTextFileContent("/WEB-INF/ip/allow.txt");
        deny=FileUtils.getTextFileContent("/WEB-INF/ip/deny.txt");
    }

    public boolean deny(HttpServletRequest request){
        if(request==null){
            return false;
        }
        try{
            String ip = getIpAddr(request);
            if(ip==null){
                log.info("无法获取到访问者的IP");
                return true;
            }

            if (hasMatch(ip, deny)) {
                log.info("ip: "+ip+" 位于黑名单中");
                return true;
            }

            if (!allow.isEmpty() && !hasMatch(ip, allow)) {
                log.info("ip: "+ip+" 没有位于白名单中");
                return true;
            }
        }catch(Exception e){}
        return false;
    }

    private boolean hasMatch(String ip, Collection<String> regExps) {
        for (String regExp : regExps) {
            try{
                if (ip.matches(regExp)) {
                    return true;
                }
            }catch(Exception e){}
        }

        return false;
    }

    private String getIpAddr(HttpServletRequest request){       
        String ipString=null;
        String temp = request.getHeader("x-forwarded-for");
        if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
            ipString=temp;
        }
        if (StringUtils.isBlank(ipString) || "unknown".equalsIgnoreCase(ipString)) {
            temp = request.getHeader("Proxy-Client-IP");
            if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
                ipString=temp;
            }
        }
        if (StringUtils.isBlank(ipString) || "unknown".equalsIgnoreCase(ipString)) {
            temp = request.getHeader("WL-Proxy-Client-IP");
            if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
                ipString=temp;
            }
        }
        if (StringUtils.isBlank(ipString) || "unknown".equalsIgnoreCase(ipString)) {
            temp = request.getRemoteAddr();
            if(temp.indexOf(":")==-1 && temp.indexOf(".")!=-1){
                ipString=temp;
            }
        }

        // 多个路由时，取第一个非unknown的ip
        final String[] arr = ipString.split(",");
        for (final String str : arr) {
            if (!"unknown".equalsIgnoreCase(str) && str.indexOf(":")==-1 && str.split(".").length==4) {
                ipString = str;
                break;
            }
        }

        return ipString;
    }
    public static void main(String[] args){
        System.out.println("127.0.0.1".matches("127.0.*.*"));
    }
}