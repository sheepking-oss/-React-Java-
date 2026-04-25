package com.campus.secondhand.controller;

import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.MessageDTO;
import com.campus.secondhand.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result<Void> send(@Validated @RequestBody MessageDTO dto) {
        messageService.sendMessage(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = messageService.getMessageList(current, size);
        return Result.success(result);
    }

    @GetMapping("/conversation")
    public Result<List<Map<String, Object>>> conversation(
            @RequestParam Long toUserId,
            @RequestParam Long productId) {
        List<Map<String, Object>> result = messageService.getConversation(toUserId, productId);
        return Result.success(result);
    }

    @PostMapping("/markRead")
    public Result<Void> markRead(@RequestParam Long toUserId, @RequestParam Long productId) {
        messageService.markAsRead(toUserId, productId);
        return Result.success();
    }

    @GetMapping("/unreadCount")
    public Result<Map<String, Long>> unreadCount() {
        Long count = messageService.getUnreadCount();
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}
