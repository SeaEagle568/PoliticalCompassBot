package com.newsforright.bot.enums;

/**
 * Interface for buttons (Util and Answers)
 * Can return a button by text
 *
 * @author seaeagle
 */
public interface Button {
    static Button getButton(String text) {
        return switch (text) {
            case "Однозначно так" -> Answer.STRONG_AGREE;
            case "Скоріше так" -> Answer.WEAK_AGREE;
            case "Важко відповісти" -> Answer.DONT_KNOW;
            case "Скоріше ні" -> Answer.WEAK_DISAGREE;
            case "Однозначно ні" -> Answer.STRONG_DISAGREE;
            case "Назад" -> Util.BACK;
            default -> Util.NULL;
        };
    }
    String getButtonType();
    String getText();
}
