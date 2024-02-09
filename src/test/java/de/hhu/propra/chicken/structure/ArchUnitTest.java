package de.hhu.propra.chicken.structure;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import de.hhu.propra.chicken.ChickenApplication;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Not important here")
@AnalyzeClasses(packagesOf = ChickenApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTest {


    @ArchTest
    ArchRule rightControllerAnnotation = classes()
            .that().resideInAPackage("..controllers..")
            .and().areNotRecords()
            .should().beAnnotatedWith(Controller.class);

    @ArchTest
    ArchRule wrongControllerAnnotation = classes()
            .that().resideOutsideOfPackage("..controllers..")
            .should().notBeAnnotatedWith(Controller.class);

    @ArchTest
    ArchRule noDeprecatedClasses = classes()
            .should().notBeAnnotatedWith(Deprecated.class).because("Keine Klasse sollte mit Deprecated annotiert sein");
    @ArchTest
    ArchRule noDeprecatedMethods = methods()
            .should().notBeAnnotatedWith(Deprecated.class).because("Keine Methode sollte mit Deprecated annotiert sein");

    @ArchTest
    ArchRule noFieldInjection = fields().should().notBeAnnotatedWith(Autowired.class).because("Keine Field Injection");

    @ArchTest
    ArchRule noMethodInjection = methods().should().notBeAnnotatedWith(Autowired.class).because("Keine Method Injection");


    @ArchTest
    ArchRule onionTest = onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.service..")
            .applicationServices("..services..")
            .adapter("web", "..controllers..")
            .adapter("db", "..repositories..")
            .adapter("config", "..configuration..");

}
