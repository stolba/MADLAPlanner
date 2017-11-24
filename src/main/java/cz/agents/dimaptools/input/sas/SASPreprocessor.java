package cz.agents.dimaptools.input.sas;

import java.util.*;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.model.*;

/**
 * Translates textual representation into concise integer representation and performs factorization of the operators and
 * the domains.
 *
 * WARNING: variable AND value names must be unique across all agents! TODO: fix that
 *
 * configuration:
 * treatGoalAsPublic(boolean) - all goal achieving actions are treated as public, default: true
 *
 * @author stolba
 *
 */
public class SASPreprocessor {

    private static final Logger LOGGER = Logger.getLogger(SASPreprocessor.class);

    private static final String PUBLIC = "-public";

    //global textual SAS domain
    private final SASDomain sas;

    //working structures
    private Map<String,Map<String,Set<String>>> agentVarVals = new HashMap<String,Map<String,Set<String>>>();
    private Map<String,Set<SASOperator>> agentOperators = new HashMap<String,Set<SASOperator>>();
    private Map<String,Domain> agentDomains = new HashMap<String,Domain>();

    //dictionaries
    private Map<String,Integer> varCodes = new HashMap<String, Integer>();
    private Map<String,Integer> valCodes = new HashMap<String, Integer>();

    //counters
    private int varCount = 0;
    private int valCount = 0;
    private Map<String,Map<Integer,Set<Integer>>> agentVariableDomains = new HashMap<String,Map<Integer, Set<Integer>>>(); // [agent,variable,set of values]

    private int publicVarMax;	//public variables are in the interval <0, publicVarMax)
    private int publicValMax;	//public values are from the interval <0, publicValMax)

    private int varMax;
    private int valMax;

    //should all goal-achieving operators be treated as public?
    private final boolean treatGoalAsPublic;
    private final boolean unitCost;

    /**
     * This constructor factorizes operators, detects public operators, variables and values and extracts
     * the domain representations
     *
     * configuration:
     * treatGoalAsPublic(boolean) - all goal achieving actions are treated as public, default: true
     *
     * @param sas
     * @param addl
     */
    public SASPreprocessor(SASDomain sas, ADDLObject addl){
        this(sas,addl,new MapConfiguration());
    }

    /**
     * This constructor factorizes operators, detects public operators, variables and values and extracts
     * the domain representations
     *
     * configuration:
     * treatGoalAsPublic(boolean) - all goal achieving actions are treated as public, default: true
     *
     * @param sas
     * @param addl
     * @param config
     */
    public SASPreprocessor(SASDomain sas, ADDLObject addl, ConfigurationInterface config){
        this.sas = sas;
        treatGoalAsPublic = config.getBoolean("treatGoalAsPublic", true);
        unitCost = config.getBoolean("unitCost", true);

//		LOGGER.setLevel(Level.INFO);

        LinkedList<String> agents = new LinkedList<String>(addl.getAgentList());

        //factorize operators based on agent names
        for(String agent : agents){
            agentVarVals.put(agent, new HashMap<String,Set<String>>());
            agentOperators.put(agent, new HashSet<SASOperator>());
        }

        for (SASOperator action : sas.operators) {
            int minIndex = Integer.MAX_VALUE;
            String minAgent = null;

            for(String agent : agents){
                int index = action.label.indexOf(" " + agent);
                if(index > 0 && index < minIndex){
                    minIndex = index;
                    minAgent = agent;
                }
            }

            if(minAgent != null){
                agentOperators.get(minAgent).add(action);
            }else{
                LOGGER.warn("ACTION " + action.label + " not assigned to any agent!");
            }
        }

        agentVarVals.put(PUBLIC, new HashMap<String,Set<String>>());

        //detect public/agent operators, variables and values
        if(agents.size() > 1){
            //compare each two agents once
            for(int i = 0; i < agents.size(); ++i){
                for(int j = i+1; j < agents.size(); ++j){
                    String a1 = agents.get(i);
                    String a2 = agents.get(j);

                    if(LOGGER.isDebugEnabled())LOGGER.debug(a1 + " - " + a2);

    //				TODO: use var-val strings or some SASFact class -> may be more efficient

                    //compare each operator with each other
                    //TODO: should not add again var-vals from already compared operators!
                    for(SASOperator op1 : agentOperators.get(a1)){
                        for(SASOperator op2 : agentOperators.get(a2)){

                            if(LOGGER.isDebugEnabled()){
                                LOGGER.debug("op1" + op1);
                                LOGGER.debug("op2" + op2);
                            }


                            Set<SASFact> facts = new HashSet<SASFact>();
                            facts.addAll(op1.getFacts());
                            facts.addAll(op2.getFacts());

                            for(SASFact f : facts){
                                //if goal should be treated as public
                                if(treatGoalAsPublic){
                                    if(sas.goal.containsKey(f.var) && sas.goal.get(f.var).equals(f.val)){

                                        addVarValToAgent(PUBLIC, f.var, f.val);

                                        if(LOGGER.isDebugEnabled())LOGGER.info("public(goal): " + f.var + "-" + f.val);

                                        if(op1.containsFact(f)){
                                            op1.isPublic = true;
                                        }

                                        if(op2.containsFact(f)){
                                            op2.isPublic = true;
                                        }
                                    }
                                }

                                //if the operators interact on this var-val
                                if(op1.containsFact(f) && op2.containsFact(f)){
                                    addVarValToAgent(PUBLIC, f.var, f.val);

                                    op1.isPublic = true;
                                    op2.isPublic = true;

                                    if(LOGGER.isDebugEnabled()){
                                        LOGGER.info("public: " + f.var + "-" + f.val);
                                        LOGGER.info("	on operators: " + op1.label + " and " + op2.label);
                                    }
                                }else{
                                    if(op1.containsFact(f)){
                                        addVarValToAgent(a1, f.var, f.val);

                                        if(LOGGER.isDebugEnabled())LOGGER.info(a1 + ": " + f.var + "-" + f.val);
                                    }else if(op2.containsFact(f)){
                                        addVarValToAgent(a2, f.var, f.val);

                                        if(LOGGER.isDebugEnabled())LOGGER.info(a2 + ": " + f.var + "-" + f.val);
                                    }
                                }
                            }




                        }
                    }


                }
            }
        }else{
            String a = agents.get(0);
            for(SASOperator op1 : agentOperators.get(a)){
                for(SASFact f : op1.getFacts()){
                    addVarValToAgent(a, f.var, f.val);
                }
            }
        }

        // add values which are in init and not in operators (as public)
        for (String var : sas.init.keySet()) {
            boolean missingVal = true;
            String owner = null;
            String val = sas.init.get(var);

            for(String agent : agentVarVals.keySet()){
                Map<String,Set<String>> varValMap = agentVarVals.get(agent);
                if(varValMap.containsKey(var)){
                    if(varValMap.get(var).contains(val)){
                        missingVal = false;
                        break;
                    }else{
                        if(owner==null){
                            owner = agent;
                            missingVal = false;
                        }else{
                            missingVal = false;
                            break;
                        }
                    }
                }
            }

            if (missingVal) {
                addVarValToAgent(PUBLIC, var, val);
            }else{
                if(owner!=null){
                    addVarValToAgent(owner, var, val);
                }
            }
        }


        //extract the  domains

        extractPublicDomain();

        varMax = publicVarMax;
        valMax = publicValMax;

        for(String agent : agents){
            agentDomains.put(agent, extractDomain(agent, varMax, valMax));
        }

        //set global size
        for(String agent : agents){
            agentDomains.get(agent).setSize(varCount);
            if(LOGGER.isInfoEnabled()){
                LOGGER.info(agentDomains.get(agent));
                LOGGER.info(agentDomains.get(agent).getNames());
            }

        }




    }

    /**
     * Add var-val pair to the agent's domain
     * @param agent
     * @param var
     * @param val
     */
    private void addVarValToAgent(String agent, String var, String val){
        if(!agentVarVals.get(agent).containsKey(var)){
            agentVarVals.get(agent).put(var, new HashSet<String>());
        }
        agentVarVals.get(agent).get(var).add(val);
    }

    /**
     * Extract the integer representations of the public variables/values
     */
    private void extractPublicDomain(){
        Map<String,Set<String>> varValMap = agentVarVals.get(PUBLIC);
        
        for(String agent : agentVarVals.keySet()){
        	agentVariableDomains.put(agent,new HashMap<Integer,Set<Integer>>());
        }

        for(String var : varValMap.keySet()){
            //var
            Domain.varNames.put(varCount, var);
            varCodes.put(var, varCount);
            for(String agent : agentVarVals.keySet()){
            	agentVariableDomains.get(agent).put(varCount,new HashSet<Integer>());
            }

            //vals
            for(String val : varValMap.get(var)){
                Domain.valNames.put(valCount, val);
                valCodes.put(val, valCount);
                
                for(String agent : agentVarVals.keySet()){
                	agentVariableDomains.get(agent).get(varCodes.get(var)).add(valCodes.get(val));
                }

                valCount++;
                publicValMax++;
                
                
            }
            
            varCount++;
            publicVarMax++;
            
            
            

        }
        
      
      
    }

    /**
     * Extract the integer representation of the agent's domain
     * @param agent
     * @param agentVarMin
     * @param agentValMin
     * @return
     */
    private Domain extractDomain(String agent, int agentVarMin, int agentValMin){
        int agentVarMax = agentVarMin;
        int agentValMax = agentValMin;

        Map<String,Set<String>> varValMap = agentVarVals.get(agent);

        for (String var : varValMap.keySet()) {
        	
        	
        	
        	if (!varCodes.containsKey(var)) {
                // var
                Domain.varNames.put(varCount, var);
                varCodes.put(var, varCount);
                
                if(!agentVariableDomains.get(agent).containsKey(varCodes.get(var))){
                	agentVariableDomains.get(agent).put(varCodes.get(var),new HashSet<Integer>());
                }

                varCount++;
                agentVarMax++;
            }

            // vals
            for (String val : varValMap.get(var)) {
                if (!valCodes.containsKey(val)) {
                    Domain.valNames.put(valCount, val);
                    valCodes.put(val, valCount);
                    
                    agentVariableDomains.get(agent).get(varCodes.get(var)).add(valCodes.get(val));

                    valCount++;
                    agentValMax++;
                }
                
            }
            
            
            
            

        }

        varMax = agentVarMax;
        valMax = agentValMax;
        
        

        Domain d = new Domain(agent,publicVarMax,publicValMax,agentVarMin,agentVarMax,agentValMin,agentValMax);
        d.setVariableDomains(agentVariableDomains.get(agent));
        return d;
    }

    /**
     * Get the integer representation domain for specified agent
     * @param agent
     * @return
     */
    public Domain getDomainForAgent(String agent){
        return agentDomains.get(agent);
    }



    /**
     * Get the init, goal and actions in integer representation for the specified agent.
     * @param agent
     * @return
     */
    public Problem getProblemForAgent(String agent){
        Domain d = agentDomains.get(agent);

        //translate initial state
        int[] init = new int[d.sizeGlobal()];
        for(String var : sas.init.keySet()){
            if(LOGGER.isDebugEnabled())LOGGER.info(var + ":" + varCodes.get(var) + ", " + sas.init.get(var) + ":" + valCodes.get(sas.init.get(var)));
            init[varCodes.get(var)] = valCodes.get(sas.init.get(var));
        }
        State initState = new State(d,init);

        //translate goal super-state
        SuperState goalSuperState = new SuperState(d);
        for(String var : sas.goal.keySet()){
            if (treatGoalAsPublic || d.inDomainVar(varCodes.get(var))) {
                goalSuperState.forceSetValue(varCodes.get(var), valCodes.get(sas.goal.get(var)));
            }
        }

        //translate actions
        Set<Action> actions = new LinkedHashSet<Action>();
        Set<Action> publicActions = new LinkedHashSet<Action>();
        for(SASOperator op : agentOperators.get(agent)){
//            LOGGER.info(op.toString());
            SuperState pre = new SuperState(d);
            for(String var : op.pre.keySet()){
                pre.setValue(varCodes.get(var), valCodes.get(op.pre.get(var)));
            }

            SuperState eff = new SuperState(d);
            for(String var : op.eff.keySet()){
                LOGGER.info(var + ":" + varCodes.get(var) + ", " + op.eff.get(var) + ":" + valCodes.get(op.eff.get(var)));
                eff.forceSetValue(varCodes.get(var), valCodes.get(op.eff.get(var)));
            }

            Action a = new Action(op.name,op.label, agent, pre, eff, op.isPublic);
            LOGGER.info(a);
            if(!unitCost){
                a.setCost(op.cost);
            }
            actions.add(a);
            if(a.isPublic()){
                publicActions.add(a);
            }

        }

        //translate other agents' public actions
        Set<Action> allActions = new LinkedHashSet<Action>(actions);
        for (String otherAgent : agentOperators.keySet()) {
            if (!otherAgent.equals(agent)) {
                for (SASOperator op : agentOperators.get(otherAgent)) {
                    if (op.isPublic) {
                        boolean isNotPure = false;

                        SuperState pre = new SuperState(d);
                        for (String var : op.pre.keySet()) {
                            isNotPure = isNotPure || pre.forceSetValue(varCodes.get(var), valCodes.get(op.pre.get(var)));
                        }

                        SuperState eff = new SuperState(d);
                        for (String var : op.eff.keySet()) {
                            eff.forceSetValue(varCodes.get(var), valCodes.get(op.eff.get(var)));
                        }

                        Action a = new Action(op.name,op.label, otherAgent, pre, eff, true, true, !isNotPure);
                        if(!unitCost){
                            a.setCost(op.cost);
                        }
                        allActions.add(a);
                        if(a.isPublic()){
                            publicActions.add(a);
                        }
                    }
                }
            }
        }


        return new Problem(agent, initState, goalSuperState, actions,allActions,publicActions);
    }

    public Map<String, Set<String>> getAgentVarVals(String agentName) {
        return agentVarVals.get(agentName);
    }

    public int getVarCode(String var) {
        return varCodes.get(var);
    }

    public int getValCode(String val) {
        return valCodes.get(val);
    }

    public State getGlobalInit(){
        Domain d = new Domain("global",publicVarMax,publicValMax,publicVarMax,varMax,publicValMax,valMax);
        d.setSize(varMax);
        //translate initial state
        int[] init = new int[d.sizeGlobal()];
        for (String var : sas.init.keySet()) {
            init[varCodes.get(var)] = valCodes.get(sas.init.get(var));
        }
        return new State(d,init);
    }

    public SuperState getGlobalGoal(){
        Domain d = new Domain("global",publicVarMax,publicValMax,publicVarMax,varMax,publicValMax,valMax);
        d.setSize(varMax);
        //translate goal super-state
        SuperState goalSuperState = new SuperState(d);
        for (String var : sas.goal.keySet()) {
            goalSuperState.forceSetValue(varCodes.get(var),valCodes.get(sas.goal.get(var)));
        }
        return goalSuperState;
    }

    public SASDomain getSASDomain() {
        return sas;
    }

    public void debugPrint(){
        for(String agent : agentVarVals.keySet()){
            LOGGER.info("agent: " + agent);
            for(String var : agentVarVals.get(agent).keySet()){
                LOGGER.info(var + ":" + agentVarVals.get(agent).get(var));
            }
        }
    }

}
