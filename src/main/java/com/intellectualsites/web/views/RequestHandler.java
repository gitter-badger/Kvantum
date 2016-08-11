package com.intellectualsites.web.views;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.views.requesthandler.MiddlewareQueue;
import com.intellectualsites.web.views.requesthandler.MiddlewareQueuePopulator;
import lombok.Getter;
import lombok.NonNull;

/**
 * A handler which uses an incoming
 * request to generate a response
 */
public abstract class RequestHandler {

    @Getter
    protected final MiddlewareQueuePopulator middlewareQueuePopulator = new MiddlewareQueuePopulator();

    /**
     * Used to check if a request is to be served
     * by this RequestHandler
     * @param request Incoming request
     * @return True if the request can be served by this handler
     *         False if not
     */
    abstract public boolean matches(final Request request);

    final public Response handle(@NonNull final Request request) {
        final MiddlewareQueue middlewareQueue = middlewareQueuePopulator.generateQueue();
        middlewareQueue.handle(request);
        if (!middlewareQueue.finished()) {
            Server.getInstance().log("Skipping request as a middleware broke the chain!");
            return null;
        }
        return generate(request);
    }

    /**
     * Generate a response for the incoming request
     * @param request The incoming request
     * @return The generated response
     */
    abstract public Response generate(final Request request);

    /**
     * Get the view specific factory (if it exists)
     *
     * @param r Request IN
     * @return Null by default, or the ProviderFactory (if set by the view)
     */
    public ProviderFactory getFactory(final Request r) {
        return null;
    }

    /**
     * Get the unique internal name of the handler
     * @return Handler name
     */
    abstract public String getName();

}
