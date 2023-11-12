import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionDefinitionNode extends Node{
    private String name;
    private LinkedList<String> parameterNames;
    protected LinkedList<LinkedList<String>> acceptedParameterNames = new LinkedList<>(); // For overloaded functions
    private LinkedList<StatementNode> statements;
    
    public FunctionDefinitionNode(String name, LinkedList<StatementNode> statements, List<String> parameterNames){
        this.name = name;
        this.parameterNames = new LinkedList<>(parameterNames);
        this.statements = statements;
        
        acceptedParameterNames.add(new LinkedList<>(parameterNames));
    }

    public FunctionDefinitionNode(String name, LinkedList<StatementNode> statements){ // No parameters
        this.name = name;
        this.statements = statements;
        this.parameterNames = new LinkedList<String>();
        
        acceptedParameterNames.add(new LinkedList<>(parameterNames));
    }
    
    public String getName(){
        return name;
    }
    @Override
    public String toString() {
        if(parameterNames == null)
            return String.format("function %s() {\n\t%s\n}", name, listToString(statements, ";\n\t"));
        return String.format("function %s(%s) {\n\t%s\n}", name, listToString(parameterNames, ", "), listToString(statements, ";\n\t"));
    }

    public LinkedList<StatementNode> getStatements(){
        return statements;
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

    // Add new parameter names to the list of accepted parameter names, only used by built-in functions.
    protected void overload(List<String> parameterNames){ 
        acceptedParameterNames.add(new LinkedList<>(parameterNames));
    }
    public LinkedList<String> getParameterNames(){
        return parameterNames;
    }
    
    
    
    
}
