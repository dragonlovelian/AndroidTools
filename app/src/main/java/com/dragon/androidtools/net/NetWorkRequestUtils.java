package com.dragon.androidtools.net;

import android.text.TextUtils;

import com.dragon.androidtools.common_utils.CommonToolUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Administrator on 2017/9/29 0029.
 */

public class NetWorkRequestUtils {

    /**
     *  网络post请求 主要用于json
     * @param url
     * @param map
     * @return 请求的数据
     */
    public static String postRequest(String url, Map<String,String> map){
        Map<String,String> temp=map;
        Set<String>entrySet= map.keySet();
        List<String>paramList= CommonToolUtils.sortSetKey(entrySet);
        String paramStr="";
        if(null!=paramList&&paramList.size()>0){
            paramStr=CommonToolUtils.jointNetParam(paramList,temp);
        }

        HttpURLConnection connection;
        URL tempUrl;
        try{
            tempUrl=new URL(url);
            connection=(HttpURLConnection) tempUrl.openConnection();
            connection.setReadTimeout(8000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream os=connection.getOutputStream();
            os.write(paramStr.getBytes());

            int resultCode=connection.getResponseCode();
            if(HttpURLConnection.HTTP_OK==resultCode){
                StringBuffer strBuffer=new StringBuffer();
                String readLine;
                BufferedReader responseReader=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                while(null!=(readLine = responseReader.readLine())&&!TextUtils.isEmpty(readLine)){
                    strBuffer.append(readLine);
                }
                responseReader.close();
                return strBuffer.toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return "";
    }

}
