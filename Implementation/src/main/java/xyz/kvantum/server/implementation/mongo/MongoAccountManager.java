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
package xyz.kvantum.server.implementation.mongo;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.implementation.Account;
import xyz.kvantum.server.implementation.MongoApplicationStructure;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
final public class MongoAccountManager implements IAccountManager
{

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<IAccount> EMPTY_OPTIONAL = Optional.empty();
    @Getter
    private final MongoApplicationStructure applicationStructure;

    private DBCollection counters;

    private static String getNewSalt()
    {
        return BCrypt.gensalt();
    }

    private static String hashPassword(final String password, final String salt)
    {
        return BCrypt.hashpw( password, salt );
    }

    @Override
    public void setup() throws Exception
    {
        DB database = applicationStructure.getMongoClient().getDB( CoreConfig.MongoDB.dbMorphia );
        this.counters = database.getCollection( "counters" );
        if ( !this.counters.find( new BasicDBObject( "_id", "userId" ) ).hasNext() )
        {
            this.counters.insert( new BasicDBObject( "_id", "userId" ).append( "seq", 0 ) );
        }

        this.checkAdmin();
    }

    private int getNextId()
    {
        return (int) counters.findAndModify( new BasicDBObject( "_id", "userId" ), new BasicDBObject( "$inc",
                new BasicDBObject( "seq", 1 ) ) ).get( "seq" );
    }

    @Override
    public Optional<IAccount> createAccount(final IAccount temporary)
    {
        final String username = temporary.getUsername();
        final String password = temporary.getSuppliedPassword();

        Assert.notEmpty( username );
        Assert.notEmpty( password );

        if ( getAccount( username ).isPresent() )
        {
            return EMPTY_OPTIONAL;
        }

        final String hashedPassword = hashPassword( password, getNewSalt() );
        final Account account = new Account( getNextId(), username, hashedPassword );
        account.setManager( this );
        this.applicationStructure.getMorphiaDatastore().save( account );

        return Optional.of( account );
    }

    @Override
    public Optional<IAccount> createAccount(final String username, final String password)
    {
        return createAccount( new Account( -1, username, password ) );
    }

    @Override
    public Optional<IAccount> getAccount(final String username)
    {
        Assert.notEmpty( username );

        Optional<Integer> accountId = ServerImplementation.getImplementation().getCacheManager().getCachedId( username );
        Optional<IAccount> ret = EMPTY_OPTIONAL;
        if ( accountId.isPresent() )
        {
            ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId.get() );
        }
        if ( ret.isPresent() )
        {
            return ret;
        }
        ret = Optional.ofNullable( applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                .field( "username" ).equal( username ).get() );
        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        ret.ifPresent( account -> account.setManager( this ) );
        return ret;
    }

    @Override
    public Optional<IAccount> getAccount(final int accountId)
    {
        Optional<IAccount> ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount(
                accountId );
        if ( ret.isPresent() )
        {
            return ret;
        }

        ret = Optional.ofNullable( applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                .field( "userId" ).equal( accountId ).get() );
        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        ret.ifPresent( account -> account.setManager( this ) );
        return ret;
    }

    @Override
    public void setData(final IAccount account, final String key, final String value)
    {
        applicationStructure.getMorphiaDatastore().update(
                applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                        .field( "username" ).equal( account.getUsername() ),
                applicationStructure.getMorphiaDatastore().createUpdateOperations( Account.class )
                        .set( "data." + key, value )
        );
    }

    @Override
    public void removeData(final IAccount account, final String key)
    {
        applicationStructure.getMorphiaDatastore().update(
                applicationStructure.getMorphiaDatastore().createQuery( Account.class )
                        .field( "username" ).equal( account.getUsername() ),
                applicationStructure.getMorphiaDatastore().createUpdateOperations( Account.class )
                        .removeFirst( "data." + key )
        );
    }

    @Override
    public void loadData(final IAccount account)
    {
        // Done automatically
    }

    @Override
    public ImmutableList<? extends IAccount> findAll()
    {
        final List<? extends IAccount> list = applicationStructure.getMorphiaDatastore().find( Account.class ).asList();
        final ImmutableList.Builder<IAccount> builder = ImmutableList.builderWithExpectedSize( list.size() );
        return builder.addAll( list ).build();
    }

    @Override
    public void deleteAccount(@NonNull final IAccount account)
    {
        this.applicationStructure.getMorphiaDatastore().delete( account );
        ServerImplementation.getImplementation().getCacheManager().deleteAccount( account );
    }

}
