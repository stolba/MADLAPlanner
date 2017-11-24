package cz.agents.dimaptools.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class AnalyzerDataAccumulator {


	private static AnalyzerDataAccumulator currentAccumulator = new AnalyzerDataAccumulator("", "");

	private static String sep = ",";

	private final String domain;
	private final String problem;

	public int agents;
	public int publicRatio;
	public int globalRPPublicRatioMax;
	public int globalRPPublicRatioAvg;
	public float agentMaxValue;
	public float agentAddPerGoalValue;
	public float agentAddValue;
	public float agentSimpleValue;
	
	private String outFileName = null;


	private AnalyzerDataAccumulator(String domain, String problem){
		int last = Math.max(domain.lastIndexOf('/'),domain.lastIndexOf('\\'));
		this.domain = last > 0 ? domain.substring(0, last) : domain;
		this.problem = problem;
	}



	public static void startNewAccumulator(String domain, String problem){
		currentAccumulator = new AnalyzerDataAccumulator(domain,problem);
	}
	
	public static AnalyzerDataAccumulator getAccumulator(){
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
		sb.append("publicRatio").append(sep);
		sb.append("globalRPPublicRatioAvg").append(sep);
		sb.append("globalRPPublicRatioMax").append(sep);
		sb.append("agentMaxValue").append(sep);
		sb.append("agentAddPerGoalValue").append(sep);
		sb.append("agentAddValue").append(sep);
		sb.append("agentSimpleValue");

		return sb.toString();
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append(domain).append(sep);
		sb.append(problem).append(sep);
		sb.append(agents).append(sep);
		sb.append(publicRatio).append(sep);
		sb.append(globalRPPublicRatioAvg).append(sep);
		sb.append(globalRPPublicRatioMax).append(sep);
		sb.append(agentMaxValue).append(sep);
		sb.append(agentAddPerGoalValue).append(sep);
		sb.append(agentAddValue).append(sep);
		sb.append(agentSimpleValue);

		return sb.toString();
	}
	
	public void writeOutput(boolean die){
		if(outFileName==null){
			System.out.println("WARN:not writing output!");
			return;
		}
        //write data accumulator output
		System.out.println("writing output...");
		System.out.println(AnalyzerDataAccumulator.getAccumulator().toString());
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
                writer.println(AnalyzerDataAccumulator.getLabels());
            }
            writer.write(AnalyzerDataAccumulator.getAccumulator().toString() + "\n");
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
