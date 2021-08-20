package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoggingController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private Producer producer;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingController.class);

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    // 如果传入的值和bean的属性相匹配，那么spring可以自动创建bean，并注入值
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }

        map.forEach(model::addAttribute);
        return "site/register";
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        switch (result) {
            case ACTIVATION_SUCCESS:
                model.addAttribute("msg", "激活成功！您的帐号已经可以正常使用！");
                model.addAttribute("target", "/login");
                break;
            case ACTIVATION_REPEAT:
                model.addAttribute("msg", "无效操作，该账号已经激活！");
                model.addAttribute("target", "/index");
                break;
            default:
                model.addAttribute("msg", "激活失败！您提供的激活码不正确！");
                model.addAttribute("target", "/index");
                break;
        }

        return "/site/operate-result";
    }

    /*
    验证码不存于session中，改为存在redis
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);

//         验证码存入session
//        session.setAttribute("kaptcha", text);

        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        // 存入redis，60s失效
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器，该输出流由spring维护，不需要关闭
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            LOGGER.error("响应验证码失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchOwner") String owner) {
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(owner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(owner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (code == null || !code.equalsIgnoreCase(kaptcha)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }

        // 检查账号，密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECOND : DEFAULT_EXPIRED_SECOND;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        // 登陆成功
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }

        map.forEach(model::addAttribute);
        return "/site/login";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }
}
