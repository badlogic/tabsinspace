package com.badlogicgames.tabsinspace;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabsInSpace {
	interface FileProcessor {
		boolean process(File file) throws IOException;
	}

	public static void walkFiles(File dir, FileProcessor processor) throws Exception {
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
			System.out.println("Usage: tabsinspaces <numspaces> <dir> (<extension1> <extension2> ...)?");
			System.exit(-1);
		}

		final int numSpaces = Integer.parseInt(args[0]);
		File dir = new File(args[1]);
		if (!dir.exists()) throw new Exception("File doesn't exist");
		final List<String> extensions = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			extensions.add(args[i]);
		}

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

			String[] lines = FileUtils.readFileToString(file, "UTF-8").split("\n");
			StringBuffer buffer = new StringBuffer();

			for (String line : lines) {
				int spaces = 0;
				boolean skip = false;
				for (char c : line.toCharArray()) {
					if (!skip && c == ' ') {
						spaces++;
						if (spaces == numSpaces) {
							spaces = 0;
							buffer.append("\t");
						}
					} else {
						for (int i = 0; i < spaces; i++) {
							buffer.append(" ");
						}
						spaces = 0;
						skip = true;
						buffer.append(c);
					}
				}
				buffer.append("\n");
			}
			FileUtils.write(file, buffer, "UTF-8");
			System.out.println(file);
			return true;
		});
	}
}
