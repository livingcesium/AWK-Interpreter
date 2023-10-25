public class StringHandler {
    //fields
    private String document;
    private int index;

    //constructors
    public StringHandler(String AWKfile){
        document = AWKfile;
        index = 0;
    }
    //methods

    public String peekString(int i){ //preserves index
        return document.substring(index, index + i);
    }
    public int untilDone(){
        return document.length() - this.index;
    }

    public String remainder(){
        return document.substring(index);
    }

    public char getChar(){
        if(!isDone())
            return document.charAt(index++); //get character then push the index
        else
            throw new StringHandlerOutOfBoundsException(String.format("attempted a move to index %d, which is outside of the document (length %d, max index %d)", index + 1, document.length(), document.length() - 1));
    }

    public void swallow(int i){ 
        if(index + i <= document.length())
            index += i; 
        else 
            throw new StringHandlerOutOfBoundsException(String.format("attempted a move to index %d, which is outside of the document (length %d, max index %d)", index + i, document.length(), document.length() - 1));
    }
    public boolean isDone(){
        return index >= document.length(); //length() used because if index pointer gets pushed to [maxIndex + 1], or [length] in other terms, you will have read the full document. To avoid making the last character not count, we point to an imaginary index when done.
    }

    
    
    //exceptions

    public static class StringHandlerOutOfBoundsException extends ArrayIndexOutOfBoundsException {
        public StringHandlerOutOfBoundsException(String msg){
            super(msg);
        }
    }

}

