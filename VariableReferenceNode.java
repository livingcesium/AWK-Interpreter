import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class VariableReferenceNode extends Node{
    private String name;
    private Optional<Node> index;
    

    public VariableReferenceNode(String name, Node index){
        this.name = name;
        this.index = Optional.of(index);
    }

    public VariableReferenceNode(String name){
        this.name = name;
        index = Optional.empty();
    }
    
    public String getName() {
        return name;
    }
    
    public Optional<Node> getIndex(){
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableReferenceNode that = (VariableReferenceNode) o;

        if (!name.equals(that.name)) return false;
        boolean hasIndex = index.isPresent();
        if(hasIndex != that.index.isPresent())
            return false;
        else if(hasIndex)
            return index.get().equals(that.index.get());
        // Both empty indexes, should be equal
        return true;
    }
    
    public String toString(){
        if(index.isEmpty())
            return String.format("VariableReferenceNode: %s", name);
        else
            return String.format("VariableReferenceNode: %s[%s]", name, index.get());
    }
    
}
