package cz.agents.dimaptools.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.search.GlobalLocalDistributedBestFirstSearch;


public class DataAccumulator {

	 private static final Logger LOGGER = Logger.getLogger(DataAccumulator.class);

	private static DataAccumulator currentAccumulator = new DataAccumulator("", "", 0, "", 0);

	private static String sep = ",";

	private final String domain;
	private final String problem;
//	private final int run;
	private final String heuristic;
	private final int recursionLevel;


	public long startTimeMs;
	public long startAfterPreprocessTimeMs;
	public long finishTimeMs;
	public Map<String,Long> startCPUTimeMs = new HashMap<>();
	public Map<String,Long> CPUTimeMs = new HashMap<>();
	public int agents;
	public Map<String,Integer> initHeuristic = new HashMap<>();
	public Map<String,Integer> initHeuristicDist = new HashMap<>();
	public long expandedStates;
	public long expandedStatesGlobal;
	public long expandedStatesLocal;
	public long searchMessages;
	public long heuristicRequestMessages;
	public long heuristicReplyMessages;
	public long totalBytes;
	public Stopwatch searchStopwatch = new Stopwatch();
	public Stopwatch globalSearchStopwatch = new Stopwatch();
	public Stopwatch localSearchStopwatch = new Stopwatch();
	public Stopwatch localHeuristicStopwatch = new Stopwatch();
	public Stopwatch globalHeuristicStopwatch = new Stopwatch();
	public Stopwatch otherHeuristicStopwatch = new Stopwatch();
	public int planLength = Integer.MAX_VALUE;
	public int maxRecursionDepth = 0;
	public boolean planValid = false;
	public boolean finished = false;
	
	private String outFileName = null;


	private DataAccumulator(String domain, String problem,int run,String heuristic,int recursionLevel){
		int last = Math.max(domain.lastIndexOf('/'),domain.lastIndexOf('\\'));
		this.domain = last > 0 ? domain.substring(0, last) : domain;
		this.problem = problem;
//		this.run = run;
		this.heuristic = heuristic;
		this.recursionLevel = recursionLevel;
	}



	public static void startNewAccumulator(String domain, String problem,int run,String heuristic,int recursionLevel){
		currentAccumulator = new DataAccumulator(domain,problem,run,heuristic,recursionLevel);
	}
	
	public static DataAccumulator getAccumulator(){
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
		sb.append("heuristic").append(sep);
		sb.append("recursionLevel").append(sep);
//		sb.append("run").append(sep);
		sb.append("totalTime").append(sep);
		sb.append("CPUTimeAgents").append(sep);
		sb.append("CPUTime").append(sep);
		sb.append("initHeuristic").append(sep);
		sb.append("initHeuristicDist").append(sep);
		sb.append("searchTime").append(sep);
		sb.append("expandedStates").append(sep);
		sb.append("expandedStatesGlobal").append(sep);
		sb.append("expandedStatesLocal").append(sep);
		sb.append("searchMessages").append(sep);
		sb.append("heuristicRequestMessages").append(sep);
		sb.append("heuristicReplyMessages").append(sep);
		sb.append("totalMessages").append(sep);
		sb.append("totalBytes").append(sep);
		sb.append("globalSearchTime").append(sep);
		sb.append("localSearchTime").append(sep);
		sb.append("globalHeuristicTime").append(sep);
		sb.append("localHeuristicTime").append(sep);
		sb.append("otherHeuristicTime").append(sep);
		sb.append("searchTimes").append(sep);
		sb.append("globalSearchTimes").append(sep);
		sb.append("localSearchTimes").append(sep);
		sb.append("globalHeuristicTimes").append(sep);
		sb.append("localHeuristicTimes").append(sep);
		sb.append("otherHeuristicTimes").append(sep);
		sb.append("planLength").append(sep);
		sb.append("maxRecursionDepth").append(sep);
		sb.append("planValid").append(sep);
		sb.append("finished");

		return sb.toString();
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append(domain).append(sep);
		sb.append(problem).append(sep);
		sb.append(agents).append(sep);
		sb.append(heuristic).append(sep);
		sb.append(recursionLevel).append(sep);
//		sb.append(run).append(sep);
		sb.append(finishTimeMs-startTimeMs).append(sep);
		long tCPU = 0;
		for(long t : CPUTimeMs.values()) tCPU+= t;
		sb.append(CPUTimeMs.toString().replace(",", "")).append(sep);
		sb.append(tCPU).append(sep);
		sb.append(initHeuristic.toString().replace(",", "")).append(sep);
		sb.append(initHeuristicDist.toString().replace(",", "")).append(sep);
		sb.append(searchStopwatch.getTotalTime()).append(sep);
		sb.append(expandedStates).append(sep);
		sb.append(expandedStatesGlobal).append(sep);
		sb.append(expandedStatesLocal).append(sep);
		sb.append(searchMessages).append(sep);
		sb.append(heuristicRequestMessages).append(sep);
		sb.append(heuristicReplyMessages).append(sep);
		sb.append(searchMessages + heuristicRequestMessages + heuristicReplyMessages).append(sep);
		sb.append(totalBytes).append(sep);
		sb.append(globalSearchStopwatch.getTotalTime()).append(sep);
		sb.append(localSearchStopwatch.getTotalTime()).append(sep);
		sb.append(globalHeuristicStopwatch.getTotalTime()).append(sep);
		sb.append(localHeuristicStopwatch.getTotalTime()).append(sep);
		sb.append(otherHeuristicStopwatch.getTotalTime()).append(sep);
		sb.append(searchStopwatch.getTimeVector()).append(sep);
		sb.append(globalSearchStopwatch.getTimeVector()).append(sep);
		sb.append(localSearchStopwatch.getTimeVector()).append(sep);
		sb.append(globalHeuristicStopwatch.getTimeVector()).append(sep);
		sb.append(localHeuristicStopwatch.getTimeVector()).append(sep);
		sb.append(otherHeuristicStopwatch.getTimeVector()).append(sep);
		sb.append(planLength).append(sep);
		sb.append(maxRecursionDepth).append(sep);
		sb.append(planValid).append(sep);
		sb.append(finished);

		return sb.toString();
	}
	
	public void writeOutput(boolean die){
		if(outFileName==null){
			System.out.println("WARN:not writing output!");
			return;
		}
        //write data accumulator output
		System.out.println("writing output...");
		System.out.println("states expanded with global heuristic: " + DataAccumulator.getAccumulator().expandedStatesGlobal);
		System.out.println("states expanded with local heuristic: " + DataAccumulator.getAccumulator().expandedStatesLocal);
		System.out.println(DataAccumulator.getAccumulator().toString());
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
                writer.println(DataAccumulator.getLabels());
            }
            writer.write(DataAccumulator.getAccumulator().toString() + "\n");
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
	
	public class Stopwatch {
		private Map<String,Long> starts = Collections.synchronizedMap(new HashMap<String,Long>());
		private Map<String,Long> totaltimes = Collections.synchronizedMap(new HashMap<String,Long>());
		
		
		
		public void start(String key){
			if(starts.containsKey(key)){
				stop(key);
			}
			starts.put(key, System.currentTimeMillis());
		}
		
		public void stop(String key){
				if(starts.containsKey(key)){
					if(!totaltimes.containsKey(key)){
						totaltimes.put(key, (System.currentTimeMillis()-starts.get(key)));
					}else{
						if(totaltimes.containsKey(key) && starts.containsKey(key))totaltimes.put(key, totaltimes.get(key) + (System.currentTimeMillis()-starts.get(key)));
					}
					starts.remove(key);
				}else{
					if(LOGGER.isInfoEnabled())LOGGER.info("Stopwatch " + key + " not started!");
				}
		}
		
		public void reset(String key){
			starts.clear();
			totaltimes.clear();
		}
		
		public long getTotalTime(){
			long totalTimeMs = 0;
			
			for(long t : totaltimes.values()){
				totalTimeMs +=t;
			}
			
			return totalTimeMs;
		}
		
		public String getTimeVector(){
			String out="";
			
			for(String k : totaltimes.keySet()){
				out += k + ":" + totaltimes.get(k) + "|";
			}
			
			return out;
		}
	}

}
