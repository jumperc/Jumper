package geoai.android.util.oauth;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.api.BaiduDialog.BaiduDialogListener;
import com.baidu.api.BaiduDialogError;
import com.baidu.api.BaiduException;

public class Baidu extends OAuthBase {

	private com.baidu.api.Baidu baidu;

	public Baidu() {
		
	}

	@Override
	public String getMediaType() {
		return "Baidu";
	}

	@Override
	public String getAppKey() {
		return "YXp3u8kFgYk6ch63lo1sXoWG";
	}

	@Override
	public void doOAuth(final Activity activity) {
		baidu = new com.baidu.api.Baidu(getAppKey(), activity);
		if(baidu.isSessionValid()){
			try {
				processTokenData(activity, new JSONObject().accumulate("access_token", baidu.getAccessToken()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}
		baidu.authorize(activity, false, true, new BaiduDialogListener(){

			@Override
			public void onBaiduException(BaiduException arg0) {
				Baidu.this.onError(arg0);
			}

			@Override
			public void onCancel() {
				Baidu.this.onCancel();
			}

			@Override
			public void onComplete(Bundle bundle) {
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
			public void onError(BaiduDialogError arg0) {
				Baidu.this.onError(arg0);
			}});
	}

	@Override
	public void doUserInfo(Activity activity, JSONObject data) {
		if(baidu != null && baidu.isSessionValid()){
			try {
				String str = baidu.request(com.baidu.api.Baidu.LoggedInUser_URL, null, "GET");
				JSONObject json = new JSONObject(str);
				mergeJSONObject(data, json);
				onToken(data);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BaiduException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void translateUserInfo(JSONObject data) {
		try {
			data.put("openid", data.get("uid"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			data.put("username", data.get("uname"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			data.put("figureurl", "http://tb.himg.baidu.com/sys/portraitn/item/" + data.get("portrait"));
			data.put("figureurl2", "http://tb.himg.baidu.com/sys/portrait/item/" + data.get("portrait"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		data.remove("uid");
		data.remove("uname");
		data.remove("portrait");
	}

}
