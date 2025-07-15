package com.jftse.emulator.common.scripting;

import lombok.Getter;
import lombok.Setter;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import javax.script.CompiledScript;
import java.io.File;

@Getter
@Setter
public class ScriptFile {
    private Long id;
    private String name;
    private File file;
    private String type;
    private String subType;

    private CompiledScript compiledScript;
    private Context context;
    private Source source;

    public ScriptFile(Long id, String name, File file, String type, String subType) {
        this.id = id;
        this.name = name;
        this.file = file;
        this.type = type;
        this.subType = subType;
    }

    @Override
    public String toString() {
        return "ScriptFile { " +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", id=" + id +
                ", file=" + file.getName() +
                " }";
    }
}
