package geoai.android.util.oauth;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

import geoai.android.util.ActivityResultUtil;
import sina.weibo.sdk.openapi.UsersAPI;

public class SinaWeibo extends OAuthBase {
	private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";// 应用的回调页
	private static final String SCOPE = // 应用申请的高级权限
	"email,direct_messages_read,direct_messages_write,"
			+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
			+ "follow_app_official_microblog," + "invitation_write";

	public SinaWeibo() {
	}

	@Override
	public String getMediaType() {
		return "SinaWeibo";
	}

	@Override
	public String getAppKey() {
		return "3429720671";
	}

	Oauth2AccessToken accessToken;
	@Override
	public void doOAuth(final Activity activity) {
		final SsoHandler mSsoHandler = new SsoHandler(activity, new AuthInfo(activity, getAppKey(), REDIRECT_URL, SCOPE));
		int requestCode = ActivityResultUtil.getInstance().getNewRquestCode(new ActivityResultUtil.OnActivityResultListener() {
			@Override
			public void onActivityResult(int requestCode, int resultCode, Intent data) {
				mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
			}
		});
		
		mSsoHandler.authorize(new WeiboAuthListener(){
			@Override
			public void onCancel() {
				SinaWeibo.this.onCancel();
			}

			@Override
			public void onComplete(Bundle bundle) {
				accessToken = Oauth2AccessToken.parseAccessToken(bundle); 
				JSONObject tokenData = new JSONObject();
				for(String key : bundle.keySet()){
					try {
						tokenData.put(key, bundle.get(key));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				processTokenData(activity, tokenData);
			}

			@Override
			public void onWeiboException(WeiboException e) {
				onError(e);
			}});
	}

	@Override
	public void doUserInfo(Activity activity, final JSONObject data) {
		UsersAPI api = new UsersAPI(activity, null, accessToken);
		try {
			api.show(Long.parseLong(accessToken.getUid()), new RequestListener() {
				
				@Override
				public void onWeiboException(WeiboException arg0) {
					onError(arg0);
				}
				
				@Override
				public void onComplete(String arg0) {
					try {
						mergeJSONObject(data, new JSONObject(arg0));
						onToken(data);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void translateUserInfo(JSONObject data) {
		try {
			data.put("username", data.get("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			data.put("openid", ((JSONObject)data.get("access_token_data")).get("uid"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			String g = data.getString("gender");
			data.put("gender", "m".equals(g)? 1 : ("m".equals("f") ? 2 : 0));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			data.put("figureurl", data.get("profile_image_url"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			data.put("figureurl2", data.get("avatar_large"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
