package cz.agents.dimaptools.util;

import java.util.HashMap;
import java.util.Map;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class InteractionGraph {
	
	private final Map<String,Problem> problems = new HashMap<>();
	
	private String graph = "";
	
	
	public void addAgent(String agent, Problem problem){
		problems.put(agent, problem);
	}
	
	public void createGraph(){
		for(String agent1 : problems.keySet()){
			for(String agent2 : problems.keySet()){
				if(!agent1.equals(agent2)){
					
					Problem p1 = problems.get(agent1);
					Problem p2 = problems.get(agent2);
					boolean edge=false;
					
					for(Action a1 : p1.getMyPublicActions()){
						for(Action a2 : p2.getMyPublicActions()){
							if(a1.getEffect().interactsWith(a2.getPrecondition())){
								addEdge(agent1,agent2);
								edge=true;
								break;
							}
						}
						if(edge)break;
					}
					
				}
			}
			
		}
	}

	private void addEdge(String agent1, String agent2) {
		graph += agent1 + " -> " + agent2 + "\n";
	}
	
	public int numberOfAgents(){
		return problems.keySet().size();
	}
	
	public String toString(){
		return graph;
	}

}
