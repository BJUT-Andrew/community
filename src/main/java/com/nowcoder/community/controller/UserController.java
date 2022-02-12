package com.nowcoder.community.controller;

import com.google.code.kaptcha.impl.FishEyeGimpy;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;


/**
 * @author andrew
 * @create 2021-10-22 20:15
 */
@Controller
@RequestMapping(value = "/user")
public class UserController implements CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private FollowService followService;

    @Autowired
    private LikeService likeService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }


    //要将头像上传至云服务器，所以废弃此方法
    @LoginRequired
    //处理修改头像的表单提交请求/user/upload
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    //SpringMVC中使用MultipartFile处理上传文件，headerImage接收来自前端提交的表单信息，model用于存储错误信息
    public String uploadHeader(MultipartFile headerImage, Model model) {

        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        //取出上传文件的后缀名，并判断是否正常
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "上传的文件格式不正确！");
        }

        //为避免多个用户上传的头像文件名相同，给每个头像文件起一个随机名
        String fileName = CommunityUtil.generateUUID() + suffix;
        //根据文件名，确定头像文件的存储路径，并生成文件对象
        File dest = new File(uploadPath + "/" + fileName);
        //使用MultipartFile的transferTo(File file)方法，向file对象中存储文件
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        //更新当前用户的头像访问路径（更新为web访问路径，浏览器发送查询头像请求的请求地址，下面再专门声明一个控制器方法处理此请求）
        //http://localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        User user = hostHolder.getUser();
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }


    //要将头像上传至云服务器，所以废弃此方法
    //处理查询头像的请求，在主页通过${loginUser.headerUrl}显示用户头像，在此控制器方法中对此请求进行处理，在浏览器上响应用户头像
    @RequestMapping(value = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        //获取服务器实体存储路径
        String storagePath = uploadPath + "/" + fileName;

        //获取文件后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        //向浏览器响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(storagePath);
                ServletOutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (Exception e) {
            logger.error("读取头像失败", e.getMessage());
        }
    }

    @LoginRequired
    //处理修改密码的请求
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model){

        User user = hostHolder.getUser();

        //调用Service层方法，根据不同结果，向共享域中存放不同信息并跳转不同页面
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);

        //map为空则修改密码成功；map不为空则修改密码失败；
        if (map == null || map.isEmpty()){
            return "redirect:/logout";
        }else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    //个人主页
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProFilePage(@PathVariable("userId") int userId, Model model){

        User user = userService.findUserById(userId);

        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        //用户
        model.addAttribute("user", user);
        //用户被点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //是否已关注此用户
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";

    }
}
