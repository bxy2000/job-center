package com.boxy.job.core.util;

import com.boxy.job.core.log.JobLogger;
import org.springframework.util.Assert;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScriptUtil {
    public static void markScriptFile(String scriptFileName, String content) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(scriptFileName)) {
            fileOutputStream.write(content.getBytes("UTF-8"));
        } catch (IOException e) {
            throw e;
        }
    }

    public static int execToFile(String command, String scriptFile, String logFile, String... params) throws IOException {
        BufferedWriter writer = null;
        Thread inputThread = null;
        Thread errorThread = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));

            List<String> commands = new ArrayList<>();
            commands.add(command);
            commands.add(scriptFile);
            for (String param : params) {
                commands.add(param);
            }

            final Process process = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));

            final BufferedWriter finalWriter = writer;

            inputThread = new Thread(() -> {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                copy(reader, finalWriter);
            });

            errorThread = new Thread(() -> {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                copy(reader, finalWriter);
            });
            inputThread.start();
            errorThread.start();

            int exitValue = process.waitFor();

            return exitValue;
        } catch (Exception e) {
            JobLogger.log(e);
            return -1;
        } finally {
            if (writer != null) writer.close();
            if (inputThread != null) errorThread.interrupt();
            if (errorThread != null) errorThread.interrupt();
        }
    }

    public static int copy(BufferedReader in, BufferedWriter out) {
        try {
//            int byteCount = 0;
//            char[] buffer = new char[4096];
//
//            int bytesRead;
//            for (boolean var4 = true; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
//                out.write(buffer, 0, bytesRead);
//            }
//
//            out.flush();
//            int var5 = byteCount;
//            return var5;
            String line = null;
            while ((line = in.readLine()) != null) {
                if(line != "") {
                    out.write(line);
                    out.newLine();
                } else {
                    out.newLine();
                }
            }
            out.flush();
            return 0;
        } catch (IOException e) {
            JobLogger.log(e);
            return -1;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    JobLogger.log(e);
                }
            }
        }
    }
}
