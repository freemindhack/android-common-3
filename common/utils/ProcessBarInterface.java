package nocom.common.utils;


public interface ProcessBarInterface {
	public void onTimeout ();


	public void onProcessRoutineFail ();


	public void onProcessRoutineSucc ();


	public long isTimeout (long baseMs);


	public boolean isProcessRoutineDone ();


	public boolean isProcessRoutineFail ();


	public String getMessage ();


	public boolean isNeedFullScreen ();


	public Runnable processRoutine ();
}
