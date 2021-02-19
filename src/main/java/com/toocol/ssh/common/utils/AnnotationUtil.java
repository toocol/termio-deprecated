package com.toocol.ssh.common.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2020/4/27 12:51
 */
public final class AnnotationUtil {

    /**
     * 通过包名，注解类型获取所有标注了该注解的Class对象list
     * <p>
     * 基本的逻辑是通过packageName获取此包所在的文件夹地址
     * 然后获取此文件夹的File对象，从而可以获取到其的子文件和子文件夹
     * 递归获取所有符合条件的class的File对象
     * 通过packageName + File.getName获取类的全路径名称
     * 通过Class.forName()获取class对象
     *
     * @param packageName
     * @param annotationClazz
     * @return
     */
    public static List<Class<?>> getClassListByAnnotation(String packageName, Class<? extends Annotation> annotationClazz) {
        List<Class<?>> list = new ArrayList<>();
        try {
            Enumeration<URL> resource = Thread.currentThread().getContextClassLoader().getResources(packageName.replaceAll("\\.", "/"));
            if (resource.hasMoreElements()) {
                URL clazzUrl = resource.nextElement();
                // 获取类文件所在的文件目录
                String clazzPath = clazzUrl.getPath();

                addClassByAnnotation(list, packageName, clazzPath, annotationClazz);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void addClassByAnnotation(List<Class<?>> list, String packageName, String clazzPath, Class<? extends Annotation> annotationClazz) {
        // 获取该文件目录下所有类和文件夹的File对象
        File[] files = new File(clazzPath).listFiles((file) -> file.isFile() && file.getName().endsWith(".class") || file.isDirectory());

        // 获取所有类的全路径名称
        Arrays.stream(files).forEach(file -> {
            if (file.isFile()) {
                String name = file.getName().split("\\.")[0];
                String fullName = packageName + "." + name;
                try {
                    Class<?> targetClazz = Thread.currentThread().getContextClassLoader().loadClass(fullName);
                    Annotation annotation = targetClazz.getAnnotation(annotationClazz);
                    if (annotation != null) {
                        list.add(targetClazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                // 如果当前file是文件夹，继续进行递归
                String subPacageName = packageName + "." + file.getName();
                addClassByAnnotation(list, subPacageName, file.getPath(), annotationClazz);
            }
        });
    }
}
