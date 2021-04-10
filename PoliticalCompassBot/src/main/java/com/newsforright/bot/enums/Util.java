package com.newsforright.bot.enums;

public enum Util implements Button{
    LETSGO("Поїхали!"),
    BACK("Назад"),
    RESTART("Пройти заново"),
    NULL("")
    ;

    private final String text;

    Util(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String getButtonType() {
        return "UTIL";
    }
}
