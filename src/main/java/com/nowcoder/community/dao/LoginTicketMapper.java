package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    // 可以通过xml写配置，也可以通过注解
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    // id自动生成
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    // 编写动态sql，需要加上script标签
    @Update({
            "<script>",
            "update login_ticket set status = #{status} where ticket=#{ticket}",
            "<if test=\"ticket!=null\">",
            "and 1 = 1",
            "</if>",
            "</script>",
    })
    int updateStatus(String ticket, int status);
}
