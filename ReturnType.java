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
    public final String position;

    public ReturnType(Control controlType, String position){
        super(null);
        this.controlType = controlType;
        this.position = position;
    }
    public ReturnType(Control controlType){
        this(controlType, "[debug node]");
    }

    public ReturnType(String value, String position){
        super(value);
        this.controlType = Control.RETURN;
        this.position = position;
    }
    public ReturnType(String value){
        this(value, "[debug node]");
    }

    public ReturnType(String value, boolean returning, String position){
        super(value);
        if(returning)
            this.controlType = Control.RETURN;
        else
            this.controlType = Control.NORMAL;
        this.position = position;
    }
    public ReturnType(String value, boolean returning){
        this(value, returning, "[debug node]");
    }

    public InterpreterDataType expectData(String errorMessage){
        if(this.value == null)
            throw new ValueNotFoundException(errorMessage);
        return new InterpreterDataType(this.value);
    }
    
    public ReturnType rejectLoopControl(String errorMessage){
        if(this.controlType == Control.BREAK || this.controlType == Control.CONTINUE)
            throw new RuntimeException(errorMessage + " by " + position);
        return this;
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