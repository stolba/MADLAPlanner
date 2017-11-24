package cz.agents.dimaptools.experiment;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Trace {

    private static final boolean ENABLED = true;

    private static PrintStream stream = System.out;

    public static void setFileStream(String fileName) {
        try {
            stream = new PrintStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void it(Object... args) {
        if (ENABLED) {
            StringBuilder builder = new StringBuilder();

            builder.append("[");

            for (Object o : args) {
                if (o == null) {
                    builder.append("nil");
                } else {
                    if (o instanceof String) {
                        if (!((String)o).startsWith("'")) {
                            builder.append(":");
                        }
                    }
                    builder.append(o);
                }
                builder.append(" ");
            }

            builder.setLength(builder.length() -1);
            builder.append("]");

            stream.println(builder.toString());
        }
    }

}
