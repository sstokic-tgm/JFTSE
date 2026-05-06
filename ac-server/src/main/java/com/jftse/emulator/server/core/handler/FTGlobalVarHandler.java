package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.server.core.packets.S2CFTGlobalVarStructPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.ac.CMSGAntiCheatFTGlobalVars;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Log4j2
@PacketId(CMSGAntiCheatFTGlobalVars.PACKET_ID)
public class FTGlobalVarHandler implements PacketHandler<FTConnection, CMSGAntiCheatFTGlobalVars> {
    public static final String FTGLOBALVARS_PROPERTIES = "ftglobalvars.properties";

    @Override
    public void handle(FTConnection connection, CMSGAntiCheatFTGlobalVars packet) {
        if (packet.getN() == 1) {
            Optional<Path> p = ResourceUtil.getPath(FTGLOBALVARS_PROPERTIES);
            if (p.isEmpty())
                return;

            Properties properties = new Properties();
            List<Integer> propValues = new ArrayList<>();
            try (InputStream is = Files.newInputStream(p.get(), StandardOpenOption.READ)) {
                properties.load(is);

                propValues.add(Integer.valueOf(properties.getProperty("fileLog", "0")));
                propValues.add(Integer.valueOf(properties.getProperty("optimizeLoading", "1")));
                propValues.add(Integer.valueOf(properties.getProperty("fastGuiLoading", "1")));
                propValues.add(Integer.valueOf(properties.getProperty("halloweenEvent", "0")));
                propValues.add(Integer.valueOf(properties.getProperty("useSnowEvent", "0")));
                propValues.add(Integer.valueOf(properties.getProperty("uiSnowEffect", "0")));
                propValues.add(Integer.valueOf(properties.getProperty("cherryblossomsEvent", "0")));
                propValues.add(Integer.valueOf(properties.getProperty("hitProductShow", "0")));
                propValues.add(Integer.valueOf(properties.getProperty("enableCoupon", "0")));

            } catch (IOException e) {
                log.error("Error reading the properties file: {}", e.getMessage(), e);
                return;
            }

            S2CFTGlobalVarStructPacket ftGlobalVarStructPacket = new S2CFTGlobalVarStructPacket(propValues);
            connection.sendTCP(ftGlobalVarStructPacket);
        }
    }
}
