package com.libertaua.bot.enums;

import lombok.Getter;

/**
 * Here are all buttons that doesn't affect results counting
 */
public enum Util implements Button {

    LETSGO("Поїхали!"),
    BACK("Назад"),
    RESTART("Пройти заново"),
    RESULTS("Показати результати"),
    IDEOLOGIES("Показати найближчі ідеології"),
    ARTICLE("Стаття для розуміння політичного спектру"),
    WOMAN("Жіноча версія компасу"),
    MEMES("Зиркнути на меми"),
    CHAT("Обговорити в чатику"),
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
