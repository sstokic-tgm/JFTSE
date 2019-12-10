package com.ft.emulator.common.resource;

import java.io.InputStream;

public class ResourceUtil {

    public static InputStream getResource(String resource) {

	return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }
}