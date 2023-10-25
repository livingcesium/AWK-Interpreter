public class RegexNode extends ConstantNode<String> {
    RegexNode(String value) {
        super(value);
    }
    
    public String toString(){
        return String.format("RegexNode: %s", getValue());
    }
}
