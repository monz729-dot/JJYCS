package com.ysc.lms.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SearchIndexEvent extends ApplicationEvent {
    
    public enum ActionType {
        INDEX, UPDATE, DELETE
    }
    
    public enum EntityType {
        ORDER, USER
    }
    
    private final EntityType entityType;
    private final ActionType actionType;
    private final Long entityId;
    private final Object entity;
    
    public SearchIndexEvent(Object source, EntityType entityType, ActionType actionType, Long entityId, Object entity) {
        super(source);
        this.entityType = entityType;
        this.actionType = actionType;
        this.entityId = entityId;
        this.entity = entity;
    }
    
    public static SearchIndexEvent indexOrder(Object source, Long orderId, Object order) {
        return new SearchIndexEvent(source, EntityType.ORDER, ActionType.INDEX, orderId, order);
    }
    
    public static SearchIndexEvent updateOrder(Object source, Long orderId, Object order) {
        return new SearchIndexEvent(source, EntityType.ORDER, ActionType.UPDATE, orderId, order);
    }
    
    public static SearchIndexEvent deleteOrder(Object source, Long orderId) {
        return new SearchIndexEvent(source, EntityType.ORDER, ActionType.DELETE, orderId, null);
    }
    
    public static SearchIndexEvent indexUser(Object source, Long userId, Object user) {
        return new SearchIndexEvent(source, EntityType.USER, ActionType.INDEX, userId, user);
    }
    
    public static SearchIndexEvent updateUser(Object source, Long userId, Object user) {
        return new SearchIndexEvent(source, EntityType.USER, ActionType.UPDATE, userId, user);
    }
    
    public static SearchIndexEvent deleteUser(Object source, Long userId) {
        return new SearchIndexEvent(source, EntityType.USER, ActionType.DELETE, userId, null);
    }
}