import java.util.Objects;

public class InterpreterDataType {
    public final String value;
    public InterpreterDataType(String value){
        this.value = value;
    }

    
    public String toString(){
        return String.format("IDT{%s}", value);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterpreterDataType that = (InterpreterDataType) o;

        return Objects.equals(value, that.value);
    }
    
}
