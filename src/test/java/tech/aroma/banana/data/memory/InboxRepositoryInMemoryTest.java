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

package tech.aroma.banana.data.memory;

import java.util.List;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class InboxRepositoryInMemoryTest 
{
    
    @GenerateString
    private String userId;
    @GenerateString
    private String nameOfUser;
    private User user;
    
    @GeneratePojo
    private Message message;
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    private InboxRepositoryInMemory instance;

    @Before
    public void setUp()
    {
        user = new User()
            .setUserId(userId)
            .setName(nameOfUser);
        
        instance = new InboxRepositoryInMemory();
    }

    private void saveMessages(List<Message> messages) throws TException
    {
        for(Message message : messages)
        {
            instance.saveMessageForUser(message, user);
        }
    }
    
    @Test
    public void testSaveMessageForUser() throws Exception
    {
        instance.saveMessageForUser(message, user);
        
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, contains(message));
    }
    
    @DontRepeat
    public void testSaveMessageForUserWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.saveMessageForUser(message, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessageForUser(null, user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessageForUser(message, new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessageForUser(new Message(), user))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMessagesForUser() throws Exception
    {
        saveMessages(messages);
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, is(messages));
    }
    
    @DontRepeat
    @Test
    public void testGetMessagesForUserWhenEmpty() throws Exception
    {
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, is(empty()));
    }
    
    @DontRepeat
    @Test
    public void testGetMessagesForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMessagesForUser(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getMessagesForUser(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteMessageForUser() throws Exception
    {
    }

    @Test
    public void testDeleteAllMessagesForUser() throws Exception
    {
    }

    @Test
    public void testCountInboxForUser() throws Exception
    {
    }

}