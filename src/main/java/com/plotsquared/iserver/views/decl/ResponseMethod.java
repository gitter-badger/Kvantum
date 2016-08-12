package com.plotsquared.iserver.views.decl;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final public class ResponseMethod implements BiConsumer<Request, Response>
{

    private final Method method;
    private final Object instance;

    public ResponseMethod(final Method method, final Object instance)
    {
        Assert.notNull( method, instance );

        this.method = method;
        this.method.setAccessible( true );
        this.instance = instance;
    }

    public Response handle(final Request r)
    {
        Assert.notNull( r );

        try
        {
            return (Response) this.method.invoke( instance, r );
        } catch ( IllegalAccessException | InvocationTargetException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void accept(Request request, Response response)
    {
        response.copyFrom( handle( request ) );
    }
}
