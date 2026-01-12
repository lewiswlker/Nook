package com.hku.nook.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StreamChatManual {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java StreamChatManual <token> <message> [url]");
            System.out.println("Example: java StreamChatManual <token> \"列举三条今日国内新闻\" http://localhost:8080/ai/chat/stream");
            return;
        }
        String token = args[0];
        String message = args[1];
        String url = args.length >= 3 ? args[2] : "http://localhost:8080/ai/chat/stream";

        String payload = "{\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(message) + "\"}]}";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("token", token);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(payload);
        writer.flush();
        writer.close();

        int status = conn.getResponseCode();
        InputStreamReader inputStream = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        long start = System.currentTimeMillis();
        StringBuilder buffer = new StringBuilder();
        int[] chunkIndex = {0};
        StringBuilder raw = new StringBuilder();
        char[] chunk = new char[1024];
        int read;
        while ((read = inputStream.read(chunk)) != -1) {
            raw.append(chunk, 0, read);
            if (extractTokens(raw, buffer, start, chunkIndex)) {
                break;
            }
        }
        System.out.println();
        System.out.println("streamedChars=" + buffer.length() + ", costMs=" + (System.currentTimeMillis() - start) + ", http=" + status);
        inputStream.close();
        conn.disconnect();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String decodeJsonString(String data) {
        String text = data;
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                switch (c) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (i + 4 < text.length()) {
                            String hex = text.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append('u').append(hex);
                                i += 4;
                            }
                        } else {
                            sb.append('u');
                        }
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean extractTokens(StringBuilder raw, StringBuilder buffer, long start, int[] chunkIndex) {
        boolean done = false;
        while (true) {
            int dataIndex = raw.indexOf("data:\"\"");
            if (dataIndex < 0) {
                break;
            }
            int dataStart = dataIndex + 7;
            int end = raw.indexOf("\"\"", dataStart);
            if (end < 0) {
                break;
            }
            String payload = raw.substring(dataStart, end);
            String text = decodeJsonString(payload);
            if (!text.isEmpty()) {
                buffer.append(text);
                long elapsed = System.currentTimeMillis() - start;
                chunkIndex[0]++;
                System.out.print("[chunk " + chunkIndex[0] + " +" + elapsed + "ms] ");
                System.out.print(text);
                System.out.flush();
            }
            raw.delete(0, end + 2);
        }
        if (raw.indexOf("event:done") >= 0) {
            done = true;
        }
        return done;
    }
}
