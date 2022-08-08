package me.pesekjak.hippo.classes.builder;

import com.btk5h.skriptmirror.LibraryLoader;
import lombok.Getter;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.classtypes.SkriptAnnotation;
import me.pesekjak.hippo.classes.classtypes.SkriptEnum;
import me.pesekjak.hippo.classes.classtypes.SkriptRecord;
import me.pesekjak.hippo.classes.content.ClassContent;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.Method;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.skript.classes.annotations.DefaultValue;
import me.pesekjak.hippo.utils.Reflectness;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * ClassLoader that's capable of loading SkriptClasses in runtime.
 */
public class DynamicClassLoader extends ClassLoader {

    // Dot paths of the skript classes mapped to class data
    public final static Map<String, byte[]> CLASS_DATA = new LinkedHashMap<>();

    // Reflect's class loader without any Hippo's classes loaded
    // (used for unloading classes from memory)
    @Getter
    private static final ClassLoader EMPTY_CLASSLOADER = LibraryLoader.getClassLoader();

    // The most recent class loader used for loading Hippo's skript classes
    private static DynamicClassLoader CURRENT_CLASSLOADER;

    // Contains dot paths of already compiled skript classes by this class loader
    private final Set<String> runtimeCompiled = new LinkedHashSet<>();

    // Contains last fails reasons why skript class wasn't compiled mapped to dot paths
    @Getter
    private final Map<String, String> failReasons = new HashMap<>();

    // Contains classes that wasn't loaded by priority system
    private final HashSet<String> conflicting = new HashSet<>();
    // Maps missing data types used in classes to their dot paths
    private final Map<String, HashSet<String>> missingTypes = new HashMap<>();
    // Contains classes that failed to compile because of other problems than dependency issues
    private final HashSet<String> failed = new HashSet<>();

    private static final Field CLASSLOADER_FIELD = Reflectness.getField("classLoader", LibraryLoader.class);

    protected DynamicClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    /**
     * Creates new ClassLoader from Reflect's class loader without loaded skript classes.
     * @return new DynamicClass loader with reflect one as base
     */
    public static DynamicClassLoader create() {
        return new DynamicClassLoader(EMPTY_CLASSLOADER);
    }

    /**
     * Returns the most recent DynamicClassLoader used for defining the skript classes.
     * @return the most recent DynamicClassLoader used for reload
     */
    public static DynamicClassLoader getCurrentClassloader() {
        if(CURRENT_CLASSLOADER == null)
            reload();
        return CURRENT_CLASSLOADER;
    }

    /**
     * Tries to define all the already compiled skript classes.
     */
    public static void reload() {
        DynamicClassLoader loader = create();

        // Trying to compile classes using priority system until
        // new classes are getting compiled
        final Set<String> toCompile = new HashSet<>(CLASS_DATA.keySet());
        int left = toCompile.size();
        int current = -1;
        while(left != current) {
            current = left;
            loader.compileNext(toCompile);
            left = loader.leftToCompile(toCompile);
        }

        // Goes over all classes and checks if they can be compiled
        // even if priority system failed
        Set<String> waiting = new HashSet<>();
        for(String dotPath : toCompile) {
            if(loader.exists(dotPath)) continue;
            ISkriptClass skriptClass = SkriptClassBuilder.getSkriptClass(dotPath);
            if(loader.shouldForceCompile(skriptClass, null, null))
                waiting.add(dotPath);
        }

        // Trying to compile classes which can be force compiled until
        // new classes are getting compiled, keeps the priority of
        // super classes and interfaces
        left = waiting.size();
        current = -1;
        while(left != current) {
            current = left;
            for(String dotPath : waiting) {
                if(!loader.safeToCompile(SkriptClassBuilder.getSkriptClass(dotPath)))
                    continue;
                if(loader.exists(dotPath))
                    continue;
                loader.writeClass(dotPath);
                loader.runtimeCompiled.add(dotPath);
            }
            left = loader.leftToCompile(waiting);
        }

        // Sets the new class loader
        if(CLASSLOADER_FIELD != null)
            Reflectness.setField(CLASSLOADER_FIELD, null, loader);
        CURRENT_CLASSLOADER = loader;
    }

    /**
     * Defines the class from the provided doth path and mapped bytecode in {@link DynamicClassLoader#CLASS_DATA}
     * @param dotPath doth path of the compiled skript class to define
     */
    public void writeClass(String dotPath) {
        defineClass(dotPath, CLASS_DATA.get(dotPath), 0, CLASS_DATA.get(dotPath).length);
    }

    /**
     * Checks if super class and implemented interfaces of the class exist and the class
     * can be compiled safely.
     * @param skriptClass skript class to check for
     * @return true if super class and interfaces exist
     */
    public boolean safeToCompile(ISkriptClass skriptClass) {
        boolean safe = true;
        for(Type interfaceType : skriptClass.getInterfaces()) {
            if (exists(interfaceType.dotPath())) continue;
            safe = false;
        }
        if(!exists(skriptClass.getSuperClass().dotPath())) {
            safe = false;
        }
        return safe;
    }

    /**
     * Checks if all the Data Types of the provided SkriptClass are compiled.
     * @param skriptClass SkriptClass to check for
     * @return true if given SkriptClass can be compiled safely
     */
    public boolean shouldCompile(ISkriptClass skriptClass) {
        String dotPath = skriptClass.getType().dotPath();
        if(dotPath == null)
            return false;
        if(!missingTypes.containsKey(dotPath))
            missingTypes.put(dotPath, new HashSet<>());
        HashSet<String> missingTypesSet = missingTypes.get(dotPath);

        boolean success = true;

        // Checking interfaces
        for(Type interfaceType : skriptClass.getInterfaces()) {
            if(exists(interfaceType.dotPath())) continue;
            failReasons.put(dotPath,
                    "Implemented interface '" + interfaceType.dotPath() + "' of the class doesn't exist"
            );
            if(dotPath.equals(interfaceType.dotPath())) {
                failReasons.put(dotPath,
                        "Class can't implement itself"
                );
                failed.add(dotPath);
            }
            conflicting.add(dotPath);
            missingTypesSet.add(interfaceType.dotPath());
            success = false;
        }
        if(!safeToImplement(dotPath)) {
            failReasons.put(dotPath,
                    "Class is implementing non-interface classes"
            );
            failed.add(dotPath);
            success = false;
        }

        // Checking super class
        if(!exists(skriptClass.getSuperClass().dotPath())) {
            failReasons.put(dotPath,
                    "Super class '" + skriptClass.getSuperClass().dotPath() + "' of the class doesn't exist"
            );
            conflicting.add(dotPath);
            missingTypesSet.add(skriptClass.getSuperClass().dotPath());
            success = false;
        }
        if(dotPath.equals(skriptClass.getSuperClass().dotPath())) {
            failReasons.put(dotPath,
                    "Class can't extend itself"
            );
            failed.add(dotPath);
            success = false;
        }
        if(!safeToExtend(dotPath)) {
            failReasons.put(dotPath,
                    "'" + skriptClass.getSuperClass().dotPath() + "' can't be extended"
            );
            failed.add(dotPath);
            success = false;
        }

        // Checking class content
        for(ClassContent content : skriptClass.getClassContent().values()) {
            //noinspection ConstantConditions
            if(content.getType() instanceof NonPrimitiveType && !exists(content.getType().dotPath()) && !content.getType().dotPath().equals(skriptClass.getType().dotPath())) {
                failReasons.put(dotPath,
                        "Datatype '" + content.getType().dotPath() + "' of the content '" + content.getIdentifier() + "' of the class doesn't exist"
                );
                conflicting.add(dotPath);
                missingTypesSet.add(content.getType().dotPath());
                success = false;
            } else {
                missingTypesSet.remove(content.getType().dotPath());
            }
            if(!(content instanceof Method method)) continue;
            for(String argumentName : method.getArguments().keySet()) {
                Type argument = method.getArguments().get(argumentName);
                //noinspection ConstantConditions
                if (argument instanceof NonPrimitiveType && !exists(argument.dotPath())  && !argument.dotPath().equals(argument.dotPath())) {
                    failReasons.put(dotPath,
                            "Datatype '" + argument.dotPath() + "' of argument '" + argumentName + "' of the content '" + content.getIdentifier() + "' of the class doesn't exist"
                    );
                    conflicting.add(dotPath);
                    missingTypesSet.add(argument.dotPath());
                    success = false;
                } else {
                    missingTypesSet.remove(argument.dotPath());
                }
            }
            // Special check for annotation classes if the method
            // types can have default values.
            if(skriptClass instanceof SkriptAnnotation && !DefaultValue.canBeDefault(method.getType())) {
                failReasons.put(dotPath,
                        "Datatype '" + method.getType().dotPath() + "' can't be assigned to annotation member"
                );
                failed.add(dotPath);
                success = false;
            }
            if(!(content instanceof Constructor constructor)) continue;
            for(Type superArgument : constructor.getSuperArguments()) {
                //noinspection ConstantConditions
                if(superArgument instanceof NonPrimitiveType && !exists(superArgument.dotPath()) && !superArgument.dotPath().equals(superArgument.dotPath())) {
                    failReasons.put(dotPath,
                            "Datatype '" + superArgument.dotPath() + "' of super argument of the constructor '" + content.getIdentifier() + "' of the class doesn't exist"
                    );
                    conflicting.add(dotPath);
                    missingTypesSet.add(superArgument.dotPath());
                    success = false;
                } else {
                    missingTypesSet.remove(superArgument.dotPath());
                }
            }
        }

        if(success) {
            conflicting.remove(dotPath);
            missingTypes.remove(dotPath);
            failed.remove(dotPath);
        }
        return success;
    }

    /**
     * Checks if class is safe to compile even when priority check failed.
     * @param skriptClass SkriptClass to check
     * @param checked List of already check classes for current check, should be null
     *                if you don't know what you're doing
     * @return true if class is safe to compile
     */
    private boolean shouldForceCompile(ISkriptClass skriptClass, @Nullable Set<String> passed, @Nullable Set<String> checked) {
        String dotPath = skriptClass.getType().dotPath();
        if(shouldCompile(skriptClass)) return true; // Can be compiled normally
        if(!conflicting.contains(dotPath)) return false; // Doesn't have conflicts, should compile
        if(failed.contains(dotPath)) return false; // Failed not because of priority system, isn't safe to compile

        HashSet<String> dependencies = missingTypes.get(dotPath);
        if(dependencies == null) return false; // Doesn't have conflicts, should compile

        if(passed == null) {
            passed = new LinkedHashSet<>();
            passed.add(dotPath);
        }
        if(checked == null) {
            checked = new LinkedHashSet<>();
        }

        for(String dependency : dependencies) {
            // Conflicts with class that doesn't conflict
            if(!conflicting.contains(dependency)) {
                return false;
            }

            // Prevents looping
            if(checked.contains(dependency)) continue;
            checked.add(dotPath);

            // All dependencies have to pass the force compile check for the class to compile
            if(!shouldForceCompile(SkriptClassBuilder.getSkriptClass(dependency), passed ,checked)) {
                return false;
            }

        }

        passed.add(dotPath);
        return true;
    }

    /**
     * Tries to compile the uncompiled classes again, using the newly compiled classes.
     * @param toCompile list of dot paths of skript classes to compile
     */
    protected void compileNext(Set<String> toCompile) {
        for(String dotPath : toCompile) {
            if(exists(dotPath)) continue;
            ISkriptClass skriptClass = SkriptClassBuilder.getSkriptClass(dotPath);
            if(shouldCompile(skriptClass)) {
                writeClass(dotPath);
                runtimeCompiled.add(dotPath);
            }
        }
    }

    /**
     * Checks if the class with given dot path exists or was compiled by the class loader in runtime.
     * @param dotPath dot path of the class to check
     * @return true if the class exists or was compiled
     */
    private boolean exists(String dotPath) {
        Class<?> serverClass = null;
        try {
            serverClass = Class.forName(dotPath);
        } catch (ClassNotFoundException ignored) { }
        if(serverClass != null) return true;
        try {
            findClass(dotPath);
            return true;
        } catch (ClassNotFoundException e) {
            return runtimeCompiled.contains(dotPath);
        }
    }

    /**
     * Calculates how much skript classes is needed yet to compile.
     * @param toCompile List of dot paths of the skript classes to compile
     * @return number of skript classes yet to compile
     */
    private int leftToCompile(Set<String> toCompile) {
        int left = toCompile.size();
        for(String dotPath : toCompile) {
            if(exists(dotPath)) left--;
        }
        return left;
    }

    /**
     * Checks if class of given dot path is extending class that can be extended.
     * @param dotPath Class to check for
     * @return true if the class extend class that can be extended safely
     */
    public boolean safeToExtend(String dotPath) {
        ISkriptClass skriptClass = SkriptClassBuilder.getSkriptClass(dotPath);
        if(skriptClass != null) {
            String superPath = skriptClass.getSuperClass().dotPath();
            ISkriptClass superSkriptClass = SkriptClassBuilder.getSkriptClass(superPath);

            if(superSkriptClass != null) {
                if(superSkriptClass.getModifiers().contains(Modifier.FINAL))
                    return false;
                if(java.lang.reflect.Modifier.isFinal(superSkriptClass.getClassType().getValue()))
                    return false;
                return !java.lang.reflect.Modifier.isInterface(superSkriptClass.getClassType().getValue());
            }

            Class<?> classObject = null;
            try {
                classObject = Class.forName(superPath);
            } catch (ClassNotFoundException e) {
                try {
                    classObject = findClass(superPath);
                } catch (ClassNotFoundException ignored) { }
            }

            if(classObject == null)
                return false;

            if(classObject == Object.class)
                return true;

            if(skriptClass instanceof SkriptEnum)
                return classObject == Enum.class;

            if(skriptClass instanceof SkriptRecord)
                return classObject == Record.class;

            if(java.lang.reflect.Modifier.isFinal(classObject.getModifiers()))
                return false;
            return !java.lang.reflect.Modifier.isInterface(classObject.getModifiers());

        }

        try {
            findClass(dotPath); // If class is found, its super class can be extended
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if class of given dot path is implementing only interfaces.
     * @param dotPath Class to check for
     * @return true if the class implements only interfaces
     */
    public boolean safeToImplement(String dotPath) {
        ISkriptClass skriptClass = SkriptClassBuilder.getSkriptClass(dotPath);
        if(skriptClass != null) {
            for(Type type : skriptClass.getInterfaces()) {
                String interfacePath = type.dotPath();
                ISkriptClass skriptInterface = SkriptClassBuilder.getSkriptClass(interfacePath);

                if (skriptInterface != null) {
                    if (!java.lang.reflect.Modifier.isInterface(skriptInterface.getClassType().getValue()))
                        return false;
                    continue;
                }

                Class<?> classObject = null;
                try {
                    classObject = Class.forName(interfacePath);
                } catch (ClassNotFoundException e) {
                    try {
                        classObject = findClass(interfacePath);
                    } catch (ClassNotFoundException ignored) { }
                }

                if(classObject == null)
                    return false;

                if (!java.lang.reflect.Modifier.isInterface(classObject.getModifiers()))
                    return false;
            }
            return true;
        }

        try {
            findClass(dotPath); // If class is found, its interfaces can be implemented
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Defines the class skipping all the checks for
     * errors prevention.
     * @param dotPath dot path of the class that should be defined
     * @return exception from defining if any
     */
    public Throwable forceDefine(String dotPath) {
        if(exists(dotPath)) return null;
        try {
            writeClass(dotPath);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

}
