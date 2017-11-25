/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.cache;

import lombok.Getter;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.util.Assert;

/**
 * A saved response generated from a previous response, and saved in the {@link ICacheManager}.
 */
final public class CachedResponse implements ResponseBody
{

    @Getter
    public final Header header;
    @Getter
    private final byte[] bytes;

    /**
     * The parent response body
     *
     * @param parent parent body
     */
    public CachedResponse(final ResponseBody parent)
    {
        Assert.notNull( parent );

        this.header = parent.getHeader();
        if ( parent.isText() )
        {
            this.bytes = parent.getContent().getBytes();
        } else
        {
            this.bytes = parent.getBytes();
        }
    }

    @Override
    public String getContent()
    {
        throw new KvantumException( "Cannot access text content in cached response" );
    }

    @Override
    public boolean isText()
    {
        return false;
    }

}