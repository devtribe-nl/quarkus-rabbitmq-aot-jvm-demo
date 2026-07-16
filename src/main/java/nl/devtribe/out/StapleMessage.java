package nl.devtribe.out;


import io.quarkus.runtime.annotations.RegisterForReflection;

//Deliberate mistake forgetting a @RegisterForReflection
@RegisterForReflection
public record StapleMessage(String prefix, String suffix) {}
