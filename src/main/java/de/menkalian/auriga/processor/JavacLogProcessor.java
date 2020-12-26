package de.menkalian.auriga.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import de.menkalian.auriga.Config;
import de.menkalian.auriga.annotations.Log;
import de.menkalian.auriga.annotations.NoLog;
import de.menkalian.auriga.config.Constants;
import de.menkalian.auriga.config.FormatPlaceholder;
import de.menkalian.auriga.config.LoggerConfig;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

public class JavacLogProcessor extends AbstractProcessor {
    JavacProcessingEnvironment processingEnvironment;
    TreeMaker instance;
    JavacElements elementUtils;
    Config config;
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
        if (processingEnvironment.getOptions().containsKey("Auriga.Config.File")) {
            config = new Config(processingEnvironment.getOptions().get("Auriga.Config.File"));
        } else {
            config = new Config();
        }
    }

    private void generateHeaderLogsForMethod (Element method) {
        checkLogger(method.getEnclosingElement());

        final JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) elementUtils.getTree(method);
        if (tree == null || tree.body == null) {
            return;
        }
        final List<JCTree.JCStatement> stats = tree.body.stats;

        JCTree.JCMethodInvocation logInvocation;
        if (config.getSelectedFormatPlaceholder() == FormatPlaceholder.NONE) {
            logInvocation = instance.Apply(null, convertStringToJC(config.getLoggingMethod()), List.of(instance.Apply(null, convertStringToJC("String.format"), generateArgumentsFromMethod(method))));
        } else {
            logInvocation = instance.Apply(null, convertStringToJC(config.getLoggingMethod()), generateArgumentsFromMethod(method));
        }
        tree.body.stats = stats.prepend(
                instance.Exec(logInvocation)
        );
    }

    private void checkLogger (Element enclosingElement) {
        if (config.getSelectedLoggerConfig() == LoggerConfig.NONE)
            return;

        if (!enclosingElement.getModifiers().contains(Modifier.STATIC))
            return;

        JCTree classTree = elementUtils.getTree(enclosingElement);
        JCTree.JCClassDecl classDeclTree = (JCTree.JCClassDecl) classTree.getTree();
        boolean hasLog = classDeclTree.defs.stream()
                                           .filter(tree -> tree instanceof JCTree.JCVariableDecl)
                                           .map(tree -> (JCTree.JCVariableDecl) tree)
                                           .anyMatch(tree -> tree.name.contentEquals("log"));
        if (!hasLog) {
            JCTree.JCVariableDecl logDefTree = instance.VarDef(
                    instance.Modifiers(Flags.PRIVATE | Flags.STATIC | Flags.FINAL),
                    elementUtils.getName("log"),
                    convertStringToJC(config.getSelectedLoggerConfig().getClazz()),
                    generateLoggerInit(enclosingElement));
            classDeclTree.defs = classDeclTree.defs.prepend(logDefTree);
        }
    }

    private JCTree.JCExpression generateLoggerInit (Element classElement) {
        String[] provisioningData = config.getSelectedLoggerConfig().getProvisioning().split("\\(");
        String provisioningMethod = provisioningData[0];
        String provisioningArgument = provisioningData[1].split("\\)")[0].replace(Constants.PLACEHOLDER_CLASS, classElement.getSimpleName());
        return instance.Apply(null, convertStringToJC(provisioningMethod), List.of(convertStringToJC(provisioningArgument)));
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
        final String placeholder = config.getSelectedFormatType().getPlaceholder();
        final Element clazz = method.getEnclosingElement();

        String stringToPrint = config.getMethodTemplate();
        List<JCTree.JCExpression> parametersForFormatter = List.nil();

        while (stringToPrint.contains(Constants.PLACEHOLDER_THIS) || stringToPrint.contains(Constants.PLACEHOLDER_PARAMS)) {
            int indexOfThis = stringToPrint.indexOf(Constants.PLACEHOLDER_THIS);
            int indexOfParams = stringToPrint.indexOf(Constants.PLACEHOLDER_PARAMS);
            if (indexOfParams == -1 || (indexOfThis < indexOfParams && indexOfThis != -1)) {
                // Append a "this" as String (= Call toString)
                if (method.getModifiers().contains(Modifier.STATIC)) {
                    stringToPrint = stringToPrint.replace(Constants.PLACEHOLDER_THIS, clazz.getSimpleName() + ".class");
                } else {
                    stringToPrint = stringToPrint.replace(Constants.PLACEHOLDER_THIS, "%s");
                    parametersForFormatter = parametersForFormatter.append(instance.Apply(null, convertStringToJC("toString"), List.nil()));
                }
            } else {
                // Append the parameters
                String replacement = "";

                for (JCTree.JCVariableDecl parameter : tree.getParameters()) {
                    // Concatenation seem more reasonable here since StringBuilder does not support replacing CharSequences
                    //noinspection StringConcatenationInLoop
                    replacement += config.getParamTemplate();

                    while (replacement.contains(Constants.PLACEHOLDER_PARAM_TYPE))
                        replacement = replacement.replace(Constants.PLACEHOLDER_PARAM_TYPE, parameter.vartype.toString());

                    while (replacement.contains(Constants.PLACEHOLDER_PARAM_NAME))
                        replacement = replacement.replace(Constants.PLACEHOLDER_PARAM_NAME, parameter.name.toString());

                    while (replacement.contains(Constants.PLACEHOLDER_PARAM_VALUE)) {
                        replacement = replacement.replace(Constants.PLACEHOLDER_PARAM_VALUE, placeholder);
                        parametersForFormatter = parametersForFormatter.append(getArgumentForParameter(parameter));
                    }
                }

                stringToPrint = stringToPrint.replace(Constants.PLACEHOLDER_PARAMS, replacement);
            }
        }

        while (stringToPrint.contains(Constants.PLACEHOLDER_CLASS))
            stringToPrint = stringToPrint.replace(Constants.PLACEHOLDER_CLASS, clazz.getSimpleName());

        while (stringToPrint.contains(Constants.PLACEHOLDER_METHOD))
            stringToPrint = stringToPrint.replace(Constants.PLACEHOLDER_METHOD, method.getSimpleName());

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
