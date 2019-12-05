package com.github.wtan.jel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.github.wtan.jel.IExpression;
import com.github.wtan.jel.Statement;
import com.github.wtan.jel.exception.ExpressionException;

public class JELConsole {
    public static void usage() {
        System.out.println("  <expression> - evaluate an expression");
        System.out.println("  show         - display variables in the Map");
        System.out.println("  remove <var> - remove a variable from the Map");
        System.out.println("  quit         - exit");
        System.out.println("  help         - display this help");
    }

    public static void main(String[] args) throws IOException {
        try {
            String prompt = "> ";
            Map<?, ?> m = new HashMap(32);
            
            Terminal terminal = TerminalBuilder.builder()
            		.name("JEL Console")
                    .dumb(true)
                    .build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            terminal.writer().println("JEL Console");
            terminal.writer().flush();

            while (true) {
                String line = null;
                try {
                    line = reader.readLine(prompt);
                } 
                catch (UserInterruptException e) {
                    // Ignore
                } 
                catch (EndOfFileException e) {
                    return;
                }
                if (line == null) {
                    continue;
                }

                line = line.trim();

                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    break;
                }
                ParsedLine pl = reader.getParser().parse(line, 0);
                if ("help".equals(pl.word())) {
                	usage();
                }
                else if ("show".equals(pl.word())) {
                	if (m.isEmpty()) {
                    	terminal.writer().println("Empty");
                	}
                	else {
                    	int maxvar = 0;
                    	int maxclass = 0;
                    	for (Map.Entry<?, ?> entry : m.entrySet()) {
                    		maxvar = Math.max(((String)entry.getKey()).length(), maxvar);
                    		maxclass = Math.max(((String)entry.getValue().getClass().getName()).length(), maxclass);
                    	}
                    	maxvar++;
                    	maxclass++;
                    	for (Map.Entry<?, ?> entry : m.entrySet()) {
                    		Object obj = entry.getValue();
                        	StringBuilder sb = new StringBuilder(40);
                        	rightPad(sb, entry.getKey().toString(), maxvar);
                        	rightPad(sb, obj.getClass().getName(), maxclass);
                        	sb.append(entry.getValue());
                        	terminal.writer().println(sb.toString());
                    	}
                	}
                	terminal.writer().flush();
                }
				else if ("remove".equals(pl.word())) {
					if (pl.words().size() == 2) {
						String varname = pl.words().get(1);
						m.remove(varname);
					}
				}
                else {
                	try {
            			IExpression cond = Statement.parse(line);
            			Object objeval = cond.eval(m);
            			if (objeval != null) {
                        	terminal.writer().println(objeval.toString());
            			}
                	}
                	catch (ExpressionException e) {
                    	terminal.writer().println(e.getPrintStackTrace());
                	}
                	terminal.writer().flush();
                }
                // Removed extra ENTER
                reader.readLine();
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void rightPad(StringBuilder sb, String str, int size) {
    	sb.append(str);
    	for (int i=size-str.length(); i>0; i--) {
    		sb.append(" ");
    	}
    }
}