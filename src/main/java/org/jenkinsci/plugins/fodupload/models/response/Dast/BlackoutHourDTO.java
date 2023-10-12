package org.jenkinsci.plugins.fodupload.models.response.Dast;

public class BlackoutHourDTO {    /// <summary>
    /// The 24 hour identifier (0-23)
    /// </summary>
    public int Hour;
    /// <summary>
    /// Checked is true when this hour is blocked out. It is false when clear to scan
    /// </summary>
    public boolean Checked;
}
