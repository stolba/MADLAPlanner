package cz.agents.dimaptools.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.input.sas.SASDomain;
import cz.agents.dimaptools.input.sas.SASWriter;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

/**
 * Reference implementation of A* Search
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class ExternalSingleAgentSearch implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(ExternalSingleAgentSearch.class);

    private final Problem problem;
    private final Set<Action> actions;
    



    public ExternalSingleAgentSearch(Problem problem) {
        this(problem,problem.getAllActions());
    }
    
    public ExternalSingleAgentSearch(Problem problem,Set<Action> useActions) {
        this.problem = problem;
        actions = useActions;

       
       
    }



    @Override
    public void plan(ConfigurationInterface config, SearchCallback planFoundCallback) {
    	
    	SASDomain sasDom = new SASDomain(problem.getDomain(), problem, problem.agent, actions,config.getInt("multiplyFloatCosts", 1));
 		
 		SASWriter writer = new SASWriter(sasDom);	
 		String sas = writer.generate();
// 		LOGGER.info(sas);
 		
 		PrintWriter out;
		try {
			out = new PrintWriter(config.getString("outputString","output.sas"));
			out.println(sas);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOGGER.error("Could not write output.sas");
			planFoundCallback.planNotFound();
			return;
		}
		
		String planner = config.getString("planner", "./symba-runner");
		
		try {
            LOGGER.info(problem.agent + " RUN: " + planner);
            Process pr = Runtime.getRuntime().exec(planner);

            pr.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		// The name of the file to open.
        String planFile = config.getString("planFile", "./out.plan");

        // This will reference one line at a time
        String line = null;
        List<String> plan = new LinkedList<>();

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(planFile);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =  new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	LOGGER.info(line);
            	if(line.equals("")){
            		LOGGER.info("EMPTY PLAN");
            	}else{
            		plan.add(convertToPlanAction(line));
            	}
            }   

            // Always close files.
            bufferedReader.close();   
            
        }
        catch(FileNotFoundException ex) {
        	LOGGER.error("Unable to open file '" + planFile + "'"); 
        	planFoundCallback.planNotFound();
			return;
        }
        catch(IOException ex) {
        	LOGGER.error("Error reading file '"  + planFile + "'");                  
        	planFoundCallback.planNotFound();
			return;
        }

        planFoundCallback.planFound(plan);
        
    }
    
    private String convertToPlanAction(String act){
    	String label = act.substring(1, act.length()-1);
    	
    	if(label.contains("*")){
    		label = label.replace("*", "");
    		String agent = label.substring(label.indexOf("(")+1, label.indexOf(")"));
    		label = label.substring(0,label.indexOf("("));
    		String agentAndLabel = agent + ": " + label;
    		int hash = agentAndLabel.hashCode();
    		label = "\n"+agent + " " + label + " " + hash;
    		System.out.println(agentAndLabel + ": " + hash);
    		System.out.println("cost in projection("+hash+"): " + problem.getAction(hash).getCost());
    	}else{
    		String agentAndLabel = problem.agent + ": " + label;
    		int hash = agentAndLabel.hashCode();
    		label = "\n"+problem.agent + " " + label + " " + hash;
    		System.out.println(agentAndLabel + ": " + hash);
    		System.out.println("cost in projection("+hash+"): " + problem.getAction(hash).getCost());
    	}
    	
    	
    	
    	return label;
    }


   

}
