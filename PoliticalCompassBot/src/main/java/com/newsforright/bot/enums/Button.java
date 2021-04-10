package com.newsforright.bot.enums;

public interface Button {
    static Button getButton(String text) {
        switch (text){
            case "Однозначно так":
                return Answer.STRONG_AGREE;
            case "Скоріше так":
                return Answer.WEAK_AGREE;
            case "Важко відповісти":
                return Answer.DONT_KNOW;
            case "Скоріше ні":
                return Answer.WEAK_DISAGREE;
            case "Однозначно ні":
                return Answer.STRONG_DISAGREE;
            case "Назад":
                return Util.BACK;
            default:
                return Util.NULL;
        }
    }
    String getButtonType();
}
