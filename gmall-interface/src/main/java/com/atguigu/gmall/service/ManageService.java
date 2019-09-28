package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;
import java.util.Map;

public interface ManageService {

    //查询一级分类
    public List<BaseCatalog1> getCatalog1();
    //查询二级分类 根据一级分类ID
    public List<BaseCatalog2> getCatalog2(String catalog1Id);
    //查询二级分类 根据二级分类ID
    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    //根据三级分类查询平台属性
    public List<BaseAttrInfo> getAttrList(String catalog3Id);
    //保存平台属性
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    //根据平台属性id 查询平台属性的详情 顺便把该属性值列表也取到
    public BaseAttrInfo getBaseAttrInfo(String attrId);

    //删除平台属性

    //获得基本销售属性
    public List<BaseSaleAttr> getBaseSaleAttrList();

    //保存spu信息
    public void saveSpuInfo(SpuInfo spuInfo);

    //根据三级分类查询spu列表
    public List<SpuInfo> getSpuList(String catalog3Id);

    //根据spuID查询图片列表
    public List<SpuImage> getSpuImageList(String spuId);

    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    //保存skuinfo
    public void saveSkuInfo(SkuInfo skuInfo);

    //查询skuInfo
    public SkuInfo getSkuInfo(String skuId);

    //根据spuId查询销售属性,选中传人的sku设计的销售属性
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String skuId,String spuId);

    //根据spuid查询已有的sku涉及销售属性清单
    public Map getSkuValueIdsMap(String spuId);

    //根据多个属性值查询平台属性
    public List<BaseAttrInfo> getAttrList(List attrValueIdList);
}





















