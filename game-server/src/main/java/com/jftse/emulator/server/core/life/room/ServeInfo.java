package com.jftse.emulator.server.core.life.room;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ServeInfo {
    private short playerPosition;
    private Point playerStartLocation;
    private byte serveType;
}
