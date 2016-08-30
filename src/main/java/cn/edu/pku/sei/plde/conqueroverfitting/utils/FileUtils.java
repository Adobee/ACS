package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class FileUtils {
	public static void writeFile(String fileName, String content) {
		try {
			File outputFile = new File(fileName);
			if (!outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			if (outputFile.exists())
				outputFile.delete();
			outputFile.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write(content + "\r\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getJavaFilesInProj(String projectPath) {
		File file = new File(projectPath);
		ArrayList<String> filesPath = new ArrayList<String>();
		LinkedList<File> fileList = new LinkedList<File>();
		fileList.add(file);
		while (!fileList.isEmpty()) {
			File firstFile = fileList.removeFirst();
			File[] subFiles = firstFile.listFiles();
			if (subFiles == null){
				continue;
			}
			for (File subFile : subFiles) {
				if (subFile.isDirectory()) {
					fileList.add(subFile);
				} else if (subFile.getName().endsWith(".code") ||subFile.getName().endsWith(".java") ) {
					filesPath.add(subFile.getAbsolutePath());
				}
			}
		}
		return filesPath;
	}

	public static String getMD5(String s) {
		char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		try {
			byte[] btInput = s.getBytes("utf-8");
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getFileAddressOfJava(String srcPath, String className){
		if (className.contains("<") && className.contains(">")){
			className = className.substring(0, className.indexOf("<"));
		}
		return  srcPath.trim() + System.getProperty("file.separator") + className.trim().replace('.',System.getProperty("file.separator").charAt(0))+".java";
	}

    public static String getFileAddressOfClass(String classPath, String className){
		if (className.contains("<") && className.contains(">")){
			className = className.substring(0, className.indexOf("<"));
		}
        return  classPath.trim() + System.getProperty("file.separator") + className.trim().replace('.',System.getProperty("file.separator").charAt(0))+".class";
    }

    public static String getCodeFromFile(String srcPath, String className){
        return getCodeFromFile(getFileAddressOfJava(srcPath, className));
    }
    public static String getCodeFromFile(File file) {
        return getCodeFromFile(file.getAbsolutePath());
    }


    public static String getCodeFromFile(String fileaddress){
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File(fileaddress));
			byte[] b=new byte[stream.available()];
			int len = stream.read(b);
			if (len <= 0){
				throw new IOException("Source code file "+fileaddress+" read fail!");
			}
			stream.close();
			return new String(b);
		} catch (Exception e){
			System.out.println(e.getMessage());
			return "";
		} finally {
			if (stream!=null){
				try {
					stream.close();
				} catch (IOException e){
				}
			}
		}
	}

	public static String getTestFunctionCodeFromCode(String code, String targetFunctionName, String testSrcPath){
		String result = getTestFunctionCodeFromCode(code, targetFunctionName);
		if (result.equals("")){
			if (code.contains(" extends ")){
				String extendsClass = code.split(" extends ")[1].substring(0, code.split(" extends ")[1].indexOf("{"));
				String className = CodeUtils.getClassNameOfImportClass(code, extendsClass);
				if (className.equals("")){
					className = CodeUtils.getPackageName(code)+"."+extendsClass;
				}
				String extendsCode = FileUtils.getCodeFromFile(testSrcPath, className.trim());
				if (!extendsCode.equals("")){
					return getTestFunctionCodeFromCode(extendsCode, targetFunctionName, testSrcPath);
				}
			}
		}
		return result;
	}

	public static String getTestFunctionCodeFromCode(String code, String targetFunctionName) {
		if (code.contains("@Test")){
			String[] tests = code.split("@Test");
			for (String test: tests){
				if (test.contains("public void "+targetFunctionName+"()")){
					if (test.contains("private void ")){
						test = test.split("private void ")[0];
					}
					return test;
				}
			}
		}
		else {
			List<String> tests = divideTestFunction(code);
			for (String test: tests){
				if (test.trim().startsWith(targetFunctionName+"()")){
					return "public void"+ test.trim();
				}
			}
		}
		return "";
	}
	public static String getTestFunctionBodyFromCode(String code, String targetFunctionName, String testSrcPath) {
		String methodString = getTestFunctionCodeFromCode(code, targetFunctionName, testSrcPath);
		methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
		while (methodString.startsWith("\n")){
			methodString = methodString.substring(1);
		}
		while (methodString.endsWith("\n")){
			methodString = methodString.substring(0, methodString.length()-1);
		}
		while (methodString.split("\n")[0].trim().equals("") || methodString.split("\n")[0].trim().startsWith("//")){
			methodString = methodString.substring(methodString.indexOf("\n")+1);
		}
		return methodString;
	}



	public static String getTestFunctionBodyFromCode(String code, String targetFunctionName) {
		String methodString = getTestFunctionCodeFromCode(code, targetFunctionName);
		if (!methodString.contains("{") || !methodString.contains("}")){
			return "";
		}
		methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
		while (methodString.startsWith("\n")){
			methodString = methodString.substring(1);
		}
		while (methodString.endsWith("\n")){
			methodString = methodString.substring(0, methodString.length()-1);
		}
		if (!methodString.contains("\n")){
			return methodString;
		}
		while (methodString.split("\n")[0].trim().equals("") || methodString.split("\n")[0].trim().startsWith("//")){
			methodString = methodString.substring(methodString.indexOf("\n")+1);
		}
		return methodString;
	}


	public static List<Integer> getTestFunctionLineFromCode(String code, String targetFunctionName) throws NotFoundException{
		List<Integer> result = new ArrayList<>();
		if (code.contains("@Test")){
			String[] tests = code.split("@Test");
			for (String test: tests){
				if (test.contains("private void ")){
					test = test.split("private void ")[0];
				}
				if (test.contains("public void "+targetFunctionName+"()")){
					int firstLine = getLineNumberOfLine(code,test.split("\n")[1]);
					result.add(firstLine);
					if (test.contains("/**")){
						test = test.substring(0, test.indexOf("/**"));
					}
					test = test.substring(test.indexOf('{'), test.lastIndexOf('}'));
					result.add(firstLine+test.split("\n").length-2);
					break;
				}
			}
		}
		else {
			List<String> tests = divideTestFunction(code);
			for (String test: tests){
				if (test.trim().startsWith(targetFunctionName+"()")){
					int firstLine = getLineNumberOfLine(code,"public void"+test.split("\n")[0]);
					result.add(firstLine);
					result.add(firstLine+test.split("\n").length-1);
					break;
				}
			}
		}
		if (result.size() == 0){
			throw new NotFoundException("Target function: "+ targetFunctionName+ " No Found");
		}
		return result;
	}

	public static int getLineNumberOfLine(String code, String targetLine){
		int lineNum = 0;
		for (String line: code.split("\n")){
			if (targetLine.trim().equals(line.trim())){
				return ++lineNum;
			}
			++lineNum;
		}
		return -1;
	}


	public static List<String> getPackageImportFromCode(String code){
		List<String> result = new ArrayList<>();
		for (String line: code.split("\n")){
			if (line.startsWith("import")){
				result.add(line);
			}
		}
		return result;
	}

	private static List<String> divideTestFunction(String code){
		List<String> result = new ArrayList<String>();
		code = code.replaceAll("private void","public void");
		String[] items = code.split("public void");
		for (int j = 1; j<items.length; j++){
			String item = items[j];
			int startPoint = item.indexOf('{')+1;
			int braceCount = 1;
			for (int i=startPoint; i<item.length();i++){
				if (item.charAt(i) == '}'){
					if (--braceCount == 0){
						result.add(item.substring(0, i+1));
						break;
					}
				}
				if (item.charAt(i) == '{'){
					braceCount++;
				}
			}
		}
		return result;
	}

	public static File createFile(String filePath) throws IOException {
		File file = new File(filePath);
		if(! file.exists()) {
			makeDir(file.getParentFile());
			file.createNewFile();
		}
		return file;
	}

	/**
	 * Enhancement of java.io.File#mkdir()
	 * Create the given directory . If the parent folders don't exists, we will create them all.
	 * @see java.io.File#mkdir()
	 * @param dir the directory to be created
	 */
	private static void makeDir(File dir) {
		if(! dir.getParentFile().exists()) {
			makeDir(dir.getParentFile());
		}
		dir.mkdir();
	}


    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
        // The directory is now empty so now it can be smoked
        dir.delete();
    }



    public static File copyFile(File src, File dst){
        return copyFile(src.getAbsolutePath(), dst.getAbsolutePath());
    }

    public static File copyFile(String srcPath, String dstPath){
		FileInputStream input=null;
		FileOutputStream output=null;
        try{
            input=new FileInputStream(srcPath);
            output=new FileOutputStream(dstPath);
            int in=input.read();
            while(in!=-1){
                output.write(in);
                in=input.read();
            }
        }catch (IOException e){
            System.out.println(e.toString());
        }  finally {
			if (input != null){
				try {
					input.close();
				} catch (IOException e){
				}
			}
			if (output != null){
				try {
					output.close();
				} catch (IOException e){
				}
			}
		}
        return new File(dstPath);
    }


	public static String tempJavaPath(String classname, String identifier){
		new File(System.getProperty("user.dir")+"/temp/"+identifier).mkdirs();
		return System.getProperty("user.dir")+"/temp/"+identifier+"/"+classname.substring(classname.lastIndexOf(".")+1)+".java";
	}

	public static String tempClassPath(String classname, String identifier){
		new File(System.getProperty("user.dir")+"/temp/"+identifier).mkdirs();
		return System.getProperty("user.dir")+"/temp/"+identifier+"/"+classname.substring(classname.lastIndexOf(".")+1)+".class";
	}


	public static boolean copyDirectory(String src, String dst){
		if (!new File(src).isDirectory()){
			return false;
		}
		try {
			String result = ShellUtils.shellRun(Arrays.asList("cp -rf "+src+" "+dst));
			System.out.println(result);
			return true;
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return false;
	}

	public static boolean deleteDirNow(String path){
		if (!path.contains(System.getProperty("user.dir")) || path.equals(System.getProperty("user.dir"))){
			return false;
		}
		if (!new File(path).isDirectory()){
			return false;
		}
		try {
			String result = ShellUtils.shellRun(Arrays.asList("rm -rf "+path));
			System.out.println(result);
			return true;
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return false;
	}

	public static void unzip(String zipfile, String unzipFilePath) throws IOException{
        ZipFile zf = new ZipFile(zipfile);
        for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            String zipEntryName = entry.getName();
            if (entry.isDirectory()){
                new File(unzipFilePath + zipEntryName).mkdirs();
                continue;
            }
            InputStream in = zf.getInputStream(entry);
            OutputStream out = new FileOutputStream(unzipFilePath + zipEntryName);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
            //System.out.println("解压缩完成.");
        }
	}
}
