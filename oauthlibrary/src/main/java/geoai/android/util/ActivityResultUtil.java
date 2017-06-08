package geoai.android.util;
/**
 * 
 */


import android.content.Intent;
import android.util.SparseArray;

/**
 * @author xyf
 *
 */
public class ActivityResultUtil {
	/**
	 * 
	 */
	public interface OnActivityResultListener{
		/**
		 * @see android.app.Activity#onActivityResult(int, int, Intent)
		 */
		void onActivityResult(int requestCode, int resultCode, Intent data);
	}
	
	private static ActivityResultUtil instance;
	/**
	 * 获取实例
	 * @return
	 */
	public static ActivityResultUtil getInstance() {
		if(instance == null)
			instance = new ActivityResultUtil();
		return instance;
	}
	
	private static final int START_REQUEST_CODE = 10001; 
	
	/**
	 * 
	 */
	public ActivityResultUtil() {
		
	}
	
	private int currentRequestCode = START_REQUEST_CODE;
	private SparseArray<OnActivityResultListener> handlerMap = new SparseArray<OnActivityResultListener>();
	
	/**
	 * 注册ActivityResult，返回requestCode，并将其WeakReference保存在数组中
	 * @param handler
	 * @return 新的requestCode
	 */
	public int getNewRquestCode(OnActivityResultListener handler){
		handlerMap.put(currentRequestCode, handler);
		return currentRequestCode ++;
	}

	/**
	 * 调用注册的ActivityResult，并删除
	 * @see android.app.Activity#onActivityResult(int, int, Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		OnActivityResultListener handler = handlerMap.get(requestCode);
		if (handler != null) {
			handlerMap.remove(requestCode);
			handler.onActivityResult(requestCode, resultCode, data);
		}
	}
	
}
