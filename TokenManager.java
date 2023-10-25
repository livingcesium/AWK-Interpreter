import java.util.LinkedList;
import java.util.Optional;

public class TokenManager {
    private LinkedList<Token> tokens;
    
    public TokenManager(LinkedList<Token> list){
        tokens = list;
    }
    
    private Token lastPopped; // holds the last token we removed, for contextual error messages

    public Optional<Token> peek(int j){
        try{
            return Optional.ofNullable(tokens.get(j - 1));
        } catch (IndexOutOfBoundsException e){
            return Optional.empty();
        }
    }
    public boolean isTypeAhead(Token.TokenType type, int i){
        Optional<Token> token;
        if((token = peek(i)).isPresent()){
            return token.get().isType(type);
        }
        return false;
    }
    
    public boolean moreTokens(){
        return !tokens.isEmpty();
    }
    public Optional<Token> matchAndRemove(Token.TokenType type){
        if(!moreTokens())
            return Optional.empty();
        Token token = tokens.getFirst();
        if(token.isType(type)){
            tokens.removeFirst();
            updatePosition(token); // Keeps a copy of this token in case we need to know where things went wrong
            return Optional.of(token);
        } else {
            return Optional.empty();
        }
        
    }
    
    private void updatePosition(Token token){
        lastPopped = new Token(token); // Copy constructor
    }
    public String reportPosition(){ return lastPopped.toString();}
    
}
