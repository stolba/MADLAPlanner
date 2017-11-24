package cz.agents.dimaptools.heuristic;

import cz.agents.dimaptools.heuristic.relaxed.HelpfulActions;

public class HeuristicResult {
	
	private final boolean deadEnd;
	private final int hValue;
	private final float hValueF;
	private final HelpfulActions helpfulActions;
	
	/**
	 * dead end
	 */
	public HeuristicResult() {
		super();
		this.hValue = HeuristicInterface.LARGE_HEURISTIC;
		this.hValueF = HeuristicInterface.LARGE_HEURISTIC;
		helpfulActions = null;
		deadEnd = true;
	}
	
	public HeuristicResult(int hValue) {
		super();
		this.hValue = hValue;
		this.hValueF = hValue;
		helpfulActions = null;
		deadEnd = hValue >= HeuristicInterface.LARGE_HEURISTIC;
	}
	
	public HeuristicResult(float hValueF) {
		super();
		this.hValue = (int)hValueF;
		this.hValueF = hValueF;
		helpfulActions = null;
		deadEnd = hValue >= HeuristicInterface.LARGE_HEURISTIC;
	}

	public HeuristicResult(int hValue, HelpfulActions helpfulActions) {
		super();
		this.hValue = hValue;
		this.hValueF = hValue;
		this.helpfulActions = helpfulActions;
		deadEnd = hValue >= HeuristicInterface.LARGE_HEURISTIC;
	}

	public int getValue() {
		return hValue;
	}
	
	public float getValueF() {
		return hValueF;
	}
	
	public boolean isDeadEnd(){
		return deadEnd;
	}
	
	public boolean hasHelpfulActions(){
		return helpfulActions != null;
	}

	public HelpfulActions getHelpfulActions() {
		return helpfulActions;
	}
	
	
	
	

}
