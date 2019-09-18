package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo){
        Index.Builder indexBuilder = new Index.Builder(skuLsInfo);
        indexBuilder.index("gmall_sku_info").type("SkuInfo").id(skuLsInfo.getId());
        Index index = indexBuilder.build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {

        String query="{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\"match\": {\n" +
                "          \"skuName\": \""+skuLsParams.getKeyword()+"\"\n" +
                "        }}\n" +
                "      ],\n" +
                "      \"filter\": [ \n" +
                "          {\"term\": {\n" +
                "            \"catalog3Id\": \"61\"\n" +
                "          }},\n" +
                "          {\"term\": {\n" +
                "            \"skuAttrValueList.valueId\": \"83\"\n" +
                "          }},\n" +
                "          {\"term\": {\n" +
                "            \"skuAttrValueList.valueId\": \"154\"\n" +
                "          }},\n" +
                "          \n" +
                "           {\"range\": {\n" +
                "            \"price\": {\"gte\": 3200}\n" +
                "           }}\n" +
                "\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"from\": 0\n" +
                "    , \"size\": 2\n" +
                "    , \"highlight\": {\"fields\": {\"skuName\": {\"pre_tags\": \"<span style='color:red' >\",\"post_tags\": \"</span>\"}}  }\n" +
                "  \n" +
                "    ,\n" +
                "    \"aggs\": {\n" +
                "      \"groupby_valueid\": {\n" +
                "        \"terms\": {\n" +
                "          \"field\": \"skuAttrValueList.valueId\",\n" +
                "          \"size\": 1000\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"sort\": [\n" +
                "      {\n" +
                "        \"hotScore\": {\n" +
                "          \"order\": \"desc\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "}";
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParams.getKeyword()));
        //三级分类过滤
        boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id()));
        //平台属性过滤
        String[] valueIds = skuLsParams.getValueId();
        for (int i = 0; i<valueIds.length;i++){
            String valueId = valueIds[i];
            boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId",valueId));
        }
        //价格
        //boolQueryBuilder.filter(new RangeQueryBuilder("price").gte("3200"));

        searchSourceBuilder.query(boolQueryBuilder);

        //起始行
        searchSourceBuilder.from((skuLsParams.getPageNo()-1)*skuLsParams.getPageSize());
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //高亮
        searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>" ).postTags("</span>"));
        //聚合
        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_value_id").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);


        System.out.println(searchSourceBuilder.toString());

        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());
        Search search = searchBuilder.addIndex("gmall_sku_info").addType("SkuInfo").build();
        SkuLsResult skuLsResult = new SkuLsResult();
        try {
            SearchResult searchResult = jestClient.execute(search);

            List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;
                skuLsInfoList.add(skuLsInfo);
            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);
            //总数
            searchResult.getTotal();
            //总页数
            long totalPage= (searchResult.getTotal() + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();
            skuLsResult.setTotalPages(totalPage);

            //聚合部分   商品设计的平台属性
            List<String> attrValueIdList = new ArrayList<>();
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_value_id").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return skuLsResult;
    }
}
















