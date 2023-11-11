// Signal class made to handle certain statements, needs to extend IDT so that it can fit nicely.
// Hopefully this makes process a little smoother
public class ReturnType extends InterpreterDataType{
    public static enum Control {
        NORMAL,
        BREAK,
        CONTINUE,
        RETURN
    }
    public final Control controlType;

    public ReturnType(Control controlType){
        super(null);
        this.controlType = controlType;
    }

    public ReturnType(String value){
        super(value);
        this.controlType = Control.RETURN;
    }

    public ReturnType(String value, boolean returning){
        super(value);
        if(returning)
            this.controlType = Control.RETURN;
        else
            this.controlType = Control.NORMAL;
    }

    public InterpreterDataType expectData(String errorMessage){
        if(this.value == null)
            throw new ValueNotFoundException(errorMessage);
        return new InterpreterDataType(this.value);
    }

    public boolean isType(Control controlType){
        return this.controlType == controlType;
    }

    public static class ValueNotFoundException extends RuntimeException{
        public ValueNotFoundException(String errorMessage){
            super(errorMessage);
        }
    }
}