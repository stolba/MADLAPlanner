package cz.agents.dimaptools.communication.protocol;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.alite.communication.Message;
import cz.agents.alite.communication.MessageHandler;
import cz.agents.alite.communication.QueuedCommunicator;
import cz.agents.alite.communication.channel.CommunicationChannelBroadcast;
import cz.agents.alite.communication.protocol.DefaultProtocol;
import cz.agents.dimaptools.communication.message.PlanningFinishedMessage;
import cz.agents.dimaptools.communication.message.ReconstructPlanMessage;
import cz.agents.dimaptools.communication.message.StateMessage;

public abstract class DistributedSearchProtocol extends DefaultProtocol {
	
	private static final Logger LOGGER = Logger.getLogger(DistributedSearchProtocol.class);

	private final EncoderInterface encoder;

	public DistributedSearchProtocol(Communicator communicator, String name, final EncoderInterface encoder) {
		super(communicator, name);

		this.encoder = encoder;
		
		communicator.addMessageHandler(new MessageHandler() {
			
			@Override
			public void notify(Message message) {
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
				
				if(data instanceof StateMessage)receiveStateMessage((StateMessage)data,message.getSender());
				if(data instanceof ReconstructPlanMessage)receiveReconstructPlanMessage((ReconstructPlanMessage)data);
				if(data instanceof PlanningFinishedMessage)receivePlanningFinishedMessage((PlanningFinishedMessage)data);
				
			}
		});
	}
	
	
	
	
	public void sendStateMessage(StateMessage msg, String receiver){
		if(LOGGER.isDebugEnabled())LOGGER.debug(communicator.getAddress() + " send state " + msg);
		Message message = communicator.createMessage(encoder.encodeStateMessage(msg));
        message.addReceiver(receiver);
        communicator.sendMessage(message);
	}
	
	public void sendReconstructPlanMessage(ReconstructPlanMessage msg, String owner){
		if(LOGGER.isDebugEnabled())LOGGER.debug(communicator.getAddress() + " send " + msg + " to " + owner);
		Message message = communicator.createMessage(encoder.encodeReconstructPlanMessage(msg));
        message.addReceiver(owner);
        communicator.sendMessage(message);
	}
	
	public void sendPlanningFinishedMessage(){
		if(LOGGER.isDebugEnabled())LOGGER.debug(communicator.getAddress() + " send PlanningFinishedMessage ");
		Message message = communicator.createMessage(encoder.encodePlanningFinishedMessage(new PlanningFinishedMessage()));
        message.addReceiver(CommunicationChannelBroadcast.BROADCAST_ADDRESS);
        communicator.sendMessage(message);
	}
	
	
	public abstract void receiveStateMessage(StateMessage msg, String sender);
	
	public abstract void receiveReconstructPlanMessage(ReconstructPlanMessage msg);
	
	public abstract void receivePlanningFinishedMessage(PlanningFinishedMessage msg);

	
	public static void registerClasses(QueuedCommunicator comm){
		comm.handleMessageClass(StateMessage.class);
		comm.handleMessageClass(ReconstructPlanMessage.class);
		comm.handleMessageClass(PlanningFinishedMessage.class);
	}
}
