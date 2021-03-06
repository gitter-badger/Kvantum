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
package xyz.kvantum.server.api.mocking;

import lombok.Data;
import lombok.Getter;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Data
@SuppressWarnings("unused")
public class MockAccount implements IAccount
{

    @Getter
    private static final KvantumPojoFactory<IAccount> kvantumPojoFactory =
            KvantumPojoFactory.forClass( IAccount.class );

    private static final Random random = new Random();

    private Map<String, String> rawData = new HashMap<>();
    private int id = random.nextInt( 10000 );
    private String username = UUID.randomUUID().toString();
    private IAccountManager manager;
    private Collection<AccountRole> accountRoles = new HashSet<>();

    @Override
    public void internalMetaUpdate(final String key, final String value)
    {
        this.rawData.put( "meta." + key, value );
    }

    @Override
    public boolean passwordMatches(final String password)
    {
        return true;
    }

    @Override
    public Optional<String> getData(final String key)
    {
        return Optional.ofNullable( rawData.getOrDefault( key, null ) );
    }

    @Override
    public void setData(final String key, final String value)
    {
        this.rawData.put( key, value );
    }

    @Override
    public void removeData(final String key)
    {
        this.rawData.remove( key );
    }

    @Override
    public void addRole(final AccountRole role)
    {
        this.accountRoles.add( role );
    }

    @Override
    public void removeRole(final AccountRole role)
    {
        this.accountRoles.remove( role );
    }

    @Override
    public String getSuppliedPassword()
    {
        return "";
    }

    @Override
    public boolean isPermitted(final String permissionKey)
    {
        return true;
    }

    @Override
    public KvantumPojo<IAccount> toKvantumPojo()
    {
        return kvantumPojoFactory.of( this );
    }
}
