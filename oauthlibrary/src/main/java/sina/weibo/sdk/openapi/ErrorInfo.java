package sina.weibo.sdk.openapi;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

/**
 * 错误信息结构体。
 * 
 * @author SINA
 * @since 2013-11-24
 */
public class ErrorInfo {
    public String error;
    public String error_code;
    public String request;

    public static ErrorInfo parse(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        ErrorInfo errorInfo = new ErrorInfo();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            errorInfo.error      = jsonObject.optString("error");
            errorInfo.error_code = jsonObject.optString("error_code");
            errorInfo.request    = jsonObject.optString("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return errorInfo;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "error: " + error + 
               ", error_code: " + error_code + 
               ", request: " + request;
    }
}
