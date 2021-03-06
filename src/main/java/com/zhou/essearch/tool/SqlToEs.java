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

    /*
    * 通过匹配括号 拆分和组合小范围的对应关系
    * 其中 :in 操作单独判断
    * */
    public static List dyForList(List strListOri){
        //判断是否含 :in 操作符
        List strList = SqlToEs.inAbout(strListOri);
        // System.out.println("判断:in后形成的数组---------------->");
        // System.out.println(strList);

        List rk = new ArrayList();
        // System.out.println(strList.size());
        for (int i=0;i<strList.size();i++){
            if(")".equals(strList.get(i).toString())){
                // System.out.println("i:"+i);
                for (int j=i;j>=0;j--){
                    if ("(".equals(strList.get(j).toString())){
                        // 把左括号 存入 数组rk
                        // 得到 rk 是 最小关系体
                        rk.add(strList.get(j));
                        strList.set(j," ");
                        // ---> 调正 rk 的前后顺序 <---
                        List rkNor = new ArrayList();
                        for (int z=rk.size()-1;z>=0;z--){
                            rkNor.add(rk.get(z));
                        }
                        // 调用方法 取最小括号里面（暨最小关系体）的query语句
                        // true：表示 数组带括号 的请求
                        QueryBuilder queryBuilder = buider(rkNor,true);
                        // 用 query语句替换这个位置的value  更新strList
                        strList.set(j,queryBuilder);
                        break;
                    }else{
                        rk.add(strList.get(j)); // 把括号中的内容 存入 数组rk
                        strList.remove(j); // 空格替换 原先数组的 内容
                    }
                }
                // 一次括号结束，用新的strList再次调用自己，直到不再有括号
                dyForList(strList);
            }
        }

        return strList;
    }

    /*
    * 匹配符：  :in  =  >  >=  <  <=
    * 目的：构建最小 关系语句
    * 注意：boolean x
    * */
    public static QueryBuilder buider(List rk, boolean x){
        // true：括号拼装  /  false：简单拼装
        int numL = rk.size()-1;
        int numS = 0;
        if (x){
            // 因为数组中带括号，所以相比简单拼装，前后各多一个括号
            numL = rk.size()-2;
            numS = 1;
        }

        // 初始化 queryBuilder 存放数组
        List<QueryBuilder> queryList = new ArrayList<QueryBuilder>();
        // 初始化queryBuilder
        QueryBuilder queryBuilder;
        // 初始化操作符存放数组，用于判断 相邻关系体
        List act = new ArrayList();

        // 循环判断匹配符，并build局部query语句
        // i-- 倒序
        for(int i=numL;i>=numS;i--){
            String act_p = rk.get(i).toString();

            // 过滤换行、回车、空格、制表符
            String newString = act_p.replaceAll("(\r\n|\r|\n|\n\r)", "");
            //正则表达式 取 { 以后的 内容 进行操作
            String regEx = "\\{.*";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(newString);
            boolean rs = matcher.matches();

            // 如果匹配到 query语句，则将其直接写入 queryList 数组
            if (rs){
                // System.out.println("匹配到query语句,直接写入>_<");
                queryList.add((QueryBuilder) rk.get(i));
            }else if (":and".equals(act_p) || ":or".equals(act_p) || ":remove".equals(act_p)) {
                // 操作符写入数组
                act.add(act_p);
            } else {
                if (":in".equals(act_p) || "=".equals(act_p) || ">=".equals(act_p) || "<=".equals(act_p) || ">".equals(act_p) || "<".equals(act_p)) {

                    //20190703 做了顺序调整 //20190625 判断符号的后一位（字段） 由于数组是反的，所以注意判断
                    String act_p_last = rk.get(i - 1).toString();
                    String act_p_next = rk.get(i + 1).toString();

                    if (":in".equals(act_p)){
                        // 把:in 匹配的内容 字符串转成数组 再构建 terms
                        List inTermsList = inTerms(act_p_next);
                        queryBuilder = QueryBuilders.termsQuery(act_p_last, inTermsList);
                        queryList.add(queryBuilder);
                    } else if ("=".equals(act_p)) {
                        queryBuilder = QueryBuilders.termQuery(act_p_last, act_p_next);
                        // 单个queryBuilder写入数组
                        queryList.add(queryBuilder);
                    } else if ("<=".equals(act_p)) {
                        queryBuilder = QueryBuilders.rangeQuery(act_p_last).lte(act_p_next);
                        queryList.add(queryBuilder);
                    } else if (">=".equals(act_p)) {
                        queryBuilder = QueryBuilders.rangeQuery(act_p_last).gte(act_p_next);
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

        // System.out.println(queryList);
        // System.out.println(act);
        //循环完成后，queryList数组中只剩下 操作符关系   倒序
        // act数组中存放 操作符（:and :or :remove）      倒序
        // 调用方法，拿到一个query语
        QueryBuilder reQueryList = toQueryBuild(queryList,act);

        return reQueryList;
    }

    /*
     * 判断： :and :or :remove 操作符
     * 目的：构建query语句
     * queryList2:关系体   act:操作符
     * 两者都是倒序
     * */
    public static QueryBuilder toQueryBuild(List<QueryBuilder> queryListUn, List act){
        List queryList = new ArrayList();
        //queryList2 赋值给 queryList（ArrayList才能被编辑）
        for (int i=0;i<queryListUn.size();i++){
            queryList.add(queryListUn.get(i));
        }

        // 初始化queryBuilder
        QueryBuilder queryBuilder;

        for (int i=act.size()-1;i>=0;i--){
            // System.out.println("act数组："+act);
            // System.out.println("query的长度："+queryList.size());
            // System.out.println("query数组："+queryList);
            if (":and".equals(act.get(i))){
                queryBuilder = QueryBuilders.boolQuery().must((QueryBuilder) queryList.get(i+1)).must((QueryBuilder) queryList.get(i));
                // 更新局部query数组 queryList(改1，删1)
                queryList.set(i,queryBuilder);
                queryList.set(i+1,"");
            }else if(":or".equals(act.get(i))){
                queryBuilder = QueryBuilders.boolQuery().should((QueryBuilder) queryList.get(i+1)).should((QueryBuilder) queryList.get(i));
                // 更新局部query数组 queryList(改1，删1)
                queryList.set(i,queryBuilder);
                queryList.set(i+1,"");
            }else if(":remove".equals(act.get(i))){
                queryBuilder = QueryBuilders.boolQuery().must((QueryBuilder) queryList.get(i+1)).mustNot((QueryBuilder) queryList.get(i));
                // 更新局部query数组 queryList(改1，删1)
                queryList.set(i,queryBuilder);
                queryList.set(i+1,"");
            }
        }
        // 现在 queryList 里面组合完毕，并且只有一条数据
        // System.out.println("==========================");
        // System.out.println(queryList.get(queryList.size()-1).toString());
        return (QueryBuilder) queryList.get(0);
    }


    /* 简单处理 把 :in 括号中的内容当成一个整体
    * 暨 删除 :in 的两个括号
    * 这里认为 :in 中不允许包含任何操作符
    * */
    public static List inAbout(List strList){
        for (int i=0;i<strList.size();i++){
            if(":in".equals(strList.get(i).toString())){
                for(int j=i;j<strList.size();j++){
                    if("(".equals(strList.get(j).toString())){
                        strList.remove(j);
                    }else if (")".equals(strList.get(j).toString())){
                        strList.remove(j);
                        break;
                    }
                }
            }
        }
        return strList;
    }





    /* 整合 原伪sql -> 规则形式
    * 包含 ：(  )  <  >  <=  >=  =
    * 操作 ：对这些符号的前一位和后一位插入一个空格符
    * 目的：为字符串转数组提供判断条件
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

        // 通过 空格 对字符串进行拆分，作为List数组的元素
        List strList2 = Arrays.asList(fixStr.split("\\s+"));

        // strList2 赋给 strList   （Array才能编辑）
        List strList = new ArrayList();
        for(int i=0;i<strList2.size();i++){
            strList.add(strList2.get(i));
        }

        return strList;
    }

    /* 整合 :in 中内容 -> List
     * */
    public static List inTerms(String inStr){
        List inTermsList = Arrays.asList(inStr.split(","));
//        System.out.println("||||||||||||||||||||");
//        System.out.println(inTermsList);
        return inTermsList;
    }
}
