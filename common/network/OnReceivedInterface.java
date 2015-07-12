
package nocom.common.network;


public interface OnReceivedInterface {
	public void handleReceived (byte[] data, int count, int dataId,
		boolean calledMsgHandler);
}
