package cz.agents.madla.executor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class IPCOutputExecutor implements PlanExecutorInterface {
	
	public final String OUTPUT = "out.plan";

	private Map<String,Problem> problems = new HashMap<String,Problem>();
	private State initState;
	private SuperState goalSuperState;

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
		List<Action> actionPlan = new LinkedList<Action>();
		for(String s : plan){
			String[] list = s.split(" ");
			String agent = list[1];
			int hash = Integer.parseInt(list[list.length-1]);
			boolean added = false;
			
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
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(OUTPUT, "UTF-8");
			
			int i = 0;
			for(Action a : actionPlan){
				writer.println(i + ": " + a.printToPlan());
				++i;
			}
			
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		return true;

	}

	@Override
	public boolean executePartialPlan(List<String> plan, String initiator,int solutionCost) {
		return true;
	}

}
