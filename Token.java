public class Token{

    public static enum TokenType{
        WORD, NUMBER, SEPERATOR,
        WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, NEXTFILE, FUNCTION,
        STRINGLITERAL, REGEXLITERAL, 
        GREATEROREQUAL, INCREMENT, DECREMENT, LESSOREQUAL, EQUAL, NOTEQUAL, ASSIGNPOWER, ASSIGNMODULO, ASSIGNMULTIPLY, ASSIGNDIVIDE, ASSIGNADD, ASSIGNSUBTRACT, NOMATCH, AND, APPEND, OR,
        LEFTBRACE, RIGHTBRACE, LEFTBRACKET, RIGHTBRACKET,LEFTPAREN, RIGHTPAREN, DOLLAR, MATCH, ASSIGN, GREATER, LESS, NOT, PLUS, POWER, MINUS, QUESTION, COLON, MULTIPLY, DIVIDE, MODULO, PIPE, COMMA,
        FROM, TO
        
    }
    private TokenType type;
    String value;
    int lineNum; int charPos;
    
    public Token(int lineNum, int charPos, TokenType type, String value){
        this.lineNum = lineNum;
        this.charPos = charPos;
        this.type = type;
        this.value = value;
    }

    public Token(int lineNum, int charPos, TokenType type){
        this.lineNum = lineNum;
        this.charPos = charPos;
        this.type = type;
    }
    
    public Token(Token copyMe){
        this.lineNum = copyMe.lineNum;
        this.charPos = copyMe.charPos;
        this.type = copyMe.type;
    }

    public String getValue() {
        return value;
    }
    
    public String toString(){
        if(value != null)
            return String.format("%s (%s) at [%d, %d]", type, value, lineNum, charPos);
        else
            return String.format("%s at [%d, %d]", type, lineNum, charPos);
    }
    
    
    
    
    public boolean isType(TokenType target){
        return type == target;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }else if (!(obj instanceof Token)) {
            return false;
        }
        
        Token otherToken = (Token) obj;
        
        boolean typeEquals = this.type == otherToken.type;
        boolean lineNumEquals = this.lineNum == otherToken.lineNum;
        boolean charPosEquals = this.charPos == otherToken.charPos;
        
        boolean valueEquals = (this.value == null && otherToken.value == null)
                || (this.value != null && this.value.equals(otherToken.value));

        // Return true if all fields are equal
        return typeEquals && lineNumEquals && charPosEquals && valueEquals;
    }

}
