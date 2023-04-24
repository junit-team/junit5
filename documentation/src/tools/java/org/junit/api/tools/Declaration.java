package org.junit.api.tools;

public sealed interface Declaration permits Type, Method {
    String getPackageName();
    String getName();
    String getKind();
}

record Type(Class<?> clazz) implements Declaration {
    @Override
    public String getPackageName() {
        return clazz.getPackageName();
    }

    @Override
    public String getName() {
        var typeName = clazz.getCanonicalName();
        var packageName = getPackageName();
        if (typeName.startsWith(packageName + '.')) {
            typeName = typeName.substring(packageName.length() + 1);
        }
        return typeName;
    }

    @Override
    public String getKind() {
        switch (clazz) {
            case Class<?> c && c.isRecord() -> return "record";
        }
        if (clazz.isAnnotation()) {
            return "annotation";
        }
        if (clazz.isEnum()) {
            return "enum";
        }
        if (clazz.isInterface()) {
            return "interface";
        }
        return "class";

    }
}

record Method(java.lang.reflect.Method method) implements Declaration {
}
