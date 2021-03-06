package com.usian.service;

import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemCategoryServiceImpl implements ItemCategoryService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    /**
     * 根据分类 ID 查询子节点
     *
     * @param id
     * @return
     */
    @Override
    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(id);
        criteria.andStatusEqualTo(1);
        List<TbItemCat> list = this.tbItemCatMapper.selectByExample(example);
        return list;
    }

    @Override
    public CatResult selectItemCategoryAll() {
        //1.查询商品类目
       //因为一级菜单有子菜单。子菜单有子菜单，所以要递归调用
       List catlist= getCatList(0L);
        CatResult catResult = new CatResult();
        catResult.setData(catlist);

        return catResult;
    }

    private List getCatList(Long parentId) {
        //2.把查询结果装载到List<CatNode>,并且只装18次
        //1。查询商品类目列表
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(tbItemCatExample);
         //2.拼接CatNode
        List catNodeList = new ArrayList();
        int count = 0;
        for (int i = 0; i < tbItemCatList.size(); i++) {
            TbItemCat tbItemCat = tbItemCatList.get(i);
            //2.1该类目是父节点：n:"",i:[]
            if (tbItemCat.getIsParent()) {
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getCatList(tbItemCat.getId()));
                //父节点装到集合中
                catNodeList.add(catNode);
                count = count + 1;
                if (count == 18) {
                    break;
                }
            } else {
                //2.2该节点不是父节点。直接把类目名称添加到 catNodeList 集合中
                catNodeList.add(tbItemCat.getName());
            }
        }
        return catNodeList;
    }
}