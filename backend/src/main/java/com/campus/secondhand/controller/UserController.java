package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.UserLoginDTO;
import com.campus.secondhand.dto.UserRegisterDTO;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody UserLoginDTO dto) {
        Map<String, Object> result = userService.login(dto);
        return Result.success(result);
    }

    @GetMapping("/info")
    public Result<User> info() {
        User user = userService.getCurrentUser();
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody User user) {
        userService.updateUserInfo(user);
        return Result.success();
    }

    @PostMapping("/updatePassword")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.updatePassword(oldPassword, newPassword);
        return Result.success();
    }
}
