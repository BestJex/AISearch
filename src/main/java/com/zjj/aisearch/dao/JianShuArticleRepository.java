package com.zjj.aisearch.dao;

import com.zjj.aisearch.model.JianShuArticle;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author sanglp
 * @create 2018-07-04 9:27
 * @desc 文章reposiroty  泛型的参数分别是实体类型和主键类型
 **/
public interface JianShuArticleRepository extends ElasticsearchRepository<JianShuArticle,Integer> {

}