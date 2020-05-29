package com.usian.service;

import com.usian.mapper.TbContentCategoryMapper;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 内容分类业务层
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {
    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;

    /**
     * 根据父节点 Id 查询子节点
     *
     * @param parentId
     * @return
     */
    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long parentId) {
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbContentCategory> list =
                this.tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        return list;
    }

    @Override
    public Integer insertContentCategory(TbContentCategory tbContentCategory) {
      //添加内容分类
      Date date = new Date();
      tbContentCategory.setSortOrder(1);
        tbContentCategory.setStatus(1);
        tbContentCategory.setIsParent(false);
        tbContentCategory.setCreated(date);
        tbContentCategory.setUpdated(date);

        Integer insertSelective =  tbContentCategoryMapper.insertSelective(tbContentCategory);
        //如果他爹不是爹，要把他爹改成爹

        //查询他爹
        TbContentCategory parentContentCategory =
                tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());
        if (!parentContentCategory.getIsParent()){
            parentContentCategory.setIsParent(true);
            parentContentCategory.setUpdated(new Date());
            int  updateByPrimaryKeySelective = tbContentCategoryMapper.updateByPrimaryKeySelective(parentContentCategory);
        }
        return insertSelective;
    }

    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        TbContentCategory tbContentCategory =
                tbContentCategoryMapper.selectByPrimaryKey(categoryId);
       if (tbContentCategory.getIsParent()){
           return 0;
       }


        //2.删除当前此节点
        tbContentCategoryMapper.deleteByPrimaryKey(categoryId);

        //3.如果他爹不是爹，要把他爹改成不是爹
        TbContentCategoryExample tbContentCategoryExample = new
                TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria =
                tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(tbContentCategory.getParentId());
        List<TbContentCategory> tbContentCategoryList =
                tbContentCategoryMapper.selectByExample(tbContentCategoryExample);

        if (tbContentCategoryList==null ||tbContentCategoryList.size()==0){
            TbContentCategory parentTbContentCategory = new TbContentCategory();
            parentTbContentCategory.setId(tbContentCategory.getParentId());
            parentTbContentCategory.setIsParent(false);
            parentTbContentCategory.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKeySelective(parentTbContentCategory);
        }
        return 200;
    }

    @Override
    public Integer updateContentCategory(TbContentCategory tbContentCategory) {
        tbContentCategory.setUpdated(new Date());
        return tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
    }

}