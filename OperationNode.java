import javax.swing.text.html.Option;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class OperationNode extends Node {

    Node left;
    Operation op;
    Optional<Node> right;

    public static enum Operation {
        EQUAL, NOTEQUAL, LESSTHAN, LESSOREQUAL, GREATERTHAN, GREATEROREQUAL, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
        PREINCREMENT, POSTINCREMENT, PREDECREMENT, POSTDECREMENT, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATION,
        NOTHING

    }
    
    public OperationNode(Node left, Operation op) {
        if (op == Operation.NOTHING)
            throw new IllegalArgumentException("OperationNode must have an operation");

        this.left = left;
        this.op = op;
        this.right = Optional.empty();
    }
    
    public OperationNode(Node left, Operation op, Node right) {
        if (op == Operation.NOTHING)
            throw new IllegalArgumentException("OperationNode must have an operation");

        this.left = left;
        this.op = op;
        this.right = Optional.of(right);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationNode that = (OperationNode) o;

        if (!left.equals(that.left)) 
            return false;
        if (op != that.op) 
            return false;
        
        if(right.isEmpty() && that.right.isEmpty())
            return true;
        
        else 
            return right.get().equals(that.right.get());
    }
    
    public String toString(){
        if(right.isEmpty())
            return String.format("OperationNode: %s->%s", op, left);
        else
            return String.format("OperationNode: %s %s %s", left, op, right.get());
    }
    
    public Node getLeft(){
        return left;
    }
    public Optional<Node> getRight(){
        return right;
    }
    
    public Operation getOperation(){
        return op;
    }
    
    public boolean isOp(Operation op){
        return this.op == op;
    }
    public boolean isLeftOp(Operation op){
        return left instanceof OperationNode && ((OperationNode) left).isOp(op);
    }
    public boolean isRightOp(Operation op){
        return right.isPresent() && right.get() instanceof OperationNode && ((OperationNode) right.get()).isOp(op);
    }
    
    

}
