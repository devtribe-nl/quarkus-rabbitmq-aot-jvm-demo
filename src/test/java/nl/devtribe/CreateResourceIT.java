package nl.devtribe;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
public class CreateResourceIT extends CreateResourceTest {
    // they fail because in memory adapter cant be injected in AoT mode.
}
