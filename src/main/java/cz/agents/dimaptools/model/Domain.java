package cz.agents.dimaptools.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import gnu.trove.TIntObjectHashMap;

/**
 * Domain interprets the array of values in state in respect to the particular agent. It determines which variables
 * are in the agent's domain and which values are considered as defined. It also provides functionality to translate
 * ("humanize") integer values into respective strings.
 * The public block of variables is <0, publicVarMax), agent-specific variables are in <agentVarMin, agentVarMax).
 * The public values are from <0, publicValMax), agent-specific values <agentValMin, agentValMax).
 * Currently it is not specified which variables can have which values (TODO?)
 * @author stolba
 *
 */
public class Domain {

	public static int UNDEFINED = -1;

	/**
	 * Agent whose domain is it
	 */
	public final String agent;

	private int domainSize;	//total number of variables (global)

	//indexes to the values array
	public final int publicVarMax;	//public variables are in the interval <0, publicVarMax)
	public final int agentVarMin;	//agent variables are in the interval <agentVarMin, agentVarMax)
	public final int agentVarMax;

	//possible values in the values array
	public final int publicValMax;	//public values are from the interval <0, publicValMax)
	public final int agentValMin;	//agent values are from the interval <agentValMin, agentValMax)
	public final int agentValMax;

	//dictionary
	public static final TIntObjectHashMap varNames = new TIntObjectHashMap();
	public static final TIntObjectHashMap valNames = new TIntObjectHashMap();
	
	private Map<Integer, Set<Integer>> variableDomains;


	public Domain(String agent,
								int publicVarMax, int publicValMax,
								int agentVarMin, int agentVarMax,
								int agentValMin, int agentValMax) {
		super();
		this.agent = agent;
		this.publicVarMax = publicVarMax;
		this.agentVarMin = agentVarMin;
		this.agentVarMax = agentVarMax;
		this.publicValMax = publicValMax;
		this.agentValMin = agentValMin;
		this.agentValMax = agentValMax;
	}

	/**
	 * Returns array of proper size filled with UNDEFINED values.
	 * @return
	 */
	public int[] getEmptyDomain(){
		int[] values = new int[domainSize];
		Arrays.fill(values, Domain.UNDEFINED);
		return values;
	}

	public void setSize(int size){
		domainSize = size;
	}

	/**
	 * Number of all variables (across all agents)
	 * @return
	 */
	public int sizeGlobal(){
		return domainSize;
	}

	/**
	 * Number of variables particular for the agent
	 * @return
	 */
	public int sizeAgent(){
		return publicVarMax + agentVarMax - agentVarMin;
	}

	/**
	 * Determines if given value is considered to be defined for the domain owner.
	 * @param val
	 * @return
	 */
	public boolean isDefined(int val){
		return val != UNDEFINED && ((val >= 0 && val < publicValMax) || (val >= agentValMin && val < agentValMax));
	}

	public boolean isPublicVar(int var) {
		return (var >= 0 && var < publicVarMax);
	}

	public boolean isPublicVal(int val) {
		return (val >= 0 && val < publicValMax);
	}

	/**
	 * Determines if given variable is in the agent's domain.
	 * @param var
	 * @return
	 */
	public boolean inDomainVar(int var){
		return (var >= 0 && var < publicVarMax) || (var >= agentVarMin && var < agentVarMax);
	}

	public boolean inDomainVal(int val){
//		TODO: not sufficient?
		return (val >= 0 && val < publicValMax) || (val >= agentValMin && val < agentValMax);
	}

	/**
	 * Returns String representation of given value (according to the FDR representation)
	 * @param val
	 * @return
	 */
	public String humanizeVal(int val){
		return (inDomainVal(val) && valNames.contains(val)) ? (String)valNames.get(val) : "?";
	}

	/**
	 * Returns String representation of given variable (according to the FDR representation)
	 * @param var
	 * @return
	 */
	public String humanizeVar(int var){
		return (inDomainVar(var) && varNames.contains(var)) ? (String)varNames.get(var) : "?";
	}

	/**
	 * Translates the whole state representation according to the owner agent
	 * @param values State representation array
	 * @return
	 */
	public String humanize(int[] values){
		String out = "{";

		for(int var = 0; var < values.length; ++var){
			if(isDefined(values[var])){
				out += varNames.get(var) + "=" + valNames.get(values[var]) + ",";
			}
		}

		return out + "}";
	}

	@Override
	public String toString() {
		return "Domain("+domainSize+") [agent=" + agent + ", vars=<" + agentVarMin
				+ ", " + agentVarMax + "), vals=<"
				+ agentValMin + ", " + agentValMax
				+")]";
	}

	public String getNames(){
		String out = "\npublic:";

		out += "\n   vars: ";
		for(int i = 0; i < publicVarMax ; ++i){
			out += i + "("+humanizeVar(i)+"), ";
		}
		out += "\n   vals: ";
		for(int i = 0; i < publicValMax ; ++i){
			out += i + "("+humanizeVal(i)+"), ";
		}

		out += "\n"+agent+":";

		out += "\n   vars: ";
		for(int i = agentVarMin; i < agentVarMax ; ++i){
			out += i + "("+humanizeVar(i)+"), ";
		}
		out += "\n   vals: ";
		for(int i = agentValMin; i < agentValMax ; ++i){
			out += i + "("+humanizeVal(i)+"), ";
		}

		return out;
	}

	public Map<Integer, Set<Integer>> getVariableDomains() {
		return variableDomains;
	}

	public void setVariableDomains(Map<Integer, Set<Integer>> map) {
		variableDomains = map;
	}









}


