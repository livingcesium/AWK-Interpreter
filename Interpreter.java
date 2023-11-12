import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {
    private static class LineManager{
        private LinkedList<String> lines;
        private LinkedList<String> out;
        
        public LineManager(List<String> lines, List<String> out){
            this.out = new LinkedList<>(out);
            switchFile(lines);
        }
        
        public void switchFile(List<String> newLines){
            this.lines = new LinkedList<>(newLines);
            globalVariables.put("FNR", new InterpreterDataType("1"));
            globalVariables.put("NR", new InterpreterDataType("1"));
            if(!handleNextLine())
                splitAndAssign("");
        }
        
        // This splits & assigns the next line, progresses the line manager
        public boolean handleNextLine(){
            if(lines.isEmpty())
                return false;
            return splitAndAssign(getNext());
        }
        
        // This splits & assigns any given line, does not progress the line manager on its own
        public boolean splitAndAssign(String line){
            globalVariables.put("$0", new InterpreterDataType(line));
            String separator = globalVariables.get("FS").value;
            String[] split = line.split(separator);
            
            int i = 1;
            for(String s : split){
                globalVariables.put("$"+ i++, new InterpreterDataType(s));
            }
            // NF = i + 1 (Number of fields)
            globalVariables.put("NF", new InterpreterDataType(Integer.toString(i + 1)));
            return true;
        }
        public boolean editField(int index, String newValue){
            
            if(index == 0){ // Replace whole line
                splitAndAssign(newValue);
                return true;
            }
            
            if(!globalVariables.containsKey("NF") || Double.parseDouble(globalVariables.get("NF").value) < index)
            // Fail if we haven't assigned fields yet, or if we're trying to edit a field that doesn't exist
                return false;
            
            globalVariables.put("$" + index, new InterpreterDataType(newValue));
            return true;
        }
        
        private String getNext(){
            
            if(globalVariables.containsKey("$0"))
            // Put the previous line into the output
                toOutput(globalVariables.get("$0").value);
            
            // NR++ (Number of records)
            globalVariables.put("NR", new InterpreterDataType(Integer.toString(Integer.parseInt(globalVariables.get("NR").value) + 1)));
            // FNR ++ (File Number of Records)
            globalVariables.put("FNR", new InterpreterDataType(Integer.toString(Integer.parseInt(globalVariables.get("FNR").value) + 1)));
            return this.lines.remove(0);
        }
        
        private boolean toOutput(String line){
            return this.out.add(line);
        }
        
        public List<String> getOutput(){
            return this.out;
        }
        
        public String peekNext(){
            if(lines.isEmpty())
                throw new IndexOutOfBoundsException("No more lines to peek");
            return this.lines.get(0);
        }
        
    }
    
    private static HashMap<String, InterpreterDataType> globalVariables = new HashMap<>();
    private HashMap<String, FunctionDefinitionNode> functions = new HashMap<>();

    private LineManager lineManager;


    public Interpreter(ProgramNode program, Path path) throws IOException {

        System.out.println("New interpreter created, globals cleared");
        globalVariables = new HashMap<>();
        globalVariables.put("FILENAME", new InterpreterDataType(path.toString()));
        setDefaults();
        setFunctions(program.getFunctions());
        lineManager = new LineManager(Files.readAllLines(path), List.of());


    }

    public Interpreter(ProgramNode program){
        System.out.println("New interpreter created in stdin mode, globals cleared");
        globalVariables = new HashMap<>();
        globalVariables.put("FILENAME", new InterpreterDataType("STDIN"));
        setDefaults();
        setFunctions(program.getFunctions());
        this.lineManager = new LineManager(List.of(), List.of());
    }
    
    public Interpreter(List<String> debugLines){
        System.out.println("New interpreter created in stdin mode, globals cleared");
        globalVariables = new HashMap<>();
        globalVariables.put("FILENAME", new InterpreterDataType("STDIN"));
        setDefaults();
        this.lineManager = new LineManager(debugLines, List.of());
    }

    // TODO: Change from public once full functionality is implemented
    public void setFunctions(LinkedList<FunctionDefinitionNode> functions){
        for(FunctionDefinitionNode function : functions){
            this.functions.put(function.getName(), function);
        }
    }
    
    private void setDefaults(){
        globalVariables.put("FS", new InterpreterDataType(" "));
        globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
        globalVariables.put("OFS", new InterpreterDataType("\n"));
        populateKnownFunctions();
    }
    
    public ReturnType evaluateStatement(StatementNode statement, HashMap<String,InterpreterDataType> locals){
        if(statement instanceof ASTnode syntax){
        // If, for, while...
            return evaluateSyntax(syntax, locals);
        } else if(statement instanceof AssignmentNode assignment){
            return evaluateAssignment(assignment, locals);
        } else if(statement instanceof FunctionCallNode functionCall){
            return evaluateCall(functionCall, locals);
        } else 
            throw new RuntimeException("Unrecognized statement type, did the dev finish implementing it?");
    }
    
    private ReturnType evaluateSyntax(ASTnode syntax, HashMap<String, InterpreterDataType> locals){
        
        // Check every ASTnode and move onto the correct function/return the corresponding signal
        if(syntax instanceof ASTnode.IfNode ifNode){
            return evaluateIf(ifNode, locals);
        } else if(syntax instanceof ASTnode.WhileNode whileNode){
            return evaluateWhile(whileNode, locals);
        } else if(syntax instanceof ASTnode.ForNode forNode) {
            return evaluateFor(forNode, locals);
        } else if(syntax instanceof ASTnode.DeleteNode deleteNode) {
            return evaluateDelete(deleteNode, locals);
        } else if(syntax instanceof ASTnode.ReturnNode returnNode) {
            return evaluateReturn(returnNode, locals);
        } else if(syntax instanceof ASTnode.BreakNode) {
            return new ReturnType(ReturnType.Control.BREAK);
        } else if(syntax instanceof ASTnode.ContinueNode) {
            return new ReturnType(ReturnType.Control.CONTINUE);
        } else
            throw new RuntimeException("Unrecognized syntax type, did the dev finish implementing it?");
        
    }

    private ReturnType evaluateDelete(ASTnode.DeleteNode delete, HashMap<String, InterpreterDataType> locals){
        InterpreterDataType target = getIDT(delete.target, locals);
        Collection<Node> indices = delete.indices;

        if(target instanceof InterpreterArrayDataType array){
            HashMap<String, InterpreterDataType> arrayData = array.getArrayValue();
            for(Node index : indices){
                InterpreterDataType indexData = getIDT(index, locals);
                if(!arrayData.containsKey(indexData.value))
                    throw new IndexOutOfBoundsException(String.format("Index %s out of bounds for array %s", indexData.value, delete.target));
                arrayData.remove(indexData.value);
            }
        } else
            throw new IllegalArgumentException("Cannot delete from non-array variable");

        return new ReturnType(ReturnType.Control.NORMAL);
    }

    private ReturnType evaluateReturn(ASTnode.ReturnNode returnNode, HashMap<String, InterpreterDataType> locals){
        return new ReturnType(getIDT(returnNode.value, locals).value);
    }
    
    private ReturnType evaluateIf(ASTnode.IfNode ifNode, HashMap<String, InterpreterDataType> locals){
        InterpreterDataType condition = getIDT(ifNode.getCondition(), locals);
        Optional<ASTnode.IfNode> elseNode;
        boolean trueCase = asBoolean(condition.value);
        
        if(trueCase)
            return evaluateBlock(ifNode.getStatements(), locals);
        else if((elseNode = ifNode.getElse()).isPresent())
            return evaluateIf(elseNode.get(), locals);

        return new ReturnType(ReturnType.Control.NORMAL);
    }

    private ReturnType evaluateFor(ASTnode.ForNode forNode, HashMap<String, InterpreterDataType> locals){
        if(forNode.forIn)
            return evaluateForIn(forNode, locals);

        // Initialize section: for(_;;)
        // i=...
        getIDT(forNode.getInit(), locals);


        Node condition = forNode.getCondition();
        InterpreterDataType conditionData;
        BlockNode block = forNode.getStatements();

        ReturnType result;
        while(asBoolean((conditionData = getIDT(condition, locals)).value)) /*for(;_;)*/ {

            if((result = evaluateBlock(block, locals)).controlType == ReturnType.Control.BREAK)
                break;
            else if(result.controlType == ReturnType.Control.RETURN)
                return result;
            // Update section: for(;;_)
            //i=...
            getIDT(forNode.getUpdate(), locals);


        }

        return new ReturnType(ReturnType.Control.NORMAL);
    }

    private ReturnType evaluateForIn(ASTnode.ForNode forNode, HashMap<String, InterpreterDataType> locals){
        String memberName = forNode.getMember().getName();
        InterpreterDataType data = getIDT(forNode.getCollection(), locals);
        if(!(data instanceof InterpreterArrayDataType array))
            throw new IllegalArgumentException("Cannot iterate over non-array");
        BlockNode block = forNode.getStatements();

        HashMap<String, InterpreterDataType> scope = (locals == null) ? globalVariables : locals;
        ReturnType result;

        for(Map.Entry<String, InterpreterDataType> entry : array.getArrayValue().entrySet()){
            scope.put(memberName, entry.getValue());

            if((result = evaluateBlock(block, locals)).controlType == ReturnType.Control.BREAK)
                break;
            else if(result.controlType == ReturnType.Control.RETURN)
                return result;
        }

        return new ReturnType(ReturnType.Control.NORMAL);
    }

    private ReturnType evaluateDoWhile(ASTnode.WhileNode whileNode, HashMap<String, InterpreterDataType> locals){
        InterpreterDataType condition;
        boolean trueCase;
        BlockNode block = whileNode.getStatements();
        ReturnType result;

        do{
            if((result = evaluateBlock(block, locals)).controlType == ReturnType.Control.BREAK)
                break;
            else if(result.controlType == ReturnType.Control.RETURN)
                return result;
            condition = getIDT(whileNode.getCondition(), locals);
            trueCase = asBoolean(condition.value);
        } while(trueCase);

        return new ReturnType(ReturnType.Control.NORMAL);
    }

    private ReturnType evaluateWhile(ASTnode.WhileNode whileNode, HashMap<String, InterpreterDataType> locals){
        if(whileNode.doWhile)
            return evaluateDoWhile(whileNode, locals);
        InterpreterDataType condition = getIDT(whileNode.getCondition(), locals);
        boolean trueCase = asBoolean(condition.value);
        BlockNode block = whileNode.getStatements();
        ReturnType result;

        while(trueCase){
            if((result = evaluateBlock(block, locals)).controlType == ReturnType.Control.BREAK)
                break;
            else if(result.controlType == ReturnType.Control.RETURN)
                return result;
            condition = getIDT(whileNode.getCondition(), locals);
            trueCase = asBoolean(condition.value);
        }

        return new ReturnType(ReturnType.Control.NORMAL);
    }
    
    private ReturnType evaluateBlock(BlockNode block, HashMap<String, InterpreterDataType> locals){
        Optional<Node> condition;
        InterpreterDataType conditionData;
        boolean shouldRun = true;

        if((condition = block.getCondition()).isPresent()){
            if(condition.get() instanceof RegexNode regex)
            // Translates RegexNode[regex] into OperationNode[$0 ~ regex]
                conditionData = getIDT(new OperationNode(new FieldReferenceNode(new ConstantNode<Double>(0.0)), OperationNode.Operation.MATCH, regex), locals);
            else
                conditionData = getIDT(condition.get(), locals);

            shouldRun = asBoolean(conditionData.value);
        }

        if(shouldRun)
            return evaluateStatements(block.getStatements(), locals);
        else
            return new ReturnType(ReturnType.Control.NORMAL);
    }

    private ReturnType evaluateStatements(List<StatementNode> statements, HashMap<String,InterpreterDataType> locals){
        ReturnType currentValue;
        for(StatementNode statement : statements){
            currentValue = evaluateStatement(statement, locals);
            if(!currentValue.isType(ReturnType.Control.NORMAL)) // break, continue, return...
                return currentValue;
        }

        return new ReturnType(ReturnType.Control.NORMAL);
    }
    
    private ReturnType evaluateCall(FunctionCallNode call, HashMap<String,InterpreterDataType> locals){
        
        if(!functions.containsKey(call.getName()))
            throw new RuntimeException(String.format("Function %s not defined", call.getName()));
        
        FunctionDefinitionNode func = functions.get(call.getName());
        
        if(func instanceof BuiltInFunctionDefinitionNode builtIn) {
            return evaluateBuiltIn(builtIn, call.getArguments(), locals);
        }

        HashMap<String,InterpreterDataType> scope = (locals == null) ? globalVariables : locals;
        scope.putAll(collectArgs(func.getParameterNames(), call.getArguments(), locals));

        return evaluateStatements(func.getStatements(), scope);
        
    }
    
    private HashMap<String, InterpreterDataType> collectArgs(LinkedList<String> parameterNames, List<Node> arguments, HashMap<String,InterpreterDataType> locals) {
        // Built-in functions don't use this one, maybe could be refactored but the small differences are annoying enough to push that decision to later

        HashMap<String, InterpreterDataType> args = new HashMap<>();
        
        Iterator<String> paramNameIterator = parameterNames.iterator();
        if(arguments.size() > parameterNames.size())
            throw new IllegalArgumentException("Too many arguments");
        else if(arguments.size() < parameterNames.size())
            throw new IllegalArgumentException("Too few arguments");
        
        for(Node param: arguments){
            if(paramNameIterator.hasNext()){
                args.put(paramNameIterator.next(), getIDT(param, locals));
            } else
                throw new RuntimeException("Too many arguments passed into function");
        }
        
        return args;
    }
    
    private ReturnType evaluateBuiltIn(BuiltInFunctionDefinitionNode builtIn, LinkedList<Node> argumentNodes, HashMap<String, InterpreterDataType> locals){
        
        HashMap<String, InterpreterDataType> scope = (locals == null) ? globalVariables : locals;
        // Gather args
        HashMap<String,InterpreterDataType> args = new HashMap<>();
        HashMap<String, String> varToParam = new HashMap<>(); // Map variable name -> variable parameter name
        // The above map is used to change variables that are meant to be mutable
        
        Iterator<Node> argNodeIterator = argumentNodes.iterator();
        Node currentNode;
        
        // Check every possible parameter set, validating that they match what was passed in as we collect arguments one by one
        for(LinkedList<String> parameterSet : builtIn.getAcceptedParameterNames()){
            for(String parameter : parameterSet){
                if(!argNodeIterator.hasNext()) {
                    argNodeIterator = argumentNodes.iterator(); // Reset iterator
                    break; // This parameter set is invalid (too few arguments)
                }
                
                currentNode = argNodeIterator.next();
                
                if(parameter.matches("^var")){ 
                // For built-in functions, I made all mutable parameters start with var
                    if(!(currentNode instanceof VariableReferenceNode variableRef))
                        break; // This parameter set is invalid (non-variable passed to var parameter)
                    varToParam.put(variableRef.getName(), parameter);
                }
                
                args.put(parameter, getIDT(currentNode, scope));
            }
            
            if(args.size() == parameterSet.size())
                break; // We found a valid parameter set
            else {
            // This parameter set is invalid (too many arguments, or missing variable parameter)
                args = new HashMap<>(); // Reset everything we collected or used ( what a waste :/ )
                varToParam = new HashMap<>();
                argNodeIterator = argumentNodes.iterator();
            }
        }
        
        if(args.isEmpty())
            throw new IllegalArgumentException("No valid parameter set found for built-in function. Double check parameter count and if there are any variable parameters expected");
        
        // Run code
        String output = builtIn.getExecute().apply(args);
        
        // Update all mutable arguments
        for(String variableName : varToParam.keySet())
            scope.put(variableName, args.get(varToParam.get(variableName)));
        
        return new ReturnType(output);
    }
    
    public InterpreterDataType getIDT(Node node, HashMap<String, InterpreterDataType> locals){
        if(node instanceof RegexNode)
            throw new RuntimeException("Regex literals are only valid in the condition of conditional control blocks, or passed into certain built-in functions"); // TODO: don't make yourself a liar

        // IntelliJ is suggesting I change this into some pattern switch statement thing, that isn't even normally supported by the java version I'm on lol.
        if (node instanceof AssignmentNode assignment) {
            return evaluateAssignment(assignment, locals).expectData("Assignment as expression must result in a value");
        } else if (node instanceof ConstantNode<?> constant) {
            return evaluateConstant(constant);
        } else if (node instanceof FunctionCallNode functionCall) {
            return evaluateCall(functionCall, locals).expectData("Function call as expression must result in a value");
        } else if (node instanceof TernaryNode ternary) {
            return evaluateTernary(ternary, locals);
        } else if (node instanceof VariableReferenceNode variableRef) {
            return evaluateVariableRef(variableRef, locals);
        } else if (node instanceof OperationNode operation) {
            return evaluateOperation(operation, locals);
        } else {
            throw new RuntimeException("Unknown node type");
        }

    }
    
    private InterpreterDataType evaluateTernary(TernaryNode node, HashMap<String, InterpreterDataType> scope){
        InterpreterDataType condition = getIDT(node.getCondition(), scope);
        if(asBoolean(condition.value))
            return getIDT(node.getTrueCase(), scope);
        else
            return getIDT(node.getFalseCase(), scope);
    }
    
    private InterpreterDataType evaluateOperation(OperationNode operation, HashMap<String, InterpreterDataType> scope){
        Node left = operation.getLeft();
        Optional<Node> right = Optional.empty();
        if((right = operation.getRight()).isPresent())
            right = operation.getRight();
        
        
        // Get data in left and right
        InterpreterDataType leftData = getIDT(left, scope);
        InterpreterDataType rightData = null;
        if(right.isPresent())
            if(right.get() instanceof RegexNode regex && (operation.isOp(OperationNode.Operation.MATCH) || operation.isOp(OperationNode.Operation.NOTMATCH)))
            // Regex literals are accepted in this case, so we get its more agreeable twin to pass through getIDT in its place
                rightData = getIDT(regex.getGeneralized(), scope);
            else
                rightData = getIDT(right.get(), scope);
        else{
        // Single operand operations, if no right is present it never escapes from here
            switch(operation.getOperation()){
                case NOT -> {
                    if(asBoolean(leftData.value))
                        return new InterpreterDataType("0");
                    else
                        return new InterpreterDataType("1");
                }
                case UNARYNEG -> {
                    try{
                        return new InterpreterDataType(Double.toString(-Double.parseDouble(leftData.value)));
                    } catch (NumberFormatException e){
                        throw new IllegalArgumentException("UNARYNEG operator requires numeric operand");
                    }
                }
                case UNARYPOS -> { // TODO: maybe make this less like eating glass
                    try{
                        return new InterpreterDataType(Double.toString(Double.parseDouble(leftData.value)));
                    } catch (NumberFormatException maybeNumberFollowedByCharacters){
                        String extractedNumber = leftData.value.strip();
                        // Repeatedly trim off the last character and return the first number we get.
                        while(!extractedNumber.isEmpty()){
                            try{
                                return new InterpreterDataType(Double.toString(Double.parseDouble(extractedNumber)));
                            } catch (NumberFormatException e){
                                extractedNumber = extractedNumber.substring(0, extractedNumber.length() - 1);
                            }
                        }
                        // No number at all
                        return new InterpreterDataType("0");
                    }
                }
                case POSTINCREMENT, PREINCREMENT -> {
                    try{
                        return new InterpreterDataType(Double.toString(Double.parseDouble(leftData.value) + 1));
                    } catch (NumberFormatException e){
                        throw new IllegalArgumentException("INCREMENT operator requires numeric operand");
                    }
                }
                case POSTDECREMENT, PREDECREMENT -> {
                    try{
                        return new InterpreterDataType(Double.toString(Double.parseDouble(leftData.value) - 1));
                    } catch (NumberFormatException e){
                        throw new IllegalArgumentException("DECREMENT operator requires numeric operand");
                    }
                }
                default -> {
                    throw new RuntimeException("This single operand operation is not implemented yet");
                }
            }
        }
        
        // Now we have both left and right
        
        if(rightData == null) // Safety check
            throw new RuntimeException("Right data ended up null while evaluating operation, this should be impossible");
        Double leftDouble, rightDouble;
        
        // Get doubles if possible
        try {
            leftDouble = Double.parseDouble(leftData.value);
            rightDouble = Double.parseDouble(rightData.value);
        } catch (NumberFormatException e){
            leftDouble = null;
            rightDouble = null;
        }
        
        // Get boolean interpretation (we already have the doubles stored here, so forget the helper functions)
        Boolean leftBool = null, rightBool = null;
        try {
            leftBool = asBoolean(leftData.value);
            rightBool = asBoolean(rightData.value);
        } catch (IncompatibleTypeException e){
            leftBool = null;
            rightBool = null; // Just ensure both are null, so we only have to check one
        }
        
        switch(operation.getOperation()){
            case EQUAL -> {
                if(leftDouble != null)
                    return new InterpreterDataType(booleanAsString(leftDouble.equals(rightDouble)));
                return new InterpreterDataType(booleanAsString(leftData.value.equals(rightData.value)));
            }
            case NOTEQUAL -> {
                if(leftDouble != null)
                    return new InterpreterDataType(booleanAsString(!leftDouble.equals(rightDouble)));
                return new InterpreterDataType(booleanAsString(!leftData.value.equals(rightData.value)));
            }
            case LESSTHAN -> {
                if(leftDouble != null)
                    return new InterpreterDataType(booleanAsString(leftDouble < rightDouble));
                else 
                    return new InterpreterDataType(booleanAsString(leftData.value.compareTo(rightData.value) < 0));
            }
            case LESSOREQUAL -> {
                if(leftDouble != null)
                    return new InterpreterDataType(booleanAsString(leftDouble <= rightDouble));
                else 
                    return new InterpreterDataType(booleanAsString(leftData.value.compareTo(rightData.value) <= 0));
            }
            case GREATERTHAN -> {
                if(leftDouble != null)
                    return new InterpreterDataType(booleanAsString(leftDouble > rightDouble));
                else 
                    return new InterpreterDataType(booleanAsString(leftData.value.compareTo(rightData.value) > 0));
            }
            case GREATEROREQUAL -> {
                if(leftDouble != null)
                    return new InterpreterDataType(booleanAsString(leftDouble >= rightDouble));
                else 
                    return new InterpreterDataType(booleanAsString(leftData.value.compareTo(rightData.value) >= 0));
            }
            case AND -> {
                if(leftBool == null)
                    throw new IllegalArgumentException("AND operator requires boolean operands");
                return new InterpreterDataType(booleanAsString(leftBool && rightBool));
            }
            case OR -> {
                if(leftBool == null)
                    throw new IllegalArgumentException("OR operator requires boolean operands");
                return new InterpreterDataType(booleanAsString(leftBool || rightBool));
            }
            case MATCH -> {
                return new InterpreterDataType(booleanAsString(Pattern.matches(rightData.value, leftData.value)));
            }
            case NOTMATCH -> {
                return new InterpreterDataType(booleanAsString(!Pattern.matches(rightData.value, leftData.value)));
            }
            case IN -> {
                if(!(rightData instanceof InterpreterArrayDataType array))
                    throw new IllegalArgumentException("IN operator requires array operand");
                return new InterpreterDataType(booleanAsString(array.getArrayValue().containsKey(leftData.value)));
            }
            case CONCATENATION -> {
                return new InterpreterDataType(leftData.value + rightData.value);
            }
            case ADD -> {
                if(leftDouble != null)
                    return new InterpreterDataType(Double.toString(leftDouble + rightDouble));
                else 
                    throw new IllegalArgumentException("ADD operator requires numeric operand");
            }
            case SUBTRACT -> {
                if(leftDouble != null)
                    return new InterpreterDataType(Double.toString(leftDouble - rightDouble));
                else 
                    throw new IllegalArgumentException("SUBTRACT operator requires numeric operand");
            }
            case MULTIPLY -> {
                if(leftDouble != null)
                    return new InterpreterDataType(Double.toString(leftDouble * rightDouble));
                else 
                    throw new IllegalArgumentException("MULTIPLY operator requires numeric operand");
            }
            case DIVIDE -> {
                if(leftDouble != null)
                    return new InterpreterDataType(Double.toString(leftDouble / rightDouble));
                else 
                    throw new IllegalArgumentException("DIVIDE operator requires numeric operand");
            }
            case MODULO -> {
                if(leftDouble != null)
                    return new InterpreterDataType(Double.toString(leftDouble % rightDouble));
                else 
                    throw new IllegalArgumentException("MODULO operator requires numeric operand");
            }
            case EXPONENT -> {
                if(leftDouble != null)
                    return new InterpreterDataType(Double.toString(Math.pow(leftDouble, rightDouble)));
                else 
                    throw new IllegalArgumentException("EXPONENTIATION operator requires numeric operand");
            }
            default -> {
                throw new RuntimeException("Operation not implemented yet");
            }
        }
        
    }
    
    private InterpreterDataType evaluateFieldReference(FieldReferenceNode node, HashMap<String, InterpreterDataType> locals){
        Node index = node.getIndex().orElseThrow(); // getIndex should never fail here
        InterpreterDataType indexData = getIDT(index, locals);
        int fieldIndex;
        try {
            fieldIndex = (int) Double.parseDouble(indexData.value); // any decimal value is truncated
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Field index must be numeric");
        }
        
        if(fieldIndex < 0)
            throw new IllegalArgumentException("Field index must be positive");
        if(fieldIndex > Integer.parseInt(globalVariables.get("NF").value))
            throw new IndexOutOfBoundsException(String.format("Index %d out of bounds for %d fields", fieldIndex, Integer.parseInt(globalVariables.get("NF").value)));
        
        return globalVariables.get("$" + fieldIndex);
    }
    private InterpreterDataType evaluateVariableRef(VariableReferenceNode node, HashMap<String, InterpreterDataType> locals){
        String name = node.getName();
        Optional<Node> index;
        if(node instanceof AssignmentNode assignment)
            return evaluateAssignment(assignment, locals);
        
        if(node instanceof FieldReferenceNode field) {
            return evaluateFieldReference(field, locals);
        }
        
        HashMap<String, InterpreterDataType> scope = (locals == null) ? globalVariables : locals;
        
        // Get the variable data
        InterpreterDataType variableData;
        if(scope.containsKey(name))
            variableData = scope.get(name);
        else if(globalVariables.containsKey(name)) {
            variableData = globalVariables.get(name);
        } else
            throw new RuntimeException(String.format("Variable %s not defined", name));
        
        // Handle the array (or field reference) case
        if((index = node.getIndex()).isPresent()){
            if(variableData instanceof InterpreterArrayDataType arrayData){
                InterpreterDataType indexData = getIDT(index.get(), scope);
                HashMap<String, InterpreterDataType> array = arrayData.getArrayValue();
                if(!array.containsKey(indexData.value))
                    throw new IndexOutOfBoundsException(String.format("Index %s out of bounds for array %s", indexData.value, name));

                return array.get(indexData.value);
            } else 
                throw new RuntimeException(String.format("Attempted to make array reference to non-array variable %s", name));
        }
        
        return variableData;
        }
    
    private ReturnType evaluateAssignment(AssignmentNode node, HashMap<String, InterpreterDataType> locals){
        String name = node.getName();
        InterpreterDataType value = getIDT(node.getAssignedTo(), locals);
        boolean postOperation;
        
        // Check to see if we're working with a post-operation, so we can return the correct value
        if(node.getAssignedTo() instanceof OperationNode operation)
            postOperation = operation.getOperation() == OperationNode.Operation.POSTINCREMENT || operation.getOperation() == OperationNode.Operation.POSTDECREMENT;
        else
            postOperation = false;
        
        InterpreterDataType original = null;
        HashMap<String, InterpreterDataType> scope = (locals == null) ? globalVariables : locals;
        
        
        // Ensure that if were doing a post-operation, we have something to return
        // (since post operations return the original value, not the new one)
        

        if (scope.containsKey(name)) {
            original = scope.get(name);
        } else if (globalVariables.containsKey(name)) {
            original = globalVariables.get(name);
        } else if(postOperation)
            throw new RuntimeException(String.format("Variable %s not defined, cannot return value before assignment (post operation)", name));

        Optional<Node> indexNode; // Used for both array and field references
        
        if(node.getTarget() instanceof FieldReferenceNode fieldReference){
        // Field assignment case (note: The order matters! Field references also fit the array case, so we sift them out first.)
            if((indexNode = fieldReference.getIndex()).isEmpty())
                throw new RuntimeException("Field assignment requires index");
            InterpreterDataType indexData = getIDT(indexNode.get(), locals);
            int index;
            try {
                index = (int) Double.parseDouble(indexData.value); // parseDouble used because numbers are stored as doubles
            }catch(NumberFormatException e){
                throw new IllegalArgumentException("Field index must be numeric");
            }
            
            lineManager.editField(index, value.value);
        } else if((indexNode = node.getTarget().getIndex()).isPresent()){
        // Array assignment case
            HashMap<String, InterpreterDataType> newArray;
            InterpreterDataType indexData = getIDT(indexNode.get(), locals);
            
            // Get the original array, or create a new one if it doesn't exist
            if(original instanceof InterpreterArrayDataType arrayData)
                newArray = arrayData.getArrayValue();
            else newArray = new HashMap<>();
            
            newArray.put(indexData.value, value);

            scope.put(name, new InterpreterArrayDataType(newArray));
        } else {
        // Normal assignment case
            if(original instanceof InterpreterArrayDataType arrayData)
                throw new RuntimeException(String.format("Attempted to assign non-array value to array variable %s", name));
            scope.put(name, value);
        }
        
        if(postOperation) {
            if(original == null)
                throw new RuntimeException("Post-operation failed to get original value, isn't this impossible?");
            return new ReturnType(original.value, false);
        }
        else 
            return new ReturnType(value.value, false); // Maybe refactor value.value away
    }
    
    private InterpreterDataType evaluateConstant(ConstantNode<?> node){
        return new InterpreterDataType(node.getValue());
    }
    
    private void populateKnownFunctions(){

        // Print
        Function<HashMap<String,InterpreterDataType>,String> executePrint = (HashMap<String,InterpreterDataType> array) -> {
            LinkedList<String> args = new LinkedList<>();

            int i = 1; // 1-indexed
            String data;

            // Collect args 
            while(array.containsKey(Integer.toString(i))) {
                data = array.get(Integer.toString(i++)).value;
                args.add(data);
            }

            // Format args with separator
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < i - 1; j++) { // Stops one element early, see comment below
                output.append(args.poll()).append(Interpreter.getGlobalVariable("FS").get().value); // certainly present
            }
            output.append(args.poll()); // Last one doesn't get separator

            System.out.print(output);

            return output.toString();
        };
        functions.put("print", new BuiltInFunctionDefinitionNode("print", executePrint, true));

        // Printf
        Function<HashMap<String,InterpreterDataType>,String> executePrintf = (HashMap<String,InterpreterDataType> array) -> {
            LinkedList<String> args = new LinkedList<>();

            int i = 1;
            String format;
            String data;
            // get 1st arg, the format string
            if(array.containsKey(Integer.toString(i))){
                format = array.get(Integer.toString(i++)).value;
            } else {
                throw new IllegalArgumentException("printf requires a format string");
            }

            // get remaining args
            while(array.containsKey(Integer.toString(i))){
                data = array.get(Integer.toString(i)).value;
                    args.add(data);
                i++;
            }

            System.out.printf(format, args);
            return String.format(format, args);

        };

        functions.put("printf", new BuiltInFunctionDefinitionNode("printf", executePrintf, true));

        // Getline
        Function<HashMap<String,InterpreterDataType>,String> executeGetLine = (HashMap<String,InterpreterDataType> args) -> {
            
            if(args.isEmpty())
                return lineManager.handleNextLine() ? "1" : "0";
            else if(!args.containsKey("var"))
                throw new IllegalArgumentException("getline requires either no arguments, or a variable to store into");
            
            try {
                args.put("var", new InterpreterDataType(lineManager.peekNext()));
                return "1";
            } catch (IndexOutOfBoundsException e){
                return "0";
            }
            
        };
        
        functions.put("getline", new BuiltInFunctionDefinitionNode("getline", executeGetLine, List.of(
                List.of(), // No args, just move to the next line
                List.of("var") // Store into a variable
        )));

        // Next
        Function<HashMap<String,InterpreterDataType>,String> executeNext = (HashMap<String,InterpreterDataType> noArgs) -> {
            lineManager.handleNextLine();
            return "";
        };

        functions.put("next", new BuiltInFunctionDefinitionNode("next", executeNext, false));

        // Gsub
        Function<HashMap<String,InterpreterDataType>,String> executeGsub = (HashMap<String,InterpreterDataType> args) -> {
            String regex = args.get("regex").value;
            String replacement = args.get("replacement").value;
            String target;
            if (args.containsKey("var")){
                target = args.get("var").value;
                args.put("var", new InterpreterDataType(target = target.replaceAll(regex, replacement)));
            } else {
                target = globalVariables.get("$0").value;
                lineManager.splitAndAssign(target = target.replaceAll(regex, replacement));
            }
            return target;
        };
        
        // Note: gsub and sub have the same argument structure
        List<List<String>> subArgs = List.of(
                List.of("regex", "replacement", "var"),
                List.of("regex", "replacement") // Defaults to $0
        ); 

        functions.put("gsub", new BuiltInFunctionDefinitionNode("gsub", executeGsub, subArgs));

        // Sub
        Function<HashMap<String,InterpreterDataType>,String> executeSub = (HashMap<String,InterpreterDataType> args) -> {
            String regex = args.get("regex").value;
            String replacement = args.get("replacement").value;
            String target;
            
            if(args.containsKey("var")) {
                target = args.get("var").value;
                args.put("var", new InterpreterDataType(target = target.replaceFirst(regex, replacement)));
            } else {
                target = globalVariables.get("$0").value;
                lineManager.splitAndAssign(target = target.replaceFirst(regex, replacement));
            }
            return target;
        };
        
        // identical argNames from above
        
        functions.put("sub", new BuiltInFunctionDefinitionNode("sub", executeSub, subArgs));

        // Match
        Function<HashMap<String,InterpreterDataType>,String> executeMatch = (HashMap<String,InterpreterDataType> args) -> {
            String target = args.get("target").value;
            String regex = args.get("regex").value;

            // Regex matcher gets our match index for free
            Matcher matcher = Pattern.compile(regex).matcher(target);
            
            if(args.containsKey("varArray")){
                
                if(!(args.get("varArray") instanceof InterpreterArrayDataType arrayData))
                    throw new IllegalArgumentException("Match requires either an array to store into, or no third argument");
                
                HashMap<String, InterpreterDataType> extractedArray = arrayData.getArrayValue();
                int i = 1; // 1-indexed
                while(matcher.find())
                // Fill the array with every valid match
                    extractedArray.put(Integer.toString(i++), new InterpreterDataType(matcher.group()));
                
                if(i == 1)
                // If we didn't find any matches (i is untouched)
                    return "0";
                
                args.put("varArray", new InterpreterArrayDataType(extractedArray));
            } else if(!matcher.find())
            // No output array case: fail if we can't find a match
                return "0";
            
            return Integer.toString(matcher.start() + 1); // 1-indexed
        };
        
        functions.put("match", new BuiltInFunctionDefinitionNode("match", executeMatch, List.of(
                List.of("target", "regex"),
                List.of("target", "regex", "varArray")
        )));

        // Length
        Function<HashMap<String,InterpreterDataType>,String> executeLength = (HashMap<String,InterpreterDataType> args) -> {
           
            String target;
            if(args.containsKey("target"))
                target = args.get("target").value;
            else
                target = globalVariables.get("$0").value;

            return Integer.toString(target.length());
        };
        
        functions.put("length", new BuiltInFunctionDefinitionNode("length", executeLength, List.of(
                List.of("target"),
                List.of() // Defaults to $0
        )));


        // Index
        Function<HashMap<String,InterpreterDataType>,String> executeIndex = (HashMap<String,InterpreterDataType> args) -> {
            String string = args.get("string").value;
            String substring = args.get("substring").value;

            return Integer.toString(string.indexOf(substring) + 1); // 1-indexed
        };
        
        functions.put("index", new BuiltInFunctionDefinitionNode("index", executeIndex, 
                new LinkedList<String>(List.of("string", "substring"))
        )); // Only one valid argument set, no need for a list of lists

        
        // Substr
        Function<HashMap<String,InterpreterDataType>,String> executeSubstr = (HashMap<String,InterpreterDataType> args) -> {
            String string = args.get("string").value;
            int start = (int) Double.parseDouble(args.get("start").value);
            int length = args.containsKey("length") ? (int) Double.parseDouble(args.get("length").value) : string.length();
            // ^ Get length arg (as int) or default to length of string
            
            if(length < 0 || length > string.length())
                throw new IndexOutOfBoundsException("Substring length must be positive and less than the length of the original string");

            return string.substring(start - 1, length + 1); // 1-indexed, the length arg is exclusive
        };
        
        functions.put("substr", new BuiltInFunctionDefinitionNode("substr", executeSubstr, List.of(
                List.of("string", "start", "length"),
                List.of("string", "start")
        )));
        

        // ToLower
        Function<HashMap<String,InterpreterDataType>,String> executeToLower = (HashMap<String,InterpreterDataType> args) -> {
            String string = args.get("string").value;

            return string.toLowerCase();
        };
        
        functions.put("tolower", new BuiltInFunctionDefinitionNode("tolower", executeToLower, 
                new LinkedList<String>(List.of("string"))
        ));
        

        // ToUpper
        Function<HashMap<String,InterpreterDataType>,String> executeToUpper = (HashMap<String,InterpreterDataType> args) -> {
            String string = args.get("string").value;

            return string.toUpperCase();
        };
        
        functions.put("toupper", new BuiltInFunctionDefinitionNode("toupper", executeToUpper, 
                new LinkedList<String>(List.of("string"))
        ));


    }
    
    public static Optional<InterpreterDataType> getGlobalVariable(String name){
        if(globalVariables.containsKey(name))
            return Optional.of(globalVariables.get(name));
        else
            return Optional.empty();
    }
    
    
    
    public static boolean asBoolean(String value){
        if(value.isEmpty()) // "" is false
            return false;
        ;
        try {
        // If it's a number return that interpretation
            return Double.parseDouble(value) != 0;
        } catch (NumberFormatException e){
        // If it's not, we have a non-empty string (true)
            return true;
        }
        
    }
    
    private static String booleanAsString(Boolean bool){
        return bool ? "1" : "0";
    }
    
    private static class IncompatibleTypeException extends RuntimeException {
        public IncompatibleTypeException(String message){
            super(message);
        }
    }
}
