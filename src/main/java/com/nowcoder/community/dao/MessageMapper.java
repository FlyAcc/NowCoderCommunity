package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 私信列表需要显示的信息有：总的未读数，所有私信会话及数量（只显示最新的一条私信内容），每个会话中的所有消息及其数量和未读数
 */
@Mapper
public interface MessageMapper {
    /**
     * 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
     *
     * @param userId 用户id
     * @param offset
     * @param limit
     * @return 会话列表
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询当前用户的所有会话的数量
     *
     * @param userId 用户id
     * @return 会话数量
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话所包含的所有消息
     *
     * @param conversationId 会话id
     * @param offset
     * @param limit
     * @return 一个会话内的所有消息
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 查询某个会话所包含的消息数量
     *
     * @param conversationId 会话id
     * @return 会话内的消息数量
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询未读消息数量，若会话id为null，则查询全部未读消息的数量
     *
     * @param userId         用户id
     * @param conversationId 会话id
     * @return 未读消息数量
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    int insertMessage(Message message);

    /**
     * 修改消息状态
     *
     * @param ids
     * @param status 1已读 2删除
     * @return
     */
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读通知的数量
    int selectNoticeUnreadCount(int userId, String topic);
}
