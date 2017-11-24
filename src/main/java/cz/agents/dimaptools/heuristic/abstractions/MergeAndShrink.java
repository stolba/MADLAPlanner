package cz.agents.dimaptools.heuristic.abstractions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cz.agents.dimaptools.model.Problem;

public class MergeAndShrink {
	
	private Abstraction abstraction;
	private Map<Integer,AtomicProjection> projections = new HashMap<>();
	private final int stateLimit;
	
	private final static Logger LOGGER = Logger.getLogger(MergeAndShrink.class);
	
	public enum ShrinkStrategy {EQUAL_GOAL_DIST, EQUAL_G};

	
	public MergeAndShrink(Problem problem, int stateLimit) {
		this(problem, stateLimit, false);
	}
	
	public MergeAndShrink(Problem problem, int stateLimit, boolean onlyPublic) {
		
		this.stateLimit = stateLimit;
		
		LOGGER.setLevel(Level.WARN);
		LOGGER.info("- Atomic Projections: -");
		
		for(int var=0; var < problem.getDomain().publicVarMax; ++var){
			AtomicProjection ap = new AtomicProjection(problem,var);
			if(LOGGER.isInfoEnabled())LOGGER.info(ap);
			projections.put(var, ap);
		}
		
		if(!onlyPublic){
			for(int var=problem.getDomain().agentVarMin; var < problem.getDomain().agentVarMax; ++var){
				AtomicProjection ap = new AtomicProjection(problem,var);
				if(LOGGER.isInfoEnabled())LOGGER.info(ap);
				projections.put(var, ap);
			}
		}
		
		
	}
	
	public void shrinkOnEqualGoalDist(){
		Set<AbstractState> toShrink = abstraction.getGoalStates();
		Set<AbstractState> closed = new HashSet<>();
		
		while(!toShrink.isEmpty() && !toShrink.contains(abstraction.getInit())){
			AbstractState shrinked;
			
			if(toShrink.size()==1){
				shrinked = toShrink.iterator().next();
			}else{
				shrinked = abstraction.shrink(toShrink);
			}
			
			closed.add(shrinked);
			
			toShrink = new HashSet<AbstractState>();
			for(AbstractEdge e : shrinked.getInEdges()){
				if(!closed.contains(e.getFrom())){
					toShrink.add(e.getFrom());
				}
			}
		}
		
	}
	
	public void shrinkOnEqualInitDist(){
		abstraction.resetSearchStats();
		Dijkstra d = new Dijkstra();
		d.search(abstraction.getInit());
		
		Map<Float,Set<AbstractState>> buckets = new HashMap<>();
		
		for(AbstractState s : abstraction.getAllStates()){
			if(!buckets.containsKey(s.getG())){
				buckets.put(s.getG(), new HashSet<AbstractState>());
			}
			buckets.get(s.getG()).add(s);
		}
		
		for(Set<AbstractState> bucket : buckets.values()){
			abstraction.shrink(bucket);
		}
	
	}
	
	private void shrink(ShrinkStrategy strategy){
		
//		Dijkstra d = new Dijkstra();
//		abstraction.resetSearchStats();
//		float dist = d.search(abstraction.getInit());
//		
//		System.out.println("xxx pre-shrink: xxxx");
//		System.out.println("COST: " + dist);
//		System.out.println("PLAN: " + d.getShortestPath());
		
		switch(strategy){
		case EQUAL_GOAL_DIST:
			shrinkOnEqualGoalDist();
			break;
		case EQUAL_G:
			shrinkOnEqualInitDist();
			break;
		}
		
		
//		abstraction.resetSearchStats();
//		dist = d.search(abstraction.getInit());
//		
//		System.out.println("xxx post-shrink: xxxx");
//		System.out.println("COST: " + dist);
//		System.out.println("PLAN: " + d.getShortestPath());
	}
	
	
	public void mergeAndShrink(ShrinkStrategy strategy){
		
		LOGGER.info("-- Merge: --");
		
		List<AtomicProjection> projList = new LinkedList<>(projections.values());
		abstraction=projList.get(0);
		for(int i = 1; i < projList.size(); ++i){
			
			if(abstraction.getAllStates().size() * projList.get(i).getAllStates().size() > stateLimit ){
				LOGGER.info("- Shrink: -");
				shrink(strategy);
				if(LOGGER.isInfoEnabled())LOGGER.info(abstraction);
			}
			
			Abstraction apm = Abstraction.merge(abstraction, projList.get(i));
			
			if(LOGGER.isInfoEnabled())LOGGER.info(apm);
			
			abstraction = apm;
			
		}
		
		if(abstraction.getAllStates().size() > stateLimit ){
			LOGGER.info("- Shrink: -");
			shrink(strategy);
		}
		
		LOGGER.info("-- Abstraction Done: --");
		if(LOGGER.isInfoEnabled())LOGGER.info(abstraction);
		
	}
	
	
	
	
	public Abstraction getAbstraction(){
		return abstraction;
	}
	
	

}
