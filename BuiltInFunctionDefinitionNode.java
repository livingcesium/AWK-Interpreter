import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode{

    Function<HashMap<String,InterpreterDataType>,String> execute; // IDT execute(parameters) {...}
    boolean variadic = false;

    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String,InterpreterDataType>,String> execute) {
        super(name, new LinkedList<>());
        this.execute = execute;
    }

    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String,InterpreterDataType>,String> execute, boolean variadic) {
        super(name, new LinkedList<>());
        this.execute = execute;
        this.variadic = variadic;
    }
}
