import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
public class Lexer {
    private final StringHandler inputReader;
    private final static int LINE = 0, COL = 1; //for readability in regard to the array below
    private final int[] lineCol = {1,0};
    private HashMap<String, Token.TokenType> knownWords;
    private HashMap<String, Token.TokenType> knownSymbols;
    private HashMap<String, Token.TokenType> knownSymbolPairs;
    public Lexer(String document){
        knownWords = new HashMap<String, Token.TokenType>();
        populateKnownWords();
        knownSymbols = new HashMap<String, Token.TokenType>();
        knownSymbolPairs = new HashMap<String, Token.TokenType>();
        populateKnownSymbols();
        inputReader = new StringHandler(document);
    }
    
    public LinkedList<Token> lex(){
        
        LinkedList<Token> output = new LinkedList<Token>();
        while(!inputReader.isDone()){
            char c = inputReader.peekString(1).charAt(0);
            
            if( c == '#'){
                while(c != '\n'){
                    inputReader.swallow(1);
                    if(!inputReader.isDone())
                        c = inputReader.peekString(1).charAt(0);
                    else{
                        return output; //If we reach end of file, then this comment closes the document, and we're good to go
                    }
                } 
                    
            }
            
            if( c == ' ' || c == '\t'){
                lineCol[COL]++;
                inputReader.swallow(1);
            } else if( c == '\n'){
                lineCol[COL]++;
                inputReader.swallow(1);
                output.add(new Token(lineCol[LINE], lineCol[COL], Token.TokenType.SEPERATOR)); 
                
                lineCol[LINE]++;
                lineCol[COL] = 0;
            } else if( c == '\r'){
                //lineCol[COL]--; // Pretend like we never even saw this
                inputReader.swallow(1);
            } else if(String.valueOf(c).matches("[a-zA-Z]"))
                output.add(processWord());
            else if(String.valueOf(c).matches("[0-9]"))
                output.add(processDigit());
            else if(c == '"' || c == '“'){
                inputReader.swallow(1); lineCol[COL]++;
                output.add(handleStringLiteral());
            } else if(c == '`'){
                inputReader.swallow(1); lineCol[COL]++;
                output.add(handleRegexLiteral());
            } else {
                Token token;
                if((token = processSymbol()) != null)
                    output.add(token);
                else throw new RuntimeException(String.format("Unrecognized Character %s, at [%d, %d]", c, lineCol[LINE], lineCol[COL]));
            }
                
        }
        
        //tail-end separator if needed
        if(!output.getLast().isType(Token.TokenType.SEPERATOR))
            output.add(new Token(lineCol[LINE], ++lineCol[COL], Token.TokenType.SEPERATOR));
        
        return output;
    }
    
    private Token processWord(){
        String[] acceptedCharacters = {"[a-zA-Z]", "[0-9]", "_"}; //these are regexes
        StringBuilder accumulator = new StringBuilder();
        Token output;
        while(!inputReader.isDone()){
            boolean found = false;
            
            String c = inputReader.peekString(1);
            for (String regex : acceptedCharacters) {
                if(c.matches(regex)){
                    found = true;
                    accumulator.append(c); inputReader.swallow(1);
                    lineCol[COL]++;
                    break; //ICKY NASTY BREAK saves me headache
                }
            }
            if(!found) {
                Token.TokenType type;
                return identifyWord(accumulator.toString()); // returns keyword or regular word
            }
        }
        
        return null;
    }
    
    private Token processDigit(){
        boolean decimalFound = false;
        StringBuilder accumulator = new StringBuilder();
        while(!inputReader.isDone()){
            boolean found = false;

            String c = inputReader.peekString(1);
            
            if(c.matches("[0-9]")){
                found = true;
                accumulator.append(c); inputReader.swallow(1);
                lineCol[COL]++;
            }else if(c.equals(".") && !decimalFound){
                decimalFound = true;
                found = true;
                accumulator.append(c); inputReader.swallow(1);
                lineCol[COL]++;
            }
            
            if(!found || inputReader.isDone()) {
                if(!accumulator.isEmpty())
                    return new Token(lineCol[LINE], lineCol[COL], Token.TokenType.NUMBER, accumulator.toString());
            }
        }

        return null;
    }
    
    private Token processSymbol(){
        if(!inputReader.isDone()) {
            
            Token token;
            String symbol;
            if(inputReader.untilDone() < 2)
                symbol = inputReader.peekString(1); //redundant but it stops an edge case.
            else 
                symbol = inputReader.peekString(2);
            
            if (knownSymbolPairs.containsKey(symbol)) { // see if symbol matches the two symbol list
                
                lineCol[COL] += 2;
                inputReader.swallow(2);
                token = new Token(lineCol[LINE], lineCol[COL], knownSymbolPairs.get(symbol), symbol);
                
                return token;
            } else if(knownSymbols.containsKey( (symbol = inputReader.peekString(1)) )) { // get just the first character and see if it matches the single symbol list
                
                Token.TokenType type = knownSymbols.get(symbol);
                inputReader.swallow(1);
                
                if(type == Token.TokenType.SEPERATOR){
                    lineCol[COL]++;
                    token = new Token(lineCol[LINE], lineCol[COL], type);
                    lineCol[LINE]++;
                    lineCol[COL] = 0;
                } else {
                    lineCol[COL]++;
                    token = new Token(lineCol[LINE], lineCol[COL], type, symbol);
                    
                }
                
                return token;
            } else 
                return null;
            
        } else 
            throw new RuntimeException(String.format("End of document reached at [%d,%d] while processing symbol", lineCol[LINE], lineCol[COL]));
        
    }
    
    private void populateKnownWords() {
        knownWords.put("while", Token.TokenType.WHILE);
        knownWords.put("if", Token.TokenType.IF);
        knownWords.put("do", Token.TokenType.DO);
        knownWords.put("for", Token.TokenType.FOR);
        knownWords.put("break", Token.TokenType.BREAK);
        knownWords.put("continue", Token.TokenType.CONTINUE);
        knownWords.put("else", Token.TokenType.ELSE);
        knownWords.put("return", Token.TokenType.RETURN);
        knownWords.put("BEGIN", Token.TokenType.BEGIN);
        knownWords.put("END", Token.TokenType.END);
        knownWords.put("print", Token.TokenType.PRINT);
        knownWords.put("printf", Token.TokenType.PRINTF);
        knownWords.put("next", Token.TokenType.NEXT);
        knownWords.put("in", Token.TokenType.IN);
        knownWords.put("delete", Token.TokenType.DELETE);
        knownWords.put("getline", Token.TokenType.GETLINE);
        knownWords.put("exit", Token.TokenType.EXIT);
        knownWords.put("nextfile", Token.TokenType.NEXTFILE);
        knownWords.put("function", Token.TokenType.FUNCTION);
        knownWords.put("from", Token.TokenType.FROM);
        knownWords.put("to", Token.TokenType.TO);
    }

    private void populateKnownSymbols() {
        populateKnownSymbolPairs();

        knownSymbols.put("}", Token.TokenType.RIGHTBRACE);
        knownSymbols.put("{", Token.TokenType.LEFTBRACE);
        knownSymbols.put("[", Token.TokenType.LEFTBRACKET);
        knownSymbols.put("]", Token.TokenType.RIGHTBRACKET);
        knownSymbols.put("(", Token.TokenType.LEFTPAREN);
        knownSymbols.put(")", Token.TokenType.RIGHTPAREN);
        knownSymbols.put("$", Token.TokenType.DOLLAR);
        knownSymbols.put("~", Token.TokenType.MATCH);
        knownSymbols.put("=", Token.TokenType.ASSIGN);
        knownSymbols.put("<", Token.TokenType.LESS);
        knownSymbols.put(">", Token.TokenType.GREATER);
        knownSymbols.put("!", Token.TokenType.NOT);
        knownSymbols.put("+", Token.TokenType.PLUS);
        knownSymbols.put("^", Token.TokenType.POWER);
        knownSymbols.put("-", Token.TokenType.MINUS);
        knownSymbols.put("?", Token.TokenType.QUESTION);
        knownSymbols.put(":", Token.TokenType.COLON);
        knownSymbols.put("*", Token.TokenType.MULTIPLY);
        knownSymbols.put("/", Token.TokenType.DIVIDE);
        knownSymbols.put("%", Token.TokenType.MODULO);
        knownSymbols.put(";", Token.TokenType.SEPERATOR);
        knownSymbols.put("\\n", Token.TokenType.SEPERATOR);
        knownSymbols.put("|", Token.TokenType.PIPE);
        knownSymbols.put(",", Token.TokenType.COMMA);
    }
    private void populateKnownSymbolPairs() {
        knownSymbolPairs.put(">=", Token.TokenType.GREATEROREQUAL);
        knownSymbolPairs.put("++", Token.TokenType.INCREMENT);
        knownSymbolPairs.put("--", Token.TokenType.DECREMENT);
        knownSymbolPairs.put("<=", Token.TokenType.LESSOREQUAL);
        knownSymbolPairs.put("==", Token.TokenType.EQUAL);
        knownSymbolPairs.put("!=", Token.TokenType.NOTEQUAL);
        knownSymbolPairs.put("^=", Token.TokenType.ASSIGNPOWER);
        knownSymbolPairs.put("%=", Token.TokenType.ASSIGNMODULO);
        knownSymbolPairs.put("*=", Token.TokenType.ASSIGNMULTIPLY);
        knownSymbolPairs.put("/=", Token.TokenType.ASSIGNDIVIDE);
        knownSymbolPairs.put("+=", Token.TokenType.ASSIGNADD);
        knownSymbolPairs.put("-=", Token.TokenType.ASSIGNSUBTRACT);
        knownSymbolPairs.put("!~", Token.TokenType.NOMATCH);
        knownSymbolPairs.put("&&", Token.TokenType.AND);
        knownSymbolPairs.put(">>", Token.TokenType.APPEND);
        knownSymbolPairs.put("||", Token.TokenType.OR);
    }

    private Token identifyWord(String word){
        return new Token(lineCol[LINE], lineCol[COL], knownWords.getOrDefault(word, Token.TokenType.WORD), word);
    }
    
    private Token handleStringLiteral(){
        char c;
        StringBuilder literal = new StringBuilder();
        while((c = inputReader.getChar()) != '"' && c != '“') {
            if(c=='\\'){ //escape sequence
                
                lineCol[COL]++; // + 1 to count the backslash, even if we never add it to the literal
                c = inputReader.getChar();
                if(String.valueOf(c).matches("^[a-z]$")){ //check for lowercase letters, for cases like \t \n \... etc
                    lineCol[COL]++; //the \abc bit mentioned in the jUnit tests
                    literal.append("\\").append(c);
                }else{
                    lineCol[COL]++;
                    literal.append(c);
                }
            } else {
                lineCol[COL]++;
                literal.append(c);
            }
        }
        lineCol[COL]++; // account for closing "
        
        return new Token(lineCol[LINE], lineCol[COL], Token.TokenType.STRINGLITERAL, literal.toString());
    }
    
    private Token handleRegexLiteral(){
        char c;
        StringBuilder literal = new StringBuilder();
        while((c = inputReader.getChar()) != '`'){
            
            // assuming newlines in regex is garbage that we'll eat up for now.
            
//            if(c == '\n') { // Don't let our numbers get messed up
//                lineCol[LINE]++;
//                lineCol[COL] = 0;
//            } else
            if(c == '\\'){ //escape sequence
                lineCol[COL] += 2; // + 1 to count the backslash, + 1 again to count the absorbed character following 
                literal.append( (c = inputReader.getChar()) );
            } else {
                lineCol[COL]++;
                literal.append(c);
            }
        }
        lineCol[COL]++; // account for closing `
        
        return new Token(lineCol[LINE], lineCol[COL], Token.TokenType.REGEXLITERAL, literal.toString());
    }
}


