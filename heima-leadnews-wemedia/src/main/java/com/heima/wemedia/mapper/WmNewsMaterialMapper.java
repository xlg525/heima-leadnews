package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface WmNewsMaterialMapper extends BaseMapper<WmNewsMaterial> {

    void saveRelations(@Param("materials") List<String> materialsIds, @Param("newsId")Integer newsId, @Param("type")Short type);
}