package cz.agents.madla.protocol;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import cz.agents.alite.communication.DefaultPerformerCommunicator;
import cz.agents.alite.communication.Message;
import cz.agents.alite.communication.content.Content;
import cz.agents.alite.communication.content.binary.BinaryContent;
import cz.agents.alite.communication.content.binary.BinaryMessageHandler;
import cz.agents.alite.communication.zeromq.MapReceiverTable;
import cz.agents.alite.communication.zeromq.ZeroMQCommunicationChannel;
import cz.agents.madla.communication.protocol.ProtobufEncoder;

public class ProtobufMessagingTest {

    private static MapReceiverTable directory = new MapReceiverTable();
    private static DefaultPerformerCommunicator sender;
    private static DefaultPerformerCommunicator receiver;

    private static ProtobufEncoder encoder = new ProtobufEncoder();

    private static Object received;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        sender = new DefaultPerformerCommunicator("sender");
        setUpCommunicator(sender, "sender", "tcp://localhost:9876");

        receiver = new DefaultPerformerCommunicator("receiver");
        setUpCommunicator(receiver, "receiver", "tcp://localhost:9875");

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        sender.performClose();
        receiver.performClose();
    }

    public static void setUpCommunicator(DefaultPerformerCommunicator comm, final String id, String address){

        comm.addPerformerChannel(new ZeroMQCommunicationChannel(comm,id,address,directory));

        directory.put(id, address);

        comm.addMessageHandler(new BinaryMessageHandler() {

            @Override
            public void handleMessage(Message message, BinaryContent content) {

                received = encoder.decode(content);

                System.out.println(id + " received msg from " + message.getSender() + " content: " + received.toString());

            }
        });
    }

    public static void send(Content content){
        Message msg = sender.createMessage(content);
        msg.addReceiver("receiver");
        sender.sendMessage(msg);
    }

    public static void receiveAndTest(Object msg){
        received = null;
        while(received==null){
            receiver.performReceiveNonblock();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertTrue(msg.toString().equals(received.toString()));
    }

//	@Test
//	public void testStateMessage() {
//
//		int[] stateValues = {1,2,3};
//		StateMessage msg = new StateMessage(stateValues,1,2);
//
//		System.out.println("Send StateMessage: " + msg);
//
//		send(encoder.encodeStateMessage(msg));
//		receiveAndTest(msg);
//	}
//
//	@Test
//	public void testHeuristicReplyWithPublicActionsMessage() {
//
//		int[] ids = {1,2,3};
//		HeuristicReplyWithPublicActionsMessage msg = new HeuristicReplyWithPublicActionsMessage(1, 2, 3, 4, ids, 5);
//
//		System.out.println("HeuristicReplyWithPublicActionsMessage: " + msg);
//
//		send(encoder.encodeHeuristicReplyWithPublicActionsMessage(msg));
//		receiveAndTest(msg);
//	}
//
//	@Test
//	public void testHeuristicRequestMessage() {
//
//		int[] stateValues = {1,2,3};
//		int[] requestedValues = {3,2,1};
//		HeuristicRequestMessage msg = new HeuristicRequestMessage(1, stateValues, requestedValues, 2);
//
//		System.out.println("HeuristicRequestMessage: " + msg);
//
//		send(encoder.encodeHeuristicRequestMessage(msg));
//		receiveAndTest(msg);
//	}
//
//	@Test
//	public void testPlanningFinishedMessage() {
//
//		PlanningFinishedMessage msg = new PlanningFinishedMessage();
//
//		System.out.println("PlanningFinishedMessage: " + msg);
//
//		send(encoder.encodePlanningFinishedMessage(msg));
//		receiveAndTest(msg);
//	}
//
//	@Test
//	public void testReconstructPlanMessage() {
//
//		String[] plan = {"a1","a2","a3"};
//		ReconstructPlanMessage msg = new ReconstructPlanMessage(Arrays.asList(plan),3);
//
//		System.out.println("ReconstructPlanMessage: " + msg);
//
//		send(encoder.encodeReconstructPlanMessage(msg));
//		receiveAndTest(msg);
//	}

}
