import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ParserTest {
    public static LinkedHashMap<String, Integer> currentCase = new LinkedHashMap<>();
    
    @Test
    public void testBasics(){
        // Basics refers to separator stripping and action/function structure parsing

        Token[][] cases = new Token[][]{
                // pre-lexed:
                // #newline
                // #newline
                // END {}
                {new Token(1,1, Token.TokenType.SEPERATOR), new Token(2,1, Token.TokenType.SEPERATOR), new Token(3,1, Token.TokenType.END), new Token(4,1, Token.TokenType.LEFTBRACE), new Token(5,1, Token.TokenType.RIGHTBRACE)},
                // pre-lexed:
                // function func(a, b){
                // }
                {new Token(1,8, Token.TokenType.FUNCTION), new Token(1,13, Token.TokenType.WORD, "func"), new Token(1,14, Token.TokenType.LEFTPAREN), new Token(1,15, Token.TokenType.WORD, "a"), new Token(1,16, Token.TokenType.COMMA), new Token(1,18, Token.TokenType.WORD, "b"), new Token(1,19, Token.TokenType.RIGHTPAREN), new Token(1,20, Token.TokenType.LEFTBRACE), new Token(1,21, Token.TokenType.SEPERATOR), new Token(2,1, Token.TokenType.RIGHTBRACE), new Token(2,2, Token.TokenType.SEPERATOR)},
        };

        Parser parser = new Parser(new LinkedList<Token>(List.of(cases[0])));
        assertFalse(parser.parse().getEnd().isEmpty()); // END array should have 1 member, proving actions and newline removal works

        parser = new Parser(new LinkedList<Token>(List.of(cases[1])));
        assertEquals("function func(a, b) {\n\t\n}", parser.parse().getFunctions().getFirst().toString());
    }
    
    @Test
    public void testSingleOperations(){
        String identifier = "testSingleOperations";
        int currentCaseNum = 0; // 0 represents no cases tested yet
        currentCase.put(identifier, currentCaseNum);
        
        Map<Token[], Node> cases = new LinkedHashMap<>();
        
        // Begins at case 1
        
        // pre-lexed:
        // ++a
        cases.put(
                new Token[]{new Token(1,2, Token.TokenType.INCREMENT), new Token(1,3, Token.TokenType.WORD, "a")},
                new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new VariableReferenceNode("a"), OperationNode.Operation.PREINCREMENT))
        );
        // pre-lexed:
        // ++$b
        cases.put(
                new Token[]{new Token(1,2, Token.TokenType.INCREMENT), new Token(1, 3, Token.TokenType.DOLLAR), new Token(1,4, Token.TokenType.WORD, "b")},
                new AssignmentNode(new FieldReferenceNode(new VariableReferenceNode("b")), new OperationNode(new FieldReferenceNode(new VariableReferenceNode("b")), OperationNode.Operation.PREINCREMENT))
        );
        // pre-lexed:
        // (++d)
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.LEFTPAREN), new Token(1,3, Token.TokenType.INCREMENT), new Token(1,4, Token.TokenType.WORD, "d"), new Token(1,5, Token.TokenType.RIGHTPAREN)},
                new AssignmentNode(new VariableReferenceNode("d"), new OperationNode(new VariableReferenceNode("d"), OperationNode.Operation.PREINCREMENT))
        );
        // pre-lexed:
        // -5
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.MINUS), new Token(1,2, Token.TokenType.NUMBER, "5")},
                new OperationNode(new ConstantNode<Double>(5.0), OperationNode.Operation.UNARYNEG)
        );
        // pre-lexed:
        // e[++b]
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.WORD, "e"), new Token(1,2, Token.TokenType.LEFTBRACKET), new Token(1,4, Token.TokenType.INCREMENT), new Token(1,5, Token.TokenType.WORD, "b"), new Token(1,6, Token.TokenType.RIGHTBRACKET)},
                new VariableReferenceNode("e", new AssignmentNode(new VariableReferenceNode("b"), new OperationNode(new VariableReferenceNode("b"), OperationNode.Operation.PREINCREMENT)))
        );
        // pre-lexed:
        // $7
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.DOLLAR), new Token(1,2, Token.TokenType.NUMBER, "7")},
                new FieldReferenceNode(new ConstantNode<Double>(7.0))
        );
        // pre-lexed:
        // 5 + 6
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.NUMBER, "5"), new Token(1,3, Token.TokenType.PLUS), new Token(1,5, Token.TokenType.NUMBER, "6")},
                new OperationNode(new ConstantNode<Double>(5.0), OperationNode.Operation.ADD, new ConstantNode<Double>(6.0))
        );
        // pre-lexed:
        // "Hello" " World"
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.STRINGLITERAL, "Hello"), new Token(1,8, Token.TokenType.STRINGLITERAL, " World")},
                new OperationNode(new ConstantNode<String>("Hello"), OperationNode.Operation.CONCATENATION,new ConstantNode<String>(" World"))
        );
        // pre-lexed:
        // 5 > 4
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.NUMBER, "5"), new Token(1,3, Token.TokenType.GREATER), new Token(1,5, Token.TokenType.NUMBER, "4")},
                new OperationNode(new ConstantNode<Double>(5.0), OperationNode.Operation.GREATERTHAN, new ConstantNode<Double>(4.0))
        );
        // pre-lexed:
        // "dog5" ~ `dog[0-9]`
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.STRINGLITERAL, "dog5"), new Token(1,7, Token.TokenType.MATCH), new Token(1,9, Token.TokenType.REGEXLITERAL, "dog[0-9]")},
                new OperationNode(new ConstantNode<String>("dog5"), OperationNode.Operation.MATCH, new RegexNode("dog[0-9]"))
        );
        
        // pre-lexed:
        // 5 in numbers
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.NUMBER, "5"), new Token(1,3, Token.TokenType.IN), new Token(1,6, Token.TokenType.WORD, "numbers")},
                new OperationNode(new ConstantNode<Double>(5.0), OperationNode.Operation.IN, new VariableReferenceNode("numbers"))
        );
        
        // pre-lexed:
        // (1,2,3) in numbers
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.LEFTPAREN), new Token(1,2, Token.TokenType.NUMBER, "1"), new Token(1,3, Token.TokenType.COMMA), new Token(1,4, Token.TokenType.NUMBER, "2"), new Token(1,5, Token.TokenType.COMMA), new Token(1,6, Token.TokenType.NUMBER, "3"), new Token(1,7, Token.TokenType.RIGHTPAREN), new Token(1,9, Token.TokenType.IN), new Token(1,12, Token.TokenType.WORD, "numbers")},
                new OperationNode(new ConstantNode<Double>(3.0), OperationNode.Operation.IN, new OperationNode(new ConstantNode<Double>(2.0), OperationNode.Operation.IN, new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.IN, new VariableReferenceNode("numbers"))))
        );
        
        // pre-lexed:
        // isTested && noBugs
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.WORD, "isTested"), new Token(1,9, Token.TokenType.AND), new Token(1,11, Token.TokenType.WORD, "noBugs")},
                new OperationNode(new VariableReferenceNode("isTested"), OperationNode.Operation.AND, new VariableReferenceNode("noBugs"))
        );
        
        // pre-lexed:
        // isTested ? noBugs : hasBugs
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.WORD, "isTested"), new Token(1,9, Token.TokenType.QUESTION), new Token(1,11, Token.TokenType.WORD, "noBugs"), new Token(1,18, Token.TokenType.COLON), new Token(1,20, Token.TokenType.WORD, "hasBugs")},
                new TernaryNode(new VariableReferenceNode("isTested"), new VariableReferenceNode("noBugs"), new VariableReferenceNode("hasBugs"))        
        );
        
        // pre-lexed:
        // passedTests += 1
        cases.put(
                new Token[]{new Token(1,1, Token.TokenType.WORD, "passedTests"), new Token(1,12, Token.TokenType.ASSIGNADD), new Token(1,15, Token.TokenType.NUMBER, "1")},
                new AssignmentNode(new VariableReferenceNode("passedTests"), new OperationNode(new VariableReferenceNode("passedTests"), OperationNode.Operation.ADD, new ConstantNode<Double>(1.0)))
        );
        
        // use new map
        for (Map.Entry<Token[], Node> entry : cases.entrySet()) {
            currentCase.put(identifier, ++currentCaseNum); // For easily singling out a case while debugging
            
            System.out.println("Testing: " + Arrays.toString(entry.getKey()));
            Parser parser = new Parser(new LinkedList<Token>(List.of(entry.getKey())));
            try {
                assertEquals(entry.getValue(), parser.parseOperation().orElseThrow());
            } catch (AssertionError e){
                System.out.println("Failed on case " + currentCaseNum);
                throw e;
            }
        }
        
    }

    @Test
    public void testMultipleOperations(){
        String identifier = "testMultipleOperations";
        int currentCaseNum = 0; // 0 represents no cases tested yet
        currentCase.put(identifier, currentCaseNum);
        
        // No pre-lexing on this one, too much of a pain
        Map<String, Node> cases = new LinkedHashMap<>();
        
        cases.put(
                "2^3^4",
                new OperationNode(new ConstantNode<Double>(2.0), OperationNode.Operation.EXPONENT, new OperationNode(new ConstantNode<Double>(3.0), OperationNode.Operation.EXPONENT, new ConstantNode<Double>(4.0)))
        );
        cases.put(
                "1 + 2 + 3",
                new OperationNode(new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.ADD, new ConstantNode<Double>(2.0)), OperationNode.Operation.ADD, new ConstantNode<Double>(3.0))
        );
        cases.put(
                "1 + 2 * 3",
                new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.ADD, new OperationNode(new ConstantNode<Double>(2.0), OperationNode.Operation.MULTIPLY, new ConstantNode<Double>(3.0)))
        );
        cases.put(
                "1 < 2 < 3",
                new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(2.0))
        );
        cases.put(
                "unnested ? \"good.\" : forced ? \"fine.\" : \"D:\"",
                new TernaryNode(new VariableReferenceNode("unnested"), new ConstantNode<String>("good."), new TernaryNode(new VariableReferenceNode("forced"), new ConstantNode<String>("fine."), new ConstantNode<String>("D:")))
        );
        cases.put(
                "nested ? forced ? \"fine.\" : \"D:\" : \"good.\"",
                new TernaryNode(new VariableReferenceNode("nested"), new TernaryNode(new VariableReferenceNode("forced"), new ConstantNode<String>("fine."), new ConstantNode<String>("D:")), new ConstantNode<String>("good."))
        );
        cases.put(
                "sanity /= bugs *= 1000",
                new AssignmentNode(new VariableReferenceNode("sanity"), new OperationNode(new VariableReferenceNode("sanity"), OperationNode.Operation.DIVIDE, new AssignmentNode(new VariableReferenceNode("bugs"), new OperationNode(new VariableReferenceNode("bugs"), OperationNode.Operation.MULTIPLY, new ConstantNode<Double>(1000.0)))))
        );
        
        for (Map.Entry<String, Node> entry : cases.entrySet()) {
            currentCase.put(identifier, ++currentCaseNum); // For easily singling out a case while debugging

            System.out.println("Testing: " + entry.getKey());
            Lexer lexer = new Lexer(entry.getKey());
            Parser parser = new Parser(lexer.lex());
            try {
                assertEquals(entry.getValue(), parser.parseOperation().orElseThrow());
            } catch (AssertionError e){
                System.out.println("Failed on case " + currentCaseNum);
                throw e;
            }
        }

    }
    
    @Test public void testFinal(){
        String identifier = "testFinal";
        int currentCaseNum = 0; // 0 represents no cases tested yet
        currentCase.put(identifier, currentCaseNum);

        Map<String, ProgramNode> cases = new LinkedHashMap<>();
        
        
        
        LinkedList<StatementNode> statements = new LinkedList<>(List.of(
                new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(5.0)),
                new AssignmentNode(new VariableReferenceNode("y"), new ConstantNode<Double>(10.0)),
                new AssignmentNode(new VariableReferenceNode("z"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.ADD, new VariableReferenceNode("y"))),
                new FunctionCallNode("print", List.of(new VariableReferenceNode("z")))
        ));
        
        LinkedList<BlockNode> blocks = new LinkedList<>();
        blocks.add(new BlockNode(statements));
        
        cases.put(
                """
                        `[0-9]` {
                            x = 5
                            y = 10
                            z = x + y
                            print(z)
                        }
                        """        
                ,
                new ProgramNode(new LinkedList<>(), new LinkedList<>(), blocks, new LinkedList<>())
                
        );

        // Next Case...
        
        LinkedList<StatementNode> forNodes = new LinkedList<>(List.of(
                new FunctionCallNode("print", List.of(new ConstantNode<String>("i:"), new VariableReferenceNode("i"), new ConstantNode<String>("j:"), new VariableReferenceNode("j")))
        ));
        
        LinkedList<StatementNode> whileNodes = new LinkedList<>(List.of(
                new ASTnode.ForNode(
                        new AssignmentNode(new VariableReferenceNode("j"), new ConstantNode<Double>(1.0)),
                        new OperationNode(new VariableReferenceNode("j"), OperationNode.Operation.LESSOREQUAL, new ConstantNode<Double>(3.0)),
                        new AssignmentNode(new VariableReferenceNode("j"), new OperationNode(new VariableReferenceNode("j"), OperationNode.Operation.POSTINCREMENT)),
                        new BlockNode(forNodes)
                ),
                new AssignmentNode(new VariableReferenceNode("i"), new OperationNode(new VariableReferenceNode("i"), OperationNode.Operation.POSTINCREMENT))
                
        ));
        
        LinkedList<StatementNode> ifNodes = new LinkedList<>(List.of(
                new AssignmentNode(new VariableReferenceNode("i"), new ConstantNode<Double>(1.0)),
                new ASTnode.WhileNode(
                        new OperationNode(new VariableReferenceNode("i"), OperationNode.Operation.LESSOREQUAL, new ConstantNode<Double>(2.0)),
                        new BlockNode(whileNodes)
                )
        ));
        
        statements = new LinkedList<>(List.of(
                new AssignmentNode(new VariableReferenceNode("value"), new ConstantNode<Double>(6.0)),
                new ASTnode.IfNode(
                        new OperationNode(new VariableReferenceNode("value"), OperationNode.Operation.GREATERTHAN, new ConstantNode<Double>(5.0)),
                        new BlockNode(ifNodes)
                )
        ));
        
        cases.put(
                """
                        BEGIN {
                            value = 6
                            if (value > 5) {
                                i = 1
                                while (i <= 2) {
                                    for (j = 1; j <= 3; j++) {
                                        print "i:", i, "j:", j
                                    }
                                    i++
                                }
                            }
                        }
                        """
                ,
                new ProgramNode(new LinkedList<BlockNode>(List.of(new BlockNode(statements))), new LinkedList<>(), new LinkedList<>(), new LinkedList<>())
        );
        
        // Next case..
        
        
        forNodes = new LinkedList<>(List.of(
                new FunctionCallNode("print", List.of(new VariableReferenceNode("key")))
        ));
        LinkedList<StatementNode> elseNodes = new LinkedList<>(List.of(
                new FunctionCallNode("print", List.of(new ConstantNode<String>("Not found")))
        ));
        ifNodes = new LinkedList<>(List.of(
                new FunctionCallNode("print", List.of(new ConstantNode<String>("Found")))     
        ));
        statements = new LinkedList<>(List.of(
                new AssignmentNode(new VariableReferenceNode("arr", new ConstantNode<String>("a")), new ConstantNode<Double>(1.0)),
                new ASTnode.IfNode(
                        new OperationNode(new ConstantNode<String>("a"), OperationNode.Operation.IN, new VariableReferenceNode("arr")),
                        new BlockNode(ifNodes),
                        new ASTnode.IfNode(new BlockNode(elseNodes))
                ),
                new ASTnode.ForNode(
                        new VariableReferenceNode("key"),
                        new VariableReferenceNode("arr"),
                        new BlockNode(forNodes)
                )
        ));
        
        cases.put(
                """
                        END {
                            arr["a"] = 1
                            if ("a" in arr) {
                                print("Found")
                            } else {
                                print("Not found")
                            }
                            for (key in arr) {
                                print(key)
                            }
                        }
                        """
                , 
                new ProgramNode(new LinkedList<>(), new LinkedList<>(List.of(new BlockNode(statements))), new LinkedList<>(), new LinkedList<>())
                
                
        );
        
        
        
        for (Map.Entry<String, ProgramNode> entry : cases.entrySet()) {
            currentCase.put(identifier, ++currentCaseNum); // For easily singling out a case while debugging

            System.out.println("Testing: " + entry.getKey());
            Lexer lexer = new Lexer(entry.getKey());
            Parser parser = new Parser(lexer.lex());
            try {
                assertEquals(entry.getValue(), parser.parse());
            } catch (AssertionError e){
                System.out.println("Failed on case " + currentCaseNum);
                throw e;
            }
        }
    }

    @Test
    public void testTokenManager(){

        TokenManager manager = new TokenManager(new LinkedList<>(List.of(
                new Token(1, 1, Token.TokenType.SEPERATOR), new Token(2, 1, Token.TokenType.SEPERATOR), new Token(3, 1, Token.TokenType.SEPERATOR), new Token(4, 1, Token.TokenType.SEPERATOR), new Token(5, 1, Token.TokenType.SEPERATOR)
        )));
        
        int length = 5;

        for (int i = 0; i < length; i++) {
            assertTrue(manager.moreTokens());
            assertTrue(manager.peek(1).isPresent());
            assertTrue(manager.matchAndRemove(Token.TokenType.SEPERATOR).isPresent());
        }
        
        assertFalse(manager.moreTokens());
    }

}
