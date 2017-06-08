
package sina.weibo.sdk.openapi;

import org.json.JSONObject;

/**
 * 微博可见性结构体。
 * 
 * @author SINA
 * @since 2013-11-24
 */
public class Visible {
    
    public static final int VISIBLE_NORMAL  = 0;
    public static final int VISIBLE_PRIVACY = 1;
    public static final int VISIBLE_GROUPED = 2;
    public static final int VISIBLE_FRIEND  = 3;
    
    /** type 取值，0：普通微博，1：私密微博，3：指定分组微博，4：密友微博 */
    public int type;
    /** 分组的组号 */
    public int list_id;
    
    public static Visible parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        
        Visible visible = new Visible();
        visible.type    = jsonObject.optInt("type", 0);
        visible.list_id = jsonObject.optInt("list_id", 0);
        
        return visible;
    }
}