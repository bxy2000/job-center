package com.boxy.job.admin.controller;

import com.boxy.job.admin.controller.annotation.PermissionLimit;
import com.boxy.job.admin.core.model.JobGroup;
import com.boxy.job.admin.core.model.JobUser;
import com.boxy.job.admin.core.util.I18nUtil;
import com.boxy.job.admin.dao.JobGroupDao;
import com.boxy.job.admin.dao.JobUserDao;
import com.boxy.job.admin.service.LoginService;
import com.boxy.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private JobUserDao jobUserDao;
    @Resource
    private JobGroupDao jobGroupDao;

    @RequestMapping
    @PermissionLimit(adminuser = true)
    public String index(Model model) {

        // 执行器列表
        List<JobGroup> groupList = jobGroupDao.findAll();
        model.addAttribute("groupList", groupList);

        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String username, int role) {

        // page list
        List<JobUser> list = jobUserDao.pageList(start, length, username, role);
        int list_count = jobUserDao.pageListCount(start, length, username, role);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @RequestMapping("/add")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> add(JobUser jobUser) {

        // valid username
        if (!StringUtils.hasText(jobUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input")+I18nUtil.getString("user_username") );
        }
        jobUser.setUsername(jobUser.getUsername().trim());
        if (!(jobUser.getUsername().length()>=4 && jobUser.getUsername().length()<=20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        }
        // valid password
        if (!StringUtils.hasText(jobUser.getPassword())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input")+I18nUtil.getString("user_password") );
        }
        jobUser.setPassword(jobUser.getPassword().trim());
        if (!(jobUser.getPassword().length()>=4 && jobUser.getPassword().length()<=20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        }
        // md5 password
        jobUser.setPassword(DigestUtils.md5DigestAsHex(jobUser.getPassword().getBytes()));

        // check repeat
        JobUser existUser = jobUserDao.loadByUserName(jobUser.getUsername());
        if (existUser != null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("user_username_repeat") );
        }

        // write
        jobUserDao.save(jobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/update")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> update(HttpServletRequest request, JobUser jobUser) {

        // avoid opt login seft
        JobUser loginUser = (JobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        if (loginUser.getUsername().equals(jobUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
        }

        // valid password
        if (StringUtils.hasText(jobUser.getPassword())) {
            jobUser.setPassword(jobUser.getPassword().trim());
            if (!(jobUser.getPassword().length()>=4 && jobUser.getPassword().length()<=20)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
            }
            // md5 password
            jobUser.setPassword(DigestUtils.md5DigestAsHex(jobUser.getPassword().getBytes()));
        } else {
            jobUser.setPassword(null);
        }

        // write
        jobUserDao.update(jobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> remove(HttpServletRequest request, int id) {

        // avoid opt login seft
        JobUser loginUser = (JobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        if (loginUser.getId() == id) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
        }

        jobUserDao.delete(id);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public ReturnT<String> updatePwd(HttpServletRequest request, String password){

        // valid password
        if (password==null || password.trim().length()==0){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码不可为空");
        }
        password = password.trim();
        if (!(password.length()>=4 && password.length()<=20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        }

        // md5 password
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // update pwd
        JobUser loginUser = (JobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);

        // do write
        JobUser existUser = jobUserDao.loadByUserName(loginUser.getUsername());
        existUser.setPassword(md5Password);
        jobUserDao.update(existUser);

        return ReturnT.SUCCESS;
    }

}
