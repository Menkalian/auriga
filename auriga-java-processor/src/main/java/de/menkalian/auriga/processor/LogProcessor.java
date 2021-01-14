package de.menkalian.auriga.processor;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import de.menkalian.auriga.config.UtilKt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("de.menkalian.auriga.annotations.*")
public class LogProcessor extends AbstractProcessor {
    AbstractProcessor specializedProcessor;

    @Override
    public synchronized void init (ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        if (processingEnv instanceof JavacProcessingEnvironment) {
            specializedProcessor = new JavacLogProcessor();
            specializedProcessor.init(processingEnv);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Unknown Processing-Environment detected! Auriga does not work here properly! Detected Environment: " + processingEnv.getClass().getCanonicalName());
        }
    }

    @Override
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (specializedProcessor == null) {
            return false;
        } else {
            return specializedProcessor.process(annotations, roundEnv);
        }
    }

    @Override
    public Set<String> getSupportedOptions () {
        return UtilKt.getSupportedConfigKeys();
    }

    @Override
    public SourceVersion getSupportedSourceVersion () {
        return SourceVersion.latest();
    }
}
