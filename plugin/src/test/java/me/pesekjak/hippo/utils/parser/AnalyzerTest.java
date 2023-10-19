package me.pesekjak.hippo.utils.parser;

import org.junit.jupiter.api.Test;

public class AnalyzerTest {

    @Test
    public void test() {
        Analyzer analyzer = new Analyzer("Hello World!");

        assert analyzer.peek() == 'H';
        assert analyzer.analyzed().equals("");
        assert analyzer.rest().equals("Hello World!");

        assert analyzer.move() == 'H';
        analyzer.eat();
        assert analyzer.peek() == 'l';
        assert analyzer.analyzed().equals("He");
        assert analyzer.rest().equals("llo World!");

        for (int i = 0; i < 10; i++) analyzer.move();
        assert !analyzer.canMove();
        assert analyzer.analyzed().equals("Hello World!");
    }

    @Test
    public void movement() {
        Analyzer analyzer = new Analyzer("Hello World!");
        analyzer.eat();
        analyzer.mark();
        for (int i = 0; i < 10; i++) analyzer.move();
        analyzer.moveToMark();
        assert analyzer.move() == 'e';
    }

}
