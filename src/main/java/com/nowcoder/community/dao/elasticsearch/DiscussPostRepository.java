package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 根据目标实体类，继承了ElasticsearchRepository接口的子接口
 * @author andrew
 * @create 2021-11-04 15:03
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer>{
}
