import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LexerTest {
    
    @Test
    public void testStringHandler(){
        ArrayList<String> cases = new ArrayList<>();
        cases.add("TestTestTest\nTestTest\nTest\n");
        cases.add("Test Test");
        cases.add("");

        for (String input : cases) {
            StringHandler handler = new StringHandler(input);
            assertEquals(input.substring(0,0), handler.peekString(0));
            
            int i = 0;
            while(!handler.isDone()){
                assertEquals(handler.getChar(), input.charAt(i++));
            }
            
        }
    }
    
    @Test
    public void testLexer(){
        HashMap<String, LinkedList<Token>> cases = new HashMap<String, LinkedList<Token>>();
        LinkedList<Token> token;
        cases.put("\n", 
                new LinkedList<Token>(List.of(
                        new Token(1, 1, Token.TokenType.SEPERATOR)
            ))
        );

        cases.put("1\ntwo\n3hree\n4 five 6", 
                new LinkedList<Token>(List.of(
                        new Token(1, 1, Token.TokenType.NUMBER, "1"),
                        new Token(1, 2, Token.TokenType.SEPERATOR),
                        new Token(2, 3, Token.TokenType.WORD, "two"),
                        new Token(2, 4, Token.TokenType.SEPERATOR),
                        new Token(3, 1, Token.TokenType.NUMBER, "3"),
                        new Token(3, 5, Token.TokenType.WORD, "hree"),
                        new Token(3, 6, Token.TokenType.SEPERATOR),
                        new Token(4, 1, Token.TokenType.NUMBER, "4"),
                        new Token(4, 6, Token.TokenType.WORD, "five"),
                        new Token(4, 8, Token.TokenType.NUMBER, "6"),
                        new Token(4, 9, Token.TokenType.SEPERATOR)) )
        );

        cases.put("2.632\n#nothing", 
                new LinkedList<Token>(List.of(
                        new Token(1, 5, Token.TokenType.NUMBER, "2.632"),
                        new Token(1, 6, Token.TokenType.SEPERATOR))
                ));

        cases.put("while (i < 10) next;",
                new LinkedList<Token>(List.of(
                        new Token(1, 5, Token.TokenType.WHILE, "while"),
                        new Token(1, 7, Token.TokenType.LEFTPAREN, "("),
                        new Token(1, 8, Token.TokenType.WORD, "i"),
                        new Token(1, 10, Token.TokenType.LESS, "<"),
                        new Token(1, 13, Token.TokenType.NUMBER, "10"),
                        new Token(1, 14, Token.TokenType.RIGHTPAREN, ")"),
                        new Token(1, 19, Token.TokenType.NEXT, "next"),
                        new Token(1, 20, Token.TokenType.SEPERATOR)
                ))
        );

        cases.put("#nothing\ni++; i--;",
                new LinkedList<Token>(List.of(
                        new Token(1,1, Token.TokenType.SEPERATOR),
                        new Token(2, 1, Token.TokenType.WORD, "i"),
                        new Token(2, 3, Token.TokenType.INCREMENT, "++"),
                        new Token(2, 4, Token.TokenType.SEPERATOR),
                        new Token(3, 2, Token.TokenType.WORD, "i"),
                        new Token(3, 4, Token.TokenType.DECREMENT, "--"),
                        new Token(3, 5, Token.TokenType.SEPERATOR)
                ))
        );

        cases.put("`[a-z]+` \"hello\"",
                new LinkedList<Token>(List.of(
                        new Token(1, 8, Token.TokenType.REGEXLITERAL, "[a-z]+"),
                        new Token(1, 16, Token.TokenType.STRINGLITERAL, "hello"),
                        new Token(1, 17, Token.TokenType.SEPERATOR)
                ))
        );

        // THE TEST BELOW BREAKS JUNIT !!! The expected is identical to the given, but it still fails, even down to the newline type the test should be passing. 
        // InteliJ even says it outright when inspecting the results. One day this will make sense. I tried .toString comparisons in the Token.equals() AND 
        // manually checking everything

        // I'm counting \n as two characters because I feel like that might be crucial in some debugging situations. That doesn't affect the junit 
        // weirdness at all, just have to get rid of a lineCol[COL]++ in the \abc bit of the escape logic.
        
        cases.put("\"Hello\\nAWK!\"",
                new LinkedList<Token>(List.of(
                        new Token(1, 13, Token.TokenType.STRINGLITERAL, "Hello\nAWK!"),
                        new Token(1, 14, Token.TokenType.SEPERATOR)
                ))
        );

        cases.put("\"Hello\\\"AWK\\\"World!\"",
                new LinkedList<Token>(List.of(
                        new Token(1, 20, Token.TokenType.STRINGLITERAL, "Hello\"AWK\"World!"),
                        new Token(1, 21, Token.TokenType.SEPERATOR)
                ))
        );
        


        for(String input : cases.keySet()){
            LinkedList<Token> expectedOutput = cases.get(input);
            LinkedList<Token> output = ( new Lexer(input) ).lex();
            
            assertEquals(expectedOutput, output);
        }
    }
    
    
}
