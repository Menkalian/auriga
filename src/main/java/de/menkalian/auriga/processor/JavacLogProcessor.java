package de.menkalian.auriga.processor;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import de.menkalian.auriga.Config;
import de.menkalian.auriga.annotations.Log;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public class JavacLogProcessor extends AbstractProcessor {
    JavacProcessingEnvironment processingEnvironment;
    TreeMaker instance;
    JavacElements elementUtils;
    Config config;

    @Override
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processingEnvironment != null) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Log.class);
            for (Element element : elements) {
                processElement(element);
            }
            return true;
        }

        return false;
    }

    public void processElement (Element element) {
        ElementKind elementKind = element.getKind();
        if (
                elementKind == ElementKind.PACKAGE ||
                elementKind == ElementKind.CLASS ||
                elementKind == ElementKind.ENUM ||
                elementKind == ElementKind.INTERFACE ||
                elementKind == ElementKind.ANNOTATION_TYPE
        ) {
            element.getEnclosedElements().forEach(this::processElement);
        } else if (
                elementKind == ElementKind.METHOD ||
                elementKind == ElementKind.CONSTRUCTOR
        ) {
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
        final JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) elementUtils.getTree(method);
        if (tree.body == null) {
            return;
        }
        final List<JCTree.JCStatement> stats = tree.body.stats;
        tree.body.stats = stats.prepend(
                instance.Exec(instance.Apply(null, convertStringToJC("System.out.printf"), generateArgumentsFromMethod(method)))
        );
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
        StringBuilder formatStringParameter = new StringBuilder();
        List<JCTree.JCExpression> formatParameters = List.nil();

        formatStringParameter.append("Executing Method: ").append(method.getSimpleName()).append("%n");

        for (JCTree.JCVariableDecl parameter : tree.getParameters()) {
            formatStringParameter.append("Parameter ").append(parameter.name.toString()).append(": %s%n");
            if (parameter.vartype instanceof JCTree.JCArrayTypeTree) {
                formatParameters = formatParameters.append(instance.Apply(null, instance.Select(convertStringToJC("java.util.Arrays"), elementUtils.getName("toString")), List.of(instance.Ident(parameter))));
            } else if (parameter.vartype instanceof JCTree.JCPrimitiveTypeTree) {
                formatParameters = formatParameters.append(instance.Apply(null, instance.Select(convertStringToJC("String"), elementUtils.getName("valueOf")), List.of(instance.Ident(parameter))));
            } else {
                formatParameters = formatParameters.append(instance.Apply(null, instance.Select(instance.Ident(parameter), elementUtils.getName("toString")), List.nil()));
            }
        }
        return formatParameters.prepend(instance.Literal(formatStringParameter.toString()));
    }

}
