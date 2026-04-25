package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.dto.UserLoginDTO;
import com.campus.secondhand.dto.UserRegisterDTO;
import com.campus.secondhand.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> login(UserLoginDTO dto);

    void register(UserRegisterDTO dto);

    User getCurrentUser();

    void updateUserInfo(User user);

    void updatePassword(String oldPassword, String newPassword);

    void updateCreditScore(Long userId, int score);
}
