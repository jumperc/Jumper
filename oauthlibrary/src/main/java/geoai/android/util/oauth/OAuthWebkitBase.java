package geoai.android.util.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.geoai.android.util.oauth.R;

import geoai.android.util.HttpUtil;


public abstract class OAuthWebkitBase extends OAuthBase {

	private final static String REDIRECT_URL = "https://www.dzyt.com.cn/auth_redirect.php";
	private final static String ENCODING = "utf-8";

	/**
	 * 返回授权URL
	 */
	protected abstract String getAuthorizeURL();

	/**
	 * 返回获取用户信息的URL，为空则不去获取URL
	 */
	protected abstract String getInfoURL();

	/**
	 * 获取用户信息的URL是否是HTTP GET
	 */
	protected boolean getInfoUrlIsGet() {
		return true;
	}

	/**
	 * 根据access_token的数据生成调用API时使用的oauth相关参数。缺省只设置access_token
	 * 
	 * @param data
	 * @return
	 */
	protected JSONObject buildAccessTokenParam(JSONObject data) {
		try {
			return new JSONObject().accumulate("access_token",
					data.getString("access_token"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}

	/**
	 * 返回一个数组，第一个元素是错误代码的字段名，第二个元素是错误信息的字段名
	 */
	protected String[] getErrorFieldNames() {
		return new String[] { "code", "msg" };
	}

	/**
	 * 判断数据中是否包含错误信息，并清除错误信息，返回错误信息
	 */
	protected Throwable parseError(JSONObject data) {
		final String[] errorFieldNames = getErrorFieldNames();
		Throwable e = new Exception(data.optString(errorFieldNames[1]));
		data.remove(errorFieldNames[0]);
		data.remove(errorFieldNames[1]);
		return e;
	}

	private static Map<String, Object> _SESSION = new HashMap<String, Object>();

	private static String uniqid(String prefix, boolean more_entropy) {
		String data = prefix + String.valueOf(new Date().getTime())
				+ String.valueOf(Math.random());
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] result = md5.digest(data.getBytes());
			return byte2hex(result);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return data;
	}

	private final String getState() {
		return getState(false, false);
	}

	/**
	 * 获取state，如果没有，则生成一个新的
	 * 
	 * @param regen
	 *            是否重新生成一个新的state。缺省false
	 * @param useMediaType
	 *            是否在state中添加MediaType前缀。缺省false
	 */
	private final String getState(boolean regen, boolean useMediaType) {
		if (regen) {
			String prefix = (useMediaType) ? getMediaType() + '_' : "";
			String state = uniqid(prefix, true);
			_SESSION.put("oauth_state", state);
			return state;
		} else {
			if (_SESSION.containsKey("oauth_state"))
				return (String) _SESSION.get("oauth_state");
			else
				return getState(true, useMediaType);
		}
	}

	/**
	 * 清除state
	 */
	private static final void clearState() {
		_SESSION.remove("oauth_state");
	}

	//
	// public final static String getToken(String mediaType) {
	// JSONObject data = getData(mediaType, false);
	// if (data != null)
	// try {
	// return (String) data.get("access_token");
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

//	public static final JSONObject getData(String mediaType) {
//		return getData(mediaType, true);
//	}
//
//	@SuppressWarnings("unchecked")
//	public static final JSONObject getData(String mediaType, boolean create) {
//		Map<String, Object> data = (Map<String, Object>) _SESSION
//				.get("OAuth_tokens");
//		if (data == null) {
//			data = new HashMap<String, Object>();
//			_SESSION.put("OAuth_tokens", data);
//		}
//		JSONObject data2 = (JSONObject) data.get("mediaType");
//		if (data2 == null && create) {
//			data2 = new JSONObject();
//			data.put(mediaType, data2);
//
//		}
//		return data2;
//	}

//	protected final void saveData(JSONObject data) {
//		saveData(data, false);
//	}
//
//	@SuppressWarnings("unchecked")
//	protected final void saveData(JSONObject data, boolean replaceAll) {
//		String mediaType = getMediaType();
//		Map<String, Object> tokens = (Map<String, Object>) _SESSION
//				.get("OAuth_tokens");
//		if (tokens == null) {
//			tokens = new HashMap<String, Object>();
//			_SESSION.put("OAuth_tokens", tokens);
//		}
//		JSONObject data2 = (JSONObject) tokens.get(mediaType);
//		if (replaceAll || data2 == null) {
//			tokens.put(mediaType, data);
//			return;
//		}
//		try {
//			mergeJSONObject(data2, data);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * 返回重定向url，包括media_type参数
	 */
	protected final String getRedirectURL() {
		Map<String, Object> urlData = new HashMap<String, Object>();
		urlData.put("media_type", getMediaType());
		return combineURL(REDIRECT_URL, urlData);
	}

	/**
	 * 返回取消时重定向的URL
	 */
	protected String getCancelURL() {
		return null;
	}

	/**
	 * 修改Login的参数，缺省不做修改，直接返回。
	 */
	protected void modifyLoginParam(Map<String, Object> data) {
	}

	/**
	 * 修改最终的Login URL，缺省不做修改，直接返回原参数。
	 * 
	 * @param
	 *
	 * @return string
	 */
	protected String modifyLoginUrl(String url) {
		return url;
	}

	/**
	 * 返回登录授权的URL
	 * 
	 * @return string
	 */
	public final String getLoginURL() {
		String state = getState();
		String redirect_url = getRedirectURL();
		Map<String, Object> keysArr = new HashMap<String, Object>();
		keysArr.put("response_type", "token");
		keysArr.put("client_id", getAppKey());
		keysArr.put("redirect_uri", redirect_url);
		keysArr.put("state", state);
		modifyLoginParam(keysArr);
		String login_url = combineURL(getAuthorizeURL(), keysArr);
		login_url = modifyLoginUrl(login_url);
		return login_url;
	}

	/**
	 * combineURL 拼接url
	 * 
	 * @param baseURL
	 *            基于的url
	 * @param keysArr
	 *            Map<String, Object>|JSONObject|String 参数列表数组或者字符串
	 * @return string 返回拼接的url
	 */
	public static String combineURL(String baseURL, Object keysArr) {
		int pos = baseURL.indexOf('?');
		String combined = (pos < 0) ? baseURL + "?" : baseURL + '&';
		ArrayList<String> valueArr = new ArrayList<String>();
		if (keysArr instanceof String) {
			try {
				valueArr.add(URLEncoder.encode((String) keysArr, ENCODING));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else if (keysArr instanceof JSONObject) {
			JSONObject arr = (JSONObject) keysArr;
			@SuppressWarnings("rawtypes")
			Iterator it = arr.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				try {
					valueArr.add(key + "="
							+ URLEncoder.encode(arr.optString(key), ENCODING));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else if (keysArr instanceof Map) {
			@SuppressWarnings({ "rawtypes" })
			Map arr = (Map) keysArr;
			if (arr != null) {
				for (Object key : arr.keySet()) {
					try {
						valueArr.add(key
								+ "="
								+ URLEncoder.encode((String) arr.get(key),
										ENCODING));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}

		combined += implode("&", valueArr);

		return combined;
	}

	protected Activity activity;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public final void doOAuth(Activity activity) {
		this.activity = activity;
		final String login_url = getLoginURL();
		System.out.println("login_url: " + login_url);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.oauth_webkit_layout, null);
		final WebView mWebView = (WebView) layout.findViewById(R.id.webView1);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSavePassword(false);
		Dialog dlg = new Dialog(activity);
		dlg.setContentView(layout);
		dlg.setCancelable(true);
		dlg.setCanceledOnTouchOutside(true);
		dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mWebView.stopLoading();
				OAuthWebkitBase.this.onCancel();
			}
		});
		dlg.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				mWebView.loadUrl(login_url);
				InputMethodManager inManager = (InputMethodManager) mWebView
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inManager.showSoftInput(mWebView,
						InputMethodManager.SHOW_FORCED);
			}
		});
		mWebView.setWebViewClient(new MyWebViewClient(dlg));
		dlg.show();
	}

	@Override
	public final void doUserInfo(Activity activity, JSONObject data) {
		this.activity = activity;
		final String infoURL = getInfoURL();
		if (infoURL != null && infoURL.length() > 0) {
			JSONObject keysArr = buildAccessTokenParam(data);
			mergeUrlJSON(activity, data, infoURL, keysArr, getInfoUrlIsGet());
		}
		onToken(data);
	}

	/**
	 * 获取url中的JSON数据，并合并到data中
	 * @param activity
	 * @param data	合并目标
	 * @param url	要获取的url
	 * @param params	url的参数
	 */
	protected final void mergeUrlJSON(Activity activity, final JSONObject data,
			String url, Object params) {
		mergeUrlJSON(activity, data, url, params, true);
	}

	/**
	 * 获取url中的JSON数据，并合并到data中
	 * @param activity
	 * @param data	合并目标
	 * @param url	要获取的url
	 * @param params	url的参数
	 * @param isGet	url是否HTTP GET
	 */
	protected final void mergeUrlJSON(Activity activity, final JSONObject data,
			String url, Object params, boolean isGet) {
		JSONObject user = null;
		System.out.println("mergeUrlJSON: " + url);
		try {
			if (isGet) {
				String url2 = combineURL(url, params);
				user = HttpUtil.httpGetJSON(activity, url2);
			} else {
				user = HttpUtil.httpPutJSON(activity, url, params);
			}
		} catch (HttpUtil.CanceledNetworkException e) {
			onCancel();
			return;
		} catch (HttpUtil.NetworkException e) {
			onError(e);
			return;
		}
		try {
			mergeJSONObject(data, user);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * java字节码转HEX字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		StringBuilder hs = new StringBuilder();

		for (int n = 0; n < b.length; n++) {
			// 整数转成十六进制表示
			final String tmp = Integer.toHexString(b[n] & 0XFF);
			if (tmp.length() == 1) {
				hs.append("0");
			}
			hs.append(tmp);
		}
		return hs.toString();

	}

	public static String implode(String glue, Iterable<String> pieces) {
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = pieces.iterator();
		while (it.hasNext()) {
			String str = it.next();
			if (sb.length() > 0)
				sb.append(glue);
			sb.append(str);
		}

		return new String(sb);
	}

	private class MyWebViewClient extends WebViewClient {
		final Dialog dialog;

		public MyWebViewClient(Dialog dialog) {
			this.dialog = dialog;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			dialog.dismiss();
			OAuthWebkitBase.this.onError(new Exception("web error: "
					+ description));
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith(getRedirectURL())) {
				url = getUrParamslPart(url);
				final JSONObject values = parseUrlParams(url);
				if (values != null && values.length() > 0) {
					String[] errFields = getErrorFieldNames();
					if (values.has(errFields[0])) {
						OAuthWebkitBase.this.onError(parseError(values));
						dialog.dismiss();
						return true;
					}
					try {
						String state = values.getString("state");
						if (!getState().equals(state)) {
							onError(new Exception(
									"The state does not match. You may be a victim of CSRF."));
							return true;
						}
						clearState();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					final Activity activity = (Activity) view.getContext();
					// new AsyncTask<Void, Void, Void>() {
					// @Override
					// protected Void doInBackground(Void... params) {
					// processTokenData(activity, values);
					// return null;
					// }
					// }.execute();
					processTokenData(activity, values);
					dialog.dismiss();
					return true;
				}
			} else {
				final String cancelURL = getCancelURL();
				if (cancelURL != null && cancelURL.length() != 0
						&& url.startsWith(cancelURL)) {
					OAuthWebkitBase.this.onCancel();
					dialog.dismiss();
					return true;
				}
			}
			return false;
		}

	}

	private static JSONObject parseUrlParams(String url) {
		JSONObject ret = new JSONObject();
		if (url == null || url.length() == 0)
			return ret;
		for (String str : url.split("&")) {
			String[] keyAndValues = str.split("=");
			if (keyAndValues != null && keyAndValues.length == 2) {
				String key = keyAndValues[0];
				String value = keyAndValues[1];
				try {
					ret.put(URLDecoder.decode(key, ENCODING),
							URLDecoder.decode(value, ENCODING));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 * 返回回调的重定向url中的参数部分。缺省返回?到#之间的内容
	 * @param url 回调的重定向url
	 * @return
	 */
	protected String getUrParamslPart(String url) {
		int start = url.indexOf("?");
		if (start < 0)
			return null;
		start++;
		int stop = url.indexOf('#', start);
		if (stop < 0)
			stop = url.length();
		if (start >= stop)
			return null;
		String part = url.substring(start, stop);
		return part;
	}
}
