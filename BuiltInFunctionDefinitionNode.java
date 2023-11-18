import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode{

    Function<HashMap<String,InterpreterDataType>,String> execute; // IDT execute(parameters) {...}
    private boolean variadic = false;

    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String,InterpreterDataType>,String> execute) {
        super(name, new LinkedList<>());
        this.execute = execute;
    }
    
    // Only one set of parameter names possible
    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String,InterpreterDataType>,String> execute, LinkedList<String> parameterNames) {
        super(name, new LinkedList<>(), parameterNames);
        this.execute = execute;
        // not variadic, parameters are named (or empty)
    }
    
    // Variadic if the last bool is true, no arguments if it's false.
    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String,InterpreterDataType>,String> execute, boolean variadic) {
        super(name, new LinkedList<>(), new LinkedList<>());
        this.execute = execute;
        this.variadic = variadic;
    }

    // Multiple sets of parameter names possible
    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String,InterpreterDataType>,String> execute, List<List<String>> acceptedParameterNames) {
        super(name, new LinkedList<>(), new LinkedList<>(acceptedParameterNames.get(0)));
        Iterator<List<String>> acceptedParameters = acceptedParameterNames.iterator();
        acceptedParameters.next(); // Skip the first one, it's already added in the super constructor
        while(acceptedParameters.hasNext()){
            overload(new LinkedList<String>(acceptedParameters.next()));
        }

        // Sorting the arg list based on their size in descending order
        this.acceptedParameterNames.sort(new Comparator<List<String>>() {
            @Override
            public int compare(List<String> o1, List<String> o2) {
                return Integer.compare(o2.size(), o1.size()); // Note the order of o2 and o1 for descending order
            }
        });

        this.execute = execute;
        // not variadic, parameters are named (or empty)
    }
    
    public boolean isVariadic(){
        return variadic;
    }
    
    public LinkedList<LinkedList<String>> getAcceptedParameterNames(){
        return this.acceptedParameterNames;
    }
    
    public Function<HashMap<String,InterpreterDataType>,String> getExecute(){
        return execute;
    }
    
    
}
