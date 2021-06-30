package com.heima.admin.feign;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("leadnews-article")
public interface ArticleFeign {

    /**
     * 保存文章数据
     * @param apArticle 文章对象
     * @return
     */
    @PostMapping("/api/v1/article/save")
    public ApArticle saveAparticle(ApArticle apArticle);

    /**
     * 保存文章配置对象
     * @param apArticleConfig 文章配置对象
     * @return
     */
    @PostMapping("/api/v1/article_config/save")
    public ResponseResult saveArticleConfig(ApArticleConfig apArticleConfig);

    /**
     * 保存文章内容数据
     * @param apArticleContent 文章内容
     * @return
     */
    @PostMapping("/api/v1/article_content/save")
    public ResponseResult saveArticleContent(ApArticleContent apArticleContent);

    /**
     * 根据名字查询作者
     * @param name 名字
     * @return
     */
    @GetMapping("/api/v1/author/findByName/{name}")
    public ApAuthor selectAuthorByName(@PathVariable("name") String name);
}
