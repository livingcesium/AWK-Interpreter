import java.util.*;

public class Parser {
    private TokenManager tokens;
    
    // These will eventually be the ProgramNode: 
        private LinkedList<BlockNode> begin;
        private LinkedList<BlockNode> end;
        private LinkedList<BlockNode> other;
        private LinkedList<FunctionDefinitionNode> functions;
    
    public Parser(LinkedList<Token> list){
        tokens = new TokenManager(list);
        
        begin = new LinkedList<>(); 
        end = new LinkedList<>();
        other = new LinkedList<>(); 
        functions = new LinkedList<>();
    }
    
    private boolean acceptSeperators(){
        if(!tokens.moreTokens())
            return false;
        boolean found = false;
        while(tokens.matchAndRemove(Token.TokenType.SEPERATOR).isPresent()){
            found = true; // yes this runs a ton of times for no reason, but it reads well
        }
        return found;
    }
    
    public ProgramNode parse(){
        while(tokens.moreTokens()){
            acceptSeperators();
            if(!parseFunction() && !parseAction()){
                // try to parse function, try to parse action
                // if there is neither, throw a fit
                throw new RuntimeException(String.format("expected either function or action after %s", reportPosition()));
            }
            
        }
        return new ProgramNode(begin, end, other, functions);
    }
    
    private boolean parseFunction(){
        
        if(tokens.matchAndRemove(Token.TokenType.FUNCTION).isEmpty())
            return false;
        
        Optional<Token> maybeToken;
        if((maybeToken = tokens.matchAndRemove(Token.TokenType.WORD)).isEmpty())
            return false;
        
        String name = maybeToken.get().getValue();

        if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty())
            return false;

        LinkedList<String> paramNames = new LinkedList<>();
        
        if((maybeToken = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent()) {
            paramNames.add(maybeToken.get().getValue());
            
            while(tokens.matchAndRemove(Token.TokenType.COMMA).isPresent()){
                acceptSeperators();
                if((maybeToken = tokens.matchAndRemove(Token.TokenType.WORD)).isEmpty())
                    throw new RuntimeException(String.format("expected parameter name after comma, reached %s", reportPosition()));
                
                paramNames.add(maybeToken.get().getValue());
            }
        }
        
        if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
            return false;
        acceptSeperators();

        LinkedList<StatementNode> statements = new LinkedList<>();
        
        BlockNode block = parseBlock(); // { ... }
        
        if(paramNames.isEmpty())
            functions.add(new FunctionDefinitionNode(name, block.getStatements()));
        else
            functions.add(new FunctionDefinitionNode(name, block.getStatements(), paramNames));
        
        return true;
        
    }
    
    private BlockNode parseBlock(){
        
        LinkedList<StatementNode> statements = new LinkedList<>();
        
        if (tokens.matchAndRemove(Token.TokenType.LEFTBRACE).isEmpty()){
            Optional<StatementNode> statementNode;
            if((statementNode = parseStatement()).isEmpty())
                throw new RuntimeException(String.format("Could not parse one line statement, reached %s", reportPosition()));
            statements.add(statementNode.get());
            return new BlockNode(statements);
        }
        acceptSeperators();
        
        Optional<StatementNode> statementNode;
        
        while((statementNode = parseStatement()).isPresent()){
            statements.add(statementNode.get());
            acceptSeperators();
        }
        
        tokens.matchAndRemove(Token.TokenType.RIGHTBRACE);
        acceptSeperators();
        
        
        return new BlockNode(statements);
    }
    
    private Optional<StatementNode> parseStatement(){
        Optional<StatementNode> statement;
        if((statement = parseContinue()).isPresent()) {
            acceptSeperators();
            return statement;
        }
        if((statement = parseBreak()).isPresent()) {
            acceptSeperators();
            return statement;
        }
        if((statement = parseIf()).isPresent()){
            acceptSeperators();
            return statement;
        }
        if((statement = parseFor()).isPresent()){
            acceptSeperators();
            return statement;
        }
        if((statement = parseDelete()).isPresent()){
            acceptSeperators();
            return statement;
        }
        if((statement = parseWhile()).isPresent()){
            acceptSeperators();
            return statement;
        }
        if((statement = parseDoWhile()).isPresent()){
            acceptSeperators();
            return statement;
        }
        if((statement = parseReturn()).isPresent()){
            acceptSeperators();
            return statement;
        }
        Optional<Node> operation;
        
        // Get operation, then determine if it's a statement
        if((operation = parseOperation()).isPresent() && (statement = operation.get().tryGetStatement()).isPresent()) {
            acceptSeperators();
            return statement;
        }
        
        acceptSeperators();
        return Optional.empty();
    }
    private boolean parseAction(){
        LinkedList<BlockNode> output;
        Optional<Node> condition = Optional.empty();
        if((tokens.matchAndRemove(Token.TokenType.BEGIN)).isPresent())
            output = begin;
        else if((tokens.matchAndRemove(Token.TokenType.END)).isPresent())
            output = end;
        else {
            condition = parseOperation();
            output = other; // assumed, unsure from instructions
        }
        
        // Peek to foresee failure
        Optional<Token> seeker;
        if((seeker = tokens.peek(1)).isEmpty() || !seeker.get().isType(Token.TokenType.LEFTBRACE)) {
            System.out.printf("Looking for {, got %s instead", seeker.isPresent() ? seeker.get():"nothing");
            return false;
        }
        
        BlockNode block = parseBlock();
        
        // A little weird to make another one, but we need to account for conditions
        output.add(new BlockNode(block.getStatements(), condition)); // { ... }
        
        return true;
    }
    
    private String reportPosition(){
        return tokens.reportPosition();
    }
    
    private Optional<StatementNode> parseContinue(){
        if(tokens.matchAndRemove(Token.TokenType.CONTINUE).isEmpty())
            return Optional.empty();
        acceptSeperators();
        return Optional.of(new ASTnode.ContinueNode());
    }
    private Optional<StatementNode> parseBreak(){
        if(tokens.matchAndRemove(Token.TokenType.BREAK).isEmpty())
            return Optional.empty();
        acceptSeperators();
        return Optional.of(new ASTnode.BreakNode());
    }
    private Optional<StatementNode> parseDelete(){
        if(tokens.matchAndRemove(Token.TokenType.DELETE).isEmpty())
            return Optional.empty();
        
        Optional<Token> nameToken;
        if((nameToken = tokens.matchAndRemove(Token.TokenType.WORD)).isEmpty())
            throw new RuntimeException(String.format("Expected variable name after \"delete\", reached %s", reportPosition()));
        
        if(tokens.matchAndRemove(Token.TokenType.LEFTBRACKET).isEmpty())
            return Optional.of(new ASTnode.DeleteNode(new VariableReferenceNode(nameToken.get().getValue())));
        
        Optional<Node> index;
        LinkedList<Node> indices = new LinkedList<>();
        do{
            if((index = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse index for array, reached %s", reportPosition()));
            indices.add(index.get());
        } while(tokens.matchAndRemove(Token.TokenType.COMMA).isPresent());
        
        acceptSeperators();
        return Optional.of(new ASTnode.DeleteNode(new VariableReferenceNode(nameToken.get().getValue()), indices));
    }
    
    private Optional<StatementNode> parseReturn(){
        if(tokens.matchAndRemove(Token.TokenType.RETURN).isEmpty())
            return Optional.empty();
        
        Optional<Node> value;
        if((value = parseOperation()).isEmpty())
            throw new RuntimeException(String.format("Could not parse return value, reached %s", reportPosition()));
        
        acceptSeperators();
        return Optional.of(new ASTnode.ReturnNode(value.get()));
    }
    private Optional<StatementNode> parseFor(){
        if(tokens.matchAndRemove(Token.TokenType.FOR).isEmpty())
            return Optional.empty();
        if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \"(\" after \"for\", reached %s", reportPosition()));
        
        // Seek "in"
        Optional<Token> seeker;
        boolean forIn = false;
        int i = 1;
        while((seeker = tokens.peek(i++)).isPresent() && !seeker.get().isType(Token.TokenType.RIGHTPAREN)){
            if(forIn = seeker.get().isType(Token.TokenType.IN)) // Store value of boolean
                break;
        }
        
        Optional<StatementNode> forNode;
        BlockNode statements;
        if(forIn) {
            Optional<Node> operation = parseOperation(); // Should get to parseArrayMembership()
            if(operation.isEmpty())
                throw new RuntimeException(String.format("Could not parse for-in array, reached %s", reportPosition()));
            OperationNode membership = operation.get().getOperationOrThrow(String.format("Invalid expression in for-in array definition, reached %s", reportPosition()));
            if(membership.isRightOp(OperationNode.Operation.IN))
                throw new IllegalArgumentException(String.format("Cannot use multidimensional index in for-in array definition, reached %s", reportPosition()));
            VariableReferenceNode left = membership.getLeft().getVariableReferenceOrThrow("Cannot use non-variable in for-in array definition, reached %s");

            if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
                throw new RuntimeException(String.format("Expected \")\" after for loop's increment, reached %s", reportPosition()));
            acceptSeperators();

            statements = parseBlock();
            return Optional.of(new ASTnode.ForNode(left, membership.getRight().get(), statements)); // Freebie get() here should be safe
        } else {
            Optional<Node> init;
            Optional<Node> condition;
            Optional<Node> update;

            if((init = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse operation in for loop's assignment, reached %s", reportPosition()));
            if(tokens.matchAndRemove(Token.TokenType.SEPERATOR).isEmpty())
                throw new RuntimeException(String.format("Expected \";\" after for loop's assignment, reached %s", reportPosition()));
            if((condition = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse operation in for loop's condition, reached %s", reportPosition()));
            if(tokens.matchAndRemove(Token.TokenType.SEPERATOR).isEmpty())
                throw new RuntimeException(String.format("Expected \";\" after for loop's condition, reached %s", reportPosition()));
            if((update = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse operation in for loop's increment, reached %s", reportPosition()));
            
            if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
                throw new RuntimeException(String.format("Expected \")\" after for loop's increment, reached %s", reportPosition()));
            acceptSeperators();

            statements = parseBlock();
            return Optional.of(new ASTnode.ForNode(init.get(), condition.get(), update.get(), statements));
        }
    }
    
    private Optional<StatementNode> parseDoWhile(){
        if(tokens.matchAndRemove(Token.TokenType.DO).isEmpty())
            return Optional.empty();
        
        BlockNode statements = parseBlock();
        
        if(tokens.matchAndRemove(Token.TokenType.WHILE).isEmpty())
            throw new RuntimeException(String.format("Expected \"while\" after \"do\", reached %s", reportPosition()));
        
        if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \"(\" after \"while\", reached %s", reportPosition()));
        
        Optional<Node> condition;
        if((condition = parseOperation()).isEmpty())
            throw new RuntimeException(String.format("Could not parse condition for do-while loop, reached %s", reportPosition()));
        
        if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \")\" after condition for do-while loop, reached %s", reportPosition()));
        acceptSeperators();
        
        return Optional.of(new ASTnode.WhileNode(condition.get(), statements, true));
    }
    private Optional<StatementNode> parseWhile(){
        if(tokens.matchAndRemove(Token.TokenType.WHILE).isEmpty())
            return Optional.empty();
        
        if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \"(\" after \"while\", reached %s", reportPosition()));
        
        Optional<Node> condition;
        if((condition = parseOperation()).isEmpty())
            throw new RuntimeException(String.format("Could not parse condition for while loop, reached %s", reportPosition()));
        
        if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \")\" after condition for while loop, reached %s", reportPosition()));
        acceptSeperators();
        
        BlockNode statements = parseBlock();

        return Optional.of(new ASTnode.WhileNode(condition.get(), statements));
        
            
    }
    
    private Optional<StatementNode> parseIf(){
        
        Optional<Node> condition;
        if((condition = getIfHeader()).isEmpty())
            return Optional.empty();
        
        BlockNode statements = parseBlock();
        
        
        ASTnode.IfNode ifNode = new ASTnode.IfNode(condition.get(), statements);
        ASTnode.IfNode elseNode;
        
        Optional<Node> nextCondition;
        ASTnode.IfNode currentNode = ifNode;
        boolean finalElse = false;
        while(!finalElse && tokens.matchAndRemove(Token.TokenType.ELSE).isPresent()){
            
            nextCondition = getIfHeader();
            if(finalElse = nextCondition.isPresent()) // Save the result
                elseNode = new ASTnode.IfNode(nextCondition.get(), parseBlock());
            else 
                elseNode = new ASTnode.IfNode(new ConstantNode<Boolean>(true), parseBlock());
            
            currentNode.setElse(elseNode);
            currentNode = elseNode;
        }
        
        return Optional.of(ifNode);
        
    }
    // Parses "if (...) and returns the condition"
    private Optional<Node> getIfHeader(){
        if(tokens.matchAndRemove(Token.TokenType.IF).isEmpty())
            return Optional.empty();
        Optional<Node> condition;
        
        if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \"(\" after \"if\", reached %s", reportPosition()));

        if((condition = parseOperation()).isEmpty())
            throw new RuntimeException(String.format("Could not parse condition for if statement, reached %s", reportPosition()));
        
        if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \")\" after condition for if statement, reached %s", reportPosition()));
        acceptSeperators();
        
        return condition;
    }
    
    public Optional<Node> parseOperation(){
        
        Optional<Node> expression;
        Optional<Node> finalExpression;
        
        // We always start with the bottom level, so if that's not found then we have nothing.
        
        if((expression = parseBottomLevel()).isEmpty()) // expression is a factor here
            return Optional.empty();
        
        if((expression = parseMathOperations(expression, true)).isEmpty()) // Expression becomes a term or stays a factor here
            // *, /, % only
            throw new IllegalStateException(String.format("Could not parse term, parseLeftAssociative() is likely broken. Reached %s", reportPosition()));
        
        finalExpression = expression;
        
        if((expression = parseMathOperations(finalExpression, false)).isPresent())  // Expression is now fully represented, at least in terms of math operations
            // +, - only
            finalExpression = expression; // Single term expression is valid, and common.
        
        if((expression = parseStringConcat(finalExpression)).isPresent())
            finalExpression = expression;
        
        if((expression = parseNonAssociative(finalExpression)).isPresent()) 
            // <, <=, !=, ==, >, >=  
            finalExpression = expression;
        
        if((expression = parseArrayMembership(finalExpression, false)).isPresent())
            finalExpression = expression;

        if((expression = parseLogical(finalExpression)).isPresent())
            // && and ||
            finalExpression = expression;
        
        if((expression = parseTernary(finalExpression)).isPresent())
            finalExpression = expression;
        
        if((expression = parseRightAssociative(true, finalExpression)).isPresent()) // Assignments
            finalExpression = expression;
        
        
        return finalExpression; // The highest precedence level is the default, retrieved from parseBottomLevel()
        
    }
    
    private Optional<Node> parseTernary(Optional<Node> first){
        if(first.isEmpty())
            return Optional.empty();
        ArrayDeque<Node> nodes = new ArrayDeque<>(List.of(first.get())); // Gather all expressions and work backwards
        Optional<Node> node;
        while(tokens.matchAndRemove(Token.TokenType.QUESTION).isPresent()){
            
            if((node = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse true case in ternary expression, reached %s", reportPosition()));
            nodes.push(node.get());
            
            if(tokens.matchAndRemove(Token.TokenType.COLON).isEmpty())
                throw new RuntimeException(String.format("Expected \":\" in ternary expression, reached %s", reportPosition()));
            
            if((node = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse false case in ternary expression, reached %s", reportPosition()));
            nodes.push(node.get());
        }
        if(nodes.size() == 1) // No ternary expressions found
            return Optional.empty();
        
        if(nodes.size() % 3 != 0)
            throw new RuntimeException(String.format("Ternary expression has an incorrect number of expressions (%d), reached %s", nodes.size(),reportPosition()));
        
        Node falseCase = nodes.pop(); // ... : condition ? trueCase : falseCase will be ..., condition, trueCase, falseCase with falseCase popped first
        Node trueCase = nodes.pop();
        
        TernaryNode finalTernary = new TernaryNode(nodes.pop(), trueCase, falseCase);
        while(!nodes.isEmpty()){
            // the pattern goes backwards as newCondition ? newTrueCase : (lastCondition ? lastTrueCase : lastFalseCase)
            trueCase = nodes.pop();
            falseCase = finalTernary;
            finalTernary = new TernaryNode(nodes.pop(), trueCase, falseCase);
        }
        
        return Optional.of(finalTernary);
        
    }
    private Optional<Node> parseArrayMembership(Optional<Node> first, boolean multiDimensional){
        
        if(first.isEmpty())
            throw new RuntimeException(String.format("Could not parse array index, reached %s", reportPosition()));
        Optional<Node> nextIndex;
        
        if((nextIndex = first.get().getNext()).isEmpty() && multiDimensional)
            return Optional.empty(); // Not multidimensional, fail
        
        if(tokens.matchAndRemove(Token.TokenType.IN).isEmpty())
            return Optional.empty(); // Not an array membership check, fail
        
        Optional<Node> node;
        if((node = parseBottomLevel()).isEmpty()) 
            throw new RuntimeException(String.format("Could not parse array, reached %s", reportPosition()));

        
        VariableReferenceNode targetArray = node.get().getVariableReferenceOrThrow(String.format("Cannot do array membership check on non-variable, encountered by %s", reportPosition()));
        OperationNode output = new OperationNode(first.get(), OperationNode.Operation.IN, targetArray);
        
        if(multiDimensional){
            while(nextIndex.isPresent()){
                output = new OperationNode(nextIndex.get(), OperationNode.Operation.IN, output);
                nextIndex = nextIndex.get().getNext();
            }
        }
        return Optional.of(output);
    }
    private Optional<Node> parseLogical(Optional<Node> first){
        if(first.isEmpty())
            return Optional.empty();
        
        Optional<Node> left = first;
        Optional<Node> right;
        OperationNode.Operation op = OperationNode.Operation.NOTHING;
        
        if(tokens.matchAndRemove(Token.TokenType.AND).isPresent())
            op = OperationNode.Operation.AND;
        else if(tokens.matchAndRemove(Token.TokenType.OR).isPresent())
            op = OperationNode.Operation.OR;
        
        while(op != OperationNode.Operation.NOTHING){
            if((right = parseOperation()).isEmpty())
                throw new RuntimeException(String.format("Could not parse right-hand side of logical expression, reached %s", reportPosition()));
            
            left = Optional.of(new OperationNode(left.get(), op, right.get()));
            
            // Update op before looping
            if(tokens.matchAndRemove(Token.TokenType.AND).isPresent())
                op = OperationNode.Operation.AND;
            else if(tokens.matchAndRemove(Token.TokenType.OR).isPresent())
                op = OperationNode.Operation.OR;
        }
        
        return left;
    }
    // Comparisons:
    private Optional<Node> parseNonAssociative(Optional<Node> first){
        if(first.isEmpty())
            return Optional.empty();

        LinkedHashMap<Token.TokenType, OperationNode.Operation> symbolToOperation = new LinkedHashMap<>();
            symbolToOperation.put(Token.TokenType.LESS, OperationNode.Operation.LESSTHAN);
            symbolToOperation.put(Token.TokenType.LESSOREQUAL, OperationNode.Operation.LESSOREQUAL);
            symbolToOperation.put(Token.TokenType.NOTEQUAL, OperationNode.Operation.NOTEQUAL);
            symbolToOperation.put(Token.TokenType.EQUAL, OperationNode.Operation.EQUAL);
            symbolToOperation.put(Token.TokenType.GREATER, OperationNode.Operation.GREATERTHAN);
            symbolToOperation.put(Token.TokenType.GREATEROREQUAL, OperationNode.Operation.GREATEROREQUAL);
            symbolToOperation.put(Token.TokenType.MATCH, OperationNode.Operation.MATCH);
            symbolToOperation.put(Token.TokenType.NOMATCH, OperationNode.Operation.NOTMATCH);
            
        for(Token.TokenType type : symbolToOperation.keySet()){
            if(tokens.matchAndRemove(type).isPresent()){
                OperationNode.Operation op = symbolToOperation.get(type);
                Optional<Node> next;
                if((next = parseBottomLevel()).isEmpty())
                    throw new RuntimeException(String.format("Expected expression for operation %s, encountered by %s", op, reportPosition()));
                return Optional.of(new OperationNode(first.get(), op, next.get()));
            }
        }
        // If we get here, we didn't find any of the above operations
        return Optional.empty();
    }
    
    // In terms of the rubric, this is factor AND term. Sorry for making you step into the grave I dug for myself.
    // Simple math operations (String concat and array stuff separate):
    private Optional<Node> parseMathOperations(Optional<Node> first, boolean term){
        if(first.isEmpty())
            return Optional.empty();
        
        LinkedHashMap<Token.TokenType, OperationNode.Operation> symbolToOperation = new LinkedHashMap<>(); // Linked for order-preservation
        
        // Term determines if we're looking for whole expressions (like for addition/logic) or just terms (like for multiplication)
        if(term) {
            symbolToOperation.put(Token.TokenType.MULTIPLY, OperationNode.Operation.MULTIPLY);
            symbolToOperation.put(Token.TokenType.DIVIDE, OperationNode.Operation.DIVIDE);
            symbolToOperation.put(Token.TokenType.MODULO, OperationNode.Operation.MODULO);
        } else {
            symbolToOperation.put(Token.TokenType.PLUS, OperationNode.Operation.ADD);
            symbolToOperation.put(Token.TokenType.MINUS, OperationNode.Operation.SUBTRACT);
            symbolToOperation.put(Token.TokenType.AND, OperationNode.Operation.AND);
            symbolToOperation.put(Token.TokenType.OR, OperationNode.Operation.OR);
        }
        
        OperationNode.Operation op = OperationNode.Operation.NOTHING;
        Token.TokenType type = null; // Java is forcing me to do this, but this should never get past the for loop.
        
        // Go through all possible operations until a match is found, then store it in op
        for (Token.TokenType t : symbolToOperation.keySet()) {
            if(tokens.matchAndRemove(t).isPresent()){
                op = symbolToOperation.get(t);
                type = t;
                break;
            }
        }
        
        if(op == OperationNode.Operation.NOTHING || type == null) { // Redundant OR here but whatever, lets not play games with null
            return parseRightAssociative(false, first); // Return partial. Also handles exponents for us, neat.
        }
        // Operation found, building output
        
        Optional<Node> currentNode = first;
        Optional<Node> nextNode;
        OperationNode finalOperation;
        
        do {
            // Ternary below enforces the order of operations for PEMDAS operations.
            // If we have a term, we parseBottomLevel() to find a factor for it. ex: (x+y) * 3<- factor 3 ... 3(x+y) is a term
            // If we have an expression, we recurse to find a term to add to it. ex: (x+y) + 3 * z<- term (3*z) ... (x+y) + (3*z) is an expression with two terms
            
            // We also parse for exponents here, with a call to parseRightAssociative() where assignment is false.
            // We don't want to resort to entering the murky waters of parseOperations() for that.
            
            // nextNode := checkExponents(bottomLevel()) OR nextNode = checkTerm(checkExponents(bottomLevel())) (the call to self, checkTerm() here, handles some more exponents)
            if((nextNode = term ? parseRightAssociative(false,parseBottomLevel()) : parseMathOperations(parseRightAssociative(false, parseBottomLevel()), true)).isPresent()){
                finalOperation = new OperationNode(currentNode.get(), op, nextNode.get());
                currentNode = Optional.of(finalOperation);
            } else 
                throw new RuntimeException(String.format("Expected expression for operation %s, encountered by %s", op, reportPosition()));
        
        } while ((tokens.matchAndRemove(type)).isPresent());
        
        return Optional.of(finalOperation);
    }
    private Optional<Node> parseStringConcat(Optional<Node> first){
        if(first.isEmpty() || first.get().tryGetConstant(String.class).isEmpty() || first.get().isVariableReference())
            return Optional.empty();

        OperationNode finalOperation;
        Optional<Node> current = first;
        Optional<Node> next;

        boolean entered = false;

        if((next = parseOperation()).isEmpty())
            return Optional.empty();

        finalOperation = new OperationNode(current.get(), OperationNode.Operation.CONCATENATION, next.get());

        while((next = parseOperation()).isPresent()){
            finalOperation = new OperationNode(finalOperation, OperationNode.Operation.CONCATENATION, next.get());

            if(!entered)
                entered = true;

            if(current.get().tryGetConstant(String.class).isEmpty() || first.get().isVariableReference())
                throw new RuntimeException(String.format("Expected string or variable for string concatenation, encountered by %s", reportPosition()));

            current = next;
        }

        return Optional.of(finalOperation);
    }
    
    // Exponents and assignments. Only safe to fail when not assignment
    private Optional<Node> parseRightAssociative(boolean assignment, Optional<Node> first) {
        // Since exponents are the only right associative that aren't an assignment... 
        // (ternarys are a little weird, so they get their own function)
        // ...we use a boolean to single it out.
        if(first.isEmpty())
            return Optional.empty();
        
        if(assignment && !first.get().isVariableReference())
            return Optional.empty();
        
        OperationNode.Operation op = null;
        Token.TokenType type = null;
        LinkedHashMap<Token.TokenType, OperationNode.Operation> assignmentToOperation = new LinkedHashMap<>();
        ArrayDeque<Node> targets = new ArrayDeque<>(List.of(first.get()));
        Optional<Node> currentNode;

        if (assignment) {
            assignmentToOperation.put(Token.TokenType.ASSIGNPOWER, OperationNode.Operation.EXPONENT);
            assignmentToOperation.put(Token.TokenType.ASSIGNMODULO, OperationNode.Operation.MODULO);
            assignmentToOperation.put(Token.TokenType.ASSIGNMULTIPLY, OperationNode.Operation.MULTIPLY);
            assignmentToOperation.put(Token.TokenType.ASSIGNDIVIDE, OperationNode.Operation.DIVIDE);
            assignmentToOperation.put(Token.TokenType.ASSIGNADD, OperationNode.Operation.ADD);
            assignmentToOperation.put(Token.TokenType.ASSIGNSUBTRACT, OperationNode.Operation.SUBTRACT);
            assignmentToOperation.put(Token.TokenType.ASSIGN, OperationNode.Operation.NOTHING);
        }

        if (!assignment) { // Exponentiation is the only case that enters here

            if (tokens.matchAndRemove(Token.TokenType.POWER).isEmpty())
                return first;
            op = OperationNode.Operation.EXPONENT;
            type = Token.TokenType.POWER;

            if ((currentNode = parseOperation()).isEmpty()) // Get right hand side
                throw new RuntimeException(String.format("Could not parse right side of operation %s, encountered by %s", op, reportPosition()));
            targets.push(currentNode.get());

            // Add all subjects of exponentiation to a stack for later
            while (tokens.matchAndRemove(type).isPresent()) {
                if ((currentNode = parseOperation()).isEmpty())
                    throw new RuntimeException(String.format("Could not parse a right-hand side in chain of operation %s, encountered by %s", op, reportPosition()));
                targets.push(currentNode.get());
            }

        } else {
            // We loop through each token type and get it's corresponding operation
            for (Token.TokenType t : assignmentToOperation.keySet()) {
                if (tokens.matchAndRemove(t).isPresent()) {
                    type = t;
                    op = assignmentToOperation.get(type);
                    break; // Save us some time.
                }
            }

            if (op == null)
                return Optional.empty(); // Soft fail if an assignment is not found

            if ((currentNode = parseOperation()).isEmpty()) // Get right hand side
                throw new RuntimeException(String.format("Could not parse right side of assignment %s, encountered by %s", op, reportPosition()));
            targets.push(currentNode.get());

            // If there are more, add all subjects of assignment to a stack for later
            while (tokens.matchAndRemove(type).isPresent()) {
                if ((currentNode = parseOperation()).isEmpty())
                    throw new RuntimeException(String.format("Could not parse a right-hand side in chain of assignment %s, encountered by %s", op == OperationNode.Operation.NOTHING ? "": op , reportPosition()));
                targets.push(currentNode.get());
            }
        }

        
        if (assignment) {
            Node value = targets.pop(); // ... ?= target ?= value will be ...,target,value with value popped first
            VariableReferenceNode target = targets.pop().getVariableReferenceOrThrow(String.format("Cannot assign to an non-variable, encountered by %s", reportPosition()));

            AssignmentNode finalAssignment;
            Boolean isGenericAssignment = op == OperationNode.Operation.NOTHING;
            if(!isGenericAssignment)
                finalAssignment = new AssignmentNode(target, new OperationNode(target, op, value));
            else
                finalAssignment = new AssignmentNode(target, value);
            while (!targets.isEmpty()) {
                // the pattern goes backwards as ... ?= target ?= (lastTarget ?= lastValue) [ ?= is generic assignment ]
                target = targets.pop().getVariableReferenceOrThrow(String.format("Cannot assign to an non-variable, encountered by %s", reportPosition()));
                // only the tail of this chain can be a non-variable. Think about it! Otherwise, a constant is being assigned (garbage nonsense).
                if(!isGenericAssignment)
                    finalAssignment = new AssignmentNode(target, new OperationNode(target, op, finalAssignment));
                else
                    finalAssignment = new AssignmentNode(target, finalAssignment);
            }
            return Optional.of(finalAssignment);
        } else {
            Node right = targets.pop();
            Node left = targets.pop(); // ... ^ left ^ right in the list will be ...,left,right with right popped first
            
            OperationNode finalOperation = new OperationNode(left, op, right);
            while (!targets.isEmpty()) {
                // the pattern goes backwards as ... ^ newLeft ^ (lastLeft^lastRight)
                left = targets.pop();
                
                finalOperation = new OperationNode(left, op, finalOperation);
            }
            return Optional.of(finalOperation);
        }
        
        
    }
    
    private Optional<AssignmentNode> parsePostOperators(VariableReferenceNode target){
        OperationNode.Operation op;
        
        if(tokens.matchAndRemove(Token.TokenType.INCREMENT).isPresent()) {
            op = OperationNode.Operation.POSTINCREMENT;
            return Optional.of(new AssignmentNode(target, new OperationNode(target, op)));
        } else if(tokens.matchAndRemove(Token.TokenType.DECREMENT).isPresent()){
            op = OperationNode.Operation.POSTDECREMENT;
            
            return Optional.of(new AssignmentNode(target, new OperationNode(target, op)));
        } else
            return Optional.empty();
    }
    
    private Optional<Node> parseBottomLevel(){
        if(!tokens.moreTokens())
            return Optional.empty();
        
        Token.TokenType[] typesToSeek = {
          Token.TokenType.STRINGLITERAL,
          Token.TokenType.NUMBER,
          Token.TokenType.REGEXLITERAL,
          Token.TokenType.LEFTPAREN,
          Token.TokenType.NOT,
          Token.TokenType.MINUS,
          Token.TokenType.PLUS, 
          Token.TokenType.POWER,      
          Token.TokenType.INCREMENT,
          Token.TokenType.DECREMENT
        };
        
        for(Token.TokenType type : typesToSeek){
            Optional<Token> token = tokens.matchAndRemove(type);
            if(token.isPresent()){
                Optional<Node> temp; // This is used when we have some work to do before using the output of some Node-returning function
                switch(type){
                    case STRINGLITERAL -> {
                        return Optional.of(new ConstantNode<String>(token.get().getValue()));
                    }
                    case NUMBER -> {
                        return Optional.of(new ConstantNode<Double>(Double.valueOf(token.get().getValue())));
                    }
                    case REGEXLITERAL -> {
                        return Optional.of(new RegexNode(token.get().getValue()));
                    }
                    case LEFTPAREN -> {
                        if((temp = parseOperation()).isEmpty())
                            throw new RuntimeException(String.format("Could not parse parenthesized operation, reached %s", reportPosition()));
                        
                        // If we get a list of stuff, it might be a parenthesized list of indices for an array membership check
                        Optional<Node> current = temp;
                        Optional<Node> next;
                        while(tokens.matchAndRemove(Token.TokenType.COMMA).isPresent()){
                            next = parseOperation();
                            if(next.isEmpty())
                                throw new RuntimeException(String.format("Could not parse expression in parenthesized list, reached %s", reportPosition()));
                            current.get().setNext(next.get());
                            current = next;
                        }
                        if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
                            throw new RuntimeException(String.format("Expected closing parenthesis for expression, reached %s", reportPosition()));

                        if(temp.get().getNext().isPresent()) // We have a list
                            return parseArrayMembership(temp, true);
                        
                        return temp; // Single expression
                    }
                    case NOT -> {
                        if((temp = parseOperation()).isEmpty())
                            throw new RuntimeException(String.format("Could not parse NOT operation, reached %s", reportPosition()));

                        return Optional.of(new OperationNode(temp.get(), OperationNode.Operation.NOT));
                    }
                    case MINUS -> {
                        if((temp = parseOperation()).isEmpty())
                            throw new RuntimeException(String.format("Could not parse subtraction operation, reached %s", reportPosition()));
                        
                        return Optional.of(new OperationNode(temp.get(), OperationNode.Operation.UNARYNEG));
                    }
                    case PLUS -> {
                        if((temp = parseOperation()).isEmpty())
                            throw new RuntimeException(String.format("Could not parse addition operation, reached %s", reportPosition()));

                        return Optional.of(new OperationNode(temp.get(), OperationNode.Operation.UNARYPOS));
                    }
                    case INCREMENT -> {
                        if((temp = parseOperation()).isEmpty())
                            throw new RuntimeException(String.format("Could not parse increment operation, reached %s", reportPosition()));

                        return Optional.of(
                                new AssignmentNode(
                                        temp.get().getVariableReferenceOrThrow(String.format("Cannot assign to an non-variable, encountered by %s", reportPosition())),
                                        new OperationNode(temp.get(), OperationNode.Operation.PREINCREMENT)
                                )
                        );
                    }
                    case DECREMENT -> {
                        if((temp = parseOperation()).isEmpty())
                            throw new RuntimeException(String.format("Could not parse decrement operation, reached %s", reportPosition()));

                        return Optional.of(
                                new AssignmentNode(
                                        temp.get().getVariableReferenceOrThrow(String.format("Cannot assign to an non-variable, encountered by %s", reportPosition())),
                                        new OperationNode(temp.get(), OperationNode.Operation.PREDECREMENT)
                                )
                        );
                    }
                }
            }
        }
        Optional<Node> knownFunction;
        if((knownFunction = parseKnownFunctions()).isPresent())
            return knownFunction;
        else 
            return parseLvalue();
    }
    
    private Optional<Node> parseKnownFunctions(){
        LinkedList<Token.TokenType> typesToSeek = new LinkedList<>(List.of(
                Token.TokenType.PRINT,
                Token.TokenType.PRINTF,
                Token.TokenType.NEXT,
                Token.TokenType.NEXTFILE,
                Token.TokenType.GETLINE,
                Token.TokenType.EXIT,
                Token.TokenType.GSUB,
                Token.TokenType.MATCHFUNC,
                Token.TokenType.SUB,
                Token.TokenType.INDEX,
                Token.TokenType.LENGTH,
                Token.TokenType.SUBSTR,
                Token.TokenType.TOLOWER,
                Token.TokenType.TOUPPER
        ));
        Token.TokenType type = null;
        for(Token.TokenType t : typesToSeek){
            if (tokens.matchAndRemove(t).isPresent()) {
                type = t;
                break;
            }
        }
        
        if(type == null)
            return Optional.empty();
        
        boolean parenthesized = tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty();

        LinkedList<Node> arguments = getArguments();

        if(parenthesized && tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \")\" after function call, reached %s", reportPosition()));
        acceptSeperators();
        
        return Optional.of(new FunctionCallNode(type.toString().toLowerCase(), arguments));
        
    }
    
    private Optional<Node> parseFunctionCall(String name){
        if(!tokens.moreTokens())
            return Optional.empty();
        
        if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isEmpty())
            return Optional.empty();
        
        LinkedList<Node> arguments = getArguments();
        
        if(tokens.matchAndRemove(Token.TokenType.RIGHTPAREN).isEmpty())
            throw new RuntimeException(String.format("Expected \")\" after function call, reached %s", reportPosition()));
        
        acceptSeperators();
        return Optional.of(new FunctionCallNode(name, arguments));
    }
    
    private LinkedList<Node> getArguments(){
        Optional<Node> argument;
        LinkedList<Node> arguments = new LinkedList<>();

        if((argument = parseOperation()).isPresent()) // Add first argument if present
            arguments.add(argument.get());
        
        if(!arguments.isEmpty()) // If we have one, loop for more
            while(tokens.matchAndRemove(Token.TokenType.COMMA).isPresent()){
                if((argument = parseOperation()).isEmpty()) {
                    throw new RuntimeException(String.format("Could not parse argument, reached %s", reportPosition()));
                }
                arguments.add(argument.get());
            }
        
        return arguments;
    }
    private Optional<Node> parseLvalue(){
        if(!tokens.moreTokens())
            throw new IndexOutOfBoundsException("Out of tokens");
        Optional<Node> value;
        
        if(tokens.matchAndRemove(Token.TokenType.DOLLAR).isPresent()){
            value = parseBottomLevel();
            if(value.isEmpty())
                throw new RuntimeException(String.format("Could not evaluate lvalue, reached %s", reportPosition()));
            return Optional.of(new FieldReferenceNode(value.get()));
        }
        
        Optional<Token> nameToken;
        VariableReferenceNode reference;
        Optional<AssignmentNode> assignment; // if we find post-operator
        if(( nameToken = tokens.matchAndRemove(Token.TokenType.WORD) ).isPresent()){
            
            if(tokens.matchAndRemove(Token.TokenType.LEFTPAREN).isPresent()){
                return parseFunctionCall(nameToken.get().getValue());
            }
            
            // Array Reference
            if(tokens.matchAndRemove(Token.TokenType.LEFTBRACKET).isPresent()){
                if((value = parseOperation()).isEmpty())
                    throw new RuntimeException(String.format("Could not evaluate index, reached %s", reportPosition()));
                
                if(tokens.matchAndRemove(Token.TokenType.RIGHTBRACKET).isEmpty())
                    throw new RuntimeException(String.format("Missing \"]\", Unclosed array reference? Reached %s", reportPosition()));
                
                reference = new VariableReferenceNode(nameToken.get().getValue(), value.get());

            } else
                // Variable Reference
                reference = new VariableReferenceNode(nameToken.get().getValue());
            
            // Check for post-operators
            if((assignment = parsePostOperators(reference)).isPresent())
                return Optional.of(assignment.get());
            else
                return Optional.of(reference);
        
        } else 
            return Optional.empty();
        
    }
}
