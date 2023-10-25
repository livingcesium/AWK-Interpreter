import java.util.LinkedList;

public class ProgramNode {
    private LinkedList<BlockNode> begin;
    private LinkedList<BlockNode> end;
    private LinkedList<BlockNode> other;
    private LinkedList<FunctionDefinitionNode> functions;
    
    public ProgramNode(LinkedList<BlockNode> begin, LinkedList<BlockNode> end, LinkedList<BlockNode> other, LinkedList<FunctionDefinitionNode> functions){
        this.begin = begin;
        this.end = end;
        this.other = other;
        this.functions = functions;
    }

    public LinkedList<BlockNode> getBegin() {
        return new LinkedList<>(begin);
    }
    public LinkedList<BlockNode> getOther() {
        return new LinkedList<>(other);
    }
    public LinkedList<BlockNode> getEnd(){
        return new LinkedList<>(end);
    }
    public LinkedList<FunctionDefinitionNode> getFunctions(){
        return new LinkedList<>(functions);
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ProgramNode\n");
        sb.append("Begin:\n");
        for(BlockNode node : begin){
            sb.append("\t");
            sb.append(node.toString().replace("\n", "\n\t"));
            sb.append("\n");
        }
        sb.append("End:\n");
        for(BlockNode node : end){
            sb.append("\t");
            sb.append(node.toString().replace("\n", "\n\t"));
            sb.append("\n");
        }
        sb.append("Other:\n");
        for(BlockNode node : other){
            sb.append("\t");
            sb.append(node.toString().replace("\n", "\n\t"));
            sb.append("\n");
        }
        sb.append("Functions:\n");
        for(FunctionDefinitionNode node : functions){
            sb.append("\t");
            sb.append(node.toString().replace("\n", "\n\t")); // Add tab to every line
            sb.append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o){
        if(o == this)
            return true;
        if(!(o instanceof ProgramNode))
            return false;
        ProgramNode other = (ProgramNode) o;
        return this.begin.equals(other.begin) && this.end.equals(other.end) && this.other.equals(other.other) && this.functions.equals(other.functions);
        
    }
}
