package com.zhou.essearch.service.impl;

import com.zhou.essearch.page.Page;
import com.zhou.essearch.service.BaseSearchService;
import com.zhou.essearch.tool.SqlToEs;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * elasticsearch 搜索引擎
 * @author zhoudong
 * @version 0.1
 * @date 2018/12/13 15:33
 */
@Service
public class BaseSearchServiceImpl<T> implements BaseSearchService<T> {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;


    @Override
    public List<T> pieceQuery(String strSQL, Class<T> clazz){
        // 整合 strSQL 并 通过 List存储
        List strList = SqlToEs.regular(strSQL);

        // strList2 赋给 strList   （Array才能编辑） // 为什么不直接ArrayList 灵魂拷问??? 20190702
//        List strList = new ArrayList();
//        for(int i=0;i<strList2.size();i++){
//            strList.add(strList2.get(i));
//        }

        /**/
        //匹配括号，拼装query语句
        List nextList = SqlToEs.dyForList(strList);
        System.out.println("最后形成的数组---------------->");
        System.out.println(nextList);

        // 匹配括号结束，简单语句拼装query语句
        QueryBuilder finList = SqlToEs.buider(nextList,false);
        System.out.println("最后形成的query语句---------------->");
        System.out.println(finList);
        /**/
//        Stack stack = new Stack();  // 建栈
//        stack.push(strList.get(0)); // 第一个元素进栈
//        for (int i=1;i<strList.size();i++){
//            String c1 = stack.peek().toString(); // 栈内元素
//            String c2 = strList.get(i).toString(); // List数组元素
//            if (")".equals(c2)){
//
//                for (int j=0;j<=i;j++){
//                    if ("(".equals(stack.peek().toString())){
//                        stack.pop(); // 出栈左括号
//                        break;  //本轮出栈结束
//                    }
//                    String rList = stack.pop().toString(); // 循环出栈
//                }
//            }else {
//                stack.push(strList.get(i)); // 入栈
//            }
//        }

//        int left = 0; // 定位 左右括号
//        int right =0;
//        for (int i=0;i<str.size();i++){
//            if ("(".equals(str.get(i))){
//                left = i;
//            }
//        }
//        for (int i=str.size();i>=0;i--){
//            if (")".equals(str.get(i))){
//                right = i;
//            }
//        }

//        QueryBuilder queryBuilder1 = createQueryBuilder("A","productName");
//        QueryBuilder queryBuilder2 = createQueryBuilder("B","productDec");
//        QueryBuilder queryBuilder3 = createQueryBuilder("C","createTime");
//        QueryBuilder queryBuilder4 = createQueryBuilder("D","updateTime");
//        //and链接两个查询条件用must()，or的话使用should()，remove的话使用mustNot()。
//        QueryBuilder queryBuilder11 = QueryBuilders.boolQuery().must(queryBuilder3).mustNot(queryBuilder4);
//        QueryBuilder queryBuilder22 = QueryBuilders.boolQuery().should(queryBuilder2).should(queryBuilder11);
//        // QueryBuilder queryBuilder33 = QueryBuilders.boolQuery().must(queryBuilder1).mustNot(queryBuilder2);
//        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(queryBuilder1).must(queryBuilder22);
//
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(queryBuilder) // 多字段各自匹配关键词查询
//                // .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
//                // .withSort(new FieldSortBuilder("productName").order(SortOrder.DESC))
//                .build();
//        System.out.println("查询的语句:" + searchQuery.getQuery().toString());

        return null;
    }

// -------------------------query---拼装---END---------------------------------




    @Override
    public List<T> query(String keyword, Class<T> clazz) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(new QueryStringQueryBuilder(keyword)) // 全局匹配查询
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                // .withSort(new FieldSortBuilder("productName").order(SortOrder.DESC))
                .build();
        System.out.println("查询的语句:" + searchQuery.getQuery().toString());
        return elasticsearchTemplate.queryForList(searchQuery,clazz);
    }

    @Override
    public List<T> query2(String keyword, Class<T> clazz) {
        QueryBuilder queryBuilder = createQueryBuilder(keyword,"productName.keyword"); // 关键词匹配

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder) // 单字段匹配关键词查询
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                // .withSort(new FieldSortBuilder("productName").order(SortOrder.DESC))
                .build();
        System.out.println("查询的语句:" + searchQuery.getQuery().toString());
        return elasticsearchTemplate.queryForList(searchQuery,clazz);
    }

    @Override
    public List<T> query3(String keywords, Class<T> clazz) {
        List keyword = Arrays.asList(keywords.split("，"));
        // 关键词匹配
        QueryBuilder queryBuilder1 = createQueryBuilder(keyword.get(0).toString(),"productName");
        QueryBuilder queryBuilder2 = createQueryBuilder(keyword.get(1).toString(),"createTime");
        //and链接两个查询条件用must()，or的话使用should()，remove的话使用mustNot()。
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(queryBuilder1).mustNot(queryBuilder2);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder) // 多字段各自匹配关键词查询
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                // .withSort(new FieldSortBuilder("productName").order(SortOrder.DESC))
                .build();
        System.out.println("查询的语句:" + searchQuery.getQuery().toString());
        return elasticsearchTemplate.queryForList(searchQuery,clazz);
    }

    @Override
    public List<T> query4(String keyword, Class<T> clazz) {
        // List keywords = Arrays.asList(keyword.split("，"));
        // 关键词范围定位
        // 大于-> gt  大于等于->gte  小于->lt  小于等于->lte
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("createTime").gt(keyword);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                // .withSort(new FieldSortBuilder("productName").order(SortOrder.DESC))
                .build();
        System.out.println("查询的语句:" + searchQuery.getQuery().toString());
        return elasticsearchTemplate.queryForList(searchQuery,clazz);
    }

    /**
     * 高亮显示
     * @auther: zhoudong
     * @date: 2018/12/13 21:22
     */
    @Override
    public  List<Map<String,Object>> queryHit(String keyword,String indexName,String ... fieldNames) {
        // 构造查询条件,使用标准分词器.
        QueryBuilder matchQuery = createQueryBuilder(keyword,fieldNames);

        // 设置高亮,使用默认的highlighter高亮器
        HighlightBuilder highlightBuilder = createHighlightBuilder(fieldNames);

        // 设置查询字段
        SearchResponse response = elasticsearchTemplate.getClient().prepareSearch(indexName)
                .setQuery(matchQuery)
                .highlighter(highlightBuilder)
                .setSize(10000) // 设置一次返回的文档数量，最大值：10000
                .get();

        // 返回搜索结果
        SearchHits hits = response.getHits();

        return getHitList(hits);
    }
    /**
     * 高亮显示，返回分页
     * @auther: zhoudong
     * @date: 2018/12/18 10:29
     */
    @Override
    public Page<Map<String, Object>> queryHitByPage(int pageNo, int pageSize, String keyword, String indexName, String... fieldNames) {
        // 构造查询条件,使用标准分词器.
        QueryBuilder matchQuery = createQueryBuilder(keyword,fieldNames);

        // 设置高亮,使用默认的highlighter高亮器
        HighlightBuilder highlightBuilder = createHighlightBuilder(fieldNames);

        // 设置查询字段
        SearchResponse response = elasticsearchTemplate.getClient().prepareSearch(indexName)
                .setQuery(matchQuery)
                .highlighter(highlightBuilder)
                .setFrom((pageNo-1) * pageSize)
                .setSize(pageNo * pageSize) // 设置一次返回的文档数量，最大值：10000
                .get();


        // 返回搜索结果
        SearchHits hits = response.getHits();

        Long totalCount = hits.getTotalHits();
        Page<Map<String, Object>> page = new Page<>(pageNo,pageSize,totalCount.intValue());
        page.setList(getHitList(hits));
        return page;
    }

    /**
     * 构造查询条件
     * @auther: zhoudong
     * @date: 2018/12/18 10:42
     */
    public static QueryBuilder createQueryBuilder(String keyword, String... fieldNames){
        // 构造查询条件,使用标准分词器.
        return QueryBuilders.multiMatchQuery(keyword,fieldNames)   // matchQuery(),单字段搜索
                // .analyzer("ik_max_word")
                .operator(Operator.OR);
    }
    /**
     * 构造高亮器
     * @auther: zhoudong
     * @date: 2018/12/18 10:44
     */
    private HighlightBuilder createHighlightBuilder(String... fieldNames){
        // 设置高亮,使用默认的highlighter高亮器
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                // .field("productName")
                .preTags("<span style='color:red'>")
                .postTags("</span>");

        // 设置高亮字段
        for (String fieldName: fieldNames) highlightBuilder.field(fieldName);

        return highlightBuilder;
    }

    /**
     * 处理高亮结果
     * @auther: zhoudong
     * @date: 2018/12/18 10:48
     */
    private List<Map<String,Object>> getHitList(SearchHits hits){
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map;
        for(SearchHit searchHit : hits){
            map = new HashMap<>();
            // 处理源数据
            map.put("source",searchHit.getSourceAsMap());
            // 处理高亮数据
            Map<String,Object> hitMap = new HashMap<>();
            searchHit.getHighlightFields().forEach((k,v) -> {
                String hight = "";
                for(Text text : v.getFragments()) hight += text.string();
                hitMap.put(v.getName(),hight);
            });
            map.put("highlight",hitMap);
            list.add(map);
        }
        return list;
    }

    @Override
    public void deleteIndex(String indexName) {
        elasticsearchTemplate.deleteIndex(indexName);
    }
}
