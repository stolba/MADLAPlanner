package cz.agents.dimaptools.input.sas;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Parses the SAS+ file returned by Malte Helmert's translator and preprocessor. Currently does not use mutexes and axioms.
 * @author stolba
 *
 */
public class SASParser {

	private static final Logger LOGGER = Logger.getLogger(SASParser.class);

	private SASDomain domain;

	private int metric;

	public SASParser(File sasFile) {

//    	LOGGER.setLevel(Level.DEBUG);

    	LOGGER.info("SAS: " + sasFile);

    	domain = new SASDomain();

		try {
			FileInputStream fstream = new FileInputStream(sasFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				if(LOGGER.isDebugEnabled())LOGGER.debug("skip: " + strLine);
				if(strLine.endsWith("begin_metric")){
					break;
				}
			}
			
			metric = Integer.parseInt(br.readLine()); //metric
			br.readLine(); //end metric

			if(LOGGER.isInfoEnabled())System.out.println("\n--- DOMAIN ---");
			readVariables(br);

			if(LOGGER.isInfoEnabled())System.out.println("\n--- MUTEXES ---");
			readMutexes(br);

			if(LOGGER.isInfoEnabled())System.out.println("\n--- INIT ---");
			readState(br);

			if(LOGGER.isInfoEnabled())System.out.println("\n--- GOAL ---");
			readGoal(br);

			if(LOGGER.isInfoEnabled())System.out.println("\n--- OPERATORS ---");
			readOperators(br);

			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

    }

	public SASDomain getDomain(){
		return domain;
	}

	private void readVariables(BufferedReader br) throws IOException{
    	int vars = Integer.decode(br.readLine());

    	for(int i = 0; i < vars; i++){
    		br.readLine(); //begin

    		String var = br.readLine(); //name
    		domain.addVariable(var);

    		br.readLine(); //axiom layer

    		int domainSize = Integer.parseInt(br.readLine()); //domain size

    		for(int j = 0; j<domainSize;++j){
    			String atom = br.readLine(); //value
    			String val;

    			if(atom.endsWith("<none of those>")){
    				val = "NONE";
    			}else{
    				val = atom.replaceAll("Atom ", "");
    			}

    			domain.addValue(var, val);
    		}

    		br.readLine(); //end
    	}

    	domain.finishVariables();

    }

	private void readMutexes(BufferedReader br) throws IOException{
    	int mxgs = Integer.decode(br.readLine());

    	for(int i = 0; i < mxgs; i++){
    		br.readLine(); //begin

    		int mxs = Integer.decode(br.readLine());

    		for(int j = 0; j < mxs; j++){
    			br.readLine(); //var val - skip for now
    		}

    		br.readLine(); //end
    	}
    }

	private void readState(BufferedReader br) throws IOException{
    	br.readLine(); //begin

    	Map<String, String> stateVariableMap = new HashMap<String,String>();

    	for(String var : domain.getVariables()){
    		int valIndex = Integer.parseInt(br.readLine());
    		String val = domain.getVal(var,valIndex);
    		stateVariableMap.put(var, val);
    	}

    	br.readLine(); //end

    	domain.setInit(stateVariableMap);
    }

	private void readGoal(BufferedReader br) throws IOException{
    	br.readLine(); //begin

    	Map<String, String> stateVariableMap = new HashMap<String,String>();

    	int vars = Integer.parseInt(br.readLine()); //num of variables

    	for(int i = 0; i<vars; ++i){
    		String[] ln = br.readLine().split(" "); //var val
    		int var = Integer.parseInt(ln[0]);
    		int val = Integer.parseInt(ln[1]);

    		stateVariableMap.put(
    				domain.getVar(var),
    				domain.getVal(var,val));
    	}


    	br.readLine(); //end

    	domain.setGoal(stateVariableMap);
    }

	private void readOperators(BufferedReader br) throws NumberFormatException, IOException{
    	int ops = Integer.parseInt(br.readLine()); //num of operators

    	for(int i = 0; i < ops; ++i){
    		br.readLine(); //begin
    		String label = br.readLine(); //name
    		String name = label.split(" ")[0];
    		//label = label.replaceAll(" ", "-");

    		Map<String, String> pre = new HashMap<String, String>();
    		Map<String, String> eff = new HashMap<String, String>();

    		int maxP = Integer.parseInt(br.readLine()); //prevail conditions
    		for(int p = 0; p < maxP; ++ p){
    			String[] cond = br.readLine().split(" "); //var val
    			int var = Integer.parseInt(cond[0]);
        		int val = Integer.parseInt(cond[1]);

    			pre.put(domain.getVar(var), domain.getVal(var,val));
    		}

    		int maxE = Integer.parseInt(br.readLine()); //effects
    		for(int e = 0; e < maxE; ++ e){
    			String[] effects = br.readLine().split(" "); //var val

    			//effect conditions
    			int ec = 1;
    			for(; ec< Integer.parseInt(effects[0]);ec+=2){
    				int var = Integer.parseInt(effects[ec]);
            		int val = Integer.parseInt(effects[ec+1]);

            		pre.put(domain.getVar(var), domain.getVal(var,val));
    			}

    			//affected variable
    			String var = domain.getVar(Integer.parseInt(effects[ec]));
    			//pre value
    			if(Integer.parseInt(effects[ec+1]) > -1){
    				String valPre = domain.getVal(var,Integer.parseInt(effects[ec+1]));
    				pre.put(var, valPre);
    			}
    			//eff value
    			String valEff = domain.getVal(var,Integer.parseInt(effects[ec+2]));
    			eff.put(var, valEff);

    		}

    		int cost = Integer.parseInt(br.readLine()); //cost

    		domain.addOperator(name,label,pre,eff,cost);

    		br.readLine(); //end
    	}
    	
    	
    }

	public boolean isMetric() {
		return metric == 1;
	}


}
