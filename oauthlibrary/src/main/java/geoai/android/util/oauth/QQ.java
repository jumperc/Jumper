package geoai.android.util.oauth;//package com.geoai.android.util.oauth;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.app.Activity;
//
//import com.tencent.connect.UserInfo;
//import com.tencent.tauth.IUiListener;
//import com.tencent.tauth.Tencent;
//import com.tencent.tauth.UiError;
//
//public class QQ extends OAuthBase {
//
//	private static final String SCOPE = "all";
////	private static final String GRAPH_USER_INFO = "https://graph.qq.com/user/get_user_info";
//	private Tencent mTencent;
//
//	public QQ() {
//	}
//
//	@Override
//	public String getMediaType() {
//		return "QQ";
//	}
//
//	@Override
//	public String getAppKey() {
//		return "101070838";
//	}
//
//	@Override
//	public void doOAuth(final Activity activity) {
//		if (mTencent == null){
//			mTencent = Tencent.createInstance(getAppKey(),
//					activity.getApplicationContext());
//		}else {
//			if (mTencent.isSessionValid()) {
//				JSONObject tokenData = new JSONObject();
//				try {
//					tokenData.put("access_token", mTencent.getAccessToken());
//					tokenData.put("expire_in", mTencent.getExpiresIn());
//					tokenData.put("openid", mTencent.getOpenId());
//					processTokenData(activity, tokenData);
//					return;
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		mTencent.login(activity, SCOPE, new IUiListener() {
//			@Override
//			public void onError(UiError arg0) {
//				QQ.this.onError(new Exception("code: " + arg0.errorCode + ", msg: " + arg0.errorMessage + ", detail: " + arg0.errorDetail));
//			}
//
//			@Override
//			public void onComplete(Object response) {
//				processTokenData(activity, (JSONObject) response);
//			}
//
//			@Override
//			public void onCancel() {
//				QQ.this.onCancel();
//			}
//		});
//	}
//
//	@Override
//	public void doUserInfo(Activity activity, final JSONObject data) {
//		UserInfo u = new UserInfo(activity, mTencent.getQQToken());
//		u.getUserInfo(new IUiListener() {
//
//			@Override
//			public void onCancel() {
//				QQ.this.onCancel();
//			}
//
//			@Override
//			public void onComplete(Object arg0) {
//				JSONObject userData = (JSONObject) arg0;
//				try {
//					mergeJSONObject(data, userData);
//					onToken(data);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onError(UiError arg0) {
//				QQ.this.onError(new Exception(arg0.errorMessage));
//			}
//		});
//	}
//
//	@Override
//	protected void translateUserInfo(JSONObject data) throws JSONException {
//		try {
//			data.put("openid", data.getJSONObject("access_token_data").getString("openid"));
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		try {
//			data.put("username", data.get("nickname"));
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		try {
//			data.put("figureurl2", data.get("figureurl_2"));
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		try {
//			String g = data.getString("gender");
//			data.put("gender", "男".equals(g) ? 1 : ("女".equals(g) ? 2 : 0));
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//}
