import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Arrays;

public class JavAWK {
    public static void main(String[] args) {
        if(args.length < 1)
            throw new IllegalArgumentException("""
            Expected at least one argument. Usages:
                java JavAWK "<AWK code>" [args]
                java JavAWK -f <AWK file> [args]
                java JavAWK "<AWK code>" -e "<AWK code>" [args]
            
            """);
        LinkedList<String> code = new LinkedList<>();
        LinkedList<Path> fileArgs = new LinkedList<>();
        
        Iterator<String> argIterator = Arrays.asList(args).iterator();
        String arg;
        HashMap<String, String> awkArgs = new HashMap<>();
        while(argIterator.hasNext())
            switch (arg = argIterator.next()) {
                case "-e":
                    code.add(argIterator.next()); // Next arg is a string of AWK code
                    break;
                case "-f":
                    try {
                        code.addAll(Files.readAllLines(Path.of(argIterator.next()))); // Next arg is a file of AWK code
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Could not read AWK file.", e);
                    }
                    break;
                default:
                    // Some cases need regex
                    if(arg.matches("^F"))
                        awkArgs.put("FS", arg.substring(1).replaceAll("\"", "")); // Get rid of quotes and leading F, use that for FS
                    else if(code.isEmpty())
                    // First arg (no code added yet) must be a string of AWK code if it reaches here
                        code.add(arg); // First arg is a string of AWK code
                    else
                        fileArgs.add(Path.of(arg)); // Any other args are files to be read
                    
            }
        
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer.lex());
        ProgramNode program = parser.parse();
        
        Interpreter interpreter;
        
        if(fileArgs.isEmpty()) {
            interpreter = new Interpreter(program, awkArgs);
            interpreter.interpretProgram();
        }
        else try{
            interpreter = new Interpreter(program, fileArgs.poll(), awkArgs);
            interpreter.interpretProgram();
            while(!fileArgs.isEmpty()){
                interpreter.changeFile(fileArgs.poll());
                interpreter.interpretProgram();
            }
            
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read input file.", e);
        }
    }
    
    
}
