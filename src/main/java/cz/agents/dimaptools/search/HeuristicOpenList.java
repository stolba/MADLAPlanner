package cz.agents.dimaptools.search;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.model.State;

public class HeuristicOpenList implements Comparable<HeuristicOpenList>{
	public final String label;
	private final int hash;
	private final PriorityBlockingQueue<SearchState> open;
	private final PriorityBlockingQueue<SearchState> preOpen;
	private final HeuristicInterface heuristic;
    private final HeuristicInterface requestHeuristic;
    
    private final boolean usePreferred;
    private final boolean recomputeHeuristiconReceive;
    
    private int openPriority = 0;
    private int preOpenPriority = 0;
    
    public HeuristicOpenList(String label,boolean usePreferred, HeuristicInterface heuristic, boolean recomputeHeuristiconReceive) {
    	this(label,usePreferred,heuristic,heuristic, recomputeHeuristiconReceive);
    }
    
	public HeuristicOpenList(String label,boolean usePreferred, HeuristicInterface heuristic, HeuristicInterface requestHeuristic, boolean recomputeHeuristiconReceive) {
		super();
		this.label = label;
		this.heuristic = heuristic;
		this.requestHeuristic = requestHeuristic;
		this.usePreferred = usePreferred;
		this.recomputeHeuristiconReceive = recomputeHeuristiconReceive;
		
		open = new PriorityBlockingQueue<SearchState>();
		preOpen = new PriorityBlockingQueue<SearchState>();
		hash = label.hashCode();
	}
	
	
	public boolean isEmpty(){
		return open.isEmpty() && (!usePreferred || preOpen.isEmpty());
	}
	
	public boolean usePreferred(){
		return usePreferred;
	}
	
	public boolean recomputeHeuristicOnReceive(){
		return recomputeHeuristiconReceive;
	}
	
	public SearchState pollOpen(){
		
			if(usePreferred){
				
				if(preOpen.isEmpty()){
					--openPriority;
					return open.poll();
				}
				
				if(open.isEmpty()){
					--preOpenPriority;
					return preOpen.poll();
				}

				if(preOpenPriority >= openPriority){
					--preOpenPriority;
					return preOpen.poll();
				}else{
					--openPriority;
					return open.poll();
				}
				
			}else{
				return open.poll();
			}
			
	}
	
	
	public void boost(boolean boostPreferred) {
		
		if(boostPreferred){
			preOpenPriority += 1000;  
//			System.out.println("boost preferred: " + preOpenPriority + ", normal: " + openPriority);
		}else{
			openPriority += 1000; 
//			System.out.println("boost normal: " + openPriority + ", preferred: " + preOpenPriority);
		}
//		System.out.println("boost "+label+"("+boostPreferred+"), preferred: " + preOpenPriority + ", normal: " + openPriority);
	}
	
	public void add(SearchState state, boolean helpful){
		open.add(state);
		if(helpful)preOpen.add(state);
	}
	
	public void addAll(Collection<SearchState> states, boolean helpful){
		open.addAll(states);
		if(helpful)preOpen.addAll(states);
	}
	
	
	public void getHeuristic(State state, HeuristicComputedCallback callback){
		heuristic.getHeuristic(state, callback);
	}

	/**
	 * Process communication
	 */
	public void processMessages(){
		heuristic.processMessages();
		requestHeuristic.processMessages();
	}
	
	public int getPriority(){
		return Math.max(openPriority, preOpenPriority);
	}
	
	@Override
	public int compareTo(HeuristicOpenList o) {
		return getPriority() - o.getPriority();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeuristicOpenList other = (HeuristicOpenList) obj;
		if (hash != other.hash)
			return false;
		return true;
	}

	public int size(boolean preferred) {
		if(preferred){
			return preOpen.size();
		}else{
			return open.size();
		}
	}

	

	

	

}