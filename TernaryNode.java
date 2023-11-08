public class TernaryNode extends Node {
    private Node condition; // representing boolean comparison
    private Node trueCase;
    
    private Node falseCase;
    
    public TernaryNode(Node condition, Node trueCase, Node falseCase){
        this.condition = condition;
        this.trueCase = trueCase;
        this.falseCase = falseCase;
    }
    
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        
        TernaryNode that = (TernaryNode) o;
        
        if(!condition.equals(that.condition)) return false;
        if(!trueCase.equals(that.trueCase)) return false;
        return falseCase.equals(that.falseCase);
    }
    
    public String toString(){
        return String.format("TernaryNode: %s ? %s : %s", condition, trueCase, falseCase);
    }
    
    public Node getCondition(){
        return condition;
    }
    
    public Node getTrueCase(){
        return trueCase;
    }
    
    public Node getFalseCase(){
        return falseCase;
    }
}
