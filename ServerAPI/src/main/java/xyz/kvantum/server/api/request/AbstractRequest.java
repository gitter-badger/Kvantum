/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.request.post.PostRequest;
import xyz.kvantum.server.api.response.ResponseCookie;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.ITempFileManager;
import xyz.kvantum.server.api.util.MapUtil;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.Validatable;
import xyz.kvantum.server.api.util.VariableHolder;
import xyz.kvantum.server.api.util.VariableProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The HTTP Request Class
 * <p>
 * This is generated when a client
 * connects to the web server, and
 * contains the information needed
 * for the server to generate a
 * proper response. This is what
 * everything is based around!
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractRequest implements
        ProviderFactory<AbstractRequest>,
        VariableProvider,
        Validatable,
        RequestChild,
        VariableHolder
{

    @SuppressWarnings("ALL")
    public static final String INTERNAL_REDIRECT = "internalRedirect";
    public static final String ALTERNATE_OUTCOME = "alternateOutcome";

    public Set<ResponseCookie> postponedCookies = new HashSet<>();
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private ProtocolType protocolType;
    @Getter(AccessLevel.PROTECTED)
    private Map<String, Object> meta = new HashMap<>();
    @Getter
    private Map<AsciiString, AsciiString> headers = new HashMap<>();
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private ListMultimap<AsciiString, Cookie> cookies = ArrayListMultimap.create();
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private Query query;
    @Setter
    private PostRequest postRequest;
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private SocketContext socket;
    @NonNull
    @Setter
    private ISession session;
    @Setter
    @Getter
    private boolean valid = true;
    @Setter(AccessLevel.PROTECTED)
    private Authorization authorization;
    @Getter
    @Setter
    private byte[] overloadBytes;
    @Getter
    private final Map<String, ProviderFactory<? extends VariableProvider>> models = new HashMap<>();

    public ITempFileManager getTempFileManager()
    {
        return this.socket.getTempFileManager();
    }

    @Override
    public AbstractRequest getParent()
    {
        return this;
    }

    public void removeMeta(final String metaKey)
    {
        this.meta.remove( Assert.notEmpty( metaKey ) );
    }

    public Optional<Authorization> getAuthorization()
    {
        return Optional.ofNullable( authorization );
    }

    @Override
    public Optional<AbstractRequest> get(final AbstractRequest r)
    {
        return Optional.of( this );
    }

    @Override
    public String providerName()
    {
        return null;
    }

    @Override
    public boolean contains(final String variable)
    {
        return getVariables().containsKey( Assert.notNull( variable ) );
    }

    @Override
    public Object get(final String variable)
    {
        return getVariables().get( Assert.notNull( variable ) );
    }

    @Override
    public Map<String, Object> getAll()
    {
        return MapUtil.convertMap( getVariables(), (s) -> s );
    }

    public void useAlternateOutcome(final String identifier)
    {
        this.addMeta( ALTERNATE_OUTCOME, Assert.notNull( identifier ) );
    }

    public void addModel(final String name, final VariableProvider provider)
    {
        final ProviderFactory<VariableProvider> providerFactory = new ProviderFactory<VariableProvider>()
        {
            @Override
            public Optional<VariableProvider> get(AbstractRequest r)
            {
                return Optional.of( provider );
            }

            @Override
            public String providerName()
            {
                return name;
            }
        };
        this.models.put( name, providerFactory );
    }

    public abstract void onCompileFinish();

    /**
     * Get the PostRequest
     *
     * @return PostRequest if exists, null if not
     */
    public PostRequest getPostRequest()
    {
        return this.postRequest;
    }

    protected abstract AbstractRequest newRequest(final String query);

    /**
     * Get a request header. These
     * are sent by the client, and
     * are not to be confused with the
     * response headers.
     *
     * @param name Header Name
     * @return The header value, if the header exists. Otherwise an empty string will be returned.
     */
    public AsciiString getHeader(final AsciiString name)
    {
        Assert.notNull( name );

        if ( this.headers.containsKey( name.toLowerCase() ) )
        {
            return this.headers.get( name.toLowerCase() );
        }

        return AsciiString.empty;
    }

    public AsciiString getHeader(final String name)
    {
        return getHeader( AsciiString.of( name ) );
    }

    /**
     * Build a string for logging
     *
     * @return Compiled string
     */
    public String buildLog()
    {
        String msg = Message.REQUEST_LOG.toString();
        for ( final Object a : new CharSequence[]{ socket.getAddress().toString(), getHeader(
                "User-Agent" ), getHeader( "query" ), getHeader( "Host" ), this.query.buildLog() } )
        {
            msg = msg.replaceFirst( "\\{}", a.toString() );
        }
        return msg;
    }

    /**
     * Add a meta value, which can
     * be used to share an object
     * throughout the lifespan of
     * the request.
     *
     * @param name Key (which will be used to get the meta value)
     * @param var  Value (Any object will do)
     * @see #getMeta(String) To get the value
     */
    public void addMeta(final String name, final Object var)
    {
        Assert.notNull( name );
        meta.put( name, var );
    }

    public void internalRedirect(final String url)
    {
        Assert.notNull( url );

        this.addMeta( INTERNAL_REDIRECT, newRequest( url ) );
        Message.INTERNAL_REDIRECT.log( url );
    }

    /**
     * Get a meta value
     *
     * @param name The key
     * @return Meta value if exists, else null
     * @see #addMeta(String, Object) To set a meta value
     */
    public Object getMeta(final String name)
    {
        Assert.notNull( name );

        if ( !meta.containsKey( name ) )
        {
            return null;
        }
        return meta.get( name );
    }

    @Override
    @SuppressWarnings("ALL")
    public Map<String, String> getVariables()
    {
        return (Map<String, String>) getMeta( "variables" );
    }

    @Override
    public String toString()
    {
        return this.socket.getAddress().getHostName();
    }

    public boolean hasMeta(final String key)
    {
        return this.meta.containsKey( Assert.notNull( key ) );
    }

    public Map<String, Object> getAllMeta()
    {
        return new HashMap<>( this.meta );
    }

    public abstract void requestSession();

    @SuppressWarnings("ALL")
    @SneakyThrows
    public <T> T getMetaUnsafe(final String key)
    {
        return (T) this.getMeta( key );
    }

    public ISession getSession()
    {
        if ( this.session == null )
        {
            this.requestSession();
        }
        return this.session;
    }

    public abstract void dumpRequest();

    /**
     * The query, for example:
     * "http://localhost/query?example=this"
     */
    final public static class Query
    {

        @Getter
        private final HttpMethod method;
        @Getter
        private final String resource;
        @Getter
        private final Map<String, String> parameters = new HashMap<>();

        /**
         * The query constructor
         *
         * @param method   Request Method
         * @param resource The requested resource
         */
        public Query(final HttpMethod method, final ProtocolType protocolType, final String resource)
        {
            Assert.notNull( method, resource );

            String resourceName = resource;

            final String illegalBeginning = protocolType == ProtocolType.HTTP ? "http" : "https";

            if ( resourceName.startsWith( illegalBeginning ) )
            {
                // For some reason the request has been parsed as http://hostname:port/resource
                // rather than /resource, let's fix that
                // We start by calculating the number of characters to be ignored in the split
                // which is http or https + "://"
                final int ignoredChars = illegalBeginning.length() + 3;
                // We'll now split hostname:port/resource into
                // hostname + resource
                resourceName = StringUtils.split( resourceName.substring( ignoredChars ), "/", 2 )[ 1 ];
            }

            this.method = method;
            if ( resourceName.contains( "?" ) )
            {
                final String[] parts = resource.split( "\\?" );
                if ( parts.length > 1 )
                {
                    String parameters = parts[ 1 ];
                    try
                    {
                        parameters = URLDecoder.decode( parameters, StandardCharsets.UTF_8.toString() );
                    } catch ( final UnsupportedEncodingException ignore )
                    {
                        if ( CoreConfig.debug )
                        {
                            ignore.printStackTrace();
                        }
                    }
                    final String[] subParts = parameters.split( "&" );
                    resourceName = parts[ 0 ];
                    for ( final String part : subParts )
                    {
                        final String[] subSubParts = StringUtils.split( part, "=", 2 );
                        this.parameters.put( subSubParts[ 0 ], subSubParts[ 1 ] );
                    }
                } else
                {
                    resourceName = resourceName.substring( 0, resourceName.length() - 1 );
                }
            }
            this.resource = resourceName;
        }

        /**
         * Build a logging string... for logging?
         *
         * @return compiled string
         */
        String buildLog()
        {
            return "Query: [Method: " + method.toString() + " | Resource: " + resource + "]";
        }

        public String getFullRequest()
        {
            final String parameters = MapUtil.join( getParameters(), "=", "&" );
            return parameters.isEmpty() ? resource : resource + "?" + parameters;
        }

    }

    /**
     * Used to handle HTTP authentication
     */
    @SuppressWarnings("unused")
    final public static class Authorization
    {

        @Getter
        private final AsciiString mechanism;
        @Getter
        private final AsciiString username;
        @Getter
        private final AsciiString password;

        Authorization(final AsciiString input)
        {
            final List<AsciiString> parts = input.split( "\\s" );
            this.mechanism = parts.get( 1 );
            final val auth = AsciiString.of( Base64.getDecoder().decode( parts.get( 2 ).getValue() ) )
                    .split( ":" );
            if ( auth.size() < 2 )
            {
                this.username = null;
                this.password = null;
            } else
            {
                this.username = auth.get( 0 );
                this.password = auth.get( 1 );
            }
        }

        public boolean isValid()
        {
            return this.mechanism != null && this.username != null && this.password != null;
        }

    }

}
