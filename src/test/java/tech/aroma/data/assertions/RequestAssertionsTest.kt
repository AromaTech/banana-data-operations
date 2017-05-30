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

package tech.aroma.data.assertions

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sir.wellington.alchemy.collections.lists.Lists
import sir.wellington.alchemy.collections.sets.Sets
import tech.aroma.data.AromaGenerators
import tech.aroma.thrift.*
import tech.aroma.thrift.authentication.*
import tech.aroma.thrift.channels.*
import tech.aroma.thrift.generators.ApplicationGenerators
import tech.aroma.thrift.generators.ImageGenerators
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.arguments.AlchemyAssertion
import tech.sirwellington.alchemy.arguments.FailedAssertionException
import tech.sirwellington.alchemy.generator.EnumGenerators
import tech.sirwellington.alchemy.test.junit.runners.*

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import tech.aroma.data.failedAssertion
import tech.aroma.thrift.generators.ChannelGenerators.mobileDevices
import tech.aroma.thrift.generators.ImageGenerators.appIcons
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.CollectionGenerators.listOf
import tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers
import tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs
import tech.sirwellington.alchemy.generator.ObjectGenerators.pojos
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.generator.StringGenerators.uuids
import tech.sirwellington.alchemy.test.junit.ExceptionOperation
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

/**

 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner::class)
class RequestAssertionsTest
{

    @GenerateString(UUID)
    private lateinit var validId: String

    @GenerateString(ALPHABETIC)
    private lateinit var invalidId: String

    @GeneratePojo
    private lateinit var appToken: ApplicationToken

    @GeneratePojo
    private lateinit var userToken: UserToken

    @GeneratePojo
    private lateinit var authToken: AuthenticationToken

    @GeneratePojo
    private lateinit var app: Application

    @GeneratePojo
    private lateinit var message: Message

    @GeneratePojo
    private lateinit var organization: Organization

    @GeneratePojo
    private lateinit var user: User

    @GenerateString
    private lateinit var string: String

    @Before
    fun setUp()
    {

        app = ApplicationGenerators.applications().get()
        app.applicationId = validId
        app.organizationId = validId

        message.messageId = validId
        message.applicationId = validId
        user.userId = validId
        organization.organizationId = validId

        organization.owners = Lists.createFrom<String>(validId)
    }

    @DontRepeat
    @Test
    fun testConstuctor()
    {
        assertThrows(ExceptionOperation { RequestAssertions() }).isInstanceOf(IllegalAccessException::class.java)
    }

    @Test
    fun testValidApplication()
    {
        val assertion = RequestAssertions.validApplication()
        assertThat(assertion, notNullValue())

        assertion.check(app)

        assertThrows { assertion.check(null) }.failedAssertion()

        val empty = Application()
        assertThrows { assertion.check(empty) }.failedAssertion()

        val appWithInvalidId = Application(app)
                .setApplicationId(this.invalidId)

        assertThrows { assertion.check(appWithInvalidId) }.failedAssertion()

        val appWithoutOwners = Application(app).setOwners(Sets.emptySet<String>())
        assertThrows { assertion.check(appWithoutOwners) }.failedAssertion()

        val appWithInvalidOwners = Application(app).setOwners(Sets.copyOf(listOf(alphabeticString())))
        assertThrows { assertion.check(appWithInvalidOwners) }
    }

    @Test
    fun testValidUser()
    {
        val assertion = RequestAssertions.validUser()
        assertThat(assertion, notNullValue())

        assertion.check(user)

        assertThrows { assertion.check(null) }.failedAssertion()

        assertThrows { assertion.check(User()) }.failedAssertion()

        val userWithInvalidId = User(user)
                .setUserId(invalidId)

        assertThrows { assertion.check(userWithInvalidId) }.failedAssertion()
    }

    @Test
    fun testIsNullOrEmpty()
    {
        assertThat(RequestAssertions.isNullOrEmpty(string), `is`(false))
        assertThat(RequestAssertions.isNullOrEmpty(""), `is`(true))
        assertThat(RequestAssertions.isNullOrEmpty(null), `is`(true))
    }

    @Test
    fun testValidMessage()
    {
        val assertion = RequestAssertions.validMessage()
        assertThat(assertion, notNullValue())

        assertion.check(message)
    }

    @DontRepeat
    @Test
    fun testValidMessageWithBadMessages()
    {
        val assertion = RequestAssertions.validMessage()

        assertThrows { assertion.check(null) }.failedAssertion()

        val emptyMessage = Message()
        assertThrows { assertion.check(emptyMessage) }.failedAssertion()

        val messageWithoutTitle = emptyMessage.setMessageId(one(uuids))
        assertThrows { assertion.check(messageWithoutTitle) }.failedAssertion()

        val messageWithInvalidId = Message(message)
                .setMessageId(invalidId)
        assertThrows { assertion.check(messageWithInvalidId) }.failedAssertion()

        val messageWithInvalidAppId = Message(message)
                .setApplicationId(invalidId)
        assertThrows { assertion.check(messageWithInvalidAppId) }.failedAssertion()

    }

    @Test
    fun testValidApplicationId()
    {
        val assertion = RequestAssertions.validApplicationId()
        assertThat(assertion, notNullValue())

        assertion.check(validId)

        assertThrows { assertion.check(invalidId) }.failedAssertion()
    }

    @Test
    fun testValidMessageId()
    {
        val assertion = RequestAssertions.validMessageId()
        assertThat(assertion, notNullValue())

        assertion.check(validId)

        assertThrows { assertion.check(invalidId) }.failedAssertion()
    }

    @Test
    fun testValidUserId()
    {
        val assertion = RequestAssertions.validUserId()
        assertThat(assertion, notNullValue())

        assertion.check(validId)

        assertThrows { assertion.check(invalidId) }.failedAssertion()
    }

    @Test
    fun testValidOrganization()
    {
        val assertion = RequestAssertions.validOrganization()

        assertThrows { assertion.check(null) }.failedAssertion()

        val emptyOrg = Organization()
        assertThrows { assertion.check(emptyOrg) }
        
        val orgWithoutName = emptyOrg.setOrganizationId(validId)
        assertThrows { assertion.check(orgWithoutName) }.failedAssertion()

        val orgWithInvalidId = Organization(organization)
                .setOrganizationId(invalidId)
        assertThrows { assertion.check(orgWithInvalidId) }.failedAssertion()

        val orgWithBadOwners = Organization(organization)
                .setOwners(listOf(alphabeticString(2)))
        assertThrows { assertion.check(orgWithBadOwners) }.failedAssertion()

    }

    @Test
    fun testValidOrgId()
    {
        val assertion = RequestAssertions.validOrgId()
        assertThat(assertion, notNullValue())

        assertion.check(validId)

        assertThrows { assertion.check(invalidId) }.failedAssertion()
    }

    @Test
    fun testTokenContainingOwnerId()
    {
        val assertion = RequestAssertions.tokenContainingOwnerId()
        assertThat(assertion, notNullValue())

        assertion.check(authToken)
    }

    @Test
    fun testTokenContainingOwnerIdWithBadArgs()
    {
        val assertion = RequestAssertions.tokenContainingOwnerId()
        assertThat(assertion, notNullValue())

        assertThrows { assertion.check(null) }.failedAssertion()

        val emptyToken = AuthenticationToken()
        assertThrows { assertion.check(emptyToken) }.failedAssertion()

    }

    @Test
    fun testValidAndroidDevice()
    {
        val good = one(pojos(AndroidDevice::class.java))
        val bad = AndroidDevice()

        val assertion = RequestAssertions.validAndroidDevice()
        assertThat(assertion, notNullValue())

        //Check with good
        assertion.check(good)

        //Check with bad
        assertThrows { assertion.check(bad) }
    }

    @Test
    fun testValidiOSDevice()
    {
        val good = one(pojos(IOSDevice::class.java))
        val bad = IOSDevice()

        val assertion = RequestAssertions.validiOSDevice()

        //Check with good
        assertion.check(good)

        //Check with bad
        assertThrows { assertion.check(bad) }
    }

    @Test
    fun testValidMobileDevice()
    {
        val good = one(mobileDevices())
        val bad = MobileDevice()

        val assertion = RequestAssertions.validMobileDevice()
        assertThat(assertion, notNullValue())

        //Check with good
        assertion.check(good)

        //Check with bad
        assertThrows { assertion.check(bad) }
    }

    @Test
    @Throws(Exception::class)
    fun testValidLengthOfTime()
    {
        val value = one(positiveLongs())
        val unit = EnumGenerators.enumValueOf(TimeUnit::class.java).get()

        val time = LengthOfTime().setUnit(unit).setValue(value)

        val assertion = RequestAssertions.validLengthOfTime()
        assertThat(assertion, notNullValue())

        assertion.check(time)
    }


    @Test
    @Throws(Exception::class)
    fun testValidLengthOfTimeWithInvalid()
    {

        val assertion = RequestAssertions.validLengthOfTime()

        val value = one(negativeIntegers()).toLong()
        val unit = EnumGenerators.enumValueOf(TimeUnit::class.java).get()

        val time = LengthOfTime(unit, value)

        assertThrows { assertion.check(time) }.isInstanceOf(FailedAssertionException::class.java)

        time.unsetUnit()
        assertThrows { assertion.check(time) }.isInstanceOf(FailedAssertionException::class.java)

    }

    @Test
    @Throws(Exception::class)
    fun testValidReaction()
    {
        val assertion = RequestAssertions.validReaction()
        assertThat(assertion, notNullValue())

        val reaction = Reaction()
        assertion.check(reaction)

        assertThrows { assertion.check(null) }.isInstanceOf(FailedAssertionException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testValidImage()
    {
        val assertion = RequestAssertions.validImage()
        assertThat(assertion, notNullValue())

        val image = one(appIcons())
        assertion.check(image)

    }

    @Test
    @Throws(Exception::class)
    fun testValidImageWithBadArgs()
    {
        val assertion = RequestAssertions.validImage()
    }


}
