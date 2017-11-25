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
package xyz.kvantum.server.implementation;

import lombok.Getter;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ApplicationStructure;
import xyz.kvantum.server.api.util.SQLiteManager;

import java.io.IOException;
import java.sql.SQLException;

public abstract class SQLiteApplicationStructure extends ApplicationStructure
{

    @Getter
    private final SQLiteManager databaseManager;

    public SQLiteApplicationStructure(final String applicationName)
    {
        super( applicationName );
        try
        {
            this.databaseManager = new SQLiteManager( this.applicationName );
        } catch ( final IOException | SQLException e )
        {
            throw new KvantumException( e );
        }
        this.accountManager = createNewAccountManager();
        Logger.info( "Initialized SQLiteApplicationStructure: %s", this.applicationName );
    }

}