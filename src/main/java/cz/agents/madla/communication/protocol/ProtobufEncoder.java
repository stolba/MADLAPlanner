package cz.agents.madla.communication.protocol;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import cz.agents.alite.communication.content.Content;
import cz.agents.alite.communication.content.binary.BinaryContent;
import cz.agents.dimaptools.communication.message.ActionCostMessage;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.message.PlanningFinishedMessage;
import cz.agents.dimaptools.communication.message.ReconstructPlanMessage;
import cz.agents.dimaptools.communication.message.ReconstructRPMessage;
import cz.agents.dimaptools.communication.message.SharedProblemInfoMessage;
import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.dimaptools.communication.protocol.EncoderInterface;
import cz.agents.dimaptools.heuristic.relaxed.RelaxedPlan;
import cz.agents.madla.communication.generated.ProtobufActionCostMessage.PBActionCostMessage;
import cz.agents.madla.communication.generated.ProtobufHeuristicReplyWithPublicActionsMessage.PBHeuristicReplyWithPublicActionsMessage;
import cz.agents.madla.communication.generated.ProtobufHeuristicRequestMessage.PBHeuristicRequestMessage;
import cz.agents.madla.communication.generated.ProtobufPlanningFinishedMessage.PBPlanningFinishedMessage;
import cz.agents.madla.communication.generated.ProtobufProtocolMessage.ProtocolMessage;
import cz.agents.madla.communication.generated.ProtobufProtocolMessage.ProtocolMessage.Type;
import cz.agents.madla.communication.generated.ProtobufReconstructPlanMessage.PBReconstructPlanMessage;
import cz.agents.madla.communication.generated.ProtobufSharedProblemInfoMessage.PBSharedProblemInfoMessage;
import cz.agents.madla.communication.generated.ProtobufStateMessage.PBStateMessage;
import cz.agents.madla.communication.generated.ReconstructRPMessage.PBReconstructRPMessage;

public class ProtobufEncoder implements EncoderInterface {
	
	private final static Logger LOGGER = Logger.getLogger(ProtobufEncoder.class);

	@Override
	public Content encodeStateMessage(StateMessage msg) {
		PBStateMessage.Builder pbmsgb = PBStateMessage.newBuilder()
				.setG(msg.getG())
				.setH(msg.getH())
				.setHash(msg.getHash())
				.setPreferred(msg.isPreferred());
		
		for(int v : msg.getValues()){
			pbmsgb.addValues(v);
		}
		
		if(msg.isGlobalH()){
			pbmsgb.setGlobalH(true);
		}
		
		if(msg.hasQueueID()){
			pbmsgb.setQueueID(msg.getQueueID());
		}
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.STATE)
				.setState(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}

	@Override
	public Content encodeReconstructPlanMessage(ReconstructPlanMessage msg) {
		PBReconstructPlanMessage.Builder pbmsgb = PBReconstructPlanMessage.newBuilder()
				.setLastStateHash(msg.getLastStateHash())
				.addAllPlan(msg.getPlan())
				.setInitiator(msg.getInitiatorID())
				.setSolutionCost(msg.getSolutionCost());
		
		
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.RECONSTRUCT_PLAN)
				.setReconstructPlan(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}

	@Override
	public Content encodePlanningFinishedMessage(PlanningFinishedMessage msg) {
		PBPlanningFinishedMessage.Builder pbmsgb = PBPlanningFinishedMessage.newBuilder();
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.PLANNING_FINISHED)
				.setPlanningFinished(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}
	
	@Override
	public Content encodeHeuristicRequestMessage(HeuristicRequestMessage req) {
		PBHeuristicRequestMessage.Builder pbmsgb = PBHeuristicRequestMessage.newBuilder()
				.setHash(req.getRequestHash())
				.setRecursionDepth(req.getRecursionDepth());
		
		for(int v : req.getRequestedValues()){
			pbmsgb.addRequestedValues(v);
		}
		
		for(int v : req.getStateValues()){
			pbmsgb.addStateValues(v);
		}
		
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.HEURISTIC_REQUEST)
				.setHeuristicRequest(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}

	@Override
	public Content encodeHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re) {
		PBHeuristicReplyWithPublicActionsMessage.Builder pbmsgb = PBHeuristicReplyWithPublicActionsMessage.newBuilder()
				.setGoalHash(re.getGoalHash())
				.setHeuristicValue(re.heuristicValue)
				.setRecursionDepth(re.getRecursionDepth())
				.setRequestHash(re.getRequestHash())
				.setStateHash(re.getStateHash());
		
		for(int a : re.usedPublicActionIDs){
			pbmsgb.addUsedPublicActionIDs(a);
		}
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.HEURISTIC_REPLY_WPA)
				.setHeuristicReplyWPA(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}
	
	@Override
	public Content encodeSharedProblemInfoMessage(SharedProblemInfoMessage msg) {
		PBSharedProblemInfoMessage.Builder pbmsgb = PBSharedProblemInfoMessage.newBuilder().setCoupling(msg.getCouplingEstimate());
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.PROBLEM_INFO)
				.setProblemInfo(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
		
	}
	
	
	@Override
	public Content encodeActionCostMessage(ActionCostMessage msg) {
		PBActionCostMessage.Builder pbmsgb = PBActionCostMessage.newBuilder().setActionHash(msg.getActionHash()).setStateHash(msg.getStateHash()).setHeuristicValue(msg.getHeuristicValue());
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.ACTION_COST)
				.setActionCost(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}

	@Override
	public Content encodeReconstructRPMessage(ReconstructRPMessage msg) {
		PBReconstructRPMessage.Builder pbmsgb = PBReconstructRPMessage.newBuilder().setActionHash(msg.getActionHash());
		
		for(int a : msg.getRelaxedPlan().toArray()){
			pbmsgb.addRelaxedPlan(a);
		}
		
		ProtocolMessage pm = ProtocolMessage.newBuilder()
				.setType(Type.RECONSTRUCT_RP)
				.setReconstructRP(pbmsgb.build())
				.build();
		
		return new BinaryContent(pm.toByteArray());
	}
	
	
	
	
	
	@Override
	public Object decode(Content content){
		ProtocolMessage pm = null;
		
		try {
			pm = ProtocolMessage.parseFrom(((BinaryContent)content).getData());
		} catch (InvalidProtocolBufferException e) {
			LOGGER.error("Problem decoding protobuf message: " + Arrays.toString(((BinaryContent)content).getData()), e);
			return null;
		}
		
		switch(pm.getType().getNumber()){
		case Type.STATE_VALUE:
			return decodeStateMessage(pm.getState());
		case Type.HEURISTIC_REPLY_WPA_VALUE:
			return decodeHeuristicReplyWPA(pm.getHeuristicReplyWPA());
		case Type.HEURISTIC_REQUEST_VALUE:
			return decodeHeuristicRequestMessage(pm.getHeuristicRequest());
		case Type.PLANNING_FINISHED_VALUE:
			return decodePlanningFinishedMessage(pm.getPlanningFinished());
		case Type.RECONSTRUCT_PLAN_VALUE:
			return decodeReconstructPlanMessage(pm.getReconstructPlan());
		case Type.PROBLEM_INFO_VALUE:
			return decodeSharedProblemInfoMessage(pm.getProblemInfo());
		case Type.ACTION_COST_VALUE:
			return decodeActionCost(pm.getActionCost());
		case Type.RECONSTRUCT_RP_VALUE:
			return decodeReconstructRPMessage(pm.getReconstructRP());
		}
		LOGGER.warn("Unknown message type: " + pm);
		
		return null;
	}
	
	
	
	
	

	private ReconstructRPMessage decodeReconstructRPMessage(PBReconstructRPMessage pbmsg) {
		RelaxedPlan rp = new RelaxedPlan();
		
		for(int a : pbmsg.getRelaxedPlanList()){
			rp.add(a);
		}
		
		return new ReconstructRPMessage(pbmsg.getActionHash(), rp);
	}

	private ActionCostMessage decodeActionCost(PBActionCostMessage pbmsg) {
		return new ActionCostMessage(pbmsg.getStateHash(), pbmsg.getActionHash(), pbmsg.getHeuristicValue());
	}

	private ReconstructPlanMessage decodeReconstructPlanMessage(PBReconstructPlanMessage pbmsg) {
		return new ReconstructPlanMessage(new ArrayList<String>(pbmsg.getPlanList()),pbmsg.getLastStateHash(),pbmsg.getInitiator(),pbmsg.getSolutionCost());
	}

	private PlanningFinishedMessage decodePlanningFinishedMessage(PBPlanningFinishedMessage pbmsg) {
		return new PlanningFinishedMessage();
	}

	private HeuristicRequestMessage decodeHeuristicRequestMessage(PBHeuristicRequestMessage pbmsg) {
		int[] svals = new int[pbmsg.getStateValuesCount()];
		for(int i = 0; i < svals.length; ++i)svals[i]=pbmsg.getStateValues(i);
		
		int[] rvals = new int[pbmsg.getRequestedValuesCount()];
		for(int i = 0; i < rvals.length; ++i)rvals[i]=pbmsg.getRequestedValues(i);
		
		return new HeuristicRequestMessage(pbmsg.getHash(),svals,rvals,pbmsg.getRecursionDepth());
	}

	private HeuristicReplyWithPublicActionsMessage decodeHeuristicReplyWPA(PBHeuristicReplyWithPublicActionsMessage pbmsg) {
		int[] vals = new int[pbmsg.getUsedPublicActionIDsCount()];
		for(int i = 0; i < vals.length; ++i)vals[i]=pbmsg.getUsedPublicActionIDs(i);
		
		return new HeuristicReplyWithPublicActionsMessage(pbmsg.getRequestHash(),pbmsg.getStateHash(),pbmsg.getGoalHash(),pbmsg.getHeuristicValue(),vals,pbmsg.getRecursionDepth());
	}

	public StateMessage decodeStateMessage(PBStateMessage pbmsg){
		int[] vals = new int[pbmsg.getValuesCount()];
		for(int i = 0; i < vals.length; ++i)vals[i]=pbmsg.getValues(i);
		
		boolean globalH = pbmsg.hasGlobalH() ? pbmsg.getGlobalH() : false;
		
		String queueID = pbmsg.hasQueueID() ? pbmsg.getQueueID() : null;
		
		if(queueID==null){
			return new StateMessage(vals,pbmsg.getG(),pbmsg.getH(),globalH,pbmsg.getPreferred());
		}else{
			return new StateMessage(vals,pbmsg.getG(),pbmsg.getH(),pbmsg.getPreferred(),pbmsg.getQueueID());
		}
	}
	
	private Object decodeSharedProblemInfoMessage(PBSharedProblemInfoMessage problemInfo) {
		return new SharedProblemInfoMessage(problemInfo.getCoupling());
	}

	

	

	

}
