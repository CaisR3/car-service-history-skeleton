package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.ServiceContract
import com.template.ServiceState
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
object ServiceFlow {
    class Initiator(val registration: String, val description: String) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            // Flow implementation goes here
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            val builder = TransactionBuilder(notary)

            val service = serviceHub.vaultService.queryBy<ServiceState>().states
                    .singleOrNull { it.state.data.registration == registration } ?: throw FlowException("No service record found")

            val newService = service.state.data.copy(description = description)
            val owner = service.state.data.owner

            builder
                    .addInputState(service)
                    .addOutputState(newService, ServiceContract.ID)
                    .addCommand(ServiceContract.Commands.Service(), ourIdentity.owningKey, owner.owningKey)

            val ptx = serviceHub.signInitialTransaction(builder)

            val session = initiateFlow(owner)

            val stx = subFlow(CollectSignaturesFlow(ptx, listOf(session)))

            return subFlow(FinalityFlow(stx))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {

                }
            }
        }
    }
}