import java.util.LinkedList;
import java.util.Iterator;

public class FunctionDefinitionNode extends Node{
    private String name;
    private LinkedList<String> parameterNames;
    private LinkedList<StatementNode> statements;
    
    public FunctionDefinitionNode(String name, LinkedList<StatementNode> statements, LinkedList<String> parameterNames){
        this.name = name;
        this.parameterNames = parameterNames;
        this.statements = statements;
    }

    public FunctionDefinitionNode(String name, LinkedList<StatementNode> statements){
        this.name = name;
        this.statements = statements;
    }

    @Override
    public String toString() {
        if(parameterNames == null)
            return String.format("function %s() {\n\t%s\n}", name, listToString(statements, ";\n\t"));
        return String.format("function %s(%s) {\n\t%s\n}", name, listToString(parameterNames, ", "), listToString(statements, ";\n\t"));
    }

    private String listToString(LinkedList<?> list, String delimiter) {
        
        if(list.isEmpty())
            return "";
        
        StringBuilder builder = new StringBuilder();
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            builder.append(it.next());
            if (it.hasNext()) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }
}
