/*
 * Copyright 2016 Aroma Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.data.cassandra;

import com.datastax.driver.core.Row;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.cassandra.Tables.Activity;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MappersTest
{
    @GenerateString(GenerateString.Type.UUID)
    private String userId;

    @Mock
    private Row row;

    @GeneratePojo
    private Application app;

    @GeneratePojo
    private Message message;

    @GeneratePojo
    private User user;

    @GeneratePojo
    private Organization org;
    
    private Event event;
    
    private Reaction reaction;
    
    private List<Reaction> reactions;
    
    private List<String> serializedReactions;

    @Before
    public void setUp()
    {
        user.userId = userId;
        
        event = one(events());
        reaction = one(reactions());
        reactions = listOf(reactions(), 10);
        
        serializedReactions = reactions.parallelStream()
            .map(this::toJson)
            .filter(Objects::nonNull)
            .collect(toList());
    }
    
    private <T extends TBase> String toJson(T object)
    {
        try
        {
            return ThriftObjects.toJson(object);
        }
        catch (TException ex)
        {
            return null;
        }
    }

    @Test
    @DontRepeat
    public void testCannotInstantiate()
    {
        assertThrows(() -> new Mappers())
            .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testAppMapper()
    {
        Function<Row, Application> mapper = Mappers.appMapper();
        assertThat(mapper, notNullValue());
        
    }

    @Test
    public void testMessageMapper()
    {
        Function<Row, Message> mapper = Mappers.messageMapper();
        assertThat(mapper, notNullValue());
    }

    @Test
    public void testOrgMapper()
    {
        Function<Row, Organization> mapper = Mappers.orgMapper();
        assertThat(mapper, notNullValue());
    }

    @Test
    public void testUserMapper()
    {
        Function<Row, User> mapper = Mappers.userMapper();
        assertThat(mapper, notNullValue());
        
        Row row = rowFor(user);
        User result = mapper.apply(row);
        
        assertThat(result, notNullValue());
        assertThat(result.userId, is(user.userId));
        assertThat(result.firstName, is(user.firstName));
        assertThat(result.middleName, is(user.middleName));
        assertThat(result.lastName, is(user.lastName));
        assertThat(result.birthdate, is(user.birthdate));
    }
    
    private Row rowFor(User user)
    {
        Row row = mock(Row.class);
        
        when(row.getUUID(Tables.Users.USER_ID)).thenReturn(UUID.fromString(user.userId));
        when(row.getString(Tables.Users.FIRST_NAME)).thenReturn(user.firstName);
        when(row.getString(Tables.Users.MIDDLE_NAME)).thenReturn(user.middleName);
        when(row.getString(Tables.Users.LAST_NAME)).thenReturn(user.lastName);
        when(row.getTimestamp(Tables.Users.BIRTH_DATE)).thenReturn(new Date(user.birthdate));
        
        return row;
    }

    @Test
    public void testEventMapper() throws TException
    {
        Function<Row, Event> instance = Mappers.eventMapper();
        assertThat(instance, notNullValue());
        
        Row row = rowFor(event);
        Event result = instance.apply(row);
        assertThat(result, is(event));
    }

    private Row rowFor(Event event) throws TException
    {
        Row row = mock(Row.class);
        
        String serializedEvent = ThriftObjects.toJson(event);
        UUID actorId = UUID.fromString(event.userIdOfActor);
        UUID appId = UUID.fromString(event.applicationId);
        UUID eventId = UUID.fromString(event.eventId);
        
        when(row.getUUID(Activity.ACTOR_ID)).thenReturn(actorId);
        when(row.getUUID(Activity.APP_ID)).thenReturn(appId);
        when(row.getUUID(Activity.EVENT_ID)).thenReturn(eventId);
        when(row.getString(Activity.SERIALIZED_EVENT)).thenReturn(serializedEvent);
        
        return row;
    }

    @Test
    public void testReactionsMapper()
    {
        Function<Row, List<Reaction>> mapper = Mappers.reactionsMapper();
        assertThat(mapper, notNullValue());

        when(row.getList(Tables.Reactions.SERIALIZED_REACTIONS, String.class))
            .thenReturn(serializedReactions);
        
        List<Reaction> result = mapper.apply(row);
        assertThat(result, is(reactions));
    }

    @Test
    public void testDeserializeReaction() throws TException
    {
        String json = ThriftObjects.toJson(reaction);
        
        Reaction result = Mappers.deserializeReaction(json);
        assertThat(result, is(reaction));
    }

}
