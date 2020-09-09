package stateSpace;

public interface NetworkState<T> {

    public Message getNextReadyToSendMessage();

    public void addMessageToBuffer(T message);
}
