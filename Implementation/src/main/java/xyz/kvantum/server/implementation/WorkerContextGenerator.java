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
import xyz.kvantum.nanotube.Generator;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.socket.SocketContext;

/**
 * Class responsible for creating {@link WorkerContext} instances
 * from incoming {@link SocketContext} instances
 */
@RequiredArgsConstructor
final class WorkerContextGenerator extends Generator<SocketContext, WorkerContext>
{

    private final Kvantum server;
    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;

    @Override
    protected WorkerContext handle(final SocketContext socketContext) throws Throwable
    {
        final WorkerContext workerContext = new WorkerContext( server, workerProcedureInstance );
        workerContext.setSocketContext( socketContext );
        return workerContext;
    }

}
