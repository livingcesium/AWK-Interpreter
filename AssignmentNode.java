import java.util.Optional;

public class AssignmentNode extends VariableReferenceNode implements StatementNode{

    VariableReferenceNode target;
    Node assignedTo;
    String position;
    
    
    public AssignmentNode(VariableReferenceNode target, Node assignedTo, String position){
        super(target.getName()); // An assignment returns a reference to the variable it assigns to, so in essence it IS a reference to that variable
        this.target = target;
        this.assignedTo = assignedTo;
        this.position = position;
    }

    public AssignmentNode(VariableReferenceNode target, Node assignedTo){
        this(target, assignedTo, "[debug node]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssignmentNode that = (AssignmentNode) o;

        if (!target.equals(that.target)) 
            return false;
        else 
            return assignedTo.equals(that.assignedTo);
    }
    
    public VariableReferenceNode getTarget(){
        return target;
    }
    
    public Node getAssignedTo(){
        return assignedTo;
    }
    
    public String toString(){
        return String.format("AssignmentNode: %s = %s", target, assignedTo);
    }
    
    public String reportPosition(){
        return position;
    }

}
