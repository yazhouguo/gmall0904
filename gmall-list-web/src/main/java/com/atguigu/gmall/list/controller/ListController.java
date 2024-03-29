package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @GetMapping("list.html")
    public String list(SkuLsParams skuLsParams, Model model){

        // 设置每页显示的条数
        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
        model.addAttribute("skuLsResult",skuLsResult);
        //得到平台属性清单
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        model.addAttribute("attrList",attrList);

        String paramUrl = makeParamUrl(skuLsParams);

        //已选择的平台属性值信息
        ArrayList<BaseAttrValue> selectedValueList = new ArrayList<>();

        //把所有已经选择的属性值 从属性+属性值清单中删除
       //清单 attrList  已选择的属性值  skuLsParams.getAttrValueIds

        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];
                        if (baseAttrValue.getId().equals(selectedValueId)) {
                            iterator.remove();//1.删除属性行
                            //2.添加到已选择列表
                            String selectedParamUrl = makeParamUrl(skuLsParams, selectedValueId);
                            baseAttrValue.setParamUrl(selectedParamUrl);
                            selectedValueList.add(baseAttrValue);
                        }
                    }
                }
            }
        }



        model.addAttribute("totalPages", skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());




        model.addAttribute("paramUrl",paramUrl);

        model.addAttribute("selectedValueList",selectedValueList);

        model.addAttribute("keyword",skuLsParams.getKeyword());

        return "list";
    }

    /**
     * 把页面传入的参数对象 转换成为参数url
     * @param skuLsParams
     * @return
     */
    public String makeParamUrl(SkuLsParams skuLsParams,String... excludeValueId){

        String paramUrl="";
        if( skuLsParams.getKeyword()!=null){
            paramUrl+="keyword="+skuLsParams.getKeyword();
        }
        else if(skuLsParams.getCatalog3Id()!=null){
            paramUrl+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        if(skuLsParams.getValueId()!=null &&skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if(excludeValueId!=null&&excludeValueId.length>0){ //需要排除的valueid
                    String exValueId = excludeValueId[0];
                    if( valueId.equals(exValueId)) {
                        continue;
                    }
                }


                if(paramUrl.length()>0){
                    paramUrl+="&";
                }
                paramUrl+="valueId="+valueId;

            }
        }

        return paramUrl;

    }
}
















