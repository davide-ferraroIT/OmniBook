package com.davideferraroit.omnibook.backend;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.davideferraroit.omnibook.backend")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule layerDependenciesAreRespected = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controllers").definedBy("com.davideferraroit.omnibook.backend.controller..")
            .layer("Services").definedBy("com.davideferraroit.omnibook.backend.service..")
            .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
            .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Services");
}
