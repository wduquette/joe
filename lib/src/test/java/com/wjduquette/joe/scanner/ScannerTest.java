package com.wjduquette.joe.scanner;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkList;
import static com.wjduquette.joe.scanner.TokenType.*;

public class ScannerTest extends Ted {
    private Scanner scanner;
    private final List<String> errors = new ArrayList<>();

    @Test public void testWindow() {
        test("testWindow");
        scan("+ - *");
        check(scanner.isPrimed()).eq(true);

        check(scanner.previous()).eq(null);
        check(scanner.peek().type()).eq(PLUS);
        check(scanner.peekNext().type()).eq(MINUS);
        check(scanner.check(PLUS)).eq(true);
        check(scanner.check(EOF)).eq(false);
        check(scanner.checkTwo(PLUS, MINUS)).eq(true);
        check(scanner.checkTwo(PLUS, EOF)).eq(false);
        check(scanner.isAtEnd()).eq(false);

        scanner.advance();
        check(scanner.previous().type()).eq(PLUS);
        check(scanner.peek().type()).eq(MINUS);
        check(scanner.peekNext().type()).eq(TokenType.STAR);
        check(scanner.check(MINUS)).eq(true);
        check(scanner.checkTwo(MINUS, STAR)).eq(true);
        check(scanner.isAtEnd()).eq(false);

        scanner.advance();
        check(scanner.previous().type()).eq(MINUS);
        check(scanner.peek().type()).eq(STAR);
        check(scanner.peekNext().type()).eq(TokenType.EOF);
        check(scanner.check(STAR)).eq(true);
        check(scanner.checkTwo(STAR, EOF)).eq(true);
        check(scanner.isAtEnd()).eq(false);

        scanner.advance();
        check(scanner.previous().type()).eq(STAR);
        check(scanner.peek().type()).eq(EOF);
        check(scanner.peekNext().type()).eq(TokenType.EOF);
        check(scanner.check(EOF)).eq(true);
        check(scanner.checkTwo(EOF, EOF)).eq(true);
        check(scanner.isAtEnd()).eq(true);
    }

    @Test public void testMatch() {
        test("testError_first");
        scan("+ - *");
        check(scanner.isPrimed()).eq(true);

        check(scanner.match(NULL)).eq(false);
        check(scanner.match(PLUS)).eq(true);
        check(scanner.match(NULL)).eq(false);
        check(scanner.match(MINUS)).eq(true);
        check(scanner.match(NULL)).eq(false);
        check(scanner.match(STAR)).eq(true);
        check(scanner.match(NULL)).eq(false);
        check(scanner.match(EOF)).eq(true);
    }

    @Test public void testError_first() {
        test("testError_first");
        scan("^ + - *");
        check(scanner.isPrimed()).eq(true);
        check(scanner.check(PLUS)).eq(true); // error skipped
        checkList(errors)
            .items("At '^', unexpected character.");
    }

    @Test public void testMultipleErrors() {
        test("testMultipleErrors");
        scan("^ + ^ - ^ * ^");
        check(scanner.isPrimed()).eq(true);
        check(scanner.match(PLUS)).eq(true);
        check(scanner.match(MINUS)).eq(true);
        check(scanner.match(STAR)).eq(true);
        check(scanner.match(EOF)).eq(true);
    }

    @Test public void testConsume_good() {
        test("testConsume_good");
        scan("+ - *");
        check(scanner.isPrimed()).eq(true);
        scanner.consume(PLUS, "plus");
        scanner.consume(MINUS, "minus");
        scanner.consume(STAR, "start");
        check(scanner.isAtEnd()).eq(true);
        check(errors.isEmpty()).eq(true);
    }

    @Test public void testConsume_error() {
        test("testConsume_error");
        scan("+ - *");
        check(scanner.isPrimed()).eq(true);
        scanner.consume(TRUE, "expected true.");
        checkList(errors)
            .items("error at '+', expected true.");
    }

    //-------------------------------------------------------------------------
    // Helpers

    @SuppressWarnings("unused")
    private void dumpWindow() {
        println(
            "prev=" + (scanner.previous() != null
                ? scanner.previous().type() : "null") +
            " peek=" + scanner.peek().type() +
            " next=" + scanner.peekNext().type());
    }

    private void scan(String input) {
        errors.clear();
        var source = new SourceBuffer("-", input);
        scanner = new Scanner(source, this::errorHandler);
        scanner.prime();
    }

    private void errorHandler(SourceBuffer.Span span, String message) {
        errors.add(message);
    }
}
