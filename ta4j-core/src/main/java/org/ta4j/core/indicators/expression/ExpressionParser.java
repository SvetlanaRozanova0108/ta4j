/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent lexer and parser for indicator expressions.
 *
 * <p>
 * Two interchangeable notations are accepted and may be mixed:
 * <ul>
 * <li>function-call notation: {@code SMA(Close, 10)}</li>
 * <li>Lisp-like prefix notation: {@code (SMA Close 10)}, {@code (> (SMA (RSI
 * (AVG High Low) 5) 10) (RSI (AVG High Low) 5))}</li>
 * </ul>
 *
 * <p>
 * Tokens are: identifiers (letters, digits, '_', '.'), operator symbols
 * ({@code + - * / % < > = <= >= != <>}), numeric literals (including negative
 * ones), parentheses and commas. Commas and whitespace are free separators.
 * Nesting depth is unlimited.
 */
final class ExpressionParser {

    private ExpressionParser() {
    }

    // ------------------------------------------------------------------ AST

    /** Base class of all parsed expression nodes. */
    abstract static class Node {
        final int position;

        Node(int position) {
            this.position = position;
        }
    }

    /** A numeric literal, e.g. {@code 5} or {@code -1.5}. */
    static final class NumberNode extends Node {
        final double value;

        NumberNode(double value, int position) {
            super(position);
            this.value = value;
        }
    }

    /** A bare name: data field, parameter or nullary function. */
    static final class NameNode extends Node {
        final String name;

        NameNode(String name, int position) {
            super(position);
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** A function call: {@code name(arg1, arg2, ...)} or {@code (name arg1 arg2 ...)}. */
    static final class CallNode extends Node {
        final String name;
        final int namePosition;
        final List<Node> args;

        CallNode(String name, int namePosition, List<Node> args, int position) {
            super(position);
            this.name = name;
            this.namePosition = namePosition;
            this.args = args;
        }

        @Override
        public String toString() {
            return name + args;
        }
    }

    // --------------------------------------------------------------- tokens

    private enum TokenType {
        NAME, NUMBER, LPAREN, RPAREN, EOF
    }

    private static final class Token {
        final TokenType type;
        final String text;
        final double number;
        final int position;

        Token(TokenType type, String text, double number, int position) {
            this.type = type;
            this.text = text;
            this.number = number;
            this.position = position;
        }
    }

    // --------------------------------------------------------------- lexing

    private static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int length = expression.length();
        while (i < length) {
            char c = expression.charAt(i);
            if (Character.isWhitespace(c) || c == ',') {
                i++;
            } else if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", 0, i));
                i++;
            } else if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", 0, i));
                i++;
            } else if (isNumberStart(expression, i)) {
                int start = i;
                i++; // optional minus
                while (i < length && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    i++;
                }
                if (i < length && (expression.charAt(i) == 'e' || expression.charAt(i) == 'E')) {
                    i++;
                    if (i < length && (expression.charAt(i) == '+' || expression.charAt(i) == '-')) {
                        i++;
                    }
                    while (i < length && Character.isDigit(expression.charAt(i))) {
                        i++;
                    }
                }
                String text = expression.substring(start, i);
                double value;
                try {
                    value = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    throw new ExpressionValidationException("invalid number '" + text + "'", start);
                }
                tokens.add(new Token(TokenType.NUMBER, text, value, start));
            } else if (isNameStart(c)) {
                int start = i;
                while (i < length && isNamePart(expression.charAt(i))) {
                    i++;
                }
                tokens.add(new Token(TokenType.NAME, expression.substring(start, i), 0, start));
            } else {
                throw new ExpressionValidationException("unexpected character '" + c + "'", i);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", 0, length));
        return tokens;
    }

    private static boolean isNumberStart(String s, int i) {
        char c = s.charAt(i);
        if (Character.isDigit(c)) {
            return true;
        }
        // a minus only starts a negative literal when followed by a digit and the
        // previous token cannot end an expression that a number could continue
        // (we simply require a digit after the minus; '-' as an operator inside a
        // name like "A-B" is not used, symbol names are registered separately)
        return c == '-' && i + 1 < s.length()
                && (Character.isDigit(s.charAt(i + 1)) || (s.charAt(i + 1) == '.' && i + 2 < s.length()
                        && Character.isDigit(s.charAt(i + 2))));
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_' || isOperatorChar(c);
    }

    private static boolean isNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || isOperatorChar(c);
    }

    private static boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '<' || c == '>' || c == '='
                || c == '!';
    }

    // --------------------------------------------------------------- parsing

    /**
     * Parses the given expression into a node tree.
     *
     * @param expression the expression string
     * @return the root node
     * @throws ExpressionValidationException on any syntax error
     */
    static Node parse(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new ExpressionValidationException("expression must not be empty");
        }
        List<Token> tokens = tokenize(expression);
        int[] cursor = { 0 };
        Node node = parseNode(tokens, cursor);
        if (peek(tokens, cursor).type != TokenType.EOF) {
            throw new ExpressionValidationException("unexpected token '" + peek(tokens, cursor).text + "'",
                    peek(tokens, cursor).position);
        }
        return node;
    }

    private static Token peek(List<Token> tokens, int[] cursor) {
        return tokens.get(cursor[0]);
    }

    private static Token next(List<Token> tokens, int[] cursor) {
        return tokens.get(cursor[0]++);
    }

    private static Node parseNode(List<Token> tokens, int[] cursor) {
        Token token = peek(tokens, cursor);
        switch (token.type) {
        case NUMBER:
            next(tokens, cursor);
            return new NumberNode(token.number, token.position);
        case NAME:
            next(tokens, cursor);
            Token after = peek(tokens, cursor);
            // function-call notation requires the '(' to immediately follow the
            // name; with whitespace in between the name is a plain atom and the
            // parenthesized node that follows becomes its own argument
            // (Lisp notation: "(SMA (RSI Close 5) 10)")
            if (after.type == TokenType.LPAREN && after.position == token.position + token.text.length()) {
                next(tokens, cursor); // consume '('
                List<Node> args = parseArgs(tokens, cursor, token.text, token.position);
                return new CallNode(token.text, token.position, args, token.position);
            }
            return new NameNode(token.text, token.position);
        case LPAREN:
            return parseList(tokens, cursor);
        default:
            throw new ExpressionValidationException(
                    token.type == TokenType.RPAREN ? "unexpected ')'" : "unexpected end of expression",
                    token.position);
        }
    }

    /**
     * Parses a Lisp-like list {@code (head arg ...)} after '(' was consumed. A
     * list whose head is not a plain name is treated as a parenthesized single
     * expression.
     */
    private static Node parseList(List<Token> tokens, int[] cursor) {
        int openPosition = next(tokens, cursor).position; // consume '('
        if (peek(tokens, cursor).type == TokenType.RPAREN) {
            throw new ExpressionValidationException("empty parentheses '()'", openPosition);
        }
        Node head = parseNode(tokens, cursor);
        if (!(head instanceof NameNode)) {
            // parenthesized expression, e.g. ((SMA Close 10))
            Token after = peek(tokens, cursor);
            if (after.type != TokenType.RPAREN) {
                throw new ExpressionValidationException(
                        "expected ')' after parenthesized expression but found '" + after.text + "'",
                        after.position);
            }
            next(tokens, cursor); // consume ')'
            return head;
        }
        List<Node> args = parseArgs(tokens, cursor, ((NameNode) head).name, ((NameNode) head).position);
        return new CallNode(((NameNode) head).name, ((NameNode) head).position, args, openPosition);
    }

    /**
     * Parses the remaining arguments and the closing ')' of a call.
     *
     * @param tokens      the token list
     * @param cursor      the cursor
     * @param calleeName  the name of the callee (for error messages)
     * @param position    the position of the callee (for error messages)
     * @return the argument nodes
     */
    private static List<Node> parseArgs(List<Token> tokens, int[] cursor, String calleeName, int position) {
        List<Node> args = new ArrayList<>();
        while (true) {
            Token token = peek(tokens, cursor);
            if (token.type == TokenType.EOF) {
                throw new ExpressionValidationException("missing ')' after '" + calleeName + "'", position);
            }
            if (token.type == TokenType.RPAREN) {
                next(tokens, cursor); // consume ')'
                return args;
            }
            args.add(parseNode(tokens, cursor));
        }
    }
}
