package cz.agents.dimaptools.communication.protocol;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.alite.communication.Message;
import cz.agents.alite.communication.MessageHandler;
import cz.agents.alite.communication.QueuedCommunicator;
import cz.agents.alite.communication.channel.CommunicationChannelBroadcast;
import cz.agents.alite.communication.protocol.DefaultProtocol;
import cz.agents.dimaptools.communication.message.ActionCostMessage;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.message.MessageVisitorInterface;
import cz.agents.dimaptools.communication.message.PlanningFinishedMessage;
import cz.agents.dimaptools.communication.message.ReconstructPlanMessage;
import cz.agents.dimaptools.communication.message.ReconstructRPMessage;
import cz.agents.dimaptools.communication.message.SharedProblemInfoMessage;
import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.dimaptools.communication.message.VisitableMessage;

public abstract class SynchronizedDistributedHeuristicProtocol extends DefaultProtocol implements MessageVisitorInterface {
	
	private static final Logger LOGGER = Logger.getLogger(SynchronizedDistributedHeuristicProtocol.class);

	private final EncoderInterface encoder;

	public SynchronizedDistributedHeuristicProtocol(Communicator communicator, final String name, final EncoderInterface encoder) {
		super(communicator, name);

		this.encoder = encoder;
		
		communicator.addMessageHandler(new MessageHandler() {
			
			@Override
			public void notify(Message message) {
				if(message.getSender().equals(name))return;
				
				
				Object data = null;
				
				try{
					data = encoder.decode(message.getContent());
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
				
				if(data == null){
					LOGGER.warn("received empty data: " + message);
				}
				
//				LOGGER.info("receive " + message);
				
				((VisitableMessage)data).visit(SynchronizedDistributedHeuristicProtocol.this, message.getSender());
				
				
			}
		});
	}
	
	
	
	
	public void sendActionCostMessage(ActionCostMessage msg){
//		LOGGER.info(communicator.getAddress() + " send ActionCostMessage: " + msg);
		Message message = communicator.createMessage(encoder.encodeActionCostMessage(msg));
        message.addReceiver(CommunicationChannelBroadcast.BROADCAST_ADDRESS);
        communicator.sendMessage(message);
	}
	
	
	public static void registerClasses(QueuedCommunicator comm){
		comm.handleMessageClass(ActionCostMessage.class);
	}




	@Override
	public void process(HeuristicReplyWithPublicActionsMessage msg, String sender) {
		
	}

	@Override
	public void process(HeuristicRequestMessage msg, String sender) {
		
	}

	@Override
	public void process(PlanningFinishedMessage msg, String sender) {
		
	}

	@Override
	public void process(ReconstructPlanMessage msg, String sender) {
		
	}

	@Override
	public void process(StateMessage msg, String sender) {
		
	}
	
	@Override
	public void process(SharedProblemInfoMessage msg, String sender) {
		
	}
	
	@Override
	public void process(ReconstructRPMessage reconstructRPMessage, String sender){
		
	}
	
	

}
