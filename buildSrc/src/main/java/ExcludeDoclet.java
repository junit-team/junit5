import com.sun.javadoc.*;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Taken from http://sixlegs.com/blog/java/exclude-javadoc-tag.html
 * where it was declared public domain
 */
public class ExcludeDoclet {
    public static void main(String[] args) {
        String name = ExcludeDoclet.class.getName();
        Main.execute(name, name, args);
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter)
            throws java.io.IOException {
        return Standard.validOptions(options, reporter);
    }

    public static int optionLength(String option) {
        return Standard.optionLength(option);
    }

    public static boolean start(RootDoc root)
            throws java.io.IOException {
        return Standard.start((RootDoc) process(root, RootDoc.class));
    }

    private static boolean exclude(Doc doc) {
        if (doc instanceof ProgramElementDoc) {
            ProgramElementDoc programElementDoc = (ProgramElementDoc) doc;
            return isExcluded(programElementDoc) || exclude(programElementDoc.containingClass());
        }
        return false;
    }

    private static boolean isExcluded(ProgramElementDoc doc) {
        AnnotationDesc[] annotations = doc.annotations();
        Optional<AnnotationDesc> apiAnnotation = Arrays.stream(annotations)
				.filter(annotationDesc -> annotationDesc.annotationType()
						.qualifiedTypeName().equals("org.junit.platform.commons.meta.API")).findAny();
        return apiAnnotation.map(annotationDesc -> Arrays.stream(annotationDesc.elementValues())
                .filter(pair -> pair.element().name().equals("value"))
                .map(pair -> pair.value().value())
                .map(FieldDoc.class::cast)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Required 'value' annotation parameter was not present"))
                .name()
                .equals("Internal"))
                .orElse(false);
    }

    private static Object process(Object obj, Class expect) {
        if (obj == null) {
            return null;
        }
        Class cls = obj.getClass();
        if (cls.getName().startsWith("com.sun.")) {
            return Proxy.newProxyInstance(cls.getClassLoader(),
                    cls.getInterfaces(),
                    new ExcludeHandler(obj));
        } else if (obj instanceof Object[]) {
            if (expect.isArray()) {
                Class componentType = expect.getComponentType();
                Object[] array = (Object[]) obj;
                List<Object> list = new ArrayList<>(array.length);
                for (Object entry : array) {
                    if ((entry instanceof Doc) && exclude((Doc) entry))
                        continue;
                    list.add(process(entry, componentType));
                }
                return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
            } else {
                return obj;
            }
        } else {
            return obj;
        }
    }

    private static class ExcludeHandler
            implements InvocationHandler {
        private final Object target;

        ExcludeHandler(Object target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (args != null) {
                String methodName = method.getName();
                if (methodName.equals("compareTo") ||
                        methodName.equals("equals") ||
                        methodName.equals("overrides") ||
                        methodName.equals("subclassOf")) {
                    args[0] = unwrap(args[0]);
                }
            }
            try {
                return process(method.invoke(target, args), method.getReturnType());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private Object unwrap(Object proxy) {
            if (proxy instanceof Proxy)
                return ((ExcludeHandler) Proxy.getInvocationHandler(proxy)).target;
            return proxy;
        }
    }
}
