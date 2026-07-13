package nl.devtribe.in;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import nl.devtribe.out.CreateStapleOutCommand;
import nl.devtribe.out.CreateStapleOutResponse;
import nl.devtribe.out.StapleProducer;

@ApplicationScoped
@Path("/create")
public class CreateResource {

    private final StapleProducer StapleProducer;

    public CreateResource(StapleProducer StapleProducer) {
        this.StapleProducer = StapleProducer;
    }

    @POST
    public CreateStapleResponse create(CreateStapleRequest createStapleRequest) {
        CreateStapleOutResponse staple = StapleProducer.staple(toCommand(createStapleRequest));
        return toCreateStapleResponse(staple);
    }

    private CreateStapleOutCommand toCommand(CreateStapleRequest createStapleRequest) {
        return new CreateStapleOutCommand(createStapleRequest.prefix(), createStapleRequest.suffix());
    }

    private CreateStapleResponse toCreateStapleResponse(CreateStapleOutResponse createStapleOutResponse) {
        return new CreateStapleResponse(createStapleOutResponse.prefix(), createStapleOutResponse.suffix());
    }
}
