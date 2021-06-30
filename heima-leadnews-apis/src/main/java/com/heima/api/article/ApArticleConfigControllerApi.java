package com.heima.api.article;

import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleConfigControllerApi {

   /**
    * 保存app端文章配置
    * @param apArticleConfig
    * @return
    */
   ResponseResult saveArticleConfig(ApArticleConfig apArticleConfig);
}