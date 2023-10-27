import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Interpreter {
    private static class LineManager{
        List<String> lines;
        
        public LineManager(List<String> lines){
            this.lines = lines;
        }
        
        public boolean splitAndAssign(){
            if(lines.isEmpty())
                return false;
            
            String line = getNext();
            String separator = globalVariables.get("FS").value;
            String[] split = line.split(separator);
            
            int i = 0;
            for(String s : split){
                globalVariables.put("$"+ i, new InterpreterDataType(s));
            }
            // NF = i + 1
            globalVariables.put("NF", new InterpreterDataType(Integer.toString(i + 1)));
            // NR++
            globalVariables.put("NR", new InterpreterDataType(Integer.toString(Integer.parseInt(globalVariables.get("NR").value) + 1)));
            return true;
        }
        private String getNext(){
            globalVariables.put("FNR", new InterpreterDataType(Integer.toString(Integer.parseInt(globalVariables.get("FNR").value) + 1)));
            return lines.remove(0);
        }
    }
    
    public static HashMap<String, InterpreterDataType> globalVariables = new HashMap<>();
    HashMap<String, FunctionDefinitionNode> functions = new HashMap<>();
    LineManager lines;

    private void populateKnownFunctions(){

//                Token.TokenType.PRINT,
//                Token.TokenType.PRINTF,
//                Token.TokenType.NEXT,
//                Token.TokenType.NEXTFILE,
//                Token.TokenType.GETLINE,
//                Token.TokenType.EXIT

        // Print
        Function<HashMap<String,InterpreterDataType>,String> executePrint = (HashMap<String,InterpreterDataType> array) -> {
            LinkedList<String> out = new LinkedList<>();

            int i = 0;
            String data;
            while(array.containsKey(Integer.toString(i))) {
                data = array.get(Integer.toString(i)).value;
                if(globalVariables.containsKey(data))
                    out.add(globalVariables.get(data).value);
                else
                    out.add(data);
                i++;
            }
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < i; j++) { // Stops 1 early
               output.append(out.poll()).append(globalVariables.get("FS"));
            }
            output.append(out.poll()); // Last one doesn't get separator

            System.out.print(output);

            return output.toString();
        };

        functions.put("print", new BuiltInFunctionDefinitionNode("print", executePrint, true));







//        Function<HashMap<String,InterpreterDataType>,String> executePrint = (HashMap<String,InterpreterDataType> array) -> {
//            LinkedList<String> args = new LinkedList<>();
//
//            int i = 0;
//            while(array.containsKey(Integer.toString(i)))
//                args.add(array.get(Integer.toString(i++)).value);
//
//            StringBuilder format = new StringBuilder();
//            for (int j = 0; j < i; j++) { // ends early by one, we have (i + 1) arguments
//                format.append("%s ");
//            }
//            format.append("%s"); // final one doesn't have a space on it
//
//            System.out.printf(format.toString(), args);
//            return String.format(format.toString(), args);
//
//        };
//
//        functions.put("print", new BuiltInFunctionDefinitionNode("print", executePrint, true));



    }

    public Interpreter(ProgramNode program, Path path) throws IOException {
        globalVariables.put("FILENAME", new InterpreterDataType(path.toString()));
        globalVariables.put("FS", new InterpreterDataType(" "));
        globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
        globalVariables.put("OFS", new InterpreterDataType("\n"));

        lines = new LineManager(Files.readAllLines(path));


    }

    public Interpreter(ProgramNode program){
        this.lines = new LineManager(List.of());
    }
    
    
}
