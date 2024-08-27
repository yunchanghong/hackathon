package com.rakuten.hackathon.data.feed.constants;

public class Const {

    private Const() {
        // hide bean creation on static class
        super();
    }

    public static final String CLASSPATH = "classpath:";

    public static final String FIELD_SPLITTER = ",";

    public static final String UNKNOWN_PREFIX = "[Unknown]";

    public static final String DEFAULT_CREATOR = "DMP";

    public static final String DEFAULT_OWNER = "prj-dmp-all@mail.rakuten.com";

    public static final String S2S_SYNC_TYPE = "S2S Sync";
    
    public static final String DELIMITER = "::";

    public enum FacebookKeys {

        APP_ID("appID"),
        APP_SECRET("appSecret"),
        ACCESS_TOKEN("accessToken"),
        DEFAULT_VALUE("defaultValue"),
        THIRD_PARTY("thirdParty");

        private final String keyName;

        FacebookKeys(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }

    public enum TwitterKeys {

        API_KEY("apiKey"),
        ACCOUNT_ID("accountId"),
        THIRD_PARTY("thirdParty"),
        ACCESS_TOKEN("accessToken"),
        API_SECRET_KEY("apiSecretKey"),
        ACCESS_TOKEN_SECRET("accessTokenSecret");

        private final String keyName;

        TwitterKeys(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }

    public enum SmartNewsKeys {

        ACCOUNT_ID("accountId"),
        THIRD_PARTY("thirdParty"),
        AUTHENTICATION_TOKEN("authenticationToken");

        private final String keyName;

        SmartNewsKeys(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }
    
    public enum RunaKeys {

        APP_KEY("apiKey"),
        APP_SECRET("apiSecret"),
        ACCOUNT_ID("accountId"),
        DEFAULT_VALUE("defaultValue"),
        THIRD_PARTY("thirdParty"),
        ID("id"),
        AUDIENCE_FOLDER_ID("audienceFolderId"),
        AUDIENCE_FOLDER_KEY("audienceFolderKey"),
        AUDIENCE_FOLDER_NAME("audienceFolderName"),
        AUDIENCE_FOLDER_HIDDEN("audienceFolderHidden");

        private final String keyName;

        RunaKeys(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }
}
