package cz.agents.madla.planner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ExternalOptimalPlanner {
	
	private String cmd = "./optimal-planner-runner";
	private String cmdSAS = "./optimal-planner-runner-sas";
	
	private final static Logger LOGGER = Logger.getLogger(ExternalOptimalPlanner.class);

	public int solveOptimally(String domain, String problem) {
		solve(domain, problem);
		return read();
	}
	
	public int solveOptimallySAS(String sasfile) {
		solve(sasfile);
		return read();
	}
	
	private void solve(String domain, String problem){
		
		try {
            LOGGER.info("RUN: " + cmd + " " + domain + " " + problem);
            ProcessBuilder builder = new ProcessBuilder(cmd, domain, problem);
            builder.redirectErrorStream(true);
            builder.redirectOutput(new File("optimal_planner.out"));
            Process pr = builder.start();
            

            pr.waitFor();
            LOGGER.error(pr.getErrorStream().read());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	private void solve(String sasfile){
		
		try {
            LOGGER.info("RUN: " + cmdSAS + " " + sasfile);
            ProcessBuilder builder = new ProcessBuilder(cmd, sasfile);
            builder.redirectErrorStream(true);
            builder.redirectOutput(new File("optimal_planner.out"));
            Process pr = builder.start();
            

            pr.waitFor();
            LOGGER.error(pr.getErrorStream().read());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	private int read(){
		
		try {	
	         File inputFile = new File("optimal_plan.cost");
	         
	         String costStrLong = readFileAsString(inputFile).split("\n")[0];
	         String costStr = costStrLong.split(" ")[2];
	         int cost = Integer.parseInt(costStr);
	        	 
	         return cost;
	         
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		
		return -1;
	   
		
		
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
