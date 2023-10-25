import java.util.LinkedList;
import java.util.Optional;

public class BlockNode extends Node {
    private LinkedList<StatementNode> statements;
    private Optional<Node> condition;
    
    public BlockNode(LinkedList<StatementNode> statements, Node condition){
        this.statements = statements;
        this.condition = Optional.of(condition);
    }

    public BlockNode(LinkedList<StatementNode> statements, Optional<Node> condition){
        this.statements = statements;
        this.condition = condition;
    }
    
    public BlockNode(LinkedList<StatementNode> statements){
        this.statements = statements;
        this.condition = Optional.empty();
    }
    
    public LinkedList<StatementNode> getStatements(){
        return new LinkedList<>(statements);
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("BlockNode\n");
        for(StatementNode node : statements){
            sb.append(node.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public boolean equals(Object o){
        if(o == this)
            return true;
        if(!(o instanceof BlockNode))
            return false;
        BlockNode other = (BlockNode) o;
        return statements.equals(other.statements);
    }
}
