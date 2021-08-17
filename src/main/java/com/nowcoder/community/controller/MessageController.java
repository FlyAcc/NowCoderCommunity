package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
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
import java.util.stream.Collectors;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /*
    私信列表需要显示的信息有：总的未读数，所有私信会话（只显示最新的一条私信内容），每个会话中的所有消息及其数量和未读数
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 会话列表
        List<Message> conversationsList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationInfos = new ArrayList<>();
        if (conversationsList != null) {
            for (Message message : conversationsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                // target即私信的对象
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversationInfos.add(map);
            }
        }
        model.addAttribute("conversations", conversationInfos);

        // 查询所有未读消息的数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", getLetterTarget(conversationId));
        List<Integer> unreadLetters = getUnreadLetters(letterList);
        if (!unreadLetters.isEmpty()) {
            messageService.readMessage(unreadLetters);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getUnreadLetters(List<Message> letters) {
        return letters == null ? new ArrayList<>() :
                letters.stream()
                        .filter(letter -> hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0)
                        .map(Message::getId)
                        .collect(Collectors.toList());
    }

    /*
    获取私信对象
     */
    private User getLetterTarget(String conversationId) {
        User user = hostHolder.getUser();
        String[] ids = conversationId.split("_");
        int targetId = Integer.parseInt(ids[0].equals(user.getId() + "") ? ids[1] : ids[0]);
        return userService.findUserById(targetId);
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody // 异步使用，不返回网页，返回数据（json），网页端获取数据后，动态更新网页（不刷新）
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJsonString(1, "目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        String conversationId = message.getFromId() < message.getToId() ?
                message.getFromId() + "_" + message.getToId() : message.getToId() + "_" + message.getFromId();
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJsonString(0);
    }
}