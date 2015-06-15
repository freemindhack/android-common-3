package nocom.common.utils;


public interface ProcessBarInterface {
	public void onTimeout ();


	public void onProcessRoutineFail ();


	public void onProcessRoutineSucc ();


	public void onCancel ();


	public long isTimeout (long baseMs);


	public boolean isProcessRoutineDone ();


	public enum ProcessRoutineState {
		NOT_GOT, SUCC, FAIL,
	};


	public ProcessRoutineState getProcessRoutineState ();


	public void setMessage (String msg);


	public boolean isNeedFullScreen ();


	public void startProcessRoutine ();


	public void stopProcessRoutine ();
}
