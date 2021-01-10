package de.menkalian.auriga.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import de.menkalian.auriga.annotations.Log;
import de.menkalian.auriga.annotations.NoLog;
import de.menkalian.auriga.config.AurigaConfig;
import de.menkalian.auriga.config.AurigaLoggingConfig;
import de.menkalian.auriga.config.FormatPlaceholder;
import de.menkalian.auriga.config.Placeholder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class JavacLogProcessor extends AbstractProcessor {
    JavacProcessingEnvironment processingEnvironment;
    TreeMaker instance;
    JavacElements elementUtils;
    AurigaConfig config;
    private Set<? extends Element> excludedElements;

    @Override
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processingEnvironment != null) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Log.class);
            excludedElements = roundEnv.getElementsAnnotatedWith(NoLog.class);
            for (Element element : elements) {
                processElement(element);
            }
            return true;
        }

        return false;
    }

    public void processElement (Element element) {
        if (excludedElements.contains(element))
            return;

        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, "AURIGA: Processing " + element);
        ElementKind elementKind = element.getKind();
        if (
                elementKind == ElementKind.PACKAGE ||
                elementKind == ElementKind.CLASS ||
                elementKind == ElementKind.ENUM ||
                elementKind == ElementKind.INTERFACE ||
                elementKind == ElementKind.ANNOTATION_TYPE
        ) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.OTHER, "AURIGA: Processing children of " + element);
            element.getEnclosedElements().forEach(this::processElement);
        } else if (
                elementKind == ElementKind.METHOD ||
                elementKind == ElementKind.CONSTRUCTOR
        ) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.OTHER, "AURIGA: Generating Logs for " + element);
            generateHeaderLogsForMethod(element);
        }
    }

    @Override
    public synchronized void init (ProcessingEnvironment processingEnv) {
        processingEnvironment = (JavacProcessingEnvironment) processingEnv;

        elementUtils = processingEnvironment.getElementUtils();
        instance = TreeMaker.instance(processingEnvironment.getContext());

        // Load config
        Map<String, String> options = processingEnvironment.getOptions();
        config = new AurigaConfig(Collections.unmodifiableMap(options));
    }

    private void generateHeaderLogsForMethod (Element method) {
        checkLogger(method.getEnclosingElement());

        final JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) elementUtils.getTree(method);
        if (tree == null || tree.body == null) {
            return;
        }
        final List<JCTree.JCStatement> stats = tree.body.stats;

        JCTree.JCMethodInvocation logInvocation;
        AurigaLoggingConfig loggingConfig = config.getLoggingConfig();
        if (loggingConfig.getPlaceholderEnum() == FormatPlaceholder.NONE) {
            logInvocation = instance.Apply(null, convertStringToJC(loggingConfig.getMethod()), List.of(instance.Apply(null, convertStringToJC("String.format"), generateArgumentsFromMethod(method))));
        } else {
            logInvocation = instance.Apply(null, convertStringToJC(loggingConfig.getMethod()), generateArgumentsFromMethod(method));
        }
        if (stats.get(0).toString().startsWith("super")) {
            List<JCTree.JCStatement> newStats = List.of(stats.get(0)).append(instance.Exec(logInvocation));

            for (int i = 1 ; i < stats.size() ; i++) {
                newStats = newStats.append(stats.get(i));
            }

            tree.body.stats = newStats;
        } else {
            tree.body.stats = stats.prepend(
                    instance.Exec(logInvocation)
            );
        }
    }

    private void checkLogger (Element enclosingElement) {
        if (config.getLoggerConfig().getType().equals("NONE"))
            return;

        if (enclosingElement.getEnclosingElement().getKind() != ElementKind.PACKAGE)
            return;

        JCTree classTree = elementUtils.getTree(enclosingElement);
        JCTree.JCClassDecl classDeclTree = (JCTree.JCClassDecl) classTree.getTree();
        boolean hasLog = classDeclTree.defs.stream()
                                           .filter(tree -> tree instanceof JCTree.JCVariableDecl)
                                           .map(tree -> (JCTree.JCVariableDecl) tree)
                                           .anyMatch(tree -> tree.name.contentEquals("log"));
        if (!hasLog) {
            JCTree.JCVariableDecl logDefTree = instance.VarDef(
                    instance.Modifiers(Flags.STATIC | Flags.FINAL),
                    elementUtils.getName("log"),
                    convertStringToJC(config.getLoggerConfig().getClazz()),
                    generateLoggerInit(enclosingElement));
            classDeclTree.defs = classDeclTree.defs.prepend(logDefTree);
        }
    }

    private JCTree.JCExpression generateLoggerInit (Element classElement) {
        String[] provisioningData = config.getLoggerConfig().getSource().split("\\(");
        String provisioningMethod = provisioningData[0];
        String provisioningArgument = provisioningData[1].split("\\)")[0].replace(Placeholder.CLASS, classElement.getSimpleName());
        if (provisioningArgument.startsWith("\"")) {
            return instance.Apply(null, convertStringToJC(provisioningMethod), List.of(convertStringToJC(provisioningArgument)));
        } else {
            return instance.Apply(null, convertStringToJC(provisioningMethod), List.of(instance.Literal(provisioningArgument.substring(1, provisioningArgument.length() - 1))));
        }
    }

    private JCTree.JCExpression convertStringToJC (String ref) {
        final String[] split = ref.split("\\.");

        Name name = elementUtils.getName(split[0]);
        JCTree.JCExpression toReturn = instance.Ident(name);

        for (int i = 1 ; i < split.length ; i++) {
            name = elementUtils.getName(split[i]);
            toReturn = instance.Select(toReturn, name);
        }

        return toReturn;
    }

    private List<JCTree.JCExpression> generateArgumentsFromMethod (Element method) {
        final JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) elementUtils.getTree(method);
        final String placeholder = config.getLoggingConfig().getPlaceholderEnum().getPlaceholder();
        final Element clazz = method.getEnclosingElement();

        String stringToPrint = config.getLoggingConfig().getEntryTemplate();
        List<JCTree.JCExpression> parametersForFormatter = List.nil();

        while (stringToPrint.contains(Placeholder.THIS) || stringToPrint.contains(Placeholder.PARAMS)) {
            int indexOfThis = stringToPrint.indexOf(Placeholder.THIS);
            int indexOfParams = stringToPrint.indexOf(Placeholder.PARAMS);
            if (indexOfParams == -1 || (indexOfThis < indexOfParams && indexOfThis != -1)) {
                // Append a "this" as String (= Call toString)
                if (method.getModifiers().contains(Modifier.STATIC)) {
                    stringToPrint = stringToPrint.replace(Placeholder.THIS, clazz.getSimpleName() + ".class");
                } else {
                    stringToPrint = stringToPrint.replace(Placeholder.THIS, "%s");
                    parametersForFormatter = parametersForFormatter.append(instance.Apply(null, convertStringToJC("toString"), List.nil()));
                }
            } else {
                // Append the parameters
                String replacement = "";

                for (JCTree.JCVariableDecl parameter : tree.getParameters()) {
                    // Concatenation seem more reasonable here since StringBuilder does not support replacing CharSequences
                    //noinspection StringConcatenationInLoop
                    replacement += config.getLoggingConfig().getParamTemplate();

                    while (replacement.contains(Placeholder.PARAM_TYPE))
                        replacement = replacement.replace(Placeholder.PARAM_TYPE, parameter.vartype.toString());

                    while (replacement.contains(Placeholder.PARAM_NAME))
                        replacement = replacement.replace(Placeholder.PARAM_NAME, parameter.name.toString());

                    while (replacement.contains(Placeholder.PARAM_VALUE)) {
                        replacement = replacement.replace(Placeholder.PARAM_VALUE, placeholder);
                        parametersForFormatter = parametersForFormatter.append(getArgumentForParameter(parameter));
                    }
                }

                stringToPrint = stringToPrint.replace(Placeholder.PARAMS, replacement);
            }
        }

        while (stringToPrint.contains(Placeholder.CLASS))
            stringToPrint = stringToPrint.replace(Placeholder.CLASS, clazz.getSimpleName());

        while (stringToPrint.contains(Placeholder.METHOD))
            stringToPrint = stringToPrint.replace(Placeholder.METHOD, method.getSimpleName());

        return parametersForFormatter.prepend(instance.Literal(stringToPrint));
    }

    private JCTree.JCExpression getArgumentForParameter (JCTree.JCVariableDecl parameter) {
        if (parameter.vartype instanceof JCTree.JCArrayTypeTree) {
            return instance.Apply(null, instance.Select(convertStringToJC("java.util.Arrays"), elementUtils.getName("toString")), List.of(instance.Ident(parameter)));
        } else if (parameter.vartype instanceof JCTree.JCPrimitiveTypeTree) {
            return instance.Apply(null, instance.Select(convertStringToJC("String"), elementUtils.getName("valueOf")), List.of(instance.Ident(parameter)));
        } else {
            return instance.Apply(null, instance.Select(instance.Ident(parameter), elementUtils.getName("toString")), List.nil());
        }
    }
}
