package de.test.antennapod;

import android.test.InstrumentationTestRunner;
import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.TestSuite;

public class AntennaPodTestRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        return new TestSuiteBuilder(AntennaPodTestRunner.class)
                .includeAllPackagesUnderHere()
                .excludePackages("de.test.antennapod.gpodnet", "de.test.antennapod.ui", "de.test.antennapod.handler", "de.test.antennapod.discovery", "de.test.antennapod.twitter")
                .build();
    }

}
