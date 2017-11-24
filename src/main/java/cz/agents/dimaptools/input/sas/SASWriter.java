package cz.agents.dimaptools.input.sas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class SASWriter {
	
	private final SASDomain sas;
	private Map<String,Integer> varCodes = new HashMap<String, Integer>();
	private Map<String,Integer> valCodes = new HashMap<String, Integer>();
	
	public SASWriter(SASDomain sas) {
		super();
		this.sas = sas;
	}
	
	public void write(PrintStream out){
		out.println(generate());
	}
	
	public void write(File file){
		try {
			PrintStream out = new PrintStream(file);
			out.println(generate());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public String generate(){
		String //header
		out = "begin_version\n3\nend_version\nbegin_metric\n1\nend_metric\n";
		
		//variables
		int varc = 0;
		out += sas.getVariables().size() + "\n";
		for(String var : sas.getVariables()){
			varCodes.put(var, varc++);
			out += "begin_variable\n";
			out += var + "\n";
			out += "-1\n";
			out += sas.getDomain(var).size() + "\n";
			
			int valc = 0;
			for(String val : sas.getDomain(var)){
				valCodes.put(val, valc++);
				out += "Atom " + val + "\n";
			}
			out += "end_variable\n";
		}
		
		//mutexes
		out += "0\n";
		
		//init
		out += "begin_state\n";
		for(String var : sas.getVariables()){
			out += valCodes.get(sas.init.get(var)) + "\n";
		}
		out += "end_state\n";
		
		//goal
		out += "begin_goal\n";
		out += sas.goal.size() + "\n";
		for(String var : sas.goal.keySet()){
			out += varCodes.get(var) + " " + valCodes.get(sas.goal.get(var)) + "\n";
		}
		out += "end_goal\n";
		
		// variables
		out += sas.operators.size() + "\n";
		for (SASOperator op : sas.operators) {
			out += "begin_operator\n";
			out += op.label + "\n";
			
			//prevail conditions
			int pre = 0;
			String preS = "";
			for(String var : op.pre.keySet()){
				if(op.eff.get(var)==null){
					++pre;
					preS += varCodes.get(var) + " " + valCodes.get(op.pre.get(var)) + "\n";
				}
			}
			out += pre + "\n";
			out += preS;
			
			//effects
			out += op.eff.size() + "\n";
			for(String var : op.eff.keySet()){
				int p = valCodes.get(op.pre.get(var)) == null ? -1 : valCodes.get(op.pre.get(var));
				out += "0 " + varCodes.get(var) + " " + p + " " + valCodes.get(op.eff.get(var)) + "\n";
			}
			
			//cost
			out += op.cost + "\n";
			
			out += "end_operator\n";
			
			
		}
		
		out += "0\n";
		
		return out;
	}
	
	

}
