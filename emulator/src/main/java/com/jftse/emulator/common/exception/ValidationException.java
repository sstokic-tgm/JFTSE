package com.jftse.emulator.common.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends Exception implements ExtraInformationException {
    private boolean handleable = false;

    public ValidationException(String message) {
        super(message);
    }

    public boolean getHandleable() {
        return this.handleable;
    }

    public void setHandleable(boolean handleable) {
        this.handleable = handleable;
    }

    public Map<String, Object> getExtraInformation() {
        Map<String, Object> information = new HashMap<>();
        information.put("handleable", this.getHandleable());
        return information;
    }
}
