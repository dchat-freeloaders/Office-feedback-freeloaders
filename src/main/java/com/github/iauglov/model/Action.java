package com.github.iauglov.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {

    // ADMIN
    ADMIN("Админ-панель"),
    ADMIN_EVENTS("События"),
    ADMIN_EVENT_LIST("Список событий"),
    ADMIN_EVENT_FEEDBACKS("Список отзывов"),
    ADMIN_EVENT_FEEDBACKS_CONFIRMATION(""),
    ADMIN_EVENT_CREATE("Добавить событие"),
    ADMIN_EVENT_DELETE("Удалить событие"),
    ADMIN_EVENT_DELETE_CONFIRMATION(""),
    ADMIN_EVENT_EDIT("Редактировать событие"),
    ADMIN_EVENT_EDIT_CONFIRMATION(""),

    // USER
    USER_START("В начало"),
    USER_FEEDBACK_CREATE("Оставить фидбек о событии"),
    USER_FEEDBACK_CREATE_CONFIRMATION_FIRST_STEP(""),
    USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP("");

    private final String label;

    public static boolean canProcess(String id) {
        try {
            valueOf(id.toUpperCase());
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    public String asId() {
        return this.name().toLowerCase();
    }
}
