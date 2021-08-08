package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /**
     * @param userId userid用于未来开发个人主页，显示某个用户的所有帖子，首页显示帖子不需要该参数
     * @param offset 当前行号
     * @param limit  每页最多显示多少条帖子（用于分页）
     * @return 帖子
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * @param userId
     * @return 帖子数量
     */
    /*@Param用于给参数添加别名，如果该参数在<if>里使用（需要动态的拼接条件），且只有这一个参数，一定要加别名*/
    int selectDiscussPostRows(@Param("userId") int userId);
}
