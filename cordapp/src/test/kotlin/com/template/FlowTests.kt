package com.template

import com.template.flows.RequestFlow
import com.template.flows.ServiceFlow
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FlowTests {
    private val network = MockNetwork(listOf("com.template"))
    private val a = network.createNode()
    private val b = network.createNode()
    private val partyA = a.info.legalIdentities[0]
    private val partyB = b.info.legalIdentities[0]

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `golden path request`() {
        val registration = "KR60 LWT"
        val requestFlowFuture = a.startFlow(RequestFlow(partyB, registration))
        network.runNetwork()
        val requestTransaction = requestFlowFuture.getOrThrow()

        val ledgerTransaction = requestTransaction.toLedgerTransaction(a.services)
        assertEquals(0, ledgerTransaction.inputStates.size)
        assertEquals(1, ledgerTransaction.commands.size)
        assertEquals(1, ledgerTransaction.outputStates.size)

        val command = ledgerTransaction.commands[0]
        assertEquals(ServiceContract.Commands.Request::class, command.value::class)
        assertEquals(1, command.signers.size)
        assertEquals(partyA.owningKey, command.signers[0])

        val output = ledgerTransaction.outputStates[0]
        assertEquals(ServiceState::class, output::class)
        val serviceStateOutput = output as ServiceState
        assertEquals(partyA, serviceStateOutput.owner)
        assertEquals(partyB, serviceStateOutput.mechanic)
        assertEquals(registration, serviceStateOutput.registration)
        assertEquals(false, serviceStateOutput.serviced)
        assertEquals(null, serviceStateOutput.servicesProvided)
    }

    @Test
    fun `golden path service`() {
        val registration = "KR60 LWT"
        val servicesProvided = "Oil filter changed."

        val requestFlowFuture = a.startFlow(RequestFlow(partyB, registration))
        network.runNetwork()
        requestFlowFuture.getOrThrow()

        val serviceFlowFuture = b.startFlow(ServiceFlow(registration, servicesProvided))
        network.runNetwork()
        val serviceTransaction = serviceFlowFuture.getOrThrow()

        val ledgerTransaction = serviceTransaction.toLedgerTransaction(a.services)
        assertEquals(1, ledgerTransaction.inputStates.size)
        assertEquals(1, ledgerTransaction.commands.size)
        assertEquals(1, ledgerTransaction.outputStates.size)

        val input = ledgerTransaction.inputStates[0]
        assertEquals(ServiceState::class, input::class)
        val serviceStateInput = input as ServiceState
        assertEquals(partyA, serviceStateInput.owner)
        assertEquals(partyB, serviceStateInput.mechanic)
        assertEquals(registration, serviceStateInput.registration)
        assertEquals(false, serviceStateInput.serviced)
        assertEquals(null, serviceStateInput.servicesProvided)

        val command = ledgerTransaction.commands[0]
        assertEquals(ServiceContract.Commands.Service::class, command.value::class)
        assertEquals(1, command.signers.size)
        assertEquals(partyB.owningKey, command.signers[0])

        val output = ledgerTransaction.outputStates[0]
        assertEquals(ServiceState::class, output::class)
        val serviceStateOutput = output as ServiceState
        assertEquals(partyA, serviceStateOutput.owner)
        assertEquals(partyB, serviceStateOutput.mechanic)
        assertEquals(registration, serviceStateOutput.registration)
        assertEquals(true, serviceStateOutput.serviced)
        assertEquals(servicesProvided, serviceStateOutput.servicesProvided)
    }
}