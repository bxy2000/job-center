package com.boxy.job.core.util;

import com.boxy.job.core.log.JobLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *  1、内嵌编译器如"PythonInterpreter"无法引用扩展包，因此推荐使用java调用控制台进程方式"Runtime.getRuntime().exec()"来运行脚本(shell或python)；
 *  2、因为通过java调用控制台进程方式实现，需要保证目标机器PATH路径正确配置对应编译器；
 *  3、暂时脚本执行日志只能在脚本执行结束后一次性获取，无法保证实时性；因此为确保日志实时性，可改为将脚本打印的日志存储在指定的日志文件上；
 *  4、python 异常输出优先级高于标准输出，体现在Log文件中，因此推荐通过logging方式打日志保持和异常信息一致；否则用prinf日志顺序会错乱
 */
public class ScriptUtil2 {

    /**
     * make script file
     *
     * @param scriptFileName
     * @param content
     * @throws IOException
     */
    public static void markScriptFile(String scriptFileName, String content) throws IOException {
        // make file,   filePath/gluesource/666-123456789.py
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(scriptFileName);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (Exception e) {
            throw e;
        }finally{
            if(fileOutputStream != null){
                fileOutputStream.close();
            }
        }
    }

    /**
     * 脚本执行，日志文件实时输出
     *
     * @param command
     * @param scriptFile
     * @param logFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execToFile(String command, String scriptFile, String logFile, String... params) throws IOException {

//        FileOutputStream fileOutputStream = null;
        OutputStreamWriter streamWriter = null;
        Thread inputThread = null;
        Thread errThread = null;
        try {
            // file
//            fileOutputStream = new FileOutputStream(logFile, true);
            streamWriter = new OutputStreamWriter(new FileOutputStream(logFile, true));//, "UTF-8");

            // command
            List<String> cmdarray = new ArrayList<>();
            cmdarray.add(command);
            cmdarray.add(scriptFile);
            if (params!=null && params.length>0) {
                for (String param:params) {
                    cmdarray.add(param);
                }
            }
            String[] cmdarrayFinal = cmdarray.toArray(new String[cmdarray.size()]);

            // process-exec
            final Process process = Runtime.getRuntime().exec(cmdarrayFinal);

            // log-thread
//            final FileOutputStream finalFileOutputStream = fileOutputStream;
            final OutputStreamWriter finalOutputStreamWriter = streamWriter;
            inputThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final InputStreamReader finalInputStreadReader = new InputStreamReader(process.getInputStream());
//                        copy(process.getInputStream(), finalFileOutputStream, new byte[1024]);
                        copy(finalInputStreadReader, finalOutputStreamWriter, new char[1024]);
                    } catch (IOException e) {
                        JobLogger.log(e);
                    }
                }
            });
            errThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        copy(process.getErrorStream(), finalFileOutputStream, new byte[1024]);
                        final InputStreamReader finalInputStreadReader = new InputStreamReader(process.getErrorStream());
                        copy(finalInputStreadReader, finalOutputStreamWriter, new char[1024]);
                    } catch (IOException e) {
                        JobLogger.log(e);
                    }
                }
            });
            inputThread.start();
            errThread.start();

            // process-wait
            int exitValue = process.waitFor();      // exit code: 0=success, 1=error

            // log-thread join
            inputThread.join();
            errThread.join();

            return exitValue;
        } catch (Exception e) {
            JobLogger.log(e);
            return -1;
        } finally {
            if (streamWriter != null) {
                try {
                    streamWriter.close();
                } catch (IOException e) {
                    JobLogger.log(e);
                }

            }
            if (inputThread != null && inputThread.isAlive()) {
                inputThread.interrupt();
            }
            if (errThread != null && errThread.isAlive()) {
                errThread.interrupt();
            }
        }
    }

    /**
     * 数据流Copy（Input自动关闭，Output不处理）
     */
    private static long copy(InputStreamReader streamReader, OutputStreamWriter streamWriter, char[] buffer) throws IOException {
        try {
            long total = 0;
            int read = -1;
            while((read = streamReader.read(buffer, 0, 1024)) != -1) {
                streamWriter.write(buffer, 0, read);
                total += read;
            }
            streamWriter.flush();
            //out = null;
            streamReader.close();
            streamReader = null;
            return total;
        } finally {
            if (streamReader != null) {
                streamReader.close();
            }
        }
    }
}
