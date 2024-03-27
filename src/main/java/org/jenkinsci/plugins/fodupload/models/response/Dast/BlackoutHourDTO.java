package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD", "NM_FIELD_NAMING_CONVENTION"})
public class BlackoutHourDTO {    /// <summary>
    /// The 24 hour identifier (0-23)
    /// </summary>
    public int hour;
    /// <summary>
    /// Checked is true when this hour is blocked out. It is false when clear to scan
    /// </summary>
    public boolean checked;
}
