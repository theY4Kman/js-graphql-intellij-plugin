package com.intellij.lang.jsgraphql.languageservice;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class NodeDetectionUtil {
    
    private static final String NODE_INTERPRETER_BASE_NAME;
    private static final FileFilter NODE_FILTER;

    private NodeDetectionUtil() {
    }

    @Nullable
    public static File findInterpreterInPath() {
        File interpreter = PathEnvironmentVariableUtil.findInPath(NODE_INTERPRETER_BASE_NAME, NODE_FILTER);
        if(interpreter != null) {
            return interpreter;
        } else {
            NodeDetectionUtil.NodeInterpreters interpreters = new NodeDetectionUtil.NodeInterpreters();
            addNodeInterpretersFromNvm(interpreters);
            listNodeInterpretersFromHomeBrew(interpreters);
            if(interpreters.myInterpreters.size() == 1) {
                List sortedInterpreters = interpreters.getSortedInterpreters();
                return (File)ContainerUtil.getFirstItem(sortedInterpreters);
            } else {
                return null;
            }
        }
    }

    private static void addNodeInterpretersFromNvm(@NotNull NodeDetectionUtil.NodeInterpreters interpreters) {
        String nvmDirPath = EnvironmentUtil.getValue("NVM_DIR");
        if(StringUtil.isEmpty(nvmDirPath)) {
            if(!SystemInfo.isUnix) {
                return;
            }

            nvmDirPath = SystemProperties.getUserHome() + "/.nvm";
        }

        File nvmDir = new File(nvmDirPath);
        if(nvmDir.isAbsolute()) {
            addNodeInterpretersFromVersionDir(interpreters, nvmDir, true);
            File versionsDir = new File(nvmDir, "versions");
            if(versionsDir.isDirectory()) {
                addNodeInterpretersFromVersionDir(interpreters, new File(versionsDir, "node"), true);
                addNodeInterpretersFromVersionDir(interpreters, new File(versionsDir, "io.js"), true);
            }

        }
    }

    private static void listNodeInterpretersFromHomeBrew(@NotNull NodeDetectionUtil.NodeInterpreters interpreters) {
        addNodeInterpretersFromVersionDir(interpreters, new File("/usr/local/Cellar/node"), true);
    }

    private static void addNodeInterpretersFromVersionDir(@NotNull NodeDetectionUtil.NodeInterpreters interpreters, @NotNull File parentDir, boolean insideBinDir) {
        if(parentDir.isDirectory()) {
            File[] dirs = parentDir.listFiles();
            if(dirs != null) {
                for(int i = 0; i < dirs.length; ++i) {
                    File dir = dirs[i];
                    SemVer semVer = parseSemVer(dir.getName());
                    if(semVer != null) {
                        String relativePath = insideBinDir?"bin" + File.separator + NODE_INTERPRETER_BASE_NAME:NODE_INTERPRETER_BASE_NAME;
                        File interpreter = new File(dir, relativePath);
                        if(interpreter.isFile() && interpreter.canExecute()) {
                            interpreters.add(interpreter, semVer);
                        }
                    }
                }

            }
        }
    }

    @Nullable
    private static SemVer parseSemVer(@NotNull String name) {
        if(name.startsWith("v")) {
            name = name.substring(1);
        }

        return SemVer.parseFromText(name);
    }


    static {
        NODE_INTERPRETER_BASE_NAME = SystemInfo.isWindows?"node.exe":"node";
        NODE_FILTER = pathname -> {
            String path;
            if(SystemInfo.isWindows) {
                path = pathname.getAbsolutePath();
                if(path.contains("Microsoft HPC Pack")) {
                    return false;
                }
            }

            if(SystemInfo.isUnix) {
                path = pathname.getAbsolutePath();
                if("/usr/sbin/node".equals(path)) {
                    return false;
                }
            }

            return true;
        };
    }

    private static class NodeInterpreter {
        private final File myInterpreter;
        private final SemVer mySemVer;

        public NodeInterpreter(@NotNull File interpreter, @Nullable SemVer semVer) {
            myInterpreter = interpreter;
            mySemVer = semVer;
        }

        @NotNull
        public File getInterpreter() {
            return myInterpreter;
        }

        @Nullable
        public SemVer getSemVer() {
            return mySemVer;
        }

        public String toString() {
            return myInterpreter.getAbsolutePath() + (mySemVer != null?", " + mySemVer:"");
        }
    }

    private static class NodeInterpreters {
        private final List<NodeDetectionUtil.NodeInterpreter> myInterpreters;

        private NodeInterpreters() {
            myInterpreters = ContainerUtil.newArrayList();
        }

        public void add(@NotNull File interpreterFile, @Nullable SemVer semVer) {
            if(semVer != null) {
                myInterpreters.add(new NodeDetectionUtil.NodeInterpreter(interpreterFile, semVer));
            }
        }

        @NotNull
        public List<File> getSortedInterpreters() {
            Collections.sort(myInterpreters, (interpreter1, interpreter2) -> {
                SemVer semVer1 = interpreter1.getSemVer();
                SemVer semVer2 = interpreter2.getSemVer();
                if(semVer1 != null && semVer2 != null) {
                    int res = semVer2.compareTo(semVer1);
                    if(res == 0) {
                        res = interpreter1.getInterpreter().getAbsolutePath().compareTo(interpreter2.getInterpreter().getAbsolutePath());
                    }

                    return res;
                } else {
                    return semVer1 == null && semVer2 == null?interpreter1.getInterpreter().getAbsolutePath().compareTo(interpreter2.getInterpreter().getAbsolutePath()):(semVer1 == null?-1:1);
                }
            });
            LinkedHashSet result = new LinkedHashSet(myInterpreters.size());

            NodeDetectionUtil.NodeInterpreter interpreter;
            for(Iterator iterator = myInterpreters.iterator(); iterator.hasNext(); result.add(interpreter.getInterpreter())) {
                interpreter = (NodeDetectionUtil.NodeInterpreter)iterator.next();
                if(result.contains(interpreter.getInterpreter())) {
                    result.remove(interpreter.getInterpreter());
                }
            }

            return ContainerUtil.newArrayList(result);
        }
    }
}
