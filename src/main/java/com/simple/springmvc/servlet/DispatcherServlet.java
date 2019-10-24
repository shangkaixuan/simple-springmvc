package com.simple.springmvc.servlet;

import com.simple.springmvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author srh
 * @date 2019/10/23
 **/
@WebServlet(name = "dispatcherServlet", urlPatterns = "/", loadOnStartup = 1, initParams = {
        @WebInitParam(name = "base-package", value = "com.simple.springmvc")
})
public class DispatcherServlet extends HttpServlet {

    /**
     * 扫描的基包
     */
    private String basePackage = "";
    /**
     * 基包下所有带包路径全类名
     */
    private List<String> packageNames = new ArrayList<String>();
    /**
     * 注解名称:实例对象
     */
    private Map<String, Object> annotationValInstanceMap = new HashMap<String, Object>();
    /**
     * 全类名:注解名称
     */
    private Map<String, String> packageAnnotationValMap = new HashMap<String, String>();
    /**
     * url:method
     */
    private Map<String, Method> urlMethodMap = new HashMap<String, Method>();
    /**
     * method:全类名
     */
    private Map<Method, String> methodPackageMap = new HashMap<Method, String>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.replaceAll(contextPath, "");

        Method method = urlMethodMap.get(path);
        if (null != method) {
            String packageName = methodPackageMap.get(method);
            String annotationName = packageAnnotationValMap.get(packageName);
            Object instance = annotationValInstanceMap.get(annotationName);

            method.setAccessible(true);
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        basePackage = config.getInitParameter("base-package");

        try {
            // 1. 扫描基包得到所有的全类名
            scanBasePackage(basePackage);
            // 2. 将 controller, service, repository 注解的实体放入map中
            fillInstant(packageNames);
            // 3. ioc 注入
            springIoc();
            // 4. 完成url与method之间的映射
            handlerUrlMethodMap();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void scanBasePackage(String basePackage) {
        // 将 com.simple.springmvc 包路径转换为绝对路径 com/simple/springmvc
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        assert url != null;
        File basePackageFile = new File(url.getPath());
        File[] files = basePackageFile.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                scanBasePackage(basePackage.concat(".").concat(file.getName()));
            } else if (file.isFile()) {
                packageNames.add(basePackage.concat(".").concat(file.getName().split("\\.")[0]));
            }
        }
    }

    private void fillInstant(List<String> packageNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (packageNames.size() < 1) {
            return;
        }
        for (String className : packageNames) {
            Class<?> clazz = Class.forName(className);

            if (clazz.isAnnotationPresent(Controller.class)) {
                String value = clazz.getAnnotation(Controller.class).value();
                annotationValInstanceMap.put(value, clazz.newInstance());
                packageAnnotationValMap.put(className, value);
            } else if (clazz.isAnnotationPresent(Service.class)) {
                String value = clazz.getAnnotation(Service.class).value();
                annotationValInstanceMap.put(value, clazz.newInstance());
                packageAnnotationValMap.put(className, value);
            } else if (clazz.isAnnotationPresent(Repository.class)) {
                String value = clazz.getAnnotation(Repository.class).value();
                annotationValInstanceMap.put(value, clazz.newInstance());
                packageAnnotationValMap.put(className, value);
            }
        }
    }

    private void springIoc() throws IllegalAccessException {
        for (Map.Entry<String, Object> entry : annotationValInstanceMap.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Quaifier.class)) {
                    String annotationVal = field.getAnnotation(Quaifier.class).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(), annotationValInstanceMap.get(annotationVal));
                }
            }
        }
    }

    private void handlerUrlMethodMap() throws ClassNotFoundException {
        if (packageNames.size() < 1) {
            return;
        }
        for (String className : packageNames) {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class)) {
                // 拼接类名上的mapping
                String typeUrlMapping = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    typeUrlMapping += clazz.getAnnotation(RequestMapping.class).value();
                }
                // 拼接method上的mapping
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        String methodUrlMapping = method.getAnnotation(RequestMapping.class).value();
                        urlMethodMap.put(mergeUrlMapping(typeUrlMapping, methodUrlMapping), method);
                        methodPackageMap.put(method, className);
                    }
                }
            }
        }
    }

    /**
     * todo 多种情况, 暂且以 /a/b 来约束
     *
     * @param typeUrlMapping
     * @param methodUrlMapping
     * @return
     */
    private String mergeUrlMapping(String typeUrlMapping, String methodUrlMapping) {
        StringBuilder sb = new StringBuilder();
        if (null != typeUrlMapping && typeUrlMapping.startsWith("/")) {
            sb.append(typeUrlMapping);
        }
        if (null != methodUrlMapping && methodUrlMapping.startsWith("/")) {
            sb.append(methodUrlMapping);
        }
        return sb.toString();
    }
}
