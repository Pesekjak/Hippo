package me.pesekjak.hippo.core.loader;

import me.pesekjak.hippo.core.AbstractClass;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Contains classes in reloaded scripts by the user.
 * <p>
 * Only a single instance can exist at the time.
 */
public final class ClassUpdate {

    private static ClassUpdate instance;

    private final List<ClassSignature> signatures = new CopyOnWriteArrayList<>();

    public static ClassUpdate get() {
        if (instance == null) createNew();
        return instance;
    }

    /**
     * Creates new class update.
     */
    public static void createNew() {
        instance = new ClassUpdate();
    }

    /**
     * Enrolls new class update.
     */
    public static void enroll() {
        ClassUpdate update = get();
        createNew();

        DynamicClassLoader classLoader = DynamicClassLoader.getInstance();

        Set<ClassSignature> updated = new HashSet<>();
        for (ClassSignature signature : update.getSignatures()) {
            ClassSignature old = Optional.ofNullable(classLoader.loaders.get(signature.clazz().getName()))
                    .map(SingleClassLoader::getClassSignature)
                    .orElse(null);
            if (old != null && signature.matches(old)) continue;
            updated.add(signature);
        }

        Set<AbstractClass> dependant = new HashSet<>();
        for (ClassSignature signature : updated)
            dependant.addAll(LoaderUtils.getDependant(signature.clazz()));

        ClassChecker checker = new ClassChecker();

        List<String> dependantNames = dependant.stream().map(AbstractClass::getName).toList();
        List<Type> notDependant = classLoader.loaders.values().stream()
                .map(SingleClassLoader::getClassSignature)
                .map(ClassSignature::clazz)
                .filter(c -> !dependantNames.contains(c.getName()))
                .map(AbstractClass::getType)
                .toList();
        checker.addSuccessful(notDependant);

        for (AbstractClass d : dependant) {
            checker.hasLegitSuperClass(d);
            checker.hasLegitInterfaces(d);
        }

        Set<ClassSignature> sortedSuccessful;
        List<Type> checkerResult = new ArrayList<>(checker.getSuccessful());
        ClassSignature[] successful = new ClassSignature[checkerResult.size()];

        for (ClassSignature signature : updated) {
            int index = checkerResult.indexOf(signature.clazz().getType());
            if (index == -1) continue;
            successful[index] = signature;
        }

        sortedSuccessful = Arrays.stream(successful)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Result result = new Result(sortedSuccessful, checker.getFailed());
        classLoader.pushUpdate(result);
    }

    private ClassUpdate() {
        this(Collections.emptyList());
    }

    private ClassUpdate(Collection<ClassSignature> signatures) {
        this.signatures.addAll(signatures);
    }

    /**
     * Adds new signature to the class update.
     *
     * @param signature signature to add
     */
    public void add(ClassSignature signature) {
        signatures.add(signature);
    }

    /**
     * @return all signatures of this class update
     */
    public @Unmodifiable List<ClassSignature> getSignatures() {
        return Collections.unmodifiableList(signatures);
    }

    /**
     * Represents a result of enrolled class update.
     *
     * @param successful classes that should be reloaded
     * @param failed classes that failed the pre-compilation checks
     */
    public record Result(Set<ClassSignature> successful, Map<Type, CompileException> failed) {
    }

}
