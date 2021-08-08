package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    private final DiscussPostMapper discussPostMapper;

    @Autowired
    public DiscussPostService(DiscussPostMapper discussPostMapper) {
        this.discussPostMapper = discussPostMapper;
    }

    public List<DiscussPost> findDiscussPost(int useId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(useId, offset, limit);
    }

    public int findDiscussPostRows(int useId) {
        return discussPostMapper.selectDiscussPostRows(useId);
    }
}
