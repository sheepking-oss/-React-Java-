package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.MessageDTO;
import com.campus.secondhand.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService extends IService<Message> {

    void sendMessage(MessageDTO dto);

    PageResult<Map<String, Object>> getMessageList(Integer current, Integer size);

    List<Map<String, Object>> getConversation(Long toUserId, Long productId);

    void markAsRead(Long toUserId, Long productId);

    Long getUnreadCount();
}
