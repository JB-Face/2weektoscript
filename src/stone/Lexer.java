package stone;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Lexer{
    public static String regexPat
            = "\\s*((//.*)|([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")"
            + "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct})?";
    private Pattern pattern = Pattern.compile(regexPat);
    private ArrayList<Token> queue = new ArrayList<Token>();
    private boolean hasMore;
    private LineNumberReader reader;

    public Lexer(Reader r){
        hasMore = true;
        reader = new LineNumberReader(r);
    }
//read和peek作用是在其中读取需要的单词
    //read获取以后直接在queue中移除
    public Token read() throws ParseException{
        if (fillQueue(0))
            return queue.remove(0);
        else
            return  Token.EOF;
    }
    //peed会在获取以后保留单词以方便重查
    public Token peek(int i )throws ParseException{
        if(fillQueue(i))
            return queue.get(i);
        else
            return Token.EOF;
    }
//判断在这一行中是否还有内容，有则使用分析器并返回true 无则返回false
    private boolean fillQueue(int i) throws ParseException {
        while (i >= queue.size())
            if (hasMore)
                readLine();
            else
                return false;
        return true;
    }
// 词分析器
    protected void readLine() throws  ParseException {
        String line;
        try{
            line = reader.readLine();
        }catch(IOException e){
            throw new ParseException(e);//抛出异常
        }
        if (line == null) {
            hasMore = false;
            return;
        }

        int lineNo = reader.getLineNumber();
        Matcher matcher = pattern.matcher(line);
        matcher.useTransparentBounds(true).useTransparentBounds(false);
        int pos = 0;
        int endPos = line.length();
        while (pos<endPos) {
            matcher.region(pos, endPos);
            if (matcher.lookingAt()) {
                addToken(lineNo,matcher);
                pos = matcher.end();
            }
            else
                throw new ParseException("bad token at line "+ lineNo);

        }
        queue.add(new IdToken(lineNo,Token.EOL));


    }

    //token分类器

    protected void addToken(int lineNo, Matcher matcher) {
        String m = matcher.group(1);
        if (m != null) // 判断是否为空
            if (matcher.group(2) == null) { //判断是否为
                Token token;
                if (matcher.group(3) != null)
                    token = new NumToken(lineNo, Integer.parseInt(m));
                else if (matcher.group(4) != null)
                    token = new StrToken(lineNo, toStringLiteral(m));
                else
                    token = new IdToken(lineNo, m);
                queue.add(token);
            }
    }

    //字符串转换器
    protected String toStringLiteral(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length() - 1;
        for (int i = 1; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < len) {
                int c2 = s.charAt(i + 1);
                if (c2 == '"' || c2 == '\\')
                    c = s.charAt(++i);
                else if (c2 == 'n') {
                    ++i;
                    c = '\n';
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    //三种不同的token

    protected static class NumToken extends Token {
        private int value;

        protected NumToken(int line, int v) {
            super(line);
            value = v;
        }
        public boolean isNumber() { return true; }
        public String getText() { return Integer.toString(value); }
        public int getNumber() { return value; }
    }

    protected static class IdToken extends Token {
        private String text;
        protected IdToken(int line, String id) {
            super(line);
            text = id;
        }
        public boolean isIdentifier() { return true; }
        public String getText() { return text; }
    }

    protected static class StrToken extends Token {
        private String literal;
        StrToken(int line, String str) {
            super(line);
            literal = str;
        }
        public boolean isString() { return true; }
        public String getText() { return literal; }
    }


    public static void main(String[] args) {
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
    }
}
