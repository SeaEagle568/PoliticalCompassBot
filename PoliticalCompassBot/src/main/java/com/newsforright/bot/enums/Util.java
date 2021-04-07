package com.newsforright.bot.enums;

public enum Util {
    LETSGO("Поїхали!"),
    BACK("Назад")
    ;

    private final String text;

    Util(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
