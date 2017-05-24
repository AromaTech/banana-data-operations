/*
 * Copyright 2017 RedRoma, Inc.
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


package tech.aroma.data.sql

import com.google.inject.*
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.isEmpty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.data.sql.serializers.ModuleSerializers
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion
import java.util.*

/**
 * @author SirWellington
 */

object TestingResources
{

    var LOG = LoggerFactory.getLogger(TestingResources::class.java)

    private val injector = Guice.createInjector(ModuleTesting(),
                                                ModuleSerializers())

    fun connectToDatabase(): JdbcTemplate
    {
        return injector.getInstance(JdbcTemplate::class.java)
    }

    val messageSerializer: DatabaseSerializer<Message>
        get()
        {
            val literal = object : TypeLiteral<DatabaseSerializer<Message>>()
            {

            }
            return injector.getInstance(Key.get(literal))
        }
}

val isNull = Matcher(Objects::isNull)
val notNull = !isNull
val notEmpty = !isEmpty


fun ThrowableAssertion.invalidArg(): ThrowableAssertion
{
    return this.isInstanceOf(tech.aroma.thrift.exceptions.InvalidArgumentException::class.java)
}

fun ThrowableAssertion.operationError(): ThrowableAssertion
{
    return this.isInstanceOf(tech.aroma.thrift.exceptions.OperationFailedException::class.java)
}