package org.jenkinsci.plugins.fodupload;

import hudson.FilePath;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;

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
    private static final String TS_JS_TS_HTML = "JS/TS/HTML";

    public static int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String getFileExpressionPatternString(String technologyStack) {
        String constantFiles = "|.*\\.html|.*\\.htm|.*\\.js|.*\\.jsx|.*\\*.ts|.*\\*.tsx|.*\\.xml|.*\\.xsd|.*\\.xmi|.*" +
                "\\.wsdd|.*\\.config|.*\\.settings|.*\\.cpx|.*\\.xcfg|.*\\.cscfg|.*\\.cscdef|.*\\.wadcfg|.*" +
                "\\.appxmanifest|.*\\.wsdl|.*\\.plist|.*\\.properties|.*\\.ini|.*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb";

        switch (technologyStack) {
            case TS_DOT_NET_KEY:
                return ".*\\.dll|.*\\.pdb|.*\\.cs|.*\\.aspx|.*\\.asp|.*\\.vb|.*\\.vbproj|.*\\.csproj|.*\\.sln|.*\\.cshtml|.*\\.vbhtml" + constantFiles;
            case TS_JAVA_KEY:
                return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.tld" +
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
            case TS_JS_TS_HTML:
                return ".*\\*.json" + constantFiles;
            default:
                return ".*";
        }
    }

    /**
     * Zips a folder, stores it in a temp location and returns the object
     *
     * @param techStack technology stack of the folder to zip
     * @param workspace location of the files to zip
     * @param logger    logger to write status text to
     * @return a File object
     * @throws IOException no files
     */
    public static File createZipFile(String techStack, FilePath workspace, PrintStream logger) throws IOException {
        logger.println("Begin Create Zip.");
        logger.println("Source file directory: " + workspace);

        String tempDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tempDir);


        File tempZip = File.createTempFile("fodupload", ".zip", dir);
        try (FileOutputStream fos = new FileOutputStream(tempZip)) {
            final Pattern pattern = Pattern.compile(Utils.getFileExpressionPatternString(techStack),
                    Pattern.CASE_INSENSITIVE);
            workspace.zip(fos, new RegexFileFilter(pattern));
            logger.println("Temporary file created at: " + tempZip.getAbsolutePath());

        } catch (Exception e) {
            logger.println(e.getMessage());
        }
        logger.println("End Create Zip.");
        return tempZip;
    }
    
    public static String decrypt(String stringToDecrypt) {
        Secret decryptedSecret = Secret.decrypt(stringToDecrypt);
        return  decryptedSecret != null ?  decryptedSecret.getPlainText() : stringToDecrypt;
    }
    
    public static String decrypt(Secret stringToDecrypt) {
        return stringToDecrypt.getPlainText();
    }
    
    public static String encrypt(String stringToEncrypt) {
        String result = stringToEncrypt;
        if(Secret.decrypt(stringToEncrypt) == null){
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
}
