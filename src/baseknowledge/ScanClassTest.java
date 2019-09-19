package baseknowledge;

import com.annotation.Controller;
import com.annotation.RequestMapping;
import com.util.RequestMappingMap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScanClassTest {

    //发现文件夹下所有文件
    public static Set<Class<?>> getAllClazzByPackagename(String packageName) {

        //是否遍历下一级目录的开关
        boolean recursive = true;

        //容器
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        String pack = packageName;
        String packDirname = pack.replace(".", "/");
        System.out.println("pack:" + pack);
        //
        Enumeration<URL> resources = null;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources(packDirname);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                //System.out.println(protocol);
                if (protocol.equals("file")) {
                    System.out.println(protocol + ":类型扫描！");
                    // 获取包的物理路径  /F:/work_idea/mybatis/out/production/springmvc_simulation/com/controller
                    String file = url.getFile();
                    // System.out.println("file:"+file);
                    String filePath = URLDecoder.decode(file, "UTF-8");
                    //System.out.println(filePath);
                    findAndAddClassesInPackageByFile(pack, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    System.out.println(protocol + ":类型扫描！");
                    JarFile jarFile;
                    JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
                    jarFile = urlConnection.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        //便利包内所有文件
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        System.out.println(name);
                        System.out.println(name.indexOf("/"));
                        String jarPackName = null;
                        //与配置的包前缀一致的文件
                        if (name.startsWith(packDirname)) {
                            int idx = name.lastIndexOf("/");//获取文件夹   最后一个/的位置
                            System.out.println(idx);
                            if (idx != -1) {
                                //文件夹
                                jarPackName = name.substring(0, idx).replace('/', '.');
                            }
                            if ((idx != -1) || recursive) {
                                if (name.endsWith(".class") && !entry.isDirectory()) {//class文件
                                    //获得类名
                                    String className = name.substring(jarPackName.length() + 1, name.length() - 6);
                                    System.out.println(className);
                                    System.out.println(jarPackName + "." + className);
                                    try {
                                        classes.add(Class.forName(jarPackName + "." + className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        }
                    }
                }
            }
            for (Class<?> aClass : classes) {
                System.out.println(aClass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;

    }

    /**
     * @param pack      com.controller
     * @param filePath  /F:/work_idea/mybatis/out/production/springmvc_simulation/com/controller
     * @param recursive 开关
     * @param classes   集合类
     */
    private static void findAndAddClassesInPackageByFile(String pack, String filePath, final boolean recursive, Set<Class<?>> classes) {

        //物理路径判断咯
        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            //文件路径不存在 或者不是目录
            return;
        }
        //过滤文件夹
        File[] filearray = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File filex) {
                //过滤规则 文件夹或者 .class文件
                return (recursive && filex.isDirectory()) || ((filex.getName().endsWith(".class")));
            }
        });
        System.out.println(filearray.length);
        //遍历查询下全部文件
        for (File file1 : filearray) {
            //System.out.println("所有包下文件名称：" + file1.getName());
            //System.out.println("所有包下文件名称：" + file1.getName().substring(0,file1.getName().length()-6));
            //获得类全名
            System.out.println("所有包下文件名称：" + pack + "." + file1.getName().substring(0, file1.getName().length() - 6));
            String className = file1.getName().substring(0, file1.getName().length() - 6);
            if (file1.isDirectory()) {//文件夹
                findAndAddClassesInPackageByFile(pack + "." + file1.getName(), file1.getAbsolutePath(), recursive, classes);
            } else {
                //文件  将文件加入集合中
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(pack + '.' + className));
                } catch (ClassNotFoundException e) {
                    System.out.println(pack + '.' + className + "类名不存在!");
                    //e.printStackTrace();
                }
            }
        }
        //System.out.println("------------读取容器开始--------------");
        //for (Class<?> aClass : classes) {
        //    System.out.println(aClass.getName());
        //}
        //System.out.println("-------------读取容器结束-------------");
    }

    public static void main(String[] args) {
        String packageName = "com.controller";
        Set<Class<?>> setClasses = getAllClazzByPackagename(packageName);
        System.out.println(setClasses.size());
        for (Class setClass : setClasses) {//遍历class容器
            //类上是否有controller注解
            if (setClass.isAnnotationPresent(Controller.class)) {
                //遍历类上所有方法
                Method[] methods = setClass.getDeclaredMethods();
                //判断方法上是否有requestMapping注解
                for (Method method : methods) {
                    //判断方法上是否有requestmapping注解
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        //获得注解的value值
                        String anoValue = method.getAnnotation(RequestMapping.class).value();
                        if (RequestMappingMap.getRequesetMap().containsKey(anoValue)){
                           throw new RuntimeException(anoValue+"该注解标记路径重复了！");
                        }
                        RequestMappingMap.getRequesetMap().put(anoValue,setClass);
                    }
                }
            }
        }
        System.out.println("所有注解方法容器："+RequestMappingMap.getRequesetMap());
    }

}
