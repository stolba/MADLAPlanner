package cz.agents.dimaptools.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class CPDataAccumulator {


	private static CPDataAccumulator currentAccumulator = new CPDataAccumulator("", "");

	private static String sep = ",";

	public final String domain;
	public final String problem;

	public int agents;
	public boolean global;
	public String solver;
	public float globalEstimate;
	public float globalOptimum;
	public float maxOfLocalOptimums;
	public float sumOfLocalOptimums;
	public boolean isCP;
	public String note="";
	
	private String outFileName = null;


	private CPDataAccumulator(String domain, String problem){
		int last = Math.max(domain.lastIndexOf('/'),domain.lastIndexOf('\\'));
		String onlyDomain = last > 0 ? domain.substring(0, last) : domain;
		last = Math.max(onlyDomain.lastIndexOf('/'),onlyDomain.lastIndexOf('\\'));
		this.domain = last > 0 ? onlyDomain.substring( last+1,onlyDomain.length()) : onlyDomain;
		last = Math.max(problem.lastIndexOf('/'),problem.lastIndexOf('\\'));
		this.problem = last > 0 ? problem.substring( last+1,problem.length()) : problem;
	}



	public static void startNewAccumulator(String domain, String problem){
		currentAccumulator = new CPDataAccumulator(domain,problem);
	}
	
	public static CPDataAccumulator getAccumulator(){
		return currentAccumulator;
	}
	
	public void setOutputFile(String output){
		outFileName = output;
	}

	public static String getLabels(){
		StringBuilder sb = new StringBuilder();

		sb.append("domain").append(sep);
		sb.append("problem").append(sep);
		sb.append("agents").append(sep);
		sb.append("global").append(sep);
		sb.append("solver").append(sep);
		sb.append("globalEstimate").append(sep);
		sb.append("globalOptimum").append(sep);
		sb.append("maxOfLocalOptimums").append(sep);
		sb.append("sumOfLocalOptimums").append(sep);
		sb.append("isCP").append(sep);
		sb.append("note");

		return sb.toString();
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append(domain).append(sep);
		sb.append(problem).append(sep);
		sb.append(agents).append(sep);
		sb.append(global).append(sep);
		sb.append(solver).append(sep);
		sb.append(globalEstimate).append(sep);
		sb.append(globalOptimum).append(sep);
		sb.append(maxOfLocalOptimums).append(sep);
		sb.append(sumOfLocalOptimums).append(sep);
		sb.append(isCP).append(sep);
		sb.append(note);

		return sb.toString();
	}
	
	public void writeOutput(boolean die){
		if(outFileName==null){
			System.out.println("WARN:not writing output!");
			return;
		}
        //write data accumulator output
		System.out.println("writing output...");
		System.out.println(CPDataAccumulator.getLabels());
		System.out.println(CPDataAccumulator.getAccumulator().toString());
        File outFile = new File(outFileName);
        boolean newFile = false;
        if (!outFile.exists()) {
            try {
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
                writer.println(CPDataAccumulator.getLabels());
            }
            writer.write(CPDataAccumulator.getAccumulator().toString() + "\n");
            writer.flush();
            writer.close();
        } catch (IOException ex){
          // report
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
        
        if(die){
        	System.out.println("DIE!");
        	System.exit(0);
        }
    }

}
