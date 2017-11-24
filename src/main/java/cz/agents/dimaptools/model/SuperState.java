package cz.agents.dimaptools.model;

import gnu.trove.TIntHashSet;

import java.util.Arrays;


/**
 * State represented by an array of integers - index represents the variable, value the value. Interpreted by the Domain.
 * @author stolba
 *
 */
public class SuperState {

//	private static final Logger LOGGER = Logger.getLogger(SuperState.class);

    protected int[] values;
    protected final Domain domain;	//shared

    protected int setValues = 0;
    private int hash = 0;

    protected SuperState(Domain domain, boolean initValues) {
        this.domain = domain;

        if(initValues){
            values = domain.getEmptyDomain();
        }

    }

    public SuperState(Domain domain) {
        this.domain = domain;

        values = domain.getEmptyDomain();

    }

    public SuperState(Domain domain, int[] values) {
        this.domain = domain;

        this.values = values;

        for(int val : values){
            if(domain.isDefined(val)){
                ++setValues;
            }
        }

    }


    public SuperState(SuperState stateToCopy) {
        domain = stateToCopy.domain;
        values = new int[domain.sizeGlobal()];
        values = Arrays.copyOf(stateToCopy.values, domain.sizeGlobal());
        setValues = stateToCopy.setValues;
    }



    public int[] getValues() {
        return values;
    }

    public int getNumberOfSetValues(){
        return setValues;
    }

    public TIntHashSet getSetVariableNames(){

        TIntHashSet set = new TIntHashSet();

        for(int var = 0; var < values.length; ++var){
            if(domain.isDefined(values[var])){
                set.add(var);
            }
        }

        return set;
    }


    public int getValue(int var) {
        if (!domain.isDefined(values[var])){
            throw new RuntimeException("State variable "+domain.humanizeVar(var)+" not defined!");
        }
        return values[var];
    }

    public boolean isSet(int var){
        return domain.inDomainVar(var) && domain.isDefined(values[var]);
    }

    public boolean isDefined(int var) {
        return domain.isDefined(values[var]);
    }

    /**
     * Set value with consistency checks
     * @param var
     * @param val
     */
    public void setValue(int var, int val) {
        if (!domain.inDomainVar(var)) {
            throw new RuntimeException("Setting state variable "+domain.humanizeVar(var)+", which is not present in the domain!");
        }
        if (!domain.inDomainVal(val)) {
            throw new RuntimeException("Setting incompatible value "+val+" ("+domain.humanizeVal(val)+")!");
        }
        if(!domain.isDefined(values[var]) && domain.isDefined(val)){
            ++setValues;
        }
        values[var] = val;
        hash = 0;
    }

    /**
     * Set value without (WARNING) consistency checks
     * @param var
     * @param val
     * @return returns true if setting was forced, i.e either the set variable or value are not in the agent's domain
     */
    public boolean forceSetValue(int var, int val) {
        if(!domain.isDefined(values[var]) && domain.isDefined(val)){
            ++setValues;
        }
        if(domain.isDefined(values[var]) && !domain.isDefined(val)){
            --setValues;
        }
        values[var] = val;
        hash = 0;

        return !domain.inDomainVar(var) || !domain.inDomainVal(val);
    }

    public void setAllValuesBy(SuperState superState) {
        int var;
        for(var = 0; var < domain.publicVarMax; ++var){
            if(domain.isDefined(superState.values[var])){
                setValue(var,superState.values[var]);
            }
        }
        for(var = domain.agentVarMin; var < domain.agentVarMax; ++var){
            if(domain.isDefined(superState.values[var])){
                setValue(var,superState.values[var]);
            }
        }

        hash = 0;
    }

    /**
     * Checks whether all variables which are set in otherSuperState (wrt domain) have the same values in this super state.
     * @param otherSuperState
     * @return
     */
    public boolean unifiesWith(SuperState otherSuperState) {
//		LOGGER.info(domain.humanize(this.getValues()) + "=?" + domain.humanize(otherSuperState.getValues()));
//		LOGGER.info(Arrays.toString(this.getValues()) + "=?" + Arrays.toString(otherSuperState.getValues()));
        int var;
        for(var = 0; var < domain.publicVarMax; ++var){
//        	LOGGER.info(var + ": " + domain.humanizeVal(values[var]) + " =? " + domain.humanizeVal(otherSuperState.values[var]));
            if(domain.isDefined(otherSuperState.values[var]) && otherSuperState.values[var] != values[var]){
//        		LOGGER.info("FALSE");
                return false;
            }
        }
        for(var = domain.agentVarMin; var < domain.agentVarMax; ++var){
//        	LOGGER.info(domain.humanizeVar(var) + ": " + domain.humanizeVal(values[var]) + " =? " + domain.humanizeVal(otherSuperState.values[var]));
            if(domain.isDefined(otherSuperState.values[var]) && otherSuperState.values[var] != values[var]){
//        		LOGGER.info("FALSE");
                return false;
            }
        }

//        LOGGER.info("TRUE");
        return true;
    }
    
    /**
     * Checks whether all public variables which are set in otherSuperState (wrt domain) have the same values in this super state.
     * @param otherSuperState
     * @return
     */
    public boolean unifiesPubliclyWith(SuperState otherSuperState) {
//		LOGGER.info(domain.humanize(this.getValues()) + "=?" + domain.humanize(otherSuperState.getValues()));
//		LOGGER.info(Arrays.toString(this.getValues()) + "=?" + Arrays.toString(otherSuperState.getValues()));
        int var;
        for(var = 0; var < domain.publicVarMax; ++var){
//        	LOGGER.info(var + ": " + domain.humanizeVal(values[var]) + " =? " + domain.humanizeVal(otherSuperState.values[var]));
            if(domain.isDefined(otherSuperState.values[var]) && otherSuperState.values[var] != values[var]){
//        		LOGGER.info("FALSE");
                return false;
            }
        }

//        LOGGER.info("TRUE");
        return true;
    }

    /**
     * Checks (bidirectionally) whether all variables which are defined in both super states have the same values.
     * @param otherSuperState
     * @return
     */
    public boolean unifiesFullyWith(SuperState otherSuperState) {
        int var;
        for(var = 0; var < domain.sizeGlobal(); ++var){
            if(domain.isDefined(otherSuperState.values[var])
                    && otherSuperState.domain.isDefined(values[var])
                    && otherSuperState.values[var] != values[var]){
                return false;
            }
        }

        return true;
    }

    //returns true if any variable is set in both states
    public boolean interactsWith(SuperState otherSuperState) {
        int var;
        for(var = 0; var < domain.publicVarMax; ++var){
            if(isSet(var) && otherSuperState.isSet(var)){
                return true;
            }
        }
        for(var = domain.agentVarMin; var < domain.agentVarMax; ++var){
            if(isSet(var) && otherSuperState.isSet(var)){
                return true;
            }
        }
        return false;
    }


    public boolean equals(Object obj) {
        if (obj instanceof SuperState) {
            return Arrays.equals(values, ((SuperState) obj).values);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        if(hash != 0){
            return hash;
        }

        hash = Arrays.hashCode(values);
        return hash;
    }

    @Override
    public String toString() {
        return "SuperState [stateVariableMap["+Arrays.toString(values)+"]=" + domain.humanize(values) + "]";
    }

    public int getBytes() {
        return values.length * 4 + 4 + 4;
    }

}
