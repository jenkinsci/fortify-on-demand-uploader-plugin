package org.jenkinsci.plugins.fodupload;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.FilePath;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;

import net.sf.json.JSONObject;
import org.apache.commons.lang3.EnumUtils;
import org.apache.http.HttpStatus;
import org.jenkinsci.plugins.fodupload.FodApi.ResponseContent;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.IFodEnum;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class Utils {

    private static final String TS_DOT_NET_KEY = ".NET";
    private static final String TS_JAVA_KEY = "JAVA/J2EE";
    private static final String TS_RUBY_KEY = "Ruby";
    private static final String TS_PYTHON_KEY = "Python";
    private static final String TS_OBJECTIVE_C_KEY = "Objective-C";
    private static final String TS_ABAP_KEY = "ABAP";
    private static final String TS_ASP_KEY = "ASP";
    private static final String TS_CFML_KEY = "CFML";
    private static final String TS_COBOL_KEY = "COBOL";
    private static final String TS_ANDROID_KEY = "Android";
    private static final String TS_PHP_KEY = "PHP";
    private static final String TS_PLSQL_TSQL_KEY = "PL/SQL & T-SQL";
    private static final String TS_VB6_KEY = "VB6";
    private static final String TS_VB_SCRIPT_KEY = "VBScript";
    private static final String TS_XML_HTML_KEY = "XML/HTML";

     /*
        Maura E. Ardden: 09/15/2022
        static initializers to support URL validation error messages

    */

    protected static final String FOD_URL_ERROR_MESSAGE = "FOD_URL_ERROR_MESSAGE: \n\n" +
            "The Fortify On Demand url is not valid. \n" +
            "Make sure to test the URLs under System Configuration --> Fortify On Demand. \n" +
            "To do so, use the 'Test Connection' button";
    protected static final String FOD_BASEURL_ERROR_MESSAGE = "FOD_BASEURL_ERROR_MESSAGE: \n\n" +
            "The Fortify On Demand url is not valid. \n" +
            "Click the help icon (?) for details";

    protected static final String FOD_APIURL_ERROR_MESSAGE = "FOD_APIURL_ERROR_MESSAGE: \n\n" +
            "The Fortify On Demand API url is not valid. \n" +
            "Click the help icon (?) for details";

    public static int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static Integer tryParseInt(String value, Integer defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    /*
        Maura E. Ardden: 09/15/2022

        URL validation routine using a regex
        Scope is limited (not all possible use cases covered).
    */

    public static String isValidUrl(String url) {
        if (!url.matches("^https?:\\/\\/([^\\. :;\\?\\+&\\/])+(\\S[a-zA-Z0-9:.\\-\\_]+)[^\\. :;\\?\\+&\\/]+$")) {
            return null;
        }
        return url;
    }

    public static String getFileExpressionPatternString(String technologyStack) {
        String constantFiles = "|.*\\.html|.*\\.htm|.*\\.js|.*\\.xml|.*\\.xsd|.*\\.xmi|.*\\.wsdd|.*\\.config" +
                "|.*\\.settings|.*\\.cpx|.*\\.xcfg|.*\\.cscfg|.*\\.cscdef|.*\\.wadcfg|.*\\.appxmanifest"
                + "|.*\\.wsdl|.*\\.plist|.*\\.properties|.*\\.ini|.*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb"
                + "|.*\\.asl|.*\\.conf|.*\\.inc|.*\\.json|.*\\.jsx|.*\\.phtml|.*\\.tldj|.*\\.ts|.*\\.tsx|.*\\.xaml|.*\\.xhtml|.*\\.yaml|.*\\.yml";


        switch (technologyStack) {
            case TS_DOT_NET_KEY:
                return ".*\\.dll|.*\\.pdb|.*\\.cs|.*\\.aspx|.*\\.asp|.*\\.vb|.*\\.vbproj|.*\\.csproj|.*\\.sln|.*\\.cshtml|.*\\.vbhtml" + constantFiles;
            case TS_JAVA_KEY:
                return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.kt|.*\\.kts|.*\\.tld" +
                        "|.*\\.jspx|.*\\.xhtml|.*\\.faces|.*\\.jsff|.*\\.properties" + constantFiles;
            case TS_PYTHON_KEY:
                return ".*\\.py" + constantFiles;
            case TS_RUBY_KEY:
                return ".*\\.rb|.*\\.erb" + constantFiles;
            case TS_ASP_KEY:
                return ".*\\.asp" + constantFiles;
            case TS_PHP_KEY:
                return ".*\\.php" + constantFiles;
            case TS_VB6_KEY:
                return ".*\\.vbs|.*\\.bas|.*\\.frm|.*\\.ctl|.*\\.cls" + constantFiles;
            case TS_VB_SCRIPT_KEY:
                return ".*\\.vbscript" + constantFiles;
            case TS_ANDROID_KEY:
                // APK is not normally used for Static analysis but we are collecting in the event it is useful
                return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.tld" +
                        "|.*\\.jspx|.*\\.xhtml|.*\\.faces|.*\\.jsff|.*\\.properties|.*\\.apk" + constantFiles;
            case TS_XML_HTML_KEY:
                return ".*\\.xml|.*\\.xsd|.*\\.xmi|.*\\.wsdd|.*\\.config|.*\\.cpx|.*\\.xcfg" + constantFiles;
            case TS_PLSQL_TSQL_KEY:
                return ".*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb" + constantFiles;
            case TS_ABAP_KEY:
                return ".*\\.abap" + constantFiles;
            case TS_CFML_KEY:
                return ".*\\.cfm|.*\\.cfml|.*\\.cfc" + constantFiles;
            default:
                return ".*";
        }
    }

    /**
     * Zips a folder, stores it in a temp location and returns the object
     *
     * @param techStack technology stack of the folder to zip
     * @param payload   location of the files to zip
     * @param logger    logger to write status text to
     *                  at_return a File object
     * @throws IOException no files
     */
    public static File createZipFile(String techStack, FilePath payload, PrintStream logger) throws IOException {
        logger.println("Begin Create Zip.");
        logger.println("Source file directory: " + payload);
        File tempZip = File.createTempFile("fodupload", ".zip");

        try (FileOutputStream fos = new FileOutputStream(tempZip)) {
            final Pattern pattern = Pattern.compile(Utils.getFileExpressionPatternString(techStack),
                    Pattern.CASE_INSENSITIVE);
            // Is this local when in remote
            payload.zip(fos, new RegexFileFilter(pattern));
            logger.println("Temporary file created at: " + tempZip.getAbsolutePath());
        } catch (Exception e) {
            logger.println(e.getMessage());
        }

        logger.println("End Create Zip.");
        return tempZip;
    }

    public static String decrypt(String stringToDecrypt) {
        Secret decryptedSecret = Secret.decrypt(stringToDecrypt);
        return decryptedSecret != null ? decryptedSecret.getPlainText() : stringToDecrypt;
    }

    public static String decrypt(Secret stringToDecrypt) {
        return stringToDecrypt.getPlainText();
    }

    public static String encrypt(String stringToEncrypt) {
        String result = stringToEncrypt;
        if (Secret.decrypt(stringToEncrypt) == null) {
            result = Secret.fromString(stringToEncrypt).getEncryptedValue();
        }
        return result;
    }

    public static boolean isEncrypted(String stringToEncrypt) {
        return (Secret.decrypt(stringToEncrypt) != null);
    }

    public static boolean isCredential(String id) {
        StringCredentials s = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StringCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        null,
                        null
                ),
                CredentialsMatchers.allOf(
                        CredentialsMatchers.withId(id)
                )
        );
        return (s != null);
    }

    public static String retrieveSecretDecryptedValue(String id) {
        StringCredentials s = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StringCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        null,
                        null
                ),
                CredentialsMatchers.allOf(
                        CredentialsMatchers.withId(id)
                )
        );
        return s != null ? decrypt(s.getSecret()) : id;
    }

    public static Boolean isUnauthorizedResponse(ResponseContent response) {
        return response.code() == HttpStatus.SC_FORBIDDEN || response.code() == HttpStatus.SC_UNAUTHORIZED;
    }

    public static <T> String createResponseViewModel(T response) {
        Gson gson = new Gson();
        Type typeOfSrc = new TypeToken<T>() {
        }.getType();
        return gson.toJson(response, typeOfSrc);
    }

    public static String createCorrelationId() {
        return UUID.randomUUID().toString();
    }

    private static List<String> unexpectedErrorResponseErrors;

    static {
        unexpectedErrorResponseErrors = new ArrayList<>();
        unexpectedErrorResponseErrors.add("Unexpected response from server");
    }

    public static List<String> unexpectedServerResponseErrors() {
        return new ArrayList<>(unexpectedErrorResponseErrors);
    }

    private static List<String> unauthorizedErrorResponseErrors;

    static {
        unauthorizedErrorResponseErrors = new ArrayList<>();
        unauthorizedErrorResponseErrors.add("Not authorized to perform this action");
    }

    public static List<String> unauthorizedServerResponseErrors() {
        return new ArrayList<>(unauthorizedErrorResponseErrors);
    }

    public static AuthenticationModel getAuthModelFromObject(JSONObject authModelObject) {
        AuthenticationModel authModel = new AuthenticationModel(false, null, null, null);
        if (authModelObject.getBoolean("overrideGlobalAuth")) {
            authModel = AuthenticationModel.fromPersonalAccessToken(
                    authModelObject.getString("username"),
                    authModelObject.getString("accessTokenKey"),
                    authModelObject.getString("tenantId"));
        }
        return authModel;
    }

    public static <E extends Enum<E> & IFodEnum> Boolean isValidEnumValue(Class<E> enumClass, String value) {
        if (isNullOrEmpty(value)) return false;
        List<E> enums = EnumUtils.getEnumList(enumClass);
        Integer valFromInt = tryParseInt(value, null);

        if (valFromInt != null) {
            for (IFodEnum e : enums) {
                if (e.getIntValue().equals(valFromInt)) return true;
            }
        } else {
            for (IFodEnum e : enums) {
                if (e.getStringValue().equals(value)) return true;
            }
        }

        return false;
    }

    public static String getLogTimestampFormat() {
        return "yyyy-MM-dd HH:mm:ss.SSS";
    }

    private final static boolean _traceLogging = System.getenv("FOD_TRACE") != null && System.getenv("FOD_TRACE").equals("true");

    public static boolean traceLogging() {
        return _traceLogging;
    }
}
