package cz.agents.dimaptools.input.addl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADDLParser {

    public ADDLObject parse(File file) {
        String content = readFileAsString(file);

        System.out.println(content);

        Pattern pattern = Pattern.compile("\\(:agents\\s*(.*?)\\s*\\)");
        Matcher m = pattern.matcher(content);
        m.find();
        String agentNameString = m.group(1);
        String[] agentNames = agentNameString.split("\\s+");

        return new ADDLObject(Arrays.asList(agentNames));
    }

    private String readFileAsString(File file) {
        StringBuilder fileData = new StringBuilder();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            char[] buf = new char[1024];
            int numRead = 0;
            while((numRead = reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileData.toString();
    }

}
