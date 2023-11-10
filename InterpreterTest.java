import org.junit.Test;
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
}
