// Signal class made to handle certain statements, needs to extend IDT so that it can fit nicely.
// Hopefully this makes process a little smoother
public class ControlSignal extends InterpreterDataType{
    public static enum SignalType{
        BREAK,
        CONTINUE,
        SUCCESS
    }
    public final SignalType type;

    public ControlSignal(SignalType type){
        super(null); // Hopefully this makes any erroneous uses of this class obvious
        this.type = type;
    }
}