package com.heima.api.article;

import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleContentControllerApi {

   /**
    * 保存app端文章内容
    * @param apArticleContent
    * @return
    */
   ResponseResult saveArticleContent(ApArticleContent apArticleContent);
}