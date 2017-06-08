package geoai.android.util.oauth;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class QQ2 extends OAuthWebkitBase {

	public QQ2() {
	}

	@Override
	protected String getAuthorizeURL() {
		return "https://graph.qq.com/oauth2.0/authorize";
	}

	@Override
	protected void modifyLoginParam(Map<String, Object> data) {
		data.put("display", "mobile");
	}

	@Override
	protected String getInfoURL() {
		return "https://graph.qq.com/oauth2.0/me";
	}
	private static final String infoURL2 = "https://graph.qq.com/user/get_user_info";
	
	@Override
	protected String getUrParamslPart(String url) {
		int pos = url.indexOf('#');
		if(pos >= 0){
			return url.substring(pos + 1);
		}
		return null;
	}

	@Override
	public String getMediaType() {
		return "QQ";
	}

	@Override
	public String getAppKey() {
		return "101070838";
	}

	@Override
	protected void translateUserInfo(JSONObject data) throws JSONException {
		String access_token = data.getString("access_token");
		String open_id = data.getString("openid");
		JSONObject params = new JSONObject().accumulate("access_token", access_token).accumulate("openid", open_id).accumulate("oauth_consumer_key", getAppKey());
		mergeUrlJSON(activity, data, infoURL2, params);
		data.put("username", data.optString("nickname"));
		data.put("figureurl2", data.optString("figureurl_2"));
		String g = data.optString("gender");
		data.put("gender", "男".equals(g) ? 1 : ("女".equals(g) ? 2 : 0));
	}

}
