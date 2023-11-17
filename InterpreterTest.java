import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InterpreterTest {
    
//    @Test
//    public void testKnownFunctions(){
//        Interpreter interpreter = new Interpreter();
//    }
    
    private void testBuiltin(){
        
    }
    
    @Test
    public void testSingleNode(){
        Interpreter interpreter = new Interpreter(List.of("Test file ha ha"));
        
        // Test assignment
        Node node = new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(1.0));
        InterpreterDataType result = interpreter.getIDT(node, null);
        assertEquals(new InterpreterDataType("1.0"), result);
        
        // Test math
        node = new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.ADD, new ConstantNode<Double>(2.0)));
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("3.0"), result);
        
        // Test unary operators
        node = new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.UNARYNEG);
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("-1.0"), result);
        
        node = new OperationNode(new ConstantNode<String>("     100%"), OperationNode.Operation.UNARYPOS);
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("100.0"), result);
        
        // Test pre operators
        node = new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.PREINCREMENT));
        result = interpreter.getIDT(node, null);

        assertEquals(new InterpreterDataType("4.0"), result);
        
        // Test post operators
        node = new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.POSTINCREMENT));
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("4.0"), result); // Should return the value from before
        
        result = interpreter.getIDT(new VariableReferenceNode("x"), null);
        
        assertEquals(new InterpreterDataType("5.0"), result); // Should be incremented
        
        // Test reassignment
        node = new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<String>("Hello"));
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("Hello"), result);
        
        node = new VariableReferenceNode("x");
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("Hello"), result);
        
        // Test boolean operations
        node = new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(2.0));
        node = new AssignmentNode(new VariableReferenceNode("y"), node);
        result = interpreter.getIDT(node, null);
        node = new VariableReferenceNode("y");
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("1"), result);
        
        // Test ternary
        node = new TernaryNode(new ConstantNode<Boolean>(false), new ConstantNode<Double>(1.0), new ConstantNode<Double>(2.0));
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("2.0"), result);

        // Test field reference
        node = new FieldReferenceNode(new ConstantNode<Double>(0.0));
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("Test file ha ha"), result);
        
        // Test field assignment
        node = new AssignmentNode((FieldReferenceNode) node, new ConstantNode<String>("Lets change things up"));
        interpreter.getIDT(node, null); // Change $0 to "Lets change things up"
        result = interpreter.getIDT(new FieldReferenceNode(new ConstantNode<Double>(2.0)), null);
        
        assertEquals(new InterpreterDataType("change"), result);
        
        // Test match (also regex literal)
        node = new OperationNode(new ConstantNode<String>("Hello"), OperationNode.Operation.MATCH, new RegexNode("[A-Za-z]+"));
        result = interpreter.getIDT(node, null);
        
        assertEquals(new InterpreterDataType("1"), result);

        node = new OperationNode(new ConstantNode<String>("Hello"), OperationNode.Operation.MATCH, new RegexNode("[0-9]+"));
        result = interpreter.getIDT(node, null);

        assertEquals(new InterpreterDataType("0"), result);
        
        // Test array
        node = new AssignmentNode(new VariableReferenceNode("array", new ConstantNode<Double>(0.0)), new ConstantNode<Double>(1.0));
        interpreter.getIDT(node, null); // Assign 1.0 to array[0]
        result = interpreter.getIDT(new VariableReferenceNode("array", new ConstantNode<Double>(0.0)), null);
        
        assertEquals(new InterpreterDataType("1.0"), result);
        
        result = interpreter.getIDT(new OperationNode(new ConstantNode<Double>(1.0), OperationNode.Operation.IN, new VariableReferenceNode("array")), null);
        result = interpreter.getIDT(new OperationNode(new ConstantNode<Double>(2.0), OperationNode.Operation.IN, new VariableReferenceNode("array")), null);
    }

    @Test
    public void testIf(){
        Interpreter interpreter = new Interpreter(List.of("Test file ha ha"));

        // run = true
        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("run"), new ConstantNode<Boolean>(true)), null);

        ASTnode.IfNode elseNode = new ASTnode.IfNode(new BlockNode(new LinkedList<>(
                List.of(new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(2.0)))
        )));

        ASTnode.IfNode ifNode = new ASTnode.IfNode(new VariableReferenceNode("run"), new BlockNode(new LinkedList<>(
                List.of(new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(1.0)))
        )), elseNode);

        interpreter.evaluateStatement(ifNode, null);

        InterpreterDataType result = interpreter.getIDT(new VariableReferenceNode("x"), null);
        assertEquals(new InterpreterDataType("1.0"), result);

        // run = false
        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("run"), new ConstantNode<Boolean>(false)), null);
        interpreter.evaluateStatement(ifNode, null);

        result = interpreter.getIDT(new VariableReferenceNode("x"), null);
        assertEquals(new InterpreterDataType("2.0"), result);

    }

    // Continue tested in this one too
    @Test
    public void testFor(){
        Interpreter interpreter = new Interpreter(List.of("Test file ha ha"));

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("y"), new ConstantNode<Double>(0.0)), null); // y = 0;
        ASTnode.ForNode forNode = new ASTnode.ForNode(
                new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(0.0)), // x = 0
                new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(10.0)), // x < 10
                new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.POSTINCREMENT)), // x++
                new BlockNode(new LinkedList<>(List.of(
                        new AssignmentNode(new VariableReferenceNode("y"), new OperationNode(new VariableReferenceNode("y"), OperationNode.Operation.POSTDECREMENT)) // y--
                )))
        );
        interpreter.evaluateStatement(forNode, null);

        InterpreterDataType result = interpreter.getIDT(new VariableReferenceNode("y"), null);
        assertEquals(new InterpreterDataType("-10.0"), result);

        // Test continue

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("y"), new ConstantNode<Double>(0.0)), null); // y = 0;

        // This will skip the step x = 5
        ASTnode.IfNode conditionalSkip = new ASTnode.IfNode(new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.EQUAL, new ConstantNode<Double>(5.0)), new BlockNode(new LinkedList<>(List.of(
                new ASTnode.ContinueNode()
        ))));

        forNode = new ASTnode.ForNode(
                new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(0.0)), // x = 0
                new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(10.0)), // x < 10
                new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.POSTINCREMENT)), // x++
                new BlockNode(new LinkedList<>(List.of(
                        conditionalSkip,
                        new AssignmentNode(new VariableReferenceNode("y"), new OperationNode(new VariableReferenceNode("y"), OperationNode.Operation.POSTDECREMENT)) // y--
                )))
        );

        interpreter.evaluateStatement(forNode, null);

        result = interpreter.getIDT(new VariableReferenceNode("y"), null);
        assertEquals(new InterpreterDataType("-9.0"), result);

        // Test for in

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("array", new ConstantNode<Double>(0.0)), new ConstantNode<Character>('a')), null);
        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("array", new ConstantNode<Double>(1.0)), new ConstantNode<Character>('b')), null);
        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("array", new ConstantNode<Double>(2.0)), new ConstantNode<Character>('c')), null);
        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("string"), new ConstantNode<String>("")), null);
        forNode = new ASTnode.ForNode(
                new VariableReferenceNode("entry"),
                new VariableReferenceNode("array"),
                new BlockNode(new LinkedList<>(List.of(
                        new AssignmentNode(new VariableReferenceNode("string"), new OperationNode(new VariableReferenceNode("string"), OperationNode.Operation.CONCATENATION, new VariableReferenceNode("entry"))) // string = string entry (concat)
                )))
        );
        interpreter.evaluateStatement(forNode, null);

        result = interpreter.getIDT(new VariableReferenceNode("string"), null);
        assertEquals(new InterpreterDataType("abc"), result);
    }

    // Break tested in this one too
    @Test
    public void testWhile(){
        Interpreter interpreter = new Interpreter(List.of("Test file ha ha"));

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(0.0)), null); // x = 0
        ASTnode.WhileNode whileNode = new ASTnode.WhileNode(
                new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(10.0)),
                new BlockNode(new LinkedList<>(List.of(
                        new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.POSTINCREMENT))
                )))
        );
        interpreter.evaluateStatement(whileNode, null);

        InterpreterDataType result = interpreter.getIDT(new VariableReferenceNode("x"), null);
        assertEquals(new InterpreterDataType("10.0"), result);

        // Test do while

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(0.0)), null); // x = 0

        ASTnode.WhileNode doWhileNode = new ASTnode.WhileNode(
                new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(11.0)),
                new BlockNode(new LinkedList<>(List.of(
                        new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.POSTINCREMENT))
                )))
        , true);
        interpreter.evaluateStatement(doWhileNode, null);

        result = interpreter.getIDT(new VariableReferenceNode("x"), null);
        assertEquals(new InterpreterDataType("11.0"), result);

        // Test break

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(0.0)), null); // x = 0

        ASTnode.WhileNode breakWhile = new ASTnode.WhileNode(
                new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.LESSTHAN, new ConstantNode<Double>(10.0)),
                new BlockNode(new LinkedList<>(List.of(
                        new AssignmentNode(new VariableReferenceNode("x"), new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.POSTINCREMENT)),
                        new ASTnode.BreakNode()
                )))
        );

        interpreter.evaluateStatement(breakWhile, null);

        result = interpreter.getIDT(new VariableReferenceNode("x"), null);
        assertEquals(new InterpreterDataType("1.0"), result); // Should only increment once, otherwise would be infinite lol

    }


    // Return tested in this one too
    @Test
    public void testFunctionCall(){
        Interpreter interpreter = new Interpreter(List.of("Test file ha ha"));

        FunctionDefinitionNode function = new FunctionDefinitionNode("add", new LinkedList<>(List.of(
                new ASTnode.ReturnNode(new OperationNode(new VariableReferenceNode("x"), OperationNode.Operation.ADD, new VariableReferenceNode("y")))
        )), new LinkedList<>(List.of("x", "y")));

        interpreter.setFunctions(new LinkedList<>(List.of(function)));

        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("x"), new ConstantNode<Double>(1.0)), null);
        interpreter.evaluateStatement(new AssignmentNode(new VariableReferenceNode("y"), new ConstantNode<Double>(2.0)), null);

        FunctionCallNode functionCall = new FunctionCallNode("add", new LinkedList<>(List.of(
                new VariableReferenceNode("x"),
                new VariableReferenceNode("y")
        )));


        InterpreterDataType result = interpreter.evaluateStatement(functionCall, null);;
        assertEquals(new ReturnType("3.0"), result);

        // Test function as expression (and return)

        result = interpreter.getIDT(new OperationNode(functionCall, OperationNode.Operation.POSTINCREMENT), null);
        assertEquals(new InterpreterDataType("4.0"), result);
    }
    
    @Test
    public void testVariadicFunctionCall(){
        Interpreter interpreter = new Interpreter(List.of("Test file ha ha"));

        FunctionDefinitionNode function = new FunctionDefinitionNode("add", new LinkedList<>(List.of(
                new AssignmentNode(new VariableReferenceNode("sum"), new ConstantNode<Double>(0.0)),
                new ASTnode.ForNode(new VariableReferenceNode("number"), new VariableReferenceNode("add"), new BlockNode(new LinkedList<>(List.of(
                        new AssignmentNode(new VariableReferenceNode("sum"), new OperationNode(new VariableReferenceNode("sum"), OperationNode.Operation.ADD, new VariableReferenceNode("number")))
                )))),
                new ASTnode.ReturnNode(new VariableReferenceNode("sum")))
        ));
        
        interpreter.setFunctions(new LinkedList<>(List.of(function)));
        
        FunctionCallNode call = new FunctionCallNode("add", new LinkedList<>(List.of(
                new ConstantNode<Double>(1.0),
                new ConstantNode<Double>(2.0),
                new ConstantNode<Double>(3.0),
                new ConstantNode<Double>(4.0),
                new ConstantNode<Double>(5.0)
        )));
        
        InterpreterDataType result = interpreter.evaluateStatement(call, null);
        
        assertEquals(new ReturnType("15.0"), result);
    }
}
