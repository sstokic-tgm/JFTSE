package com.jftse.emulator.common.discord;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

@Getter
@Setter
public class DiscordWebhook {

    private final String url;
    private String content;

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void execute() throws IOException {

        JSONObject json = new JSONObject();
        json.put("content", this.content);

        URL url = new URL(this.url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json; utf-8");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes());
        stream.flush();
        stream.close();

        connection.getInputStream().close();
        connection.disconnect();
    }
}