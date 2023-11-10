public class ConstantNode<T> extends Node{
    private T value;
    
    ConstantNode(T value){
        this.value = value;
    }
    
    public String getValue(){
        if(value instanceof Boolean bool)
        // Auto translate booleans to AWKs definition
            return bool ? "1" : "0";
        return value.toString();
    }
    
    
    
    public String toString(){
        return String.format("ConstantNode: %s", value);
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstantNode<?> that = (ConstantNode<?>) o;

        return value.equals(that.value);
    }
    
}
