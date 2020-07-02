package com.speaker.controller;

import com.speaker.config.WebSecurityConfig;
import com.speaker.model.*;
import com.speaker.service.RoleService;
import com.speaker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class UserController
{

    public static String uploadDirectory = System.getProperty("user.dir")+"\\src\\main\\resources\\static\\uploads";
    public static String postUploadDirectory = System.getProperty("user.dir")+"\\src\\main\\resources\\static\\postUploads";

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService RoleService;

    @Autowired
    private WebSecurityConfig webSecurityConfig;


    @GetMapping("/registration")
    public String registration(Model model)
    {
        boolean isUserLogged = webSecurityConfig.isUserLogged();
        if(isUserLogged)
        {
            return "redirect:/chat";
        }

        model.addAttribute("user", new User.UserBuilder().build());

        return "registration";
    }

    @PostMapping(value = "/registration")
    public String registration(@Valid User user, BindingResult bindingResult, Role role, Model model)
    {
        if (bindingResult.hasErrors())
        {
            bindingResult.getAllErrors().forEach(error ->
            {
                System.out.println(error.getObjectName() + " " + error.getDefaultMessage());
            });
            return "registration";
        }
        else
        {
            User similar = userService.findUserByEmail(user.getEmail());
            if(similar != null)
            {
                model.addAttribute("error", "This email is already exists.");
                return "registration";
            }

            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());

            user.setPassword(encodedPassword);
            user.setBackgroundPic("");
            user.setDescription("");
            user.setProfile("");
            user.setEnabled(true);

            role.setEmail(user.getEmail());
            role.setRole("USER");

            userService.createUser(user);
            RoleService.persistRole(role);

            return "redirect:/";
        }
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout)
    {
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "login";
    }

    @GetMapping("/userProfile")
    public String userProfile(@RequestParam("id") Integer id, Model model, User userProfile)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.isEnabled()) return "redirect:/logout";
        Object privileges = webSecurityConfig.userPrivileges();
        Integer userId = user.getId();

        userProfile = userService.findUserById(id);

        List<UserPost> userPosts = userService.findUserPosts(id);
        List<PostLikes> userLikes = userService.findUserLikes(userId);
        List<FriendsDTO> friends = userService.findUserFriends(id);
        List<FriendsDTO> invitedYou = userService.findUsersInvitedYou(userId);
        List<PostLikes> postLikes = userService.findUserPostLikes();
        List<PostComments> postComments = userService.findUserPostComments();

        String friend = "Add friend";
        for(FriendsDTO isFriend : friends)
        {
            if(user.getId().equals(userProfile.getId()))
            {
                friend = "";
                break;
            }
            if(user.getId().equals(isFriend.getUserId()))
            {
                if(isFriend.isConfirm())
                {
                    friend = "Remove friend";
                }
                else
                {
                    friend = "Request sent";
                }
                break;
            }
            else
            {
                friend = "Add friend";
            }
        }

        Integer countFriends = 0;
        for(FriendsDTO f : friends)
        {
            if(f.isConfirm()) countFriends++;
        }

        model.addAttribute("user", user);
        model.addAttribute("privileges", privileges);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("friend", friend);
        model.addAttribute("friends", friends);
        model.addAttribute("invitedYou", invitedYou);
        model.addAttribute("countFriends", countFriends);
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("postLikes", postLikes);
        model.addAttribute("userLikes", userLikes);
        model.addAttribute("postComments", postComments);
        return "userProfile";
    }

    @PostMapping(value = "/userProfile/update")
    public String updateUser(User user, RedirectAttributes redirectAttributes)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        if(!userEmail.equals(user.getEmail()))
        {
            return "redirect:/logout";
        }

        if(user.getUsername() == null || user.getUsername().equals(""))
        {
            redirectAttributes.addFlashAttribute("message", "Username can not be empty!");
            return "redirect:/userProfile?id="+user.getId();
        }
        userService.updateUserProfile(user);
        return "redirect:/userProfile?id="+user.getId();
    }

    @RequestMapping("/userProfile/backgroundUpload")
    public String backgroundUpload(RedirectAttributes redirectAttributes, @RequestParam("fileBackground") MultipartFile backgroundUpload,
                                   @RequestParam("id") Integer id)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(id))
        {
            return "redirect:/logout";
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(backgroundUpload.getOriginalFilename());
        Path fileNameAndPath = Paths.get(uploadDirectory, backgroundUpload.getOriginalFilename());
        String mimeType = "";
        int i = fileName.lastIndexOf(".");
        if(i > 0)
        {
            mimeType = fileName.substring(i + 1);
        }

        if(mimeType.equals("jpg") || mimeType.equals("jpeg") || mimeType.equals("png") || mimeType.equals("JPG")
                || mimeType.equals("JPEG") || mimeType.equals("PNG"))
        {
            try
            {
                Files.write(fileNameAndPath, backgroundUpload.getBytes());
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

            userService.backgroundUpload(id, fileName.toString());
            redirectAttributes.addFlashAttribute("message", "Successfully uploaded " + fileName.toString());
        }
        else
        {
            redirectAttributes.addFlashAttribute("message", "You can only upload jpg, jpeg or png files!");
            return "redirect:/userProfile?id="+id;
        }

        return "redirect:/userProfile?id="+id;
    }

    @RequestMapping("/userProfile/profileUpload")
    public String profileUpload(RedirectAttributes redirectAttributes, @RequestParam("fileProfile") MultipartFile profileUpload,
                                @RequestParam("id") Integer id)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(id))
        {
            return "redirect:/logout";
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(profileUpload.getOriginalFilename());
        Path fileNameAndPath = Paths.get(uploadDirectory, profileUpload.getOriginalFilename());
        String mimeType = "";
        int i = fileName.lastIndexOf(".");
        if(i > 0)
        {
            mimeType = fileName.substring(i + 1);
        }

        if(mimeType.equals("jpg") || mimeType.equals("jpeg") || mimeType.equals("png") || mimeType.equals("JPG")
                || mimeType.equals("JPEG") || mimeType.equals("PNG"))
        {
            try
            {
                Files.write(fileNameAndPath, profileUpload.getBytes());
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }

            userService.profileUpload(id, fileName.toString());
            redirectAttributes.addFlashAttribute("message", "Successfully uploaded " + fileName.toString());
        }
        else
        {
            redirectAttributes.addFlashAttribute("message", "You can only upload jpg, jpeg or png files!");
            return "redirect:/userProfile?id="+id;
        }

        return "redirect:/userProfile?id="+id;
    }

    @RequestMapping("/userProfile/addPost")
    public String addPost(RedirectAttributes redirectAttributes, @RequestParam("userId") Integer userId,
                          @RequestParam("link") String link, @RequestParam("description") String description,
                          @RequestParam("filePost") MultipartFile postUpload, UserPost userPost)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        if(postUpload.equals("") || postUpload.equals(null))
        {
            redirectAttributes.addFlashAttribute("message", "You have to include pic to your post!");
            if(link.equals("profile"))
            {
                return "redirect:/userProfile?id="+userId;
            }
            else
            {
                return "redirect:/";
            }
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(postUpload.getOriginalFilename());
        Path fileNameAndPath = Paths.get(postUploadDirectory, postUpload.getOriginalFilename());
        String mimeType = "";
        int i = fileName.lastIndexOf(".");
        if(i > 0)
        {
            mimeType = fileName.substring(i + 1);
        }

        if(mimeType.equals("jpg") || mimeType.equals("jpeg") || mimeType.equals("png") || mimeType.equals("JPG")
                || mimeType.equals("JPEG") || mimeType.equals("PNG"))
        {
            try
            {
                Files.write(fileNameAndPath, postUpload.getBytes());
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

            String time = new SimpleDateFormat("EEE, d MMM yyyy HH:mm").format(new Date());
            userPost.setDatePosted(time);
            userPost.setUserId(userId);
            userPost.setDescription(description);
            userPost.setPic(fileName.toString());
            userPost.setCountLikes(0);
            userPost.setCountComments(0);

            userService.postUpload(userPost);
            userService.postUpload(userPost);
            redirectAttributes.addFlashAttribute("message", "Successfully post uploaded");
        }
        else
        {
            redirectAttributes.addFlashAttribute("message", "You can only upload jpg, jpeg or png files!");
            if(link.equals("profile"))
            {
                return "redirect:/userProfile?id="+userId;
            }
            else
            {
                return "redirect:/";
            }
        }
        if(link.equals("profile"))
        {
            return "redirect:/userProfile?id="+userId;
        }
        else
        {
            return "redirect:/";
        }
    }

    @RequestMapping("/userProfile/deletePost/{userId}/{postId}/{link}")
    public String deletePost(@PathVariable("userId") Integer userId, @PathVariable("postId") Integer postId,
                             @PathVariable("link") String link)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        userService.deletePost(postId);
        if(link.equals("profile"))
        {
            return "redirect:/userProfile?id="+userId;
        }
        else
        {
            return "redirect:/";
        }
    }

    @RequestMapping("/userProfile/like/{postId}/{userId}/{userProfileId}/{link}")
    public String like(@PathVariable("postId") Integer postId, @PathVariable("userId") Integer userId,
                       @PathVariable("userProfileId") Integer userProfileId, @PathVariable("link") String link, PostLikes postLikes)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }

        PostLikes isLiked = userService.isLiked(postId, userId);
        if(isLiked == null || isLiked.equals(""))
        {
            postLikes.setPostId(postId);
            postLikes.setUserId(userId);
            userService.postLike(postLikes);
        }
        else
        {
            userService.deleteLike(userId, postId);
        }

        if(link.equals("profile"))
        {
            return "redirect:/userProfile?id="+userProfileId;
        }
        else
        {
            return "redirect:/";
        }
    }

    @RequestMapping("/userProfile/addCom")
    public String addCom(@RequestParam("postId") Integer postId, @RequestParam("userId") Integer userId,
                         @RequestParam("description") String description, PostComments postComments,
                         @RequestParam("profileId") Integer profileId, @RequestParam("link") String link)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }

        if(description.equals("")) return "redirect:/userProfile?id="+profileId;

        postComments.setPostId(postId);
        postComments.setUserId(userId);
        postComments.setDescription(description);
        userService.addCom(postComments);

        if(link.equals("profile"))
        {
            return "redirect:/userProfile?id="+profileId;
        }
        else
        {
            return "redirect:/";
        }
    }

    @RequestMapping("/userProfile/deleteCom")
    public String deleteCom(@RequestParam("commentId") Integer commentId, @RequestParam("postId") Integer postId,
                            @RequestParam("userId") Integer userId, @RequestParam("profileId") Integer userProfileId,
                            @RequestParam("link") String link)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        userService.deleteCom(commentId, postId);

        if(link.equals("profile"))
        {
            return "redirect:/userProfile?id="+userId;
        }
        else
        {
            return "redirect:/";
        }
    }

    @RequestMapping("userProfile/addFriend/{userId}/{userProfileId}")
    public String addFriend(@PathVariable("userId") Integer userId, @PathVariable("userProfileId") Integer userProfileId, Friends friend)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        friend.setUserId(userId);
        friend.setFriendId(userProfileId);
        friend.setConfirm(false);
        userService.addFriend(friend);
        return "redirect:/userProfile?id="+userProfileId;
    }

    @RequestMapping("userProfile/removeFriend/{userId}/{userProfileId}")
    public String removeFriend(@PathVariable("userId") Integer userId, @PathVariable("userProfileId") Integer userProfileId)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        userService.removeFriend(userId, userProfileId);
        return "redirect:/userProfile?id="+userProfileId;
    }

    @RequestMapping("userInvitedYou/allow/{userId}/{userInvitedYou}")
    public String allowFriendship(@PathVariable("userId") Integer userId, @PathVariable("userInvitedYou")Integer userInvitedYouId)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        Friends friend = new Friends();
        friend.setUserId(userId);
        friend.setFriendId(userInvitedYouId);
        friend.setConfirm(true);
        userService.addFriend(friend);
        userService.allowFriendship(userId, userInvitedYouId, true);
        return "redirect:/userProfile?id="+userId;
    }

    @RequestMapping("userInvitedYou/decline/{userId}/{userInvitedYou}")
    public String declineFriendship(@PathVariable("userId") Integer userId, @PathVariable("userInvitedYou")Integer userInvitedYouId)
    {
        String userEmail = webSecurityConfig.getLoggedUserEmail();
        User user = userService.findUserByEmail(userEmail);
        if(!user.getId().equals(userId))
        {
            return "redirect:/logout";
        }
        userService.declineFriendship(userId, userInvitedYouId);
        return "redirect:/userProfile?id="+userId;
    }

}
