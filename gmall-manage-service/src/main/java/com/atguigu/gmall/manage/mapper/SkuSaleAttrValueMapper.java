package com.atguigu.gmall.manage.mapper;


import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    public List<Map> getSaleAttrValuesBySpu(String spuId);
}
