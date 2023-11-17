import java.util.Collection;
import java.util.LinkedList;

public class FunctionCallNode extends Node implements StatementNode{
    private String name;
    private Collection<Node> arguments;
    private String position;
    public FunctionCallNode(String name, Collection<Node> arguments, String position){
        this.name = name;
        this.arguments = arguments;
        this.position = position;
    }
    
    public FunctionCallNode(String name, String position){
        this(name, new LinkedList<>(), position);
    }
    
    public FunctionCallNode(String name, Collection<Node> arguments){
        this(name, arguments, "[debug node]");
    }
    public FunctionCallNode(String name){
        this(name, new LinkedList<>());
    }
    
    public String getName(){
        return name;
    }
    
    public LinkedList<Node> getArguments(){
        return new LinkedList<>(arguments);
    }
    
    public String reportPosition(){
        return position;
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
