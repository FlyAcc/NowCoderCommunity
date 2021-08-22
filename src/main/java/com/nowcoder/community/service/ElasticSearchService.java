package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticSearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate searchTemplate;

    // 重新保存即为修改
    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(DiscussPost post) {
        discussPostRepository.delete(post);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        // fargment设为0，highlightfield显示全部内容
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>").numOfFragments(0),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>").numOfFragments(0)
                ).build();
        SearchHits<DiscussPost> searchHits = searchTemplate.search(query, DiscussPost.class);
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            List<String> titles = searchHit.getHighlightField("title");
            if (!titles.isEmpty()) {
                searchHit.getContent().setTitle(titles.get(0));
            }
            List<String> contents = searchHit.getHighlightField("content");
            if (!contents.isEmpty()) {
                searchHit.getContent().setContent(contents.get(0));
            }
        }
        return (Page<DiscussPost>) SearchHitSupport.unwrapSearchHits(
                SearchHitSupport.searchPageFor(searchHits, query.getPageable()));
    }
}
