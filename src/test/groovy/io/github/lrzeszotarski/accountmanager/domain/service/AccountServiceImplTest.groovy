package io.github.lrzeszotarski.accountmanager.domain.service

import io.github.lrzeszotarski.accountmanager.domain.entity.Account
import io.github.lrzeszotarski.accountmanager.domain.entity.Event
import io.github.lrzeszotarski.accountmanager.domain.repository.AccountRepository
import io.github.lrzeszotarski.accountmanager.domain.repository.EventRepository
import spock.lang.Specification

class AccountServiceImplTest extends Specification {

    def accountRepository = Mock(AccountRepository)

    def eventRepository = Mock(EventRepository)

    def identifierService = Mock(IdentifierService)

    def eventStatisticsService = Mock(EventStatisticsService)

    def testedInstance = new AccountServiceImpl(accountRepository, eventRepository, identifierService, eventStatisticsService)

    def "test createAccount"() {
        given:
        def account = new Account()
        def uuid = UUID.randomUUID()
        when:
        def createdAccount = testedInstance.createAccount(account)
        then:
        1 * accountRepository.save(account) >> account
        1 * identifierService.generateIdentifier() >> uuid
        account == createdAccount
        createdAccount.getAccountId() == uuid
    }

    def "test updateAccount"() {
        given:
        def uuid = UUID.randomUUID()
        def existingAccount = new Account(accountId: uuid, name: "Sample Name")
        def updatedAccount = new Account(accountId: uuid, name: "New Sample Name")
        when:
        def accountAfterUpdate = testedInstance.updateAccount(updatedAccount)
        then:
        1 * accountRepository.findByAccountId(existingAccount.getAccountId()) >> existingAccount
        accountAfterUpdate.getName() == "New Sample Name"
    }

    def "test findAccount"() {
        given:
        def uuid = UUID.randomUUID()
        def account = new Account(accountId: uuid)
        when:
        def searchedAccount = testedInstance.findAccount(uuid)
        then:
        1 * accountRepository.findByAccountId(uuid) >> account
        account == searchedAccount
        searchedAccount.getAccountId() == uuid
    }

    def "test createEvent"() {
        given:
        def accountUuid = UUID.randomUUID()
        def eventUuid = UUID.randomUUID()
        def account = new Account(accountId: accountUuid, eventList: new ArrayList<Event>())
        def event = new Event()
        when:
        def createdEvent = testedInstance.createEvent(accountUuid, event)
        then:
        1 * accountRepository.findByAccountId(accountUuid) >> account
        1 * identifierService.generateIdentifier() >> eventUuid
        1 * eventStatisticsService.updateStatistics(account, event)
        account.getEventList().size() == 1
        account.getEventList().get(0).getEventId() == eventUuid
        account.getEventList().get(0) == createdEvent
        event.getAccount() == account
    }

    def "test findEvent if match with account"() {
        given:
        def accountId = UUID.randomUUID()
        def eventId = UUID.randomUUID()
        def account = new Account(accountId: accountId)
        def event = new Event(eventId: eventId, account: account)
        when:
        def searchedEvent = testedInstance.findEvent(accountId, eventId)
        then:
        1 * eventRepository.findByEventId(eventId) >> event
        event == searchedEvent
        searchedEvent.getEventId() == eventId
    }

    def "test findEvent if not matched with account"() {
        given:
        def accountId = UUID.randomUUID()
        def otherAccountId = UUID.randomUUID()
        def eventId = UUID.randomUUID()
        def otherAccount = new Account(accountId: otherAccountId)
        def event = new Event(eventId: eventId, account: otherAccount)
        when:
        def searchedEvent = testedInstance.findEvent(accountId, eventId)
        then:
        1 * eventRepository.findByEventId(eventId) >> event
        searchedEvent == null
    }

    def "test findEvent if event not found"() {
        given:
        def accountId = UUID.randomUUID()
        def otherAccountId = UUID.randomUUID()
        def eventId = UUID.randomUUID()
        def otherAccount = new Account(accountId: otherAccountId)
        def event = new Event(eventId: eventId, account: otherAccount)
        when:
        def searchedEvent = testedInstance.findEvent(accountId, eventId)
        then:
        1 * eventRepository.findByEventId(eventId) >> null
        searchedEvent == null
    }
}
