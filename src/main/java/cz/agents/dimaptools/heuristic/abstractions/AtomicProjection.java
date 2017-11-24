package cz.agents.dimaptools.heuristic.abstractions;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class AtomicProjection extends Abstraction {
	
	private final String varStr;
	private final int varInt;

	public AtomicProjection(Problem problem, int var) {
		super();
		varStr = problem.getDomain().humanizeVar(var);
		varInt = var;
		
		for(int val : problem.getDomain().getVariableDomains().get(var)){
			AbstractState s = new AbstractState(val,var+"-"+val);//problem.getDomain().humanizeVal(val));
			if(problem.goalSuperState.isSet(var)){
				if(problem.goalSuperState.getValue(var)==val){
					s.setGoalState(true);
				}
			}else{
				s.setGoalState(true);
			}
			if(problem.initState.isDefined(var) && problem.initState.getValue(var)==val){
				s.setInitState(true);
			}
			this.addState(s);
		}
		
		
		for(Action a : problem.getAllActions()){
			if(a.getEffect().getNumberOfSetValues()==0) continue;
			
			AbstractState from = a.getPrecondition().isSet(var) ? this.getState(a.getPrecondition().getValue(var)) : null;
			AbstractState to = a.getEffect().isSet(var) ? this.getState(a.getEffect().getValue(var)) : null;
			
			if(from!=null && to!=null){
				createEgde(a,from,to);
			}
			
			if(from==null && to!=null){
				for(AbstractState f : this.getAllStates()){
					createEgde(a,f,to);
				}
			}
			
			if(from!=null && to==null){
				createEgde(a,from,from);
			}
			
			if(from==null && to==null){
				for(AbstractState f : this.getAllStates()){
					createEgde(a,f,f);
				}
			}
			
			
		}
		
	}
	
	private void createEgde(Action a, AbstractState from, AbstractState to){
		AbstractEdge e = new AbstractEdge(this.newEdgeID(),a.getCost());
//		e.addLabel(a.getLabel());
		e.addLabel(a.hashCode());
		e.setFrom(from);
		e.setTo(to);
		this.addEdge(e);
	}

	@Override
	public String toString() {
		return "AtomicProjection " + varStr + "(" + varInt + "): " + super.toString();
	}
	
	
	
	

}
