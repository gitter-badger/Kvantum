/*
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

import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.socket.SocketContext;

import java.util.Collection;

@RequiredArgsConstructor
abstract class SocketReader
{

    protected final SocketContext socketContext;
    protected final RequestReader requestReader;

    boolean isDone()
    {
        return requestReader.isDone();
    }

    abstract void tick() throws Exception;

    Collection<String> getLines()
    {
        return this.requestReader.getLines();
    }
}
