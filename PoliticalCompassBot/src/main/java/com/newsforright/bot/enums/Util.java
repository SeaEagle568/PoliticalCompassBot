package com.newsforright.bot.enums;

import lombok.Getter;

/**
 * Here are all buttons that doesn't affect results counting
 */
public enum Util implements Button {

    LETSGO("Поїхали!"),
    BACK("Назад"),
    RESTART("Пройти заново"),
    RESULTS("Показати результати"),
    NULL("")
    ;

    @Getter
    private final String text;

    @Override
    public String getButtonType() {
        return "UTIL";
    }

    Util(String text) {
        this.text = text;
    }
}
