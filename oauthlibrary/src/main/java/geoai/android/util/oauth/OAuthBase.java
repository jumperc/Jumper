/**
 * 
 */
package geoai.android.util.oauth;

import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

/**
 * oauth2.0授权客户端的基类
 * @author xyf
 * $Id$
 */
public abstract class OAuthBase {
	/**
	 * OAuth处理
	 */
	public interface OAuthHandler{
		/**
		 * 授权完成后返回授权数据
		 * @param data OAuth返回的数据：
		 * <table border="1">
		 * <tr><th>参数名</th><th>必填</th><th>参数说明</th></tr>
		 * <tr><td>access_token</td><td>是</td><td>授权token</td></tr>
		 * <tr><td>access_token_data</td><td>是</td><td>token的详细数据</td></tr>
		 * <tr><td>authorize_time</td><td></td><td>授权的时间</td></tr>
		 * <tr><td>openid</td><td>是</td><td>服务端用户id</td></tr>
		 * <tr><td>username</td><td>是</td><td>用户名</td></tr>
		 * <tr><td>figureurl</td><td></td><td>头像url</td></tr>
		 * <tr><td>figureurl2</td><td></td><td>大头像url</td></tr>
		 * <tr><td>birthday</td><td></td><td>生日。格式为yyyy-mm-dd，年月日为0表示未知，年可能为“70后”之类的</td></tr>
		 * <tr><td>gender</td><td></td><td>性别：1 男，2 女，其他 未知</td></tr>
		 * <tr><td>email</td><td></td><td>用户email</td></tr>
		 * </table>
		 */
		public void onToken(JSONObject data);
		
		/**
		 * 授权被用户取消
		 */
		public void onCancel();
		
		/**
		 * 授权失败
		 * @param e
		 */
		public void onError(Throwable e);
	}
	
	/**
	 * 返回模块名
	 * @return
	 */
	public abstract String getMediaType();
	/**
	 * 返回client_id
	 * @return
	 */
	public abstract String getAppKey();
	
	private OAuthHandler oauthHandler;
	
	/**
	 * @return the oauthHandler
	 */
	public final OAuthHandler getOauthHandler() {
		return oauthHandler;
	}
	/**
	 * @param oauthHandler the oauthHandler to set
	 */
	public final void setOauthHandler(OAuthHandler oauthHandler) {
		this.oauthHandler = oauthHandler;
	}
	
	/**
	 * 开始授权过程。成功调用{@link #processTokenData(Activity, JSONObject)}
	 */
	public abstract void doOAuth(Activity activity);
	
	/**
	 * 获取用户信息，并将其与TokenData合并。成功调用{@link#onToken(JSONObject)}
	 * @param activity
	 * @param data
	 */
	public void doUserInfo(Activity activity, final JSONObject data){
		onToken(data);
	}
	
	/**
	 * 将服务器返回的用户信息翻译成固定的格式，并将其返回。
	 * @param data
	 * @see OAuthHandler#onToken(JSONObject)
	 */
	protected abstract void translateUserInfo(final JSONObject data) throws JSONException;
	
	private JSONObject tokenData;
	
	public final JSONObject getTokenData() {
		return tokenData;
	}
	protected final void setTokenData(JSONObject tokenData) {
		this.tokenData = tokenData;
	}
	
	/**
	 * @see OAuthHandler#onToken(JSONObject)
	 */
	protected final void onToken(JSONObject data) {
		System.out.println("beforeTranslate");
		System.out.println(data);

		try {
			translateUserInfo(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		tokenData = data;
		
		System.out.println("onToken");
		System.out.println(data);
		
		if(oauthHandler != null)
			oauthHandler.onToken(data);
	}

	/**
	 * @see com.geoai.android.util.oauth.OAuthHandler#onCancel()
	 */
	protected final void onCancel() {
		if(oauthHandler != null)
			oauthHandler.onCancel();
	}

	/**
	 * @see com.geoai.android.util.oauth.OAuthHandler#onError(Throwable)
	 */
	protected final void onError(Throwable e) {
		if(oauthHandler != null)
			oauthHandler.onError(e);
	}
	
	/**
	 * 将tokenData转换为需要存储的data.提供给子类的{@link #doOAuth(Activity)}中调用
	 * @param tokenData
	 */
	protected final void processTokenData(final Activity activity, final JSONObject tokenData) {
		System.out.println("tokenData");
		System.out.println(tokenData);
		final JSONObject data = new JSONObject();
		try {
			data.put("access_token", tokenData.get("access_token"));
			data.put("authorize_time", new Date().getTime() / 1000);
			data.put("access_token_data", tokenData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.tokenData = tokenData;
		new Thread() {
			@Override
			public void run() {
				doUserInfo(activity, data);
			}
		}.start();
	}

	/**
	 * 合并JSONObject到j1
	 * @param j1
	 * @param j2
	 * @throws JSONException
	 */
	public static final void mergeJSONObject (final JSONObject j1, JSONObject ... j2) throws JSONException{
		for(JSONObject j : j2){
			if(j == null) continue;
			@SuppressWarnings("rawtypes")
			Iterator it = j.keys();
			while(it.hasNext()){
				String key = (String) it.next();
				j1.put(key, j.get(key));
			}
		}
	}

	private static final String birthdayPatternStr = "/(?:(\\d{4,})(?:年)?|(\\d+后))(?:\\/|-)?(\\d+)(?:月)?(?:\\/|-)?(\\d+)(?:日)?/";
	private static Pattern birthdayPattern;

	/**
	 * 尝试格式化生日字符串。yyyy-mm-dd，年月日为0表示未知，年可能为“70后”之类的。
	 */
	public static String parseBirthday(String birthday) {
		if (birthdayPattern == null)
			birthdayPattern = Pattern.compile(birthdayPatternStr);
		Matcher matches = birthdayPattern.matcher(birthday);
		if (matches.matches()) {
			birthday = matches.group(1) + matches.group(2) + '-'
					+ matches.group(3) + '-' + matches.group(4);
		}
		return birthday;
	}

}

