import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType{
    HashMap<String, InterpreterDataType> value;
    public InterpreterArrayDataType(){
        value = new HashMap<>();
    }
}
