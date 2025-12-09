package tools;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPacketGen {
    private static Path PACKET_SRC;
    private static Path OUTPUT_SRC;

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("message\\s+(\\w+)(?:\\s*\\((\\dx\\w+)\\))?\\s*\\{([^}]*)}", Pattern.DOTALL);
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "(repeated\\s+)?(\\w+)\\s+(\\w+)\\s*=\\s*\\d+\\s*(\\[[^]]*])?\\s*;?"
    );

    private static final List<GeneratedClass> generatedClasses = new ArrayList<>();
    private static final Map<String, List<Field>> parsedMessages = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java FTPacketGen <packet_src_dir> <output_src_dir>");
            return;
        }
        PACKET_SRC = Paths.get(args[0]);
        OUTPUT_SRC = Paths.get(args[1]);

        System.out.println("[FTPacketGen] Scanning " + PACKET_SRC.toAbsolutePath());
        Files.createDirectories(OUTPUT_SRC);

        List<Path> packetFiles = new ArrayList<>();
        try (var walk = Files.walk(PACKET_SRC)) {
            walk.filter(p -> p.toString().endsWith(".packet")).forEach(packetFiles::add);
        }

        for (Path file : packetFiles) {
            generateForFile(file);
        }

        generateAutoRegisterClasses();

        System.out.println("[FTPacketGen] Done. Generated classes in " + OUTPUT_SRC.toAbsolutePath());
    }

    private static void generateAutoRegisterClasses() throws IOException {
        if (generatedClasses.isEmpty()) return;

        String pkg = "com.jftse.server.core.protocol";
        Path pkgDir = OUTPUT_SRC.resolve(pkg.replace('.', '/'));
        Files.createDirectories(pkgDir);

        Path outFile = pkgDir.resolve("PacketAutoRegister.java");
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outFile))) {
            out.println("package " + pkg + ";");
            out.println();
            out.println("import lombok.extern.log4j.Log4j2;");
            out.println();
            out.println("@Log4j2");
            out.println("public final class PacketAutoRegister {");
            out.println("    public static void registerAll() {");
            out.println("        log.info(\"Registering packets...\");");
            out.println("        try {");

            generatedClasses.sort(Comparator.comparingInt(GeneratedClass::packetId));

            int maxCodeLen = 0;
            int maxDecLen = 0;
            for (GeneratedClass c : generatedClasses) {
                int codeLen = ("            Class.forName(\"" + c.packageName + "\");").length();
                if (codeLen > maxCodeLen) maxCodeLen = codeLen;

                int decLen = String.valueOf(c.packetId).length();
                if (decLen > maxDecLen) maxDecLen = decLen;
            }

            for (GeneratedClass c : generatedClasses) {
                final String code = "            Class.forName(\"" + c.packageName + "\");";
                final String packetId = String.valueOf(c.packetId);

                final String codePadding = " ".repeat(Math.max(0, maxCodeLen - code.length() + 2));
                final String decPadding = " ".repeat(Math.max(0, maxDecLen - packetId.length() + 1));

                final String comment = "// " + packetId + decPadding + String.format("[0x%X]", c.packetId);
                out.println(code + codePadding + comment);
            }
            out.println("        } catch (ClassNotFoundException e) {");
            out.println("            log.error(\"Error during packet auto registration\", e);");
            out.println("        }");
            out.println("        log.info(\"Registered {} packets.\", " + generatedClasses.size() + ");");
            out.println("        com.jftse.server.core.protocol.PacketRegistry.registerHandlers();");
            out.println("    }");
            out.println("}");
        }

        System.out.println("[FTPacketGen] Generated PacketAutoRegister with " + generatedClasses.size() + " packets.");
    }

    private static String generateForFile(Path file) throws IOException {
        String src = Files.readString(file);
        Matcher msgMatch = MESSAGE_PATTERN.matcher(src);
        boolean defaultPacket = false;
        if (!msgMatch.find()) {
            Matcher defaultMatch = Pattern.compile("message\\s+(\\w+)\\s*\\((\\d)\\)\\s*\\{").matcher(src);
            if (!defaultMatch.find()) {
                System.err.println("No message block found in " + file);
                return null;
            } else {
                msgMatch = defaultMatch;
                defaultPacket = true;
            }
        }

        msgMatch.reset();

        String lastMsgName = null;
        String relative = PACKET_SRC.relativize(file.getParent()).toString();
        String pkg = relative.replace(File.separatorChar, '.');
        if (!pkg.isBlank()) pkg = "com.jftse.server.core.shared.packets." + pkg;
        else pkg = "com.jftse.server.core.shared.packets";

        Path pkgDir = OUTPUT_SRC.resolve(pkg.replace('.', '/'));
        Files.createDirectories(pkgDir);

        while (msgMatch.find()) {
            String msgName = msgMatch.group(1);
            String sPacketId = msgMatch.group(2);
            final boolean isPacket = sPacketId != null && !StringUtils.isEmpty(sPacketId);
            String body = defaultPacket ? "" : msgMatch.group(3).trim();
            if (!defaultPacket) {
                validateFieldOrder(msgName, body);
            }

            List<Field> fields = defaultPacket ? new ArrayList<>() : parseFields(body);

            String className = toClassName(msgName);
            boolean isClientMessage = msgName.startsWith("CMSG") || msgName.startsWith("cmsg");

            Path outFile = pkgDir.resolve(className + ".java");
            System.out.println("[FTPacketGen] Generating " + outFile.toAbsolutePath());
            if (!isPacket) {
                System.out.println("[FTPacketGen] Generating class " + className);
                parsedMessages.putIfAbsent(className, fields);
                try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outFile))) {
                    writeClass(out, pkg, className, fields, isClientMessage);
                }
                System.out.println("[FTPacketGen] Generated " + outFile + " (" + className + ")");
                lastMsgName = pkg + "." + className;
                continue;
            }

            int packetId = Integer.decode(sPacketId);
            System.out.println("[FTPacketGen] Generating packet class " + className + " with ID " + String.format("0x%04X", packetId));
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outFile))) {
                writePacketClass(out, pkg, className, packetId, fields, isClientMessage);
            }
            System.out.println("[FTPacketGen] Generated " + outFile);
            if (isClientMessage) {
                generatedClasses.add(new GeneratedClass(pkg + "." + className, packetId));
            }
            lastMsgName = pkg + "." + className;
        }

        return lastMsgName;
    }

    private static List<Field> parseFields(String body) {
        List<Field> fields = new ArrayList<>();
        Matcher fieldMatch = FIELD_PATTERN.matcher(body);
        while (fieldMatch.find()) {
            boolean repeated = fieldMatch.group(1) != null;
            String type = fieldMatch.group(2);
            String name = fieldMatch.group(3);
            if (name.equals("data") || name.equals("metaData")) {
                System.err.println("Field name 'data' or 'metaData' is reserved. Please rename the field.");
                continue;
            }
            String opts = fieldMatch.group(4);
            Map<String, String> options = parseOptions(opts);
            fields.add(new Field(type, name, repeated, options));
        }
        return fields;
    }

    private static Map<String, String> parseOptions(String opts) {
        Map<String, String> options = new HashMap<>();
        if (opts == null) return options;

        Matcher matcher = Pattern.compile("(\\w+)\\s*=\\s*([^,\\]]+)").matcher(opts);
        while (matcher.find()) options.put(matcher.group(1), matcher.group(2));
        return options;
    }

    private static String toClassName(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = true;
        for (char c : name.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            } else {
                upperNext = true;
            }
        }
        return sb.toString();
    }

    private static void writeClass(PrintWriter out, String pkg, String className, List<Field> fields, boolean isClientMessage) {
        out.println("package " + pkg + ";");
        out.println();
        out.println("import java.util.*;");
        out.println();
        out.println("public class " + className + " {");
        for (Field f : fields) {
            if (f.repeated && !f.type.equals("bytes")) {
                out.printf("    private List<%s> %s = new ArrayList<>();%n", mapType(f), f.name);
            } else {
                out.printf("    private %s %s;%n", mapType(f), f.name);
            }
        }
        out.println();

        writeBuilderClass(out, className, false, fields, isClientMessage);
        writeBuilderGetter(out);

        writeGetterSetter(out, fields);

        out.println("}");
    }

    private static void writePacketClass(PrintWriter out, String pkg, String className, int packetId, List<Field> fields, boolean isClientMessage) {
        out.println("package " + pkg + ";");
        out.println();
        out.println("import java.nio.*;");
        out.println("import java.util.*;");
        out.println("import com.jftse.emulator.common.utilities.BitKit;");
        out.println("import com.jftse.server.core.protocol.IPacket;");
        out.println();
        out.println("public class " + className + " implements IPacket {");
        out.println();
        out.println("    public static final int PACKET_ID = " + packetId + ";");
        out.println();
        out.println("    private static class MetaData {");
        out.println("        char checkSerial;");
        out.println("        char checkSum;");
        out.println("        char packetId;");
        out.println("        char dataLen;");
        out.println("    }");
        out.println();

        for (Field f : fields) {
            if (f.repeated && !f.type.equals("bytes")) {
                out.printf("    private List<%s> %s = new ArrayList<>();%n", mapType(f), f.name);
            } else {
                out.printf("    private %s %s;%n", mapType(f), f.name);
            }
        }
        out.println();

        out.println("    private final MetaData metaData = new MetaData();");
        out.println("    private byte[] data;");
        out.println("    private int readPos = 0;");
        out.println();

        if (isClientMessage) {
            out.println("    static {");
            out.println("        com.jftse.server.core.protocol.PacketRegistry.register(PACKET_ID, " + className + "::fromBytes);");
            out.println("    }");
            out.println();
        }

        out.println("    private " + className + "() { }");
        out.println();

        writeBuilderClass(out, className, true, fields, isClientMessage);
        writeBuilderGetter(out);

        // fromBytes
        out.println("    public static " + className + " fromBytes(byte[] rawData) {");
        out.println("        " + className + " msg = new " + className + "();");
        out.println("        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.nativeOrder());");
        out.println();
        out.println("        msg.metaData.checkSerial = buffer.getChar(0);");
        out.println("        msg.metaData.checkSum = buffer.getChar(2);");
        out.println("        msg.metaData.packetId = buffer.getChar(4);");
        out.println("        msg.metaData.dataLen = buffer.getChar(6);");
        out.println();
        out.println("        msg.data = new byte[msg.metaData.dataLen];");
        out.println("        BitKit.blockCopy(rawData, 8, msg.data, 0, msg.metaData.dataLen);");
        out.println();

        if (isClientMessage) {
            for (Field f : fields) {
                out.println("        msg." + f.name + " = " + readExpr(f) + ";");
            }
        }
        out.println("        return msg;");
        out.println("    }");
        out.println();

        out.println("    public static " + className + " from(IPacket packet) {");
        out.println("        return fromBytes(packet.toBytes());");
        out.println("    }");
        out.println();

        // toBytes
        out.println("    @Override");
        out.println("    public byte[] toBytes() {");
        out.println("        byte[] packet = new byte[8 + this.metaData.dataLen];");
        out.println("        byte[] _serial = BitKit.getBytes(this.metaData.checkSerial);");
        out.println("        byte[] _check = BitKit.getBytes(this.metaData.checkSum);");
        out.println("        byte[] _packetId = BitKit.getBytes(this.metaData.packetId);");
        out.println("        byte[] _dataLen = BitKit.getBytes(this.metaData.dataLen);");
        out.println("        BitKit.blockCopy(_serial, 0, packet, 0, 2);");
        out.println("        BitKit.blockCopy(_check, 0, packet, 2, 2);");
        out.println("        BitKit.blockCopy(_packetId, 0, packet, 4, 2);");
        out.println("        BitKit.blockCopy(_dataLen, 0, packet, 6, 2);");
        out.println("        BitKit.blockCopy(this.data, 0, packet, 8, this.metaData.dataLen);");
        out.println("        return packet;");
        out.println("    }");
        out.println();

        writeGetterSetter(out, fields);

        out.println("    @Override public char getDataLength() { return this.metaData.dataLen; }");
        out.println("    @Override public char getPacketId() { return this.metaData.packetId; }");
        out.println("    @Override public char getCheckSerial() { return this.metaData.checkSerial; }");
        out.println("    @Override public char getCheckSum() { return this.metaData.checkSum; }");
        out.println();
        if (isClientMessage) {
            out.println("    public <T> T read(Class<T> type) {");
            out.println("        Object value;");
            out.println("        if (type == Character.class) value = readChar();");
            out.println("        else if (type == Short.class) value = readShort();");
            out.println("        else if (type == Integer.class) value = readInt();");
            out.println("        else if (type == Long.class) value = readLong();");
            out.println("        else if (type == String.class) value = readString();");
            out.println("        else if (type == Byte.class) value = readByte();");
            out.println("        else if (type == Boolean.class) value = readBoolean();");
            out.println("        else if (type == Float.class) value = readFloat();");
            out.println("        else if (type == Double.class) value = readDouble();");
            out.println("        else if (type == java.util.Date.class) value = readDate();");
            out.println("        else value = null; // unsupported type");
            out.println("        return type.cast(value);");
            out.println("    }");
            out.println();

            out.println(readFuncs());
            out.println(readStringFuncs());
            out.println(
                    """
                                private int indexOf(byte[] data, byte[] pattern, int offset) {
                                      for (int i = offset; i < data.length; i += pattern.length) {
                                          boolean found = false;
                                          for (int j = 0; j < pattern.length; j++) {
                                              if (data[i + j] != pattern[j])
                                                  break;
                                              found = true;
                                          }
                                          if (found)
                                              return i;
                                      }
                                      return -1;
                                }
                            
                                private boolean isAscii(String text) {
                                    return text.chars().allMatch(c -> c >= 0x20 && c < 0x7F);
                                }
                            """);
            if (packetId == 0) {
                out.println(writeFuncs());
            }
        } else {
            out.println(writeFuncs());
        }

        out.println(writeToStringFunc(fields, className));

        out.println("}");
    }

    private static void writeBuilderClass(PrintWriter out, String className, boolean isPacket, List<Field> fields, boolean isClientMessage) {
        out.println("    public static final class Builder {");
        out.println("        private final " + className + " msg;");
        out.println();
        out.println("        public Builder() {");
        out.println("            msg = new " + className + "();");
        if (isPacket) {
            out.println("            msg.metaData.packetId = (char) PACKET_ID;");
            out.println("            msg.metaData.checkSerial = 0;");
            out.println("            msg.metaData.checkSum = 0;");
            out.println("            msg.metaData.dataLen = 0;");
            out.println("            msg.data = new byte[16384];");
        }
        out.println("        }");
        for (Field f : fields) {
            String t = mapType(f);
            if (f.repeated && !f.type.equals("bytes")) {
                out.printf("        public Builder %s(List<%s> %s) { msg.%s = %s; return this; }%n", f.name, t, f.name, f.name, f.name);
            } else {
                out.printf("        public Builder %s(%s %s) { msg.%s = %s; return this; }%n", f.name, t, f.name, f.name, f.name);
            }
        }

        out.println("        public " + className + " build() {");
        if (!isClientMessage && isPacket) {
            for (Field f : fields) {
                out.println("            " + writeExpr(f));
            }
        }
        out.println("            return msg;");
        out.println("        }");
        out.println("    }");
        out.println();
    }

    private static void writeBuilderGetter(PrintWriter out) {
        out.println("    public static Builder builder() {");
        out.println("        return new Builder();");
        out.println("    }");
        out.println();
    }

    private static String mapType(String t) {
        return switch (t) {
            case "int", "uint32", "int32" -> "int";
            case "long", "uint64", "int64" -> "long";
            case "float" -> "float";
            case "double" -> "double";
            case "string" -> "String";
            case "bool", "boolean" -> "boolean";
            case "byte", "uint8", "int8" -> "byte";
            case "char" -> "char";
            case "short", "uint16", "int16" -> "short";
            case "date" -> "java.util.Date";
            case "bytes" -> "byte[]";
            default -> t;
        };
    }

    private static boolean supportedType(String t) {
        return switch (t) {
            case "int", "uint32", "int32",
                 "long", "uint64", "int64",
                 "float",
                 "double",
                 "string",
                 "bool", "boolean",
                 "byte", "uint8", "int8",
                 "char",
                 "short", "uint16", "int16",
                 "date",
                 "bytes" -> true;
            default -> false;
        };
    }

    private static String mapType(Field f) {
        String baseType = mapType(f.type);
        return f.repeated ? typeToObjectType(baseType) : baseType;
    }

    private static String typeToObjectType(String t) {
        return switch (t) {
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "boolean" -> "Boolean";
            case "byte" -> "Byte";
            case "char" -> "Character";
            case "short" -> "Short";
            case "byte[]" -> "byte[]";
            default -> t;
        };
    }

    private static String readExpr(Field f) {
        return readExprRecursive(f, 0);
    }

    private static String readExprRecursive(Field f, int depth) {
        String t = f.type;
        String mappedType = mapType(t);
        Map<String, String> o = f.options;

        if (f.repeated && !t.equals("bytes")) {
            if (o.containsKey("len")) {
                return "msg.readRepeated(() -> " + readExprRecursive(new Field(t, "tmp", false, o), depth + 1) + ", " + o.get("len") + ")";
            }

            return "msg.readRepeated(() -> " + readExprRecursive(new Field(t, "tmp", false, o), depth + 1) + ")";
        }

        return switch (t) {
            case "int", "uint32", "int32" -> "msg.readInt()";
            case "long", "uint64", "int64" -> "msg.readLong()";
            case "float" -> "msg.readFloat()";
            case "double" -> "msg.readDouble()";
            case "string" -> {
                if (o.containsKey("len")) {
                    yield "msg.readFixedString(" + o.get("len") + ")";
                } else {
                    yield "msg.readString()";
                }
            }
            case "bool", "boolean" -> "msg.readBoolean()";
            case "byte", "uint8", "int8" -> "msg.readByte()";
            case "char" -> "msg.readChar()";
            case "short", "uint16", "int16" -> "msg.readShort()";
            case "date" -> "msg.readDate()";
            case "bytes" -> {
                if (o.containsKey("len")) {
                    yield "msg.readBytes(" + o.get("len") + ")";
                } else {
                    yield "msg.readBytes()";
                }
            }
            default -> {
                if (parsedMessages.containsKey(t)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(t).append(".builder()");
                    for (Field subField : parsedMessages.get(t)) {
                        sb.append(".").append(subField.name).append("(").append(readExprRecursive(subField, depth + 1)).append(")");
                    }
                    sb.append(".build()");
                    yield sb.toString();
                }

                yield "null /* unsupported type " + t + " */";
            }
        };
    }

    private static String readStringFuncs() {
        return """
                    private String readString() {
                        String result = "";
                        if (this.readPos >= 0 && this.readPos < this.data.length) {
                            String text = new String(new byte[]{this.data[this.readPos], this.data[this.readPos + 1]});
                            if (!this.isAscii(text)) {
                                int stringLength = indexOf(this.data, new byte[]{0x00, 0x00}, this.readPos) - this.readPos;
                
                                if (stringLength > 1) {
                                    result = new String(this.data, this.readPos, stringLength, java.nio.charset.StandardCharsets.UTF_16LE);
                                    this.readPos += stringLength + 2;
                                } else {
                                    this.readPos += 2;
                                }
                            } else {
                                int stringLength = indexOf(this.data, new byte[]{0x00}, this.readPos) - this.readPos;
                
                                if (stringLength > 0) {
                                    result = new String(this.data, this.readPos, stringLength, java.nio.charset.StandardCharsets.UTF_8);
                                    this.readPos += stringLength + 1;
                                } else {
                                    this.readPos += 1;
                                }
                            }
                        }
                        return result;
                    }
                
                    private String readFixedString(int len) {
                        if (this.readPos + len > this.data.length)
                            len = data.length - readPos;
                
                        byte[] strBytes = new byte[len];
                        BitKit.blockCopy(this.data, readPos, strBytes, 0, len);
                        this.readPos += len;
                
                        return new String(strBytes, java.nio.charset.StandardCharsets.UTF_8);
                    }
                """;
    }

    private static String writeExpr(Field f) {
        Map<String, String> o = f.options;
        if (o.containsKey("type") && !parsedMessages.containsKey(f.type)) {
            final String sizeType = o.get("type");
            final boolean supportedType = supportedType(sizeType);
            if (supportedType && f.repeated) {
                return "msg.write((" + mapType(new Field(sizeType, "tmp", false, o)) + ") msg." + f.name + ".size());\n\t\tmsg." + f.name + ".forEach(msg::write);";
            }
            if (supportedType) {
                return "msg.write((" + mapType(new Field(sizeType, "tmp", false, o)) + ") msg." + f.name + ");";
            }
        }

        if (f.repeated && parsedMessages.containsKey(f.type) && !f.type.equals("bytes")) {
            return getRepeatedObjectFields(f, o);
        }

        if (parsedMessages.containsKey(f.type) && !f.type.equals("bytes")) {
            return getObjectFields(f, 0);
        }

        // default behavior
        if (f.repeated && !f.type.equals("bytes")) {
            return "msg.write((" + mapType(new Field("byte", "tmp", false, o)) + ") msg." + f.name + ".size());\n\t\tmsg." + f.name + ".forEach(msg::write);";
        }

        if (f.type.equals("string")) {
            if (o.containsKey("len")) {
                return "msg.writeFixedString(msg." + f.name + ", " + o.get("len") + ");";
            }

            if (o.containsKey("encoding") && o.get("encoding").equalsIgnoreCase("utf8")) {
                return "msg.writeStringUTF8(msg." + f.name + ");";
            }
        }

        return "msg.write(msg." + f.name + ");";
    }

    private static String getObjectFields(Field f, int tabCount) {
        return collectObjectFieldsRecursive(f.type, "msg." + f.name, tabCount);
    }

    private static String collectObjectFieldsRecursive(String type, String accessor, int tabCount) {
        String tabs = "\t".repeat(tabCount);
        List<Field> subFields = parsedMessages.get(type);
        if (subFields == null || subFields.isEmpty()) return accessor;

        List<Field> nestedFields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        sb.append(tabs).append("msg.write(");
        for (Field sub : subFields) {
            String sizeType = sub.options.getOrDefault("type", "byte");
            String getter = accessor + ".get" +
                    Character.toUpperCase(sub.name.charAt(0)) +
                    sub.name.substring(1) + "()";

            if (sub.repeated && parsedMessages.containsKey(sub.type)) {
                nestedFields.add(sub);
                continue;
            }

            if (parsedMessages.containsKey(sub.type)) {
                nestedFields.add(sub);
                continue;
            }

            if (sub.repeated) {
                sb.append("(").append(mapType(new Field(sizeType, "tmp", false, sub.options))).append(") ").append(getter).append(".size());\n");
                sb.append(tabCount).append(getter).append(".forEach(msg::write);\n");
                continue;
            }

            sb.append(getter).append(", ");
        }
        if (sb.toString().endsWith(", ")) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(");");
        if (!nestedFields.isEmpty()) {
            sb.append("\n");
        }

        for (Field nested : nestedFields) {
            String nestedAccessor = accessor + ".get" +
                    Character.toUpperCase(nested.name.charAt(0)) +
                    nested.name.substring(1) + "()";
            if (nested.repeated && parsedMessages.containsKey(nested.type)) {
                tabs = "\t".repeat(tabCount == 0 ? tabCount + 2 : tabCount);
                sb.append("\n").append(tabs)
                        .append(getRepeatedObjectFields(nested, nested.options, tabCount == 0 ? tabCount + 2 : tabCount, nestedAccessor));
                continue;
            }

            sb.append(collectObjectFieldsRecursive(nested.type, nestedAccessor, tabCount == 0 ? tabCount + 2 : tabCount));
        }

        return sb.toString();
    }

    private static String getRepeatedObjectFields(Field f, Map<String, String> o) {
        String accessor = "msg." + f.name;
        return getRepeatedObjectFields(f, o, 3, accessor);
    }

    private static String getRepeatedObjectFields(Field f, Map<String, String> o, int tabCount, String accessor) {
        String tabs = "\t".repeat(tabCount);
        String sizeType = o.getOrDefault("type", "byte");
        StringBuilder sb = new StringBuilder();

        String param = deriveLambdaName(f.type);

        sb.append("msg.write((")
                .append(mapType(new Field(sizeType, "tmp", false, o)))
                .append(") ")
                .append(accessor)
                .append(".size());\n");

        sb.append(tabs).append(accessor)
                .append(".forEach(")
                .append(param)
                .append(" -> {\n")
                .append(collectObjectFieldsRecursive(f.type, param, tabCount + 1))
                .append("\n").append(tabs).append("});");

        return sb.toString();
    }

    private static String deriveLambdaName(String type) {
        StringBuilder shortName = new StringBuilder();
        for (char c : type.toCharArray()) {
            if (Character.isUpperCase(c)) {
                shortName.append(Character.toLowerCase(c));
                if (shortName.length() >= 2) break;
            }
        }
        return shortName.isEmpty() ? "x" : shortName.toString();
    }

    private static String writeFuncs() {
        return """
                     public void write(Object... o) {
                         List<Object> dataList = new ArrayList<>(Arrays.asList(o));
                         dataList.forEach(this::write);
                     }
                
                     public void write(Object element) {
                         byte[] dataElement;
                         if (element instanceof Character) {
                             dataElement = BitKit.getBytes((char) element);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 2);
                             this.metaData.dataLen += (char) 2;
                         } else if (element instanceof Short) {
                             dataElement = BitKit.getBytes((short) element);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 2);
                             this.metaData.dataLen += (char) 2;
                         } else if (element instanceof Integer) {
                             dataElement = BitKit.getBytes((int) element);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 4);
                             this.metaData.dataLen += (char) 4;
                         } else if (element instanceof Long) {
                             dataElement = BitKit.getBytes((long) element);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 8);
                             this.metaData.dataLen += (char) 8;
                         } else if (element instanceof String e) {
                             if (!e.isEmpty()) {
                                 dataElement = e.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
                                 BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, dataElement.length);
                                 this.metaData.dataLen += (char) dataElement.length;
                             }
                
                             BitKit.blockCopy(new byte[]{0, 0}, 0, this.data, this.metaData.dataLen, 2);
                             this.metaData.dataLen += 2;
                         } else if (element instanceof Byte) {
                             dataElement = BitKit.getBytes(BitKit.byteToChar((byte) element));
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 1);
                             this.metaData.dataLen += (char) 1;
                         } else if (element instanceof Boolean) {
                             dataElement = BitKit.getBytes((byte) ((boolean) element ? 1 : 0));
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 1);
                             this.metaData.dataLen += (char) 1;
                         } else if (element instanceof Float) {
                             dataElement = BitKit.getBytes((float) element);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 4);
                             this.metaData.dataLen += (char) 4;
                         } else if (element instanceof Double) {
                             dataElement = BitKit.getBytes((double) element);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 8);
                             this.metaData.dataLen += (char) 8;
                         } else if (element instanceof java.util.Date) {
                             dataElement = BitKit.getBytes((((java.util.Date) element).getTime() + 11644473600000L) * 10000L);
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, 8);
                             this.metaData.dataLen += (char) 8;
                         } else if (element instanceof byte[]) {
                             dataElement = (byte[]) element;
                             BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, dataElement.length);
                             this.metaData.dataLen += (char) dataElement.length;
                         } else if (element == null) {
                             // do nothing for null values
                         }
                     }
                     
                     public void writeStringUTF8(String text) {
                            if (text != null && !text.isEmpty()) {
                                byte[] dataElement = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                                BitKit.blockCopy(dataElement, 0, this.data, this.metaData.dataLen, dataElement.length);
                                this.metaData.dataLen += (char) dataElement.length;
                            }
                            BitKit.blockCopy(new byte[]{0}, 0, this.data, this.metaData.dataLen, 1);
                            this.metaData.dataLen += 1;
                     }
                     
                     public void writeFixedString(String text, int len) {
                            if (text == null || text.length() < len) return;
                            byte[] dataElement = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            for (int i = 0; i < len; i++)
                                write(dataElement[i]);
                     }
                """;
    }

    private static String readFuncs() {
        return """
                     private float readFloat() {
                         float result = BitKit.bytesToFloat(this.data, this.readPos);
                         this.readPos += 4;
                         return result;
                     }
                
                     private int readInt() {
                         int result = BitKit.bytesToInt(this.data, this.readPos);
                         this.readPos += 4;
                         return result;
                     }
                
                     private long readLong() {
                         long result = BitKit.bytesToLong(this.data, this.readPos);
                         this.readPos += 8;
                         return result;
                     }
                
                     private byte readByte() {
                         byte result = this.data[this.readPos];
                         this.readPos += 1;
                         return result;
                     }
                
                     private boolean readBoolean() {
                         boolean result = this.data[this.readPos] != 0;
                         this.readPos += 1;
                         return result;
                     }
                
                     private char readChar() {
                         char element = BitKit.bytesToChar(this.data, this.readPos);
                         this.readPos += 2;
                         return element;
                     }
                
                     private short readShort() {
                         short element = BitKit.bytesToShort(this.data, this.readPos);
                         this.readPos += 2;
                         return element;
                     }
                
                     private double readDouble() {
                         double result = BitKit.bytesToDouble(this.data, this.readPos);
                         this.readPos += 8;
                         return result;
                     }
                
                     private java.util.Date readDate() {
                         long filetime = BitKit.bytesToLong(this.data, this.readPos);
                         this.readPos += 8;
                         return new java.util.Date((filetime / 10000L) - 11644473600000L);
                     }
                
                     private <T> List<T> readRepeated(java.util.function.Supplier<T> reader) {
                         int count = this.readByte();
                         List<T> list = new ArrayList<>(count);
                         for (int i = 0; i < count; i++) {
                             list.add(reader.get());
                         }
                         return list;
                     }
                
                     private <T> List<T> readRepeated(java.util.function.Supplier<T> reader, int len) {
                         List<T> list = new ArrayList<>(len);
                         for (int i = 0; i < len; i++) {
                             list.add(reader.get());
                         }
                         return list;
                     }
                
                     private byte[] readBytes(int len) {
                         if (this.readPos + len > this.data.length)
                             len = data.length - readPos;
                
                         byte[] bytes = new byte[len];
                         BitKit.blockCopy(this.data, this.readPos, bytes, 0, len);
                         this.readPos += len;
                         return bytes;
                     }
                
                     private byte[] readBytes() {
                         int len = this.metaData.dataLen - this.readPos;
                         return readBytes(len);
                     }

                """;
    }

    private static String writeToStringFunc(List<Field> fields, String className) {
        final boolean isDefaultPacket = className.equals("CMSGDefault");
        StringBuilder sb = new StringBuilder();
        sb.append("    @Override\n");
        sb.append("    public String toString() {\n");
        sb.append("        return \"" + className + " {\" +\n");
        sb.append("            \" \\\"id\\\": \\\"\" + String.format(\"0x%X\", (int) this.metaData.packetId) + \"\\\",\" +\n");
        sb.append("            \" \\\"len\\\": \" + (int) this.metaData.dataLen + \",\" +\n");
        sb.append("            \" \\\"data\\\": \" + ");

        if (isDefaultPacket) {
            sb.append("BitKit.toString(this.data, 0, this.metaData.dataLen)");
        } else {
            sb.append(toJsonObject(fields, "this"));
        }

        sb.append(" + \" }\";\n");
        sb.append("    }\n");
        return sb.toString();
    }

    private static String toJsonObject(List<Field> fields, String var) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"{\"");
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            sb.append(" + \" \\\"").append(f.name).append("\\\": \" + ").append(toJsonExpr(var, f));
            if (i < fields.size() - 1)
                sb.append(" + \",\"");
        }
        sb.append(" + \" }\"");
        return sb.toString();
    }

    private static String toJsonExpr(String parent, Field f) {
        String ref = parent + "." + f.name;

        // Primitive or simple type
        if (supportedType(f.type)) {
            if (f.type.equals("string")) {
                return "(" + ref + " != null ? \"\\\"\" + " + ref + " + \"\\\"\" : \"null\")";
            }
            if (f.type.equals("bytes")) {
                return "\"\\\"\" + BitKit.toString(" + ref + ", 0, " + ref + ".length) + \"\\\"\"";
            }
            if (f.repeated && !f.type.equals("bytes")) {
                return "(" + ref + " != null ? " + ref + ".toString() : \"null\")";
            }

            if (f.type.equals("char")) {
                return "(int) " + ref;
            }

            return ref;
        }

        // Composite / nested type
        if (parsedMessages.containsKey(f.type)) {
            if (f.repeated && !f.type.equals("bytes")) {
                String p = deriveLambdaName(f.type);
                return "(" + ref + " != null ? " +
                        ref + ".stream().map(" + p + " -> " +
                        toStringComposite(p, f.type) +
                        ").collect(java.util.stream.Collectors.joining(\", \", \"[\", \"]\")) : \"null\")";
            } else {
                return "(" + ref + " != null ? " + toStringComposite(ref, f.type) + " : \"null\")";
            }
        }

        // Fallback
        return "(" + ref + " != null ? String.valueOf(" + ref + ") : \"null\")";
    }

    private static String toStringComposite(String var, String type) {
        if (!parsedMessages.containsKey(type)) return var;

        StringBuilder sb = new StringBuilder();
        sb.append("\"{\"");
        List<Field> subs = parsedMessages.get(type);
        for (int i = 0; i < subs.size(); i++) {
            Field sub = subs.get(i);
            String getter = var + ".get" +
                    Character.toUpperCase(sub.name.charAt(0)) +
                    sub.name.substring(1) + "()";

            sb.append(" + \"\\\"").append(sub.name).append("\\\": \" + ");

            if (supportedType(sub.type)) {
                if (sub.type.equals("string")) {
                    sb.append("(").append(getter)
                            .append(" != null ? \"\\\"\" + ").append(getter).append(" + \"\\\"\" : \"null\")");
                } else if (sub.type.equals("bytes")) {
                    sb.append("\"\\\"\" + BitKit.toString(").append(getter).append(", 0, ").append(getter).append(".length) + \"\\\"\"");
                } else if (sub.repeated && !sub.type.equals("bytes")) {
                    sb.append("(").append(getter).append(" != null ? ").append(getter).append(".toString() : \"null\")");
                } else {
                    if (sub.type.equals("char")) {
                        sb.append("(int) ");
                    }
                    sb.append(getter);
                }
            } else if (parsedMessages.containsKey(sub.type)) {
                if (sub.repeated && !sub.type.equals("bytes")) {
                    String p = deriveLambdaName(sub.type);
                    sb.append("(").append(getter).append(" != null ? ")
                            .append(getter)
                            .append(".stream().map(").append(p).append(" -> ")
                            .append(toStringComposite(p, sub.type))
                            .append(").collect(java.util.stream.Collectors.joining(\", \", \"[\", \"]\")) : \"null\")");
                } else {
                    sb.append(toStringComposite(getter, sub.type));
                }
            } else {
                sb.append("(").append(getter)
                        .append(" != null ? String.valueOf(").append(getter).append(") : \"null\")");
            }

            if (i < subs.size() - 1)
                sb.append(" + \", \"");
        }
        sb.append(" + \"}\"");
        return sb.toString();
    }


    private static void writeGetterSetter(PrintWriter out, List<Field> fields) {
        for (Field f : fields) {
            String t = mapType(f);
            String capName = Character.toUpperCase(f.name.charAt(0)) + f.name.substring(1);
            if (f.repeated && !f.type.equals("bytes")) {
                out.printf("    public List<%s> get%s() { return this.%s; }%n", t, capName, f.name);
                out.printf("    public void set%s(List<%s> %s) { this.%s = %s; }%n", capName, t, f.name, f.name, f.name);
            } else {
                out.printf("    public %s get%s() { return this.%s; }%n", t, capName, f.name);
                out.printf("    public void set%s(%s %s) { this.%s = %s; }%n", capName, t, f.name, f.name, f.name);
            }
        }
    }

    private static void validateFieldOrder(String msgName, String body) {
        Matcher fieldNumMatcher = Pattern.compile("=\\s*(\\d+)\\s*(?:;|\\[|$)").matcher(body);
        List<Integer> numbers = new ArrayList<>();
        while (fieldNumMatcher.find()) {
            numbers.add(Integer.parseInt(fieldNumMatcher.group(1)));
        }

        if (numbers.isEmpty()) return;

        Set<Integer> set = new HashSet<>(numbers);
        if (set.size() != numbers.size()) {
            throw new IllegalStateException("[FTPacketGen] Duplicate field numbers in message " + msgName);
        }

        int expected = 1;
        for (int num : numbers) {
            if (num != expected) {
                throw new IllegalStateException("[FTPacketGen] Missing field number " + expected + " in message " + msgName);
            }
            expected++;
        }
    }

    private record Field(String type, String name, boolean repeated, Map<String, String> options) {
    }

    private record GeneratedClass(String packageName, int packetId) {
    }
}
