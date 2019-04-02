package cz.cuni.mff.mymockito;

import com.sun.istack.internal.NotNull;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

class MyMockito {

    static class Triple {
        Object o;
        Method m;
        Object returnValue;

        Triple(Object o, Method m, Object returnValue) {
            this.o = o;
            this.m = m;
            this.returnValue = returnValue;
        }
    }

    private static Triple[] triples = new Triple[10];


    private static Method lastMethod;
    private static Object lastObject;

    static <T> MyWhen<T> when(T t) {
        return new MyWhen<>(lastObject, lastMethod);
    }

    static class MyWhen<T> {

        private Object o;
        private Method m;
        private int counter = 0;
        MyWhen(Object o, Method m) {
            this.o = o;
            this.m = m;
        }

        void thenReturn(T t) {
            triples[counter++] = new Triple(o,m,t);
        }
    }


    static < T > T mock(Class<T> classObject){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(classObject); // parameter of mock method
        enhancer.setCallback(new MyMockClassCallback());
        //noinspection unchecked
        return (T) enhancer.create(); // T is template arg of mock method
    }

    private static class MyMockClassCallback implements MethodInterceptor {

        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable{
            lastMethod = method;
            lastObject = o;

            for (Triple t : triples) {
                if (t == null) {
                    continue;
                }
                if (t.o == o && t.m == method) {
                    return t.returnValue;
                }
            }

            return methodProxy.invokeSuper(o, args);
        }
    }

}