package com.campus.secondhand.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.MessageDTO;
import com.campus.secondhand.entity.Message;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.MessageMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.MessageService;
import com.campus.secondhand.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void sendMessage(MessageDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        if (userId.equals(dto.getToUserId())) {
            throw new RuntimeException("不能给自己发消息");
        }

        Message message = new Message();
        message.setProductId(dto.getProductId());
        message.setFromUserId(userId);
        message.setToUserId(dto.getToUserId());
        message.setContent(dto.getContent());
        message.setImageUrl(dto.getImageUrl());
        message.setIsRead(0);

        this.save(message);
    }

    @Override
    public PageResult<Map<String, Object>> getMessageList(Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        List<Message> messages = this.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getFromUserId, userId)
                .or()
                .eq(Message::getToUserId, userId)
                .orderByDesc(Message::getCreateTime));

        Map<String, List<Message>> grouped = messages.stream()
                .collect(Collectors.groupingBy(msg -> {
                    Long otherId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
                    return otherId + "_" + msg.getProductId();
                }));

        List<Map<String, Object>> records = new ArrayList<>();
        for (Map.Entry<String, List<Message>> entry : grouped.entrySet()) {
            String key = entry.getKey();
            List<Message> msgs = entry.getValue();
            Message latest = msgs.get(0);

            Long otherId = latest.getFromUserId().equals(userId) ? latest.getToUserId() : latest.getFromUserId();
            Long unreadCount = msgs.stream()
                    .filter(m -> m.getToUserId().equals(userId) && m.getIsRead() == 0)
                    .count();

            Map<String, Object> item = new HashMap<>();
            item.put("key", key);
            item.put("otherUserId", otherId);
            item.put("productId", latest.getProductId());
            item.put("lastMessage", latest.getContent());
            item.put("lastTime", latest.getCreateTime());
            item.put("unreadCount", unreadCount);

            User otherUser = userMapper.selectById(otherId);
            if (otherUser != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", otherUser.getId());
                userMap.put("nickname", otherUser.getNickname());
                userMap.put("avatar", otherUser.getAvatar());
                item.put("otherUser", userMap);
            }

            Product product = productMapper.selectById(latest.getProductId());
            if (product != null) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("title", product.getTitle());
                productMap.put("coverImage", product.getCoverImage());
                productMap.put("price", product.getPrice());
                item.put("product", productMap);
            }

            records.add(item);
        }

        records.sort((a, b) -> {
            if (a.get("lastTime") == null && b.get("lastTime") == null) return 0;
            if (a.get("lastTime") == null) return 1;
            if (b.get("lastTime") == null) return -1;
            return ((Comparable) b.get("lastTime")).compareTo(a.get("lastTime"));
        });

        int total = records.size();
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);

        List<Map<String, Object>> pageRecords = start < total ? records.subList(start, end) : new ArrayList<>();

        return new PageResult<>(pageRecords, (long) total, (long) size, (long) current);
    }

    @Override
    public List<Map<String, Object>> getConversation(Long toUserId, Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        List<Message> messages = this.list(new LambdaQueryWrapper<Message>()
                .and(wrapper -> wrapper
                        .eq(Message::getFromUserId, userId).eq(Message::getToUserId, toUserId)
                        .or()
                        .eq(Message::getFromUserId, toUserId).eq(Message::getToUserId, userId))
                .eq(Message::getProductId, productId)
                .orderByAsc(Message::getCreateTime));

        List<Map<String, Object>> records = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", message.getId());
            item.put("fromUserId", message.getFromUserId());
            item.put("toUserId", message.getToUserId());
            item.put("content", message.getContent());
            item.put("imageUrl", message.getImageUrl());
            item.put("isRead", message.getIsRead());
            item.put("createTime", message.getCreateTime());
            item.put("isMe", message.getFromUserId().equals(userId));

            records.add(item);
        }

        return records;
    }

    @Override
    public void markAsRead(Long toUserId, Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        List<Message> messages = this.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getFromUserId, toUserId)
                .eq(Message::getToUserId, userId)
                .eq(Message::getProductId, productId)
                .eq(Message::getIsRead, 0));

        for (Message message : messages) {
            message.setIsRead(1);
        }

        if (!messages.isEmpty()) {
            this.updateBatchById(messages);
        }
    }

    @Override
    public Long getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return 0L;
        }

        return this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0));
    }
}
