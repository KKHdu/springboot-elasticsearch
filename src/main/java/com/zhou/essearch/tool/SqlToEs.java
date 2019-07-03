package com.zhou.essearch.tool;

import com.zhou.essearch.service.impl.BaseSearchServiceImpl;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlToEs extends BaseSearchServiceImpl {

    public static List dyForList(List strList){
        List rk = new ArrayList();
        // System.out.println(strList.size());
        for (int i=0;i<strList.size();i++){
            if(")".equals(strList.get(i).toString())){
                // System.out.println("i:"+i);
                for (int j=i;j>=0;j--){
                    if ("(".equals(strList.get(j).toString())){
                        // System.out.println("j:"+j);
                        rk.add(strList.get(j)); // 把左括号 存入 数组rk
                        strList.set(j," ");
                        // 调正 rk 的前后顺序
                        List rkNor = new ArrayList();
                        for (int z=rk.size()-1;z>=0;z--){
                            rkNor.add(rk.get(z));
                        }

                        QueryBuilder queryBuilder = buider(rkNor,true); // 调用方法 拿到最小括号里面的query语句
                        strList.set(j,queryBuilder); // 用 query语句替换这个位置的value  更新strList
                        break;
                    }else{
                        rk.add(strList.get(j)); // 把括号中的内容 存入 数组rk
                        strList.remove(j); // 空格替换 原先数组的 内容
                    }
                }
                dyForList(strList); // 一次括号结束，用新的strList再次调用自己
            }

        }

        // strList 当前顺序是倒序 编辑成正常顺序

        return strList;
    }

    public static QueryBuilder toQueryBuild(List<QueryBuilder> queryList2, List act){
        List queryList = new ArrayList();
        for (int i=0;i<queryList2.size();i++){  //queryList2 赋值给 queryList
            queryList.add(queryList2.get(i));
        }
        QueryBuilder queryBuilder; // 初始化queryBuilder

        // System.out.println("act数组："+act);
        for (int i=act.size()-1;i>=0;i--){
//            System.out.println("act数组："+act);
//            System.out.println("query的长度："+queryList.size());
//            System.out.println("query数组："+queryList);
            if (":and".equals(act.get(i))){
                queryBuilder = QueryBuilders.boolQuery().must((QueryBuilder) queryList.get(i+1)).must((QueryBuilder) queryList.get(i));
                queryList.set(i,queryBuilder);// 更新局部query数组 queryList(改1，删1)
                queryList.set(i+1,"");
            }else if(":or".equals(act.get(i))){
                queryBuilder = QueryBuilders.boolQuery().should((QueryBuilder) queryList.get(i+1)).should((QueryBuilder) queryList.get(i));
                queryList.set(i,queryBuilder);// 更新局部query数组 queryList(改1，删1)
                queryList.set(i+1,"");
            }else if(":remove".equals(act.get(i))){
                queryBuilder = QueryBuilders.boolQuery().must((QueryBuilder) queryList.get(i+1)).mustNot((QueryBuilder) queryList.get(i));
                queryList.set(i,queryBuilder);// 更新局部query数组 queryList(改1，删1)
                queryList.set(i+1,"");
            }
        }
        // 现在 queryList 里面组合完毕，并且只有一条数据
//        System.out.println("==========================");
//        System.out.println(queryList.get(queryList.size()-1).toString());
        return (QueryBuilder) queryList.get(0);
    }

    public static QueryBuilder buider(List rk, boolean x){
        // 判断 简单拼装 / 括号拼装 ;     true：括号拼装
        int numL = rk.size()-1;
        int numS = 0;
        if (x){
            numL = rk.size()-2;
            numS = 1;
        }

        List<QueryBuilder> queryList = new ArrayList<QueryBuilder>(); // 初始化 queryBuilder 存放数组
        QueryBuilder queryBuilder; // 初始化queryBuilder

        List act = new ArrayList(); // 初始化操作符存放数组

        for(int i=numL;i>=numS;i--){ // 循环判断匹配符，并build局部query语句
            String act_p = rk.get(i).toString();
            String newString = act_p.replaceAll("(\r\n|\r|\n|\n\r)", ""); // 过滤换行、回车、空格、制表符

//            System.out.println("m+++++++++++++++++m");
//            System.out.println(newString);
//            System.out.println("m-----------------m");

            //正则表达式 取 { 以后的 内容 进行操作
            String regEx = "\\{.*";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(newString);
            boolean rs = matcher.matches();

            if (rs){
                System.out.println("匹配到query语句,直接写入>_<");
                queryList.add((QueryBuilder) rk.get(i));
            }else if (":and".equals(act_p) || ":or".equals(act_p) || ":remove".equals(act_p)) {
                act.add(act_p); // 操作符写入数组
            } else {
                if ("=".equals(act_p) || ">=".equals(act_p) || "<=".equals(act_p) || ">".equals(act_p) || "<".equals(act_p)) {

                    String act_p_last = rk.get(i + 1).toString(); //判断符号的后一位（字段） 由于数组是反的，所以注意判断
                    String act_p_next = rk.get(i - 1).toString(); // 判断符号的前一位（值）

                    if ("=".equals(act_p)) {
                            queryBuilder = QueryBuilders.termQuery(act_p_next, act_p_last);
                        queryList.add(queryBuilder);// 单个queryBuilder写入数组
                    } else if ("<=".equals(act_p)) {
                        queryBuilder = QueryBuilders.rangeQuery(act_p_last).gte(act_p_next);
                        queryList.add(queryBuilder);
                    } else if (">=".equals(act_p)) {
                        queryBuilder = QueryBuilders.rangeQuery(act_p_last).lte(act_p_next);
                        queryList.add(queryBuilder);
                    } else if (">".equals(act_p)) {
                        queryBuilder = QueryBuilders.rangeQuery(act_p_last).gt(act_p_next);
                        queryList.add(queryBuilder);
                    } else if ("<".equals(act_p)) {
                        queryBuilder = QueryBuilders.rangeQuery(act_p_last).lt(act_p_next);
                        queryList.add(queryBuilder);
                    }
                }
            }
        }

//        System.out.println("1234567890");
//        System.out.println(queryList);
//        System.out.println(act);
        QueryBuilder reQueryList = toQueryBuild(queryList,act); // 调用方法，拿到一个query语

        return reQueryList;
    }

    /* 整合 原伪sql -> 规则形式
    * 包含 (  )  <  >  <=  >=  =
    * */
    public static List regular(String str){
        String fixStr = "";
        for (int i=0;i<str.length();i++){
            char ch = str.charAt(i);
            String strFch = String.valueOf(ch);
            if ("(".equals(strFch) || ")".equals(strFch)){
                fixStr += " " + ch + " ";
            }else if ("<".equals(strFch) ||">".equals(strFch)){
                char chNext = str.charAt(i+1);
                String strFchNext = String.valueOf(chNext);
                fixStr += " " + ch;
                if (!"=".equals(strFchNext)){
                    fixStr += " ";
                }
            }else if ("=".equals(strFch)){
                char chLast = str.charAt(i-1);
                String strFchLast = String.valueOf(chLast);
                if (!"<".equals(strFchLast) && !">".equals(strFchLast)){
                    fixStr += " ";
                }
                fixStr += ch + " ";
            }else{
                fixStr += ch;
            }
        }
        List strList2 = Arrays.asList(fixStr.split("\\s+"));

        return strList2;
    }

}
