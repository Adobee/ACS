package cn.edu.pku.sei.plde.ACS.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	private String source;

	public ReadFile(String filePath) {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);

			String line = "";
			line = br.readLine();
			while (line != null) {
				line = line.replaceAll("&nbsp;", " ");
				line = line.replaceAll("\t", " ");
				line = line.replaceAll("\\s{2,100}", " ");
				source = source + line + "\r\n";
				line = br.readLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e){

				}
			}
			if (fr != null){
				try {
					fr.close();
				} catch (IOException e){

				}
			}
		}
	}

	public String getSource() {
		return source;
	}
}
