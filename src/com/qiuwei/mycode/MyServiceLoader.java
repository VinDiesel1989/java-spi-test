package com.qiuwei.mycode;

import com.spi.facade.Logger;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 自己实现一个 ServiceLoader
 * <p>
 * 主要的流程就是：
 * <p>
 * 通过 URL 工具类从 jar 包的 /META-INF/services 目录下面找到对应的文件，
 * 读取这个文件的名称找到对应的 spi 接口，
 * 通过 InputStream 流将文件里面的具体实现类的全类名读取出来，
 * 根据获取到的全类名，先判断跟 spi 接口是否为同一类型，如果是的，那么就通过反射的机制构造对应的实例对象，
 * 将构造出来的实例对象添加到 Providers 的列表中
 *
 * @param <S>
 */
public class MyServiceLoader<S> {
    /**
     * 对应的接口Class模板
     */
    private final Class<S> service;
    /**
     * 对应实现类的 可以有多个，用 List 进行封装
     */
    private final List<S> providers = new ArrayList<>();
    /**
     * 类加载器
     */
    private final ClassLoader classLoader;

    public static <S> MyServiceLoader<S> load(Class<S> service) {
        return new MyServiceLoader<>(service);
    }

    private MyServiceLoader(Class<S> service) {
        this.service = service;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载具体的实现类 逻辑
     */
    private void doLoad() {
        try {
            // 读取所有 jar 包里面 META-INF/services 包下面的文件，这个文件名就是接口名，然后文件里面的内容就是具体的实现类的路径加全类名
            Enumeration<URL> urls = classLoader.getResources("META-INF/services/" + service.getName());
            // 挨个遍历取到的文件
            while (urls.hasMoreElements()) {

                // 取出当前的文件
                URL url = urls.nextElement();

                // 建立链接
                URLConnection urlConnection = url.openConnection();
                urlConnection.setUseCaches(false);

                //获取文件输入流
                InputStream inputStream = urlConnection.getInputStream();

                //从文件输入流获取缓存
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                //从文件内容里面得到实现类的全类名
                String className = bufferedReader.readLine();

                if (className != null) {
                    //通过反射拿到实现类的实例
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    // 如果声明的接口跟这个具体的实现类是属于同一类型，（可以理解为Java的一种多态，接口跟实现类、父类和子类等等这种关系。）则构造实例
                    if (service.isAssignableFrom(clazz)) {
                        Constructor<? extends S> constructor = (Constructor<? extends S>) clazz.getConstructor(null);
                        S instance = constructor.newInstance(null);
                        // 把当前构造的实例对象添加到 Provider的列表里面
                        providers.add(instance);

                    }
                }
                // 继续读取下一行的实现类，可以有多个实现类，只需要换行就可以了。
                className = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回spi接口对应的具体实现类列表
     *
     * @return
     */
    public List<S> getProviders() {
        return providers;
    }

    public static void main(String[] args) {
        MyServiceLoader serviceLoader = new MyServiceLoader(Logger.class);
        serviceLoader.doLoad();
        List<Logger> loggers = serviceLoader.getProviders();
        if (loggers != null && !loggers.isEmpty()) {
            Logger logger = loggers.get(0);
            logger.info("测试 自己实现的 ServiceLoader ， SPI机制的原理");
        }
    }
}
