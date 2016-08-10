package com.badlogicgames.tabsinspace;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by badlogic on 10/08/16.
 */
public class LicensesInSpace {
    interface FileProcessor {
        boolean process(File file) throws IOException;
    }

    public static void walkFiles(File dir, TabsInSpace.FileProcessor processor) throws Exception {
        if (!processor.process(dir)) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                walkFiles(f, processor);
            } else {
                processor.process(f);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: licensesinspace <licensefile> <startline> <middleline> <endline> <dir> (<extension1> <extension2> ...)?");
            System.exit(-1);
        }

        File licenseFile = new File(args[0]);
        if (!licenseFile.exists()) throw new Exception("License file doesn't exist");
        String license = FileUtils.readFileToString(licenseFile, "UTF-8");

        String startLine = args[1];
        String midLine = args[2];
        String endLine = args[3];

        File dir = new File(args[4]);
        if (!dir.exists()) throw new Exception("Directory doesn't exist");
        final List<String> extensions = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            extensions.add(args[i]);
        }

        StringBuffer b = new StringBuffer();
        b.append(startLine);
        b.append("\n");
        for (String line: license.split("\n")) {
            b.append(midLine);
            b.append(line);
            b.append("\n");
        }
        b.append(endLine);
        b.append("\n\n");
        String fixedLicense = b.toString();

        walkFiles(dir, (file) -> {
            if (file.isDirectory()) return true;
            if (!extensions.isEmpty()) {
                boolean found = false;
                for (String ext : extensions) {
                    if (file.getName().endsWith(ext)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return true;
            }

            String content = FileUtils.readFileToString(file, "UTF-8");
            if (!content.startsWith(startLine)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(fixedLicense);
                buffer.append(content);
                FileUtils.write(file, buffer, "UTF-8");
                System.out.println(file);
            } else {
                int index = content.indexOf(endLine);
                if (index == -1) {
                    System.out.println("Couldn't find end line in file " + file.getAbsolutePath());
                } else {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(fixedLicense);
                    buffer.append(content.substring(index + endLine.length() + 2));
                    FileUtils.write(file, buffer, "UTF-8");
                    System.out.println(file);
                }
            }
            return true;
        });
    }
}
