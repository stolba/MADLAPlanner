package cz.agents.dimaptools.communication.message;

import java.util.Arrays;

/**
 * Message used to send the search state
 * @author stolba
 *
 */
public class StateMessage  implements VisitableMessage{

	private final int[] values;
	private final int g;
	private final int h;
	private final int hash;
	private final boolean preferred;
	private final boolean globalH;
	private final String queueID;


	/**
	 * creator
	 * @param values State value array
	 * @param g State cost
	 * @param h State heuristic estimate
	 */
	public StateMessage(int[] values, int g, int h) {
		this(values,g,h,false);
	}

	/**
	 * creator
	 * @param values State value array
	 * @param g State cost
	 * @param h State heuristic estimate
	 * @param preferred true if expanded by preferred operator
	 */
	public StateMessage(int[] values, int g, int h, boolean preferred) {
		this(values,g,h,false,preferred);
	}
	
	public StateMessage(int[] values, int g, int h, boolean globalH, boolean preferred) {
		this.values = values;
		this.g = g;
		this.h = h;
		this.preferred = preferred;
		this.globalH = globalH;
		this.queueID = null;

		hash = Arrays.hashCode(values);
	}
	
	public StateMessage(int[] values, int g, int h, boolean preferred, String queueID) {
		this.values = values;
		this.g = g;
		this.h = h;
		this.preferred = preferred;
		this.globalH = false;
		this.queueID = queueID;

		hash = Arrays.hashCode(values);
	}

	public int[] getValues() {
		return values;
	}

	public int getG() {
		return g;
	}

	public int getH() {
		return h;
	}

	public int getHash() {
		return hash;
	}
	
	public boolean isPreferred(){
		return preferred;
	}
	
	public boolean isGlobalH(){
		return globalH;
	}

	@Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "State ["+Arrays.toString(values)+"]";
    }

    public int getBytes() {
        return values.length * 4 + 4 + (queueID!=null?1:0);
    }
    
    public boolean hasQueueID(){
    	return queueID != null;
    }
    
    public String getQueueID(){
    	return queueID;
    }
    
    @Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}

}
