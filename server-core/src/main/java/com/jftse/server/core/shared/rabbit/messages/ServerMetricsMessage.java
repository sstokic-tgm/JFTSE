package com.jftse.server.core.shared.rabbit.messages;

import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ServerMetricsMessage extends AbstractBaseMessage {
    private String server;
    private long timestampMs;
    private String revision;

    private long ticksTotal;
    private double avgTickMsTotal;
    private long maxTickMsTotal;
    private double avgUpdateMsTotal;
    private long maxUpdateMsTotal;
    private long ticksDelta;
    private double ticksPerSec;
    private double avgTickMsWindow;
    private Map<String, Object> attributes;

    @Builder
    public ServerMetricsMessage(String server,
                                long timestampMs,
                                String revision,
                                long ticksTotal,
                                double avgTickMsTotal,
                                long maxTickMsTotal,
                                double avgUpdateMsTotal,
                                long maxUpdateMsTotal,
                                long ticksDelta,
                                double ticksPerSec,
                                double avgTickMsWindow,
                                Map<String, Object> attributes) {
        this.server = server;
        this.timestampMs = timestampMs;
        this.revision = revision;
        this.ticksTotal = ticksTotal;
        this.avgTickMsTotal = avgTickMsTotal;
        this.maxTickMsTotal = maxTickMsTotal;
        this.avgUpdateMsTotal = avgUpdateMsTotal;
        this.maxUpdateMsTotal = maxUpdateMsTotal;
        this.ticksDelta = ticksDelta;
        this.ticksPerSec = ticksPerSec;
        this.avgTickMsWindow = avgTickMsWindow;
        this.attributes = attributes;
    }

    @Override
    public String getMessageType() {
        return "SERVER_METRICS";
    }
}
