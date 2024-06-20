package org.sswr.util.net;

import org.sswr.util.data.JSONArray;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONObject;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.textenc.FormEncoding;

public class ArcGISRESTAPI
{
	private String url;
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

	public ArcGISRESTAPI(String url)
	{
		if (url.endsWith("/"))
			this.url = url.substring(0, url.length() - 1);
		else
			this.url = url;
	}

	public String buildLoginUrl(String appId, String redirUrl)
	{
		return this.url+"/oauth2/authorize?client_id="+appId+"&response_type=token&redirect_uri="+FormEncoding.formEncode(redirUrl);
	}

	public User getUserInfo(String token, String userName)
	{
		String url = this.url+"/community/users/"+userName+"?token="+token+"&f=json";
		SharedInt statusCode = new SharedInt();
		String ret = HTTPMyClient.getAsString(url, statusCode);
		if (statusCode.value != 200)
			return null;
		JSONBase json = JSONBase.parseJSONStr(ret);
		if (json == null)
			return null;
		if (!(json instanceof JSONObject))
			return null;
		return parseUser((JSONObject)json);
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
}
