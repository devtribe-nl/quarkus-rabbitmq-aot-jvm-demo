package nl.devtribe.out;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

public class StapleProducer {
    private final Emitter<StapleMessage> stapleEmitter;
    private final StapleMessageCreator stapleMessageCreator;

    public StapleProducer(
            @Channel("staple-commands-out") Emitter<StapleMessage> stapleEmitter,
            StapleMessageCreator stapleMessageCreator) {
        this.stapleEmitter = stapleEmitter;
        this.stapleMessageCreator = stapleMessageCreator;
    }

    public CreateStapleOutResponse staple(CreateStapleOutCommand createStapleOutCommand) {
        stapleEmitter.send(stapleMessageCreator.apply(toMessage(createStapleOutCommand)));
        return toResponse(createStapleOutCommand);
    }

    private StapleMessage toMessage(CreateStapleOutCommand createStapleOutCommand) {
        return new StapleMessage(createStapleOutCommand.prefix(), createStapleOutCommand.suffix());
    }

    private CreateStapleOutResponse toResponse(CreateStapleOutCommand createStapleOutCommand) {

        return new CreateStapleOutResponse(createStapleOutCommand.prefix(), createStapleOutCommand.suffix());
    }
}
