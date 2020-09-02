# Fortify on Demand Jenkins Plugin

[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/fortify-on-demand-uploader.svg)](https://plugins.jenkins.io/fortify-on-demand-uploader/)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=plugins/fortify-on-demand-uploader-plugin/master)](https://ci.jenkins.io/job/plugins/job/fortify-on-demand-uploader-plugin/job/master)

Fortify on Demand is a Software as a Service (SaaS) solution that enables your organization to easily and quickly build and expand a Software Security Assurance program. The Fortify on Demand Jenkins Plugin enables users to upload code directly from Jenkins for Static Application Security Testing (SAST). This plugin features the following tasks:

* Run a static assessment for each build triggered by Jenkins.  
* Poll for scan status and scan results. 

This plugin requires a Fortify on Demand account. For more information on Fortify on Demand and to request a free trial, see https://software.microfocus.com/en-us/software/fortify-on-demand.

**More Information**  
Changelog: https://github.com/jenkinsci/fortify-on-demand-uploader-plugin/blob/master/CHANGELOG.md  
Usage instructions: https://www.microfocus.com/documentation/fortify-on-demand-jenkins-plugin/

## Additional Considerations For Maven Users

For the most complete assessment of your application it is important to ensure all dependencies for deployment are satisfied. Maven provides a simple means of outputting these libraries by the **maven-dependency-plugin**. The section **\<excludeGroupIds\>** may be used to ensure test framework code, for example, is not included.

**Example POM Section**:

    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <version>2.6</version>
      <executions>
        <execution>
          <id>copy-dependencies</id>
          <phase>prepare-package</phase>
          <goals>
            <goal>copy-dependencies</goal>
          </goals>
          <configuration>
            <outputDirectory>target/classes/lib</outputDirectory>
            <overWriteIfNewer>true</overWriteIfNewer>
            <excludeGroupIds>
              junit,org.easymock,${project.groupId}
            </excludeGroupIds>
          </configuration>
        </execution>
        <execution>
          <phase>generate-sources</phase>
          <goals>
            <goal>sources</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <verbose>true</verbose>
        <detail>true</detail>
        <outputDirectory>${project.build.directory}</outputDirectory>
      </configuration>
    </plugin>
 
    ...
 
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-source-plugin</artifactId>
      <executions>
        <execution>
          <id>attach-sources</id>
          <goals>
            <goal>jar</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

## Known Limitations
-   The 2.0.9 (Obsolete) plugin version is slow to populate the pull down menu's in Redhat 7 machines.  Please wait a minute or two and the first field should populate.
