package com.qcloud.qcloudfr_android_sdk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcloud.qcloudfr_android_sdk.sign.Base64Util;

public class QcloudFrSDK {

	protected static String API_YOUTU_END_POINT = "https://youtu.api.qcloud.com/youtu/api/";  
	protected String m_appid; 
	protected String mAuthorization;

	/**
	 * QcloudFrSDK 构造方法
	 * 
	 * @param appid
	 *            授权appid
	 * @param authorization
	 *            通过appid secretId和secretKey生成的鉴权密钥 
	 */
	public QcloudFrSDK(String appid, String authorization ) {
		m_appid = appid; 
		mAuthorization = authorization;
	}

	private void GetBase64FromFile(String filePath, StringBuffer base64)
			throws IOException {
		File imageFile = new File(filePath);
		if (imageFile.exists()) {
			InputStream in = new FileInputStream(imageFile);
			byte data[] = new byte[(int) imageFile.length()]; // 创建合适文件大小的数组
			in.read(data); // 读取文件中的内容到b[]数组
			in.close();
			base64.append(Base64Util.encode(data));

		} else {
			throw new FileNotFoundException(filePath + " not exist");
		}

	}

	private JSONObject SendRequest(JSONObject postData, String mothod)
			throws IOException, JSONException { 
		System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
		System.setProperty("sun.net.client.defaultReadTimeout", "30000");
		URL url = new URL(API_YOUTU_END_POINT + mothod);
		// 设置信任服务器证书
		try {
			trustAllHttpsCertificates();
		} catch (KeyManagementException e) {
			e.printStackTrace();
			System.out.println("trust all https certificates,KeyManagementException "+e.getMessage());
		} catch (NoSuchAlgorithmException e) { 
			e.printStackTrace();
			System.out.println("trust all https certificates,NoSuchAlgorithmException "+e.getMessage());
		}
		// 设置不验证主机名
		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		HttpsURLConnection connection = (HttpsURLConnection) url
				.openConnection();

		// set header
		connection.setRequestMethod("POST");
		connection.setRequestProperty("accept", "*/*"); 
		connection.setRequestProperty("user-agent", "youtu-java-sdk");
		connection.setRequestProperty("Authorization", mAuthorization);

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Content-Type", "text/json");
		connection.connect();

		// POST请求
		DataOutputStream out = new DataOutputStream(
				connection.getOutputStream());

		postData.put("app_id", m_appid);
		String podataStr = postData.toString();
		out.writeBytes(podataStr);
		out.flush();
		out.close();

		// 读取响应
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String lines;
		StringBuffer resposeBuffer = new StringBuffer("");
		while ((lines = reader.readLine()) != null) {
			lines = new String(lines.getBytes(), "utf-8");
			resposeBuffer.append(lines);
		}
		reader.close();
		// 断开连接
		connection.disconnect();

		JSONObject respose = new JSONObject(resposeBuffer.toString());

		return respose;

	}

	/**
	 * 人脸属性分析 检测给定图片(Image)中的所有人脸(Face)的位置和相应的面部属性。位置包括(x, y, w, h)，
	 * 面部属性包括性别(gender), 年龄(age), 表情(expression), 眼镜(glass)和姿态(pitch，roll，yaw).
	 * 
	 * @param image_path
	 *            人脸图片的路径
	 * @return 请求json结果
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject DetectFace(String image_path) throws IOException,
			JSONException{

		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();

		GetBase64FromFile(image_path, image_data);
		data.put("image", image_data.toString());
		JSONObject respose = SendRequest(data, "detectface");

		return respose;
	}

	/**
	 * 五官定位
	 * 
	 * @param image_path
	 *            人脸图片的路径
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject FaceShape(String image_path) throws IOException,
			JSONException{

		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();

		GetBase64FromFile(image_path, image_data);
		data.put("image", image_data.toString());
		JSONObject respose = SendRequest(data, "faceshape");

		return respose;
	}

	/**
	 * 人脸对比， 计算两个Face的相似性以及五官相似度。
	 * 
	 * @param image_path_a
	 *            第一张人脸图片路径
	 * @param image_path_b
	 *            第二张人脸图片路径
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject FaceCompare(String image_path_a, String image_path_b)
			throws IOException, JSONException {

		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();

		GetBase64FromFile(image_path_a, image_data);
		data.put("imageA", image_data.toString());
		image_data.setLength(0);

		GetBase64FromFile(image_path_b, image_data);
		data.put("imageB", image_data.toString());
		JSONObject respose = SendRequest(data, "facecompare");

		return respose;
	}

	/**
	 * 人脸验证，给定一个Face和一个Person，返回是否是同一个人的判断以及置信度。
	 * 
	 * @param image_path
	 *            需要验证的人脸图片路径
	 * @param person_id
	 *            验证的目标person
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject FaceVerify(String image_path, String person_id)
			throws IOException, JSONException{

		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();

		GetBase64FromFile(image_path, image_data);
		data.put("image", image_data.toString());
		image_data.setLength(0);

		data.put("person_id", person_id);

		JSONObject respose = SendRequest(data, "faceverify");

		return respose;
	}

	/**
	 * 人脸识别，对于一个待识别的人脸图片，在一个Group中识别出最相似的Top5 Person作为其身份返回，返回的Top5中按照相似度从大到小排列。
	 * 
	 * @param image_path
	 *            需要识别的人脸图片路径
	 * @param group_id
	 *            人脸face组
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject FaceIdentify(String image_path, String group_id)
			throws IOException, JSONException{
		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();

		GetBase64FromFile(image_path, image_data);
		data.put("image", image_data.toString());
		image_data.setLength(0);

		data.put("group_id", group_id);

		JSONObject respose = SendRequest(data, "faceidentify");

		return respose;
	}

	/**
	 * 创建一个Person，并将Person放置到group_ids指定的组当中
	 * 
	 * @param image_path
	 *            需要新建的人脸图片路径
	 * @param person_id
	 *            指定创建的人脸
	 * @param group_ids
	 *            加入的group列表
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject NewPerson(String image_path, String person_id,
			List<String> group_ids) throws IOException, JSONException {

		return NewPerson(image_path, person_id, group_ids, "", "");
	}

	/**
	 * 创建一个Person，并将Person放置到group_ids指定的组当中
	 * 
	 * @param image_path
	 *            需要新建的人脸图片路径
	 * @param person_id
	 *            指定创建的人脸
	 * @param group_ids
	 *            加入的group列表
	 * @param personName
	 *            名字
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject NewPerson(String image_path, String person_id,
			List<String> group_ids, String personName) throws IOException,
			JSONException {

		return NewPerson(image_path, person_id, group_ids, personName, "");
	}

	/**
	 * 创建一个Person，并将Person放置到group_ids指定的组当中
	 * 
	 * @param image_path
	 *            需要新建的人脸图片路径
	 * @param person_id
	 *            指定创建的人脸
	 * @param group_ids
	 *            加入的group列表
	 * @param personName
	 *            名字
	 * @param personTag
	 *            备注
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject NewPerson(String image_path, String person_id,
			List<String> group_ids, String personName, String personTag)
			throws IOException, JSONException{
		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();
		data.put("person_id", person_id);
		if (personName != null && !personName.equals(""))
			data.put("person_name", personName);
		if (personTag != null && !personTag.equals(""))
			data.put("tag", personTag);

		if (group_ids != null && group_ids.size() > 0) {
			JSONArray array = new JSONArray();
			for (String gId : group_ids)
				array.put(gId);
			data.put("group_ids", array);
		}
		GetBase64FromFile(image_path, image_data);
		data.put("image", image_data.toString());
		image_data.setLength(0);
		JSONObject respose = SendRequest(data, "newperson");

		return respose;
	}

	/**
	 * 删除一个Person
	 * 
	 * @param person_id
	 *            要删除的person ID
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject DelPerson(String person_id) throws IOException,
			JSONException {

		JSONObject data = new JSONObject();
		data.put("person_id", person_id);
		JSONObject respose = SendRequest(data, "delperson");

		return respose;
	}

	/**
	 * 增加一个人脸Face.将一组Face加入到一个Person中。注意，一个Face只能被加入到一个Person中。
	 * 一个Person最多允许包含100个Face。
	 * 
	 * @param person_id
	 *            人脸Face的person id
	 * @param image_path_arr
	 *            人脸图片路径列表
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject AddFace(String person_id, List<String> image_path_arr)
			throws IOException, JSONException {
		StringBuffer image_data = new StringBuffer("");
		JSONObject data = new JSONObject();
		JSONArray images = new JSONArray();
		for (String image_path : image_path_arr) {
			image_data.setLength(0);
			GetBase64FromFile(image_path, image_data);
			images.put(image_data.toString());
		}

		data.put("images", images);
		image_data.setLength(0);
		data.put("person_id", person_id);
		JSONObject respose = SendRequest(data, "addface");
		return respose;
	}

	/**
	 * 删除一个person下的face，包括特征，属性和face_id.
	 * 
	 * @param person_id
	 *            待删除人脸的person ID
	 * @param face_id_arr
	 *            删除人脸id的列表
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject DelFace(String person_id, 
			List<String> face_id_arr) throws IOException, JSONException{

		JSONObject data = new JSONObject();

		if (face_id_arr != null && face_id_arr.size() > 0) {
			JSONArray array = new JSONArray();
			for (String fId : face_id_arr)
				array.put(fId);
			data.put("face_ids", array);
		}
		data.put("person_id", person_id);
		JSONObject respose = SendRequest(data, "delface");

		return respose;

	}

	/**
	 * 设置Person的name.
	 * 
	 * @param person_name
	 *            新的name
	 * @param person_id
	 *            要设置的person id
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject SetInfo(String person_name, String person_id)
			throws IOException, JSONException {
		JSONObject data = new JSONObject();

		data.put("person_name", person_name);
		data.put("person_id", person_id);
		JSONObject respose = SendRequest(data, "setinfo");

		return respose;

	}

	/**
	 * 获取一个Person的信息, 包括name, id, tag, 相关的face, 以及groups等信息。
	 * 
	 * @param person_id
	 *            待查询个体的ID
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject GetInfo(String person_id) throws IOException,
			JSONException {
		JSONObject data = new JSONObject();

		data.put("person_id", person_id);
		JSONObject respose = SendRequest(data, "getinfo");

		return respose;
	}

	/**
	 * 获取一个AppId下所有group列表
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject GetGroupIds() throws IOException, JSONException {
		JSONObject data = new JSONObject();

		JSONObject respose = SendRequest(data, "getgroupids");

		return respose;
	}

	/**
	 * 获取一个组Group中所有person列表
	 * 
	 * @param group_id
	 *            待查询的组id
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject GetPersonIds(String group_id) throws IOException,
			JSONException {
		JSONObject data = new JSONObject();

		data.put("group_id", group_id);
		JSONObject respose = SendRequest(data, "getpersonids");

		return respose;
	}

	/**
	 * 获取一个组person中所有face列表
	 * 
	 * @param person_id
	 *            待查询的个体id
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject GetFaceIds(String person_id) throws IOException,
			JSONException {
		JSONObject data = new JSONObject();

		data.put("person_id", person_id);
		JSONObject respose = SendRequest(data, "getfaceids");

		return respose;
	}

	/**
	 * 获取一个face的相关特征信息
	 * 
	 * @param face_id
	 *            带查询的人脸ID
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject GetFaceInfo(String face_id) throws IOException,
			JSONException {
		JSONObject data = new JSONObject();

		data.put("face_id", face_id);
		JSONObject respose = SendRequest(data, "getfaceinfo");

		return respose;
	}

	HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String urlHostName, SSLSession session) {
			return true;
		}
	};

	private void trustAllHttpsCertificates() throws NoSuchAlgorithmException, KeyManagementException {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc;
		sc = javax.net.ssl.SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}

	class miTM implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

}