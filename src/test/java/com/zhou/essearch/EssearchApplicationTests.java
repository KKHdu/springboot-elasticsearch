package com.zhou.essearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.zhou.essearch.document.ProductDocument;
import com.zhou.essearch.document.ProductDocumentBuilder;
import com.zhou.essearch.page.Page;
import com.zhou.essearch.service.EsSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EssearchApplicationTests {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EsSearchService esSearchService;

    @Test
    public void save() {
        // log.info("【创建索引前的数据条数】：{}",esSearchService.getAll().size());
        String str = "[{'id':5,'productName':'天下读物2','productDesc':'我们让搜索稍微再变的复杂一些。'}," +
                "{'id':5,'productName':'历史探索2','productDesc':'我们依旧想要找到姓氏为“Smith”的员工'}," +
                "{'id':5,'productName':'人文读物2','productDesc':'我们的语句将添加过滤器(filter)'}]";
        JSONArray jsonArray = JSONArray.parseArray(str);
        int num = esSearchService.getAll().size();
//        System.out.println(jsonArray);
//        System.out.println(num);
//        System.out.println(jsonArray.getJSONObject(0).get("productName").toString());
        for(int i = 0;i < jsonArray.size();i++){
            ProductDocument productDocument = ProductDocumentBuilder.create()
                    .addId(System.currentTimeMillis() + ""+ (num+i) +"")
                    .addProductName(jsonArray.getJSONObject(i).get("productName").toString())
                    .addProductDesc(jsonArray.getJSONObject(i).get("productDesc").toString())
                    .addCreateTime(new Date()).addUpdateTime(new Date())
                    .builder();

            esSearchService.save(productDocument);
        }

       /**
        ProductDocument productDocument = ProductDocumentBuilder.create()
                .addId(System.currentTimeMillis() + "01")
                .addProductName("无印良品 MUJI 基础润肤化妆水")
                .addProductDesc("无印良品 MUJI 基础润肤化妆水 高保湿型 200ml")
                .addCreateTime(new Date()).addUpdateTime(new Date())
                .builder();

        ProductDocument productDocument1 = ProductDocumentBuilder.create()
                .addId(System.currentTimeMillis() + "02")
                .addProductName("荣耀 V10 尊享版")
                .addProductDesc("荣耀 V10 尊享版 6GB+128GB 幻夜黑 移动联通电信4G全面屏游戏手机 双卡双待")
                .addCreateTime(new Date()).addUpdateTime(new Date())
                .builder();

        ProductDocument productDocument2 = ProductDocumentBuilder.create()
                .addId(System.currentTimeMillis() + "03")
                .addProductName("资生堂(SHISEIDO) 尿素红罐护手霜")
                .addProductDesc("日本进口 资生堂(SHISEIDO) 尿素红罐护手霜 100g/罐 男女通用 深层滋养 改善粗糙")
                .addCreateTime(new Date()).addUpdateTime(new Date())
                .builder();


        esSearchService.save(productDocument,productDocument1,productDocument2);
        **/

//        log.info("【创建索引ID】:{},{},{}",productDocument.getId(),productDocument1.getId(),productDocument2.getId());
//        log.info("【创建索引后的数据条数】：{}",esSearchService.getAll().size());
    }

    @Test
    public void getAll(){
        esSearchService.getAll().parallelStream()
                .map(JSON::toJSONString)
                .forEach(System.out::println);
    }

    @Test
    public void deleteAll() {
        esSearchService.deleteAll();
    }

    @Test
    public void delete() {
        esSearchService.delete("F9kB92oBrEm2GCHywZal");
    }

    @Test
    public void getById() {
        log.info("【根据ID查询内容】：{}", JSON.toJSONString(esSearchService.getById("155868876457201")));
    }

    // 解析伪SQL
    // 目前只涉及-> :and :or :remove
    @Test
    public void pieceQuery() {
        String str = "#98260 = 22 :and ( #55678 < 37 :or ( #55678 > 44 :and #98442 <= 1 ) ) :or #1778 = 1";
        String str1 = "#98260 = 22 :and ( #55678 < 37 :or ( #55678 > 44 :and #98442 <= 1 ) )";
        String str2 = "#1111 = 22 :and #2222 < 37 :or #3333 > 44";

        log.info("【全局搜索内容】：{}", JSON.toJSONString(esSearchService.pieceQuery(str, ProductDocument.class)));
    }
    // ---------------------------------

    @Test
    public void query() {
        log.info("【全局搜索内容】：{}", JSON.toJSONString(esSearchService.query("荣耀", ProductDocument.class)));
    }

    @Test
    public void query2() {
        log.info("【单字段匹配单关键词查询】：{}", JSON.toJSONString(esSearchService.query2("天下读物2", ProductDocument.class)));
    }

    @Test
    public void query3() {
        log.info("【多字段各自匹配单关键词查询】：{}", JSON.toJSONString(esSearchService.query3("荣耀，1558688764572", ProductDocument.class)));
    }

    @Test
    public void query4() {
        log.info("【根据关键词范围查询】：{}", JSON.toJSONString(esSearchService.query4("1558688764572", ProductDocument.class)));
    }

    @Test
    public void queryHit() {

        String keyword = "联通尿素";
        String indexName = "orders";

        List<Map<String,Object>> searchHits = esSearchService.queryHit(keyword,indexName,"productName","productDesc");
        log.info("【根据关键字搜索内容，命中部分高亮，返回内容】：{}", JSON.toJSONString(searchHits));
        //[{"highlight":{"productDesc":"<span style='color:red'>无印良品</span> MUJI 基础润肤化妆水 高保湿型 200ml","productName":"<span style='color:red'>无印良品</span> MUJI 基础润肤化妆水"},"source":{"productDesc":"无印良品 MUJI 基础润肤化妆水 高保湿型 200ml","createTime":1544755966204,"updateTime":1544755966204,"id":"154475596620401","productName":"无印良品 MUJI 基础润肤化妆水"}},{"highlight":{"productDesc":"<span style='color:red'>荣耀</span> V10 尊享版 6GB+128GB 幻夜黑 移动联通电信4G全面屏游戏手机 双卡双待","productName":"<span style='color:red'>荣耀</span> V10 尊享版"},"source":{"productDesc":"荣耀 V10 尊享版 6GB+128GB 幻夜黑 移动联通电信4G全面屏游戏手机 双卡双待","createTime":1544755966204,"updateTime":1544755966204,"id":"154475596620402","productName":"荣耀 V10 尊享版"}}]
    }

    @Test
    public void queryHitByPage() {

        String keyword = "联通尿素";
        String indexName = "orders";

        Page<Map<String,Object>> searchHits = esSearchService.queryHitByPage(1,1,keyword,indexName,"productName","productDesc");
        log.info("【分页查询，根据关键字搜索内容，命中部分高亮，返回内容】：{}", JSON.toJSONString(searchHits));
        //[{"highlight":{"productDesc":"<span style='color:red'>无印良品</span> MUJI 基础润肤化妆水 高保湿型 200ml","productName":"<span style='color:red'>无印良品</span> MUJI 基础润肤化妆水"},"source":{"productDesc":"无印良品 MUJI 基础润肤化妆水 高保湿型 200ml","createTime":1544755966204,"updateTime":1544755966204,"id":"154475596620401","productName":"无印良品 MUJI 基础润肤化妆水"}},{"highlight":{"productDesc":"<span style='color:red'>荣耀</span> V10 尊享版 6GB+128GB 幻夜黑 移动联通电信4G全面屏游戏手机 双卡双待","productName":"<span style='color:red'>荣耀</span> V10 尊享版"},"source":{"productDesc":"荣耀 V10 尊享版 6GB+128GB 幻夜黑 移动联通电信4G全面屏游戏手机 双卡双待","createTime":1544755966204,"updateTime":1544755966204,"id":"154475596620402","productName":"荣耀 V10 尊享版"}}]
    }

    @Test
    public void deleteIndex() {
        log.info("【删除索引库】");
        esSearchService.deleteIndex("orders");
    }

}

