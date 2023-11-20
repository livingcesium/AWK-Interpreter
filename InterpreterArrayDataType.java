import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType{
    private final HashMap<String, InterpreterDataType> arrayValue;
    public InterpreterArrayDataType(HashMap<String, InterpreterDataType> value){
        super(stringify(value)); // placeholder, pretty much just for debugging potential. If you ever see this in the output, something went wrong.
        arrayValue = value;
    }
    
    public HashMap<String, InterpreterDataType> getArrayValue(){
        return arrayValue;
    }
    
    private static String stringify(HashMap<String, InterpreterDataType> array){
        if(array.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String key : array.keySet()){
            sb.append(key);
            sb.append(": ");
            sb.append(array.get(key).value);
            sb.append(", ");
        }
        sb.delete(sb.length()-2, sb.length()); // get rid of ", " at the end
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        
        InterpreterArrayDataType that = (InterpreterArrayDataType) o;
        
        return arrayValue.equals(that.arrayValue);
    }
    
}
