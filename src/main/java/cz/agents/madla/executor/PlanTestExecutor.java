package cz.agents.madla.executor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class PlanTestExecutor implements PlanExecutorInterface {

	private Map<String,Problem> problems = new HashMap<String,Problem>();
	private State initState;
	private SuperState goalSuperState;
	private float cost = -1;
	private List<Action> actionPlan = null;

	public void setInitAndGoal(State initState, SuperState goalSuperState){
		this.initState = initState;
		this.goalSuperState = goalSuperState;
	}

	public void addProblem(Problem problem){
		problems.put(problem.agent, problem);
	}

	/* (non-Javadoc)
	 * @see cz.agents.madla.executor.PlanExecutorInterface#testPlan(java.util.List)
	 */
	@Override
	public boolean executePlan(List<String> plan) {
		actionPlan = new LinkedList<Action>();
		for(String s : plan){
			String[] split = s.split(" ");
			String agent = split[1];
//			String label = s.split(" ")[2];
			int hash = Integer.parseInt(split[split.length-1]);
			boolean added = false;
			
//			System.out.println("agent:"+agent);
			
			Action a = problems.get(agent).getAction(hash);
			if(a != null){
				actionPlan.add(a);
				added = true;
			}
			
			if(!added){
				String label = s.split(" ")[2];
				System.err.println("EXECUTOR: Action " + label + " from plan not found in the problem!");
				return false;
			}
		}
		
		cost = 0;

		State s = new State(initState);
		for(Action a : actionPlan){
//			System.out.println("EXECUTOR: Check: " + a.getLabel() + " projection:"+a.isProjection());
			if(a.isApplicableIn(s)){
				a.transform(s);
				cost += a.getCost();
			}else{
				System.err.println("EXECUTOR: Action " + a + " not applicable in "+s+"!");
				a.isApplicableIn(s);
				cost = -1;
				return false;
			}
		}

		if(s.unifiesWith(goalSuperState)){
			System.out.println("EXECUTOR: Plan of "+actionPlan.size()+" actions (cost "+cost+") correct!");
			
			return true;
		}else{
			System.err.println("EXECUTOR: Goal not reached!");
			cost = -1;
			return false;
		}

	}
	
	public float getCost(){
		return cost;
	}
	
	public List<Action> getActionPlan(){
		return actionPlan;
	}

	@Override
	public boolean executePartialPlan(List<String> plan, String initiator,int solutionCost) {
		return true;
	}

}
