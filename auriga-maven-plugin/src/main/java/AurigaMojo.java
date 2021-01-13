import de.menkalian.auriga.config.Auriga;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Objects;

@SuppressWarnings({"RedundantThrows", "unused", "unchecked"})
@Mojo(name = "auriga", defaultPhase = LifecyclePhase.GENERATE_SOURCES, executionStrategy = "always")
public class AurigaMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(property = Auriga.Config.type, defaultValue = "ARGS", required = true)
    private String aurigaConfigType;

    @Parameter(property = Auriga.Config.location, defaultValue = " ")
    private String aurigaConfigLocation;

    @Parameter(property = Auriga.Config.base, defaultValue = " ")
    private String aurigaConfigBase;

    @Parameter(property = Auriga.Logger.type, defaultValue = " ")
    private String aurigaLoggerType;

    @Parameter(property = Auriga.Logger.clazz, defaultValue = " ")
    private String aurigaLoggerClazz;

    @Parameter(property = Auriga.Logger.source, defaultValue = " ")
    private String aurigaLoggerSource;

    @Parameter(property = Auriga.Logging.method, defaultValue = " ")
    private String aurigaLoggingMethod;

    @Parameter(property = Auriga.Logging.mode, defaultValue = " ")
    private String aurigaLoggingMode;

    @Parameter(property = Auriga.Logging.placeholder, defaultValue = " ")
    private String aurigaLoggingPlaceholder;

    @Parameter(property = Auriga.Logging.Template.param, defaultValue = " ")
    private String aurigaLoggingTemplateParam;

    @Parameter(property = Auriga.Logging.Template.entry, defaultValue = " ")
    private String aurigaLoggingTemplateEntry;

    @Override
    public void execute () throws MojoExecutionException, MojoFailureException {
        getLog().info("Working :)");
        if (project.getRepositories().stream().noneMatch(repo -> ((Repository) repo).getUrl().equals("http://server.menkalian.de:8081/artifactory/auriga"))) {
            Repository menkalianArtifactoryRepository = new Repository();
            menkalianArtifactoryRepository.setId("auriga-artifactory-menkalian");
            menkalianArtifactoryRepository.setName("auriga-artifactory-menkalian");
            menkalianArtifactoryRepository.setUrl("http://server.menkalian.de:8081/artifactory/auriga");

            //noinspection unchecked
            project.getRepositories().add(menkalianArtifactoryRepository);
        }

        if (project.getDependencies().stream().noneMatch(dep -> ((Dependency) dep).getArtifactId().equals("auriga-annotations"))) {
            Dependency aurigaAnnotationsDependency = new Dependency();
            aurigaAnnotationsDependency.setGroupId("de.menkalian.auriga");
            aurigaAnnotationsDependency.setGroupId("auriga-annotations");
            aurigaAnnotationsDependency.setGroupId("1.0.1");
        }

        Plugin compilerPlugin = project.getBuild().getPluginsAsMap().get("org.apache.maven.plugins:maven-compiler-plugin");

        Object configuration = compilerPlugin.getConfiguration();
        Xpp3Dom configurationDom;
        if (configuration instanceof Xpp3Dom) {
            configurationDom = (Xpp3Dom) configuration;
        } else {
            configurationDom = new Xpp3Dom("configuration");
        }

        Xpp3Dom compilerArgs = configurationDom.getChild("compilerArgs");
        if (compilerArgs == null) {
            compilerArgs = new Xpp3Dom("compilerArgs");
            configurationDom.addChild(compilerArgs);
        }

        for (String key : Auriga.INSTANCE.getKeys()) {
            String memberVariable = key;
            int pos;
            while ((pos = memberVariable.indexOf(".")) != -1) {
                memberVariable = memberVariable.substring(0, pos) + memberVariable.substring(pos + 1, pos + 2).toUpperCase() + memberVariable.substring(pos + 2);
            }

            try {
                String value = Objects.toString(AurigaMojo.class.getDeclaredField(memberVariable).get(this));
                addCompilerArg(compilerArgs, key, value);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        getLog().info(configurationDom.toString());
    }

    public void addCompilerArg (Xpp3Dom parent, String key, String value) {
        if (value == null || value.isEmpty() || value.isBlank())
            return;
        Xpp3Dom argument = new Xpp3Dom("arg");
        argument.setValue("-A" + key + "=" + value);
        parent.addChild(argument);
    }
}
