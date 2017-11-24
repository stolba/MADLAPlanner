package cz.agents.dimaptools.communication.protocol;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.alite.communication.Message;
import cz.agents.alite.communication.MessageHandler;
import cz.agents.alite.communication.QueuedCommunicator;
import cz.agents.alite.communication.protocol.DefaultProtocol;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;

public abstract class DistributedHeuristicRequestProtocol extends DefaultProtocol {
	
	private static final Logger LOGGER = Logger.getLogger(DistributedHeuristicRequestProtocol.class);
	
	private final EncoderInterface encoder;

	public DistributedHeuristicRequestProtocol(Communicator communicator, String name, final EncoderInterface encoder) {
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
				
				
				if(data instanceof HeuristicReplyWithPublicActionsMessage){
					receiveHeuristicReplyWithPublicActionsMessage((HeuristicReplyWithPublicActionsMessage)data,message.getSender());
				}
				
				
			}
		});
	}
	
	
	public void sendHeuristicRequestMessage(HeuristicRequestMessage req, String receiver){
		
		if(LOGGER.isDebugEnabled())LOGGER.debug(communicator.getAddress() + " send " + req + " to " + receiver);
		Message message = communicator.createMessage(encoder.encodeHeuristicRequestMessage(req));
	    message.addReceiver(receiver);
	    communicator.sendMessage(message);
		
	}
	
	
	public abstract void receiveHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re, String sender);


	public String getAddress() {
		return communicator.getAddress();
	}


	public static void registerClasses(QueuedCommunicator comm){
		comm.handleMessageClass(HeuristicReplyWithPublicActionsMessage.class);
	}
	

}
