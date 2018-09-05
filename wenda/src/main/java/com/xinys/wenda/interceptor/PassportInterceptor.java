package com.xinys.wenda.interceptor;

import com.xinys.wenda.dao.LoginTicketDAO;
import com.xinys.wenda.dao.UserDAO;
import com.xinys.wenda.model.HostHolder;
import com.xinys.wenda.model.LoginTicket;
import com.xinys.wenda.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by nowcoder on 2016/7/3.
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            // 1.从request中获取Cookies
            for (Cookie cookie : httpServletRequest.getCookies()) {
                // 2.找到Cookies中ticket的value
                if (cookie.getName().equals("ticket")) {
                    // 3.把value保存下来，然后去数据库中查找这个value
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        if (ticket != null) {
            // 4.如果token不为空，放到数据库中取查找
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
                // 5.如果找不到token的value直接返回
                return true;
            }
            // 6.如果数据库中有value,则根据此value对象获取User对象的id
            User user = userDAO.selectById(loginTicket.getUserId());
            hostHolder.setUser(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
