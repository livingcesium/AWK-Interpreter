import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new UnsupportedOperationException("expected 1 argument: filename");
        }
        File file = new File(args[0]);
        String input = new String(Files.readAllBytes(file.toPath()));
        
        LinkedList<Token> lexedInput = ( new Lexer(input) ).lex();
        Files.writeString(Path.of("lexer.log"), lexedInput.toString().strip().replace(",", "\n"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        ProgramNode program = (new Parser(lexedInput)).parse();
        Files.writeString(Path.of("parser.log"), program.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.printf("Completed lexing and parsing \"%s\", see logs.", args[0]);


    }
}
