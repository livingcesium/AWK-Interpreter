public class RegexNode extends ConstantNode<String> {
    RegexNode(String value) {
        super(value);
    }
    
    public ConstantNode<String> getGeneralized(){
        return new ConstantNode<String>(this.getValue());
    }
    
    public String toString(){
        return String.format("RegexNode: %s", getValue());
    }
}
