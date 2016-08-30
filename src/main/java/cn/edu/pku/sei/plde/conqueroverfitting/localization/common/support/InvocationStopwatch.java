package cn.edu.pku.sei.plde.conqueroverfitting.localization.common.support;

import org.slf4j.Logger;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.ClassLibrary;

import java.lang.reflect.Method;

import static java.lang.String.format;
import static cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.LoggerLibrary.logInfo;
import static cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.LoggerLibrary.loggerFor;

public class InvocationStopwatch {

    public static Object invoke(Method method, Object receiver, Object... arguments) {
        long start = currentTime();
        Object result = ClassLibrary.invoke(method, receiver, arguments);
        long finish = currentTime();
        logInfo(logger(), format("[Invocation to '%s' on '%s': %d ms]", method.toString(), receiver.toString(), finish - start));
        return result;
    }

    private static long currentTime() {
        return System.currentTimeMillis();
    }

    protected static Logger logger() {
        return loggerFor(InvocationStopwatch.class);
    }
}
