package stateSpace;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.PriorityQueue;

public class CANNetworkState implements NetworkState<CANMessage> {

    private PriorityQueue<CANMessage> messagesBuffer;

    public CANNetworkState() {
        this(new PriorityQueue<>(new CANMessagesComparator()));
    }

    public CANNetworkState(CANNetworkState canNetworkState) {
        this(canNetworkState.messagesBuffer);
    }

    private CANNetworkState(PriorityQueue<CANMessage> messagesBuffer) {
        this.messagesBuffer = messagesBuffer;
    }

    @Override
    @Nullable
    public Message getNextReadyToSendMessage() {
        final CANMessage canMessage = messagesBuffer.poll();
        return canMessage != null ? canMessage.getMessage() : null;
    }

    @Override
    public void addMessageToBuffer(CANMessage message) {
        messagesBuffer.add(message);
    }

    @Nullable
    public CANMessage highestPriority() {
        return messagesBuffer.peek();
    }

    public boolean isBufferEmpty() {
        return messagesBuffer.isEmpty();
    }

//    public void increaseMessagesArrivalTime(float deltaTime) {
//        messagesBuffer.forEach(canMessage ->
//                canMessage.getMessage().setArrivalTime(canMessage.getMessage().getArrivalTime() + deltaTime)
//        );
//    }

    public static class CANMessagesComparator implements Comparator<CANMessage> {

        @Override
        public int compare(CANMessage m1, CANMessage m2) {
            return m1.getPriority().compareTo(m2.getPriority());
        }
    }
}
