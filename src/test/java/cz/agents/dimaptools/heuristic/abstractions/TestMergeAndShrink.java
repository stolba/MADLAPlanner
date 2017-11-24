package cz.agents.dimaptools.heuristic.abstractions;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestMergeAndShrink {

	@Test
	public void testMerge() {
		System.out.println("--- Abstraction 1: ---");
		Abstraction a1 = new Abstraction();
		
		AbstractState s1 = new AbstractState(1,"1");
		a1.addState(s1);
		AbstractState s2 = new AbstractState(2,"2");
		s2.setGoalState(true);
		a1.addState(s2);
		AbstractState s3 = new AbstractState(3,"3");
		s3.setInitState(true);
		a1.addState(s3);
		
		AbstractEdge e1 = new AbstractEdge(1,1);
		e1.addLabel(1);
		e1.setFrom(s1);
		e1.setTo(s2);
		a1.addEdge(e1);
		AbstractEdge e2 = new AbstractEdge(2,1);
		e2.addLabel(2);
		e2.setFrom(s3);
		e2.setTo(s2);
		a1.addEdge(e2);
		AbstractEdge e3 = new AbstractEdge(3,1);
		e3.addLabel(3);
		e3.setFrom(s3);
		e3.setTo(s1);
		a1.addEdge(e3);
		
		System.out.println(a1);
		
		Dijkstra d = new Dijkstra();
		float dist = d.search(s3);
		System.out.println("PATH("+dist+"):" + d.getShortestPath());
		
		
		System.out.println("\n--- Abstraction 2: ---");

		Abstraction a2 = new Abstraction();
		
		AbstractState s4 = new AbstractState(4,"4");
		s4.setInitState(true);
		a2.addState(s4);
		AbstractState s5 = new AbstractState(5,"5");
		s5.setGoalState(true);
		a2.addState(s5);
		
		AbstractEdge e4 = new AbstractEdge(4,1);
		e4.addLabel(1);
		e4.setFrom(s4);
		e4.setTo(s5);
		a2.addEdge(e4);
		AbstractEdge e5 = new AbstractEdge(5,1);
		e5.addLabel(2);
		e5.setFrom(s5);
		e5.setTo(s4);
		a2.addEdge(e5);
		AbstractEdge e6 = new AbstractEdge(6,1);
		e6.addLabel(3);
		e6.setFrom(s4);
		e6.setTo(s4);
		a2.addEdge(e6);
		
		System.out.println(a2);
		
		System.out.println("--- Merge 1&2: ---");
		
		Abstraction a3 = Abstraction.merge(a1, a2);
		
		System.out.println(a3);
		
		
		
		Set<AbstractState> toShrink = new HashSet<>();
		toShrink.add(a3.getState(0));
		toShrink.add(a3.getState(2));
		
		System.out.println("--- Shrink "+toShrink+": ---");
		
		a3.shrink(toShrink);
		System.out.println(a3);
		
		
		
		toShrink = new HashSet<>();
		toShrink.add(a3.getState(4));
		toShrink.add(a3.getState(6));
		
		System.out.println("--- Shrink "+toShrink+": ---");
		
		a3.shrink(toShrink);
		System.out.println(a3);
		
		toShrink = new HashSet<>();
		toShrink.add(a3.getState(5));
		toShrink.add(a3.getState(7));
		
		System.out.println("--- Shrink "+toShrink+": ---");
		
		a3.shrink(toShrink);
		System.out.println(a3);
		
		d = new Dijkstra();
		dist = d.search(a3.getState(8));
		System.out.println("PATH("+dist+"):" + d.getShortestPath());
	}

	

}
