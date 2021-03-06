/*
Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1.  Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
2.  Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
    its contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ea.orbit.actors.providers.postgresql.test;

import com.ea.orbit.actors.IActor;
import com.ea.orbit.actors.OrbitStage;
import com.ea.orbit.actors.providers.postgresql.PostgreSQLStorageProvider;
import com.ea.orbit.actors.test.FakeClusterPeer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class PostgreSQLPersistenceTest {

    private String clusterName = "cluster." + Math.random();
    private Connection conn;
    private ObjectMapper objectMapper;

    @Test
    public void checkWritesTest() throws Exception {
        OrbitStage stage = createStage();
        assertEquals(0, count(IHelloActor.class));
        IHelloActor helloActor = IActor.getReference(IHelloActor.class, "300");
        helloActor.sayHello("Meep Meep").join();
        assertEquals(1, count(IHelloActor.class));
    }

    @Test
    public void checkReadTest() throws Exception {
        OrbitStage stage = createStage();
        IHelloActor helloActor = IActor.getReference(IHelloActor.class, "300");
        helloActor.sayHello("Meep Meep").join();
        assertEquals(readHelloState("300").lastName, "Meep Meep");
    }

    @Test
    public void checkClearTest() throws Exception {
        OrbitStage stage = createStage();
        assertEquals(0, count(IHelloActor.class));
        IHelloActor helloActor = IActor.getReference(IHelloActor.class, "300");
        helloActor.sayHello("Meep Meep").join();
        assertEquals(1, count(IHelloActor.class));
        helloActor.clear().join();
        assertEquals(0, count(IHelloActor.class));
    }

    @Test
    public void checkUpdateTest() throws Exception {
        OrbitStage stage = createStage();
        assertEquals(0, count(IHelloActor.class));
        IHelloActor helloActor = IActor.getReference(IHelloActor.class, "300");
        helloActor.sayHello("Meep Meep").join();
        assertEquals(1, count(IHelloActor.class));
        helloActor.sayHello("Peem Peem").join();
        assertEquals(readHelloState("300").lastName, "Peem Peem");
    }

    public OrbitStage createStage() throws Exception {
        OrbitStage stage = new OrbitStage();
        final PostgreSQLStorageProvider storageProvider = new PostgreSQLStorageProvider();
        storageProvider.setPort(5433);
        storageProvider.setDatabase("orbit");
        storageProvider.setUsername("orbit");
        storageProvider.setPassword("secret");
        stage.addProvider(storageProvider);
        stage.setClusterName(clusterName);
        stage.setClusterPeer(new FakeClusterPeer());
        stage.start().get();
        stage.bind();
        return stage;
    }

    @Before
    public void setup() throws Exception {
        Class.forName("org.postgresql.Driver");
        this.conn = DriverManager.getConnection("jdbc:postgresql://localhost:5433/orbit", "orbit", "secret");
        this.objectMapper =  new ObjectMapper();
    }

    @After
    public void cleanup() throws Exception {
        Statement dropStmt = this.conn.createStatement();
        dropStmt.execute("DROP TABLE actor_states");
        dropStmt.close();
        this.conn.close();
    }

    private int count(Class<?> actorInterface) throws Exception {
        Statement stmt = this.conn.createStatement();
        String name = actorInterface.getSimpleName();
        ResultSet results = stmt.executeQuery("SELECT COUNT(*) AS \"cnt\" FROM actor_states WHERE actor = '" + name + "'");
        results.next();
        int count = results.getInt("cnt");
        stmt.close();
        return count;
    }

    private HelloActor.State readHelloState(String identity) throws Exception {
        Statement stmt = this.conn.createStatement();
        ResultSet results = stmt.executeQuery(
                "SELECT state AS \"state\" FROM actor_states WHERE actor = '" + IHelloActor.class.getSimpleName()
                        + "' AND identity = '" + identity + "'");
        results.next();
        HelloActor.State state = objectMapper.readValue(results.getString("state"), HelloActor.State.class);
        stmt.close();
        return state;
    }

}
