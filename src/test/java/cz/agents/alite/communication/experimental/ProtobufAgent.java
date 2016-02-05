package cz.agents.alite.communication.experimental;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.DefaultPerformerCommunicator;
import cz.agents.alite.communication.Message;
import cz.agents.alite.communication.content.binary.BinaryContent;
import cz.agents.alite.communication.content.binary.BinaryMessageHandler;
import cz.agents.alite.communication.zeromq.ZeroMQCommunicationChannel;
import cz.agents.alite.communication.zeromq.ZeroMQCommunicationChannel.ReceiverTable;
import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.madla.communication.protocol.ProtobufEncoder;

public class ProtobufAgent{

    private final static Logger LOGGER = Logger.getLogger(ProtobufAgent.class);

    public final String id;
//	private final String address;
    private final ReceiverTable directory;
    private final DefaultPerformerCommunicator comm;
    private final ProtobufEncoder encoder = new ProtobufEncoder();

    private final Timer timer = new Timer();
    private boolean closed = false;

    public int received = 0;


    public ProtobufAgent(final String id, String address, ReceiverTable directory) {
        super();
        this.id = id;
//		this.address = address;
        this.directory = directory;

        LOGGER.info(id + " INIT COMMUNICATOR");

        comm = new DefaultPerformerCommunicator(id);
        comm.addPerformerChannel(new ZeroMQCommunicationChannel(comm,id,address,directory));

        comm.addMessageHandler(new BinaryMessageHandler() {

            @Override
            public void handleMessage(Message message, BinaryContent content) {

                String msg = encoder.decode(content).toString();

                LOGGER.info(id + " received msg from " + message.getSender() + " content: " + msg);

                ++received;

                waitRandom(500,500);

                sendMessage(message.getSender());

            }
        });



    }

    public void initConversation() {
        LOGGER.info(id + " SCHEDULE RECEIVER");

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if(!closed)comm.performReceiveNonblock();
            }
        }, 0, 100);

//		waitRandom(100,0);

        LOGGER.info(id + " INIT CONVERSATION");
        for(String rec : directory.getAvailableReceiverIDs()){
            if(!rec.equals(id)){
                sendMessage(rec);
                waitRandom(100,100);
            }
        }

    }

    private void sendMessage(String rec){
        if(closed)return;


        int[] values = {1,2,3};
        StateMessage sm = new StateMessage(values,4,received);

        //String bin = new String(((BinaryContent)encoder.encodeStateMessage(sm)).getData());
        LOGGER.info(id + " send msg to " + rec + ", content: " + sm + ", binary: " + Arrays.toString(((BinaryContent)encoder.encodeStateMessage(sm)).getData()));

        Message message = comm.createMessage(encoder.encodeStateMessage(sm));
        message.addReceiver(rec);
        comm.sendMessage(message);
    }

    public void waitRandom(int w, int r){
        try {
            Thread.sleep((int)(w + r * Math.random()));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void close(){
        closed = true;
        comm.performClose();
    }





}
