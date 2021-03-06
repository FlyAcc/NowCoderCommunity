package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJsonString(403, "你还没有登陆！");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件
        fireDiscussPostEvent(user.getId(), discussPost.getId());

        // 异常将来用统一异常处理
        return CommunityUtil.getJsonString(0, "发布成功");
    }

    /*
    帖子详情页：该页面有两种评论，一是回帖，二是回复
    discussPost: 当前帖子
    user: 楼主
    comments: 回帖（list：包含评论对象map：commentViewObject）
        commentViewObject含四个字段：comment、user（回帖用户）、replyCount（回复数），replies（回复，即楼中楼）
            replies：楼层中用户相互的回复（list：包含回复对象map：replyViewObject）
                replyViewObject含三个字段：user（回复主体）、reply（回复）、target（被回复的对象，直接回复层主则没有target）
     */
    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("discussPost", discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);
        // 点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() != null ?
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId) : 0;
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 评论列表
        List<Comment> comments = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, discussPostId, page.getOffset(), page.getLimit()
        );
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        // 评论VO列表
        if (comments != null) {
            for (Comment comment : comments) {
                // 评论显示对象
                Map<String, Object> commentViewObject = new HashMap<>();
                commentViewObject.put("comment", comment);
                commentViewObject.put("user", userService.findUserById(comment.getUserId()));

                // 点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentViewObject.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() != null ?
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                ENTITY_TYPE_COMMENT, comment.getId()) : 0;
                commentViewObject.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyViewObject = new HashMap<>();
                        replyViewObject.put("reply", reply);
                        replyViewObject.put("user", userService.findUserById(reply.getUserId()));
                        // 回复对象（只有回复该字段才有意义）
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyViewObject.put("target", target);

                        // 点赞数
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyViewObject.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() != null ?
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                        ENTITY_TYPE_COMMENT, reply.getId()) : 0;
                        replyViewObject.put("likeStatus", likeStatus);

                        replyVoList.add(replyViewObject);
                    }
                }

                commentViewObject.put("replies", replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentViewObject.put("replyCount", replyCount);
                commentVoList.add(commentViewObject);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        return updatePost(id, -1, 1);
    }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        return updatePost(id, 1, -1);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDeleted(int id) {
        discussPostService.updateStatus(id, 2);
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJsonString(0);
    }

    /**
     * 更新帖子类型或状态
     *
     * @param id     帖子id
     * @param status 帖子状态：0-正常; 1-精华; 2-拉黑; -1-不进行设置
     * @param type   帖子类型 0-普通; 1-置顶；-1-不进行设置
     * @return 状态信息
     */
    private String updatePost(int id, int status, int type) {
        if (status != -1) {
            discussPostService.updateStatus(id, status);
        }

        if (type != -1) {
            discussPostService.updateType(id, type);
        }

        fireDiscussPostEvent(hostHolder.getUser().getId(), id); // 更新elasticsearch中的索引
        return CommunityUtil.getJsonString(0); // 成功状态，异步
    }

    private void fireDiscussPostEvent(int userId, int postId) {
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(userId)
                .setEntityId(postId)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
    }
}
