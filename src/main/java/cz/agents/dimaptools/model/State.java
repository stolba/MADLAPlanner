package cz.agents.dimaptools.model;


/**
 * Same as SuperState, except has to have all variables in domain set)
 * @author stolba
 *
 */
public class State extends SuperState {

	public State(Domain domain, int[] values) {
		super(domain,false);
		this.values = values;

		setValues = 0;
		for(int val : values){
			if(domain.isDefined(val)){
				++setValues;
			}
		}

		if(domain.sizeGlobal() != values.length){
			throw new RuntimeException("Incompatible domain sizes!");
		}

//		for(int val : values){
//			if(!domain.inDomainVal(val)){
//				throw new RuntimeException("Undefined value: "+domain.humanizeVal(val)+"!");
//			}
//		}
	}

	public Domain getDomain(){
		return domain;
	}

	public State(State state) {
        super(state);
    }

    @Override
    public String toString() {
        return "State [] -> " + super.toString();
    }
    
    @Override
    public int hashCode() {
    	return super.hashCode();
    }

}
