package me.pesekjak.hippo.utils.parser;

/**
 * Text analyzer used to easier text parsing.
 */
public class Analyzer {

    private String string;
    private int cursor = 0;
    private int mark = 0;

    public Analyzer(String string) {
        this.string = string;
    }

    /**
     * @return index of the cursor
     */
    public int cursor() {
        return cursor;
    }

    /**
     * Moves the cursor to the new position.
     *
     * @param position new position
     */
    public void cursor(int position) {
        if (position < 0 || position > string.length()) throw new IllegalStateException();
        cursor = position;
    }

    /**
     * Trims the analyzing text.
     */
    public void trim() {
        string = string.trim();
    }

    /**
     * @return next character
     */
    public char peek() {
        return string.charAt(cursor);
    }

    /**
     * Moves cursor one character forward.
     */
    public void eat() {
        if (!canMove()) throw new UnsupportedOperationException();
        cursor++;
    }

    /**
     * Moves cursor one character forward.
     *
     * @return this character
     */
    public char move() {
        if (!canMove()) throw new UnsupportedOperationException();
        char c = peek();
        eat();
        return c;
    }

    /**
     * @return text that has been analyzed so far
     */
    public String analyzed() {
        if (cursor == 0) return "";
        return string.substring(0, cursor);
    }

    /**
     * @return text that is yet left to be analyzed
     */
    public String rest() {
        return string.substring(cursor);
    }

    /**
     * @return how many characters is left
     */
    public int left() {
        return rest().length();
    }

    /**
     * @return whether the cursor can move
     */
    public boolean canMove() {
        return left() > 0;
    }

    /**
     * Marks the current position of the cursor.
     */
    public void mark() {
        mark = cursor;
    }

    /**
     * Moves cursor to the marked position.
     */
    public void moveToMark() {
        cursor = mark;
    }

    /**
     * @return marked position
     */
    public int getMark() {
        return mark;
    }

}
