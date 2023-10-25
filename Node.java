import java.util.Optional;

public abstract class Node {
    
    private Optional<Node> next; // For certain expressions that are repetitive, pretty much only for multidimensional array checks
    public Node(){ 
        next = Optional.empty();
    }
    
    public Node(Node next){
        this.next = Optional.of(next);
    }
    
    public void setNext(Node next){
        this.next = Optional.of(next);
    }
    
    public Optional<Node> getNext(){
        return next;
    }
    
    // I figured getting variables is a common enough operation to warrant a method
    public VariableReferenceNode getVariableReferenceOrThrow(String msg){
        if(this instanceof VariableReferenceNode)
            return (VariableReferenceNode) this;
        else 
            throw new RuntimeException(msg);
    }
    public OperationNode getOperationOrThrow(String msg){
        if(this instanceof OperationNode)
            return (OperationNode) this;
        else 
            throw new NotAnOperationException(msg);
    }
    public boolean isVariableReference(){
        return this instanceof VariableReferenceNode;
    }
    
    public <T> Optional<ConstantNode<T>> tryGetConstant(Class<T> type){
        // Some java shenanegans, but hopefully it makes type checking really easy
        
        if(!(this instanceof ConstantNode<?> copy))
            return Optional.empty();

        if(type.isInstance(copy.getValue()))
            return Optional.of((ConstantNode<T>) copy);
        else
            return Optional.empty();
    }
    
    public Optional<StatementNode> tryGetStatement(){
        if(this instanceof StatementNode)
            return Optional.of((StatementNode) this);
        else
            return Optional.empty();
    }

    private static class NotAVariableException extends RuntimeException{
        public NotAVariableException(String msg){
            super(msg);
        }
    }
    private static class NotAnOperationException extends RuntimeException{
        public NotAnOperationException(String msg){
            super(msg);
        }
    }
}
