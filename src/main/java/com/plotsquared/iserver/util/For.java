package com.plotsquared.iserver.util;

import java.util.function.Consumer;

public class For
{

    private final boolean increase;
    private int currentIteration;
    private final int iterations;

    private For(final boolean increase, final int iterations)
    {
        this.increase = increase;
        this.iterations = iterations;
        if ( increase )
        {
            currentIteration = 0;
        }
        else
        {
            currentIteration = iterations;
        }
    }

    public void perform(Consumer<Integer> consumer)
    {
        if ( increase )
        {
            for (; currentIteration < iterations; currentIteration++)
            {
                consumer.accept( currentIteration );
            }
        } else
        {
            for (; currentIteration > 0; currentIteration--)
            {
                consumer.accept( currentIteration );
            }
        }
    }

    public static For upTo(int n)
    {
        return new For( true, n );
    }

    public static For upToInclude(int n)
    {
        return upTo( n + 1 );
    }

    public static For downFrom(int n)
    {
        return new For( false, n );
    }
}
