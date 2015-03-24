/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

/**
 * The Either monad is used to chain methods returning either a Left or a
 * Right return type (often Left is an Exception or Error condition, Right
 * is a successful result), allowing callers to perform exception handling
 * in-place.
 */
public class Either<Left, Right> {

    /**
     * The Fold lambda is used to process one of the Left or Right results,
     * returning a common result type.
     */
    public interface Fold<ValueType, ResultType> {
        ResultType fold(ValueType value);
    }

    /**
     * Returns true if the result is of type Left
     */
    public boolean isLeft() {
        return this.left != null;
    }

    /**
     * Returns true if the result is of type Right
     */
    public boolean isRight() {
        return this.right != null;
    }

    /**
     * Applies the correct folding function for the partial result, returning
     * the value returned by the folding function.
     *
     * @param ifLeft The folding function to call if the result is of type Left
     * @param ifRight The function to call if the result is of type Right
     * @return The result of the folding function
     */
    public <V> V fold(Fold<Left, V> ifLeft, Fold<Right, V> ifRight) {
        if (isLeft()) return ifLeft.fold(this.left);
        else return ifRight.fold(this.right);
    }

    /**
     * The result value, if isLeft() returns true.
     */
    public final Left left;

    /**
     * The result value, if isRight() returns true.
     */
    public final Right right;

    /**
     * Returns a new Either instance with the Left return value.
     *
     * @param value The result value of type Left.
     * @return An Either for the Left type.
     */
    @SuppressWarnings("unchecked")
    public static <L, R> Either<L, R> left(L value) {
        return (Either<L, R>)new Either(value, null);
    }

    /**
     * Returns a new Either instance with the Right return value.
     *
     * @param value The result value of type Right.
     * @return An Either for the Right type.
     */
    @SuppressWarnings("unchecked")
    public static <L, R> Either<L, R> right(R value) {
        return (Either<L, R>)new Either(null, value);
    }

    private Either(Left left, Right right) {
        this.left = left;
        this.right = right;
    }
}
