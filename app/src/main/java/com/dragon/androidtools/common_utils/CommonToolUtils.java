package com.dragon.androidtools.common_utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/29 0029.
 */

public class CommonToolUtils {
    /**
     * 对一个包含字符的集合的字符串进行排序
     * @param collection java所有集合实现的一个接口
     * @return 一个list集合
     */
    public static List<String> sortSetKey(Collection<String> collection){
        if(collection!=null){
            ArrayList<String>list=new ArrayList<>();
            list.addAll(collection);
            Collections.sort(list, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });

        }
        return null;
    }

    /**
     * 拼接网络参数
     * @param list 参数集合
     * @param map 参数的键值对
     * @return 拼接好的字符串
     */
    public static String jointNetParam(List<String>list, Map<String,String>map){

        if(null!=list&&list.size()>0){
            String jointStr="";
            for(String str:list){
                jointStr=jointStr+str+"="+map.get(str)+"&";
            }
            return jointStr.substring(0,jointStr.length()-1);
        }


        return null;
    }


}
