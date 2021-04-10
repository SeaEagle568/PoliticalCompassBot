package com.newsforright.bot.enums;

public enum Answer implements Button{
    STRONG_AGREE  ("Однозначно так", 2),
    WEAK_AGREE("Скоріше так",1 ),
    DONT_KNOW ("Важко відповісти", 0),
    WEAK_DISAGREE ("Скоріше ні",-1),
    STRONG_DISAGREE ("Однозначно ні",-2)
    ;


    private final int value;
    private final int invertedValue;
    private final String text;

    Answer(String text, int value) {
        this.text = text;
        this.value = value;
        this.invertedValue = -value;
    }

    public int getValue(Boolean inverted) {
        if (inverted) return this.invertedValue;
        return this.value;
    }
    public String getText() {
        return this.text;
    }

    @Override
    public String getButtonType() {
        return "ANSWER";
    }
}
