/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.request;

import com.plotsquared.iserver.api.util.Assert;

/**
 * A very simple representation of a cookie
 */
public final class Cookie
{

    private final String name;
    private final String value;

    /**
     * Create a new cookie (Essentially name=value)
     *
     * @param name  Cookie name
     * @param value Cookie value
     */
    public Cookie(final String name, final String value)
    {
        Assert.notNull( name, value );

        this.name = name;
        this.value = value;
    }

    /**
     * Get the cookie name
     * @return Cookie name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the cookie value
     * @return Cookie value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Get the full cookie, as per the HTTP protocol format
     * @return name=value
     */
    public String toString()
    {
        return name + "=" + value;
    }

}
