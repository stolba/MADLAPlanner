package cz.agents.dimaptools.lp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class LPSolver {
	
	public enum Solver {LPSOLVE, CPLEX};
	
	public abstract LPSolution solveLP(String LP);
	
	public abstract Solver isSolver();
	
	protected void writeFile(String content, String outFileName){
		System.out.println("writing "+outFileName+"...");
	    File outFile = new File(outFileName);
	    boolean newFile = false;
	    if (!outFile.exists()) {
	        try {
	            outFile.createNewFile();
	            newFile = true;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }else{
	    	try {
	    		outFile.delete();
	            outFile.createNewFile();
	            newFile = true;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    PrintWriter writer = null;

	    try {
	        writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile,true)));
	        if(newFile){
	            writer.print(content);
	        }
	        writer.flush();
	        writer.close();
	    } catch (IOException ex){
	      // report
	    } finally {
	       try {writer.close();} catch (Exception ex) {}
	    }
	}

	protected String readFileAsString(File file) {
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



