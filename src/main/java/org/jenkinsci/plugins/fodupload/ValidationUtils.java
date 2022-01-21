package org.jenkinsci.plugins.fodupload;

import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.w3c.tidy.Out;

import java.util.Arrays;
import java.util.List;

public class ValidationUtils {

    private static List<Integer> techStacksSupportSonatypeScans = Arrays.asList(2,3,5,6,11,14,18,21);
    private static List<Integer> scanCentralOnlyTechStacks = Arrays.asList(2,3,5,6,11,14,18,21);

    public enum ScanCentralValidationResult {
        Valid, Mismatched, ScanCentralRequired, NoSelection
    }

    public static boolean isSonatypeScanNotAllowedForTechStack(int techStack){
        return techStacksSupportSonatypeScans.contains(techStack);
    }

    public static ScanCentralValidationResult isValidScanCentralAndTechStack(String scanCentral, int techStack)  {
        switch (scanCentral) {
            case "Gradle":
            case "Maven":
                if(techStack != 7) return ScanCentralValidationResult.Mismatched;
                break;
            case "MSBuild":
                if(techStack != 1 && techStack != 23) return ScanCentralValidationResult.Mismatched;
                break;
            case "PHP":
                if(techStack != 9) return ScanCentralValidationResult.Mismatched;
                break;
            case "Python":
                if(techStack != 10) return ScanCentralValidationResult.Mismatched;
                break;
            default:
                if(scanCentralOnlyTechStacks.contains(techStack)) return ScanCentralValidationResult.ScanCentralRequired;
                else if(techStack < 1) return ScanCentralValidationResult.NoSelection;
        }

        return ScanCentralValidationResult.Valid;
    }

}
