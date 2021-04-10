package com.newsforright.bot.util;

/**
 * Basic std::pair implementation
 * @param <F> First element type
 * @param <S> Second element type
 */
public class Pair<F, S> {
    public F first;
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}