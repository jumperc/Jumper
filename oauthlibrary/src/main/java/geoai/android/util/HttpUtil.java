package geoai.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.channels.ClosedByInterruptException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;

public abstract class HttpUtil {
	private static final class JSONResponseHandler implements
			ResponseHandler<JSONObject> {
		@Override
		public JSONObject handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			HttpEntity entity = response.getEntity();
			switch (response.getStatusLine().getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				InputStream stream = entity.getContent();
				if (stream != null) {
					try {
						final Header encoding = entity.getContentEncoding();
						if (encoding != null)
							System.out.println("encoding: "
									+ encoding.getValue());
						if (encoding != null
								&& "gzip".equalsIgnoreCase(encoding.getValue())) {
							stream = new GZIPInputStream(stream);
						}
						return handleStream(stream,
								(int) entity.getContentLength());
					} finally {
						stream.close();
					}
				} else {
					throw new NetworkException("Can't get content stream.");
				}
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				throw new UnauthorizedNetworkException();
			}

			return null;
		}

		private JSONObject handleStream(InputStream inputStream,
				int contentLength) throws IOException {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"));
				StringBuilder target = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (Thread.currentThread().isInterrupted()) {
						throw new ClosedByInterruptException();
					}
					target.append(line).append("\n");
				}
				String str = target.toString().trim();
				if (str.startsWith("callback")) {
					int start = str.indexOf('(');
					int stop = str.lastIndexOf(')');
					if (start > 0 && stop > 0) {
						str = str.substring(start + 1, stop);
					}
				}
				return new JSONObject(str);
			} catch (ClosedByInterruptException e) {
				e.printStackTrace();
				throw new CanceledNetworkException();
			} catch (JSONException e) {
				e.printStackTrace();
				throw new IOException("jsonException: " + e.getMessage());
			}
		}
	}

	public static class NetworkException extends IOException {
		private static final long serialVersionUID = 1L;

		public NetworkException(String msg) {
			super(msg);
		}
	}

	public static class CanceledNetworkException extends NetworkException {
		private static final long serialVersionUID = 1L;

		public CanceledNetworkException() {
			super("Canceled");
		}

		public CanceledNetworkException(String msg) {
			super(msg);
		}

	}

	public static class UnauthorizedNetworkException extends NetworkException {
		private static final long serialVersionUID = 1L;

		public UnauthorizedNetworkException(String msg) {
			super(msg);
		}

		public UnauthorizedNetworkException() {
			super("HTTP_UNAUTHORIZED");
		}
	}

	private static final String DEFAULT_USER_AGENT = "AndroidHttpClient";

	/**
	 * 获取url中的JSON数据，使用HTTP GET
	 * @param context
	 * @param url	待获取的url
	 * @return
	 * @throws NetworkException
	 */
	public static JSONObject httpGetJSON(Context context, String url)
			throws NetworkException {
		return httpGetJSON(context, url, DEFAULT_USER_AGENT);
	}

	/**
	 * 获取url中的JSON数据，使用HTTP GET
	 * @param context
	 * @param url	待获取的url
	 * @param userAgent	to report in your HTTP requests
	 * @return
	 * @throws NetworkException
	 */
	public static JSONObject httpGetJSON(Context context, String url,
			String userAgent) throws NetworkException {
		HttpClient httpClient = AndroidHttpClient.newInstance(userAgent,
				context);
		HttpGet req = new HttpGet(url);
		JSONObject ret = null;
		try {
			ret = httpClient.execute(req, new JSONResponseHandler());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 获取url中的JSON数据，使用HTTP PUT
	 * @param activity
	 * @param url	待获取的url
	 * @param params	put的参数
	 * @return
	 * @throws NetworkException
	 */
	public static JSONObject httpPutJSON(Activity activity, String url,
			Object params) throws NetworkException {
		return httpPutJSON(activity, url, params, DEFAULT_USER_AGENT);
	}

	/**
	 * 获取url中的JSON数据，使用HTTP PUT
	 * @param activity
	 * @param url	待获取的url
	 * @param params	put的参数
	 * @param userAgent	to report in your HTTP requests
	 * @return
	 * @throws NetworkException
	 */
	public static JSONObject httpPutJSON(Activity activity, String url,
			Object params, String userAgent) throws NetworkException {
		HttpClient httpClient = AndroidHttpClient.newInstance(userAgent,
				activity);
		HttpPost req = new HttpPost(url);
		HttpParams httpParams = new BasicHttpParams();
		if (params instanceof String) {
			// TODO: String to HttpParams is not implements
		} else if (params instanceof JSONObject) {
			JSONObject arr = (JSONObject) params;
			@SuppressWarnings("rawtypes")
			Iterator it = arr.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				httpParams.setParameter(key, arr.opt(key));
			}
		} else if (params instanceof Map) {
			@SuppressWarnings({ "rawtypes" })
			Map arr = (Map) params;
			if (arr != null) {
				for (Object key : arr.keySet()) {
					httpParams.setParameter((String) key, arr.get(key));
				}
			}
		}
		req.setParams(httpParams);
		JSONObject ret = null;
		try {
			ret = httpClient.execute(req, new JSONResponseHandler());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

}
