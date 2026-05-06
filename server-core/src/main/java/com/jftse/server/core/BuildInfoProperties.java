package com.jftse.server.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "build")
@Getter
@Setter
public class BuildInfoProperties {
    private String rev;
    private String time;
    private String branch;
    private String host;
    private String arch;

    public String getFullVersion() {
        return String.format("JFTSE rev. %s %s (%s branch) (%s, %s)", rev, time, branch, host, arch);
    }
}
