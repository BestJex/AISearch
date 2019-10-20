package com.zjj.aisearch.controller;

import com.zjj.aisearch.model.*;
import com.zjj.aisearch.service.IndexService;
import com.zjj.aisearch.utils.DateTimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/***
 * @program: AISearch
 * @description:
 * @author: zjj
 * @create: 2019-09-21 19:16:26
 **/
@RestController
@Slf4j
@Api(value = "首页", description = "首页")
public class IndexController {

    @Autowired
    private IndexService indexServiceImpl;

    /**
     * 异步校验用户名
     *
     * @return
     */
    @GetMapping("/validateUsername")
    
    public Object validateUsername(String username) {
        int result = indexServiceImpl.validateUsername(username);
        ResponseResult responseResult = new ResponseResult();
        if (result == 0) {
            responseResult.setStatus(0);
            return responseResult;
        } else {
            responseResult.setStatus(-1).setMsg("用户名已存在");
            return responseResult;
        }
    }

    /**
     * login.html,ajax发送的登录请求
     * UserInfo 包含浏览器传过来的所有信息
     * <p>
     * 待优化:没做异常处理,
     * 数据库字段如果为not null,
     * 前端传过来为null,就会出问题
     */
    @RequestMapping("/tologin")
    
    @ApiOperation(value = "登录")
    public Object tologin(@RequestBody UserInfo userInfo) {

        String username = userInfo.getUser().getUsername();
        String password = userInfo.getUser().getPassword();
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
        ResponseResult responseResult = new ResponseResult();

        //登录验证
        try {
            subject.login(usernamePasswordToken);
        } catch (UnknownAccountException e) {
            e.printStackTrace();
            log.error("用户不存在");
            responseResult.setMsg("用户不存在").setStatus(-1);
            return responseResult;

        } catch (IncorrectCredentialsException e) {
            e.printStackTrace();
            log.error("密码错误");
            responseResult.setMsg("密码错误").setStatus(-1);
            return responseResult;
        }

        User user = (User) subject.getPrincipal();

        //插入本次登录的浏览器信息:型号,版本,系统类型
        BrowserInfo browserInfo = new BrowserInfo();
        browserInfo.setSystem(userInfo.getBrowserInfo()[0]);
        browserInfo.setBrowserType(userInfo.getBrowserInfo()[1]);
        browserInfo.setBrowserVersion(userInfo.getBrowserInfo()[2]);
        indexServiceImpl.insertBrowserInfo(browserInfo);
        //返回自动递增的ID
        String browserInfoId = browserInfo.getBrowserInfoId();

        //插入位置信息:X,Y,公网IP,地点,设备类型
        Location location = new Location();
        location.setIp(userInfo.getLocation()[0]);
        location.setLocation(userInfo.getLocation()[1]);
        location.setLocalIp(userInfo.getLocalIp());
        location.setX(userInfo.getLocation()[2]);
        location.setY(userInfo.getLocation()[3]);
        location.setKeyword(userInfo.getPcOrPhone());
        indexServiceImpl.insertLocation(location);
        //返回自动递增的ID
        String locationId = location.getLocationId();

        //插入登录日志
        Integer userId = user.getId();
        LoginLog loginLog = new LoginLog();
        loginLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        loginLog.setBrowserInfoId(browserInfoId);
        loginLog.setLocationId(locationId);
        loginLog.setUserId(userId);
        indexServiceImpl.insertLoginLog(loginLog);
        //返回本次登录日志id
        Integer loginLogId = loginLog.getId();


        log.info("[{}]正在登陆,登录ID为[{}]", username, loginLogId);

        Session session = subject.getSession();
        //往session存入用户数据,和登录loginLogId,用于判断是否登录
        session.setAttribute("user", user);
        session.setAttribute("loginLogId", loginLogId);

        //插入系统日志
        SystemLog systemLog = new SystemLog();
        systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        systemLog.setOperation("login?" + "username=" + username);
        systemLog.setLoginLogId(loginLogId);
        indexServiceImpl.insertSystemLog(systemLog);
        responseResult.setUrl("index").setStatus(0);
        return responseResult;
    }


    /**
     * regist.html,ajax发送的登录请求
     * UserInfo 包含浏览器传过来的所有信息
     * <p>
     * 待优化:没做异常处理,
     * 数据库字段如果为not null,
     * 前端传过来为null,就会出问题
     */
    @RequestMapping("/toregist")
    @ResponseBody
    public Object toregist(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        //插入本次登录的浏览器信息:型号,版本,系统类型
        BrowserInfo browserInfo = new BrowserInfo();
        browserInfo.setSystem(userInfo.getBrowserInfo()[0]);
        browserInfo.setBrowserType(userInfo.getBrowserInfo()[1]);
        browserInfo.setBrowserVersion(userInfo.getBrowserInfo()[2]);
        indexServiceImpl.insertBrowserInfo(browserInfo);
        //返回自动递增的ID
        String browserInfoId = browserInfo.getBrowserInfoId();

        //插入位置信息:X,Y,公网IP,地点,设备类型
        Location location = new Location();
        location.setIp(userInfo.getLocation()[0]);
        location.setLocation(userInfo.getLocation()[1]);
        location.setLocalIp(userInfo.getLocalIp());
        location.setX(userInfo.getLocation()[2]);
        location.setY(userInfo.getLocation()[3]);
        location.setKeyword(userInfo.getPcOrPhone());
        indexServiceImpl.insertLocation(location);
        //返回自动递增的ID
        String locationId = location.getLocationId();

        //插入用户表
        User user = userInfo.getUser();
        user.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        user.setBrowserInfoId(browserInfoId);
        user.setLocationId(locationId);

        ResponseResult responseResult = new ResponseResult();
        //异常
        try {
            indexServiceImpl.insertUser(user);
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                e.printStackTrace();
                responseResult.setMsg("用户名已存在");
                return responseResult;
            } else {
                e.printStackTrace();
                responseResult.setMsg("未知异常,请检查用户名密码是否符合规范!");
                return responseResult;
            }
        }

        //正常
        //拿到登录id,考虑如果用户已登录然后注册其他账号,
        //没有登录,loginLogId就为空
        //插入系统日志
        Integer loginLogId = (Integer) request.getSession().getAttribute("loginLogId");
        SystemLog systemLog = new SystemLog();
        systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        systemLog.setOperation("regist?" + "username=" + user.getUsername());
        systemLog.setLoginLogId(loginLogId);
        indexServiceImpl.insertSystemLog(systemLog);
        log.info("[{}]注册成功", user.getUsername());
        responseResult.setMsg("恭喜" + user.getUsername() + "注册成功" + ",您是第" + user.getId() + "位用户")
                .setUrl("login").setStatus(0);
        return responseResult;

    }


    /**
     * ajax实时搜索
     */
    @RequestMapping("/searchItem")
    @ResponseBody
    public Object searchItem(String keyword, HttpServletResponse res) throws IOException {
        if (!keyword.isEmpty()) {
            List<Item> items = indexServiceImpl.searchItem(keyword);
            return items;
        }
        return null;
    }

    /**
     * 进入命令模式
     */
    @RequestMapping("/command")
    @ResponseBody
    public Object command(String keyword, HttpServletRequest request, RedirectAttributes redirectAttributes) throws IOException {
        User user = ((User) SecurityUtils.getSubject().getPrincipal());
        int index = keyword.indexOf(" ");
        if (index != -1) {
            String substring = keyword.substring(0, index);
            String title = keyword.substring(index + 1);
            if (substring.equals("js")) {
                log.info("[{}]正在简书搜索[{}]", user.getUsername(), title);
                List<JianShuArticle> jianShuArticles = indexServiceImpl.searchJianShuArticle(title);
                return jianShuArticles;
            }
            if (substring.equals("csdn")) {
                log.info("[{}]正在CSDN搜索[{}]", user.getUsername(), title);
                List<Article> Articles = indexServiceImpl.searchArticle(title);
                return Articles;
            }
            if (substring.equals("zh")) {
                log.info("[{}]正在知乎搜索[{}]", user.getUsername(), title);
                List<ZhiHuArticle> zhiHuArticles = indexServiceImpl.searchZhiHuArticle(title);
                return zhiHuArticles;
            }
        }
        return "其他操作";
    }

    /**
     * 定向搜索结果详情
     */
    @RequestMapping("/iscommand")
    public String isCommand(String keyword, RedirectAttributes attributes, HttpServletRequest httpServletRequest, HttpServletResponse res) throws IOException {
        Integer loginLogId = (Integer) httpServletRequest.getSession().getAttribute("loginLogId");
        SystemLog systemLog = new SystemLog();
        int index = keyword.indexOf(" ");
        if (index != -1) {
            String substring = keyword.substring(0, index);
            String title = keyword.substring(index + 1);
            httpServletRequest.getSession().setAttribute("title", title);
            if (substring.equals("js")) {
                httpServletRequest.getSession().setAttribute("command", "js");
                return null;
            }
            if (substring.equals("zh")) {
                httpServletRequest.getSession().setAttribute("command", "zh");
                return null;
            }
            if (substring.equals("csdn")) {
                httpServletRequest.getSession().setAttribute("command", "csdn");
                return null;
            }
        }
        return null;

    }


    @GetMapping("/logout")
    public Object logout(HttpServletRequest httpServletRequest) {
        User user = ((User) SecurityUtils.getSubject().getPrincipal());
        ResponseResult responseResult = new ResponseResult();

        Integer loginLogId = (Integer) httpServletRequest.getSession().getAttribute("loginLogId");

        SystemLog systemLog = new SystemLog();
        systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        systemLog.setOperation(":logout?username" + user.getUsername());
        systemLog.setLoginLogId(loginLogId);
        indexServiceImpl.insertSystemLog(systemLog);

        LogoutLog logoutLog = new LogoutLog();
        logoutLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        logoutLog.setLoginLogId(loginLogId);
        indexServiceImpl.insertLogoutLog(logoutLog);

        httpServletRequest.getSession().invalidate();
        responseResult.setUrl("login").setStatus(0).setMsg("退出成功");
        return responseResult;
    }

    /**
     * 进入定向搜索结果详情页
     */
    @RequestMapping("/commandlist")
    public ModelAndView commandlist(HttpServletRequest request, ModelAndView modelAndView, HttpServletRequest
            httpServletRequest, HttpServletResponse res) throws IOException {
        String command = (String) httpServletRequest.getSession().getAttribute("command");
        String title = (String) httpServletRequest.getSession().getAttribute("title");
        Integer loginLogId = (int) httpServletRequest.getSession().getAttribute("loginLogId");
        if (command.equals("csdn")) {
            List<Article> Articles = indexServiceImpl.searchArticle(title);
            SystemLog systemLog = new SystemLog();
            systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
            systemLog.setOperation(":csdn" + "?keyword=" + title);
            systemLog.setLoginLogId(loginLogId);
            indexServiceImpl.insertSystemLog(systemLog);
            modelAndView.setViewName("commandlist");
            modelAndView.addObject("items", Articles);
            return modelAndView;
        }
        if (command.equals("js")) {
            List<JianShuArticle> jianShuArticles = indexServiceImpl.searchJianShuArticle(title);
            SystemLog systemLog = new SystemLog();
            systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
            systemLog.setOperation(":js" + "?keyword=" + title);
            systemLog.setLoginLogId(loginLogId);
            indexServiceImpl.insertSystemLog(systemLog);
            modelAndView.setViewName("commandlist");
            modelAndView.addObject("items", jianShuArticles);
            return modelAndView;
        }
        return null;
    }


    /**
     * 进入知乎定向搜索结果详情页
     */
    @RequestMapping("/zhihucommandlist")
    public ModelAndView zhihucommandlist(HttpServletRequest request, ModelAndView modelAndView, HttpServletRequest
            httpServletRequest, HttpServletResponse res) throws IOException {
        String command = (String) httpServletRequest.getSession().getAttribute("command");
        String title = (String) httpServletRequest.getSession().getAttribute("title");
        Integer loginLogId = (int) httpServletRequest.getSession().getAttribute("loginLogId");
        if (command.equals("zh")) {
            List<ZhiHuArticle> zhiHuArticles = indexServiceImpl.searchZhiHuArticle(title);
            SystemLog systemLog = new SystemLog();
            systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
            systemLog.setOperation(":zh" + "?keyword=" + title);
            systemLog.setLoginLogId(loginLogId);
            indexServiceImpl.insertSystemLog(systemLog);
            modelAndView.setViewName("zhihucommandlist");
            modelAndView.addObject("items", zhiHuArticles);
            return modelAndView;
        }
        return null;
    }

    /**
     * 重定向到搜索结果详情页
     */
    @RequestMapping("/todetail")
    public String toDetail(@RequestBody Info info, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse res) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (!info.getKeyword().isEmpty()) {
            request.getSession().setAttribute("keyword", info.getKeyword());
            //不做任何事,避免生成两次记录
            return null;
        }
        return null;
    }


    /**
     * 进入搜索结果详情页
     */
    @RequestMapping("/detail")
    public ModelAndView detail(HttpServletRequest request, ModelAndView modelAndView, HttpServletRequest
            httpServletRequest, HttpServletResponse res) throws IOException {
        String keyword = (String) httpServletRequest.getSession().getAttribute("keyword");
        List<Item> items = indexServiceImpl.searchItem(keyword);
        modelAndView.addObject("items", items);
        modelAndView.setViewName("/detail");
        Integer loginLogId = (Integer) request.getSession().getAttribute("loginLogId");
        SystemLog systemLog = new SystemLog();
        systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        systemLog.setOperation("detail" + "?keyword=" + keyword);
        systemLog.setLoginLogId(loginLogId);
        SearchRecord searchRecord = new SearchRecord();
        searchRecord.setKeyword(keyword);
        searchRecord.setSearchTime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        searchRecord.setLoginLogId(loginLogId);
        indexServiceImpl.insertSearchRecord(searchRecord);
        indexServiceImpl.insertSystemLog(systemLog);
        return modelAndView;
    }

    /**
     * 便签模式
     */
    @PostMapping("/note")
    @ResponseBody
    public Object note(@RequestBody Map<String, String> map, HttpServletRequest request) {
        log.info("便签内容为:[{}]", map.get("content"));
        User user = (User) request.getSession().getAttribute("user");
        ResponseResult responseResult = new ResponseResult();
        Integer loginLogId = (Integer) request.getSession().getAttribute("loginLogId");

        AiNote aiNote = new AiNote();
        aiNote.setContent(map.get("content"));
        aiNote.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        aiNote.setLoginLogId(loginLogId);

        SystemLog systemLog = new SystemLog();
        systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
        systemLog.setOperation("note" + "?content=" + aiNote.getContent());
        systemLog.setLoginLogId(loginLogId);

        indexServiceImpl.insertSystemLog(systemLog);
        indexServiceImpl.insertAiNote(aiNote);
        responseResult.setStatus(0).setData(map.get("content"));
        return responseResult;
    }

    /**
     * 随机csdn文章功能
     */
    @GetMapping("/articledata")
    public Object article(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        ResponseResult responseResult = new ResponseResult();
        Article article;
        for (; ; ) {
            Random r = new Random();
            int id = r.nextInt(101268) + 1;
            article = indexServiceImpl.search(id);
            if (article != null) {
                Integer loginLogId = (Integer) request.getSession().getAttribute("loginLogId");
                SystemLog systemLog = new SystemLog();
                systemLog.setCreatetime(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
                systemLog.setOperation("article" + "?id=" + article.getId() + "&title=" + article.getTitle());
                systemLog.setLoginLogId(loginLogId);
                indexServiceImpl.insertSystemLog(systemLog);
                break;
            }
        }
        responseResult.setData(article);
        return responseResult;
    }
}


