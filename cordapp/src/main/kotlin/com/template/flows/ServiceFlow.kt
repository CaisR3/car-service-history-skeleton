package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.ServiceContract
import com.template.ServiceState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class ServiceFlow(val registration: String, val servicesProvided: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val serviceStates = serviceHub.vaultService.queryBy<ServiceState>().states
        val existingServiceStateRef = serviceStates.singleOrNull { it.state.data.registration == registration }
                ?: throw FlowException("No service record found")
        val existingServiceState = existingServiceStateRef.state.data
        val updatedServiceState = existingServiceState.copy(serviced = true, servicesProvided = servicesProvided)

        val command = Command(ServiceContract.Commands.Service(), ourIdentity.owningKey)

        val transactionBuilder = TransactionBuilder(existingServiceStateRef.state.notary)
                .addInputState(existingServiceStateRef)
                .addOutputState(updatedServiceState, ServiceContract.ID)
                .addCommand(command)

        val signedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        return subFlow(FinalityFlow(signedTransaction))
    }
}