package cz.agents.madla.protocol;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import cz.agents.alite.communication.content.Content;
import cz.agents.alite.communication.content.binary.BinaryContent;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.message.PlanningFinishedMessage;
import cz.agents.dimaptools.communication.message.ReconstructPlanMessage;
import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.madla.communication.protocol.ProtobufEncoder;

public class ProtobufEncoderTest {

	@Test
	public void testHeuristicReplyWithPublicActionsMessage() {
		ProtobufEncoder encoder = new ProtobufEncoder();
		
		int[] ids = {1,2,3};
		HeuristicReplyWithPublicActionsMessage msg = new HeuristicReplyWithPublicActionsMessage(1, 2, 3, 4, ids, 5);
		Content content = encoder.encodeHeuristicReplyWithPublicActionsMessage(msg);
		HeuristicReplyWithPublicActionsMessage nmsg = (HeuristicReplyWithPublicActionsMessage) encoder.decode(content);
		
		System.out.println("HeuristicReplyWithPublicActionsMessage: " + Arrays.toString(((BinaryContent)content).getData()));
		
		assertTrue(msg.toString().equals(nmsg.toString()));
	}
	
	@Test
	public void testHeuristicRequestMessage() {
		ProtobufEncoder encoder = new ProtobufEncoder();
		
		int[] stateValues = {1,2,3};
		int[] requestedValues = {3,2,1};
		HeuristicRequestMessage msg = new HeuristicRequestMessage(1, stateValues, requestedValues, 2);
		Content content = encoder.encodeHeuristicRequestMessage(msg);
		HeuristicRequestMessage nmsg = (HeuristicRequestMessage) encoder.decode(content);
		
		System.out.println("HeuristicRequestMessage: " + Arrays.toString(((BinaryContent)content).getData()));
		
		assertTrue(msg.toString().equals(nmsg.toString()));
	}
	
	@Test
	public void testPlanningFinishedMessage() {
		ProtobufEncoder encoder = new ProtobufEncoder();
		
		PlanningFinishedMessage msg = new PlanningFinishedMessage();
		Content content = encoder.encodePlanningFinishedMessage(msg);
		PlanningFinishedMessage nmsg = (PlanningFinishedMessage) encoder.decode(content);
		
		System.out.println("PlanningFinishedMessage: " + Arrays.toString(((BinaryContent)content).getData()));
		
		assertTrue(msg.toString().equals(nmsg.toString()));
	}
	
	@Test
	public void testReconstructPlanMessage() {
		ProtobufEncoder encoder = new ProtobufEncoder();
		
		String[] plan = {"a1","a2","a3"};
		ReconstructPlanMessage msg = new ReconstructPlanMessage(Arrays.asList(plan),3,"id",10);
		Content content = encoder.encodeReconstructPlanMessage(msg);
		ReconstructPlanMessage nmsg = (ReconstructPlanMessage) encoder.decode(content);
		
		System.out.println(msg);
		System.out.println("ReconstructPlanMessage: " + Arrays.toString(((BinaryContent)content).getData()));
		System.out.println(nmsg);
		
		assertTrue(msg.toString().equals(nmsg.toString()));
	}
	
	@Test
	public void testStateMessage() {
		ProtobufEncoder encoder = new ProtobufEncoder();
		
		int[] stateValues = {1,2,3};
		StateMessage msg = new StateMessage(stateValues,1,2);
		Content content = encoder.encodeStateMessage(msg);
		StateMessage nmsg = (StateMessage) encoder.decode(content);
		
		System.out.println("StateMessage: " + Arrays.toString(((BinaryContent)content).getData()));
		
		assertTrue(msg.toString().equals(nmsg.toString()));
	}
	

}
