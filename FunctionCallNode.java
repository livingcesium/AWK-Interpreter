import java.util.Collection;
import java.util.LinkedList;

public class FunctionCallNode extends Node implements StatementNode{
    private String name;
    private Collection<Node> arguments;
    
    public FunctionCallNode(String name, Collection<Node> arguments){
        this.name = name;
        this.arguments = arguments;
    }
    
    public FunctionCallNode(String name){
        this.name = name;
        this.arguments = new LinkedList<>();
    }
    
    public String getName(){
        return name;
    }
    
    public LinkedList<Node> getArguments(){
        return new LinkedList<>(arguments);
    }
    
    public String toString(){
        return String.format("FunctionCallNode: %s(%s)", name, arguments);
    }
    
    public boolean equals(Object o){
        if (o == null) return false;
        if (o instanceof FunctionCallNode){
            FunctionCallNode other = (FunctionCallNode) o;
            return name.equals(other.name) && arguments.equals(other.arguments);
        }
        return false;
    }
}
