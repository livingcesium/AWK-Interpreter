import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

// In AWK, field references are variable references that refer to the current line.
// So to make it so that all var references are easily identifiable, I made a subclass.
// Essentially all of these are:
//                                  @FIELD[index]
// Where @FIELD is a pseudo-variable that essentially just lets us know we're dealing with a field reference
// when it comes time to interpret.

// I chose to be explicit about variables in the tree so this is me sleeping in the grave I dug for myself lol
public class FieldReferenceNode extends VariableReferenceNode{
    public static final String FIELD_VARIABLE_NAME = "@FIELD"; // @ used to make it clear this is a special variable name, not a user defined one


    public FieldReferenceNode(Node index){
        super(FIELD_VARIABLE_NAME, index);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return super.equals(o);
    }

    public String toString(){
        return String.format("FieldReferenceNode: $(%s)",super.getindex().orElseThrow());
    }

}

