package de.menkalian.auriga.processor;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import de.menkalian.auriga.annotations.Log;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("de.menkalian.auriga.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_15)
public class LogProcessor extends AbstractProcessor {
    JavacProcessingEnvironment processingEnvironment;
    TreeMaker instance;
    JavacElements elementUtils;

    @Override
    public synchronized void init (ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        if (processingEnv instanceof JavacProcessingEnvironment) {
            processingEnvironment = (JavacProcessingEnvironment) processingEnv;
            elementUtils = processingEnvironment.getElementUtils();
            instance = TreeMaker.instance(processingEnvironment.getContext());
        }
    }

    @Override
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processingEnvironment != null) {
            Set<? extends Element> methods = roundEnv.getElementsAnnotatedWith(Log.class);
            for (Element method : methods) {
                generateHeaderLogsForMethod(method);
            }
            return true;
        }

        return false;
    }

    private void generateHeaderLogsForMethod (Element method) {
        final JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) elementUtils.getTree(method);
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
                formatParameters = formatParameters.append(instance.Ident(parameter));
            } else {
                formatParameters = formatParameters.append(instance.Apply(null, instance.Select(instance.Ident(parameter), elementUtils.getName("toString")), List.nil()));
            }
        }
        return formatParameters.prepend(instance.Literal(formatStringParameter.toString()));
    }
}
