package cn.edu.pku.sei.plde.conqueroverfitting.localization.common;

/**
 * Created by localization on 16/1/5.
 */
import sacha.finder.classes.ClassFinder;
import sacha.finder.filters.ClassFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TestFinder{

    static final int CLASS_SUFFIX_LENGTH = ".class".length();
    static final int JAVA_SUFFIX_LENGTH = ".java".length();
    private final ClassFilter tester;
    private final ClassFinder finder;

    public TestFinder(ClassFinder finder, ClassFilter tester) {
        this.tester = tester;
        this.finder = finder;
    }

    public Class<?>[] process() {
        ArrayList classes = new ArrayList();
        String[] var2 = this.finder.getClasses();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String fileName = var2[var4];
            String className;
            if(this.isJavaFile(fileName)) {
                className = this.classNameFromJava(fileName);
            } else {
                if(!this.isClassFile(fileName)) {
                    continue;
                }

                className = this.classNameFromFile(fileName);
            }

            if(this.tester.acceptClassName(className) && (this.tester.acceptInnerClass() || !this.isInnerClass(className)) && !className.contains("$")) {
                try {
                    Class ncdfe = Class.forName(className);
                    if(!ncdfe.isLocalClass() && !ncdfe.isAnonymousClass() && this.tester.acceptClass(ncdfe)) {
                        classes.add(ncdfe);
                    }
                } catch (ClassNotFoundException var12) {
                    try {
                        ClassLoader ncdfe1 = Thread.currentThread().getContextClassLoader();
                        Class clazz = Class.forName(className, false, ncdfe1);
                        if(!clazz.isLocalClass() && !clazz.isAnonymousClass() ){//&& this.tester.acceptClass(clazz)) {
                            classes.add(clazz);
                        }
                    } catch (ClassNotFoundException var10) {
                        var10.printStackTrace();
                    } catch (NoClassDefFoundError var11) {
                        ;
                    }
                } catch (NoClassDefFoundError var13) {
                    ;
                }
            }
        }
        Collections.sort(classes, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Class<?>)o1).getName().compareTo(((Class<?>)o2).getName());
            }
        });
        return (Class[])classes.toArray(new Class[0]);
    }

    private String classNameFromJava(String fileName) {
        String s;
        for(s = this.replaceFileSeparators(this.cutOffExtension(fileName, JAVA_SUFFIX_LENGTH)); s.startsWith("."); s = s.substring(1)) {
            ;
        }

        return s;
    }

    private boolean isJavaFile(String fileName) {
        return fileName.endsWith(".java");
    }

    private boolean isInnerClass(String className) {
        return className.contains("$");
    }

    private boolean isClassFile(String classFileName) {
        return classFileName.endsWith(".class");
    }

    private String classNameFromFile(String classFileName) {
        String s;
        for(s = this.replaceFileSeparators(this.cutOffExtension(classFileName, CLASS_SUFFIX_LENGTH)); s.startsWith("."); s = s.substring(1)) {
            ;
        }

        return s;
    }

    private String replaceFileSeparators(String s) {
        String result = s.replace(File.separatorChar, '.');
        if(File.separatorChar != 47) {
            result = result.replace('/', '.');
        }

        return result;
    }

    private String cutOffExtension(String classFileName, int length) {
        return classFileName.substring(0, classFileName.length() - length);
    }
}
