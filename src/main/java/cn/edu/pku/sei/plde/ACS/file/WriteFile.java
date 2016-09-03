package cn.edu.pku.sei.plde.ACS.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class WriteFile {
	public static void writeFile(String fileName, String content) {
		try {
			File outputFile = new File(fileName);
			if (!outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			if (outputFile.exists())
				outputFile.delete();
			outputFile.createNewFile();
			FileWriter fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content + "\r\n");
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}