package com.ft.emulator.common.validation;

import com.ft.emulator.common.exception.ExtraInformationException;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends Exception implements ExtraInformationException {

    private boolean handleable = false;

    public ValidationException(String message) {
        super(message);
    }

    public Boolean getHandleable() {
        return handleable;
    }

    public void setHandleable(Boolean handleable) {
        this.handleable = handleable;
    }

    public Map<String, Object> getExtraInformation() {

        Map<String, Object> information = new HashMap<>();
        information.put("handleable", this.getHandleable());

        return information;
    }
}