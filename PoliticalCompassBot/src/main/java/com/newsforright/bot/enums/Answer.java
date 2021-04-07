package com.newsforright.bot.enums;

public enum Answer {
    STRONG_AGREE  ("Однозначно так", 2, -2),
    WEAK_AGREE("Скоріше так",1, -1),
    DONT_KNOW ("Важко відповісти", 0, 0),
    WEAK_DISAGREE ("Скоріше ні",-1, 1),
    STRONG_DISAGREE ("Однозначно ні",-2, 2)
    ;


    private final int value;
    private final int invertedValue;
    private final String text;

    Answer(String text, int value, int inv) {
        this.text = text;
        this.value = value;
        this.invertedValue = inv;
    }

    public int getValue(Boolean inverted) {
        if (inverted) return this.invertedValue;
        return this.value;
    }
    public String getText() {
        return this.text;
    }
}
