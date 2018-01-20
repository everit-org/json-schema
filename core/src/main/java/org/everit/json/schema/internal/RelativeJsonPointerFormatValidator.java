package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;
import org.json.JSONPointer;

public class RelativeJsonPointerFormatValidator implements FormatValidator {

    private static class ParseException extends Exception {

        public ParseException(String input) {
            super(String.format("[%s] is not a valid relative JSON Pointer", input));
        }
    }

    private static final class Parser {

        private static boolean isDigit(char c) {
            return '0' <= c && c <= '9';
        }

        private String input;

        private int pos = 0;

        public Parser(String input) {
            this.input = input;
        }

        public void parse() throws ParseException {
            parseUpwardsStepCount();
            parseJsonPointer();
            parseTrailingHashmark();
        }

        private void parseTrailingHashmark() throws ParseException {
            if (pos == input.length()) {
                return;
            }
            if (pos == input.length() - 1 && input.charAt(pos) == '#') {
                return;
            }
            fail();
        }

        private char curr() {
            return input.charAt(pos++);
        }

        private void parseUpwardsStepCount() throws ParseException {
            if (!isDigit(curr())) {
                fail();
            }
            if (pos == input.length()) {
                return;
            }
            for (char current = curr(); isDigit(current) && pos < input.length() - 1; current = curr())
                ;
            pos--;
        }

        private void fail() throws ParseException {
            throw new ParseException(input);
        }

        private void parseJsonPointer() throws ParseException {
            StringBuilder sb = new StringBuilder();
            char current;
            while (pos < input.length() && (current = curr()) != '#') {
                sb.append(current);
            }
            String pointer = sb.toString();
            if (pointer.length() == 0) {
                return;
            }
            if (pointer.startsWith("#")) {
                fail();
            }
            try {
                new JSONPointer(pointer);
            } catch (IllegalArgumentException e) {
                fail();
            }
        }
    }

    @Override

    public Optional<String> validate(String subject) {
        try {
            new Parser(subject).parse();
        } catch (ParseException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    @Override public String formatName() {
        return "relative-json-pointer";
    }
}
