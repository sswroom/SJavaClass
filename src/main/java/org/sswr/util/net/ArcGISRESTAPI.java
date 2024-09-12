package org.sswr.util.net;

import java.sql.Timestamp;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.JSONArray;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONObject;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.textenc.FormEncoding;

public class ArcGISRESTAPI
{
	private String url;
	private SocketFactory sockf;
//	private String appSecret;

	public static class Group
	{
		public String id;
		public String title;
		public boolean isInvitationOnly;
		public String owner;
		public String description;
		public String snippet;
		public String[] tags;
		public String[] typeKeywords;
		public String phone;
		public String sortField;
		public String sortOrder;
		public boolean isViewOnly;
		public String featuredItemsId;
		public String thumbnail;
		public long created;
		public long modified;
		public String access;
		public String[] capabilites;
		public boolean isFav;
		public boolean isReadOnly;
		public boolean _protected;
		public boolean autoJoin;
		public boolean notificationsEnabled;
		public String provider;
		public String providerGroupName;
		public boolean leavingDisallowed;
		public boolean hiddenMembers;
		public String membershipAccess;
	}

	public static class User
	{
		public String username;
		public String udn;
		public String id;
		public String fullName;
		public String[] categories;
		public String emailStatus;
		public String firstName;
		public String lastName;
		public String preferredView;
		public String description;
		public String email;
		public String userType;
		public String idpUsername;
		public String favGroupId;
		public long lastLogin;
		public boolean mfaEnabled;
		public long storageUsage;
		public long storageQuota;
		public String orgId;
		public String role;
		public String[] privileges;
		public String[] adminCategories;
		public String level;
		public String userLicenseTypeId;
		public boolean disabled;
		public String[] tags;
		public String culture;
		public String cultureFormat;
		public String region;
		public String units;
		public String thumbnail;
		public String access;
		public long created;
		public long modified;
		public String provider;
		public Group[] groups;
	}

	public static class SimpleUser
	{
		public String username;
		public String fullName;
		public String memberType;
		public String thumbnail;
		public long joined;
	}

	public static class PageResult<T>
	{
		public int total;
		public int start;
		public int num;
		public int nextStart;
		public T[] results;
	}

	public static class TokenResult
	{
		public String accessToken;
		public int expiresIn;
		public Timestamp expireTime;
	}

	public ArcGISRESTAPI(String url)
	{
		if (url.endsWith("/"))
			this.url = url.substring(0, url.length() - 1);
		else
			this.url = url;
		this.sockf = null;
	}

	public void setSocketFactory(SocketFactory sockf)
	{
		this.sockf = sockf;
	}

	public String buildLoginUrl(String appId, String redirUrl)
	{
		return this.url+"/oauth2/authorize?client_id="+appId+"&response_type=token&redirect_uri="+FormEncoding.formEncode(redirUrl);
	}

	public TokenResult getClientToken(String clientId, String clientSecret)
	{
		String url = this.url+"/oauth2/token?grant_type=client_credentials&client_id="+clientId+"&client_secret="+clientSecret+"&f=json";
		SharedInt statusCode = new SharedInt();
		String ret = HTTPOSClient.getAsString(sockf, url, statusCode);
		if (statusCode.value != 200)
			return null;
		JSONBase json = JSONBase.parseJSONStr(ret);
		if (json == null)
			return null;
		if (!(json instanceof JSONObject))
			return null;
		TokenResult token = new TokenResult();
		token.accessToken = json.getValueString("access_token");
		token.expiresIn = json.getValueAsInt32("expires_in");
		token.expireTime = DateTimeUtil.addSecond(DateTimeUtil.timestampNow(), token.expiresIn);
		if (token.accessToken != null)
			return token;
		return null;
	}

	public User getUserInfo(String token, String userName)
	{
		String url = this.url+"/community/users/"+userName+"?token="+token+"&f=json";
		SharedInt statusCode = new SharedInt();
		String ret = HTTPOSClient.getAsString(sockf, url, statusCode);
		if (statusCode.value != 200)
			return null;
		JSONBase json = JSONBase.parseJSONStr(ret);
		if (json == null)
			return null;
		if (!(json instanceof JSONObject))
			return null;
		return parseUser((JSONObject)json);
	}

/*	public PageResult<User> queryUsersByGroupId(String token, String groupId, Integer start, Integer num)
	{
		String url = this.url+"/community/users?filter="+FormEncoding.formEncode("group:\""+groupId+"\"")+"&token="+token+"&f=json";
		if (start != null)
		{
			url += "&start="+start.intValue();
		}
		if (num != null)
		{
			url += "&num="+num.intValue();
		}
		SharedInt statusCode = new SharedInt();
		String ret = HTTPOSClient.getAsString(sockf, url, statusCode);
		if (statusCode.value != 200)
			return null;
		JSONBase json = JSONBase.parseJSONStr(ret);
		if (json == null)
			return null;
		if (!(json instanceof JSONObject))
			return null;
		PageResult<User> res = parsePageResult((JSONObject)json);
		if (res == null)
			return null;
		res.results = parseUserArray(json.getValueArray("results"));
		return res;
	}*/

	public PageResult<Group> queryGroups(String token, String name, Integer start, Integer num)
	{
		String url = this.url+"/community/groups?token="+token;
		if (name != null)
		{
			url += "&q="+FormEncoding.formEncode(name);
		}
		if (start != null)
		{
			url += "&start="+start.intValue();
		}
		if (num != null)
		{
			url += "&num="+num.intValue();
		}
		url += "&f=json";
		SharedInt statusCode = new SharedInt();
		String ret = HTTPOSClient.getAsString(sockf, url, statusCode);
		if (statusCode.value != 200)
			return null;
		JSONBase json = JSONBase.parseJSONStr(ret);
		if (json == null)
			return null;
		if (!(json instanceof JSONObject))
			return null;
		PageResult<Group> result = parsePageResult((JSONObject)json);
		if (result != null)
		{
			result.results = parseGroupArray(json.getValueArray("results"));
		}
		return result;
	}

	public PageResult<SimpleUser> getGroupUserList(String token, String groupId, Integer start, Integer num)
	{
		String url = this.url+"/community/groups/"+groupId+"/userList?token="+token;
		if (start != null)
		{
			url += "&start="+start.intValue();
		}
		if (num != null)
		{
			url += "&num="+num.intValue();
		}
		url += "&f=json";
		SharedInt statusCode = new SharedInt();
		String ret = HTTPOSClient.getAsString(sockf, url, statusCode);
		if (statusCode.value != 200)
			return null;
		JSONBase json = JSONBase.parseJSONStr(ret);
		if (json == null)
			return null;
		if (!(json instanceof JSONObject))
			return null;
		PageResult<SimpleUser> result = parsePageResult((JSONObject)json);
		if (result != null)
		{
			result.results = parseSimpleUserArray(json.getValueArray("users"));
		}
		return result;
	}

	private static User parseUser(JSONObject obj)
	{
		if (obj == null)
			return null;
		User user = new User();
		user.username = obj.getValueString("username");
		user.udn = obj.getValueString("udn");
		user.id = obj.getValueString("id");
		user.fullName = obj.getValueString("fullName");
		user.categories = parseStrArr(obj.getObjectArray("categories"));
		user.emailStatus = obj.getValueString("emailStatus");
		user.firstName = obj.getValueString("firstName");
		user.lastName = obj.getValueString("lastName");
		user.preferredView = obj.getValueString("preferredView");
		user.description = obj.getValueString("description");
		user.email = obj.getValueString("email");
		user.userType = obj.getValueString("userType");
		user.idpUsername = obj.getValueString("idpUsername");
		user.favGroupId = obj.getValueString("favGroupId");
		user.lastLogin = obj.getValueAsInt64("lastLogin");
		user.mfaEnabled = obj.getValueAsBool("mfaEnabled");
		user.storageUsage = obj.getValueAsInt64("storageUsage");
		user.storageQuota = obj.getValueAsInt64("storageQuota");
		user.orgId = obj.getValueString("orgId");
		user.role = obj.getValueString("role");
		user.privileges = parseStrArr(obj.getObjectArray("privileges"));
		user.adminCategories = parseStrArr(obj.getObjectArray("adminCategories"));
		user.level = obj.getValueString("level");
		user.userLicenseTypeId = obj.getValueString("userLicenseTypeId");
		user.disabled = obj.getValueAsBool("disabled");
		user.tags = parseStrArr(obj.getObjectArray("tags"));
		user.culture = obj.getValueString("culture");
		user.cultureFormat = obj.getValueString("cultureFormat");
		user.region = obj.getValueString("region");
		user.units = obj.getValueString("units");
		user.thumbnail = obj.getValueString("thumbnail");
		user.access = obj.getValueString("access");
		user.created = obj.getValueAsInt64("created");
		user.modified = obj.getValueAsInt64("modified");
		user.provider = obj.getValueString("provider");
		JSONArray arr = obj.getObjectArray("groups");
		if (arr != null)
		{
			int i = 0;
			int j = arr.getArrayLength(); 
			user.groups = new Group[j];
			while (i < j)
			{
				user.groups[i] = parseGroup(arr.getArrayObject(i));
				i++;
			}
		}
		return user;
	}

	private static Group parseGroup(JSONObject obj)
	{
		if (obj == null)
			return null;
		Group group = new Group();
		group.id = obj.getValueString("id");
		group.title = obj.getValueString("title");
		group.isInvitationOnly = obj.getValueAsBool("isInvitationOnly");
		group.owner = obj.getValueString("owner");
		group.description = obj.getValueString("description");
		group.snippet = obj.getValueString("snippet");
		group.tags = parseStrArr(obj.getObjectArray("tags"));
		group.typeKeywords = parseStrArr(obj.getObjectArray("typeKeywords"));
		group.phone = obj.getValueString("phone");
		group.sortField = obj.getValueString("sortField");
		group.sortOrder = obj.getValueString("sortOrder");
		group.isViewOnly = obj.getValueAsBool("isViewOnly");
		group.featuredItemsId = obj.getValueString("featuredItemsId");
		group.thumbnail = obj.getValueString("thumbnail");
		group.created = obj.getValueAsInt64("created");
		group.modified = obj.getValueAsInt64("modified");
		group.access = obj.getValueString("access");
		group.capabilites = parseStrArr(obj.getObjectArray("capabilites"));
		group.isFav = obj.getValueAsBool("isFav");
		group.isReadOnly = obj.getValueAsBool("isReadOnly");
		group._protected = obj.getValueAsBool("protected");
		group.autoJoin = obj.getValueAsBool("autoJoin");
		group.notificationsEnabled = obj.getValueAsBool("notificationsEnabled");
		group.provider = obj.getValueString("provider");
		group.providerGroupName = obj.getValueString("providerGroupName");
		group.leavingDisallowed = obj.getValueAsBool("leavingDisallowed");
		group.hiddenMembers = obj.getValueAsBool("hiddenMembers");
		group.membershipAccess = obj.getValueString("membershipAccess");
		return group;
	}

	private static SimpleUser parseSimpleUser(JSONObject obj)
	{
		if (obj == null)
			return null;
		SimpleUser user = new SimpleUser();
		user.username = obj.getValueString("username");
		user.fullName = obj.getValueString("fullName");
		user.memberType = obj.getValueString("memberType");
		user.thumbnail = obj.getValueString("thumbnail");
		user.joined = obj.getValueAsInt64("joined");
		return user;
	}

	private static String[] parseStrArr(JSONArray arr)
	{
		if (arr == null)
			return null;
		int i = 0;
		int j = arr.getArrayLength();
		String[] retList = new String[arr.getArrayLength()];
		while (i < j)
		{
			retList[i] = arr.getArrayString(i);
			i++;
		}
		return retList;
	}

	private static <T> PageResult<T> parsePageResult(JSONObject obj)
	{
		if (obj == null)
			return null;
		PageResult<T> res = new PageResult<T>();
		res.total = obj.getValueAsInt32("total");
		res.start = obj.getValueAsInt32("start");
		res.num = obj.getValueAsInt32("num");
		res.nextStart = obj.getValueAsInt32("nextStart");
		return res;
	}

	private static Group[] parseGroupArray(JSONArray arr)
	{
		if (arr == null)
			return null;
		int i = 0;
		int j = arr.getArrayLength();
		Group[] retList = new Group[arr.getArrayLength()];
		while (i < j)
		{
			retList[i] = parseGroup(arr.getArrayObject(i));
			i++;
		}
		return retList;
	}

	private static SimpleUser[] parseSimpleUserArray(JSONArray arr)
	{
		if (arr == null)
			return null;
		int i = 0;
		int j = arr.getArrayLength();
		SimpleUser[] retList = new SimpleUser[arr.getArrayLength()];
		while (i < j)
		{
			retList[i] = parseSimpleUser(arr.getArrayObject(i));
			i++;
		}
		return retList;
	}

/* 	private static User[] parseUserArray(JSONArray arr)
	{
		if (arr == null)
			return null;
		int i = 0;
		int j = arr.getArrayLength();
		User[] retList = new User[arr.getArrayLength()];
		while (i < j)
		{
			retList[i] = parseUser(arr.getArrayObject(i));
			i++;
		}
		return retList;
	}*/
}
