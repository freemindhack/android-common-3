package nocom.common.network;

import android.os.Handler;

public class SendRecvDataManager extends SendRecvData implements
		SendRecvDataInteface {
	public SendRecvDataManager() {
		super();
	}

	@Override
	public SendRecvData getSendRecvData(String data, int whatData,
			OnReceived rcvedHandler) {
		return new SendRecvData(data, whatData, rcvedHandler);
	}

	@Override
	public SendRecvData getSendRecvData(byte[] data, int whatData,
			OnReceived rcvedHandler) {
		return new SendRecvData(data, whatData, rcvedHandler);
	}

	@Override
	public SendRecvData getSendRecvData(byte[] data, int whatData,
			Handler rcvedMsgHandler) {
		return new SendRecvData(data, whatData, rcvedMsgHandler);
	}

	@Override
	public SendRecvData getSendRecvData(String data, int whatData,
			Handler rcvedMsgHandler) {
		return new SendRecvData(data, whatData, rcvedMsgHandler);
	}

	@Override
	public SendRecvData getSendRecvData(byte[] data, int whatData,
			Handler rcvedMsgHandler, OnReceived rcvedCallback) {
		return new SendRecvData(data, whatData, rcvedMsgHandler, rcvedCallback);
	}
}
