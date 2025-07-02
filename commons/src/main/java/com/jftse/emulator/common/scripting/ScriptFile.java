package com.jftse.emulator.common.scripting;

import lombok.Getter;
import lombok.Setter;

import javax.script.CompiledScript;
import java.io.File;

@Getter
@Setter
public class ScriptFile {
    private Long id;
    private File file;
    private String type;
    private String subType;

    private CompiledScript compiledScript;

    public ScriptFile(Long id, File file, String type, String subType) {
        this.id = id;
        this.file = file;
        this.type = type;
        this.subType = subType;
    }

    @Override
    public String toString() {
        return "ScriptFile { " +
                "type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", id=" + id +
                ", file=" + file.getName() +
                " }";
    }
}
